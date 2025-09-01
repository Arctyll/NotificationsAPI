package com.arctyll.notificationsapi.util;

import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL2.*;

public class RenderUtils {

    private static long vg;
    private static final Map<ResourceLocation, Integer> loadedImages = new HashMap<>();

    public static void initNanoVG() {
        if (vg == 0) {
            vg = nvgCreate(NanoVGGL2.NVG_ANTIALIAS | NanoVGGL2.NVG_STENCIL_STROKES);
            if (vg == 0) {
                throw new IllegalStateException("Failed to create NanoVG context");
            }
        }
    }

    public static void beginFrame(int width, int height) {
        if (vg != 0) {
            nvgBeginFrame(vg, width, height, 1.0f);
        }
    }

    public static void endFrame() {
        if (vg != 0) {
            nvgEndFrame(vg);
        }
    }

    public static void drawRect(float x, float y, float w, float h, int color) {
        NVGColor nvgColor = NVGColor.create();
        decodeColor(color, nvgColor);
        nvgBeginPath(vg);
        nvgRect(vg, x, y, w, h);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public static void drawRoundedRect(float x, float y, float w, float h, float radius, int color) {
        NVGColor nvgColor = NVGColor.create();
        decodeColor(color, nvgColor);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, w, h, radius);
        nvgFillColor(vg, nvgColor);
        nvgFill(vg);
    }

    public static void drawText(float x, float y, String text, float size, int color) {
        NVGColor nvgColor = NVGColor.create();
        decodeColor(color, nvgColor);
        nvgFontSize(vg, size);
        nvgFontFace(vg, "default");
        nvgFillColor(vg, nvgColor);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgText(vg, x, y, text);
    }

	public static void drawWrappedText(float x, float y, String text, float maxWidth, float size, int color) {
		if (text == null || text.isEmpty()) return;
		NVGColor nvgColor = NVGColor.create();
		decodeColor(color, nvgColor);
		nvgFontSize(vg, size);
		nvgFontFace(vg, "default");
		nvgFillColor(vg, nvgColor);
		nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
		nvgTextBox(vg, x, y, maxWidth, text);
	}

	public static float[] measureWrappedText(String text, float maxWidth, float fontSize) {
		if (text == null || text.isEmpty()) return new float[]{0, 0, 0, 0};
		float[] bounds = new float[4];
		nvgFontSize(vg, fontSize);
		nvgFontFace(vg, "default");
		nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
		nvgTextBoxBounds(vg, 0, 0, maxWidth, text, bounds);
		return bounds;
	}

    public static void drawIcon(ResourceLocation icon, float x, float y, float size, int color) {
        if (icon == null || vg == 0) return;

        int imageHandle = getOrLoadImage(icon);
        if (imageHandle == -1) return;

        NVGColor tintColor = NVGColor.create();
        decodeColor(color, tintColor);

        NVGPaint imgPaint = NVGPaint.create();
        nvgImagePattern(vg, x, y, size, size, 0.0f, imageHandle, 1.0f, imgPaint);

        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, size, size, size * 0.5f);
        nvgFillPaint(vg, imgPaint);
        nvgFill(vg);

        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, size, size, size * 0.5f);
        nvgFillColor(vg, tintColor);
        nvgGlobalCompositeBlendFuncSeparate(vg, NVG_DST_COLOR, NVG_ONE_MINUS_SRC_ALPHA, NVG_ONE, NVG_ONE_MINUS_SRC_ALPHA);
        nvgFill(vg);
        nvgGlobalCompositeOperation(vg, NVG_SOURCE_OVER);
    }

    public static void drawIcon(ResourceLocation icon, float x, float y, float size) {
        drawIcon(icon, x, y, size, 0xFFFFFFFF);
    }

    private static int getOrLoadImage(ResourceLocation resourceLocation) {
        if (loadedImages.containsKey(resourceLocation)) {
            return loadedImages.get(resourceLocation);
        }

        try {
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
            textureManager.bindTexture(resourceLocation);

            java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(
                Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream());

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            ByteBuffer imageBuffer = ByteBuffer.allocateDirect(width * height * 4);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = bufferedImage.getRGB(x, y);
                    imageBuffer.put((byte) ((pixel >> 16) & 0xFF));
                    imageBuffer.put((byte) ((pixel >> 8) & 0xFF));
                    imageBuffer.put((byte) (pixel & 0xFF));
                    imageBuffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            imageBuffer.flip();

            int imageHandle = nvgCreateImageRGBA(vg, width, height, 0, imageBuffer);

            if (imageHandle != -1) {
                loadedImages.put(resourceLocation, imageHandle);
            }

            return imageHandle;

        } catch (Exception e) {
            System.err.println("Failed to load image: " + resourceLocation + " - " + e.getMessage());
            return -1;
        }
    }

    public static void clearLoadedImages() {
        for (int imageHandle : loadedImages.values()) {
            if (imageHandle != -1) {
                nvgDeleteImage(vg, imageHandle);
            }
        }
        loadedImages.clear();
    }

    public static void loadDefaultFont(ByteBuffer fontBuffer) {
		int font = nvgCreateFontMem(vg, "default", fontBuffer, 0);
		if (font == -1) {
			throw new RuntimeException("Failed to load default font.");
		}
	}

    public static void destroyNanoVG() {
        clearLoadedImages();
        if (vg != 0) {
            nvgDelete(vg);
            vg = 0;
        }
    }

    public static long getVG() {
        return vg;
    }

    private static void decodeColor(int color, NVGColor out) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        out.r(r).g(g).b(b).a(a);
    }
}

package com.arctyll.notificationsapi.util;

import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.nanovg.NVGColor;

import java.nio.ByteBuffer;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL2.*;

public class RenderUtils {

    private static long vg;

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

    public static void loadDefaultFont(ByteBuffer fontBuffer) {
		int font = nvgCreateFontMem(vg, "default", fontBuffer, 0);
		if (font == -1) {
			throw new RuntimeException("Failed to load default font.");
		}
	}

    public static void destroyNanoVG() {
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

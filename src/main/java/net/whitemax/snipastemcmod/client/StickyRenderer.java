package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class StickyRenderer
{
    public static final int CLOSE_SIZE = 8;
    private static final int CLOSE_INSET = 12;

    private StickyRenderer()
    {
    }

    public static int closeX(StickyImage image)
    {
        return image.x() + image.width() - CLOSE_SIZE - CLOSE_INSET;
    }

    public static int closeY(StickyImage image)
    {
        return image.y() + CLOSE_INSET;
    }

    public static boolean overClose(StickyImage image, double mouseX, double mouseY)
    {
        int x = closeX(image);
        int y = closeY(image);
        return mouseX >= x && mouseX < x + CLOSE_SIZE && mouseY >= y && mouseY < y + CLOSE_SIZE;
    }

    public static void draw(GuiGraphics graphics, boolean editing, int selectedIndex)
    {
        for (int index = 0; index < StickyBoard.get().size(); index++)
        {
            StickyImage image = StickyBoard.get().get(index);
            shadow(graphics, image);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, image.opacity());
            graphics.blit(
                    image.location(),
                    image.x(),
                    image.y(),
                    image.width(),
                    image.height(),
                    0.0F,
                    0.0F,
                    image.sourceWidth(),
                    image.sourceHeight(),
                    image.sourceWidth(),
                    image.sourceHeight());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            frame(graphics, image);

            if (editing && index == selectedIndex)
            {
                border(graphics, image.x(), image.y(), image.width(), image.height(), 0xFFFFFFFF);
                handle(graphics, image.x(), image.y());
                handle(graphics, image.x() + image.width(), image.y());
                handle(graphics, image.x(), image.y() + image.height());
                handle(graphics, image.x() + image.width(), image.y() + image.height());
                handle(graphics, image.x() + image.width() / 2, image.y());
                handle(graphics, image.x() + image.width() / 2, image.y() + image.height());
                handle(graphics, image.x(), image.y() + image.height() / 2);
                handle(graphics, image.x() + image.width(), image.y() + image.height() / 2);
                int closeX = closeX(image);
                int closeY = closeY(image);
                graphics.fill(closeX, closeY, closeX + CLOSE_SIZE, closeY + CLOSE_SIZE, 0xE0C04040);
                closeGlyph(graphics, closeX, closeY);
            }
        }
    }

    private static void closeGlyph(GuiGraphics graphics, int x, int y)
    {
        int inset = 2;
        for (int i = inset; i < CLOSE_SIZE - inset; i++)
        {
            graphics.fill(x + i, y + i, x + i + 1, y + i + 1, 0xFFFFFFFF);
            graphics.fill(x + CLOSE_SIZE - 1 - i, y + i, x + CLOSE_SIZE - i, y + i + 1, 0xFFFFFFFF);
        }
    }

    private static void border(GuiGraphics graphics, int x, int y, int width, int height, int color)
    {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static void shadow(GuiGraphics graphics, StickyImage image)
    {
        if (!SnipasteConfig.shadowEnabled())
        {
            return;
        }
        float opacity = SnipasteConfig.shadowOpacity() * image.opacity();
        if (opacity <= 0.001F)
        {
            return;
        }
        double radians = Math.toRadians(SnipasteConfig.shadowAngle());
        float dx = (float) (-Math.cos(radians) * SnipasteConfig.shadowDistance());
        float dy = (float) (Math.sin(radians) * SnipasteConfig.shadowDistance());
        float spread = SnipasteConfig.shadowSpread();
        float blur = SnipasteConfig.shadowBlur();
        int rgb = SnipasteConfig.shadowRgb();
        float left = image.x() + dx - spread;
        float top = image.y() + dy - spread;
        float right = image.x() + image.width() + dx + spread;
        float bottom = image.y() + image.height() + dy + spread;
        if (blur < 0.5F)
        {
            fill(graphics, left, top, right, bottom, color(opacity, rgb));
            return;
        }
        int steps = Mth.clamp(Math.round(blur), 1, 12);
        float layerAlpha = (float) (1.0D - Math.pow(1.0D - Mth.clamp(opacity, 0.0F, 1.0F), 1.0D / steps));
        for (int step = steps; step >= 1; step--)
        {
            float expand = blur * step / steps;
            fill(graphics, left - expand, top - expand, right + expand, bottom + expand, color(layerAlpha, rgb));
        }
    }

    private static int color(float alpha, int rgb)
    {
        int channel = Math.round(Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
        return (channel << 24) | (rgb & 0xFFFFFF);
    }

    private static void frame(GuiGraphics graphics, StickyImage image)
    {
        if (!SnipasteConfig.borderEnabled())
        {
            return;
        }
        float thickness = (float) SnipasteConfig.borderWidth();
        int alpha = Math.round(Mth.clamp(image.opacity(), 0.0F, 1.0F) * 255.0F);
        float left = image.x() - thickness;
        float top = image.y() - thickness;
        float right = image.x() + image.width() + thickness;
        float bottom = image.y() + image.height() + thickness;
        if (!SnipasteConfig.borderRainbow())
        {
            int color = (alpha << 24) | SnipasteConfig.borderRgb();
            fill(graphics, left, top, right, image.y(), color);
            fill(graphics, left, image.y() + image.height(), right, bottom, color);
            fill(graphics, left, image.y(), image.x(), image.y() + image.height(), color);
            fill(graphics, image.x() + image.width(), image.y(), right, image.y() + image.height(), color);
            return;
        }
        float outerWidth = right - left;
        float outerHeight = bottom - top;
        float perimeter = Math.max(1.0F, 2.0F * (outerWidth + outerHeight));
        float shift = (Util.getMillis() % 4000L) / 4000.0F;
        float step = 4.0F;
        float traveled = 0.0F;
        traveled = paintSpan(graphics, left, top, outerWidth, thickness, true, true, traveled, perimeter, shift, alpha, step);
        traveled = paintSpan(graphics, right - thickness, top + thickness, thickness, image.height(), false, true, traveled, perimeter, shift, alpha, step);
        traveled = paintSpan(graphics, right, bottom - thickness, outerWidth, thickness, true, false, traveled, perimeter, shift, alpha, step);
        paintSpan(graphics, left, bottom - thickness, thickness, image.height(), false, false, traveled, perimeter, shift, alpha, step);
    }

    private static float paintSpan(GuiGraphics graphics, float x, float y, float spanWidth, float spanHeight, boolean horizontal, boolean forward, float traveled, float perimeter, float shift, int alpha, float step)
    {
        float length = horizontal ? spanWidth : spanHeight;
        if (length <= 0.0F || spanWidth <= 0.0F || spanHeight <= 0.0F)
        {
            return traveled;
        }
        for (float offset = 0.0F; offset < length; offset += step)
        {
            float size = Math.min(step, length - offset);
            float along = forward ? offset : length - offset - size;
            float hue = shift + (traveled + offset) / perimeter;
            int color = hueColor(hue, alpha);
            if (horizontal)
            {
                float drawX = forward ? x + along : x - length + along;
                fill(graphics, drawX, y, drawX + size, y + spanHeight, color);
            }
            else
            {
                float drawY = forward ? y + along : y - length + along;
                fill(graphics, x, drawY, x + spanWidth, drawY + size, color);
            }
        }
        return traveled + length;
    }

    private static void fill(GuiGraphics graphics, float x0, float y0, float x1, float y1, int color)
    {
        if (x0 > x1)
        {
            float swap = x0;
            x0 = x1;
            x1 = swap;
        }
        if (y0 > y1)
        {
            float swap = y0;
            y0 = y1;
            y1 = swap;
        }
        float alpha = (color >>> 24 & 255) / 255.0F;
        float red = (color >>> 16 & 255) / 255.0F;
        float green = (color >>> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, x0, y1, 0.0F).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y1, 0.0F).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y0, 0.0F).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x0, y0, 0.0F).color(red, green, blue, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    private static int hueColor(float hue, int alpha)
    {
        float wrapped = hue - (float) Math.floor(hue);
        int rgb = java.awt.Color.HSBtoRGB(wrapped, 1.0F, 1.0F) & 0xFFFFFF;
        return (alpha << 24) | rgb;
    }

    private static void handle(GuiGraphics graphics, int centerX, int centerY)
    {
        graphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFF111111);
        graphics.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, 0xFFFFFFFF);
    }
}

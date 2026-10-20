package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class StickyRenderer
{
    private StickyRenderer()
    {
    }

    public static void draw(GuiGraphics graphics, boolean editing, int selectedIndex)
    {
        for (int index = 0; index < StickyBoard.get().size(); index++)
        {
            StickyImage image = StickyBoard.get().get(index);
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
                int close = 12;
                graphics.fill(image.x() + image.width() - close, image.y(), image.x() + image.width(), image.y() + close, 0xE0C04040);
                graphics.drawString(
                        Minecraft.getInstance().font,
                        "x",
                        image.x() + image.width() - close + 4,
                        image.y() + 2,
                        0xFFFFFFFF,
                        false);
            }
        }
    }

    private static void border(GuiGraphics graphics, int x, int y, int width, int height, int color)
    {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static void handle(GuiGraphics graphics, int centerX, int centerY)
    {
        graphics.fill(centerX - 3, centerY - 3, centerX + 3, centerY + 3, 0xFF111111);
        graphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFFFF);
    }
}

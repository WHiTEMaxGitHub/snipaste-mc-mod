package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class SnipasteColorScreen extends Screen
{
    public interface Apply
    {
        void set(int red, int green, int blue, int alpha);
    }

    private final Screen parent;
    private final Apply apply;
    private final int[] channels = new int[4];
    private float hue;
    private float saturation = 1.0F;
    private float value = 1.0F;
    private final EditBox[] boxes = new EditBox[4];
    private int squareX;
    private int squareY;
    private int squareSize;
    private int hueX;
    private int hueWidth;
    private int alphaX;
    private int alphaWidth;
    private boolean dragSquare;
    private boolean dragHue;
    private boolean dragAlpha;

    public SnipasteColorScreen(Screen parent, Component title, int rgb, int alpha, Apply apply)
    {
        super(title);
        this.parent = parent;
        this.apply = apply;
        readRgb(rgb);
        this.channels[3] = clampByte(alpha);
    }

    @Override
    protected void init()
    {
        this.hueWidth = 14;
        this.alphaWidth = 14;
        this.squareSize = Math.min(128, Math.max(72, Math.min(this.width - 108, this.height - 110)));
        int group = this.squareSize + 10 + this.hueWidth + 8 + this.alphaWidth;
        this.squareX = (this.width - group) / 2;
        this.squareY = 36;
        this.hueX = this.squareX + this.squareSize + 10;
        this.alphaX = this.hueX + this.hueWidth + 8;
        int y = this.squareY + this.squareSize + 28;
        int boxWidth = 40;
        int gap = 8;
        int rowWidth = 0;
        int[] nameWidths = new int[4];
        for (int channel = 0; channel < 4; channel++)
        {
            nameWidths[channel] = this.font.width(channelName(channel));
            rowWidth += nameWidths[channel] + 4 + boxWidth;
        }
        rowWidth += gap * 3;
        int left = Math.max(8, (this.width - rowWidth) / 2);
        int x = left;
        for (int channel = 0; channel < 4; channel++)
        {
            this.boxes[channel] = channelBox(x + nameWidths[channel] + 4, y, boxWidth, channel);
            x += nameWidths[channel] + 4 + boxWidth + gap;
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public void onClose()
    {
        commitBoxes();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void tick()
    {
        syncBoxes(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (super.mouseClicked(mouseX, mouseY, button))
        {
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            return false;
        }
        if (insideSquare(mouseX, mouseY))
        {
            this.dragSquare = true;
            pickSquare(mouseX, mouseY);
            return true;
        }
        if (insideHue(mouseX, mouseY))
        {
            this.dragHue = true;
            pickHue(mouseY);
            return true;
        }
        if (insideAlpha(mouseX, mouseY))
        {
            this.dragAlpha = true;
            pickAlpha(mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (this.dragSquare)
        {
            pickSquare(mouseX, mouseY);
            return true;
        }
        if (this.dragHue)
        {
            pickHue(mouseY);
            return true;
        }
        if (this.dragAlpha)
        {
            pickAlpha(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        this.dragSquare = false;
        this.dragHue = false;
        this.dragAlpha = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.color_hint"), this.width / 2, 24, 0xA0A0A0);
        paintField(graphics);
        paintHue(graphics);
        paintAlpha(graphics);
        paintMarker(graphics);
        int previewY = this.squareY + this.squareSize + 6;
        paintColor(graphics, this.squareX, previewY, this.alphaX + this.alphaWidth - this.squareX, 16, currentRgb(), this.channels[3]);
        if (this.boxes[0] != null)
        {
            for (int channel = 0; channel < 4; channel++)
            {
                String name = channelName(channel);
                drawPlain(graphics, name, this.boxes[channel].getX() - this.font.width(name) - 4, this.boxes[channel].getY());
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public static void paintSwatch(GuiGraphics graphics, int x, int y, int width, int height, int rgb, int alpha)
    {
        graphics.fill(x, y, x + width, y + height, 0xFF000000);
        paintColor(graphics, x + 1, y + 1, width - 2, height - 2, rgb, alpha);
    }

    private void paintField(GuiGraphics graphics)
    {
        int step = 2;
        for (int y = 0; y < this.squareSize; y += step)
        {
            float brightness = 1.0F - y / (float) Math.max(1, this.squareSize - 1);
            for (int x = 0; x < this.squareSize; x += step)
            {
                float saturation = x / (float) Math.max(1, this.squareSize - 1);
                int color = 0xFF000000 | Color.HSBtoRGB(this.hue, saturation, brightness);
                graphics.fill(this.squareX + x, this.squareY + y, this.squareX + x + step, this.squareY + y + step, color);
            }
        }
        frame(graphics, this.squareX, this.squareY, this.squareSize, this.squareSize);
    }

    private void paintHue(GuiGraphics graphics)
    {
        for (int y = 0; y < this.squareSize; y++)
        {
            float hue = y / (float) Math.max(1, this.squareSize - 1);
            graphics.fill(this.hueX, this.squareY + y, this.hueX + this.hueWidth, this.squareY + y + 1, 0xFF000000 | Color.HSBtoRGB(hue, 1.0F, 1.0F));
        }
        frame(graphics, this.hueX, this.squareY, this.hueWidth, this.squareSize);
    }

    private void paintAlpha(GuiGraphics graphics)
    {
        paintColorColumn(graphics, this.alphaX, this.squareY, this.alphaWidth, this.squareSize);
        frame(graphics, this.alphaX, this.squareY, this.alphaWidth, this.squareSize);
    }

    private void paintMarker(GuiGraphics graphics)
    {
        int x = this.squareX + Math.round(this.saturation * (this.squareSize - 1));
        int y = this.squareY + Math.round((1.0F - this.value) * (this.squareSize - 1));
        graphics.fill(x - 3, y - 3, x + 4, y + 4, 0xFF000000);
        graphics.fill(x - 2, y - 2, x + 3, y + 3, 0xFFFFFFFF);
        graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xFF000000 | currentRgb());
        int hueY = this.squareY + Math.round(this.hue * (this.squareSize - 1));
        graphics.fill(this.hueX - 2, hueY - 1, this.hueX + this.hueWidth + 2, hueY + 2, 0xFFFFFFFF);
        graphics.fill(this.hueX - 1, hueY, this.hueX + this.hueWidth + 1, hueY + 1, 0xFF000000);
        int alphaY = this.squareY + Math.round((1.0F - this.channels[3] / 255.0F) * (this.squareSize - 1));
        graphics.fill(this.alphaX - 2, alphaY - 1, this.alphaX + this.alphaWidth + 2, alphaY + 2, 0xFFFFFFFF);
        graphics.fill(this.alphaX - 1, alphaY, this.alphaX + this.alphaWidth + 1, alphaY + 1, 0xFF000000);
    }

    private void pickSquare(double mouseX, double mouseY)
    {
        float saturation = (float) ((mouseX - this.squareX) / Math.max(1, this.squareSize - 1));
        float brightness = 1.0F - (float) ((mouseY - this.squareY) / Math.max(1, this.squareSize - 1));
        this.saturation = clamp01(saturation);
        this.value = clamp01(brightness);
        writeHsv();
    }

    private void pickHue(double mouseY)
    {
        this.hue = clamp01((float) ((mouseY - this.squareY) / Math.max(1, this.squareSize - 1)));
        writeHsv();
    }

    private void pickAlpha(double mouseY)
    {
        float down = clamp01((float) ((mouseY - this.squareY) / Math.max(1, this.squareSize - 1)));
        this.channels[3] = Math.round((1.0F - down) * 255.0F);
        publish();
        showBoxes();
    }

    private void writeHsv()
    {
        int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.value);
        this.channels[0] = (rgb >> 16) & 255;
        this.channels[1] = (rgb >> 8) & 255;
        this.channels[2] = rgb & 255;
        publish();
        showBoxes();
    }

    private void readRgb(int rgb)
    {
        this.channels[0] = (rgb >> 16) & 255;
        this.channels[1] = (rgb >> 8) & 255;
        this.channels[2] = rgb & 255;
        float[] hsb = Color.RGBtoHSB(this.channels[0], this.channels[1], this.channels[2], null);
        if (hsb[1] > 0.001F)
        {
            this.hue = hsb[0];
        }
        this.saturation = hsb[1];
        this.value = hsb[2];
    }

    private void syncBoxes(boolean force)
    {
        boolean focused = false;
        for (EditBox box : this.boxes)
        {
            if (box == null)
            {
                return;
            }
            box.tick();
            focused |= box.isFocused();
        }
        if (focused || force)
        {
            boolean complete = true;
            int[] parsed = new int[4];
            for (int channel = 0; channel < 4; channel++)
            {
                Double number = NumberBoxes.parse(this.boxes[channel].getValue());
                if (number == null)
                {
                    complete = false;
                    break;
                }
                parsed[channel] = (int) Math.round(number);
            }
            if (complete && (focused && !force ? inRange(parsed) : true))
            {
                for (int channel = 0; channel < 4; channel++)
                {
                    this.channels[channel] = clampByte(parsed[channel]);
                }
                float[] hsb = Color.RGBtoHSB(this.channels[0], this.channels[1], this.channels[2], null);
                if (hsb[1] > 0.001F)
                {
                    this.hue = hsb[0];
                }
                this.saturation = hsb[1];
                this.value = hsb[2];
                publish();
            }
        }
        if (!focused || force)
        {
            showBoxes();
        }
    }

    private void commitBoxes()
    {
        syncBoxes(true);
    }

    private void showBoxes()
    {
        for (int channel = 0; channel < 4; channel++)
        {
            NumberBoxes.show(this.boxes[channel], String.valueOf(this.channels[channel]));
        }
    }

    private EditBox channelBox(int x, int y, int width, int channel)
    {
        EditBox box = new EditBox(this.font, x, y, width, 20, Component.empty());
        box.setMaxLength(3);
        box.setValue(String.valueOf(this.channels[channel]));
        box.setFilter(NumberBoxes::acceptsInteger);
        return addRenderableWidget(box);
    }

    private boolean insideSquare(double mouseX, double mouseY)
    {
        return mouseX >= this.squareX && mouseX < this.squareX + this.squareSize
                && mouseY >= this.squareY && mouseY < this.squareY + this.squareSize;
    }

    private boolean insideHue(double mouseX, double mouseY)
    {
        return mouseX >= this.hueX && mouseX < this.hueX + this.hueWidth
                && mouseY >= this.squareY && mouseY < this.squareY + this.squareSize;
    }

    private boolean insideAlpha(double mouseX, double mouseY)
    {
        return mouseX >= this.alphaX && mouseX < this.alphaX + this.alphaWidth
                && mouseY >= this.squareY && mouseY < this.squareY + this.squareSize;
    }

    private void publish()
    {
        this.apply.set(this.channels[0], this.channels[1], this.channels[2], this.channels[3]);
    }

    private int currentRgb()
    {
        return (this.channels[0] << 16) | (this.channels[1] << 8) | this.channels[2];
    }

    private void drawPlain(GuiGraphics graphics, String text, int x, int y)
    {
        graphics.drawString(this.font, text, x, y + 6, 0xFFFFFF, false);
    }

    private static void frame(GuiGraphics graphics, int x, int y, int width, int height)
    {
        graphics.fill(x - 1, y - 1, x + width + 1, y, 0xFFFFFFFF);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, 0xFFFFFFFF);
        graphics.fill(x - 1, y, x, y + height, 0xFFFFFFFF);
        graphics.fill(x + width, y, x + width + 1, y + height, 0xFFFFFFFF);
    }

    private static void paintColor(GuiGraphics graphics, int x, int y, int width, int height, int rgb, int alpha)
    {
        if (width <= 0 || height <= 0)
        {
            return;
        }
        int cell = 4;
        for (int row = 0; row < height; row += cell)
        {
            int rowHeight = Math.min(cell, height - row);
            for (int column = 0; column < width; column += cell)
            {
                int columnWidth = Math.min(cell, width - column);
                int under = checker(column, row) ? 0xFFC8C8C8 : 0xFF787878;
                graphics.fill(x + column, y + row, x + column + columnWidth, y + row + rowHeight, blend(under, rgb, alpha));
            }
        }
    }

    private void paintColorColumn(GuiGraphics graphics, int x, int y, int width, int height)
    {
        int cell = 4;
        int rgb = currentRgb();
        for (int row = 0; row < height; row += cell)
        {
            int rowHeight = Math.min(cell, height - row);
            float down = (row + rowHeight * 0.5F) / Math.max(1, height - 1);
            int alpha = Math.round((1.0F - clamp01(down)) * 255.0F);
            for (int column = 0; column < width; column += cell)
            {
                int columnWidth = Math.min(cell, width - column);
                int under = checker(column, row) ? 0xFFC8C8C8 : 0xFF787878;
                graphics.fill(x + column, y + row, x + column + columnWidth, y + row + rowHeight, blend(under, rgb, alpha));
            }
        }
    }

    private static boolean checker(int x, int y)
    {
        return ((x / 4) + (y / 4)) % 2 == 0;
    }

    private static int blend(int under, int rgb, int alpha)
    {
        int amount = clampByte(alpha);
        int red = mix((under >> 16) & 255, (rgb >> 16) & 255, amount);
        int green = mix((under >> 8) & 255, (rgb >> 8) & 255, amount);
        int blue = mix(under & 255, rgb & 255, amount);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int mix(int under, int over, int alpha)
    {
        return under + (over - under) * alpha / 255;
    }

    private static int clampByte(int value)
    {
        return Math.max(0, Math.min(255, value));
    }

    private static boolean inRange(int[] channels)
    {
        for (int channel : channels)
        {
            if (channel < 0 || channel > 255)
            {
                return false;
            }
        }
        return true;
    }

    private static float clamp01(float value)
    {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static String channelName(int channel)
    {
        String key = switch (channel)
        {
            case 0 -> "screen.snipaste_mc_mod.channel_red";
            case 1 -> "screen.snipaste_mc_mod.channel_green";
            case 2 -> "screen.snipaste_mc_mod.channel_blue";
            default -> "screen.snipaste_mc_mod.channel_alpha";
        };
        return Component.translatable(key).getString();
    }
}

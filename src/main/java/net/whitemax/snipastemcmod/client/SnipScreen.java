package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.whitemax.snipastemcmod.SnipasteMcMod;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

public class SnipScreen extends Screen
{
    private final NativeImage image;
    private DynamicTexture texture;
    private ResourceLocation textureId;
    private boolean dragging;
    private double anchorX;
    private double anchorY;
    private double currentX;
    private double currentY;
    private Component error;
    private boolean typing;
    private String text = "";
    private int cursor;
    private int boxX;
    private int boxY;

    public SnipScreen(NativeImage image)
    {
        super(Component.translatable("screen.snipaste_mc_mod.snip"));
        this.image = image;
    }

    @Override
    protected void init()
    {
        if (this.textureId != null)
        {
            return;
        }
        this.texture = new DynamicTexture(this.image);
        this.textureId = this.minecraft.getTextureManager().register(SnipasteMcMod.MOD_ID + "/snip_frame", this.texture);
    }

    @Override
    public void removed()
    {
        if (this.texture != null)
        {
            this.texture.close();
            this.texture = null;
        }
        if (this.textureId != null && this.minecraft != null)
        {
            this.minecraft.getTextureManager().release(this.textureId);
            this.textureId = null;
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        if (this.textureId != null)
        {
            graphics.blit(
                    this.textureId,
                    0,
                    0,
                    this.width,
                    this.height,
                    0.0F,
                    0.0F,
                    this.image.getWidth(),
                    this.image.getHeight(),
                    this.image.getWidth(),
                    this.image.getHeight());
        }

        if (this.typing)
        {
            renderTextBox(graphics);
        }
        else if (this.dragging)
        {
            int left = (int) Math.min(this.anchorX, this.currentX);
            int top = (int) Math.min(this.anchorY, this.currentY);
            int right = (int) Math.max(this.anchorX, this.currentX);
            int bottom = (int) Math.max(this.anchorY, this.currentY);
            dimOutside(graphics, left, top, right, bottom);
            graphics.fill(left, top, right, top + 1, 0xFFFFFFFF);
            graphics.fill(left, bottom - 1, right, bottom, 0xFFFFFFFF);
            graphics.fill(left, top, left + 1, bottom, 0xFFFFFFFF);
            graphics.fill(right - 1, top, right, bottom, 0xFFFFFFFF);

            int imageWidth = imageWidth(left, right);
            int imageHeight = imageHeight(top, bottom);
            String size = imageWidth + " × " + imageHeight;
            int textWidth = this.font.width(size);
            int textX = Mth.clamp(right + 8, 4, this.width - textWidth - 8);
            int textY = Mth.clamp(bottom + 8, 4, this.height - 14);
            graphics.fill(textX - 3, textY - 2, textX + textWidth + 3, textY + 11, 0xC0000000);
            graphics.drawString(this.font, size, textX, textY, 0xFFFFFFFF, false);
        }
        else
        {
            graphics.fill(0, 0, this.width, 28, 0x88000000);
            graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.hint_snip"), this.width / 2, 6, 0xFFFFFFFF);
            graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.hint_text_entry"), this.width / 2, 16, 0xFFDDDDDD);
        }

        if (this.error != null)
        {
            graphics.drawCenteredString(this.font, this.error, this.width / 2, this.height - 24, 0xFFFF6666);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (this.typing)
        {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.onClose();
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            return false;
        }
        this.dragging = true;
        this.anchorX = mouseX;
        this.anchorY = mouseY;
        this.currentX = mouseX;
        this.currentY = mouseY;
        this.error = null;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (!this.dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            return false;
        }
        this.currentX = mouseX;
        this.currentY = mouseY;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (this.typing || !this.dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            return false;
        }
        this.currentX = mouseX;
        this.currentY = mouseY;
        this.dragging = false;
        confirm();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (this.typing)
        {
            return typeKey(keyCode, modifiers);
        }
        if (SnipasteKeybinds.SNIP_CHORD.matches(keyCode, scanCode, modifiers))
        {
            beginText();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (!this.typing || (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT)) != 0)
        {
            return this.typing || super.charTyped(codePoint, modifiers);
        }
        if (codePoint >= 32)
        {
            insert(String.valueOf(codePoint));
        }
        return true;
    }

    private void beginText()
    {
        this.typing = true;
        this.text = "";
        this.cursor = 0;
        this.dragging = false;
        this.error = null;
        long window = this.minecraft.getWindow().getWindow();
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        double scale = this.minecraft.getWindow().getGuiScale();
        this.boxX = Mth.clamp((int) Math.round(x[0] / scale), 8, Math.max(8, this.width - 80));
        this.boxY = Mth.clamp((int) Math.round(y[1] / scale), 8, Math.max(8, this.height - 24));
    }

    private boolean typeKey(int keyCode, int modifiers)
    {
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean control = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            this.typing = false;
            return true;
        }
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && shift)
        {
            insert("\n");
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            commitText();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE)
        {
            deleteBackward();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE)
        {
            deleteForward();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT)
        {
            moveCursor(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT)
        {
            moveCursor(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP)
        {
            moveLine(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN)
        {
            moveLine(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME)
        {
            this.cursor = lineStart(this.cursor);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END)
        {
            this.cursor = lineEnd(this.cursor);
            return true;
        }
        if (control && keyCode == GLFW.GLFW_KEY_V)
        {
            String clip = TextStickers.clipboard();
            if (clip != null)
            {
                insert(clip);
            }
            return true;
        }
        return true;
    }

    private void commitText()
    {
        if (this.text.isBlank())
        {
            this.typing = false;
            return;
        }
        Path saved = ImageFolder.saveText(this.text);
        if (saved == null || !StickyBoard.get().pasteFile(saved))
        {
            this.error = Component.translatable("message.snipaste_mc_mod.text_failed");
            return;
        }
        if (this.minecraft != null)
        {
            this.minecraft.setScreen(null);
        }
        SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.saved"));
    }

    private void insert(String extra)
    {
        if (extra == null || extra.isEmpty() || this.text.length() + extra.length() > 2000)
        {
            return;
        }
        this.text = this.text.substring(0, this.cursor) + extra + this.text.substring(this.cursor);
        this.cursor += extra.length();
    }

    private void deleteBackward()
    {
        if (this.cursor <= 0)
        {
            return;
        }
        int previous = this.text.offsetByCodePoints(this.cursor, -1);
        this.text = this.text.substring(0, previous) + this.text.substring(this.cursor);
        this.cursor = previous;
    }

    private void deleteForward()
    {
        if (this.cursor >= this.text.length())
        {
            return;
        }
        int next = this.text.offsetByCodePoints(this.cursor, 1);
        this.text = this.text.substring(0, this.cursor) + this.text.substring(next);
    }

    private void moveCursor(int direction)
    {
        if (direction < 0 && this.cursor > 0)
        {
            this.cursor = this.text.offsetByCodePoints(this.cursor, -1);
        }
        else if (direction > 0 && this.cursor < this.text.length())
        {
            this.cursor = this.text.offsetByCodePoints(this.cursor, 1);
        }
    }

    private void moveLine(int direction)
    {
        int start = lineStart(this.cursor);
        int column = this.font.width(this.text.substring(start, this.cursor));
        int target = direction < 0 ? previousLineStart(start) : lineEnd(this.cursor) + 1;
        if (target < 0 || target > this.text.length())
        {
            return;
        }
        if (direction > 0 && target == this.text.length() && (this.text.isEmpty() || this.text.charAt(this.text.length() - 1) != '\n'))
        {
            return;
        }
        int end = lineEnd(Math.min(target, this.text.length()));
        int index = Math.min(target, this.text.length());
        while (index < end)
        {
            int next = this.text.offsetByCodePoints(index, 1);
            if (this.font.width(this.text.substring(Math.min(target, this.text.length()), next)) > column)
            {
                break;
            }
            index = next;
        }
        this.cursor = index;
    }

    private int lineStart(int index)
    {
        int start = this.text.lastIndexOf('\n', Math.max(0, index - 1));
        return start < 0 ? 0 : start + 1;
    }

    private int lineEnd(int index)
    {
        int end = this.text.indexOf('\n', index);
        return end < 0 ? this.text.length() : end;
    }

    private int previousLineStart(int start)
    {
        if (start <= 0)
        {
            return -1;
        }
        return lineStart(start - 1);
    }

    private void renderTextBox(GuiGraphics graphics)
    {
        String[] lines = this.text.split("\n", -1);
        int lineHeight = this.font.lineHeight;
        int contentWidth = 40;
        for (String line : lines)
        {
            contentWidth = Math.max(contentWidth, this.font.width(line));
        }
        int boxWidth = Math.min(this.width - 8, contentWidth + 12);
        int boxHeight = lines.length * lineHeight + 8;
        int x = Mth.clamp(this.boxX, 4, Math.max(4, this.width - boxWidth - 4));
        int y = Mth.clamp(this.boxY, 4, Math.max(4, this.height - boxHeight - 4));
        if (SnipasteConfig.textBackground())
        {
            graphics.fill(x, y, x + boxWidth, y + boxHeight, 0xD2000000 | SnipasteConfig.textBackgroundRgb());
        }
        graphics.fill(x, y, x + boxWidth, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + boxHeight, 0xFFFFFFFF);
        graphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, 0xFFFFFFFF);
        int color = 0xFF000000 | SnipasteConfig.textRgb();
        int lineY = y + 4;
        int remaining = this.cursor;
        for (String line : lines)
        {
            graphics.drawString(this.font, line, x + 6, lineY, color, false);
            if (remaining >= 0 && remaining <= line.length() && (Util.getMillis() / 500L) % 2L == 0L)
            {
                int caret = x + 6 + this.font.width(line.substring(0, remaining));
                graphics.fill(caret, lineY - 1, caret + 1, lineY + lineHeight - 1, color);
                remaining = -1;
            }
            else
            {
                remaining -= line.length() + 1;
            }
            lineY += lineHeight;
        }
        graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.hint_text"), this.width / 2, this.height - 16, 0xFFFFFFFF);
    }

    private void confirm()
    {
        int left = (int) Math.min(this.anchorX, this.currentX);
        int top = (int) Math.min(this.anchorY, this.currentY);
        int right = (int) Math.max(this.anchorX, this.currentX);
        int bottom = (int) Math.max(this.anchorY, this.currentY);
        int x = imageX(left);
        int y = imageY(top);
        int cropWidth = Math.min(imageWidth(left, right), this.image.getWidth() - x);
        int cropHeight = Math.min(imageHeight(top, bottom), this.image.getHeight() - y);
        if (cropWidth < 2 || cropHeight < 2)
        {
            return;
        }
        if (!ImageFolder.save(Pixels.crop(this.image, x, y, cropWidth, cropHeight)))
        {
            this.error = Component.translatable("message.snipaste_mc_mod.save_failed");
            return;
        }
        if (this.minecraft != null)
        {
            this.minecraft.setScreen(null);
        }
        SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.saved"));
    }

    private void dimOutside(GuiGraphics graphics, int left, int top, int right, int bottom)
    {
        int color = 0x99000000;
        graphics.fill(0, 0, this.width, top, color);
        graphics.fill(0, bottom, this.width, this.height, color);
        graphics.fill(0, top, left, bottom, color);
        graphics.fill(right, top, this.width, bottom, color);
    }

    private int imageX(int guiX)
    {
        return Mth.clamp((int) Math.round(guiX * (double) this.image.getWidth() / this.width), 0, this.image.getWidth());
    }

    private int imageY(int guiY)
    {
        return Mth.clamp((int) Math.round(guiY * (double) this.image.getHeight() / this.height), 0, this.image.getHeight());
    }

    private int imageWidth(int guiLeft, int guiRight)
    {
        return Mth.clamp(imageX(guiRight) - imageX(guiLeft), 0, this.image.getWidth());
    }

    private int imageHeight(int guiTop, int guiBottom)
    {
        return Mth.clamp(imageY(guiBottom) - imageY(guiTop), 0, this.image.getHeight());
    }
}

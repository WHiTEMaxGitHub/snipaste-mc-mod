package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.whitemax.snipastemcmod.SnipasteMcMod;
import org.lwjgl.glfw.GLFW;

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

        if (this.dragging)
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
            graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.hint_snip"), this.width / 2, 10, 0xFFFFFFFF);
        }

        if (this.error != null)
        {
            graphics.drawCenteredString(this.font, this.error, this.width / 2, this.height - 24, 0xFFFF6666);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
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
        if (!this.dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            return false;
        }
        this.currentX = mouseX;
        this.currentY = mouseY;
        this.dragging = false;
        confirm();
        return true;
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

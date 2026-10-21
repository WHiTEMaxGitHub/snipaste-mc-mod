package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.whitemax.snipastemcmod.SnipasteMcMod;
import org.lwjgl.glfw.GLFW;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImagePreviewScreen extends Screen
{
    private final Screen parent;
    private final Path path;
    private DynamicTexture texture;
    private ResourceLocation location;
    private int imageWidth;
    private int imageHeight;
    private int drawX;
    private int drawY;
    private int drawWidth;
    private int drawHeight;
    private long lastClickAt;

    public ImagePreviewScreen(Screen parent, Path path)
    {
        super(Component.literal(path.getFileName().toString()));
        this.parent = parent;
        this.path = path;
    }

    @Override
    protected void init()
    {
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.delete_short"), button -> deleteImage())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20)
                .build());
        if (this.location != null)
        {
            return;
        }
        try
        {
            BufferedImage image = ImageFolder.read(this.path);
            NativeImage nativeImage = Pixels.toNative(image);
            this.imageWidth = nativeImage.getWidth();
            this.imageHeight = nativeImage.getHeight();
            this.texture = new DynamicTexture(nativeImage);
            this.location = this.minecraft.getTextureManager().register(SnipasteMcMod.MOD_ID + "/pick_preview", this.texture);
        }
        catch (IOException | RuntimeException exception)
        {
            SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.read_failed"));
            onClose();
        }
    }

    @Override
    public void onClose()
    {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void removed()
    {
        if (this.texture != null)
        {
            this.texture.close();
            this.texture = null;
        }
        if (this.location != null && this.minecraft != null)
        {
            this.minecraft.getTextureManager().release(this.location);
            this.location = null;
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_SPACE)
        {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (super.mouseClicked(mouseX, mouseY, button))
        {
            this.lastClickAt = 0L;
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !overImage(mouseX, mouseY))
        {
            return false;
        }
        long now = Util.getMillis();
        if (now - this.lastClickAt < 250L)
        {
            this.lastClickAt = 0L;
            pasteDirect();
            return true;
        }
        this.lastClickAt = now;
        return true;
    }

    private void deleteImage()
    {
        DeleteImageScreen.ask(this.parent, this.path);
    }

    private void pasteDirect()
    {
        if (!StickyBoard.get().pasteFile(this.path))
        {
            return;
        }
        Screen destination = this.parent instanceof ImagePickScreen pick ? pick.previous() : this.parent;
        if (destination instanceof StickyEditScreen edit)
        {
            edit.selectLast();
        }
        this.minecraft.setScreen(destination);
    }

    private boolean overImage(double mouseX, double mouseY)
    {
        return this.drawWidth > 0
                && mouseX >= this.drawX
                && mouseY >= this.drawY
                && mouseX < this.drawX + this.drawWidth
                && mouseY < this.drawY + this.drawHeight;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        if (this.location != null && this.imageWidth > 0 && this.imageHeight > 0)
        {
            int maxWidth = Math.max(1, this.width - 28);
            int maxHeight = Math.max(1, this.height - 76);
            float scale = Math.min(maxWidth / (float) this.imageWidth, maxHeight / (float) this.imageHeight);
            this.drawWidth = Math.max(1, Math.round(this.imageWidth * scale));
            this.drawHeight = Math.max(1, Math.round(this.imageHeight * scale));
            this.drawX = (this.width - this.drawWidth) / 2;
            this.drawY = Math.max(8, (this.height - 52 - this.drawHeight) / 2);
            graphics.blit(
                    this.location,
                    this.drawX,
                    this.drawY,
                    this.drawWidth,
                    this.drawHeight,
                    0.0F,
                    0.0F,
                    this.imageWidth,
                    this.imageHeight,
                    this.imageWidth,
                    this.imageHeight);
            int border = 0xFFFFFFFF;
            graphics.fill(this.drawX - 1, this.drawY - 1, this.drawX + this.drawWidth + 1, this.drawY, border);
            graphics.fill(this.drawX - 1, this.drawY + this.drawHeight, this.drawX + this.drawWidth + 1, this.drawY + this.drawHeight + 1, border);
            graphics.fill(this.drawX - 1, this.drawY, this.drawX, this.drawY + this.drawHeight, border);
            graphics.fill(this.drawX + this.drawWidth, this.drawY, this.drawX + this.drawWidth + 1, this.drawY + this.drawHeight, border);
        }
        graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.preview_hint"), this.width / 2, this.height - 44, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

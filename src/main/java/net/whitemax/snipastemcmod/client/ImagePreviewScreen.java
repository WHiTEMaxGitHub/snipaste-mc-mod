package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
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

    public ImagePreviewScreen(Screen parent, Path path)
    {
        super(Component.literal(path.getFileName().toString()));
        this.parent = parent;
        this.path = path;
    }

    @Override
    protected void init()
    {
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        if (this.location != null && this.imageWidth > 0 && this.imageHeight > 0)
        {
            int maxWidth = Math.max(1, this.width - 24);
            int maxHeight = Math.max(1, this.height - 36);
            float scale = Math.min(maxWidth / (float) this.imageWidth, maxHeight / (float) this.imageHeight);
            int drawWidth = Math.max(1, Math.round(this.imageWidth * scale));
            int drawHeight = Math.max(1, Math.round(this.imageHeight * scale));
            int x = (this.width - drawWidth) / 2;
            int y = (this.height - drawHeight) / 2;
            graphics.blit(
                    this.location,
                    x,
                    y,
                    drawWidth,
                    drawHeight,
                    0.0F,
                    0.0F,
                    this.imageWidth,
                    this.imageHeight,
                    this.imageWidth,
                    this.imageHeight);
        }
        graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.preview_hint"), this.width / 2, this.height - 16, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.whitemax.snipastemcmod.SnipasteMcMod;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StickyBoard
{
    private static final StickyBoard INSTANCE = new StickyBoard();
    private static final int MIN_SIZE = 24;

    private final List<StickyImage> images = new ArrayList<>();
    private boolean shown = true;
    private int nextId;

    private StickyBoard()
    {
    }

    public static StickyBoard get()
    {
        return INSTANCE;
    }

    public List<StickyImage> images()
    {
        return Collections.unmodifiableList(images);
    }

    public boolean shown()
    {
        return shown;
    }

    public void setShown(boolean shown)
    {
        this.shown = shown;
    }

    public int size()
    {
        return images.size();
    }

    public StickyImage get(int index)
    {
        if (index < 0 || index >= images.size())
        {
            return null;
        }
        return images.get(index);
    }

    public boolean pasteClipboard()
    {
        String clip = TextStickers.clipboard();
        if (clip == null)
        {
            SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.no_text"));
            return false;
        }
        Path saved = ImageFolder.saveText(clip);
        if (saved == null)
        {
            SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.text_failed"));
            return false;
        }
        return pasteFile(saved);
    }

    public boolean pasteLatest()
    {
        try
        {
            Path path = ImageFolder.newest();
            if (path == null)
            {
                SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.no_image"));
                return false;
            }
            return pasteFile(path);
        }
        catch (IOException exception)
        {
            SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.read_failed"));
            return false;
        }
    }

    public boolean pasteFile(Path path)
    {
        if (path == null)
        {
            return false;
        }
        ImageFolder.choose(path);
        try
        {
            if (ImageFolder.isText(path))
            {
                String text = ImageFolder.readText(path);
                if (text == null || text.isBlank())
                {
                    SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.no_text"));
                    return false;
                }
                BufferedImage drawn = TextStickers.draw(text);
                if (drawn == null)
                {
                    SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.text_failed"));
                    return false;
                }
                return place(drawn, false);
            }
            BufferedImage buffered = ImageFolder.read(path);
            return place(buffered, true);
        }
        catch (IOException exception)
        {
            SnipasteClient.tell(Component.translatable(ImageFolder.isText(path) ? "message.snipaste_mc_mod.text_failed" : "message.snipaste_mc_mod.read_failed"));
            return false;
        }
    }

    private boolean place(BufferedImage buffered, boolean screenshot)
    {
        NativeImage nativeImage;
        try
        {
            nativeImage = Pixels.toNative(buffered);
        }
        catch (RuntimeException exception)
        {
            SnipasteClient.tell(Component.translatable(screenshot ? "message.snipaste_mc_mod.read_failed" : "message.snipaste_mc_mod.text_failed"));
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation location = minecraft.getTextureManager().register(
                SnipasteMcMod.MOD_ID + "/sticky/" + nextId++,
                texture);

        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        double guiScale = Math.max(1.0D, minecraft.getWindow().getGuiScale());
        int displayWidth = screenshot
                ? Math.max(1, (int) Math.round(nativeImage.getWidth() / guiScale * 0.5D))
                : Math.max(1, nativeImage.getWidth());
        int displayHeight = screenshot
                ? Math.max(1, (int) Math.round(nativeImage.getHeight() / guiScale * 0.5D))
                : Math.max(1, nativeImage.getHeight());
        int maxWidth = Math.max(MIN_SIZE, guiWidth - 16);
        int maxHeight = Math.max(MIN_SIZE, guiHeight - 16);
        if (displayWidth > maxWidth || displayHeight > maxHeight)
        {
            float fit = Math.min(maxWidth / (float) displayWidth, maxHeight / (float) displayHeight);
            displayWidth = Math.max(MIN_SIZE, Math.round(displayWidth * fit));
            displayHeight = Math.max(MIN_SIZE, Math.round(displayHeight * fit));
        }

        int cascade = (images.size() % 6) * 12;
        int x = guiWidth - displayWidth - 8 - cascade;
        int y = 8 + cascade;
        x = Mth.clamp(x, 0, Math.max(0, guiWidth - displayWidth));
        y = Mth.clamp(y, 0, Math.max(0, guiHeight - displayHeight));

        images.add(new StickyImage(
                location,
                texture,
                nativeImage,
                x,
                y,
                displayWidth,
                displayHeight,
                SnipasteConfig.defaultOpacity()));
        shown = true;
        return true;
    }

    public void remove(int index)
    {
        if (index < 0 || index >= images.size())
        {
            return;
        }
        StickyImage removed = images.remove(index);
        removed.texture().close();
        Minecraft.getInstance().getTextureManager().release(removed.location());
    }

    public int bringToFront(int index)
    {
        if (index < 0 || index >= images.size())
        {
            return -1;
        }
        StickyImage image = images.remove(index);
        images.add(image);
        return images.size() - 1;
    }

    public int topmostAt(double mouseX, double mouseY)
    {
        for (int index = images.size() - 1; index >= 0; index--)
        {
            if (images.get(index).contains(mouseX, mouseY))
            {
                return index;
            }
        }
        return -1;
    }
}

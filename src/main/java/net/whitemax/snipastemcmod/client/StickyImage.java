package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class StickyImage
{
    private final ResourceLocation location;
    private final DynamicTexture texture;
    private final List<String> lines;
    private final int textPadding;
    private final int sourceWidth;
    private final int sourceHeight;
    private int x;
    private int y;
    private int width;
    private int height;
    private float opacity;

    public StickyImage(ResourceLocation location, DynamicTexture texture, NativeImage source, int x, int y, int width, int height, float opacity)
    {
        this(location, texture, null, 0, source.getWidth(), source.getHeight(), x, y, width, height, opacity);
    }

    public StickyImage(List<String> lines, int textPadding, int sourceWidth, int sourceHeight, int x, int y, int width, int height, float opacity)
    {
        this(null, null, lines, textPadding, sourceWidth, sourceHeight, x, y, width, height, opacity);
    }

    private StickyImage(ResourceLocation location, DynamicTexture texture, List<String> lines, int textPadding, int sourceWidth, int sourceHeight, int x, int y, int width, int height, float opacity)
    {
        this.location = location;
        this.texture = texture;
        this.lines = lines;
        this.textPadding = textPadding;
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.opacity = opacity;
    }

    public ResourceLocation location()
    {
        return location;
    }

    public DynamicTexture texture()
    {
        return texture;
    }

    public List<String> lines()
    {
        return lines;
    }

    public int textPadding()
    {
        return textPadding;
    }

    public int sourceWidth()
    {
        return sourceWidth;
    }

    public int sourceHeight()
    {
        return sourceHeight;
    }

    public int x()
    {
        return x;
    }

    public int y()
    {
        return y;
    }

    public int width()
    {
        return width;
    }

    public int height()
    {
        return height;
    }

    public float opacity()
    {
        return opacity;
    }

    public void setOpacity(float opacity)
    {
        this.opacity = Math.max(0.0F, Math.min(1.0F, opacity));
    }

    public void move(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void place(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = Math.max(24, width);
        this.height = Math.max(24, height);
    }

    public boolean contains(double mouseX, double mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}

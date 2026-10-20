package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public final class Pixels
{
    private Pixels()
    {
    }

    public static BufferedImage toBuffered(Image source)
    {
        if (source instanceof BufferedImage buffered && buffered.getType() == BufferedImage.TYPE_INT_ARGB)
        {
            return buffered;
        }
        int width = source.getWidth(null);
        int height = source.getHeight(null);
        if (width <= 0 || height <= 0)
        {
            return null;
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return image;
    }

    public static NativeImage toNative(BufferedImage source)
    {
        BufferedImage argb = toBuffered(source);
        int width = argb.getWidth();
        int height = argb.getHeight();
        int[] pixels = argb.getRGB(0, 0, width, height, null, 0, width);
        NativeImage image = new NativeImage(width, height, false);
        for (int index = 0; index < pixels.length; index++)
        {
            image.setPixelRGBA(index % width, index / width, argbToAbgr(pixels[index]));
        }
        return image;
    }

    public static BufferedImage crop(NativeImage source, int x, int y, int width, int height)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        for (int py = 0; py < height; py++)
        {
            for (int px = 0; px < width; px++)
            {
                pixels[py * width + px] = abgrToArgb(source.getPixelRGBA(x + px, y + py));
            }
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    private static int argbToAbgr(int argb)
    {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }

    private static int abgrToArgb(int abgr)
    {
        int alpha = (abgr >>> 24) & 0xFF;
        int blue = (abgr >>> 16) & 0xFF;
        int green = (abgr >>> 8) & 0xFF;
        int red = abgr & 0xFF;
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}

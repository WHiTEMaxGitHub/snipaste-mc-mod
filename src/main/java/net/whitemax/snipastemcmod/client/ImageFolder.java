package net.whitemax.snipastemcmod.client;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ImageFolder
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "snip-";

    private ImageFolder()
    {
    }

    public static boolean save(BufferedImage image)
    {
        if (image == null)
        {
            return false;
        }
        try
        {
            Path directory = directory();
            Files.createDirectories(directory);
            Path file = directory.resolve(PREFIX + System.currentTimeMillis() + ".png");
            int suffix = 1;
            while (Files.exists(file))
            {
                file = directory.resolve(PREFIX + System.currentTimeMillis() + "-" + suffix++ + ".png");
            }
            try (OutputStream output = Files.newOutputStream(file))
            {
                if (!ImageIO.write(image, "png", output))
                {
                    return false;
                }
            }
            trim(directory);
            return true;
        }
        catch (IOException exception)
        {
            LOGGER.warn("Failed to save an image into the snipaste folder", exception);
            return false;
        }
    }

    public static BufferedImage readLatest() throws IOException
    {
        Path newest = newest(directory());
        if (newest == null)
        {
            return null;
        }
        try (InputStream input = Files.newInputStream(newest))
        {
            BufferedImage image = ImageIO.read(input);
            if (image == null)
            {
                throw new IOException("Not a readable image: " + newest);
            }
            return Pixels.toBuffered(image);
        }
    }

    private static Path directory()
    {
        return FMLPaths.GAMEDIR.get().resolve("snipaste");
    }

    private static void trim(Path directory) throws IOException
    {
        List<StoredImage> images = list(directory);
        images.sort(Comparator.comparingLong(StoredImage::modified));
        long total = 0L;
        for (StoredImage image : images)
        {
            total += image.size;
        }
        long limit = SnipasteConfig.maxFolderBytes();
        int index = 0;
        while (total > limit && index < images.size() - 1)
        {
            StoredImage oldest = images.get(index++);
            Files.deleteIfExists(oldest.path);
            total -= oldest.size;
        }
    }

    private static Path newest(Path directory) throws IOException
    {
        if (!Files.isDirectory(directory))
        {
            return null;
        }
        StoredImage newest = null;
        for (StoredImage image : list(directory))
        {
            if (newest == null || image.modified > newest.modified)
            {
                newest = image;
            }
        }
        return newest == null ? null : newest.path;
    }

    private static List<StoredImage> list(Path directory) throws IOException
    {
        List<StoredImage> images = new ArrayList<>();
        if (!Files.isDirectory(directory))
        {
            return images;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, PREFIX + "*.png"))
        {
            for (Path path : stream)
            {
                if (!Files.isRegularFile(path))
                {
                    continue;
                }
                images.add(new StoredImage(path, Files.getLastModifiedTime(path).toMillis(), Files.size(path)));
            }
        }
        return images;
    }

    private record StoredImage(Path path, long modified, long size)
    {
    }
}

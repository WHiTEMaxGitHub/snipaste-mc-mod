package net.whitemax.snipastemcmod.client;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
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
import java.util.Locale;

public final class ImageFolder
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "snip-";
    private static Path chosen;

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

    public static Path directory()
    {
        return FMLPaths.GAMEDIR.get().resolve("snipaste");
    }

    public static void choose(Path path)
    {
        chosen = path;
    }

    public static Path chosen()
    {
        return chosen;
    }

    public static BufferedImage readForPaste() throws IOException
    {
        if (chosen != null)
        {
            if (!Files.isRegularFile(chosen))
            {
                chosen = null;
            }
            else
            {
                return read(chosen);
            }
        }
        return readLatest();
    }

    public static BufferedImage readLatest() throws IOException
    {
        List<StoredImage> images = images();
        return images.isEmpty() ? null : read(images.get(0).path());
    }

    public static BufferedImage read(Path path) throws IOException
    {
        try (InputStream input = Files.newInputStream(path))
        {
            BufferedImage image = ImageIO.read(input);
            if (image == null)
            {
                throw new IOException("Not a readable image: " + path);
            }
            return Pixels.toBuffered(image);
        }
    }

    public static List<StoredImage> images() throws IOException
    {
        Path directory = directory();
        Files.createDirectories(directory);
        List<StoredImage> images = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory))
        {
            for (Path path : stream)
            {
                if (!Files.isRegularFile(path) || !isImage(path))
                {
                    continue;
                }
                images.add(new StoredImage(path, Files.getLastModifiedTime(path).toMillis(), Files.size(path)));
            }
        }
        images.sort(Comparator.comparingLong(StoredImage::modified).reversed());
        return images;
    }

    public static boolean openFolder()
    {
        try
        {
            Path directory = directory();
            Files.createDirectories(directory);
            Util.getPlatform().openFile(directory.toFile());
            return true;
        }
        catch (IOException exception)
        {
            LOGGER.warn("Failed to open the snipaste folder", exception);
            return false;
        }
    }

    private static boolean isImage(Path path)
    {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".gif")
                || name.endsWith(".bmp");
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

    public record StoredImage(Path path, long modified, long size)
    {
    }
}

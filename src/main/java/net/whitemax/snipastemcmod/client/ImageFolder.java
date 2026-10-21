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
import java.nio.file.attribute.BasicFileAttributes;
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

    public static void enforceLimit()
    {
        try
        {
            trim(directory());
        }
        catch (IOException exception)
        {
            LOGGER.warn("Failed to trim the snipaste folder", exception);
        }
    }

    public static boolean delete(Path path)
    {
        if (path == null)
        {
            return false;
        }
        try
        {
            Path directory = directory().toAbsolutePath().normalize();
            Path target = path.toAbsolutePath().normalize();
            if (!target.startsWith(directory) || !Files.isRegularFile(target))
            {
                return false;
            }
            Files.deleteIfExists(target);
            if (chosen != null && chosen.toAbsolutePath().normalize().equals(target))
            {
                chosen = null;
            }
            return true;
        }
        catch (IOException exception)
        {
            LOGGER.warn("Failed to delete an image from the snipaste folder", exception);
            return false;
        }
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
        if (SnipasteConfig.maxFolderMegabytes() <= 0 || !Files.isDirectory(directory))
        {
            return;
        }
        List<StoredImage> images = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory))
        {
            for (Path path : stream)
            {
                if (!Files.isRegularFile(path) || !isImage(path))
                {
                    continue;
                }
                images.add(new StoredImage(path, entered(path), Files.size(path)));
            }
        }
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
            if (chosen != null && chosen.toAbsolutePath().normalize().equals(oldest.path.toAbsolutePath().normalize()))
            {
                chosen = null;
            }
            total -= oldest.size;
        }
    }

    private static long entered(Path path) throws IOException
    {
        String name = path.getFileName().toString();
        if (name.startsWith(PREFIX) && name.toLowerCase(Locale.ROOT).endsWith(".png"))
        {
            String body = name.substring(PREFIX.length(), name.length() - 4);
            int dash = body.indexOf('-');
            String digits = dash >= 0 ? body.substring(0, dash) : body;
            try
            {
                return Long.parseLong(digits);
            }
            catch (NumberFormatException ignored)
            {
            }
        }
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        long created = attributes.creationTime().toMillis();
        return created > 0L ? created : attributes.lastModifiedTime().toMillis();
    }

    public record StoredImage(Path path, long modified, long size)
    {
    }
}

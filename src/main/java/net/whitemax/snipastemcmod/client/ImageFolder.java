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

    public static Path root()
    {
        return directory().toAbsolutePath().normalize();
    }

    public static boolean contains(Path path)
    {
        return path != null && path.toAbsolutePath().normalize().startsWith(root());
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
        List<StoredImage> images = collect(directory, false);
        images.sort(Comparator.comparingLong(StoredImage::modified).reversed());
        return images;
    }

    public static List<Entry> children(Path directory) throws IOException
    {
        Path root = root();
        Path current = contains(directory) ? directory.toAbsolutePath().normalize() : root;
        if (!Files.isDirectory(current))
        {
            current = root;
        }
        List<Entry> folders = new ArrayList<>();
        List<Entry> images = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(current))
        {
            for (Path path : stream)
            {
                if (Files.isSymbolicLink(path))
                {
                    continue;
                }
                if (Files.isDirectory(path))
                {
                    folders.add(new Entry(path, true, Files.getLastModifiedTime(path).toMillis()));
                    continue;
                }
                if (Files.isRegularFile(path) && isImage(path))
                {
                    images.add(new Entry(path, false, Files.getLastModifiedTime(path).toMillis()));
                }
            }
        }
        folders.sort(Comparator.comparing(entry -> entry.path().getFileName().toString(), String.CASE_INSENSITIVE_ORDER));
        images.sort(Comparator.comparingLong(Entry::modified).reversed());
        folders.addAll(images);
        return folders;
    }

    public static String displayName(Path path)
    {
        Path root = root();
        Path target = path.toAbsolutePath().normalize();
        if (contains(path))
        {
            return root.relativize(target).toString().replace('\\', '/');
        }
        return path.getFileName().toString();
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

    public static String editableName(Path path)
    {
        String name = path.getFileName().toString();
        if (Files.isDirectory(path))
        {
            return name;
        }
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    public static RenameOutcome rename(Path source, String rawName)
    {
        if (source == null)
        {
            return RenameOutcome.failed();
        }
        try
        {
            Path root = root();
            Path from = source.toAbsolutePath().normalize();
            if (from.equals(root) || !contains(from) || !Files.exists(from) || Files.isSymbolicLink(from))
            {
                return RenameOutcome.failed();
            }
            String name = finalName(from, rawName);
            if (name == null)
            {
                return RenameOutcome.failed();
            }
            if (name.equals(from.getFileName().toString()))
            {
                return new RenameOutcome(from, false);
            }
            Path parent = from.getParent();
            Path to = parent.resolve(name).normalize();
            if (!to.startsWith(root) || to.equals(from.getParent()))
            {
                return RenameOutcome.failed();
            }
            if (name.equalsIgnoreCase(from.getFileName().toString()))
            {
                Path temp = parent.resolve(".__rename-" + System.nanoTime());
                Files.move(from, temp);
                try
                {
                    Files.move(temp, to);
                }
                catch (IOException exception)
                {
                    Files.move(temp, from);
                    throw exception;
                }
            }
            else
            {
                if (Files.exists(to))
                {
                    return new RenameOutcome(null, true);
                }
                Files.move(from, to);
            }
            retarget(from, to);
            return new RenameOutcome(to, false);
        }
        catch (IOException exception)
        {
            LOGGER.warn("Failed to rename an entry in the snipaste folder", exception);
            return RenameOutcome.failed();
        }
    }

    private static String finalName(Path source, String rawName)
    {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty() || ".".equals(name) || "..".equals(name) || name.endsWith(".") || name.endsWith(" "))
        {
            return null;
        }
        if (!Files.isDirectory(source))
        {
            String current = source.getFileName().toString();
            int dot = current.lastIndexOf('.');
            String extension = dot > 0 ? current.substring(dot) : "";
            if (!extension.isEmpty() && name.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT)))
            {
                name = name.substring(0, name.length() - extension.length()).trim();
                if (name.isEmpty())
                {
                    return null;
                }
            }
            name = name + extension;
        }
        if (name.length() > 80 || !singleSegment(name))
        {
            return null;
        }
        String stem = name;
        int dot = stem.indexOf('.');
        if (dot >= 0)
        {
            stem = stem.substring(0, dot);
        }
        if (stem.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])"))
        {
            return null;
        }
        return name;
    }

    private static boolean singleSegment(String name)
    {
        for (int index = 0; index < name.length(); index++)
        {
            char character = name.charAt(index);
            if (character < 32 || "\\/:*?\"<>|".indexOf(character) >= 0)
            {
                return false;
            }
        }
        return true;
    }

    private static void retarget(Path from, Path to)
    {
        if (chosen == null)
        {
            return;
        }
        Path current = chosen.toAbsolutePath().normalize();
        if (current.equals(from))
        {
            chosen = to;
            return;
        }
        if (current.startsWith(from))
        {
            chosen = to.resolve(from.relativize(current));
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
            Path target = path.toAbsolutePath().normalize();
            if (!contains(target) || !Files.isRegularFile(target))
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

    public static boolean open(Path path)
    {
        try
        {
            Path root = root();
            Path target = contains(path) ? path.toAbsolutePath().normalize() : root;
            if (!Files.isDirectory(target))
            {
                target = root;
            }
            Files.createDirectories(target);
            Util.getPlatform().openFile(target.toFile());
            return true;
        }
        catch (IOException exception)
        {
            LOGGER.warn("Failed to open the snipaste folder", exception);
            return false;
        }
    }

    private static List<StoredImage> collect(Path root, boolean byEntered) throws IOException
    {
        List<StoredImage> images = new ArrayList<>();
        walk(root, images, byEntered, 0);
        return images;
    }

    private static void walk(Path directory, List<StoredImage> images, boolean byEntered, int depth) throws IOException
    {
        if (depth > 32 || !Files.isDirectory(directory))
        {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory))
        {
            for (Path path : stream)
            {
                if (Files.isSymbolicLink(path))
                {
                    continue;
                }
                if (Files.isDirectory(path))
                {
                    walk(path, images, byEntered, depth + 1);
                    continue;
                }
                if (!Files.isRegularFile(path) || !isImage(path))
                {
                    continue;
                }
                long time = byEntered ? entered(path) : Files.getLastModifiedTime(path).toMillis();
                images.add(new StoredImage(path, time, Files.size(path)));
            }
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
        List<StoredImage> images = collect(directory, true);
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

    public record Entry(Path path, boolean directory, long modified)
    {
    }

    public record RenameOutcome(Path path, boolean taken)
    {
        private static RenameOutcome failed()
        {
            return new RenameOutcome(null, false);
        }
    }

    public record StoredImage(Path path, long modified, long size)
    {
    }
}

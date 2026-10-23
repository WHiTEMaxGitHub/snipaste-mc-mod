package net.whitemax.snipastemcmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

final class TextStickers
{
    private static final int MAX_WIDTH = 240;
    private static final int MAX_LINES = 24;
    private static final int MAX_CHARS = 2000;
    static final int PADDING = 4;

    private TextStickers()
    {
    }

    static String clipboard()
    {
        long window = Minecraft.getInstance().getWindow().getWindow();
        String raw = GLFW.glfwGetClipboardString(window);
        if (raw == null || raw.isEmpty())
        {
            return null;
        }
        String cleaned = clean(raw);
        return cleaned.isBlank() ? null : cleaned;
    }

    static Layout layout(String text)
    {
        Font font = Minecraft.getInstance().font;
        List<String> lines = wrap(text, font);
        if (lines.isEmpty())
        {
            return null;
        }
        int contentWidth = 1;
        for (String line : lines)
        {
            contentWidth = Math.max(contentWidth, font.width(line));
        }
        int width = contentWidth + PADDING * 2;
        int height = lines.size() * font.lineHeight + PADDING * 2;
        return new Layout(lines, Math.max(1, width), Math.max(1, height));
    }

    static float scale()
    {
        int size = SnipasteConfig.textSize();
        if (size <= 0)
        {
            return 1.0F;
        }
        return size / (float) Math.max(1, Minecraft.getInstance().font.lineHeight);
    }

    record Layout(List<String> lines, int width, int height)
    {
    }

    private static String clean(String raw)
    {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (int index = 0; index < raw.length() && count < MAX_CHARS; )
        {
            int point = raw.codePointAt(index);
            int size = Character.charCount(point);
            if (point == '\t')
            {
                builder.append("    ");
                count++;
            }
            else if (point == '\n' || point == '\r' || point >= 32)
            {
                if (point != '\r')
                {
                    builder.appendCodePoint(point);
                    count++;
                }
            }
            index += size;
        }
        return builder.toString();
    }

    private static List<String> wrap(String text, Font font)
    {
        List<String> lines = new ArrayList<>();
        boolean truncated = false;
        for (String paragraph : text.split("\n", -1))
        {
            if (lines.size() >= MAX_LINES)
            {
                truncated = true;
                break;
            }
            if (paragraph.isEmpty())
            {
                lines.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            int width = 0;
            for (int index = 0; index < paragraph.length(); )
            {
                int point = paragraph.codePointAt(index);
                String character = new String(Character.toChars(point));
                int characterWidth = Math.max(1, font.width(character));
                if (width + characterWidth > MAX_WIDTH && !line.isEmpty())
                {
                    if (!addLine(lines, line.toString()))
                    {
                        truncated = true;
                        break;
                    }
                    line.setLength(0);
                    width = 0;
                }
                line.append(character);
                width += characterWidth;
                index += Character.charCount(point);
            }
            if (truncated)
            {
                break;
            }
            if (!addLine(lines, line.toString()))
            {
                truncated = true;
                break;
            }
        }
        if (truncated && !lines.isEmpty())
        {
            lines.set(lines.size() - 1, lines.get(lines.size() - 1) + "…");
        }
        return lines;
    }

    private static boolean addLine(List<String> lines, String line)
    {
        if (lines.size() >= MAX_LINES)
        {
            return false;
        }
        lines.add(line);
        return true;
    }
}

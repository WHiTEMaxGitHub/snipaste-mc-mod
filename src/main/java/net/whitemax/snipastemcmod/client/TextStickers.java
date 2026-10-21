package net.whitemax.snipastemcmod.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

final class TextStickers
{
    private static final int SIZE = 20;
    private static final int MAX_WIDTH = 420;
    private static final int MAX_LINES = 24;
    private static final int MAX_CHARS = 2000;
    private static final int PADDING = 10;
    private static Font font;

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

    static BufferedImage draw(String text)
    {
        Font chosen = font();
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D metricsGraphics = measure.createGraphics();
        metricsGraphics.setFont(chosen);
        FontMetrics metrics = metricsGraphics.getFontMetrics();
        List<String> lines = wrap(text, metrics);
        metricsGraphics.dispose();
        if (lines.isEmpty())
        {
            return null;
        }
        int lineHeight = metrics.getHeight();
        int contentWidth = 1;
        for (String line : lines)
        {
            contentWidth = Math.max(contentWidth, metrics.stringWidth(line));
        }
        int width = contentWidth + PADDING * 2;
        int height = lines.size() * lineHeight + PADDING * 2;
        BufferedImage image = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (SnipasteConfig.textBackground())
        {
            graphics.setColor(new Color(SnipasteConfig.textBackgroundRed(), SnipasteConfig.textBackgroundGreen(), SnipasteConfig.textBackgroundBlue(), 210));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        }
        graphics.setFont(chosen);
        graphics.setColor(new Color(SnipasteConfig.textRed(), SnipasteConfig.textGreen(), SnipasteConfig.textBlue(), 255));
        int baseline = PADDING + metrics.getAscent();
        for (String line : lines)
        {
            graphics.drawString(line, PADDING, baseline);
            baseline += lineHeight;
        }
        graphics.dispose();
        return image;
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

    private static List<String> wrap(String text, FontMetrics metrics)
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
                int characterWidth = metrics.stringWidth(character);
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

    private static Font font()
    {
        if (font != null)
        {
            return font;
        }
        String[] names = {"Microsoft YaHei UI", "Microsoft YaHei", "PingFang SC", "Noto Sans CJK SC", "Noto Sans SC", "Source Han Sans SC", "WenQuanYi Micro Hei", "SimHei"};
        for (String name : names)
        {
            Font candidate = new Font(name, Font.PLAIN, SIZE);
            if (candidate.getFamily().equalsIgnoreCase(name) && candidate.canDisplay('中') && candidate.canDisplay('A'))
            {
                font = candidate;
                return font;
            }
        }
        font = new Font(Font.SANS_SERIF, Font.PLAIN, SIZE);
        return font;
    }
}

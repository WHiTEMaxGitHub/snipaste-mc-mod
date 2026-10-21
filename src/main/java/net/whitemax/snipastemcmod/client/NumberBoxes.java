package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.components.EditBox;

final class NumberBoxes
{
    private NumberBoxes()
    {
    }

    static void show(EditBox box, String text)
    {
        if (!text.equals(box.getValue()))
        {
            box.setValue(text);
        }
    }

    static Double parse(String text)
    {
        if (text == null || text.isEmpty() || ".".equals(text))
        {
            return null;
        }
        String normalized = text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
        if (normalized.isEmpty())
        {
            return null;
        }
        try
        {
            return Double.parseDouble(normalized);
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    static boolean acceptsInteger(String text)
    {
        return text.matches("\\d{0,6}");
    }
}

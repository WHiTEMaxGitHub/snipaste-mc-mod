package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class SnipasteBorderScreen extends Screen
{
    private static final int ROW = 24;
    private final Screen parent;
    private EditBox widthBox;
    private EditBox redBox;
    private EditBox greenBox;
    private EditBox blueBox;
    private int widthLabelX;
    private int redLabelX;
    private int greenLabelX;
    private int blueLabelX;

    public SnipasteBorderScreen(Screen parent)
    {
        super(Component.translatable("screen.snipaste_mc_mod.border_title"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        int left = this.width / 2 - 155;
        int right = left + 160;
        int y = 32;
        addRenderableWidget(CycleButton.onOffBuilder()
                .withInitialValue(SnipasteConfig.borderEnabled())
                .create(left, y, 150, 20, Component.translatable("screen.snipaste_mc_mod.border"),
                        (button, value) -> SnipasteConfig.setBorderEnabled(value)));
        addRenderableWidget(CycleButton.onOffBuilder()
                .withInitialValue(SnipasteConfig.borderRainbow())
                .create(right, y, 150, 20, Component.translatable("screen.snipaste_mc_mod.border_rainbow"),
                        (button, value) -> SnipasteConfig.setBorderRainbow(value)));
        y += ROW;
        Component widthLabel = Component.translatable("screen.snipaste_mc_mod.border_width");
        this.widthLabelX = left;
        this.widthBox = numberBox(left + this.font.width(widthLabel) + 8, y, 72, formatWidth(SnipasteConfig.borderWidth()), 6, true);
        y += ROW;
        int nameWidth = Math.max(this.font.width(channelName(0)), Math.max(this.font.width(channelName(1)), this.font.width(channelName(2))));
        int boxWidth = 56;
        int group = nameWidth + 6 + boxWidth;
        int gap = Math.max(8, (310 - group * 3) / 2);
        this.redLabelX = left;
        this.redBox = channelBox(this.redLabelX + nameWidth + 6, y, boxWidth, SnipasteConfig.borderRed());
        this.greenLabelX = this.redBox.getX() + boxWidth + gap;
        this.greenBox = channelBox(this.greenLabelX + nameWidth + 6, y, boxWidth, SnipasteConfig.borderGreen());
        this.blueLabelX = this.greenBox.getX() + boxWidth + gap;
        this.blueBox = channelBox(this.blueLabelX + nameWidth + 6, y, boxWidth, SnipasteConfig.borderBlue());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public void onClose()
    {
        commitAll();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void tick()
    {
        syncWidth(false);
        syncChannel(this.redBox, 0, false);
        syncChannel(this.greenBox, 1, false);
        syncChannel(this.blueBox, 2, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        if (this.widthBox != null)
        {
            drawLabel(graphics, "screen.snipaste_mc_mod.border_width", this.widthLabelX, this.widthBox.getY());
            drawPlain(graphics, channelName(0), this.redLabelX, this.redBox.getY());
            drawPlain(graphics, channelName(1), this.greenLabelX, this.greenBox.getY());
            drawPlain(graphics, channelName(2), this.blueLabelX, this.blueBox.getY());
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void commitAll()
    {
        syncWidth(true);
        syncChannel(this.redBox, 0, true);
        syncChannel(this.greenBox, 1, true);
        syncChannel(this.blueBox, 2, true);
    }

    private void syncWidth(boolean force)
    {
        if (this.widthBox == null)
        {
            return;
        }
        this.widthBox.tick();
        if (this.widthBox.isFocused() && !force)
        {
            Double value = parseNumber(this.widthBox.getValue());
            if (value != null && value >= 0.1D && value <= 16.0D)
            {
                SnipasteConfig.setBorderWidth(value);
            }
            return;
        }
        Double value = parseNumber(this.widthBox.getValue());
        if (value != null)
        {
            SnipasteConfig.setBorderWidth(Math.max(0.1D, Math.min(16.0D, value)));
        }
        show(this.widthBox, formatWidth(SnipasteConfig.borderWidth()));
    }

    private void syncChannel(EditBox box, int channel, boolean force)
    {
        if (box == null)
        {
            return;
        }
        box.tick();
        if (box.isFocused() && !force)
        {
            Double value = parseNumber(box.getValue());
            if (value != null && value >= 0.0D && value <= 255.0D)
            {
                SnipasteConfig.setBorderChannel(channel, (int) Math.round(value));
            }
            return;
        }
        Double value = parseNumber(box.getValue());
        if (value != null)
        {
            int clamped = (int) Math.round(Math.max(0.0D, Math.min(255.0D, value)));
            SnipasteConfig.setBorderChannel(channel, clamped);
        }
        int current = switch (channel)
        {
            case 0 -> SnipasteConfig.borderRed();
            case 1 -> SnipasteConfig.borderGreen();
            default -> SnipasteConfig.borderBlue();
        };
        show(box, String.valueOf(current));
    }

    private EditBox numberBox(int x, int y, int width, String value, int maxLength, boolean decimal)
    {
        EditBox box = new EditBox(this.font, x, y, width, 20, Component.empty());
        box.setMaxLength(maxLength);
        box.setValue(value);
        box.setFilter(decimal ? SnipasteBorderScreen::acceptsDecimal : SnipasteBorderScreen::acceptsInteger);
        return addRenderableWidget(box);
    }

    private EditBox channelBox(int x, int y, int width, int value)
    {
        return numberBox(x, y, width, String.valueOf(value), 4, false);
    }

    private void drawLabel(GuiGraphics graphics, String key, int x, int y)
    {
        graphics.drawString(this.font, Component.translatable(key), x, y + 6, 0xFFFFFF, false);
    }

    private void drawPlain(GuiGraphics graphics, String text, int x, int y)
    {
        graphics.drawString(this.font, text, x, y + 6, 0xFFFFFF, false);
    }

    private static String channelName(int channel)
    {
        String key = switch (channel)
        {
            case 0 -> "screen.snipaste_mc_mod.channel_red";
            case 1 -> "screen.snipaste_mc_mod.channel_green";
            default -> "screen.snipaste_mc_mod.channel_blue";
        };
        return Component.translatable(key).getString();
    }

    private static void show(EditBox box, String text)
    {
        if (!text.equals(box.getValue()))
        {
            box.setValue(text);
        }
    }

    private static Double parseNumber(String text)
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

    private static boolean acceptsInteger(String text)
    {
        return text.matches("\\d{0,6}");
    }

    private static boolean acceptsDecimal(String text)
    {
        return text.matches("\\d{0,4}(\\.\\d{0,2})?");
    }

    private static String formatWidth(double width)
    {
        String text = String.format(Locale.ROOT, "%.2f", width);
        if (text.contains("."))
        {
            text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return text;
    }
}

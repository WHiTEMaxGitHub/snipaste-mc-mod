package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SnipasteConfigScreen extends Screen
{
    private static final int ROW = 24;
    private final Screen parent;
    private EditBox opacityBox;
    private EditBox folderBox;
    private EditBox borderWidthBox;
    private EditBox redBox;
    private EditBox greenBox;
    private EditBox blueBox;
    private int opacityTipX;
    private int opacityTipW;
    private int folderTipX;
    private int folderTipW;
    private int colorLabelX;
    private int letterGap;

    public SnipasteConfigScreen(Screen parent)
    {
        super(Component.translatable("screen.snipaste_mc_mod.config_title"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        int left = this.width / 2 - 155;
        int right = left + 160;
        int y = 32;
        int labelGap = 6;
        Component opacityLabel = Component.translatable("screen.snipaste_mc_mod.opacity_label");
        Component folderLabel = Component.translatable("screen.snipaste_mc_mod.folder_label");
        int opacityLabelWidth = this.font.width(opacityLabel);
        int folderLabelWidth = this.font.width(folderLabel);
        int pairGap = 18;
        int pairBoxes = 310 - opacityLabelWidth - folderLabelWidth - labelGap * 2 - pairGap;
        int opacityBoxWidth = Math.max(52, pairBoxes / 2);
        int folderBoxWidth = Math.max(52, pairBoxes - opacityBoxWidth);
        this.opacityTipX = left;
        this.opacityBox = numberBox(left + opacityLabelWidth + labelGap, y, opacityBoxWidth, String.valueOf(opacityPercent()), 3, false);
        this.folderTipX = this.opacityBox.getX() + opacityBoxWidth + pairGap;
        this.folderBox = numberBox(this.folderTipX + folderLabelWidth + labelGap, y, folderBoxWidth, String.valueOf(SnipasteConfig.maxFolderMegabytes()), 5, false);
        this.opacityTipW = this.opacityBox.getX() + this.opacityBox.getWidth() - this.opacityTipX;
        this.folderTipW = this.folderBox.getX() + this.folderBox.getWidth() - this.folderTipX;
        y += ROW;
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
        Component colorLabel = Component.translatable("screen.snipaste_mc_mod.border_color");
        int widthLabelWidth = this.font.width(widthLabel);
        int colorLabelWidth = this.font.width(colorLabel);
        int letterWidth = Math.max(this.font.width("R"), Math.max(this.font.width("G"), this.font.width("B")));
        int widthBoxWidth = 52;
        int channelGap = 6;
        this.letterGap = 4;
        int used = widthLabelWidth + labelGap + widthBoxWidth + 12 + colorLabelWidth + labelGap
                + (letterWidth + this.letterGap) * 3 + channelGap * 2;
        int channelWidth = Math.min(48, Math.max(36, (310 - used) / 3));
        this.borderWidthBox = numberBox(left + widthLabelWidth + labelGap, y, widthBoxWidth, formatWidth(SnipasteConfig.borderWidth()), 6, true);
        this.colorLabelX = this.borderWidthBox.getX() + widthBoxWidth + 12;
        int channelX = this.colorLabelX + colorLabelWidth + labelGap;
        this.redBox = channelBox(channelX + letterWidth + this.letterGap, y, channelWidth, SnipasteConfig.borderRed());
        channelX = this.redBox.getX() + channelWidth + channelGap;
        this.greenBox = channelBox(channelX + letterWidth + this.letterGap, y, channelWidth, SnipasteConfig.borderGreen());
        channelX = this.greenBox.getX() + channelWidth + channelGap;
        this.blueBox = channelBox(channelX + letterWidth + this.letterGap, y, channelWidth, SnipasteConfig.borderBlue());
        y += ROW + 8;
        addRenderableWidget(CycleButton.onOffBuilder()
                .withInitialValue(SnipasteConfig.allowStretch())
                .create(left, y, 150, 20, Component.translatable("screen.snipaste_mc_mod.allow_stretch"),
                        (button, value) -> SnipasteConfig.setAllowStretch(value)));
        y += ROW;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.shadow_page"), button -> {
                    commitAll();
                    this.minecraft.setScreen(new SnipasteShadowScreen(this));
                })
                .bounds(left, y, 150, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.keys_page"), button -> {
                    commitAll();
                    this.minecraft.setScreen(new SnipasteKeybindsScreen(this));
                })
                .bounds(right, y, 150, 20)
                .build());
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        if (this.opacityBox != null)
        {
            drawLabel(graphics, "screen.snipaste_mc_mod.opacity_label", this.opacityTipX, this.opacityBox.getY());
            drawLabel(graphics, "screen.snipaste_mc_mod.folder_label", this.folderTipX, this.folderBox.getY());
            drawLabel(graphics, "screen.snipaste_mc_mod.border_width", this.width / 2 - 155, this.borderWidthBox.getY());
            drawLabel(graphics, "screen.snipaste_mc_mod.border_color", this.colorLabelX, this.borderWidthBox.getY());
            drawPlain(graphics, "R", this.redBox.getX() - this.font.width("R") - this.letterGap, this.redBox.getY());
            drawPlain(graphics, "G", this.greenBox.getX() - this.font.width("G") - this.letterGap, this.greenBox.getY());
            drawPlain(graphics, "B", this.blueBox.getX() - this.font.width("B") - this.letterGap, this.blueBox.getY());
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.opacityBox != null && over(mouseX, mouseY, this.opacityTipX, this.opacityBox.getY(), this.opacityTipW))
        {
            graphics.renderTooltip(this.font, Component.translatable("screen.snipaste_mc_mod.opacity_tip"), mouseX, mouseY);
        }
        else if (this.folderBox != null && over(mouseX, mouseY, this.folderTipX, this.folderBox.getY(), this.folderTipW))
        {
            graphics.renderTooltip(this.font, Component.translatable("screen.snipaste_mc_mod.folder_tip"), mouseX, mouseY);
        }
    }

    @Override
    public void tick()
    {
        syncOpacity(false);
        syncFolder(false);
        syncWidth(false);
        syncChannel(this.redBox, 0, false);
        syncChannel(this.greenBox, 1, false);
        syncChannel(this.blueBox, 2, false);
    }

    private void commitAll()
    {
        syncOpacity(true);
        syncFolder(true);
        syncWidth(true);
        syncChannel(this.redBox, 0, true);
        syncChannel(this.greenBox, 1, true);
        syncChannel(this.blueBox, 2, true);
    }

    private void syncOpacity(boolean force)
    {
        if (this.opacityBox == null)
        {
            return;
        }
        this.opacityBox.tick();
        if (this.opacityBox.isFocused() && !force)
        {
            Double value = parseNumber(this.opacityBox.getValue());
            if (value != null && value >= 5.0D && value <= 100.0D)
            {
                SnipasteConfig.setDefaultOpacity(value / 100.0D);
            }
            return;
        }
        Double value = parseNumber(this.opacityBox.getValue());
        if (value != null)
        {
            double clamped = Math.max(5.0D, Math.min(100.0D, value));
            SnipasteConfig.setDefaultOpacity(clamped / 100.0D);
        }
        show(this.opacityBox, String.valueOf(opacityPercent()));
    }

    private void syncFolder(boolean force)
    {
        if (this.folderBox == null)
        {
            return;
        }
        this.folderBox.tick();
        if (this.folderBox.isFocused() && !force)
        {
            Double value = parseNumber(this.folderBox.getValue());
            if (value != null && value >= 1.0D && value <= 4096.0D)
            {
                setFolder((int) Math.round(value));
            }
            return;
        }
        Double value = parseNumber(this.folderBox.getValue());
        if (value != null)
        {
            int clamped = (int) Math.round(Math.max(1.0D, Math.min(4096.0D, value)));
            setFolder(clamped);
        }
        show(this.folderBox, String.valueOf(SnipasteConfig.maxFolderMegabytes()));
    }

    private void syncWidth(boolean force)
    {
        if (this.borderWidthBox == null)
        {
            return;
        }
        this.borderWidthBox.tick();
        if (this.borderWidthBox.isFocused() && !force)
        {
            Double value = parseNumber(this.borderWidthBox.getValue());
            if (value != null && value >= 0.1D && value <= 16.0D)
            {
                SnipasteConfig.setBorderWidth(value);
            }
            return;
        }
        Double value = parseNumber(this.borderWidthBox.getValue());
        if (value != null)
        {
            SnipasteConfig.setBorderWidth(Math.max(0.1D, Math.min(16.0D, value)));
        }
        show(this.borderWidthBox, formatWidth(SnipasteConfig.borderWidth()));
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

    private static void setFolder(int megabytes)
    {
        SnipasteConfig.addMaxFolderMegabytes(megabytes - SnipasteConfig.maxFolderMegabytes());
    }

    private static void show(EditBox box, String text)
    {
        if (!text.equals(box.getValue()))
        {
            box.setValue(text);
        }
    }

    private static int opacityPercent()
    {
        return Math.max(5, Math.min(100, Math.round(SnipasteConfig.defaultOpacity() * 100.0F)));
    }

    private EditBox numberBox(int x, int y, int width, String value, int maxLength, boolean decimal)
    {
        EditBox box = new EditBox(this.font, x, y, width, 20, Component.empty());
        box.setMaxLength(maxLength);
        box.setValue(value);
        box.setFilter(decimal ? SnipasteConfigScreen::acceptsDecimal : SnipasteConfigScreen::acceptsInteger);
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

    private static boolean over(int mouseX, int mouseY, int x, int y, int width)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 20;
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
        String text = String.format(java.util.Locale.ROOT, "%.2f", width);
        if (text.contains("."))
        {
            text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return text;
    }
}

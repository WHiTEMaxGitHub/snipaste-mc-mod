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
    private int widthLabelX;
    private Button colorSwatch;

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
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.border_color"), button -> openColor())
                .bounds(left, y, 150, 20)
                .build());
        this.colorSwatch = addRenderableWidget(Button.builder(Component.empty(), button -> openColor())
                .bounds(left + 156, y, 36, 20)
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
    public void tick()
    {
        syncWidth(false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        if (this.widthBox != null)
        {
            drawLabel(graphics, "screen.snipaste_mc_mod.border_width", this.widthLabelX, this.widthBox.getY());
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.colorSwatch != null)
        {
            SnipasteColorScreen.paintSwatch(graphics, this.colorSwatch.getX() + 2, this.colorSwatch.getY() + 2, this.colorSwatch.getWidth() - 4, this.colorSwatch.getHeight() - 4, SnipasteConfig.borderRgb(), SnipasteConfig.borderAlpha());
        }
    }

    private void commitAll()
    {
        syncWidth(true);
    }

    private void openColor()
    {
        commitAll();
        this.minecraft.setScreen(new SnipasteColorScreen(this, Component.translatable("screen.snipaste_mc_mod.border_color"), SnipasteConfig.borderRgb(), SnipasteConfig.borderAlpha(),
                (red, green, blue, alpha) -> {
                    SnipasteConfig.setBorderChannel(0, red);
                    SnipasteConfig.setBorderChannel(1, green);
                    SnipasteConfig.setBorderChannel(2, blue);
                    SnipasteConfig.setBorderChannel(3, alpha);
                }));
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
            Double value = NumberBoxes.parse(this.widthBox.getValue());
            if (value != null && value >= 0.1D && value <= 16.0D)
            {
                SnipasteConfig.setBorderWidth(value);
            }
            return;
        }
        Double value = NumberBoxes.parse(this.widthBox.getValue());
        if (value != null)
        {
            SnipasteConfig.setBorderWidth(value);
        }
        NumberBoxes.show(this.widthBox, formatWidth(SnipasteConfig.borderWidth()));
    }

    private EditBox numberBox(int x, int y, int width, String value, int maxLength, boolean decimal)
    {
        EditBox box = new EditBox(this.font, x, y, width, 20, Component.empty());
        box.setMaxLength(maxLength);
        box.setValue(value);
        box.setFilter(decimal ? SnipasteBorderScreen::acceptsDecimal : NumberBoxes::acceptsInteger);
        return addRenderableWidget(box);
    }

    private void drawLabel(GuiGraphics graphics, String key, int x, int y)
    {
        graphics.drawString(this.font, Component.translatable(key), x, y + 6, 0xFFFFFF, false);
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

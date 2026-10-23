package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SnipasteTextScreen extends Screen
{
    private static final int ROW = 24;
    private final Screen parent;
    private EditBox sizeBox;
    private int sizeLabelX;
    private int sizeTipW;
    private Button textSwatch;
    private Button backgroundSwatch;

    public SnipasteTextScreen(Screen parent)
    {
        super(Component.translatable("screen.snipaste_mc_mod.text_title"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        int left = this.width / 2 - 155;
        int y = 32;
        addRenderableWidget(CycleButton.onOffBuilder()
                .withInitialValue(SnipasteConfig.textBackground())
                .create(left, y, 150, 20, Component.translatable("screen.snipaste_mc_mod.text_background"),
                        (button, value) -> SnipasteConfig.setTextBackground(value)));
        int right = this.width / 2 + 5;
        this.sizeLabelX = right;
        int sizeLabelWidth = this.font.width(Component.translatable("screen.snipaste_mc_mod.text_size"));
        this.sizeBox = new EditBox(this.font, right + sizeLabelWidth + 6, y, 48, 20, Component.empty());
        this.sizeBox.setMaxLength(3);
        this.sizeBox.setValue(String.valueOf(SnipasteConfig.textSize()));
        this.sizeBox.setFilter(NumberBoxes::acceptsInteger);
        addRenderableWidget(this.sizeBox);
        this.sizeTipW = this.sizeBox.getX() + this.sizeBox.getWidth() - this.sizeLabelX;
        y += ROW;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.text_color"), button -> openColor(false))
                .bounds(left, y, 150, 20)
                .build());
        this.textSwatch = colorSwatch(left + 156, y, false);
        y += ROW;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.text_bg_color"), button -> openColor(true))
                .bounds(left, y, 150, 20)
                .build());
        this.backgroundSwatch = colorSwatch(left + 156, y, true);
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
        syncSize(false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        if (this.sizeBox != null)
        {
            drawLabel(graphics, "screen.snipaste_mc_mod.text_size", this.sizeLabelX, this.sizeBox.getY());
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        paintSwatch(graphics, this.textSwatch, SnipasteConfig.textRgb(), SnipasteConfig.textAlpha());
        paintSwatch(graphics, this.backgroundSwatch, SnipasteConfig.textBackgroundRgb(), SnipasteConfig.textBackgroundAlpha());
        if (this.sizeBox != null && over(mouseX, mouseY, this.sizeLabelX, this.sizeBox.getY(), this.sizeTipW))
        {
            graphics.renderTooltip(this.font, Component.translatable("screen.snipaste_mc_mod.text_size_tip"), mouseX, mouseY);
        }
    }

    private void commitAll()
    {
        syncSize(true);
    }

    private void openColor(boolean background)
    {
        commitAll();
        int rgb = background ? SnipasteConfig.textBackgroundRgb() : SnipasteConfig.textRgb();
        int alpha = background ? SnipasteConfig.textBackgroundAlpha() : SnipasteConfig.textAlpha();
        Component title = Component.translatable(background ? "screen.snipaste_mc_mod.text_bg_color" : "screen.snipaste_mc_mod.text_color");
        this.minecraft.setScreen(new SnipasteColorScreen(this, title, rgb, alpha, (red, green, blue, nextAlpha) -> {
            SnipasteConfig.setTextChannel(background, 0, red);
            SnipasteConfig.setTextChannel(background, 1, green);
            SnipasteConfig.setTextChannel(background, 2, blue);
            SnipasteConfig.setTextChannel(background, 3, nextAlpha);
        }));
    }

    private Button colorSwatch(int x, int y, boolean background)
    {
        return addRenderableWidget(Button.builder(Component.empty(), button -> openColor(background))
                .bounds(x, y, 36, 20)
                .build());
    }

    private static void paintSwatch(GuiGraphics graphics, Button button, int rgb, int alpha)
    {
        if (button == null)
        {
            return;
        }
        SnipasteColorScreen.paintSwatch(graphics, button.getX() + 2, button.getY() + 2, button.getWidth() - 4, button.getHeight() - 4, rgb, alpha);
    }

    private void syncSize(boolean force)
    {
        if (this.sizeBox == null)
        {
            return;
        }
        this.sizeBox.tick();
        if (this.sizeBox.isFocused() && !force)
        {
            Double value = NumberBoxes.parse(this.sizeBox.getValue());
            if (value != null && value >= 0.0D && value <= 128.0D)
            {
                SnipasteConfig.setTextSize((int) Math.round(value));
            }
            return;
        }
        Double value = NumberBoxes.parse(this.sizeBox.getValue());
        if (value != null)
        {
            SnipasteConfig.setTextSize((int) Math.round(value));
        }
        NumberBoxes.show(this.sizeBox, String.valueOf(SnipasteConfig.textSize()));
    }

    private void drawLabel(GuiGraphics graphics, String key, int x, int y)
    {
        graphics.drawString(this.font, Component.translatable(key), x, y + 6, 0xFFFFFF, false);
    }

    private static boolean over(int mouseX, int mouseY, int x, int y, int width)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 20;
    }
}

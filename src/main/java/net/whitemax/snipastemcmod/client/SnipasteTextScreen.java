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
    private EditBox textRed;
    private EditBox textGreen;
    private EditBox textBlue;
    private EditBox backgroundRed;
    private EditBox backgroundGreen;
    private EditBox backgroundBlue;
    private int textLabelX;
    private int backgroundLabelX;
    private int textRedX;
    private int textGreenX;
    private int textBlueX;
    private int backgroundRedX;
    private int backgroundGreenX;
    private int backgroundBlueX;

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
        y += ROW;
        int nameWidth = Math.max(this.font.width(channelName(0)), Math.max(this.font.width(channelName(1)), this.font.width(channelName(2))));
        int boxWidth = 48;
        this.textLabelX = left;
        int textLabelWidth = this.font.width(Component.translatable("screen.snipaste_mc_mod.text_color"));
        this.textRedX = left + textLabelWidth + 10;
        this.textRed = channelBox(this.textRedX + nameWidth + 4, y, boxWidth, false, 0);
        this.textGreenX = this.textRed.getX() + boxWidth + 12;
        this.textGreen = channelBox(this.textGreenX + nameWidth + 4, y, boxWidth, false, 1);
        this.textBlueX = this.textGreen.getX() + boxWidth + 12;
        this.textBlue = channelBox(this.textBlueX + nameWidth + 4, y, boxWidth, false, 2);
        y += ROW;
        this.backgroundLabelX = left;
        int backgroundLabelWidth = this.font.width(Component.translatable("screen.snipaste_mc_mod.text_bg_color"));
        this.backgroundRedX = left + backgroundLabelWidth + 10;
        this.backgroundRed = channelBox(this.backgroundRedX + nameWidth + 4, y, boxWidth, true, 0);
        this.backgroundGreenX = this.backgroundRed.getX() + boxWidth + 12;
        this.backgroundGreen = channelBox(this.backgroundGreenX + nameWidth + 4, y, boxWidth, true, 1);
        this.backgroundBlueX = this.backgroundGreen.getX() + boxWidth + 12;
        this.backgroundBlue = channelBox(this.backgroundBlueX + nameWidth + 4, y, boxWidth, true, 2);
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
        sync(this.textRed, false, 0, false);
        sync(this.textGreen, false, 1, false);
        sync(this.textBlue, false, 2, false);
        sync(this.backgroundRed, true, 0, false);
        sync(this.backgroundGreen, true, 1, false);
        sync(this.backgroundBlue, true, 2, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        if (this.textRed != null)
        {
            drawLabel(graphics, "screen.snipaste_mc_mod.text_color", this.textLabelX, this.textRed.getY());
            drawPlain(graphics, channelName(0), this.textRedX, this.textRed.getY());
            drawPlain(graphics, channelName(1), this.textGreenX, this.textGreen.getY());
            drawPlain(graphics, channelName(2), this.textBlueX, this.textBlue.getY());
            drawLabel(graphics, "screen.snipaste_mc_mod.text_bg_color", this.backgroundLabelX, this.backgroundRed.getY());
            drawPlain(graphics, channelName(0), this.backgroundRedX, this.backgroundRed.getY());
            drawPlain(graphics, channelName(1), this.backgroundGreenX, this.backgroundGreen.getY());
            drawPlain(graphics, channelName(2), this.backgroundBlueX, this.backgroundBlue.getY());
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void commitAll()
    {
        sync(this.textRed, false, 0, true);
        sync(this.textGreen, false, 1, true);
        sync(this.textBlue, false, 2, true);
        sync(this.backgroundRed, true, 0, true);
        sync(this.backgroundGreen, true, 1, true);
        sync(this.backgroundBlue, true, 2, true);
    }

    private void sync(EditBox box, boolean background, int channel, boolean force)
    {
        if (box == null)
        {
            return;
        }
        box.tick();
        if (box.isFocused() && !force)
        {
            Double value = NumberBoxes.parse(box.getValue());
            if (value != null && value >= 0.0D && value <= 255.0D)
            {
                SnipasteConfig.setTextChannel(background, channel, (int) Math.round(value));
            }
            return;
        }
        Double value = NumberBoxes.parse(box.getValue());
        if (value != null)
        {
            SnipasteConfig.setTextChannel(background, channel, (int) Math.round(value));
        }
        NumberBoxes.show(box, String.valueOf(SnipasteConfig.textChannel(background, channel)));
    }

    private EditBox channelBox(int x, int y, int width, boolean background, int channel)
    {
        EditBox box = new EditBox(this.font, x, y, width, 20, Component.empty());
        box.setMaxLength(3);
        box.setValue(String.valueOf(SnipasteConfig.textChannel(background, channel)));
        box.setFilter(NumberBoxes::acceptsInteger);
        return addRenderableWidget(box);
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
}

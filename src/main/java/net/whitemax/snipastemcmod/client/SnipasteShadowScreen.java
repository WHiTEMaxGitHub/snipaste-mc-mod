package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public class SnipasteShadowScreen extends Screen
{
    private static final int ROW = 24;
    private final Screen parent;

    public SnipasteShadowScreen(Screen parent)
    {
        super(Component.translatable("screen.snipaste_mc_mod.shadow_title"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        int left = this.width / 2 - 155;
        int right = left + 160;
        int y = 48;
        addRenderableWidget(CycleButton.onOffBuilder()
                .withInitialValue(SnipasteConfig.shadowEnabled())
                .create(left, y, 150, 20, Component.translatable("screen.snipaste_mc_mod.shadow"),
                        (button, value) -> SnipasteConfig.setShadowEnabled(value)));
        addRenderableWidget(new PercentSlider(right, y, 150, 20));
        y += ROW;
        addRenderableWidget(new IntSlider(left, y, 150, 20, 0, 360, SnipasteConfig.shadowAngle(),
                "screen.snipaste_mc_mod.shadow_angle", SnipasteConfig::setShadowAngle));
        addRenderableWidget(new IntSlider(right, y, 150, 20, 0, 64, SnipasteConfig.shadowDistance(),
                "screen.snipaste_mc_mod.shadow_distance", SnipasteConfig::setShadowDistance));
        y += ROW;
        addRenderableWidget(new IntSlider(left, y, 150, 20, 0, 48, SnipasteConfig.shadowBlur(),
                "screen.snipaste_mc_mod.shadow_blur", SnipasteConfig::setShadowBlur));
        addRenderableWidget(new IntSlider(right, y, 150, 20, 0, 32, SnipasteConfig.shadowSpread(),
                "screen.snipaste_mc_mod.shadow_spread", SnipasteConfig::setShadowSpread));
        y += ROW;
        addRenderableWidget(new ChannelSlider(left, y, 100, 20, 0));
        addRenderableWidget(new ChannelSlider(left + 105, y, 100, 20, 1));
        addRenderableWidget(new ChannelSlider(left + 210, y, 100, 20, 2));
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
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.shadow_hint"), this.width / 2, 32, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static final class PercentSlider extends AbstractSliderButton
    {
        private PercentSlider(int x, int y, int width, int height)
        {
            super(x, y, width, height, Component.empty(), SnipasteConfig.shadowOpacity());
            updateMessage();
        }

        @Override
        protected void updateMessage()
        {
            setMessage(Component.translatable("screen.snipaste_mc_mod.shadow_opacity", Math.round(this.value * 100.0D)));
        }

        @Override
        protected void applyValue()
        {
            SnipasteConfig.setShadowOpacity(this.value);
        }
    }

    private static final class IntSlider extends AbstractSliderButton
    {
        private final int min;
        private final int max;
        private final String labelKey;
        private final IntConsumer apply;

        private IntSlider(int x, int y, int width, int height, int min, int max, int current, String labelKey, IntConsumer apply)
        {
            super(x, y, width, height, Component.empty(), toSlider(min, max, current));
            this.min = min;
            this.max = max;
            this.labelKey = labelKey;
            this.apply = apply;
            updateMessage();
        }

        @Override
        protected void updateMessage()
        {
            setMessage(Component.translatable(this.labelKey, current()));
        }

        @Override
        protected void applyValue()
        {
            this.apply.accept(current());
        }

        private int current()
        {
            return this.min + Math.round((float) (this.value * (this.max - this.min)));
        }

        private static double toSlider(int min, int max, int current)
        {
            return (Math.max(min, Math.min(max, current)) - min) / (double) (max - min);
        }
    }

    private static final class ChannelSlider extends AbstractSliderButton
    {
        private final int channel;

        private ChannelSlider(int x, int y, int width, int height, int channel)
        {
            super(x, y, width, height, Component.empty(), channelValue(channel) / 255.0D);
            this.channel = channel;
            updateMessage();
        }

        @Override
        protected void updateMessage()
        {
            setMessage(Component.translatable(labelKey(), (int) Math.round(this.value * 255.0D)));
        }

        @Override
        protected void applyValue()
        {
            SnipasteConfig.setShadowChannel(this.channel, (int) Math.round(this.value * 255.0D));
        }

        private String labelKey()
        {
            return switch (this.channel)
            {
                case 0 -> "screen.snipaste_mc_mod.shadow_red";
                case 1 -> "screen.snipaste_mc_mod.shadow_green";
                default -> "screen.snipaste_mc_mod.shadow_blue";
            };
        }

        private static int channelValue(int channel)
        {
            return switch (channel)
            {
                case 0 -> SnipasteConfig.shadowRed();
                case 1 -> SnipasteConfig.shadowGreen();
                default -> SnipasteConfig.shadowBlue();
            };
        }
    }
}

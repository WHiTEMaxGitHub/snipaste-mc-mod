package net.whitemax.snipastemcmod.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SnipasteConfigScreen extends Screen
{
    private static final int ROW = 24;
    private final Screen parent;
    private KeyMapping listening;
    private Button snipButton;
    private Button pasteButton;
    private Button editButton;

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
        int y = 40;
        addRenderableWidget(new OpacitySlider(left, y, 310, 20));
        y += ROW;
        addRenderableWidget(new FolderSlider(left, y, 310, 20));
        y += ROW + 8;
        this.snipButton = keyButton(SnipasteKeybinds.SNIP, "screen.snipaste_mc_mod.snip_key", left, y);
        this.pasteButton = keyButton(SnipasteKeybinds.PASTE, "screen.snipaste_mc_mod.paste_key", right, y);
        y += ROW;
        this.editButton = keyButton(SnipasteKeybinds.EDIT, "screen.snipaste_mc_mod.edit_key", left, y);
        addRenderableWidget(reservedButton(right, y));
        y += ROW;
        addRenderableWidget(reservedButton(left, y));
        addRenderableWidget(reservedButton(right, y));
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
    public boolean shouldCloseOnEsc()
    {
        return this.listening == null;
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
        refreshKeyLabels();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (this.listening != null)
        {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE)
            {
                this.listening = null;
            }
            else if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE)
            {
                SnipasteKeybinds.unbind(this.listening);
                this.listening = null;
            }
            else
            {
                SnipasteKeybinds.rebind(this.listening, keyCode, scanCode);
                this.listening = null;
            }
            refreshKeyLabels();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Button keyButton(KeyMapping mapping, String labelKey, int x, int y)
    {
        return addRenderableWidget(Button.builder(keyLabel(labelKey, mapping), button -> {
            this.listening = mapping;
            refreshKeyLabels();
        }).bounds(x, y, 150, 20).build());
    }

    private Button reservedButton(int x, int y)
    {
        Button button = Button.builder(Component.translatable("screen.snipaste_mc_mod.config_reserved"), pressed -> {
        }).bounds(x, y, 150, 20).build();
        button.active = false;
        return button;
    }

    private void refreshKeyLabels()
    {
        if (this.snipButton != null)
        {
            this.snipButton.setMessage(keyLabel("screen.snipaste_mc_mod.snip_key", SnipasteKeybinds.SNIP));
        }
        if (this.pasteButton != null)
        {
            this.pasteButton.setMessage(keyLabel("screen.snipaste_mc_mod.paste_key", SnipasteKeybinds.PASTE));
        }
        if (this.editButton != null)
        {
            this.editButton.setMessage(keyLabel("screen.snipaste_mc_mod.edit_key", SnipasteKeybinds.EDIT));
        }
    }

    private Component keyLabel(String labelKey, KeyMapping mapping)
    {
        if (this.listening == mapping)
        {
            return Component.translatable("screen.snipaste_mc_mod.press_key");
        }
        return Component.translatable("screen.snipaste_mc_mod.key_value",
                Component.translatable(labelKey),
                mapping.getTranslatedKeyMessage());
    }

    private static final class OpacitySlider extends AbstractSliderButton
    {
        private OpacitySlider(int x, int y, int width, int height)
        {
            super(x, y, width, height, Component.empty(), (SnipasteConfig.defaultOpacity() - 0.05D) / 0.95D);
            updateMessage();
        }

        @Override
        protected void updateMessage()
        {
            setMessage(Component.translatable("screen.snipaste_mc_mod.default_opacity", Math.round(current() * 100.0F)));
        }

        @Override
        protected void applyValue()
        {
            SnipasteConfig.setDefaultOpacity(current());
        }

        private float current()
        {
            return (float) (0.05D + this.value * 0.95D);
        }
    }

    private static final class FolderSlider extends AbstractSliderButton
    {
        private static final int MIN = 16;
        private static final int MAX = 4096;
        private static final int STEP = 16;

        private FolderSlider(int x, int y, int width, int height)
        {
            super(x, y, width, height, Component.empty(), toSlider(SnipasteConfig.maxFolderMegabytes()));
            updateMessage();
        }

        @Override
        protected void updateMessage()
        {
            setMessage(Component.translatable("screen.snipaste_mc_mod.folder_slider", fromSlider(this.value)));
        }

        @Override
        protected void applyValue()
        {
            int target = fromSlider(this.value);
            SnipasteConfig.addMaxFolderMegabytes(target - SnipasteConfig.maxFolderMegabytes());
        }

        private static double toSlider(int megabytes)
        {
            int snapped = Math.max(MIN, Math.min(MAX, megabytes));
            return (snapped - MIN) / (double) (MAX - MIN);
        }

        private static int fromSlider(double slider)
        {
            int steps = Math.round((float) (slider * ((MAX - MIN) / STEP)));
            return MIN + steps * STEP;
        }
    }
}

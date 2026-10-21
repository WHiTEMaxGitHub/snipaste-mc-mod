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
    private int opacityTipX;
    private int opacityTipW;
    private int folderTipX;
    private int folderTipW;

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
        this.opacityBox = numberBox(left + opacityLabelWidth + labelGap, y, opacityBoxWidth, String.valueOf(opacityPercent()), 3);
        this.folderTipX = this.opacityBox.getX() + opacityBoxWidth + pairGap;
        this.folderBox = numberBox(this.folderTipX + folderLabelWidth + labelGap, y, folderBoxWidth, String.valueOf(SnipasteConfig.maxFolderMegabytes()), 5);
        this.opacityTipW = this.opacityBox.getX() + this.opacityBox.getWidth() - this.opacityTipX;
        this.folderTipW = this.folderBox.getX() + this.folderBox.getWidth() - this.folderTipX;
        y += ROW;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.border_page"), button -> {
                    commitAll();
                    this.minecraft.setScreen(new SnipasteBorderScreen(this));
                })
                .bounds(left, y, 150, 20)
                .build());
        addRenderableWidget(CycleButton.onOffBuilder()
                .withInitialValue(SnipasteConfig.allowStretch())
                .create(right, y, 150, 20, Component.translatable("screen.snipaste_mc_mod.allow_stretch"),
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
    }

    private void commitAll()
    {
        syncOpacity(true);
        syncFolder(true);
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
            Double value = NumberBoxes.parse(this.opacityBox.getValue());
            if (value != null && value >= 0.0D && value <= 100.0D)
            {
                SnipasteConfig.setDefaultOpacity(value / 100.0D);
            }
            return;
        }
        Double value = NumberBoxes.parse(this.opacityBox.getValue());
        if (value != null)
        {
            SnipasteConfig.setDefaultOpacity(value / 100.0D);
        }
        NumberBoxes.show(this.opacityBox, String.valueOf(opacityPercent()));
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
            Double value = NumberBoxes.parse(this.folderBox.getValue());
            if (value != null && value >= 0.0D && value <= 1024.0D)
            {
                setFolder((int) Math.round(value));
            }
            return;
        }
        Double value = NumberBoxes.parse(this.folderBox.getValue());
        if (value != null)
        {
            setFolder((int) Math.round(value));
        }
        NumberBoxes.show(this.folderBox, String.valueOf(SnipasteConfig.maxFolderMegabytes()));
        if (force)
        {
            ImageFolder.enforceLimit();
        }
    }

    private static void setFolder(int megabytes)
    {
        SnipasteConfig.addMaxFolderMegabytes(megabytes - SnipasteConfig.maxFolderMegabytes());
    }

    private static int opacityPercent()
    {
        return Math.max(0, Math.min(100, Math.round(SnipasteConfig.defaultOpacity() * 100.0F)));
    }

    private EditBox numberBox(int x, int y, int width, String value, int maxLength)
    {
        EditBox box = new EditBox(this.font, x, y, width, 20, Component.empty());
        box.setMaxLength(maxLength);
        box.setValue(value);
        box.setFilter(NumberBoxes::acceptsInteger);
        return addRenderableWidget(box);
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

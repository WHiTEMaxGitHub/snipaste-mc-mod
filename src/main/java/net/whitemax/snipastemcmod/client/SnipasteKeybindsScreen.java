package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SnipasteKeybindsScreen extends Screen
{
    private final Screen parent;
    private KeyMapping listeningKey;
    private boolean listeningClear;
    private KeyList list;
    private Button resetAll;

    public SnipasteKeybindsScreen(Screen parent)
    {
        super(Component.translatable("screen.snipaste_mc_mod.keys_title"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        this.list = new KeyList();
        addWidget(this.list);
        this.resetAll = addRenderableWidget(Button.builder(Component.translatable("controls.resetAll"), button -> resetAll())
                .bounds(this.width / 2 - 155, this.height - 29, 150, 20)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
                .build());
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    public boolean isListening()
    {
        return this.listeningKey != null || this.listeningClear;
    }

    @Override
    public void onClose()
    {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!isListening())
        {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
        if (this.listeningClear)
        {
            SnipasteKeybinds.setHide(key, SnipasteKeybinds.heldModifierMask());
            this.listeningClear = false;
        }
        else
        {
            SnipasteKeybinds.assign(this.listeningKey, key);
            this.listeningKey = null;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!isListening())
        {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            if (this.listeningClear)
            {
                SnipasteKeybinds.setHide(InputConstants.UNKNOWN, 0);
                this.listeningClear = false;
            }
            else
            {
                SnipasteKeybinds.assign(this.listeningKey, InputConstants.UNKNOWN);
                this.listeningKey = null;
            }
            return true;
        }
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if (this.listeningClear)
        {
            SnipasteKeybinds.setHide(key, modifiers);
            this.listeningClear = false;
        }
        else
        {
            SnipasteKeybinds.assign(this.listeningKey, key);
            this.listeningKey = null;
        }
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        this.resetAll.active = !allDefault();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void resetAll()
    {
        if (!SnipasteKeybinds.SNIP.isDefault())
        {
            SnipasteKeybinds.assign(SnipasteKeybinds.SNIP, SnipasteKeybinds.SNIP.getDefaultKey());
        }
        if (!SnipasteKeybinds.PASTE.isDefault())
        {
            SnipasteKeybinds.assign(SnipasteKeybinds.PASTE, SnipasteKeybinds.PASTE.getDefaultKey());
        }
        if (!SnipasteKeybinds.EDIT.isDefault())
        {
            SnipasteKeybinds.assign(SnipasteKeybinds.EDIT, SnipasteKeybinds.EDIT.getDefaultKey());
        }
        if (!SnipasteConfig.hideIsDefault())
        {
            SnipasteKeybinds.resetHide();
        }
    }

    private boolean allDefault()
    {
        return SnipasteKeybinds.SNIP.isDefault()
                && SnipasteKeybinds.PASTE.isDefault()
                && SnipasteKeybinds.EDIT.isDefault()
                && SnipasteConfig.hideIsDefault();
    }

    private void select(KeyMapping mapping)
    {
        this.listeningKey = mapping;
        this.listeningClear = false;
    }

    private void selectClear()
    {
        this.listeningKey = null;
        this.listeningClear = true;
    }

    private final class KeyList extends ContainerObjectSelectionList<KeyList.Row>
    {
        private int maxNameWidth;

        private KeyList()
        {
            super(SnipasteKeybindsScreen.this.minecraft, SnipasteKeybindsScreen.this.width + 45, SnipasteKeybindsScreen.this.height, 43, SnipasteKeybindsScreen.this.height - 32, 20);
            addEntry(new CategoryRow(Component.translatable(SnipasteKeybinds.CATEGORY)));
            addKey(SnipasteKeybinds.SNIP);
            addKey(SnipasteKeybinds.PASTE);
            addKey(SnipasteKeybinds.EDIT);
            Component clearName = Component.translatable("screen.snipaste_mc_mod.clear_key");
            this.maxNameWidth = Math.max(this.maxNameWidth, SnipasteKeybindsScreen.this.font.width(clearName));
            addEntry(new ClearRow(clearName));
        }

        private void addKey(KeyMapping mapping)
        {
            Component name = Component.translatable(mapping.getName());
            this.maxNameWidth = Math.max(this.maxNameWidth, SnipasteKeybindsScreen.this.font.width(name));
            addEntry(new KeyRow(mapping, name));
        }

        @Override
        protected int getScrollbarPosition()
        {
            return super.getScrollbarPosition() + 15;
        }

        @Override
        public int getRowWidth()
        {
            return super.getRowWidth() + 32;
        }

        private abstract class Row extends ContainerObjectSelectionList.Entry<Row>
        {
        }

        private final class CategoryRow extends Row
        {
            private final Component name;

            private CategoryRow(Component name)
            {
                this.name = name;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick)
            {
                graphics.drawCenteredString(SnipasteKeybindsScreen.this.font, this.name, SnipasteKeybindsScreen.this.width / 2, top + height - 10, 0xFFFFFF);
            }

            @Override
            public List<? extends GuiEventListener> children()
            {
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables()
            {
                return List.of();
            }
        }

        private abstract class BindRow extends Row
        {
            private final Component name;
            private final Button changeButton;
            private final Button resetButton;

            private BindRow(Component name)
            {
                this.name = name;
                this.changeButton = Button.builder(name, button -> select()).bounds(0, 0, 75, 20).build();
                this.resetButton = Button.builder(Component.translatable("controls.reset"), button -> reset()).bounds(0, 0, 50, 20).build();
            }

            abstract boolean listening();

            abstract boolean conflict();

            abstract boolean isDefault();

            abstract Component keyMessage();

            abstract void select();

            abstract void reset();

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick)
            {
                graphics.drawString(SnipasteKeybindsScreen.this.font, this.name, left + 90 - KeyList.this.maxNameWidth, top + height / 2 - 4, 0xFFFFFF, false);
                this.resetButton.setX(left + 190);
                this.resetButton.setY(top);
                this.resetButton.active = !isDefault();
                this.changeButton.setX(left + 105);
                this.changeButton.setY(top);
                Component message = keyMessage();
                if (listening())
                {
                    message = Component.literal("> ").append(message.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW);
                }
                else if (conflict())
                {
                    message = message.copy().withStyle(ChatFormatting.RED);
                }
                this.changeButton.setMessage(message);
                this.resetButton.render(graphics, mouseX, mouseY, partialTick);
                this.changeButton.render(graphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children()
            {
                return List.of(this.changeButton, this.resetButton);
            }

            @Override
            public List<? extends NarratableEntry> narratables()
            {
                return List.of(this.changeButton, this.resetButton);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button)
            {
                return this.changeButton.mouseClicked(mouseX, mouseY, button) || this.resetButton.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button)
            {
                return this.changeButton.mouseReleased(mouseX, mouseY, button) || this.resetButton.mouseReleased(mouseX, mouseY, button);
            }
        }

        private final class KeyRow extends BindRow
        {
            private final KeyMapping mapping;

            private KeyRow(KeyMapping mapping, Component name)
            {
                super(name);
                this.mapping = mapping;
            }

            @Override
            boolean listening()
            {
                return SnipasteKeybindsScreen.this.listeningKey == this.mapping;
            }

            @Override
            boolean conflict()
            {
                if (this.mapping.isUnbound())
                {
                    return false;
                }
                for (KeyMapping other : SnipasteKeybindsScreen.this.minecraft.options.keyMappings)
                {
                    if (other != this.mapping && this.mapping.same(other))
                    {
                        return true;
                    }
                }
                return SnipasteConfig.hideModifiers() == 0 && this.mapping.getKey().equals(SnipasteConfig.hideKey());
            }

            @Override
            boolean isDefault()
            {
                return this.mapping.isDefault();
            }

            @Override
            Component keyMessage()
            {
                return this.mapping.getTranslatedKeyMessage();
            }

            @Override
            void select()
            {
                SnipasteKeybindsScreen.this.select(this.mapping);
            }

            @Override
            void reset()
            {
                SnipasteKeybinds.assign(this.mapping, this.mapping.getDefaultKey());
            }
        }

        private final class ClearRow extends BindRow
        {
            private ClearRow(Component name)
            {
                super(name);
            }

            @Override
            boolean listening()
            {
                return SnipasteKeybindsScreen.this.listeningClear;
            }

            @Override
            boolean conflict()
            {
                InputConstants.Key key = SnipasteConfig.hideKey();
                if (SnipasteConfig.hideModifiers() != 0 || key.equals(InputConstants.UNKNOWN))
                {
                    return false;
                }
                for (KeyMapping mapping : SnipasteKeybindsScreen.this.minecraft.options.keyMappings)
                {
                    if (mapping.getKey().equals(key))
                    {
                        return true;
                    }
                }
                return false;
            }

            @Override
            boolean isDefault()
            {
                return SnipasteConfig.hideIsDefault();
            }

            @Override
            Component keyMessage()
            {
                return SnipasteKeybinds.hideMessage();
            }

            @Override
            void select()
            {
                SnipasteKeybindsScreen.this.selectClear();
            }

            @Override
            void reset()
            {
                SnipasteKeybinds.resetHide();
            }
        }
    }
}

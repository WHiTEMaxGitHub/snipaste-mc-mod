package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.whitemax.snipastemcmod.SnipasteMcMod;
import org.lwjgl.glfw.GLFW;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImagePickScreen extends Screen
{
    private final Screen parent;
    private ImageList list;
    private Button deleteButton;

    public ImagePickScreen(Screen parent)
    {
        super(Component.translatable("screen.snipaste_mc_mod.pick_title"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        if (this.list != null)
        {
            this.list.close();
        }
        this.list = new ImageList(this, this.minecraft, this.width, this.height);
        this.addWidget(this.list);
        this.list.reload();
        int y = this.height - 28;
        int x = this.width / 2 - 158;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.open_folder"), button -> {
            if (!ImageFolder.openFolder())
            {
                SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.open_failed"));
            }
        }).bounds(x, y, 100, 20).build());
        x += 104;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.refresh"), button -> this.list.reload())
                .bounds(x, y, 68, 20)
                .build());
        x += 72;
        this.deleteButton = addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.delete_short"), button -> deleteSelected())
                .bounds(x, y, 68, 20)
                .build());
        x += 72;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(x, y, 68, 20)
                .build());
        this.setFocused(this.list);
    }

    @Override
    public void onClose()
    {
        this.minecraft.setScreen(this.parent);
    }

    Screen previous()
    {
        return this.parent;
    }

    private void pasteAndReturn(Path path)
    {
        ImageFolder.choose(path);
        if (!StickyBoard.get().pasteLatest())
        {
            return;
        }
        if (this.parent instanceof StickyEditScreen edit)
        {
            edit.selectLast();
        }
        this.minecraft.setScreen(this.parent);
    }

    private void deleteSelected()
    {
        if (this.list == null)
        {
            return;
        }
        ImageList.Row row = this.list.getSelected();
        if (row == null)
        {
            return;
        }
        if (!ImageFolder.delete(row.path()))
        {
            SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.delete_failed"));
            return;
        }
        this.list.reload();
    }

    @Override
    public void removed()
    {
        if (this.list != null)
        {
            this.list.close();
            this.list = null;
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_SPACE && this.list != null)
        {
            ImageList.Row row = this.list.getSelected();
            if (row != null)
            {
                this.minecraft.setScreen(new ImagePreviewScreen(this, row.path()));
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        if (this.list != null)
        {
            this.list.render(graphics, mouseX, mouseY, partialTick);
        }
        if (this.deleteButton != null)
        {
            this.deleteButton.active = this.list != null && this.list.getSelected() != null;
        }
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.pick_hint"), this.width / 2, this.height - 44, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static BufferedImage thumbnail(BufferedImage source)
    {
        int max = 64;
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= max && height <= max)
        {
            return source;
        }
        float scale = max / (float) Math.max(width, height);
        int targetWidth = Math.max(1, Math.round(width * scale));
        int targetHeight = Math.max(1, Math.round(height * scale));
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return scaled;
    }

    private static final class ImageList extends ObjectSelectionList<ImageList.Row>
    {
        private final ImagePickScreen screen;
        private int nextId;

        private ImageList(ImagePickScreen screen, Minecraft minecraft, int width, int height)
        {
            super(minecraft, width, height, 32, height - 52, 40);
            this.screen = screen;
            this.setRenderBackground(true);
            this.setRenderTopAndBottom(false);
        }

        private void reload()
        {
            close();
            Path chosen = ImageFolder.chosen();
            Row selected = null;
            try
            {
                for (ImageFolder.StoredImage image : ImageFolder.images())
                {
                    Row row = Row.load(this, image);
                    if (row == null)
                    {
                        continue;
                    }
                    addEntry(row);
                    if (chosen != null && chosen.equals(image.path()))
                    {
                        selected = row;
                    }
                }
            }
            catch (IOException exception)
            {
                SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.read_failed"));
            }
            if (selected != null)
            {
                setSelected(selected);
            }
        }

        private void close()
        {
            for (Row row : this.children())
            {
                row.release();
            }
            clearEntries();
            setSelected(null);
        }

        private static final class Row extends ObjectSelectionList.Entry<Row>
        {
            private final ImageList list;
            private final Path path;
            private final DynamicTexture texture;
            private final ResourceLocation location;
            private final int sourceWidth;
            private final int sourceHeight;
            private final int fullWidth;
            private final int fullHeight;
            private boolean released;
            private long lastClickAt;

            private Row(ImageList list, Path path, DynamicTexture texture, ResourceLocation location, int sourceWidth, int sourceHeight, int fullWidth, int fullHeight)
            {
                this.list = list;
                this.path = path;
                this.texture = texture;
                this.location = location;
                this.sourceWidth = sourceWidth;
                this.sourceHeight = sourceHeight;
                this.fullWidth = fullWidth;
                this.fullHeight = fullHeight;
            }

            private static Row load(ImageList list, ImageFolder.StoredImage image)
            {
                try
                {
                    BufferedImage full = ImageFolder.read(image.path());
                    BufferedImage thumb = thumbnail(full);
                    NativeImage nativeImage = Pixels.toNative(thumb);
                    DynamicTexture texture = new DynamicTexture(nativeImage);
                    ResourceLocation location = Minecraft.getInstance().getTextureManager().register(
                            SnipasteMcMod.MOD_ID + "/pick/" + list.nextId++,
                            texture);
                    return new Row(list, image.path(), texture, location, nativeImage.getWidth(), nativeImage.getHeight(), full.getWidth(), full.getHeight());
                }
                catch (IOException | RuntimeException exception)
                {
                    return null;
                }
            }

            private Path path()
            {
                return this.path;
            }

            private void release()
            {
                if (this.released)
                {
                    return;
                }
                this.released = true;
                this.texture.close();
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft != null)
                {
                    minecraft.getTextureManager().release(this.location);
                }
            }

            @Override
            public Component getNarration()
            {
                return Component.literal(ImageFolder.displayName(this.path));
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick)
            {
                int box = 32;
                float scale = Math.min(box / (float) this.sourceWidth, box / (float) this.sourceHeight);
                int drawWidth = Math.max(1, Math.round(this.sourceWidth * scale));
                int drawHeight = Math.max(1, Math.round(this.sourceHeight * scale));
                int drawX = left + 4 + (box - drawWidth) / 2;
                int drawY = top + (height - drawHeight) / 2;
                graphics.blit(
                        this.location,
                        drawX,
                        drawY,
                        drawWidth,
                        drawHeight,
                        0.0F,
                        0.0F,
                        this.sourceWidth,
                        this.sourceHeight,
                        this.sourceWidth,
                        this.sourceHeight);
                Minecraft minecraft = Minecraft.getInstance();
                String name = minecraft.font.plainSubstrByWidth(ImageFolder.displayName(this.path), width - 48);
                int color = ImageFolder.chosen() != null && ImageFolder.chosen().equals(this.path) ? 0xFFFF55 : 0xFFFFFF;
                graphics.drawString(minecraft.font, name, left + 44, top + 8, color, false);
                graphics.drawString(minecraft.font, this.fullWidth + " x " + this.fullHeight, left + 44, top + 20, 0xA0A0A0, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button)
            {
                if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
                {
                    return false;
                }
                long now = Util.getMillis();
                boolean again = this.list.getSelected() == this && now - this.lastClickAt < 250L;
                this.lastClickAt = now;
                this.list.setSelected(this);
                ImageFolder.choose(this.path);
                if (again)
                {
                    this.list.screen.pasteAndReturn(this.path);
                }
                return true;
            }
        }
    }
}

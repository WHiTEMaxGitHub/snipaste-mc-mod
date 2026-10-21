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
    private Button renameButton;
    private Button backButton;
    private Path folder;
    private Path highlight;

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
        this.backButton = addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> back())
                .bounds(8, 4, 56, 20)
                .build());
        int y = this.height - 28;
        int x = this.width / 2 - 158;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.open_folder"), button -> {
            if (!ImageFolder.open(folder()))
            {
                SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.open_failed"));
            }
        }).bounds(x, y, 88, 20).build());
        x += 92;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.refresh"), button -> this.list.reload())
                .bounds(x, y, 50, 20)
                .build());
        x += 54;
        this.renameButton = addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.rename"), button -> renameSelected())
                .bounds(x, y, 62, 20)
                .build());
        x += 66;
        this.deleteButton = addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.delete_short"), button -> deleteSelected())
                .bounds(x, y, 50, 20)
                .build());
        x += 54;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(x, y, 50, 20)
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
        if (!StickyBoard.get().pasteFile(path))
        {
            return;
        }
        if (this.parent instanceof StickyEditScreen edit)
        {
            edit.selectLast();
        }
        this.minecraft.setScreen(this.parent);
    }

    private Path folder()
    {
        if (this.folder == null)
        {
            this.folder = ImageFolder.directory();
        }
        return this.folder;
    }

    private boolean atRoot()
    {
        return folder().toAbsolutePath().normalize().equals(ImageFolder.root());
    }

    private void enter(Path path)
    {
        this.folder = path;
        if (this.list != null)
        {
            this.list.reload();
        }
    }

    private void back()
    {
        Path parent = folder().toAbsolutePath().normalize().getParent();
        if (parent == null || !ImageFolder.contains(parent))
        {
            return;
        }
        this.folder = parent;
        if (this.list != null)
        {
            this.list.reload();
        }
    }

    void highlight(Path path)
    {
        this.highlight = path;
    }

    private void renameSelected()
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
        this.minecraft.setScreen(new RenameImageScreen(this, row.path()));
    }

    private void deleteSelected()
    {
        if (this.list == null)
        {
            return;
        }
        ImageList.Row row = this.list.getSelected();
        if (row == null || row.isDirectory())
        {
            return;
        }
        DeleteImageScreen.ask(this, row.path());
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
                if (row.isDirectory())
                {
                    enter(row.path());
                }
                else if (row.isText())
                {
                    pasteAndReturn(row.path());
                }
                else
                {
                    this.minecraft.setScreen(new ImagePreviewScreen(this, row.path()));
                }
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
        if (this.backButton != null)
        {
            this.backButton.visible = !atRoot();
        }
        ImageList.Row selected = this.list == null ? null : this.list.getSelected();
        if (this.renameButton != null)
        {
            this.renameButton.active = selected != null;
        }
        if (this.deleteButton != null)
        {
            this.deleteButton.active = selected != null && !selected.isDirectory();
        }
        Component heading = atRoot() ? this.title : Component.literal(folder().getFileName().toString());
        graphics.drawCenteredString(this.font, heading, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.pick_hint"), this.width / 2, this.height - 44, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static void drawFolder(GuiGraphics graphics, int x, int y)
    {
        graphics.fill(x, y + 1, x + 12, y + 6, 0xFFC8962E);
        graphics.fill(x, y + 5, x + 26, y + 20, 0xFFC08020);
        graphics.fill(x + 1, y + 6, x + 25, y + 19, 0xFFF0C14A);
        graphics.fill(x + 1, y + 2, x + 11, y + 5, 0xFFF6D36A);
    }

    private static void drawText(GuiGraphics graphics, int x, int y)
    {
        graphics.fill(x, y, x + 22, y + 26, 0xFF3A3A3A);
        graphics.fill(x + 1, y + 1, x + 21, y + 25, 0xFFE8E8E8);
        graphics.fill(x + 4, y + 6, x + 18, y + 8, 0xFF888888);
        graphics.fill(x + 4, y + 11, x + 18, y + 13, 0xFF888888);
        graphics.fill(x + 4, y + 16, x + 14, y + 18, 0xFF888888);
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
            Path chosen = this.screen.highlight != null ? this.screen.highlight : ImageFolder.chosen();
            this.screen.highlight = null;
            Row selected = null;
            try
            {
                for (ImageFolder.Entry entry : ImageFolder.children(this.screen.folder()))
                {
                    Row row = entry.directory()
                            ? Row.folder(this, entry.path())
                            : ImageFolder.isText(entry.path()) ? Row.text(this, entry.path()) : Row.load(this, entry.path());
                    if (row == null)
                    {
                        continue;
                    }
                    addEntry(row);
                    if (chosen != null && chosen.equals(entry.path()))
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
            private final boolean directory;
            private final boolean text;
            private final String snippet;
            private final DynamicTexture texture;
            private final ResourceLocation location;
            private final int sourceWidth;
            private final int sourceHeight;
            private final int fullWidth;
            private final int fullHeight;
            private boolean released;
            private long lastClickAt;

            private Row(ImageList list, Path path, boolean directory, boolean text, String snippet, DynamicTexture texture, ResourceLocation location, int sourceWidth, int sourceHeight, int fullWidth, int fullHeight)
            {
                this.list = list;
                this.path = path;
                this.directory = directory;
                this.text = text;
                this.snippet = snippet;
                this.texture = texture;
                this.location = location;
                this.sourceWidth = sourceWidth;
                this.sourceHeight = sourceHeight;
                this.fullWidth = fullWidth;
                this.fullHeight = fullHeight;
            }

            private static Row folder(ImageList list, Path path)
            {
                return new Row(list, path, true, false, "", null, null, 0, 0, 0, 0);
            }

            private static Row text(ImageList list, Path path)
            {
                String snippet = "";
                try
                {
                    String raw = ImageFolder.readText(path);
                    int line = raw.indexOf('\n');
                    snippet = (line >= 0 ? raw.substring(0, line) : raw).trim();
                }
                catch (IOException exception)
                {
                    snippet = "";
                }
                return new Row(list, path, false, true, snippet, null, null, 0, 0, 0, 0);
            }

            private static Row load(ImageList list, Path path)
            {
                try
                {
                    BufferedImage full = ImageFolder.read(path);
                    BufferedImage thumb = thumbnail(full);
                    NativeImage nativeImage = Pixels.toNative(thumb);
                    DynamicTexture texture = new DynamicTexture(nativeImage);
                    ResourceLocation location = Minecraft.getInstance().getTextureManager().register(
                            SnipasteMcMod.MOD_ID + "/pick/" + list.nextId++,
                            texture);
                    return new Row(list, path, false, false, "", texture, location, nativeImage.getWidth(), nativeImage.getHeight(), full.getWidth(), full.getHeight());
                }
                catch (IOException | RuntimeException exception)
                {
                    return null;
                }
            }

            private boolean isDirectory()
            {
                return this.directory;
            }

            private boolean isText()
            {
                return this.text;
            }

            private Path path()
            {
                return this.path;
            }

            private void release()
            {
                if (this.released || this.texture == null)
                {
                    this.released = true;
                    return;
                }
                this.released = true;
                this.texture.close();
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft != null && this.location != null)
                {
                    minecraft.getTextureManager().release(this.location);
                }
            }

            @Override
            public Component getNarration()
            {
                return Component.literal(this.path.getFileName().toString());
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick)
            {
                Minecraft minecraft = Minecraft.getInstance();
                String name = minecraft.font.plainSubstrByWidth(this.path.getFileName().toString(), width - 48);
                if (this.directory)
                {
                    drawFolder(graphics, left + 6, top + (height - 22) / 2);
                    graphics.drawString(minecraft.font, name, left + 44, top + (height - 8) / 2, 0xFFFFFF, false);
                    return;
                }
                if (this.text)
                {
                    drawText(graphics, left + 6, top + (height - 26) / 2);
                    int color = ImageFolder.chosen() != null && ImageFolder.chosen().equals(this.path) ? 0xFFFF55 : 0xFFFFFF;
                    graphics.drawString(minecraft.font, name, left + 44, top + 8, color, false);
                    String line = minecraft.font.plainSubstrByWidth(this.snippet, width - 48);
                    graphics.drawString(minecraft.font, line, left + 44, top + 20, 0xA0A0A0, false);
                    return;
                }
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
                if (this.directory)
                {
                    this.list.screen.enter(this.path);
                    return true;
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

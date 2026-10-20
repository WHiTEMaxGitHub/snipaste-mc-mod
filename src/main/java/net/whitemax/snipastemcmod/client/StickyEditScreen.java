package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class StickyEditScreen extends Screen
{
    private static final int MIN_SIZE = 24;
    private static final int BUTTON_WIDTH = 76;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;

    private int selected = -1;
    private Edge drag = Edge.NONE;
    private int originX;
    private int originY;
    private int originWidth;
    private int originHeight;
    private double grabX;
    private double grabY;
    private int barX;
    private int barY;
    private int barWidth;
    private int barHeight;
    private Button toggleButton;
    private Button deleteButton;
    private Button copyButton;
    private Button settingsButton;

    private static long resizeHorizontal;
    private static long resizeVertical;
    private static long handCursor;

    public StickyEditScreen()
    {
        super(Component.translatable("screen.snipaste_mc_mod.edit"));
    }

    public boolean isRebinding()
    {
        return false;
    }

    public void selectLast()
    {
        this.selected = StickyBoard.get().size() - 1;
    }

    @Override
    protected void init()
    {
        placeBar();
        int x = this.barX + 4;
        int y = this.barY + 4;
        this.deleteButton = addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.delete_short"), button -> deleteSelected())
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        x += BUTTON_WIDTH + BUTTON_GAP;
        this.copyButton = addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.copy_short"), button -> copySelected())
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        x += BUTTON_WIDTH + BUTTON_GAP;
        this.toggleButton = Button.builder(toggleLabel(), button -> {
            StickyBoard.get().setShown(!StickyBoard.get().shown());
            button.setMessage(toggleLabel());
        }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(this.toggleButton);
        x += BUTTON_WIDTH + BUTTON_GAP;
        this.settingsButton = addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.settings"), button ->
                this.minecraft.setScreen(new SnipasteConfigScreen(this)))
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        placeBar();
        positionButtons();
        if (this.toggleButton != null)
        {
            this.toggleButton.setMessage(toggleLabel());
        }
        StickyRenderer.draw(graphics, true, this.selected);
        graphics.fill(this.barX, this.barY, this.barX + this.barWidth, this.barY + this.barHeight, 0xC0101010);
        graphics.drawCenteredString(this.font, Component.translatable("screen.snipaste_mc_mod.hint_edit"), this.width / 2, this.barY - 12, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
        drawTooltip(graphics, mouseX, mouseY);
        updateCursor(mouseX, mouseY);
    }

    @Override
    public void removed()
    {
        resetCursor();
        super.removed();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (super.mouseClicked(mouseX, mouseY, button))
        {
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            return false;
        }
        if (insideBar(mouseX, mouseY))
        {
            return true;
        }
        if (closeSelected(mouseX, mouseY))
        {
            return true;
        }

        int index = StickyBoard.get().topmostAt(mouseX, mouseY);
        if (index < 0)
        {
            this.selected = -1;
            this.drag = Edge.NONE;
            return true;
        }
        this.selected = StickyBoard.get().bringToFront(index);
        StickyImage image = StickyBoard.get().get(this.selected);
        this.drag = image == null ? Edge.NONE : edgeAt(image, mouseX, mouseY);
        if (this.drag == Edge.NONE)
        {
            this.drag = Edge.BODY;
        }
        remember(image);
        this.grabX = mouseX;
        this.grabY = mouseY;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
        {
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            return false;
        }
        StickyImage image = StickyBoard.get().get(this.selected);
        if (image == null || this.drag == Edge.NONE)
        {
            return false;
        }
        resize(image, mouseX, mouseY, hasShiftDown());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        this.drag = Edge.NONE;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (insideBar(mouseX, mouseY))
        {
            return true;
        }
        int index = StickyBoard.get().topmostAt(mouseX, mouseY);
        if (index < 0)
        {
            index = this.selected;
        }
        StickyImage image = StickyBoard.get().get(index);
        if (image == null)
        {
            SnipasteConfig.setDefaultOpacity(SnipasteConfig.defaultOpacity() + delta * 0.05D);
            return true;
        }
        this.selected = index;
        image.setOpacity(image.opacity() + (float) delta * 0.05F);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if ((keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) && this.selected >= 0)
        {
            deleteSelected();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void placeBar()
    {
        int count = 4;
        this.barWidth = count * BUTTON_WIDTH + (count - 1) * BUTTON_GAP + 8;
        this.barHeight = BUTTON_HEIGHT + 8;
        this.barX = (this.width - this.barWidth) / 2;
        this.barY = this.height - 8 - this.barHeight;
    }

    private void positionButtons()
    {
        int x = this.barX + 4;
        int y = this.barY + 4;
        for (var child : this.children())
        {
            if (child instanceof Button button)
            {
                button.setX(x);
                button.setY(y);
                x += BUTTON_WIDTH + BUTTON_GAP;
            }
        }
    }

    private void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        for (var child : this.children())
        {
            if (!(child instanceof Button button) || !button.isHovered())
            {
                continue;
            }
            Component tip = tooltipFor(button);
            if (tip != null)
            {
                graphics.renderTooltip(this.font, tip, mouseX, mouseY);
            }
            return;
        }
    }

    private Component tooltipFor(Button button)
    {
        if (button == this.toggleButton)
        {
            return Component.translatable(StickyBoard.get().shown()
                    ? "screen.snipaste_mc_mod.tooltip_hide"
                    : "screen.snipaste_mc_mod.tooltip_show");
        }
        if (button == this.deleteButton)
        {
            return Component.translatable("screen.snipaste_mc_mod.tooltip_delete");
        }
        if (button == this.copyButton)
        {
            return Component.translatable("screen.snipaste_mc_mod.tooltip_copy");
        }
        if (button == this.settingsButton)
        {
            return Component.translatable("screen.snipaste_mc_mod.tooltip_settings");
        }
        return null;
    }

    private static Component toggleLabel()
    {
        return Component.translatable(StickyBoard.get().shown()
                ? "screen.snipaste_mc_mod.hide_short"
                : "screen.snipaste_mc_mod.show_short");
    }

    private boolean insideBar(double mouseX, double mouseY)
    {
        return mouseX >= this.barX && mouseX < this.barX + this.barWidth
                && mouseY >= this.barY && mouseY < this.barY + this.barHeight;
    }

    private void deleteSelected()
    {
        if (this.selected < 0)
        {
            return;
        }
        StickyBoard.get().remove(this.selected);
        this.selected = -1;
        this.drag = Edge.NONE;
    }

    private void copySelected()
    {
        StickyImage image = StickyBoard.get().get(this.selected);
        if (image == null || image.texture().getPixels() == null)
        {
            return;
        }
        if (ImageFolder.save(Pixels.crop(image.texture().getPixels(), 0, 0, image.sourceWidth(), image.sourceHeight())))
        {
            SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.saved"));
        }
        else
        {
            SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.save_failed"));
        }
    }

    private boolean closeSelected(double mouseX, double mouseY)
    {
        StickyImage image = StickyBoard.get().get(this.selected);
        if (image == null)
        {
            return false;
        }
        int size = 12;
        int x = image.x() + image.width() - size;
        int y = image.y();
        if (mouseX < x || mouseX >= x + size || mouseY < y || mouseY >= y + size)
        {
            return false;
        }
        deleteSelected();
        return true;
    }

    private void remember(StickyImage image)
    {
        if (image == null)
        {
            return;
        }
        this.originX = image.x();
        this.originY = image.y();
        this.originWidth = image.width();
        this.originHeight = image.height();
    }

    private void resize(StickyImage image, double mouseX, double mouseY, boolean lockAspect)
    {
        double dx = mouseX - this.grabX;
        double dy = mouseY - this.grabY;
        int x = this.originX;
        int y = this.originY;
        int width = this.originWidth;
        int height = this.originHeight;
        switch (this.drag)
        {
            case E -> width = this.originWidth + (int) dx;
            case S -> height = this.originHeight + (int) dy;
            case W ->
            {
                x = this.originX + (int) dx;
                width = this.originWidth - (int) dx;
            }
            case N ->
            {
                y = this.originY + (int) dy;
                height = this.originHeight - (int) dy;
            }
            case SE ->
            {
                width = this.originWidth + (int) dx;
                height = this.originHeight + (int) dy;
            }
            case NE ->
            {
                y = this.originY + (int) dy;
                height = this.originHeight - (int) dy;
                width = this.originWidth + (int) dx;
            }
            case SW ->
            {
                x = this.originX + (int) dx;
                width = this.originWidth - (int) dx;
                height = this.originHeight + (int) dy;
            }
            case NW ->
            {
                x = this.originX + (int) dx;
                width = this.originWidth - (int) dx;
                y = this.originY + (int) dy;
                height = this.originHeight - (int) dy;
            }
            case BODY ->
            {
                x = this.originX + (int) dx;
                y = this.originY + (int) dy;
            }
            case NONE ->
            {
                return;
            }
        }

        if (lockAspect && this.drag != Edge.BODY && this.originHeight > 0)
        {
            float aspect = this.originWidth / (float) this.originHeight;
            boolean horizontal = this.drag == Edge.E || this.drag == Edge.W;
            boolean vertical = this.drag == Edge.N || this.drag == Edge.S;
            if (horizontal)
            {
                height = Math.round(width / aspect);
            }
            else if (vertical)
            {
                width = Math.round(height * aspect);
            }
            else if (Math.abs(dx) / this.originWidth >= Math.abs(dy) / this.originHeight)
            {
                height = Math.round(width / aspect);
            }
            else
            {
                width = Math.round(height * aspect);
            }
            if (this.drag == Edge.W || this.drag == Edge.NW || this.drag == Edge.SW)
            {
                x = this.originX + this.originWidth - width;
            }
            if (this.drag == Edge.N || this.drag == Edge.NW || this.drag == Edge.NE)
            {
                y = this.originY + this.originHeight - height;
            }
        }

        if (width < MIN_SIZE || height < MIN_SIZE)
        {
            return;
        }
        if (this.drag == Edge.BODY)
        {
            x = Mth.clamp(x, 16 - width, this.width - 16);
            y = Mth.clamp(y, 16 - height, this.height - 16);
        }
        image.place(x, y, width, height);
    }

    private Edge edgeAt(StickyImage image, double mouseX, double mouseY)
    {
        int margin = 5;
        boolean nearLeft = Math.abs(mouseX - image.x()) <= margin;
        boolean nearRight = Math.abs(mouseX - (image.x() + image.width())) <= margin;
        boolean nearTop = Math.abs(mouseY - image.y()) <= margin;
        boolean nearBottom = Math.abs(mouseY - (image.y() + image.height())) <= margin;
        boolean insideX = mouseX >= image.x() - margin && mouseX <= image.x() + image.width() + margin;
        boolean insideY = mouseY >= image.y() - margin && mouseY <= image.y() + image.height() + margin;
        if (!insideX || !insideY)
        {
            return Edge.NONE;
        }
        if (nearTop && nearLeft)
        {
            return Edge.NW;
        }
        if (nearTop && nearRight)
        {
            return Edge.NE;
        }
        if (nearBottom && nearLeft)
        {
            return Edge.SW;
        }
        if (nearBottom && nearRight)
        {
            return Edge.SE;
        }
        if (nearTop)
        {
            return Edge.N;
        }
        if (nearBottom)
        {
            return Edge.S;
        }
        if (nearLeft)
        {
            return Edge.W;
        }
        if (nearRight)
        {
            return Edge.E;
        }
        return image.contains(mouseX, mouseY) ? Edge.BODY : Edge.NONE;
    }

    private void updateCursor(int mouseX, int mouseY)
    {
        long window = this.minecraft.getWindow().getWindow();
        long cursor = 0L;
        StickyImage image = StickyBoard.get().get(this.selected);
        if (image != null && !insideBar(mouseX, mouseY))
        {
            Edge edge = edgeAt(image, mouseX, mouseY);
            if (edge == Edge.E || edge == Edge.W)
            {
                cursor = standardCursor(GLFW.GLFW_HRESIZE_CURSOR);
            }
            else if (edge == Edge.N || edge == Edge.S)
            {
                cursor = standardCursor(GLFW.GLFW_VRESIZE_CURSOR);
            }
            else if (edge == Edge.BODY || edge == Edge.NE || edge == Edge.NW || edge == Edge.SE || edge == Edge.SW)
            {
                cursor = standardCursor(GLFW.GLFW_HAND_CURSOR);
            }
        }
        GLFW.glfwSetCursor(window, cursor);
    }

    private static long standardCursor(int shape)
    {
        if (shape == GLFW.GLFW_HRESIZE_CURSOR)
        {
            if (resizeHorizontal == 0L)
            {
                resizeHorizontal = GLFW.glfwCreateStandardCursor(shape);
            }
            return resizeHorizontal;
        }
        if (shape == GLFW.GLFW_VRESIZE_CURSOR)
        {
            if (resizeVertical == 0L)
            {
                resizeVertical = GLFW.glfwCreateStandardCursor(shape);
            }
            return resizeVertical;
        }
        if (handCursor == 0L)
        {
            handCursor = GLFW.glfwCreateStandardCursor(shape);
        }
        return handCursor;
    }

    private void resetCursor()
    {
        if (this.minecraft != null)
        {
            GLFW.glfwSetCursor(this.minecraft.getWindow().getWindow(), 0L);
        }
    }

    private enum Edge
    {
        N, S, E, W, NE, NW, SE, SW, BODY, NONE
    }
}

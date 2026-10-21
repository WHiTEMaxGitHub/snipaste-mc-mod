package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import org.lwjgl.system.windows.User32;

import java.nio.ByteBuffer;

public class StickyEditScreen extends Screen
{
    private static final int MIN_SIZE = 24;
    private static final int HANDLE_MARGIN = 6;
    private static final int BUTTON_WIDTH = 76;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int GROUP_GAP = 16;

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
    private Button deleteButton;
    private Button pickButton;
    private Button settingsButton;

    private static long resizeHorizontal;
    private static long resizeVertical;
    private static long resizeNwse;
    private static long resizeNesw;
    private static long handCursor;
    private static boolean diagonalCursorsReady;
    private static ByteBuffer nwseCursorBlock;
    private static ByteBuffer neswCursorBlock;

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
        this.pickButton = addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.pick_short"), button ->
                this.minecraft.setScreen(new ImagePickScreen(this)))
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        x += BUTTON_WIDTH + BUTTON_GAP;
        this.deleteButton = addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.delete_short"), button -> deleteSelected())
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        x += BUTTON_WIDTH + GROUP_GAP;
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
        if (this.deleteButton != null)
        {
            this.deleteButton.active = this.selected >= 0;
        }
        if (StickyBoard.get().shown())
        {
            StickyRenderer.draw(graphics, true, this.selected);
        }
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
        if (insideBar(mouseX, mouseY) || !StickyBoard.get().shown())
        {
            return true;
        }
        int index = StickyBoard.get().topmostAt(mouseX, mouseY);
        if (index >= 0)
        {
            if (index == this.selected && closeSelected(mouseX, mouseY))
            {
                return true;
            }
            this.selected = StickyBoard.get().bringToFront(index);
            StickyImage image = StickyBoard.get().get(this.selected);
            this.drag = image == null ? Edge.BODY : edgeAt(image, mouseX, mouseY);
            if (this.drag == Edge.NONE)
            {
                this.drag = Edge.BODY;
            }
            remember(image);
            this.grabX = mouseX;
            this.grabY = mouseY;
            return true;
        }
        if (closeSelected(mouseX, mouseY))
        {
            return true;
        }
        StickyImage selectedImage = StickyBoard.get().get(this.selected);
        if (selectedImage != null)
        {
            Edge edge = edgeAt(selectedImage, mouseX, mouseY);
            if (edge != Edge.NONE && edge != Edge.BODY)
            {
                this.drag = edge;
                remember(selectedImage);
                this.grabX = mouseX;
                this.grabY = mouseY;
                return true;
            }
        }
        this.selected = -1;
        this.drag = Edge.NONE;
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
        if (image == null || this.drag == Edge.NONE || !StickyBoard.get().shown())
        {
            return false;
        }
        if (isCorner(this.drag))
        {
            resizeCorner(image, mouseX, mouseY);
        }
        else
        {
            resize(image, mouseX, mouseY, hasShiftDown() || !SnipasteConfig.allowStretch());
        }
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
        this.barWidth = 3 * BUTTON_WIDTH + BUTTON_GAP + GROUP_GAP + 8;
        this.barHeight = BUTTON_HEIGHT + 8;
        this.barX = (this.width - this.barWidth) / 2;
        this.barY = this.height - 8 - this.barHeight;
    }

    private void positionButtons()
    {
        int x = this.barX + 4;
        int y = this.barY + 4;
        this.pickButton.setX(x);
        this.pickButton.setY(y);
        x += BUTTON_WIDTH + BUTTON_GAP;
        this.deleteButton.setX(x);
        this.deleteButton.setY(y);
        x += BUTTON_WIDTH + GROUP_GAP;
        this.settingsButton.setX(x);
        this.settingsButton.setY(y);
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
        if (button == this.deleteButton)
        {
            return Component.translatable("screen.snipaste_mc_mod.tooltip_delete");
        }
        if (button == this.pickButton)
        {
            return Component.translatable("screen.snipaste_mc_mod.tooltip_pick");
        }
        if (button == this.settingsButton)
        {
            return Component.translatable("screen.snipaste_mc_mod.tooltip_settings");
        }
        return null;
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

    private boolean closeSelected(double mouseX, double mouseY)
    {
        StickyImage image = StickyBoard.get().get(this.selected);
        if (image == null || !StickyRenderer.overClose(image, mouseX, mouseY))
        {
            return false;
        }
        deleteSelected();
        return true;
    }

    private static boolean isCorner(Edge edge)
    {
        return edge == Edge.NE || edge == Edge.NW || edge == Edge.SE || edge == Edge.SW;
    }

    private void resizeCorner(StickyImage image, double mouseX, double mouseY)
    {
        boolean left = this.drag == Edge.W || this.drag == Edge.NW || this.drag == Edge.SW;
        boolean top = this.drag == Edge.N || this.drag == Edge.NW || this.drag == Edge.NE;
        int anchorX = left ? this.originX + this.originWidth : this.originX;
        int anchorY = top ? this.originY + this.originHeight : this.originY;
        double rawWidth = Math.abs(mouseX - anchorX);
        double rawHeight = Math.abs(mouseY - anchorY);
        float aspect = this.originHeight <= 0 ? 1.0F : this.originWidth / (float) this.originHeight;
        int width;
        int height;
        if (rawHeight < 0.5D || rawWidth / rawHeight >= aspect)
        {
            width = (int) Math.round(rawWidth);
            height = Math.round(width / aspect);
        }
        else
        {
            height = (int) Math.round(rawHeight);
            width = Math.round(height * aspect);
        }
        if (width < MIN_SIZE)
        {
            width = MIN_SIZE;
            height = Math.max(MIN_SIZE, Math.round(width / aspect));
        }
        if (height < MIN_SIZE)
        {
            height = MIN_SIZE;
            width = Math.max(MIN_SIZE, Math.round(height * aspect));
        }
        int x = left ? anchorX - width : anchorX;
        int y = top ? anchorY - height : anchorY;
        image.place(x, y, width, height);
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
        int margin = HANDLE_MARGIN;
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
        if (!StickyBoard.get().shown() || insideBar(mouseX, mouseY))
        {
            GLFW.glfwSetCursor(window, 0L);
            return;
        }
        StickyImage selectedImage = StickyBoard.get().get(this.selected);
        if (this.drag == Edge.NONE && selectedImage != null && StickyRenderer.overClose(selectedImage, mouseX, mouseY))
        {
            GLFW.glfwSetCursor(window, 0L);
            return;
        }
        Edge edge = this.drag;
        if (edge == Edge.NONE)
        {
            int top = StickyBoard.get().topmostAt(mouseX, mouseY);
            StickyImage image = top >= 0 ? StickyBoard.get().get(top) : selectedImage;
            edge = image == null ? Edge.NONE : edgeAt(image, mouseX, mouseY);
            if (top < 0 && (edge == Edge.NONE || edge == Edge.BODY))
            {
                edge = Edge.NONE;
            }
        }
        GLFW.glfwSetCursor(window, cursorFor(edge));
    }

    private static long cursorFor(Edge edge)
    {
        return switch (edge)
        {
            case E, W -> standardCursor(GLFW.GLFW_HRESIZE_CURSOR);
            case N, S -> standardCursor(GLFW.GLFW_VRESIZE_CURSOR);
            case NW, SE -> diagonalCursor(true);
            case NE, SW -> diagonalCursor(false);
            case BODY -> standardCursor(GLFW.GLFW_HAND_CURSOR);
            case NONE -> 0L;
        };
    }

    private static long diagonalCursor(boolean nwse)
    {
        if (!diagonalCursorsReady)
        {
            resizeNwse = systemDiagonalCursor(true);
            resizeNesw = systemDiagonalCursor(false);
            if (resizeNwse == 0L)
            {
                resizeNwse = buildDiagonalCursor(true);
            }
            if (resizeNesw == 0L)
            {
                resizeNesw = buildDiagonalCursor(false);
            }
            diagonalCursorsReady = true;
        }
        long cursor = nwse ? resizeNwse : resizeNesw;
        return cursor == 0L ? standardCursor(GLFW.GLFW_HAND_CURSOR) : cursor;
    }

    private static long systemDiagonalCursor(boolean nwse)
    {
        if (Platform.get() != Platform.WINDOWS)
        {
            return 0L;
        }
        return windowsDiagonalCursor(nwse);
    }

    private static long windowsDiagonalCursor(boolean nwse)
    {
        int id = nwse ? User32.IDC_SIZENWSE : User32.IDC_SIZENESW;
        long handle = User32.nLoadCursor(0L, id);
        if (handle == 0L)
        {
            return 0L;
        }
        // GLFW 3.3 has no diagonal standard cursor. Its Windows cursor object is
        // { next, HCURSOR } and the window proc only reads the handle.
        ByteBuffer block = MemoryUtil.memCalloc(16);
        block.putLong(8, handle);
        if (nwse)
        {
            nwseCursorBlock = block;
        }
        else
        {
            neswCursorBlock = block;
        }
        return MemoryUtil.memAddress(block);
    }

    private static long buildDiagonalCursor(boolean nwse)
    {
        int size = 32;
        boolean[] core = new boolean[size * size];
        if (nwse)
        {
            arrowHead(core, size, 5, 5, 1, 1);
            arrowHead(core, size, 26, 26, -1, -1);
            shaft(core, size, 12, 12, 19, 19);
        }
        else
        {
            arrowHead(core, size, 26, 5, -1, 1);
            arrowHead(core, size, 5, 26, 1, -1);
            shaft(core, size, 19, 12, 12, 19);
        }
        ByteBuffer pixels = MemoryUtil.memAlloc(size * size * 4);
        GLFWImage image = GLFWImage.malloc();
        try
        {
            for (int y = 0; y < size; y++)
            {
                for (int x = 0; x < size; x++)
                {
                    boolean filled = core[y * size + x];
                    boolean outline = !filled && touchesCore(core, size, x, y);
                    if (filled)
                    {
                        pixels.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255);
                    }
                    else if (outline)
                    {
                        pixels.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 255);
                    }
                    else
                    {
                        pixels.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0);
                    }
                }
            }
            pixels.flip();
            image.set(size, size, pixels);
            return GLFW.glfwCreateCursor(image, 15, 15);
        }
        finally
        {
            image.free();
            MemoryUtil.memFree(pixels);
        }
    }

    private static void arrowHead(boolean[] core, int size, int tipX, int tipY, int dirX, int dirY)
    {
        int sideX = -dirY;
        int sideY = dirX;
        for (int step = 0; step <= 6; step++)
        {
            int spread = step < 5 ? step : 0;
            int cx = tipX + dirX * step;
            int cy = tipY + dirY * step;
            for (int offset = -spread; offset <= spread; offset++)
            {
                mark(core, size, cx + sideX * offset, cy + sideY * offset);
            }
        }
    }

    private static void shaft(boolean[] core, int size, int x0, int y0, int x1, int y1)
    {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        if (steps == 0)
        {
            mark(core, size, x0, y0);
            return;
        }
        for (int step = 0; step <= steps; step++)
        {
            int x = x0 + (x1 - x0) * step / steps;
            int y = y0 + (y1 - y0) * step / steps;
            mark(core, size, x, y);
            mark(core, size, x + 1, y);
        }
    }

    private static void mark(boolean[] core, int size, int x, int y)
    {
        if (x >= 0 && y >= 0 && x < size && y < size)
        {
            core[y * size + x] = true;
        }
    }

    private static boolean touchesCore(boolean[] core, int size, int x, int y)
    {
        for (int oy = -1; oy <= 1; oy++)
        {
            for (int ox = -1; ox <= 1; ox++)
            {
                int nx = x + ox;
                int ny = y + oy;
                if (nx >= 0 && ny >= 0 && nx < size && ny < size && core[ny * size + nx])
                {
                    return true;
                }
            }
        }
        return false;
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

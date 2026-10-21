package net.whitemax.snipastemcmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;

public class RenameImageScreen extends Screen
{
    private final Screen back;
    private final Path path;
    private final String suffix;
    private EditBox nameBox;
    private Button done;
    private Component error;

    public RenameImageScreen(Screen back, Path path)
    {
        super(Component.translatable("screen.snipaste_mc_mod.rename_title"));
        this.back = back;
        this.path = path;
        this.suffix = suffix(path);
    }

    @Override
    protected void init()
    {
        int boxWidth = this.suffix.isEmpty() ? 200 : 200 - this.font.width(this.suffix) - 4;
        boxWidth = Math.max(120, boxWidth);
        int x = this.width / 2 - (boxWidth + (this.suffix.isEmpty() ? 0 : this.font.width(this.suffix) + 4)) / 2;
        int y = this.height / 2 - 20;
        this.nameBox = new EditBox(this.font, x, y, boxWidth, 20, Component.empty());
        this.nameBox.setMaxLength(80);
        this.nameBox.setValue(ImageFolder.editableName(this.path));
        this.nameBox.setFilter(RenameImageScreen::accepts);
        this.nameBox.setCursorPosition(this.nameBox.getValue().length());
        this.nameBox.setHighlightPos(0);
        addRenderableWidget(this.nameBox);
        setInitialFocus(this.nameBox);
        int buttonY = y + 36;
        this.done = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> confirm())
                .bounds(this.width / 2 - 155, buttonY, 150, 20)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> cancel())
                .bounds(this.width / 2 + 5, buttonY, 150, 20)
                .build());
    }

    @Override
    public void tick()
    {
        if (this.nameBox != null)
        {
            this.nameBox.tick();
        }
        if (this.done != null && this.nameBox != null)
        {
            this.done.active = !this.nameBox.getValue().trim().isEmpty();
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            confirm();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            cancel();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.nameBox != null && !this.suffix.isEmpty())
        {
            graphics.drawString(this.font, this.suffix, this.nameBox.getX() + this.nameBox.getWidth() + 4, this.nameBox.getY() + 6, 0xA0A0A0, false);
        }
        if (this.error != null)
        {
            graphics.drawCenteredString(this.font, this.error, this.width / 2, this.height / 2 + 44, 0xFF5555);
        }
    }

    private void confirm()
    {
        if (this.done != null && !this.done.active)
        {
            return;
        }
        ImageFolder.RenameOutcome outcome = ImageFolder.rename(this.path, this.nameBox.getValue());
        if (outcome.path() == null)
        {
            this.error = Component.translatable(outcome.taken()
                    ? "message.snipaste_mc_mod.rename_exists"
                    : "message.snipaste_mc_mod.rename_failed");
            return;
        }
        if (this.back instanceof ImagePickScreen pick)
        {
            pick.highlight(outcome.path());
        }
        this.minecraft.setScreen(this.back);
    }

    private void cancel()
    {
        this.minecraft.setScreen(this.back);
    }

    private static String suffix(Path path)
    {
        if (Files.isDirectory(path))
        {
            return "";
        }
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot) : "";
    }

    private static boolean accepts(String text)
    {
        if (text.length() > 80)
        {
            return false;
        }
        for (int index = 0; index < text.length(); index++)
        {
            char character = text.charAt(index);
            if (character < 32 || "\\/:*?\"<>|".indexOf(character) >= 0)
            {
                return false;
            }
        }
        return true;
    }
}

package net.whitemax.snipastemcmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fml.ModList;
import net.whitemax.snipastemcmod.SnipasteMcMod;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

public class DeleteImageScreen extends Screen
{
    private final Screen back;
    private final Path path;
    private Checkbox skip;

    public DeleteImageScreen(Screen back, Path path)
    {
        super(Component.translatable("screen.snipaste_mc_mod.delete_image_title"));
        this.back = back;
        this.path = path;
    }

    public static void ask(Screen back, Path path)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (SnipasteConfig.skipsDeleteConfirm(scope()))
        {
            remove(path);
            minecraft.setScreen(back);
            return;
        }
        minecraft.setScreen(new DeleteImageScreen(back, path));
    }

    @Override
    protected void init()
    {
        int buttonY = this.height / 6 + 96;
        addRenderableWidget(Button.builder(Component.translatable("screen.snipaste_mc_mod.delete_confirm"), button -> finish(true))
                .bounds(this.width / 2 - 155, buttonY, 150, 20)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> finish(false))
                .bounds(this.width / 2 + 5, buttonY, 150, 20)
                .build());
        Component skipLabel = Component.translatable("screen.snipaste_mc_mod.delete_confirm_skip");
        int skipWidth = 24 + this.font.width(skipLabel);
        this.skip = new Checkbox(this.width / 2 - skipWidth / 2, buttonY + 28, skipWidth, 20, skipLabel, false);
        addRenderableWidget(this.skip);
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            finish(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 70, 0xFFFFFF);
        Component warning = Component.translatable("screen.snipaste_mc_mod.delete_image_warning", ImageFolder.displayName(this.path));
        List<FormattedCharSequence> lines = this.font.split(warning, this.width - 50);
        int y = 90;
        for (FormattedCharSequence line : lines)
        {
            graphics.drawCenteredString(this.font, line, this.width / 2, y, 0xFFFFFF);
            y += 10;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void finish(boolean confirmed)
    {
        if (confirmed)
        {
            if (this.skip != null && this.skip.selected())
            {
                SnipasteConfig.rememberDeleteConfirmSkip(scope());
            }
            remove(this.path);
        }
        this.minecraft.setScreen(this.back);
    }

    private static void remove(Path path)
    {
        if (!ImageFolder.delete(path))
        {
            SnipasteClient.tell(Component.translatable("message.snipaste_mc_mod.delete_failed"));
        }
    }

    private static String scope()
    {
        String version = ModList.get().getModContainerById(SnipasteMcMod.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("");
        Minecraft minecraft = Minecraft.getInstance();
        String world = "";
        if (minecraft.getSingleplayerServer() != null)
        {
            world = minecraft.getSingleplayerServer().getWorldData().getLevelName();
        }
        else if (minecraft.getCurrentServer() != null)
        {
            world = minecraft.getCurrentServer().ip;
        }
        return version + "\n" + world;
    }
}

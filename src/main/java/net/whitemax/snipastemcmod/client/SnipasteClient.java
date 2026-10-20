package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.whitemax.snipastemcmod.SnipasteMcMod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = SnipasteMcMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SnipasteClient
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean pendingSnip;
    private static NativeImage capturedFrame;

    private SnipasteClient()
    {
    }

    public static void requestSnip()
    {
        pendingSnip = true;
    }

    public static void tell(Component message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null)
        {
            minecraft.player.displayClientMessage(message, true);
            return;
        }
        if (minecraft.gui != null)
        {
            minecraft.gui.setOverlayMessage(message, false);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (capturedFrame != null && minecraft.screen == null)
        {
            NativeImage frame = capturedFrame;
            capturedFrame = null;
            minecraft.setScreen(new SnipScreen(frame));
        }

        boolean snip = consume(SnipasteKeybinds.SNIP);
        boolean paste = consume(SnipasteKeybinds.PASTE);
        boolean edit = consume(SnipasteKeybinds.EDIT);
        Screen screen = minecraft.screen;
        if (screen instanceof StickyEditScreen editScreen)
        {
            if (editScreen.isRebinding())
            {
                return;
            }
            if (snip)
            {
                requestSnip();
                minecraft.setScreen(null);
            }
            else if (edit)
            {
                minecraft.setScreen(null);
            }
            if (paste && minecraft.screen instanceof StickyEditScreen stillOpen && StickyBoard.get().pasteLatest())
            {
                stillOpen.selectLast();
            }
            return;
        }
        if (screen != null)
        {
            return;
        }
        if (snip)
        {
            requestSnip();
        }
        if (paste)
        {
            StickyBoard.get().pasteLatest();
        }
        if (edit)
        {
            minecraft.setScreen(new StickyEditScreen());
        }
    }

    private static boolean consume(net.minecraft.client.KeyMapping mapping)
    {
        boolean pressed = false;
        while (mapping.consumeClick())
        {
            pressed = true;
        }
        return pressed;
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || !pendingSnip || capturedFrame != null)
        {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null)
        {
            return;
        }
        pendingSnip = false;
        try
        {
            NativeImage frame = Screenshot.takeScreenshot(minecraft.getMainRenderTarget());
            if (frame.getWidth() <= 0 || frame.getHeight() <= 0)
            {
                frame.close();
                tell(Component.translatable("message.snipaste_mc_mod.capture_failed"));
                return;
            }
            capturedFrame = frame;
        }
        catch (RuntimeException exception)
        {
            LOGGER.warn("Failed to capture the game framebuffer", exception);
            tell(Component.translatable("message.snipaste_mc_mod.capture_failed"));
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || !StickyBoard.get().shown())
        {
            return;
        }
        StickyRenderer.draw(event.getGuiGraphics(), false, -1);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event)
    {
        Screen screen = event.getScreen();
        if (screen instanceof SnipScreen || screen instanceof StickyEditScreen || !StickyBoard.get().shown())
        {
            return;
        }
        StickyRenderer.draw(event.getGuiGraphics(), false, -1);
    }

    @SubscribeEvent
    public static void onMovement(MovementInputUpdateEvent event)
    {
        if (!(Minecraft.getInstance().screen instanceof StickyEditScreen))
        {
            return;
        }
        event.getInput().up = false;
        event.getInput().down = false;
        event.getInput().left = false;
        event.getInput().right = false;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
        event.getInput().forwardImpulse = 0.0F;
        event.getInput().leftImpulse = 0.0F;
    }
}

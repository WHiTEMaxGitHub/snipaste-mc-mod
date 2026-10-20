package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.whitemax.snipastemcmod.SnipasteMcMod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SnipasteMcMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SnipasteKeybinds
{
    public static final String CATEGORY = "key.categories.snipaste_mc_mod";

    public static final KeyMapping SNIP = new KeyMapping(
            "key.snipaste_mc_mod.snip",
            GLFW.GLFW_KEY_F6,
            CATEGORY);
    public static final KeyMapping PASTE = new KeyMapping(
            "key.snipaste_mc_mod.paste",
            GLFW.GLFW_KEY_F7,
            CATEGORY);
    public static final KeyMapping EDIT = new KeyMapping(
            "key.snipaste_mc_mod.edit",
            GLFW.GLFW_KEY_F8,
            CATEGORY);

    private SnipasteKeybinds()
    {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event)
    {
        event.register(SNIP);
        event.register(PASTE);
        event.register(EDIT);
    }

    public static boolean matches(KeyMapping mapping, int keyCode, int scanCode)
    {
        return mapping.matches(keyCode, scanCode);
    }

    public static void rebind(KeyMapping mapping, int keyCode, int scanCode)
    {
        mapping.setKey(InputConstants.getKey(keyCode, scanCode));
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    public static void unbind(KeyMapping mapping)
    {
        mapping.setKey(InputConstants.UNKNOWN);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }
}

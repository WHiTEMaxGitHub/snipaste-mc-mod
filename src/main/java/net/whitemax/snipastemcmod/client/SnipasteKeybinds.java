package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

    public static boolean isPressed(KeyMapping mapping)
    {
        InputConstants.Key key = mapping.getKey();
        long window = Minecraft.getInstance().getWindow().getWindow();
        return switch (key.getType())
        {
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            case SCANCODE -> mapping.isDown();
        };
    }

    public static void assign(KeyMapping mapping, InputConstants.Key key)
    {
        mapping.setKey(key);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    public static boolean hideDown()
    {
        InputConstants.Key key = SnipasteConfig.hideKey();
        if (key.equals(InputConstants.UNKNOWN) || !isDown(key))
        {
            return false;
        }
        return heldModifiers(key) == SnipasteConfig.hideModifiers();
    }

    public static boolean hideMatches(int keyCode, int scanCode, int modifiers)
    {
        InputConstants.Key bound = SnipasteConfig.hideKey();
        InputConstants.Key pressed = InputConstants.getKey(keyCode, scanCode);
        if (bound.equals(InputConstants.UNKNOWN) || !pressed.equals(bound))
        {
            return false;
        }
        return relevantModifiers(pressed, modifiers) == SnipasteConfig.hideModifiers();
    }

    public static boolean hideUses(KeyMapping mapping)
    {
        InputConstants.Key bound = SnipasteConfig.hideKey();
        return !bound.equals(InputConstants.UNKNOWN) && mapping.getKey().equals(bound);
    }

    public static void setHide(InputConstants.Key key, int modifiers)
    {
        SnipasteConfig.setHide(key, relevantModifiers(key, modifiers));
    }

    public static void resetHide()
    {
        SnipasteConfig.setHide(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F7), GLFW.GLFW_MOD_SHIFT);
    }

    public static Component hideMessage()
    {
        InputConstants.Key key = SnipasteConfig.hideKey();
        Component keyName = key.getDisplayName();
        int modifiers = SnipasteConfig.hideModifiers();
        if (modifiers == 0 || key.equals(InputConstants.UNKNOWN))
        {
            return keyName;
        }
        MutableComponent label = Component.empty();
        boolean any = false;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0)
        {
            label.append(Component.translatable("screen.snipaste_mc_mod.mod_shift"));
            any = true;
        }
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0)
        {
            if (any)
            {
                label.append(" + ");
            }
            label.append(Component.translatable("screen.snipaste_mc_mod.mod_ctrl"));
            any = true;
        }
        if ((modifiers & GLFW.GLFW_MOD_ALT) != 0)
        {
            if (any)
            {
                label.append(" + ");
            }
            label.append(Component.translatable("screen.snipaste_mc_mod.mod_alt"));
            any = true;
        }
        if (any)
        {
            label.append(" + ");
        }
        return label.append(keyName);
    }

    public static int relevantModifiers(InputConstants.Key key, int modifiers)
    {
        int mask = modifiers & (GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT);
        if (key.getType() != InputConstants.Type.KEYSYM)
        {
            return mask;
        }
        int value = key.getValue();
        if (value == GLFW.GLFW_KEY_LEFT_SHIFT || value == GLFW.GLFW_KEY_RIGHT_SHIFT)
        {
            mask &= ~GLFW.GLFW_MOD_SHIFT;
        }
        if (value == GLFW.GLFW_KEY_LEFT_CONTROL || value == GLFW.GLFW_KEY_RIGHT_CONTROL)
        {
            mask &= ~GLFW.GLFW_MOD_CONTROL;
        }
        if (value == GLFW.GLFW_KEY_LEFT_ALT || value == GLFW.GLFW_KEY_RIGHT_ALT)
        {
            mask &= ~GLFW.GLFW_MOD_ALT;
        }
        return mask;
    }

    public static int heldModifierMask()
    {
        long window = Minecraft.getInstance().getWindow().getWindow();
        int mask = 0;
        if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT))
        {
            mask |= GLFW.GLFW_MOD_SHIFT;
        }
        if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL))
        {
            mask |= GLFW.GLFW_MOD_CONTROL;
        }
        if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT))
        {
            mask |= GLFW.GLFW_MOD_ALT;
        }
        return mask;
    }

    private static int heldModifiers(InputConstants.Key key)
    {
        return relevantModifiers(key, heldModifierMask());
    }

    private static boolean isDown(InputConstants.Key key)
    {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return switch (key.getType())
        {
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            case SCANCODE -> scancodeDown(window, key.getValue());
        };
    }

    private static boolean scancodeDown(long window, int scancode)
    {
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++)
        {
            if (GLFW.glfwGetKeyScancode(key) == scancode && GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS)
            {
                return true;
            }
        }
        return false;
    }
}

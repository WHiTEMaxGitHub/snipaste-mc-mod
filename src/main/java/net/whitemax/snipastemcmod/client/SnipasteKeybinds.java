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
    public static final Binding SNIP_CHORD = new Binding(SNIP, Component.translatable(SNIP.getName()), SnipasteConfig.SNIP_KEY, SnipasteConfig.SNIP_MODIFIERS, "key.keyboard.f6", 0);
    public static final Binding PASTE_CHORD = new Binding(PASTE, Component.translatable(PASTE.getName()), SnipasteConfig.PASTE_KEY, SnipasteConfig.PASTE_MODIFIERS, "key.keyboard.f7", 0);
    public static final Binding EDIT_CHORD = new Binding(EDIT, Component.translatable(EDIT.getName()), SnipasteConfig.EDIT_KEY, SnipasteConfig.EDIT_MODIFIERS, "key.keyboard.f8", 0);
    public static final Binding HIDE_CHORD = new Binding(null, Component.translatable("screen.snipaste_mc_mod.clear_key"), SnipasteConfig.HIDE_KEY, SnipasteConfig.HIDE_MODIFIERS, SnipasteConfig.DEFAULT_HIDE_KEY, GLFW.GLFW_MOD_SHIFT);
    public static final Binding CLIP_CHORD = new Binding(null, Component.translatable("screen.snipaste_mc_mod.clip_key"), SnipasteConfig.CLIP_KEY, SnipasteConfig.CLIP_MODIFIERS, SnipasteConfig.DEFAULT_CLIP_KEY, GLFW.GLFW_MOD_SHIFT);
    public static final Binding[] CHORDS = {SNIP_CHORD, PASTE_CHORD, CLIP_CHORD, EDIT_CHORD, HIDE_CHORD};

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

    public static void assign(KeyMapping mapping, InputConstants.Key key)
    {
        mapping.setKey(key);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    public static Component format(InputConstants.Key key, int modifiers)
    {
        Component keyName = key.getDisplayName();
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

    public static final class Binding
    {
        private final KeyMapping mapping;
        private final Component name;
        private final net.minecraftforge.common.ForgeConfigSpec.ConfigValue<String> keyName;
        private final net.minecraftforge.common.ForgeConfigSpec.IntValue modifierValue;
        private final InputConstants.Key defaultKey;
        private final int defaultModifiers;

        private Binding(KeyMapping mapping, Component name, net.minecraftforge.common.ForgeConfigSpec.ConfigValue<String> keyName, net.minecraftforge.common.ForgeConfigSpec.IntValue modifierValue, String defaultName, int defaultModifiers)
        {
            this.mapping = mapping;
            this.name = name;
            this.keyName = keyName;
            this.modifierValue = modifierValue;
            this.defaultKey = InputConstants.getKey(defaultName);
            this.defaultModifiers = defaultModifiers;
        }

        public Component name()
        {
            return this.name;
        }

        public KeyMapping vanilla()
        {
            return this.mapping;
        }

        public InputConstants.Key key()
        {
            return SnipasteConfig.chordKey(this.keyName);
        }

        public int modifiers()
        {
            return this.modifierValue.get();
        }

        public Component message()
        {
            return format(key(), modifiers());
        }

        public boolean matches(int keyCode, int scanCode, int eventModifiers)
        {
            InputConstants.Key bound = key();
            InputConstants.Key pressed = InputConstants.getKey(keyCode, scanCode);
            if (bound.equals(InputConstants.UNKNOWN) || !pressed.equals(bound))
            {
                return false;
            }
            return relevantModifiers(pressed, eventModifiers) == modifiers();
        }

        public boolean consume()
        {
            adoptVanillaKey();
            if (this.mapping == null)
            {
                return false;
            }
            boolean clicked = false;
            while (this.mapping.consumeClick())
            {
                clicked = true;
            }
            if (!clicked || key().equals(InputConstants.UNKNOWN))
            {
                return false;
            }
            return heldModifiers(key()) == modifiers();
        }

        public boolean poll()
        {
            adoptVanillaKey();
            InputConstants.Key bound = key();
            if (bound.equals(InputConstants.UNKNOWN) || !isDown(bound))
            {
                return false;
            }
            return heldModifiers(bound) == modifiers();
        }

        public void set(InputConstants.Key key, int modifiers)
        {
            SnipasteConfig.setChord(this.keyName, this.modifierValue, key, relevantModifiers(key, modifiers));
            if (this.mapping != null)
            {
                assign(this.mapping, key);
            }
        }

        public void reset()
        {
            set(this.defaultKey, this.defaultModifiers);
        }

        public boolean isDefault()
        {
            return key().equals(this.defaultKey) && modifiers() == this.defaultModifiers;
        }

        public boolean same(Binding other)
        {
            InputConstants.Key bound = key();
            return !bound.equals(InputConstants.UNKNOWN) && bound.equals(other.key()) && modifiers() == other.modifiers();
        }

        public void adoptVanillaKey()
        {
            if (this.mapping == null)
            {
                return;
            }
            InputConstants.Key vanilla = this.mapping.getKey();
            if (!vanilla.equals(key()))
            {
                SnipasteConfig.setChord(this.keyName, this.modifierValue, vanilla, 0);
            }
        }
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

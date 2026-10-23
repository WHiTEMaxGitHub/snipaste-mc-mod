package net.whitemax.snipastemcmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.common.ForgeConfigSpec;
import org.lwjgl.glfw.GLFW;

public final class SnipasteConfig
{
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue DEFAULT_OPACITY;
    public static final ForgeConfigSpec.IntValue MAX_FOLDER_MEGABYTES;
    public static final ForgeConfigSpec.BooleanValue BORDER_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BORDER_RAINBOW;
    public static final ForgeConfigSpec.DoubleValue BORDER_WIDTH;
    public static final ForgeConfigSpec.IntValue BORDER_RED;
    public static final ForgeConfigSpec.IntValue BORDER_GREEN;
    public static final ForgeConfigSpec.IntValue BORDER_BLUE;
    public static final ForgeConfigSpec.IntValue BORDER_ALPHA;
    public static final ForgeConfigSpec.BooleanValue ALLOW_STRETCH;
    public static final ForgeConfigSpec.ConfigValue<String> SNIP_KEY;
    public static final ForgeConfigSpec.IntValue SNIP_MODIFIERS;
    public static final ForgeConfigSpec.ConfigValue<String> PASTE_KEY;
    public static final ForgeConfigSpec.IntValue PASTE_MODIFIERS;
    public static final ForgeConfigSpec.ConfigValue<String> EDIT_KEY;
    public static final ForgeConfigSpec.IntValue EDIT_MODIFIERS;
    public static final ForgeConfigSpec.ConfigValue<String> HIDE_KEY;
    public static final ForgeConfigSpec.IntValue HIDE_MODIFIERS;
    public static final ForgeConfigSpec.ConfigValue<String> CLIP_KEY;
    public static final ForgeConfigSpec.IntValue CLIP_MODIFIERS;
    public static final ForgeConfigSpec.ConfigValue<String> CHORD_REVISION;
    public static final ForgeConfigSpec.IntValue TEXT_RED;
    public static final ForgeConfigSpec.IntValue TEXT_GREEN;
    public static final ForgeConfigSpec.IntValue TEXT_BLUE;
    public static final ForgeConfigSpec.IntValue TEXT_ALPHA;
    public static final ForgeConfigSpec.IntValue TEXT_SIZE;
    public static final ForgeConfigSpec.BooleanValue TEXT_BACKGROUND;
    public static final ForgeConfigSpec.IntValue TEXT_BG_RED;
    public static final ForgeConfigSpec.IntValue TEXT_BG_GREEN;
    public static final ForgeConfigSpec.IntValue TEXT_BG_BLUE;
    public static final ForgeConfigSpec.IntValue TEXT_BG_ALPHA;
    public static final int MODIFIER_MASK = GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT;
    public static final ForgeConfigSpec.BooleanValue SHADOW_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SHADOW_OPACITY;
    public static final ForgeConfigSpec.IntValue SHADOW_ANGLE;
    public static final ForgeConfigSpec.DoubleValue SHADOW_DISTANCE;
    public static final ForgeConfigSpec.IntValue SHADOW_BLUR;
    public static final ForgeConfigSpec.IntValue SHADOW_SPREAD;
    public static final ForgeConfigSpec.IntValue SHADOW_RED;
    public static final ForgeConfigSpec.IntValue SHADOW_GREEN;
    public static final ForgeConfigSpec.IntValue SHADOW_BLUE;
    public static final ForgeConfigSpec.IntValue SHADOW_ALPHA;
    public static final String DEFAULT_HIDE_KEY = "key.keyboard.f6";
    public static final String DEFAULT_CLIP_KEY = "key.keyboard.f7";
    public static final ForgeConfigSpec.ConfigValue<String> SKIP_DELETE_CONFIRM;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("贴图浮层").push("overlay");
        DEFAULT_OPACITY = builder
                .comment("新贴图默认的不透明度，范围 0 到 1，对应界面上的 0 到 100。")
                .defineInRange("defaultOpacity", 1.0D, 0.0D, 1.0D);
        MAX_FOLDER_MEGABYTES = builder
                .comment("游戏目录 snipaste 文件夹的大小上限，单位 MB。0 表示不限制。")
                .defineInRange("maxFolderMegabytes", 64, 0, 1024);
        BORDER_ENABLED = builder
                .comment("是否在浮层四周绘制边框。")
                .define("borderEnabled", true);
        BORDER_RAINBOW = builder
                .comment("边框使用彩色。打开后忽略下面的 RGB。")
                .define("borderRainbow", false);
        BORDER_WIDTH = builder
                .comment("边框宽度，单位是界面像素，最小 0.1。")
                .defineInRange("borderWidth", 1.0D, 0.1D, 16.0D);
        BORDER_RED = builder
                .comment("边框红色分量，0 到 255。")
                .defineInRange("borderRed", 255, 0, 255);
        BORDER_GREEN = builder
                .comment("边框绿色分量，0 到 255。")
                .defineInRange("borderGreen", 255, 0, 255);
        BORDER_BLUE = builder
                .comment("边框蓝色分量，0 到 255。")
                .defineInRange("borderBlue", 255, 0, 255);
        BORDER_ALPHA = builder
                .comment("边框透明度，0 到 255。还会再乘贴图自己的不透明度。")
                .defineInRange("borderAlpha", 255, 0, 255);
        ALLOW_STRETCH = builder
                .comment("允许单独改变浮层的宽或高。关闭后只能等比例缩放。")
                .define("allowStretch", false);
        SNIP_KEY = chordKey(builder, "snipKey", "截图快捷键，使用原版按键名。", "key.keyboard.f6");
        SNIP_MODIFIERS = chordModifiers(builder, "snipModifiers", 0);
        PASTE_KEY = chordKey(builder, "pasteKey", "贴图快捷键，使用原版按键名。", "key.keyboard.f7");
        PASTE_MODIFIERS = chordModifiers(builder, "pasteModifiers", 0);
        EDIT_KEY = chordKey(builder, "editKey", "编辑快捷键，使用原版按键名。", "key.keyboard.f8");
        EDIT_MODIFIERS = chordModifiers(builder, "editModifiers", 0);
        HIDE_KEY = chordKey(builder, "hideKey", "清屏快捷键，使用原版按键名。", DEFAULT_HIDE_KEY);
        HIDE_MODIFIERS = chordModifiers(builder, "hideModifiers", GLFW.GLFW_MOD_SHIFT);
        CLIP_KEY = chordKey(builder, "clipKey", "读取文字剪贴板的快捷键，使用原版按键名。", DEFAULT_CLIP_KEY);
        CLIP_MODIFIERS = chordModifiers(builder, "clipModifiers", GLFW.GLFW_MOD_SHIFT);
        CHORD_REVISION = builder.comment("按键默认值迁移标记。有内容后不再改动已保存的清屏按键。").define("chordRevision", "");
        builder.comment("文字贴图").push("text");
        TEXT_RED = builder.comment("文字红色分量，0 到 255。").defineInRange("red", 255, 0, 255);
        TEXT_GREEN = builder.comment("文字绿色分量，0 到 255。").defineInRange("green", 255, 0, 255);
        TEXT_BLUE = builder.comment("文字蓝色分量，0 到 255。").defineInRange("blue", 255, 0, 255);
        TEXT_ALPHA = builder.comment("文字透明度，0 到 255。还会再乘贴图自己的不透明度。").defineInRange("alpha", 255, 0, 255);
        TEXT_SIZE = builder.comment("文字高度，单位是界面像素。0 表示和游戏界面一样大。").defineInRange("size", 0, 0, 128);
        TEXT_BACKGROUND = builder.comment("是否绘制文字背景。").define("background", true);
        TEXT_BG_RED = builder.comment("背景红色分量，0 到 255。").defineInRange("backgroundRed", 24, 0, 255);
        TEXT_BG_GREEN = builder.comment("背景绿色分量，0 到 255。").defineInRange("backgroundGreen", 24, 0, 255);
        TEXT_BG_BLUE = builder.comment("背景蓝色分量，0 到 255。").defineInRange("backgroundBlue", 24, 0, 255);
        TEXT_BG_ALPHA = builder.comment("背景透明度，0 到 255。还会再乘贴图自己的不透明度。").defineInRange("backgroundAlpha", 210, 0, 255);
        builder.pop();
        SKIP_DELETE_CONFIRM = builder
                .comment("勾选不再提示后记下的模组版本和存档。对不上就还会再问一次。")
                .define("skipDeleteConfirm", "");
        builder.comment("图像阴影").push("shadow");
        SHADOW_ENABLED = builder
                .comment("是否在图像背后绘制阴影。")
                .define("enabled", false);
        SHADOW_OPACITY = builder
                .comment("阴影不透明度，范围 0 到 1。")
                .defineInRange("opacity", 0.5D, 0.0D, 1.0D);
        SHADOW_ANGLE = builder
                .comment("光源角度。0 向右，90 向上，阴影落在反方向。")
                .defineInRange("angle", 135, 0, 360);
        SHADOW_DISTANCE = builder
                .comment("阴影与图像的距离，范围 0 到 4，可以到小数点后一位。")
                .defineInRange("distance", 4.0D, 0.0D, 4.0D);
        SHADOW_BLUR = builder
                .comment("阴影模糊半径，单位是界面像素。")
                .defineInRange("blur", 8, 0, 48);
        SHADOW_SPREAD = builder
                .comment("阴影扩展，先把影子放大再模糊，单位是界面像素。")
                .defineInRange("spread", 0, 0, 32);
        SHADOW_RED = builder
                .comment("阴影红色分量，0 到 255。")
                .defineInRange("red", 0, 0, 255);
        SHADOW_GREEN = builder
                .comment("阴影绿色分量，0 到 255。")
                .defineInRange("green", 0, 0, 255);
        SHADOW_BLUE = builder
                .comment("阴影蓝色分量，0 到 255。")
                .defineInRange("blue", 0, 0, 255);
        SHADOW_ALPHA = builder
                .comment("阴影颜色的透明度，0 到 255。还会再乘阴影不透明度和贴图自己的不透明度。")
                .defineInRange("alpha", 255, 0, 255);
        builder.pop();
        builder.pop();
        SPEC = builder.build();
    }

    private SnipasteConfig()
    {
    }

    public static float defaultOpacity()
    {
        return DEFAULT_OPACITY.get().floatValue();
    }

    public static void setDefaultOpacity(double opacity)
    {
        setDouble(DEFAULT_OPACITY, opacity, 0.0D, 1.0D);
    }

    public static int maxFolderMegabytes()
    {
        return MAX_FOLDER_MEGABYTES.get();
    }

    public static long maxFolderBytes()
    {
        return maxFolderMegabytes() * 1024L * 1024L;
    }

    public static void addMaxFolderMegabytes(int delta)
    {
        int next = Math.max(0, Math.min(1024, maxFolderMegabytes() + delta));
        if (next == maxFolderMegabytes())
        {
            return;
        }
        MAX_FOLDER_MEGABYTES.set(next);
    }

    public static boolean borderEnabled()
    {
        return BORDER_ENABLED.get();
    }

    public static void setBorderEnabled(boolean enabled)
    {
        if (BORDER_ENABLED.get() == enabled)
        {
            return;
        }
        BORDER_ENABLED.set(enabled);
    }

    public static boolean borderRainbow()
    {
        return BORDER_RAINBOW.get();
    }

    public static void setBorderRainbow(boolean rainbow)
    {
        if (BORDER_RAINBOW.get() == rainbow)
        {
            return;
        }
        BORDER_RAINBOW.set(rainbow);
    }

    public static double borderWidth()
    {
        return BORDER_WIDTH.get();
    }

    public static void setBorderWidth(double width)
    {
        double clamped = Math.max(0.1D, Math.min(16.0D, width));
        if (Math.abs(BORDER_WIDTH.get() - clamped) < 0.0001D)
        {
            return;
        }
        BORDER_WIDTH.set(clamped);
    }

    public static int borderRed()
    {
        return BORDER_RED.get();
    }

    public static int borderGreen()
    {
        return BORDER_GREEN.get();
    }

    public static int borderBlue()
    {
        return BORDER_BLUE.get();
    }

    public static int borderAlpha()
    {
        return BORDER_ALPHA.get();
    }

    public static int borderRgb()
    {
        return (borderRed() << 16) | (borderGreen() << 8) | borderBlue();
    }

    public static int textRed()
    {
        return TEXT_RED.get();
    }

    public static int textGreen()
    {
        return TEXT_GREEN.get();
    }

    public static int textBlue()
    {
        return TEXT_BLUE.get();
    }

    public static int textAlpha()
    {
        return TEXT_ALPHA.get();
    }

    public static int textRgb()
    {
        return (textRed() << 16) | (textGreen() << 8) | textBlue();
    }

    public static int textSize()
    {
        return TEXT_SIZE.get();
    }

    public static void setTextSize(int size)
    {
        setInt(TEXT_SIZE, size, 0, 128);
    }

    public static boolean textBackground()
    {
        return TEXT_BACKGROUND.get();
    }

    public static void setTextBackground(boolean enabled)
    {
        if (TEXT_BACKGROUND.get() == enabled)
        {
            return;
        }
        TEXT_BACKGROUND.set(enabled);
    }

    public static int textBackgroundRed()
    {
        return TEXT_BG_RED.get();
    }

    public static int textBackgroundGreen()
    {
        return TEXT_BG_GREEN.get();
    }

    public static int textBackgroundBlue()
    {
        return TEXT_BG_BLUE.get();
    }

    public static int textBackgroundAlpha()
    {
        return TEXT_BG_ALPHA.get();
    }

    public static int textBackgroundRgb()
    {
        return (textBackgroundRed() << 16) | (textBackgroundGreen() << 8) | textBackgroundBlue();
    }

    public static int textChannel(boolean background, int channel)
    {
        if (background)
        {
            return switch (channel)
            {
                case 0 -> textBackgroundRed();
                case 1 -> textBackgroundGreen();
                case 2 -> textBackgroundBlue();
                default -> textBackgroundAlpha();
            };
        }
        return switch (channel)
        {
            case 0 -> textRed();
            case 1 -> textGreen();
            case 2 -> textBlue();
            default -> textAlpha();
        };
    }

    public static void setTextChannel(boolean background, int channel, int value)
    {
        ForgeConfigSpec.IntValue target = background ? switch (channel)
        {
            case 0 -> TEXT_BG_RED;
            case 1 -> TEXT_BG_GREEN;
            case 2 -> TEXT_BG_BLUE;
            default -> TEXT_BG_ALPHA;
        } : switch (channel)
        {
            case 0 -> TEXT_RED;
            case 1 -> TEXT_GREEN;
            case 2 -> TEXT_BLUE;
            default -> TEXT_ALPHA;
        };
        setInt(target, value, 0, 255);
    }

    public static void setBorderChannel(int channel, int value)
    {
        ForgeConfigSpec.IntValue target = switch (channel)
        {
            case 0 -> BORDER_RED;
            case 1 -> BORDER_GREEN;
            case 2 -> BORDER_BLUE;
            default -> BORDER_ALPHA;
        };
        setInt(target, value, 0, 255);
    }

    public static boolean allowStretch()
    {
        return ALLOW_STRETCH.get();
    }

    public static void setAllowStretch(boolean allow)
    {
        if (ALLOW_STRETCH.get() == allow)
        {
            return;
        }
        ALLOW_STRETCH.set(allow);
    }

    public static InputConstants.Key chordKey(ForgeConfigSpec.ConfigValue<String> key)
    {
        try
        {
            return InputConstants.getKey(key.get());
        }
        catch (IllegalArgumentException exception)
        {
            return InputConstants.UNKNOWN;
        }
    }

    public static void keepClipboardChord()
    {
        if ("3".equals(CHORD_REVISION.get()))
        {
            return;
        }
        InputConstants.Key hide = chordKey(HIDE_KEY);
        int modifiers = HIDE_MODIFIERS.get();
        boolean oldShift = modifiers == GLFW.GLFW_MOD_SHIFT
                && ("key.keyboard.f7".equals(hide.getName()) || "key.keyboard.f8".equals(hide.getName()));
        boolean oldF9 = modifiers == 0 && "key.keyboard.f9".equals(hide.getName());
        if (oldShift || oldF9)
        {
            setChord(HIDE_KEY, HIDE_MODIFIERS, InputConstants.getKey(DEFAULT_HIDE_KEY), GLFW.GLFW_MOD_SHIFT);
        }
        CHORD_REVISION.set("3");
        SPEC.save();
    }

    public static void setChord(ForgeConfigSpec.ConfigValue<String> key, ForgeConfigSpec.IntValue modifiers, InputConstants.Key pressed, int mask)
    {
        String name = pressed.getName();
        int stored = mask & MODIFIER_MASK;
        if (name.equals(key.get()) && modifiers.get() == stored)
        {
            return;
        }
        key.set(name);
        modifiers.set(stored);
        SPEC.save();
    }

    private static ForgeConfigSpec.ConfigValue<String> chordKey(ForgeConfigSpec.Builder builder, String name, String comment, String defaultName)
    {
        return builder.comment(comment).define(name, defaultName);
    }

    private static ForgeConfigSpec.IntValue chordModifiers(ForgeConfigSpec.Builder builder, String name, int defaultValue)
    {
        return builder
                .comment("修饰键。1 是 Shift，2 是 Ctrl，4 是 Alt，可以相加。")
                .defineInRange(name, defaultValue, 0, MODIFIER_MASK);
    }

    public static boolean skipsDeleteConfirm(String scope)
    {
        String saved = SKIP_DELETE_CONFIRM.get();
        return scope != null && !scope.isEmpty() && scope.equals(saved);
    }

    public static void rememberDeleteConfirmSkip(String scope)
    {
        if (scope == null || scope.isEmpty() || scope.equals(SKIP_DELETE_CONFIRM.get()))
        {
            return;
        }
        SKIP_DELETE_CONFIRM.set(scope);
        SPEC.save();
    }

    public static boolean shadowEnabled()
    {
        return SHADOW_ENABLED.get();
    }

    public static void setShadowEnabled(boolean enabled)
    {
        if (SHADOW_ENABLED.get() == enabled)
        {
            return;
        }
        SHADOW_ENABLED.set(enabled);
    }

    public static float shadowOpacity()
    {
        return SHADOW_OPACITY.get().floatValue();
    }

    public static void setShadowOpacity(double opacity)
    {
        setDouble(SHADOW_OPACITY, opacity, 0.0D, 1.0D);
    }

    public static int shadowAngle()
    {
        return SHADOW_ANGLE.get();
    }

    public static void setShadowAngle(int angle)
    {
        setInt(SHADOW_ANGLE, angle, 0, 360);
    }

    public static double shadowDistance()
    {
        return Math.max(0.0D, Math.min(4.0D, SHADOW_DISTANCE.get()));
    }

    public static void setShadowDistance(double distance)
    {
        double clamped = Math.round(Math.max(0.0D, Math.min(4.0D, distance)) * 10.0D) / 10.0D;
        if (Math.abs(SHADOW_DISTANCE.get() - clamped) < 0.001D)
        {
            return;
        }
        SHADOW_DISTANCE.set(clamped);
    }

    public static int shadowBlur()
    {
        return SHADOW_BLUR.get();
    }

    public static void setShadowBlur(int blur)
    {
        setInt(SHADOW_BLUR, blur, 0, 48);
    }

    public static int shadowSpread()
    {
        return SHADOW_SPREAD.get();
    }

    public static void setShadowSpread(int spread)
    {
        setInt(SHADOW_SPREAD, spread, 0, 32);
    }

    public static int shadowRed()
    {
        return SHADOW_RED.get();
    }

    public static int shadowGreen()
    {
        return SHADOW_GREEN.get();
    }

    public static int shadowBlue()
    {
        return SHADOW_BLUE.get();
    }

    public static int shadowAlpha()
    {
        return SHADOW_ALPHA.get();
    }

    public static int shadowRgb()
    {
        return (shadowRed() << 16) | (shadowGreen() << 8) | shadowBlue();
    }

    public static void setShadowChannel(int channel, int value)
    {
        ForgeConfigSpec.IntValue target = switch (channel)
        {
            case 0 -> SHADOW_RED;
            case 1 -> SHADOW_GREEN;
            case 2 -> SHADOW_BLUE;
            default -> SHADOW_ALPHA;
        };
        setInt(target, value, 0, 255);
    }

    private static void setInt(ForgeConfigSpec.IntValue target, int value, int min, int max)
    {
        int clamped = Math.max(min, Math.min(max, value));
        if (target.get() == clamped)
        {
            return;
        }
        target.set(clamped);
    }

    private static void setDouble(ForgeConfigSpec.DoubleValue target, double value, double min, double max)
    {
        double clamped = Math.max(min, Math.min(max, value));
        if (Math.abs(target.get() - clamped) < 0.001D)
        {
            return;
        }
        target.set(clamped);
    }
}

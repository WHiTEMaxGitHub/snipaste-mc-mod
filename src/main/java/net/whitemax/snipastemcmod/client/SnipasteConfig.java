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
    public static final ForgeConfigSpec.BooleanValue ALLOW_STRETCH;
    public static final ForgeConfigSpec.ConfigValue<String> HIDE_KEY;
    public static final ForgeConfigSpec.IntValue HIDE_MODIFIERS;
    public static final ForgeConfigSpec.BooleanValue SHADOW_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SHADOW_OPACITY;
    public static final ForgeConfigSpec.IntValue SHADOW_ANGLE;
    public static final ForgeConfigSpec.IntValue SHADOW_DISTANCE;
    public static final ForgeConfigSpec.IntValue SHADOW_BLUR;
    public static final ForgeConfigSpec.IntValue SHADOW_SPREAD;
    public static final ForgeConfigSpec.IntValue SHADOW_RED;
    public static final ForgeConfigSpec.IntValue SHADOW_GREEN;
    public static final ForgeConfigSpec.IntValue SHADOW_BLUE;
    public static final String DEFAULT_HIDE_KEY = "key.keyboard.f7";

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("贴图浮层").push("overlay");
        DEFAULT_OPACITY = builder
                .comment("新贴的图片使用的透明度，范围 0.05 到 1。")
                .defineInRange("defaultOpacity", 1.0D, 0.05D, 1.0D);
        MAX_FOLDER_MEGABYTES = builder
                .comment("游戏目录 snipaste 文件夹的大小上限，单位 MB。大于该值时从最早放入的图片开始删除。0 表示永不删除。")
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
        ALLOW_STRETCH = builder
                .comment("允许单独改变浮层的宽或高。关闭后只能等比例缩放。")
                .define("allowStretch", false);
        HIDE_KEY = builder
                .comment("清屏快捷键，使用原版按键名。")
                .define("hideKey", DEFAULT_HIDE_KEY);
        HIDE_MODIFIERS = builder
                .comment("清屏快捷键的修饰键。1 是 Shift，2 是 Ctrl，4 是 Alt，可以相加。")
                .defineInRange("hideModifiers", GLFW.GLFW_MOD_SHIFT, 0, GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT);
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
                .comment("阴影与图像的距离，单位是界面像素。")
                .defineInRange("distance", 4, 0, 64);
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
        double clamped = Math.max(0.05D, Math.min(1.0D, opacity));
        if (Math.abs(DEFAULT_OPACITY.get() - clamped) < 0.001D)
        {
            return;
        }
        DEFAULT_OPACITY.set(clamped);
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

    public static int borderRgb()
    {
        return (borderRed() << 16) | (borderGreen() << 8) | borderBlue();
    }

    public static void setBorderChannel(int channel, int value)
    {
        ForgeConfigSpec.IntValue target = switch (channel)
        {
            case 0 -> BORDER_RED;
            case 1 -> BORDER_GREEN;
            default -> BORDER_BLUE;
        };
        int clamped = Math.max(0, Math.min(255, value));
        if (target.get() == clamped)
        {
            return;
        }
        target.set(clamped);
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

    public static InputConstants.Key hideKey()
    {
        try
        {
            return InputConstants.getKey(HIDE_KEY.get());
        }
        catch (IllegalArgumentException exception)
        {
            return InputConstants.UNKNOWN;
        }
    }

    public static int hideModifiers()
    {
        return HIDE_MODIFIERS.get();
    }

    public static boolean hideIsDefault()
    {
        return DEFAULT_HIDE_KEY.equals(HIDE_KEY.get()) && HIDE_MODIFIERS.get() == GLFW.GLFW_MOD_SHIFT;
    }

    public static void setHide(InputConstants.Key key, int modifiers)
    {
        String name = key.getName();
        int mask = modifiers & (GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT);
        if (name.equals(HIDE_KEY.get()) && HIDE_MODIFIERS.get() == mask)
        {
            return;
        }
        HIDE_KEY.set(name);
        HIDE_MODIFIERS.set(mask);
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
        double clamped = Math.max(0.0D, Math.min(1.0D, opacity));
        if (Math.abs(SHADOW_OPACITY.get() - clamped) < 0.001D)
        {
            return;
        }
        SHADOW_OPACITY.set(clamped);
    }

    public static int shadowAngle()
    {
        return SHADOW_ANGLE.get();
    }

    public static void setShadowAngle(int angle)
    {
        int clamped = Math.max(0, Math.min(360, angle));
        if (SHADOW_ANGLE.get() == clamped)
        {
            return;
        }
        SHADOW_ANGLE.set(clamped);
    }

    public static int shadowDistance()
    {
        return SHADOW_DISTANCE.get();
    }

    public static void setShadowDistance(int distance)
    {
        setInt(SHADOW_DISTANCE, distance, 0, 64);
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
            default -> SHADOW_BLUE;
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
}

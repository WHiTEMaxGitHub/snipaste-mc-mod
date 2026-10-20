package net.whitemax.snipastemcmod.client;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SnipasteConfig
{
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue DEFAULT_OPACITY;
    public static final ForgeConfigSpec.IntValue MAX_FOLDER_MEGABYTES;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("贴图浮层").push("overlay");
        DEFAULT_OPACITY = builder
                .comment("新贴上的图片使用的透明度，范围 0.05 到 1。")
                .defineInRange("defaultOpacity", 1.0D, 0.05D, 1.0D);
        MAX_FOLDER_MEGABYTES = builder
                .comment("游戏目录 snipaste 文件夹的大小上限，单位 MB。超出后从最旧的图片开始删除。")
                .defineInRange("maxFolderMegabytes", 64, 1, 4096);
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
        int next = Math.max(1, Math.min(4096, maxFolderMegabytes() + delta));
        if (next == maxFolderMegabytes())
        {
            return;
        }
        MAX_FOLDER_MEGABYTES.set(next);
    }
}

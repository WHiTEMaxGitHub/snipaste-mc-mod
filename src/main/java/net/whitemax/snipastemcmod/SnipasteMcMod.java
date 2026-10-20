package net.whitemax.snipastemcmod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.whitemax.snipastemcmod.client.SnipasteConfig;

@Mod(SnipasteMcMod.MOD_ID)
public class SnipasteMcMod
{
    public static final String MOD_ID = "snipaste_mc_mod";

    public SnipasteMcMod(FMLJavaModLoadingContext context)
    {
        context.registerConfig(ModConfig.Type.CLIENT, SnipasteConfig.SPEC);
    }
}

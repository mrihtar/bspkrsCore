package bspkrs.bspkrscore.fml;

import bspkrs.util.CommonUtils;
import bspkrs.util.UniqueNameListGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = "@MOD_VERSION@", useMetadata = true, guiFactory = Reference.GUI_FACTORY, updateJSON = Reference.FORGE_JSON_URL)
public class bspkrsCoreMod
{
    // config stuff
    private final boolean       allowDebugOutputDefault          = false;
    public boolean              allowDebugOutput                 = allowDebugOutputDefault;
    private final boolean       generateUniqueNamesFileDefault   = true;
    private boolean             generateUniqueNamesFile          = generateUniqueNamesFileDefault;

    @Metadata(value = Reference.MODID)
    public static ModMetadata   metadata;

    @Instance(value = Reference.MODID)
    public static bspkrsCoreMod instance;

    @SidedProxy(clientSide = Reference.PROXY_CLIENT, serverSide = Reference.PROXY_COMMON)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        File file = event.getSuggestedConfigurationFile();

        if (!CommonUtils.isObfuscatedEnv())
        { // debug settings for deobfuscated execution
          //            if (file.exists())
          //                file.delete();
        }

        Reference.config = new Configuration(file);

        syncConfig();
    }

    public void syncConfig()
    {
        String ctgyGen = Configuration.CATEGORY_GENERAL;
        Reference.config.load();

        Reference.config.setCategoryComment(ctgyGen, "ATTENTION: Editing this file manually is no longer necessary. \n" +
                "On the Mods list screen select the entry for bspkrsCore, then click the Config button to modify these settings.");

        List<String> orderedKeys = new ArrayList<String>(ConfigElement.values().length);

        allowDebugOutput = Reference.config.getBoolean(ConfigElement.ALLOW_DEBUG_OUTPUT.key(), ctgyGen, allowDebugOutput,
                ConfigElement.ALLOW_DEBUG_OUTPUT.desc(), ConfigElement.ALLOW_DEBUG_OUTPUT.languageKey());
        orderedKeys.add(ConfigElement.ALLOW_DEBUG_OUTPUT.key());
        generateUniqueNamesFile = Reference.config.getBoolean(ConfigElement.GENERATE_UNIQUE_NAMES_FILE.key(), ctgyGen, generateUniqueNamesFileDefault,
                ConfigElement.GENERATE_UNIQUE_NAMES_FILE.desc(), ConfigElement.GENERATE_UNIQUE_NAMES_FILE.languageKey());
        orderedKeys.add(ConfigElement.GENERATE_UNIQUE_NAMES_FILE.key());

        Reference.config.setCategoryPropertyOrder(ctgyGen, orderedKeys);

        if (Reference.config.hasCategory(ctgyGen + ".example_properties"))
            Reference.config.removeCategory(Reference.config.getCategory(ctgyGen + ".example_properties"));

        Reference.config.save();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        if (event.getSide().isClient())
        {
            MinecraftForge.EVENT_BUS.register(new NetworkHandler());
        }

        MinecraftForge.EVENT_BUS.register(instance);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (generateUniqueNamesFile)
            UniqueNameListGenerator.instance().run();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Reference.MODID))
        {
            Reference.config.save();
            syncConfig();
        }
    }
}

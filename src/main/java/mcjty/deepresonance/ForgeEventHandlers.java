package mcjty.deepresonance;

import mcjty.deepresonance.blocks.ModBlocks;
import mcjty.deepresonance.blocks.crystals.ResonatingCrystalTileEntity;
import mcjty.deepresonance.blocks.sensors.AbstractSensorTileEntity;
import mcjty.deepresonance.crafting.ModCrafting;
import mcjty.deepresonance.items.ModItems;
import mcjty.deepresonance.items.rftoolsmodule.RFToolsSupport;
import mcjty.deepresonance.network.DRMessages;
import mcjty.deepresonance.radiation.DRRadiationManager;
import mcjty.deepresonance.radiation.RadiationShieldRegistry;
import mcjty.deepresonance.varia.QuadTree;
import mcjty.lib.McJtyRegister;
import mcjty.lib.blocks.DamageMetadataItemBlock;
import mcjty.lib.network.PacketFinalizeLogin;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Map;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        McJtyRegister.registerBlocks(DeepResonance.instance, event.getRegistry());
        event.getRegistry().register(ModBlocks.denseGlassBlock);
        event.getRegistry().register(ModBlocks.denseObsidianBlock);
        event.getRegistry().register(ModBlocks.debugBlock);
        event.getRegistry().register(ModBlocks.poisonedDirtBlock);
        event.getRegistry().register(ModBlocks.machineFrame);
        event.getRegistry().register(ModBlocks.resonatingPlateBlock);
        event.getRegistry().register(ModBlocks.resonatingOreBlock);
        if (DeepResonance.setup.rftools) {
            event.getRegistry().register(RFToolsSupport.getRadiationSensorBlock());
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        McJtyRegister.registerItems(DeepResonance.instance, event.getRegistry());
        event.getRegistry().register(new ItemBlock(ModBlocks.denseGlassBlock).setRegistryName(ModBlocks.denseGlassBlock.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.denseObsidianBlock).setRegistryName(ModBlocks.denseObsidianBlock.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.debugBlock).setRegistryName(ModBlocks.debugBlock.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.poisonedDirtBlock).setRegistryName(ModBlocks.poisonedDirtBlock.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.machineFrame).setRegistryName(ModBlocks.machineFrame.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.resonatingPlateBlock).setRegistryName(ModBlocks.resonatingPlateBlock.getRegistryName()));
        event.getRegistry().register(new DamageMetadataItemBlock(ModBlocks.resonatingOreBlock).setRegistryName(ModBlocks.resonatingOreBlock.getRegistryName()));

        if (DeepResonance.setup.rftools) {
            event.getRegistry().register(new ItemBlock(RFToolsSupport.getRadiationSensorBlock()).setRegistryName(RFToolsSupport.getRadiationSensorBlock().getRegistryName()));
        }
        event.getRegistry().register(ModItems.boots);
        event.getRegistry().register(ModItems.helmet);
        event.getRegistry().register(ModItems.chestplate);
        event.getRegistry().register(ModItems.leggings);
        ModCrafting.init();
        OreDictionary.registerOre("oreResonating", ModBlocks.resonatingOreBlock);
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
    }

    @SubscribeEvent
    public static void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(DeepResonance.MODID)) {
            ConfigManager.sync(DeepResonance.MODID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        float blocker = RadiationShieldRegistry.getBlocker(event.getState());
        if (blocker >= 0.99f) {
            return;
        }

        World world = event.getWorld();
        DRRadiationManager radiationManager = DRRadiationManager.getManager(world);
        Map<GlobalCoordinate, DRRadiationManager.RadiationSource> radiationSources = radiationManager.getRadiationSources();
        if (radiationSources.isEmpty()) {
            return;
        }

        int x = event.getPos().getX();
        int y = event.getPos().getY();
        int z = event.getPos().getZ();

        for (Map.Entry<GlobalCoordinate, DRRadiationManager.RadiationSource> entry : radiationSources.entrySet()) {
            DRRadiationManager.RadiationSource source = entry.getValue();
            float radius = source.getRadius();
            GlobalCoordinate gc = entry.getKey();
            BlockPos c = gc.getCoordinate();
            if (Math.abs(c.getX()-x) < radius && Math.abs(c.getY()-y) < radius && Math.abs(c.getZ()-z) < radius) {
                Logging.logDebug("Removed blocker at: " + x + "," + y + "," + z);
                QuadTree radiationTree = source.getRadiationTree(world, c.getX(), c.getY(), c.getZ());
                radiationTree.addBlocker(x, y, z, 1.0f);
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlaceEvent(BlockEvent.PlaceEvent event) {
        float blocker = RadiationShieldRegistry.getBlocker(event.getState());
        if (blocker >= 0.99f) {
            return;
        }

        World world = event.getBlockSnapshot().getWorld();
        if (world.isRemote) {
            // Can normally not happen but in rare situations the PlaceEvent can get called client-side
            return;
        }

        DRRadiationManager radiationManager = DRRadiationManager.getManager(world);
        Map<GlobalCoordinate, DRRadiationManager.RadiationSource> radiationSources = radiationManager.getRadiationSources();
        if (radiationSources.isEmpty()) {
            return;
        }

        int x = event.getBlockSnapshot().getPos().getX();
        int y = event.getBlockSnapshot().getPos().getY();
        int z = event.getBlockSnapshot().getPos().getZ();
        for (Map.Entry<GlobalCoordinate, DRRadiationManager.RadiationSource> entry : radiationSources.entrySet()) {
            DRRadiationManager.RadiationSource source = entry.getValue();
            float radius = source.getRadius();
            GlobalCoordinate gc = entry.getKey();
            BlockPos c = gc.getCoordinate();
            if (Math.abs(c.getX()-x) < radius && Math.abs(c.getY()-y) < radius && Math.abs(c.getZ()-z) < radius) {
                Logging.logDebug("Add blocker at: " + x + "," + y + "," + z);
                QuadTree radiationTree = source.getRadiationTree(world, c.getX(), c.getY(), c.getZ());
                radiationTree.addBlocker(x, y, z, blocker);
            }
        }

    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        DRMessages.INSTANCE.sendTo(new PacketFinalizeLogin(), (EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        PacketHandler.onDisconnect();
    }

    @SubscribeEvent
    public void onPostWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote) {
            for (ResonatingCrystalTileEntity crystal : ResonatingCrystalTileEntity.todoCrystals) {
                crystal.realUpdate();
            }
            ResonatingCrystalTileEntity.todoCrystals.clear();
            for (AbstractSensorTileEntity sensor : AbstractSensorTileEntity.todoSensors) {
                sensor.realUpdate();
            }
            AbstractSensorTileEntity.todoSensors.clear();
        }
    }


}

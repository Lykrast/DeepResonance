package mcjty.deepresonance.blocks.ore;

import mcjty.deepresonance.DeepResonance;
import mcjty.deepresonance.config.ConfigMachines;
import mcjty.deepresonance.radiation.DRRadiationManager;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class ResonatingPlateBlock extends Block {

    public ResonatingPlateBlock() {
        super(Material.ROCK);
        setHardness(3.0f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 2);
        if (ConfigMachines.plateBlock.radiationStrength > 0) {
            setTickRandomly(true);
        }
        setUnlocalizedName(DeepResonance.MODID + ".resonating_block");
        setRegistryName("resonating_block");
        setCreativeTab(DeepResonance.setup.getTab());

    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }


    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (ConfigMachines.plateBlock.radiationStrength <= 0) {
            return;
        }
        int powered = world.getStrongPower(pos);
        if (powered > 0) {
            DRRadiationManager radiationManager = DRRadiationManager.getManager(world);
            GlobalCoordinate thisCoordinate = new GlobalCoordinate(pos, world.provider.getDimension());
            if (radiationManager.getRadiationSource(thisCoordinate) == null) {
                Logging.log("Created radiation source with radius " + ConfigMachines.plateBlock.radiationRadius + " and strength " + ConfigMachines.plateBlock.radiationStrength);
            }
            DRRadiationManager.RadiationSource radiationSource = radiationManager.getOrCreateRadiationSource(thisCoordinate);
            radiationSource.update(ConfigMachines.plateBlock.radiationRadius, ConfigMachines.plateBlock.radiationStrength, ConfigMachines.plateBlock.radiationTicks);
            radiationManager.save();
        }
    }

}

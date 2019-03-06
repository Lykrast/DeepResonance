package mcjty.deepresonance.blocks.ore;

import mcjty.deepresonance.DeepResonance;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DenseObsidianBlock extends Block {

    public DenseObsidianBlock() {
        super(Material.ROCK);
        setHardness(50.0f);
        setResistance(2000.0f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 3);
        setUnlocalizedName(DeepResonance.MODID + ".dense_obsidian");
        setRegistryName("dense_obsidian");
        setCreativeTab(DeepResonance.setup.getTab());
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }


}

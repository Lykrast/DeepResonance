package mcjty.deepresonance.blocks.laser;

import mcjty.deepresonance.blocks.GenericDRBlock;
import mcjty.deepresonance.client.ClientHandler;
import mcjty.deepresonance.proxy.GuiProxy;
import mcjty.lib.gui.GenericGuiContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiFunction;

//@Optional.InterfaceList({
      //  @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class LaserBlock extends GenericDRBlock<LaserTileEntity, LaserContainer> {

    public static PropertyInteger COLOR = PropertyInteger.create("color", 0, 3);

    public LaserBlock() {
        super(Material.IRON, LaserTileEntity.class, LaserContainer::new, "laser", false);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.HORIZROTATION;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<LaserTileEntity, LaserContainer, GenericGuiContainer<? super LaserTileEntity>> getGuiFactory() {
        return GuiLaser::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_LASER;
    }

    @Override
    public void initModel() {
        super.initModel();
        LaserRenderer.register();
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag advancedToolTip) {
        super.addInformation(itemStack, player, list, advancedToolTip);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add("Place this laser so it faces a lens.");
            list.add("It will infuse the liquid in the tank");
            list.add("depending on the materials used.");
        } else {
            list.add(TextFormatting.WHITE + ClientHandler.getShiftMessage());
        }
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        boolean rc = super.rotateBlock(world, pos, axis);
        if (world.isRemote) {
            // Make sure rendering is up to date.
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
        return rc;
    }

    /*
    @Override
    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, EnumFacing from) {
        return true;
    }
    */

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(FACING_HORIZ).getIndex() - 2) + ((state.getValue(COLOR)) << 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING_HORIZ, EnumFacing.VALUES[(meta&3) + 2]).withProperty(COLOR, (meta >> 2));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING_HORIZ, COLOR);
    }
}

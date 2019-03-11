package mcjty.deepresonance.blocks.valve;

import mcjty.deepresonance.blocks.GenericDRBlock;
import mcjty.deepresonance.client.ClientHandler;
import mcjty.deepresonance.proxy.GuiProxy;
import mcjty.lib.gui.GenericGuiContainer;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiFunction;

public class ValveBlock extends GenericDRBlock<ValveTileEntity, ValveContainer> {

    public ValveBlock() {
        super(Material.ROCK, ValveTileEntity.class, ValveContainer::new, "valve", true);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.HORIZROTATION;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<ValveTileEntity, ValveContainer, GenericGuiContainer<? super ValveTileEntity>> getGuiFactory() {
        return GuiValve::new;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<String> getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currentTip;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag advancedToolTip) {
        super.addInformation(itemStack, player, list,advancedToolTip);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add("This machine will transfer fluids from the upper tank");
            list.add("to a tank below if the fluid matches certain conditions");
        } else {
            list.add(TextFormatting.WHITE + ClientHandler.getShiftMessage());
        }
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_VALVE;
    }

}

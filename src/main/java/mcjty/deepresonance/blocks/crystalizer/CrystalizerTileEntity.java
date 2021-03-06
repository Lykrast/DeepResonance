package mcjty.deepresonance.blocks.crystalizer;

import elec332.core.world.WorldHelper;
import mcjty.deepresonance.DeepResonance;
import mcjty.deepresonance.blocks.ModBlocks;
import mcjty.deepresonance.blocks.tank.ITankHook;
import mcjty.deepresonance.blocks.tank.TileTank;
import mcjty.deepresonance.config.ConfigMachines;
import mcjty.deepresonance.fluid.DRFluidRegistry;
import mcjty.deepresonance.fluid.LiquidCrystalFluidTagData;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class CrystalizerTileEntity extends GenericEnergyReceiverTileEntity implements ITankHook, DefaultSidedInventory, ITickable {

    public static final String CMD_GETPROGRESS = "getProgress";
    public static final Key<Integer> PARAM_PROGRESS = new Key<>("progress", Type.INTEGER);

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CrystalizerContainer.factory, 1);

    public CrystalizerTileEntity() {
        super(ConfigMachines.crystalizer.rfMaximum, ConfigMachines.crystalizer.rfPerTick);
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    private TileTank rclTank;
    private static int totalProgress = 0;
    private int progress = 0;
    private LiquidCrystalFluidTagData mergedData = null;

    private static int clientProgress = 0;

    public static int getTotalProgress() {
        if (totalProgress == 0) {
            totalProgress = ConfigMachines.crystalizer.rclPerCrystal / ConfigMachines.crystalizer.rclPerTick;
        }
        return totalProgress;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (!canCrystalize()) {
            return;
        }

        storage.extractEnergy(ConfigMachines.crystalizer.rfPerRcl, false);
        FluidStack fluidStack = rclTank.getTank().drain(ConfigMachines.crystalizer.rclPerTick, true);
        LiquidCrystalFluidTagData data = LiquidCrystalFluidTagData.fromStack(fluidStack);
        if (mergedData == null) {
            mergedData = data;
        } else if (data != null) {
            mergedData.merge(data);
        }

        handleProgress();
    }

    private void handleProgress() {
        progress++;
        if (progress == 1) {
            // We just started to work. Notify client
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }

        if (progress >= getTotalProgress()) {
            progress = 0;
            makeCrystal();
            markDirtyClient();
        }

        markDirty();
    }

    public int getProgress() {
        return progress;
    }

    public static int getClientProgress() {
        return clientProgress;
    }



    private boolean canCrystalize() {
        if (rclTank == null || rclTank.getTank() == null) {
            return false;
        }

        if (storage.getEnergyStored() < ConfigMachines.crystalizer.rfPerRcl) {
            return false;
        }

        if (hasCrystal()) {
            return false;
        }

        FluidStack fluidStack = rclTank.getTank().drain(ConfigMachines.crystalizer.rclPerTick, false);
        if (fluidStack == null || fluidStack.amount != ConfigMachines.crystalizer.rclPerTick) {
            return false;
        }

        LiquidCrystalFluidTagData data = LiquidCrystalFluidTagData.fromStack(fluidStack);
        if (data == null) {
            return false;
        }

        return true;
    }

    public boolean hasCrystal() {
        ItemStack crystalStack = inventoryHelper.getStackInSlot(CrystalizerContainer.SLOT_CRYSTAL);
        return !crystalStack.isEmpty();
    }

    private void makeCrystal() {
        ItemStack stack = new ItemStack(ModBlocks.resonatingCrystalBlock);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setFloat("power", 100.0f);
        compound.setFloat("strength", mergedData.getStrength() * 100.0f);
        compound.setFloat("efficiency", mergedData.getEfficiency() * 100.0f);
        compound.setFloat("purity", mergedData.getPurity() * 100.0f);
        compound.setByte("version", (byte) 2);      // Legacy support to support older crystals.
        stack.setTagCompound(compound);
        mergedData = null;
        inventoryHelper.setStackInSlot(CrystalizerContainer.SLOT_CRYSTAL, stack);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("progress", progress);
        if (mergedData != null) {
            NBTTagCompound dataCompound = new NBTTagCompound();
            mergedData.writeDataToNBT(dataCompound);
            tagCompound.setTag("data", dataCompound);
            tagCompound.setInteger("amount", mergedData.getInternalTankAmount());
        }
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        progress = tagCompound.getInteger("progress");
        if (tagCompound.hasKey("data")) {
            NBTTagCompound dataCompound = (NBTTagCompound) tagCompound.getTag("data");
            int amount = dataCompound.getInteger("amount");
            mergedData = LiquidCrystalFluidTagData.fromNBT(dataCompound, amount);
        } else {
            mergedData = null;
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void hook(TileTank tank, EnumFacing direction) {
        if (direction == EnumFacing.DOWN && rclTank == null){
            if (validRCLTank(tank)){
                rclTank = tank;
            }
        }
    }

    @Override
    public void unHook(TileTank tank, EnumFacing direction) {
        if (tilesEqual(rclTank, tank)){
            rclTank = null;
            notifyAndMarkDirty();
        }
    }

    @Override
    public void onContentChanged(TileTank tank, EnumFacing direction) {
        if (tilesEqual(rclTank, tank)){
            if (!validRCLTank(tank)) {
                rclTank = null;
            }
        }
    }

    private boolean validRCLTank(TileTank tank){
        Fluid fluid = DRFluidRegistry.getFluidFromStack(tank.getFluid());
        return fluid == null || fluid == DRFluidRegistry.liquidCrystal;
    }

    private boolean tilesEqual(TileTank first, TileTank second){
        return first != null && second != null && first.getPos().equals(second.getPos()) && first.getWorld().provider.getDimension() == second.getWorld().provider.getDimension();
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { CrystalizerContainer.SLOT_CRYSTAL };
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == CrystalizerContainer.SLOT_CRYSTAL;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(ModBlocks.resonatingCrystalBlock);
    }


    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    // Request the researching amount from the server. This has to be called on the client side.
    public void requestProgressFromServer() {
        requestDataFromServer(DeepResonance.MODID, CMD_GETPROGRESS, TypedMap.EMPTY);
    }

    @Override
    public TypedMap executeWithResult(String command, TypedMap args) {
        TypedMap rc = super.executeWithResult(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETPROGRESS.equals(command)) {
            return TypedMap.builder().put(PARAM_PROGRESS, calculateProgress()).build();
        }
        return null;
    }

    public int calculateProgress() {
        return progress * 100 / getTotalProgress();
    }

    @Override
    public boolean receiveDataFromServer(String command, @Nonnull TypedMap result) {
        boolean rc = super.receiveDataFromServer(command, result);
        if (rc) {
            return true;
        }
        if (CMD_GETPROGRESS.equals(command)) {
            clientProgress = result.get(PARAM_PROGRESS);
            return true;
        }
        return false;
    }

    protected void notifyAndMarkDirty(){
        if (WorldHelper.chunkLoaded(getWorld(), pos)){
            this.markDirty();
            this.getWorld().notifyNeighborsOfStateChange(pos, blockType, false);
        }
    }

}

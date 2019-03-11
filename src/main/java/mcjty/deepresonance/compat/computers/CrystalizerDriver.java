package mcjty.deepresonance.compat.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mcjty.deepresonance.blocks.crystalizer.CrystalizerTileEntity;
import mcjty.lib.integration.computers.AbstractOCDriver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrystalizerDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("deepresonance_crystalizer", CrystalizerTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<CrystalizerTileEntity> {
            public InternalManagedEnvironment(CrystalizerTileEntity tile) {
                super(tile, "deepresonance_crystalizer");
            }

            @Callback(doc="function():number; Get the currently stored energy")
            public Object[] getEnergy(Context c, Arguments a) {
                return new Object[]{tile.getStoredPower()};
            }

            @Callback(doc="function():number; Get the maximum stored energy")
            public Object[] getMaxEnergy(Context c, Arguments a) {
                return new Object[]{tile.getCapacity()};
            }

            @Callback(doc="function():number; Get the current progress in percent")
            public Object[] getProgress(Context c, Arguments a) {
                Integer progress = tile.calculateProgress();
                return new Object[]{progress.intValue()};
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public AbstractManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((CrystalizerTileEntity)tile);
        }
    }
}

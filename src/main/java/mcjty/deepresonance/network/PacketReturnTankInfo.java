package mcjty.deepresonance.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketReturnTankInfo implements IMessage {
    private int amount;
    private int capacity;
    private String fluidName;
    private NBTTagCompound tag;

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readInt();
        capacity = buf.readInt();
        fluidName = NetworkTools.readString(buf);
        tag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(amount);
        buf.writeInt(capacity);
        NetworkTools.writeString(buf, fluidName);
        ByteBufUtils.writeTag(buf, tag);
    }

    public NBTTagCompound getTag() {
        return tag;
    }

    public String getFluidName() {
        return fluidName;
    }

    public int getAmount() {
        return amount;
    }

    public int getCapacity() {
        return capacity;
    }

    public PacketReturnTankInfo() {
    }

    public PacketReturnTankInfo(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketReturnTankInfo(int amount, int capacity, String fluidName, NBTTagCompound tag) {
        this.amount = amount;
        this.capacity = capacity;
        this.fluidName = fluidName;
        this.tag = tag;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ReturnTankInfoHelper.setEnergyLevel(this);
        });
        ctx.setPacketHandled(true);
    }
}

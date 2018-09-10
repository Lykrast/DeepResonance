package mcjty.deepresonance.network;

import io.netty.buffer.ByteBuf;
import mcjty.deepresonance.items.RadiationMonitorItem;
import mcjty.lib.network.IClientServerDelayed;
import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGetRadiationLevel implements IMessage, IClientServerDelayed {

    private GlobalCoordinate coordinate;

    @Override
    public void fromBytes(ByteBuf buf) {
        int dim = buf.readInt();
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        coordinate = new GlobalCoordinate(new BlockPos(x, y, z), dim);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(coordinate.getDimension());
        buf.writeInt(coordinate.getCoordinate().getX());
        buf.writeInt(coordinate.getCoordinate().getY());
        buf.writeInt(coordinate.getCoordinate().getZ());
    }

    public PacketGetRadiationLevel() {
    }

    public PacketGetRadiationLevel(GlobalCoordinate coordinate) {
        this.coordinate = coordinate;
    }

    public static class Handler implements IMessageHandler<PacketGetRadiationLevel, IMessage> {
        @Override
        public IMessage onMessage(PacketGetRadiationLevel message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetRadiationLevel message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            World world = player.getEntityWorld();
            float strength = RadiationMonitorItem.calculateRadiationStrength(world, message.coordinate);
            PacketReturnRadiation packet = new PacketReturnRadiation(strength);
            DRMessages.INSTANCE.sendTo(packet, ctx.getServerHandler().player);
        }

    }

}
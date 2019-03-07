package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketTargetsReady implements IMessage {

    private int target;
    private int[] targets;
    private String[] names;

    @Override
    public void fromBytes(ByteBuf buf) {
        target = buf.readInt();
        int size = buf.readInt();
        targets = new int[size];
        names = new String[size];
        for (int i = 0 ; i < size ; i++) {
            targets[i] = buf.readInt();
            names[i] = NetworkTools.readString(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(target);
        buf.writeInt(targets.length);
        for (int i = 0 ; i < targets.length ; i++) {
            buf.writeInt(targets[i]);
            NetworkTools.writeString(buf, names[i]);
        }
    }

    public PacketTargetsReady() {
    }

    public PacketTargetsReady(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketTargetsReady(int target, int[] targets, String[] names) {
        this.target = target;
        this.targets = targets;
        this.names = names;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiAdvancedPorter.setInfo(target, targets, names);
        });
        ctx.setPacketHandled(true);
    }
}

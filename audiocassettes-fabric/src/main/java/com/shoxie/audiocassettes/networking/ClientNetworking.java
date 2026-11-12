package com.shoxie.audiocassettes.networking;

import com.shoxie.audiocassettes.item.AbstractAudioCassetteItem;
import com.shoxie.audiocassettes.proxy.ClientProxy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Client -> server senders and server -> client receivers. Only ever touched from client code. */
@Environment(EnvType.CLIENT)
public final class ClientNetworking {
    private ClientNetworking() {}

    // ------------------------------------------------------------------
    // client -> server senders
    // ------------------------------------------------------------------

    public static void sendTapeDeckStartWriting(BlockPos pos, ResourceLocation res, String sname, boolean erase) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(res);
        buf.writeUtf(sname.length() > 127 ? "Untitled" : sname);
        buf.writeBoolean(erase);
        ClientPlayNetworking.send(Networking.TAPEDECK_START_WRITING, buf);
    }

    public static void sendTapeDeckSetSong(BlockPos pos, int song) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeInt(song);
        ClientPlayNetworking.send(Networking.TAPEDECK_SET_SONG, buf);
    }

    public static void sendTapeDeckStopWrite(BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(Networking.TAPEDECK_STOP_WRITE, buf);
    }

    public static void sendBoomBoxPlay(BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(Networking.BOOMBOX_PLAY_C, buf);
    }

    public static void sendBoomBoxStop(BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(Networking.BOOMBOX_STOP_C, buf);
    }

    public static void sendBoomBoxNext(BlockPos pos, boolean manually) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeBoolean(manually);
        ClientPlayNetworking.send(Networking.BOOMBOX_NEXT, buf);
    }

    public static void sendBoomBoxPrev(BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(Networking.BOOMBOX_PREV, buf);
    }

    public static void sendWalkmanPlay(String id) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(id);
        ClientPlayNetworking.send(Networking.WALKMAN_PLAY_C, buf);
    }

    public static void sendWalkmanStop() {
        ClientPlayNetworking.send(Networking.WALKMAN_STOP_C, PacketByteBufs.create());
    }

    public static void sendWalkmanNext(String id) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(id);
        ClientPlayNetworking.send(Networking.WALKMAN_NEXT, buf);
    }

    public static void sendWalkmanPrev() {
        ClientPlayNetworking.send(Networking.WALKMAN_PREV, PacketByteBufs.create());
    }

    public static void sendWalkmanDrop(String id) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(id.length() > 10 ? "-" : id, 10);
        ClientPlayNetworking.send(Networking.WALKMAN_DROP, buf);
    }

    // ------------------------------------------------------------------
    // server -> client receivers
    // ------------------------------------------------------------------

    public static void registerClientReceivers() {

        ClientPlayNetworking.registerGlobalReceiver(Networking.BOOMBOX_PLAY_S, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String id = buf.readUtf();
            boolean isowner = buf.readBoolean();
            ItemStack cassette = buf.readItem();
            client.execute(() -> {
                if (cassette.getItem() instanceof AbstractAudioCassetteItem)
                    ClientProxy.BoomBoxPlay(pos, id, isowner,
                            AbstractAudioCassetteItem.getCurrentSong(cassette),
                            AbstractAudioCassetteItem.getSongTitle(cassette));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Networking.BOOMBOX_STOP_S, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String id = buf.readUtf();
            client.execute(() -> ClientProxy.BoomBoxStop(pos, id));
        });

        ClientPlayNetworking.registerGlobalReceiver(Networking.WALKMAN_PLAY_S, (client, handler, buf, responseSender) -> {
            String id = buf.readUtf();
            String playerid = buf.readUtf();
            boolean isowner = buf.readBoolean();
            ItemStack cassette = buf.readItem();
            client.execute(() -> {
                if (cassette.getItem() instanceof AbstractAudioCassetteItem)
                    ClientProxy.WalkmanPlay(id, playerid, isowner,
                            AbstractAudioCassetteItem.getCurrentSong(cassette),
                            AbstractAudioCassetteItem.getSongTitle(cassette));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Networking.WALKMAN_STOP_S, (client, handler, buf, responseSender) -> {
            String id = buf.readUtf();
            boolean isowner = buf.readBoolean();
            client.execute(() -> ClientProxy.WalkmanStop(id, isowner));
        });
    }
}

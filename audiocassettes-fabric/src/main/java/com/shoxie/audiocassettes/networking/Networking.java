package com.shoxie.audiocassettes.networking;

import com.shoxie.audiocassettes.audiocassettes;
import com.shoxie.audiocassettes.entity.BoomBoxEntity;
import com.shoxie.audiocassettes.entity.TapeDeckEntity;
import com.shoxie.audiocassettes.item.WalkmanItem;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Packet ids, server-side receivers (client -> server) and senders for server -> client packets.
 * Client-only parts (sending client -> server, receiving on the client) live in {@link ClientNetworking}.
 * <p>
 * Forge packet class  ->  Fabric channel
 * TapeDeckStartWritingPacket  -> TAPEDECK_START_WRITING
 * TapeDeckSetSongPacket       -> TAPEDECK_SET_SONG
 * TapeDeckStopWritePacket     -> TAPEDECK_STOP_WRITE
 * CBoomBoxPlayPacket          -> BOOMBOX_PLAY_C
 * CBoomBoxStopPacket          -> BOOMBOX_STOP_C
 * BoomBoxNextSongPacket       -> BOOMBOX_NEXT
 * BoomBoxPrevSongPacket       -> BOOMBOX_PREV
 * CWalkmanPlayPacket          -> WALKMAN_PLAY_C
 * CWalkmanStopPacket          -> WALKMAN_STOP_C
 * WalkmanNextSongPacket       -> WALKMAN_NEXT
 * WalkmanPrevSongPacket       -> WALKMAN_PREV
 * WalkmanOnDropPacket         -> WALKMAN_DROP
 * SBoomBoxPlayPacket          -> BOOMBOX_PLAY_S
 * SBoomBoxStopPacket          -> BOOMBOX_STOP_S
 * SWalkmanPlayPacket          -> WALKMAN_PLAY_S
 * SWalkmanStopPacket          -> WALKMAN_STOP_S
 */
public final class Networking {
    private Networking() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(audiocassettes.MODID, path);
    }

    // client -> server
    public static final ResourceLocation TAPEDECK_START_WRITING = id("tapedeck_start_writing");
    public static final ResourceLocation TAPEDECK_SET_SONG = id("tapedeck_set_song");
    public static final ResourceLocation TAPEDECK_STOP_WRITE = id("tapedeck_stop_write");
    public static final ResourceLocation BOOMBOX_PLAY_C = id("boombox_play_c");
    public static final ResourceLocation BOOMBOX_STOP_C = id("boombox_stop_c");
    public static final ResourceLocation BOOMBOX_NEXT = id("boombox_next");
    public static final ResourceLocation BOOMBOX_PREV = id("boombox_prev");
    public static final ResourceLocation WALKMAN_PLAY_C = id("walkman_play_c");
    public static final ResourceLocation WALKMAN_STOP_C = id("walkman_stop_c");
    public static final ResourceLocation WALKMAN_NEXT = id("walkman_next");
    public static final ResourceLocation WALKMAN_PREV = id("walkman_prev");
    public static final ResourceLocation WALKMAN_DROP = id("walkman_drop");

    // server -> client
    public static final ResourceLocation BOOMBOX_PLAY_S = id("boombox_play_s");
    public static final ResourceLocation BOOMBOX_STOP_S = id("boombox_stop_s");
    public static final ResourceLocation WALKMAN_PLAY_S = id("walkman_play_s");
    public static final ResourceLocation WALKMAN_STOP_S = id("walkman_stop_s");

    // ------------------------------------------------------------------
    // server -> client senders
    // ------------------------------------------------------------------

    public static void sendBoomBoxPlay(ServerPlayer to, BlockPos pos, String id, boolean isowner, ItemStack cassette) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeUtf(id);
        buf.writeBoolean(isowner);
        buf.writeItem(cassette);
        ServerPlayNetworking.send(to, BOOMBOX_PLAY_S, buf);
    }

    public static void sendBoomBoxStop(ServerPlayer to, BlockPos pos, String id) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeUtf(id);
        ServerPlayNetworking.send(to, BOOMBOX_STOP_S, buf);
    }

    public static void sendWalkmanPlay(ServerPlayer to, String id, String playerid, boolean isowner, ItemStack cassette) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(id);
        buf.writeUtf(playerid);
        buf.writeBoolean(isowner);
        buf.writeItem(cassette);
        ServerPlayNetworking.send(to, WALKMAN_PLAY_S, buf);
    }

    public static void sendWalkmanStop(ServerPlayer to, String id, boolean isowner) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(id);
        buf.writeBoolean(isowner);
        ServerPlayNetworking.send(to, WALKMAN_STOP_S, buf);
    }

    // ------------------------------------------------------------------
    // client -> server receivers
    // ------------------------------------------------------------------

    private static <T extends BlockEntity> T blockEntity(ServerPlayer player, BlockPos pos, Class<T> type) {
        ServerLevel level = player.serverLevel();
        if (!level.hasChunkAt(pos)) return null;
        BlockEntity be = level.getBlockEntity(pos);
        return type.isInstance(be) ? type.cast(be) : null;
    }

    public static void registerServerReceivers() {

        // ---- Tape deck ----
        ServerPlayNetworking.registerGlobalReceiver(TAPEDECK_START_WRITING, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            ResourceLocation res = buf.readResourceLocation();
            String sname = buf.readUtf(128);
            boolean erase = buf.readBoolean();
            server.execute(() -> {
                TapeDeckEntity entity = blockEntity(player, pos, TapeDeckEntity.class);
                if (entity != null) entity.StartWrite(res, sname, erase);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TAPEDECK_SET_SONG, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int song = buf.readInt();
            server.execute(() -> {
                TapeDeckEntity entity = blockEntity(player, pos, TapeDeckEntity.class);
                if (entity != null) entity.setSong(song);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TAPEDECK_STOP_WRITE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                TapeDeckEntity entity = blockEntity(player, pos, TapeDeckEntity.class);
                if (entity != null) entity.stopWrite();
            });
        });

        // ---- Boombox ----
        ServerPlayNetworking.registerGlobalReceiver(BOOMBOX_PLAY_C, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                BoomBoxEntity entity = blockEntity(player, pos, BoomBoxEntity.class);
                if (entity != null) entity.playMusic(player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BOOMBOX_STOP_C, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                BoomBoxEntity entity = blockEntity(player, pos, BoomBoxEntity.class);
                if (entity != null) entity.stopMusic();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BOOMBOX_NEXT, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean manually = buf.readBoolean();
            server.execute(() -> {
                BoomBoxEntity entity = blockEntity(player, pos, BoomBoxEntity.class);
                if (entity == null) return;
                if (player.getUUID().toString().equals(entity.owneruid) || manually) {
                    boolean switched = entity.switchSong(true);
                    if (manually) {
                        if (entity.isPlaying) {
                            entity.stopMusic();
                            entity.isPlaying = false;
                        }
                    } else if (switched) entity.playMusic(player);
                    else entity.stopMusic();
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BOOMBOX_PREV, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                BoomBoxEntity entity = blockEntity(player, pos, BoomBoxEntity.class);
                if (entity == null) return;
                if (entity.isPlaying) {
                    entity.stopMusic();
                    entity.isPlaying = false;
                }
                entity.switchSong(false);
            });
        });

        // ---- Walkman ----
        ServerPlayNetworking.registerGlobalReceiver(WALKMAN_PLAY_C, (server, player, handler, buf, responseSender) -> {
            String id = buf.readUtf(16);
            server.execute(() -> {
                ItemStack mp = id.equals("-") ? WalkmanItem.getMPInHand(player) : WalkmanItem.getMPbyID(player, id);
                if (!mp.isEmpty())
                    WalkmanItem.playMusic(mp, player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(WALKMAN_STOP_C, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                ItemStack mp = WalkmanItem.getMPInHand(player);
                if (mp.isEmpty()) return;
                WalkmanItem.stopMusic(WalkmanItem.getID(mp), player, false);
                WalkmanItem.setPlaying(mp, false);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(WALKMAN_NEXT, (server, player, handler, buf, responseSender) -> {
            String id = buf.readUtf(16);
            server.execute(() -> {
                ItemStack mp;
                if (id.equals("-")) {
                    mp = WalkmanItem.getMPInHand(player);
                    if (WalkmanItem.isPlaying(mp)) {
                        WalkmanItem.stopMusic(WalkmanItem.getID(mp), player, false);
                        WalkmanItem.setPlaying(mp, false);
                    }
                } else mp = WalkmanItem.getMPbyID(player, id);
                if (mp.isEmpty()) return;

                boolean switched = WalkmanItem.switchSong(true, mp);
                if (!id.equals("-") && switched) WalkmanItem.playMusic(mp, player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(WALKMAN_PREV, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                ItemStack mp = WalkmanItem.getMPInHand(player);
                if (mp.isEmpty()) return;
                WalkmanItem.switchSong(false, mp);
                if (WalkmanItem.isPlaying(mp)) {
                    WalkmanItem.stopMusic(WalkmanItem.getID(mp), player, false);
                    WalkmanItem.setPlaying(mp, false);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(WALKMAN_DROP, (server, player, handler, buf, responseSender) -> {
            String id = buf.readUtf(10);
            server.execute(() -> WalkmanItem.stopMusic(id, player, true));
        });
    }
}

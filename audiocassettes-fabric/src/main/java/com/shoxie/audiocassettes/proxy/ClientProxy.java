package com.shoxie.audiocassettes.proxy;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.shoxie.audiocassettes.Config;
import com.shoxie.audiocassettes.audiocassettes;
import com.shoxie.audiocassettes.audio.BoomBoxSound;
import com.shoxie.audiocassettes.audio.WalkmanSound;
import com.shoxie.audiocassettes.entity.BoomBoxEntity;
import com.shoxie.audiocassettes.item.WalkmanItem;
import com.shoxie.audiocassettes.networking.ClientNetworking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Client-side sound handling. This used to be the Forge DistExecutor "proxy" (IProxy/ClientProxy/ServerProxy);
 * on Fabric client-only code simply lives in client-only classes, so no server stub is needed.
 */
@Environment(EnvType.CLIENT)
public final class ClientProxy {
    private ClientProxy() {}

    private static final Map<String, BoomBoxSound> cpsounds = new ConcurrentHashMap<>();
    private static final Map<String, WalkmanSound> walkmanSounds = new ConcurrentHashMap<>();

    private static boolean isActive(Minecraft mc, SoundInstance sound) {
        return sound != null && mc.getSoundManager().isActive(sound);
    }

    // ------------------------------------------------------------------ Walkman

    public static void WalkmanPlay(String id, String playerid, boolean isowner, SoundEvent snd, String songtitle) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            if (isActive(mc, walkmanSounds.get(id))) return;
            Player self = mc.player;
            if (self == null || mc.level == null) return;

            if (isowner) {
                ItemStack mp = WalkmanItem.getMPbyID(self, id);
                WalkmanItem.setPlaying(mp, true);
            }

            Player owner = isowner ? self : mc.level.getPlayerByUUID(UUID.fromString(playerid));
            if (owner == null) return;

            WalkmanSound sound = new WalkmanSound(owner, snd);
            walkmanSounds.put(id, sound);
            if (Config.isNowPlayingAnnounce())
                mc.gui.setOverlayMessage(Component.translatable("record.nowPlaying", songtitle), true);
            mc.getSoundManager().play(sound);

            if (!isowner) return;

            // Only the owner's client drives the playlist: when the song ends it asks the server for the next one,
            // and if the walkman has left the owner's inventory it tells the server to stop.
            Thread watcher = new Thread(() -> {
                boolean presentMP = WalkmanItem.isPlayerOwnMp(self, id);
                while (isActive(mc, sound) && presentMP) {
                    try {
                        Thread.sleep(1000);
                        presentMP = WalkmanItem.isPlayerOwnMp(self, id);
                    } catch (InterruptedException e) {
                        audiocassettes.logger.warn(e.getMessage());
                        return;
                    }
                }

                if (presentMP) {
                    ItemStack mp = WalkmanItem.getMPbyID(self, id);
                    boolean isPlaying = mp.getItem() instanceof WalkmanItem && WalkmanItem.isPlaying(mp);
                    if (isPlaying && mc.level != null)
                        mc.execute(() -> ClientNetworking.sendWalkmanNext(id));
                } else {
                    mc.execute(() -> ClientNetworking.sendWalkmanDrop(id));
                }
            }, "audiocassettes-walkman-watcher");
            watcher.setDaemon(true);
            watcher.start();
        });
    }

    public static void WalkmanStop(String id, boolean isowner) {
        Minecraft mc = Minecraft.getInstance();
        if (isowner && mc.player != null) {
            ItemStack mp = WalkmanItem.getMPbyID(mc.player, id);
            if (mp.getItem() instanceof WalkmanItem) {
                WalkmanItem.setPlaying(mp, false);
            }
        }
        WalkmanSound sound = walkmanSounds.get(id);
        if (sound != null)
            mc.getSoundManager().stop(sound);
    }

    public static boolean isWalkmanPlaying(String id) {
        return id != null && isActive(Minecraft.getInstance(), walkmanSounds.get(id));
    }

    // ------------------------------------------------------------------ BoomBox

    public static void BoomBoxPlay(BlockPos pos, String id, boolean isowner, SoundEvent snd, String songtitle) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            if (isActive(mc, cpsounds.get(id))) return;
            if (mc.level == null || mc.player == null) return;
            BlockEntity be = mc.level.getBlockEntity(pos);
            if (!(be instanceof BoomBoxEntity entity)) return;

            BoomBoxSound sound = new BoomBoxSound(snd, pos.getX(), pos.getY(), pos.getZ(), mc.player);
            cpsounds.put(id, sound);
            entity.id = id;
            entity.isPlaying = true;
            if (Config.isNowPlayingAnnounce())
                mc.gui.setOverlayMessage(Component.translatable("record.nowPlaying", songtitle), true);
            mc.getSoundManager().play(sound);

            Thread watcher = new Thread(() -> {
                while (isActive(mc, sound)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        audiocassettes.logger.warn(e.getMessage());
                        return;
                    }
                }

                // song ended on its own (not stopped by the player): the owner asks for the next one
                if (!entity.isRemoved() && entity.isPlaying && mc.level != null && isowner)
                    mc.execute(() -> ClientNetworking.sendBoomBoxNext(entity.getBlockPos(), false));
            }, "audiocassettes-boombox-watcher");
            watcher.setDaemon(true);
            watcher.start();
        });
    }

    public static void BoomBoxStop(BlockPos pos, String id) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            BoomBoxSound sound = cpsounds.get(id);
            if (sound != null)
                mc.getSoundManager().stop(sound);
            if (mc.level != null && mc.level.getBlockEntity(pos) instanceof BoomBoxEntity entity)
                entity.isPlaying = false;
        });
    }

    public static boolean isBoomBoxPlaying(String id) {
        return id != null && isActive(Minecraft.getInstance(), cpsounds.get(id));
    }
}

package com.shoxie.audiocassettes;

import com.shoxie.audiocassettes.init.Init;
import com.shoxie.audiocassettes.networking.Networking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class audiocassettes implements ModInitializer {

    public static final String MODID = "audiocassettes";
    public static final String NAME = "AudioCassettes";
    public static final String VERSION = "1.2";
    public static final Logger logger = LoggerFactory.getLogger(MODID);
    public static final String EMPTY_SOUND = "empty";
    public static int WalkmanMaxSoundDistance = 16;
    public static int BoomBoxMaxSoundDistance = 16;

    @Override
    public void onInitialize() {
        Config.load();

        // Registers blocks, items, block entities, menus and sound events
        Init.init();

        // Server-bound packet receivers
        Networking.registerServerReceivers();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(Init.BOOMBOX_ITEM);
            entries.accept(Init.TAPEDECK_ITEM);
            entries.accept(Init.DIAMOND_AUDIO_CASSETTE);
            entries.accept(Init.GOLDEN_AUDIO_CASSETTE);
            entries.accept(Init.IRON_AUDIO_CASSETTE);
            entries.accept(Init.WALKMAN);
            entries.accept(Init.CASSETTE_FRAME);
            entries.accept(Init.MAGNETIC_TAPE);
        });
    }
}

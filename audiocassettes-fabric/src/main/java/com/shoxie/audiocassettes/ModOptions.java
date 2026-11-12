package com.shoxie.audiocassettes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ModOptions {
    public static final OptionInstance<Boolean> NOW_PLAYING_ANNOUNCE =
            OptionInstance.createBoolean(
                    "options.audiocassettes.nowplayingannounce",
                    OptionInstance.cachedConstantTooltip(Component.translatable("options.audiocassettes.nowplayingannounce.tooltip")),
                    Config.isNowPlayingAnnounce(), Config::setNowPlayingAnnounce);

    public static final OptionInstance<Boolean> SKIP_EMPTY_SLOTS =
            OptionInstance.createBoolean(
                    "options.audiocassettes.skipemptyslots",
                    OptionInstance.cachedConstantTooltip(Component.translatable("options.audiocassettes.skipemptyslots.tooltip")),
                    Config.SkipEmptySlots(), Config::setSkipEmptySlots);
}

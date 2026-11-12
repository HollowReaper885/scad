package com.shoxie.audiocassettes;

import com.shoxie.audiocassettes.gui.BoomBoxScreen;
import com.shoxie.audiocassettes.gui.TapeDeckScreen;
import com.shoxie.audiocassettes.gui.WalkmanScreen;
import com.shoxie.audiocassettes.init.Init;
import com.shoxie.audiocassettes.networking.ClientNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public class AudiocassettesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(Init.BOOMBOX_MENU, BoomBoxScreen::new);
        MenuScreens.register(Init.TAPEDECK_MENU, TapeDeckScreen::new);
        MenuScreens.register(Init.WALKMAN_MENU, WalkmanScreen::new);

        ClientNetworking.registerClientReceivers();
    }
}

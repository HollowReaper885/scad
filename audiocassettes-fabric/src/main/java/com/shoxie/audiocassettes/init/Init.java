package com.shoxie.audiocassettes.init;

import com.shoxie.audiocassettes.audiocassettes;
import com.shoxie.audiocassettes.block.BoomBoxBlock;
import com.shoxie.audiocassettes.block.TapeDeckBlock;
import com.shoxie.audiocassettes.entity.BoomBoxEntity;
import com.shoxie.audiocassettes.entity.TapeDeckEntity;
import com.shoxie.audiocassettes.item.*;
import com.shoxie.audiocassettes.menu.BoomBoxMenu;
import com.shoxie.audiocassettes.menu.TapeDeckMenu;
import com.shoxie.audiocassettes.menu.WalkmanMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Registry holder. Fabric has no DeferredRegister: the static fields below are registered as soon as this class
 * is initialised, which {@link #init()} forces during mod initialisation.
 */
public class Init {

    private static ResourceLocation id(String path) {
        return new ResourceLocation(audiocassettes.MODID, path);
    }

    private static <T extends Block> T block(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, id(name), block);
    }

    private static <T extends Item> T item(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, id(name), item);
    }

    // Blocks, BIs, BEs
    public static final BoomBoxBlock BOOMBOX = block("boombox", new BoomBoxBlock(BlockBehaviour.Properties.of().strength(3.0f, 10f)));
    public static final TapeDeckBlock TAPEDECK = block("tapedeck", new TapeDeckBlock(BlockBehaviour.Properties.of().strength(3.0f, 10f)));

    public static final Item BOOMBOX_ITEM = item("boombox", new BlockItem(BOOMBOX, new Item.Properties()));
    public static final Item TAPEDECK_ITEM = item("tapedeck", new BlockItem(TAPEDECK, new Item.Properties()));

    public static final BlockEntityType<BoomBoxEntity> BOOM_BOX_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, id("boombox"),
            BlockEntityType.Builder.of(BoomBoxEntity::new, BOOMBOX).build(null));

    public static final BlockEntityType<TapeDeckEntity> TAPEDECK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, id("tapedeck"),
            BlockEntityType.Builder.of(TapeDeckEntity::new, TAPEDECK).build(null));

    // Items
    public static final IronAudioCassetteItem IRON_AUDIO_CASSETTE = item(IronAudioCassetteItem.name, new IronAudioCassetteItem());
    public static final GoldenAudioCassetteItem GOLDEN_AUDIO_CASSETTE = item(GoldenAudioCassetteItem.name, new GoldenAudioCassetteItem());
    public static final DiamondAudioCassetteItem DIAMOND_AUDIO_CASSETTE = item(DiamondAudioCassetteItem.name, new DiamondAudioCassetteItem());
    public static final WalkmanItem WALKMAN = item(WalkmanItem.name, new WalkmanItem());
    public static final CassetteFrameItem CASSETTE_FRAME = item(CassetteFrameItem.name, new CassetteFrameItem());
    public static final MagneticTapeItem MAGNETIC_TAPE = item(MagneticTapeItem.name, new MagneticTapeItem());

    // Sound events
    public static final SoundEvent BLANK_RECORD_SOUND_EVENT = Registry.register(
            BuiltInRegistries.SOUND_EVENT, id(audiocassettes.EMPTY_SOUND),
            SoundEvent.createFixedRangeEvent(id(audiocassettes.EMPTY_SOUND), 128.0F));

    // Menus
    public static final MenuType<BoomBoxMenu> BOOMBOX_MENU = Registry.register(
            BuiltInRegistries.MENU, id("boombox"),
            new ExtendedScreenHandlerType<BoomBoxMenu>((syncId, inv, buf) -> new BoomBoxMenu(syncId, inv, buf)));

    public static final MenuType<WalkmanMenu> WALKMAN_MENU = Registry.register(
            BuiltInRegistries.MENU, id("walkman"),
            new ExtendedScreenHandlerType<WalkmanMenu>((syncId, inv, buf) -> new WalkmanMenu(syncId, inv, buf)));

    public static final MenuType<TapeDeckMenu> TAPEDECK_MENU = Registry.register(
            BuiltInRegistries.MENU, id("tapedeck"),
            new ExtendedScreenHandlerType<TapeDeckMenu>((syncId, inv, buf) -> new TapeDeckMenu(syncId, inv, buf)));

    /** Forces class initialisation (and therefore registration). */
    public static void init() {
    }
}

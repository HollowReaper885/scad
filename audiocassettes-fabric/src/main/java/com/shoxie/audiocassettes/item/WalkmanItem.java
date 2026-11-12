package com.shoxie.audiocassettes.item;

import java.util.List;
import java.util.Random;


import com.shoxie.audiocassettes.Config;
import com.shoxie.audiocassettes.audiocassettes;
import com.shoxie.audiocassettes.inventory.StackNbt;
import com.shoxie.audiocassettes.menu.WalkmanMenu;
import com.shoxie.audiocassettes.networking.Networking;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Forge version kept the inserted cassette in an item-handler capability on the stack.
 * Fabric has no item capabilities, so the cassette now lives in the stack's own NBT under {@link #TAG_CASSETTE},
 * using the same layout the capability used (a 1-slot ItemStackHandler tag).
 */
public class WalkmanItem extends Item implements ExtendedScreenHandlerFactory {
	public static final String TAG_CASSETTE = "walkman";

    public int slotscnt = 1;
	public static String name = "walkman";

	public WalkmanItem() {
        super(new Item.Properties().stacksTo(1));
	}

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inv, Player player) {
        return new WalkmanMenu(i, inv, player);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        // nothing to send: the menu uses the walkman in the player's hand
    }

    @Override
    
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) {
            getID(player.getItemInHand(hand)); // make sure the walkman has an id before the menu opens
            sp.openMenu(this);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        // assign the id on the server so it is synced to the client, not generated independently on both sides
        if (!level.isClientSide()) {
            CompoundTag tag = stack.getTag();
            if (tag == null || tag.getString("uid").isEmpty())
                getID(stack);
        }
    }

	@Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        ItemStack cassette = getCassette(stack);

        if (cassette.getItem() instanceof AbstractAudioCassetteItem)
            tooltip.add(Component.literal(
                    "Current Song: " + AbstractAudioCassetteItem.getCurrentSlot(cassette)
                            + ". " + AbstractAudioCassetteItem.getSongTitle(cassette)));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.audiocassettes.walkman");
    }

	public static void setTagBool(ItemStack mp, String name, boolean val) {
		if(mp.getItem() instanceof WalkmanItem) {
			mp.getOrCreateTag().putInt(name, val ? 1 : 0);
		}
	}

	public static void setPlaying(ItemStack stack, boolean val) {
		setTagBool(stack,"isPlaying", val);
	}

	public static boolean isPlaying(ItemStack stack) {
		return stack.getItem() instanceof WalkmanItem && stack.hasTag() && stack.getTag().getInt("isPlaying") == 1;
	}

	/** Switches the song of the cassette that is inside the given walkman. */
	public static boolean switchSong(boolean forward, ItemStack walkman) {
		ItemStack cassette = getCassette(walkman);
		if(!(cassette.getItem() instanceof AbstractAudioCassetteItem)) return false;

		boolean switched = switchSongOnCassette(forward, cassette);
		if(switched) setCassette(walkman, cassette); // cassette is a copy, write it back
		return switched;
	}

	private static boolean switchSongOnCassette(boolean forward, ItemStack cassette) {
		if(Config.SkipEmptySlots()) {
			int song = AbstractAudioCassetteItem.getNonEmptySlot(cassette,forward);
			if(song != -1) AbstractAudioCassetteItem.setActiveSlot(song, cassette);
			else return false;
		}
	    else {
			int cur=AbstractAudioCassetteItem.getCurrentSlot(cassette);
			int max=AbstractAudioCassetteItem.getMaxSlots(cassette);
			if(forward) {
		        if(cur < max)
		            AbstractAudioCassetteItem.setActiveSlot(cur+1, cassette);
		        else if(cur > max)
		            AbstractAudioCassetteItem.setActiveSlot(max, cassette);
		        else return false;
			}
			else {
	            if(cur > 1)
	            	AbstractAudioCassetteItem.setActiveSlot(cur-1, cassette);
	            else if(cur < 1)
	            	AbstractAudioCassetteItem.setActiveSlot(1, cassette);
	            else return false;
			}
	    }
		return true;
	}

	public static ItemStack getMPInHand(Player player) {
		if(player == null) return ItemStack.EMPTY;
		ItemStack mp = player.getMainHandItem();
		if(!(mp.getItem() instanceof WalkmanItem))
			mp = player.getOffhandItem();
		return mp.getItem() instanceof WalkmanItem ? mp : ItemStack.EMPTY;
	}

	/** Finds the walkman with the given id in the player's selected slot, inventory or offhand. */
	public static ItemStack getMPbyID(Player p, String id) {
		if(p == null || id == null) return ItemStack.EMPTY;
		ItemStack selected = p.getInventory().getSelected();
		if(selected.getItem() instanceof WalkmanItem && id.equals(getID(selected)))
			return selected;

		for(ItemStack s : p.getInventory().items)
			if(s.getItem() instanceof WalkmanItem && id.equals(getID(s)))
				return s;

		ItemStack off = p.getOffhandItem();
		if(off.getItem() instanceof WalkmanItem && id.equals(getID(off)))
			return off;

		return ItemStack.EMPTY;
	}

    public static void playMusic(ItemStack mp, ServerPlayer sender) {
    	List<ServerPlayer> players = sender.getServer().getPlayerList().getPlayers();
    	WalkmanItem.setPlaying(mp, true);
    	ItemStack cassette = getCassette(mp);
    	if(cassette.getItem() instanceof AbstractAudioCassetteItem) {
    		String id = getID(mp);
	    	for(ServerPlayer player : players) {
	    		if(
	    				Math.abs(player.getX() - sender.getX()) < audiocassettes.WalkmanMaxSoundDistance &&
	    				Math.abs(player.getY() - sender.getY()) < audiocassettes.WalkmanMaxSoundDistance &&
	    				Math.abs(player.getZ() - sender.getZ()) < audiocassettes.WalkmanMaxSoundDistance
	    				)
	    		{
	    			Networking.sendWalkmanPlay(player, id, sender.getUUID().toString(), player == sender, cassette);
	    		}
	    	}
    	}
    }

	/** Returns the walkman's id, creating it if the walkman does not have one yet. */
	public static String getID(ItemStack mp) {
		if(!(mp.getItem() instanceof WalkmanItem)) return null;
		CompoundTag nbt = mp.getOrCreateTag();
		if(nbt.getString("uid").isEmpty())
			nbt.putString("uid", Integer.toString(new Random().nextInt(10000)));
		return nbt.getString("uid");
	}

	public static void stopMusic(String mpid, ServerPlayer sender, boolean isdropped) {
    	List<ServerPlayer> players = sender.getServer().getPlayerList().getPlayers();
    	for(ServerPlayer player : players) {
			Networking.sendWalkmanStop(player, mpid, player == sender && !isdropped);
    	}
	}

	/** Returns a COPY of the cassette inside the walkman. Use {@link #setCassette} to change it. */
	public static ItemStack getCassette(ItemStack mp) {
		if(mp == null || !(mp.getItem() instanceof WalkmanItem) || !mp.hasTag())
			return ItemStack.EMPTY;
		return StackNbt.readSingle(mp.getTag().getCompound(TAG_CASSETTE));
	}

	public static void setCassette(ItemStack mp, ItemStack cassette) {
		if(mp == null || !(mp.getItem() instanceof WalkmanItem)) return;
		if(cassette == null || cassette.isEmpty()) {
			if(mp.hasTag()) mp.getTag().remove(TAG_CASSETTE);
			return;
		}
		mp.getOrCreateTag().put(TAG_CASSETTE, StackNbt.writeSingle(cassette));
	}

	public static boolean isPlayerOwnMp(Player p, String id) {
		return !getMPbyID(p, id).isEmpty();
	}
}

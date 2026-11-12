package com.shoxie.audiocassettes.entity;

import java.util.List;
import java.util.Random;


import com.shoxie.audiocassettes.audiocassettes;
import com.shoxie.audiocassettes.Config;
import com.shoxie.audiocassettes.init.Init;
import com.shoxie.audiocassettes.inventory.StackInventory;
import com.shoxie.audiocassettes.item.AbstractAudioCassetteItem;
import com.shoxie.audiocassettes.menu.BoomBoxMenu;
import com.shoxie.audiocassettes.networking.Networking;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BoomBoxEntity extends BlockEntity implements Nameable, ExtendedScreenHandlerFactory {
    public int slotscnt = 1;
	public boolean isPlaying=false;
	public String id = "-";
	public String owneruid = "-";

    private final StackInventory inventory = new StackInventory(slotscnt, this::onInventoryChanged);

    public BoomBoxEntity(BlockPos pos, BlockState state) {
        super(Init.BOOM_BOX_ENTITY, pos, state);
    }

    private void onInventoryChanged() {
        if (level != null && !level.isClientSide())
            setChanged();
        sendUpdates();
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inv, Player player) {
        return new BoomBoxMenu(i, inv, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound("cassette"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("cassette", this.inventory.serializeNBT());
    }

    @Override
    public Component getName() {
        return getDisplayName();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.audiocassettes.boombox");
    }

    public void sendUpdates() {
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithFullMetadata();
    }

    @Override
    
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

	public ItemStack getCassette() {
		return inventory.getItem(0);
	}

	public void setSong(int song) {
		AbstractAudioCassetteItem.setActiveSlot(song, getCassette());
		sendUpdates();
		setChanged();
	}

	public boolean switchSong(boolean forward) {

		if(Config.SkipEmptySlots()) {
			int song = AbstractAudioCassetteItem.getNonEmptySlot(getCassette(),forward);
			if(song != -1) setSong(song);
			else return false;
		}
	    else {
			int cur=AbstractAudioCassetteItem.getCurrentSlot(getCassette());
			int max=AbstractAudioCassetteItem.getMaxSlots(getCassette());
			if(forward) {
		        if(cur < max)
		            this.setSong(cur+1);
		        else if(cur > max)
		            this.setSong(max);
		        else return false;
			}
			else {
	            if(cur > 1)
	            	this.setSong(cur-1);
	            else if(cur < 1)
	            	this.setSong(1);
	            else return false;
			}
	    }
		sendUpdates();
		return true;
	}

    public void playMusic(ServerPlayer sender) {

    	List<ServerPlayer> players = this.getLevel().getServer().getPlayerList().getPlayers();
    	owneruid = sender.getUUID().toString();

    	this.isPlaying = true;
    	for(ServerPlayer player : players) {
    		if(
    				Math.abs(player.getX() - this.getBlockPos().getX()) < audiocassettes.BoomBoxMaxSoundDistance &&
    				Math.abs(player.getY() - this.getBlockPos().getY()) < audiocassettes.BoomBoxMaxSoundDistance &&
    				Math.abs(player.getZ() - this.getBlockPos().getZ()) < audiocassettes.BoomBoxMaxSoundDistance
    				)
    		{
    			boolean isowner = (player == sender);
				Networking.sendBoomBoxPlay(player, this.getBlockPos(), this.getID(), isowner, this.getCassette());
    		}
    	}
    }

	public void stopMusic() {
    	List<ServerPlayer> players = this.level.getServer().getPlayerList().getPlayers();
    	for(ServerPlayer player : players) {
			Networking.sendBoomBoxStop(player, this.getBlockPos(), this.getID());
    	}
	}

    public StackInventory getInventory() {
        return inventory;
    }

    public String getID() {
	    if (this.id.equals("-")) {
		    Random rand = new Random();
		    int randomNum = rand.nextInt(10000);
	        this.id = Integer.toString(randomNum);
	    }
	    return this.id;
	}
}

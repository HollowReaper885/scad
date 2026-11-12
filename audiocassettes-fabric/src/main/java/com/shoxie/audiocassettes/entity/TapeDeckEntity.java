package com.shoxie.audiocassettes.entity;


import com.shoxie.audiocassettes.init.Init;
import com.shoxie.audiocassettes.inventory.StackInventory;
import com.shoxie.audiocassettes.item.AbstractAudioCassetteItem;

import com.shoxie.audiocassettes.menu.TapeDeckMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TapeDeckEntity extends BlockEntity implements Nameable, ExtendedScreenHandlerFactory {

	public static final int Slotscnt = 2;
	private boolean Started = false;
	private int WriteTime=0;
    // 0 = Disc 1 = Cassette
    ResourceLocation res;
	String sname;
	private boolean erase = false;

    private final StackInventory inventory = new StackInventory(Slotscnt, this::onInventoryChanged);

    public TapeDeckEntity(BlockPos pos, BlockState state) {
        super(Init.TAPEDECK_ENTITY, pos, state);
    }

    private void onInventoryChanged() {
        if (level != null && !level.isClientSide())
            setChanged();
        sendUpdates();
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inv, Player player) {
        return new TapeDeckMenu(i, inv, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }

    public void StartWrite(ResourceLocation res, String sname, boolean erase) {
    	if(
    			(
    					this.getItemInSlot(0).getItem() instanceof RecordItem ||
    					erase
    			)
    			&& this.getItemInSlot(1).getItem() instanceof AbstractAudioCassetteItem
	    )
    	{
    		AbstractAudioCassetteItem c = (AbstractAudioCassetteItem) this.getItemInSlot(1).getItem();
	    	WriteTime = c.getMaxWriteTime();
	    	Started = true;
	    	this.res = res;
	    	this.sname = sname;
	    	this.erase = erase;
    	}
    }

    public int getCurWriteTime() {
		return WriteTime;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound("inv"));
        WriteTime = tag.getInt("WriteTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inv", this.inventory.serializeNBT());
        tag.putInt("WriteTime", WriteTime);
    }

    public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        TapeDeckEntity tde = (TapeDeckEntity) blockEntity;
        if(tde.isWriting()) {

    		--tde.WriteTime;
            tde.sendUpdates();
    	}
    	else {
	    	ItemStack disc = tde.getItemInSlot(0);
	    	ItemStack cassette = tde.getItemInSlot(1);
	    	if(
			    !cassette.isEmpty() &&
			    ((!disc.isEmpty() && disc.getItem() instanceof RecordItem) || tde.erase) &&
		    	cassette.getItem() instanceof AbstractAudioCassetteItem &&
                tde.Started
	    	)
                tde.FinaliseWrite(cassette, tde.erase);
    	}
    }

    private void FinaliseWrite(ItemStack cassette, boolean erase) {
        if(!erase)
    	    if(!(cassette.getItem() instanceof AbstractAudioCassetteItem))
    	    	return;

    	AbstractAudioCassetteItem.appendSongs(cassette, res,sname);
		Started=false;
		// the cassette's NBT was changed in place: persist and sync it
		if (level != null && !level.isClientSide())
			setChanged();
		sendUpdates();
	}

	private ItemStack getItemInSlot(int itnum) {
        return this.inventory.getItem(itnum);
	}

    @Override
    public Component getName() {
        return getDisplayName();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.audiocassettes.tapedeck");
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

	public boolean isWriting() {
		return WriteTime > 0;
	}

	public void stopWrite() {
		Started = false;
		WriteTime = 0;
		sendUpdates();
	}

	public void setSong(int song) {
		AbstractAudioCassetteItem.setActiveSlot(song, this.getItemInSlot(1));
		sendUpdates();
		if (level != null && !level.isClientSide())
			setChanged();
	}

    public StackInventory getInventory() {
        return this.inventory;
    }
}

package com.shoxie.audiocassettes.slots;

import com.shoxie.audiocassettes.item.AbstractAudioCassetteItem;
import com.shoxie.audiocassettes.entity.TapeDeckEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TapeDeckCassetteSlot extends Slot {
	TapeDeckEntity entity;
	public TapeDeckCassetteSlot(Container container, int index, int xPosition, int yPosition, TapeDeckEntity entity) {
		super(container, index, xPosition, yPosition);
		this.entity = entity;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
	    return stack.getItem() instanceof AbstractAudioCassetteItem;
	}

	@Override
	public void setChanged() {
		if(!this.getItem().isEmpty())
			if(this.getItem().getItem() instanceof AbstractAudioCassetteItem)
				if(!this.getItem().hasTag()) {
			        CompoundTag nbt = new CompoundTag();
			        ItemStack stack = this.getItem();
					for(int i=1;i<=AbstractAudioCassetteItem.getMaxSlots(stack);i++)
					{
						nbt.putString("Song"+i, ("audiocassettes"+":"+"empty"));
						nbt.putString("SongName"+i, "--Empty--");
					}
					nbt.putInt("ms", 1);
					stack.setTag(nbt);
				}
	    super.setChanged();
	}
}

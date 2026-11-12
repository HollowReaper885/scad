package com.shoxie.audiocassettes.slots;

import com.shoxie.audiocassettes.entity.TapeDeckEntity;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;

public class TapeDeckDiscSlot extends Slot {
	TapeDeckEntity entity;
	public TapeDeckDiscSlot(Container container, int index, int xPosition, int yPosition, TapeDeckEntity entity) {
		super(container, index, xPosition, yPosition);
		this.entity = entity;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
	    return stack.getItem() instanceof RecordItem;
	}
}

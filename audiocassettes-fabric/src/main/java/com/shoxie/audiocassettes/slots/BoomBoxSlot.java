package com.shoxie.audiocassettes.slots;

import com.shoxie.audiocassettes.entity.BoomBoxEntity;
import com.shoxie.audiocassettes.item.AbstractAudioCassetteItem;
import com.shoxie.audiocassettes.menu.BoomBoxMenu;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoomBoxSlot extends Slot {
	private final BoomBoxMenu c;
	public BoomBoxSlot(Container container, int index, int xPosition, int yPosition, BoomBoxMenu c) {
		super(container, index, xPosition, yPosition);
		this.c = c;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
	    return stack.getItem() instanceof AbstractAudioCassetteItem;
	}

	@Override
	public void setChanged() {
		// When the cassette is taken out of a playing boombox, stop the music (server side).
		BoomBoxEntity entity = c.getEntity();
		Level level = entity.getLevel();
		if (level != null && !level.isClientSide() && this.getItem().isEmpty() && entity.isPlaying) {
			entity.stopMusic();
			entity.isPlaying = false;
		}
	    super.setChanged();
	}
}

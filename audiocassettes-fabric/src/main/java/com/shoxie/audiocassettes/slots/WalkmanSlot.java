package com.shoxie.audiocassettes.slots;

import com.shoxie.audiocassettes.item.AbstractAudioCassetteItem;
import com.shoxie.audiocassettes.item.WalkmanItem;
import com.shoxie.audiocassettes.menu.WalkmanMenu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WalkmanSlot extends Slot {
	private final WalkmanMenu c;
	public WalkmanSlot(Container container, int index, int xPosition, int yPosition, WalkmanMenu menu) {
		super(container, index, xPosition, yPosition);
		this.c = menu;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof AbstractAudioCassetteItem;
    }

	@Override
	public void setChanged() {
		super.setChanged();
		// When the cassette is taken out of a playing walkman, stop the music (server side).
		if (c.player instanceof ServerPlayer sp && this.getItem().isEmpty()) {
			ItemStack mp = WalkmanItem.getMPInHand(sp);
			if (WalkmanItem.isPlaying(mp)) {
				WalkmanItem.stopMusic(WalkmanItem.getID(mp), sp, false);
				WalkmanItem.setPlaying(mp, false);
			}
		}
	}
}

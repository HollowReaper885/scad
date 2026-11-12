package com.shoxie.audiocassettes.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;

/**
 * Replacement for Forge's ItemStackHandler: a plain vanilla container that calls back when it changes
 * and (de)serializes in the same format.
 */
public class StackInventory extends SimpleContainer {
    private final Runnable changeListener;
    private boolean loading = false;

    public StackInventory(int size, Runnable changeListener) {
        super(size);
        this.changeListener = changeListener;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading && changeListener != null) {
            changeListener.run();
        }
    }

    public CompoundTag serializeNBT() {
        return StackNbt.write(this);
    }

    public void deserializeNBT(CompoundTag tag) {
        loading = true;
        try {
            StackNbt.read(tag, this);
        } finally {
            loading = false;
        }
    }
}

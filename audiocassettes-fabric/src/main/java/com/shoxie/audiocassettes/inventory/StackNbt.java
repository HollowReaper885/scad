package com.shoxie.audiocassettes.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Reads/writes containers in the exact NBT layout used by Forge's ItemStackHandler:
 * {Size: int, Items: [{Slot: int, id: .., Count: .., tag: ..}]}
 * so data saved by the Forge version keeps loading.
 */
public final class StackNbt {
    private StackNbt() {}

    public static CompoundTag write(Container container) {
        ListTag items = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag item = new CompoundTag();
                item.putInt("Slot", i);
                stack.save(item);
                items.add(item);
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", items);
        tag.putInt("Size", container.getContainerSize());
        return tag;
    }

    public static void read(CompoundTag tag, Container container) {
        container.clearContent();
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getInt("Slot");
            if (slot >= 0 && slot < container.getContainerSize()) {
                container.setItem(slot, ItemStack.of(item));
            }
        }
    }

    /** Writes a single stack as a 1-slot handler tag. */
    public static CompoundTag writeSingle(ItemStack stack) {
        ListTag items = new ListTag();
        if (!stack.isEmpty()) {
            CompoundTag item = new CompoundTag();
            item.putInt("Slot", 0);
            stack.save(item);
            items.add(item);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", items);
        tag.putInt("Size", 1);
        return tag;
    }

    /** Reads slot 0 out of a handler tag. */
    public static ItemStack readSingle(CompoundTag tag) {
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            if (item.getInt("Slot") == 0) {
                return ItemStack.of(item);
            }
        }
        return ItemStack.EMPTY;
    }
}

package com.shoxie.audiocassettes.inventory;

import com.shoxie.audiocassettes.item.WalkmanItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * One-slot view of the cassette stored inside the walkman the player is holding.
 * <p>
 * Replaces the Forge item-handler capability. The walkman stack is looked up from the player's hands on every
 * access (client-side stack objects get replaced by server syncs, so holding on to one would go stale), and the
 * cassette is written back to the walkman's NBT whenever the slot changes.
 */
public class WalkmanContainer implements Container {
    private final Player player;
    private ItemStack cache;

    public WalkmanContainer(Player player) {
        this.player = player;
        this.cache = read();
    }

    private ItemStack read() {
        return WalkmanItem.getCassette(WalkmanItem.getMPInHand(player));
    }

    /** Re-read from the walkman if its NBT was changed from the outside (e.g. the song was switched). */
    private void refresh() {
        ItemStack fromNbt = read();
        if (!ItemStack.matches(fromNbt, cache)) {
            cache = fromNbt;
        }
    }

    private void write() {
        ItemStack mp = WalkmanItem.getMPInHand(player);
        if (!mp.isEmpty()) {
            WalkmanItem.setCassette(mp, cache);
        }
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        refresh();
        return cache.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        refresh();
        return slot == 0 ? cache : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        refresh();
        if (slot != 0 || cache.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack removed = cache.split(amount);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        refresh();
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack removed = cache;
        cache = ItemStack.EMPTY;
        write();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;
        cache = stack;
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    /** A walkman holds exactly one cassette. */
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        write();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        cache = ItemStack.EMPTY;
        write();
    }
}

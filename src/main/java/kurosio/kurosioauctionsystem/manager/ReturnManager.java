package kurosio.kurosioauctionsystem.manager;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ReturnManager {

    private final Map<UUID, List<ItemStack>> pendingReturns = new HashMap<>();

    public void addReturn(UUID uuid, ItemStack item) {

        pendingReturns
                .computeIfAbsent(uuid, k -> new ArrayList<>())
                .add(item);
    }

    public List<ItemStack> getReturns(UUID uuid) {
        return pendingReturns.get(uuid);
    }

    public void remove(UUID uuid) {
        pendingReturns.remove(uuid);
    }
}
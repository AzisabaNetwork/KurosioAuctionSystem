package kurosio.kurosioauctionsystem.manager;

import kurosio.kurosioauctionsystem.KurosioAuctionSystem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ReturnManager {

    private final KurosioAuctionSystem plugin;

    private final Map<UUID, List<ItemStack>> pendingReturns =
            new HashMap<>();

    private File returnFile;
    private YamlConfiguration returnConfig;

    public ReturnManager(KurosioAuctionSystem plugin) {

        this.plugin = plugin;

        setupFile();
        loadReturns();
    }

    private void setupFile() {

        returnFile = new File(
                plugin.getDataFolder(),
                "returns.yml"
        );

        if (!returnFile.exists()) {
            try {
                returnFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        returnConfig =
                YamlConfiguration.loadConfiguration(
                        returnFile
                );
    }

    public void addReturn(UUID uuid, ItemStack item) {

        pendingReturns
                .computeIfAbsent(
                        uuid,
                        k -> new ArrayList<>()
                )
                .add(item.clone());

        saveReturns();
    }

    public List<ItemStack> getReturns(UUID uuid) {

        return pendingReturns.getOrDefault(
                uuid,
                new ArrayList<>()
        );
    }

    public void remove(UUID uuid) {

        pendingReturns.remove(uuid);

        saveReturns();
    }

    public void saveReturns() {

        returnConfig.set("returns", null);

        for (Map.Entry<UUID, List<ItemStack>> entry :
                pendingReturns.entrySet()) {

            returnConfig.set(
                    "returns." + entry.getKey(),
                    entry.getValue()
            );
        }

        try {
            returnConfig.save(returnFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadReturns() {

        pendingReturns.clear();

        if (returnConfig.getConfigurationSection("returns") == null) {
            return;
        }

        for (String uuidString :
                returnConfig.getConfigurationSection("returns")
                        .getKeys(false)) {

            UUID uuid = UUID.fromString(uuidString);

            List<ItemStack> items =
                    (List<ItemStack>) returnConfig.getList(
                            "returns." + uuidString
                    );

            if (items != null) {
                pendingReturns.put(
                        uuid,
                        new ArrayList<>(items)
                );
            }
        }
    }
}
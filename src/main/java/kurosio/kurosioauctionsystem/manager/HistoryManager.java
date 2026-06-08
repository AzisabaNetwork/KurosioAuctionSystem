package kurosio.kurosioauctionsystem.manager;

import kurosio.kurosioauctionsystem.KurosioAuctionSystem;
import kurosio.kurosioauctionsystem.data.AuctionData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class HistoryManager {

    private final KurosioAuctionSystem plugin;

    private final File file;
    private final FileConfiguration config;

    private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public HistoryManager(KurosioAuctionSystem plugin) {
        this.plugin = plugin;

        this.file = new File(plugin.getDataFolder(), "history.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveHistory(AuctionData auction) {

        String base = "history." + auction.getAuctionId();

        // =========================
        // 時間
        // =========================
        config.set(base + ".start-time", format(auction.getStartTime()));
        config.set(base + ".end-time", format(System.currentTimeMillis()));

        // =========================
        // 出品者
        // =========================
        UUID seller = auction.getSellerUUID();
        config.set(base + ".seller.uuid", seller.toString());
        config.set(base + ".seller.name", getName(seller));

        // =========================
        // 落札者
        // =========================
        UUID winner = auction.getHighestBidder();

        if (winner != null) {
            config.set(base + ".winner.uuid", winner.toString());
            config.set(base + ".winner.name", getName(winner));
        } else {
            config.set(base + ".winner.uuid", "NONE");
            config.set(base + ".winner.name", "NONE");
        }

        // =========================
        // アイテム情報
        // =========================
        ItemStack item = auction.getItem();
        ItemMeta meta = item.getItemMeta();

        String displayName = (meta != null && meta.hasDisplayName())
                ? meta.getDisplayName()
                : item.getType().name();

        config.set(base + ".item.type", item.getType().name());
        config.set(base + ".item.amount", item.getAmount());
        config.set(base + ".item.display-name", displayName);

        // MMID
        config.set(base + ".item.mythic-item-id", auction.getMythicItemId());

        // =========================
        // 価格
        // =========================
        config.set(base + ".price", auction.getCurrentPrice());

        save();
    }

    // =========================
    // utils
    // =========================

    private String format(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZONE)
                .format(FORMATTER);
    }

    private String getName(UUID uuid) {
        if (uuid == null) return "UNKNOWN";

        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "UNKNOWN";
    }
}
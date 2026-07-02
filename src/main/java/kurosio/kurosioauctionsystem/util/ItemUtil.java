package kurosio.kurosioauctionsystem.util;

import kurosio.kurosioauctionsystem.KurosioAuctionSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemUtil {

    public static boolean giveItemOrStash(Player player, ItemStack item) {

        String mode = KurosioAuctionSystem.getInstance()
                .getConfig()
                .getString("return.mode", "AUTO")
                .toUpperCase();

        // =========================
        // STASH_ONLY
        // =========================
        if (mode.equals("STASH_ONLY")
                && Bukkit.getPluginManager().isPluginEnabled("ItemStash")) {

            try {

                Class<?> clazz =
                        Class.forName("net.azisaba.itemstash.ItemStash");

                Object instance =
                        clazz.getMethod("getInstance")
                                .invoke(null);

                clazz.getMethod(
                                "addItemToStash",
                                java.util.UUID.class,
                                ItemStack.class
                        )
                        .invoke(
                                instance,
                                player.getUniqueId(),
                                item
                        );

                player.sendMessage(
                        ChatUtil.color(
                                ChatUtil.PREFIX +
                                        "&a返却待ちアイテムをItemStashへ返却しました。"
                        )
                );

                return true;

            } catch (Exception e) {
                return false;
            }
        }

        // =========================
        // AUTO
        // =========================

        Map<Integer, ItemStack> leftOver =
                player.getInventory().addItem(item);

        if (leftOver.isEmpty()) {
            return true;
        }

        boolean notified = false;

        for (ItemStack left : leftOver.values()) {

            boolean stashed = false;

            if (Bukkit.getPluginManager().isPluginEnabled("ItemStash")) {

                try {

                    Class<?> clazz =
                            Class.forName("net.azisaba.itemstash.ItemStash");

                    Object instance =
                            clazz.getMethod("getInstance")
                                    .invoke(null);

                    clazz.getMethod(
                                    "addItemToStash",
                                    java.util.UUID.class,
                                    ItemStack.class
                            )
                            .invoke(
                                    instance,
                                    player.getUniqueId(),
                                    left
                            );

                    stashed = true;

                    if (!notified) {

                        player.sendMessage(
                                ChatUtil.color(
                                        ChatUtil.PREFIX +
                                                "&eインベントリがいっぱいのため、ItemStashへ送信しました。"
                                )
                        );

                        notified = true;
                    }

                } catch (Exception ignored) {
                }
            }

            if (!stashed) {

                player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        left
                );

                if (!notified) {

                    player.sendMessage(
                            ChatUtil.color(
                                    ChatUtil.PREFIX +
                                            "&cインベントリがいっぱいのため、足元へドロップしました。"
                            )
                    );

                    notified = true;
                }
            }
        }

        return true;
    }
}
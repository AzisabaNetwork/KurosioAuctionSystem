package kurosio.kurosioauctionsystem.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemUtil {

    public static void giveItemOrStash(Player player, ItemStack item) {

        Map<Integer, ItemStack> leftOver =
                player.getInventory().addItem(item);

        if (leftOver.isEmpty()) {
            return;
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
                    // ItemStash側でエラーが起きた場合は床へドロップ
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
    }
}
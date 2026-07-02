package kurosio.kurosioauctionsystem.listener;

import kurosio.kurosioauctionsystem.KurosioAuctionSystem;
import kurosio.kurosioauctionsystem.manager.ReturnManager;
import kurosio.kurosioauctionsystem.util.ChatUtil;
import kurosio.kurosioauctionsystem.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(
                KurosioAuctionSystem.getInstance(),
                () -> {

                    // 待機中に退出していたら何もしない
                    if (!player.isOnline()) {
                        return;
                    }

                    ReturnManager returnManager =
                            KurosioAuctionSystem.getInstance().getReturnManager();

                    List<ItemStack> items =
                            returnManager.getReturns(player.getUniqueId());

                    if (items.isEmpty()) {
                        return;
                    }

                    boolean success = true;

                    for (ItemStack item : items) {

                        if (!ItemUtil.giveItemOrStash(player, item)) {
                            success = false;
                            break;
                        }
                    }

                    if (!success) {
                        return;
                    }

                    returnManager.remove(player.getUniqueId());

                    player.sendMessage(ChatUtil.color(
                            ChatUtil.PREFIX +
                                    "&a返却待ちだったアイテムを返却しました。"
                    ));

                },
                KurosioAuctionSystem.getInstance()
                        .getConfig()
                        .getLong("return.join-delay")
        );
    }
}
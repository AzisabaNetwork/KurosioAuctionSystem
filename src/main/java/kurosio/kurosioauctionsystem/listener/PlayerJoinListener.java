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
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(
                KurosioAuctionSystem.getInstance(),
                () -> {

                    ReturnManager manager =
                            KurosioAuctionSystem.getInstance()
                                    .getReturnManager();

                    List<ItemStack> items =
                            manager.getReturns(
                                    player.getUniqueId()
                            );

                    if (items.isEmpty()) {
                        return;
                    }

                    for (ItemStack item : items) {

                        ItemUtil.giveItemOrStash(
                                player,
                                item
                        );
                    }

                    player.sendMessage(ChatUtil.color(
                            ChatUtil.PREFIX +
                                    "&a未受取の返却アイテムを受け取りました。"
                    ));

                    manager.remove(player.getUniqueId());

                },
                60L
        );
    }
}
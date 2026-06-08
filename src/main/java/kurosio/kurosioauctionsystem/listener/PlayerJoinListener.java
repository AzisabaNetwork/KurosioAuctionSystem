package kurosio.kurosioauctionsystem.listener;

import kurosio.kurosioauctionsystem.KurosioAuctionSystem;
import kurosio.kurosioauctionsystem.manager.ReturnManager;
import kurosio.kurosioauctionsystem.util.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();

        ReturnManager manager =
                KurosioAuctionSystem.getInstance()
                        .getReturnManager();

        List<ItemStack> items =
                manager.getReturns(
                        player.getUniqueId()
                );

        if (items == null || items.isEmpty()) {
            return;
        }

        for (ItemStack item : items) {

            Map<Integer, ItemStack> leftOver =
                    player.getInventory().addItem(item);

            for (ItemStack left : leftOver.values()) {

                player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        left
                );
            }
        }

        player.sendMessage(ChatUtil.color(
                ChatUtil.PREFIX +
                        "&eサーバー異常終了により中止されたオークションのアイテムを返却しました。"
        ));

        manager.remove(player.getUniqueId());
    }
}
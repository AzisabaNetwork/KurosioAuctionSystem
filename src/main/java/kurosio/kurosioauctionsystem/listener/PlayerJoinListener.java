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

        ReturnManager returnManager =
                KurosioAuctionSystem.getInstance().getReturnManager();

        List<ItemStack> items =
                returnManager.getReturns(player.getUniqueId());

        if (items.isEmpty()) {
            return;
        }

        for (ItemStack item : items) {
            ItemUtil.giveItemOrStash(player, item);
        }

        returnManager.remove(player.getUniqueId());

        player.sendMessage(ChatUtil.color(
                ChatUtil.PREFIX +
                        "&a返却待ちだったアイテムを返却しました。"
        ));
    }
}
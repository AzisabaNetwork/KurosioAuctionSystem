package kurosio.kurosioauctionsystem.listener;

import kurosio.kurosioauctionsystem.KurosioAuctionSystem;
import kurosio.kurosioauctionsystem.data.AuctionData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class AuctionQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        KurosioAuctionSystem plugin = KurosioAuctionSystem.getInstance();
        UUID uuid = event.getPlayer().getUniqueId();

        for (AuctionData auction : plugin.getAuctionManager().getAuctions()) {

            if (!auction.isActive()) continue;

            // 出品者なら即終了
            if (auction.getSellerUUID().equals(uuid)) {
                plugin.cancelAuction(auction, "出品者がログアウトしたため");
                return;
            }
        }

        // 参加者なら退出
        if (plugin.getAuctionManager().hasJoined(uuid)) {
            plugin.getAuctionManager().leaveAuction(uuid);
        }
    }
}
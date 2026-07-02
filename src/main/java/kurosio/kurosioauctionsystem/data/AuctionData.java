package kurosio.kurosioauctionsystem.data;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AuctionData {

    private final String auctionId;
    private final UUID sellerUUID;
    private final ItemStack item;

    private final Map<UUID, Long> highestOffers = new HashMap<>();

    private long startPrice;
    private long currentPrice;
    private long bidUnit;

    private long startTime;
    private long lastBidTime;
    private long endTime;

    private boolean active = true;
    private boolean autoBidEnabled = false;
    private boolean lastAutoBid = false;

    private UUID highestBidder;
    private UUID excludedPlayer;

    private String mythicItemId;

    public AuctionData(String auctionId,
                       UUID sellerUUID,
                       ItemStack item,
                       long startPrice,
                       long bidUnit) {

        this.auctionId = auctionId;
        this.sellerUUID = sellerUUID;
        this.item = item;

        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.bidUnit = bidUnit;

        this.startTime = System.currentTimeMillis();
        this.lastBidTime = System.currentTimeMillis();
    }

    // ===== 基本 =====

    public String getAuctionId() { return auctionId; }
    public UUID getSellerUUID() { return sellerUUID; }
    public ItemStack getItem() { return item; }

    public long getStartPrice() { return startPrice; }
    public long getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(long v) { this.currentPrice = v; }

    public long getBidUnit() { return bidUnit; }

    public long getStartTime() { return startTime; }
    public long getLastBidTime() { return lastBidTime; }
    public void setLastBidTime(long v) { this.lastBidTime = v; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long v) { this.endTime = v; }

    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }

    public boolean isAutoBidEnabled() { return autoBidEnabled; }
    public void setAutoBidEnabled(boolean v) { this.autoBidEnabled = v; }

    public boolean isLastAutoBid() { return lastAutoBid; }
    public void setLastAutoBid(boolean v) { this.lastAutoBid = v; }

    public UUID getHighestBidder() { return highestBidder; }
    public void setHighestBidder(UUID v) { this.highestBidder = v; }

    public UUID getExcludedPlayer() { return excludedPlayer; }
    public void setExcludedPlayer(UUID v) { this.excludedPlayer = v; }

    public String getMythicItemId() { return mythicItemId; }
    public void setMythicItemId(String v) { this.mythicItemId = v; }

    // ===== 入札管理 =====

    public Map<UUID, Long> getHighestOffers() {
        return highestOffers;
    }

    public void removeBidder(UUID uuid) {
        highestOffers.remove(uuid);
    }

    public List<Map.Entry<UUID, Long>> getRanking() {

        List<Map.Entry<UUID, Long>> list =
                new ArrayList<>(highestOffers.entrySet());

        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        return list;
    }

    public long getHighestOfferPrice() {
        return getRanking().isEmpty()
                ? startPrice
                : getRanking().get(0).getValue();
    }

    public void updateWinner(UUID bidder, long offerPrice, long currentPrice) {
        this.highestBidder = bidder;
        this.currentPrice = currentPrice;
    }

    private boolean finishing = false;

    public boolean isFinishing() {
        return finishing;
    }

    public void setFinishing(boolean finishing) {
        this.finishing = finishing;
    }
}
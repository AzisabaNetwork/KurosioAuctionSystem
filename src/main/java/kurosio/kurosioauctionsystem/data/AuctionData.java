package kurosio.kurosioauctionsystem.data;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    private boolean active = true;
    private boolean autoBidEnabled = false;

    private long endTime;

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
        this.highestOfferPrice = startPrice;
        this.bidUnit = bidUnit;
        this.active = true;
        this.lastBidTime = System.currentTimeMillis();
        this.startTime = System.currentTimeMillis();
    }


    public String getAuctionId() {
        return auctionId;
    }

    public UUID getSellerUUID() {
        return sellerUUID;
    }

    public ItemStack getItem() {
        return item;
    }


    public long getStartPrice() {
        return startPrice;
    }

    public long getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(long currentPrice) {
        this.currentPrice = currentPrice;
    }

    public long getBidUnit() {
        return bidUnit;
    }


    public long getLastBidTime() {
        return lastBidTime;
    }

    public void setLastBidTime(long lastBidTime) {
        this.lastBidTime = lastBidTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private UUID highestBidder;

    public UUID getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(UUID highestBidder) {
        this.highestBidder = highestBidder;
    }

    private long highestOfferPrice;

    public long getHighestOfferPrice() {
        return highestOfferPrice;
    }

    public void setHighestOfferPrice(long highestOfferPrice) {
        this.highestOfferPrice = highestOfferPrice;
    }


    public long getStartTime() {
        return startTime;
    }

    public String getMythicItemId() {
        return mythicItemId;
    }

    public void setMythicItemId(String mythicItemId) {
        this.mythicItemId = mythicItemId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    private boolean lastAutoBid = false;

    public boolean isLastAutoBid() {
        return lastAutoBid;
    }

    public void setLastAutoBid(boolean lastAutoBid) {
        this.lastAutoBid = lastAutoBid;
    }

    public boolean isAutoBidEnabled() {
        return autoBidEnabled;
    }

    public void setAutoBidEnabled(boolean autoBidEnabled) {
        this.autoBidEnabled = autoBidEnabled;
    }

    private UUID excludedPlayer;

    public UUID getExcludedPlayer() {
        return excludedPlayer;
    }

    public void setExcludedPlayer(UUID excludedPlayer) {
        this.excludedPlayer = excludedPlayer;
    }

    public Map<UUID, Long> getHighestOffers() {
        return highestOffers;
    }

    public List<Map.Entry<UUID, Long>> getRanking() {

        List<Map.Entry<UUID, Long>> ranking =
                new ArrayList<>(highestOffers.entrySet());

        ranking.sort((a, b) -> {

            int compare =
                    Long.compare(b.getValue(), a.getValue());

            if (compare != 0) {
                return compare;
            }

            return 0;
        });

        return ranking;
    }

    public void removeBidder(UUID uuid) {
        highestOffers.remove(uuid);
    }


    public void updateWinner(UUID bidder, long offerPrice, long currentPrice) {

        this.highestBidder = bidder;
        this.highestOfferPrice = offerPrice;
        this.currentPrice = currentPrice;
    }



}
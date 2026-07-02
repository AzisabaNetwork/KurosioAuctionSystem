package kurosio.kurosioauctionsystem.command;

import kurosio.kurosioauctionsystem.KurosioAuctionSystem;
import kurosio.kurosioauctionsystem.data.AuctionData;
import kurosio.kurosioauctionsystem.manager.AuctionManager;
import kurosio.kurosioauctionsystem.manager.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import kurosio.kurosioauctionsystem.util.ChatUtil;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.*;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import static kurosio.kurosioauctionsystem.util.ChatUtil.color;

public class KACCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {


        if (args.length == 1 && args[0].equalsIgnoreCase("test")) {

            String id = KurosioAuctionSystem
                    .getInstance()
                    .getAuctionManager()
                    .generateAuctionId();

            sender.sendMessage("Generated ID: " + id);

            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(color(
                    ChatUtil.PREFIX + "&a/kac help &fでヘルプを表示できます。"
            ));
            return true;
        }


        if (args.length == 1 && args[0].equalsIgnoreCase("money")) {

            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("Player only");
                return true;
            }

            org.bukkit.entity.Player player =
                    (org.bukkit.entity.Player) sender;

            double money = VaultManager
                    .getEconomy()
                    .getBalance(player);

            player.sendMessage("所持金: " + money + "円");

            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("start")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("プレイヤーのみ実行可能");
                return true;
            }

            Player player = (Player) sender;

            long startPrice;
            long bidUnit = 1000;
            double radius = 30;
            boolean autoBidEnabled = false;
            UUID excludedPlayer = null;

            // =====================
            // 開始価格
            // =====================
            try {
                startPrice = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("価格が不正です");
                return true;
            }

            if (startPrice <= 0) {
                player.sendMessage("開始価格は1円以上にしてください");
                return true;
            }

            for (int i = 2; i < args.length; i++) {

                String option = args[i];

                if (option.startsWith("u:")) {

                    try {
                        bidUnit = Long.parseLong(option.substring(2));
                    } catch (NumberFormatException e) {
                        player.sendMessage("入札単位が不正です");
                        return true;
                    }

                    if (bidUnit <= 0) {
                        player.sendMessage("入札単位は1円以上を指定してください。");
                        return true;
                    }
                } else if (option.startsWith("r:")) {

                    try {
                        radius = Double.parseDouble(option.substring(2));
                    } catch (NumberFormatException e) {
                        player.sendMessage("半径が不正です");
                        return true;
                    }

                    if (radius > 50) {
                        player.sendMessage("半径は最大50mまでです");
                        radius = 50;
                    }

                    if (radius < 0) {
                        player.sendMessage("半径は0以上にしてください");
                        return true;
                    }

                } else if (option.startsWith("auto:")) {

                    String value = option.substring(5);

                    if (value.equalsIgnoreCase("on")) {
                        autoBidEnabled = true;
                    } else if (value.equalsIgnoreCase("off")) {
                        autoBidEnabled = false;
                    } else {
                        player.sendMessage("auto:on または auto:off を指定してください");
                        return true;
                    }

                } else if (option.startsWith("exp:")) {

                    String name = option.substring(4);

                    Player target = Bukkit.getPlayerExact(name);

                    if (target == null) {
                        player.sendMessage("プレイヤーが見つかりません。");
                        return true;
                    }

                    excludedPlayer = target.getUniqueId();

                } else {

                    player.sendMessage("不明なオプション: " + option);
                    return true;
                }
            }

            // =====================
            // アイテム取得
            // =====================
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage("アイテムを持ってください");
                return true;
            }

            ItemMeta meta = item.getItemMeta();

            String displayName = (meta != null && meta.hasDisplayName())
                    ? meta.getDisplayName()
                    : item.getType().name();

            // =====================
            // オークション作成
            // =====================
            AuctionManager manager = KurosioAuctionSystem.getInstance().getAuctionManager();

            if (manager.hasAuction(player.getUniqueId())) {
                player.sendMessage("既に出品中です");
                return true;
            }

            String auctionId = manager.generateAuctionId();

            AuctionData auction = new AuctionData(
                    auctionId,
                    player.getUniqueId(),
                    item.clone(),
                    startPrice,
                    bidUnit
            );

            auction.setAutoBidEnabled(autoBidEnabled);
            auction.setExcludedPlayer(excludedPlayer);

            manager.addAuction(auction);
            manager.registerSeller(
                    player.getUniqueId(),
                    auctionId
            );

            manager.registerJoin(player.getUniqueId(), auctionId);

// YAMLへ即保存
            manager.notifyUpdate();

            player.getInventory().setItemInMainHand(null);
            player.updateInventory();

            // =====================
            // 通知対象
            // =====================
            List<Player> targets = new ArrayList<>();

            if (radius <= 0) {
                targets.addAll(Bukkit.getOnlinePlayers());
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(player.getWorld())
                            && p.getLocation().distance(player.getLocation()) <= radius) {
                        targets.add(p);
                    }
                }
            }

            // =====================
            // 通知
            // =====================
            for (Player p : targets) {

                ChatUtil.send(p, "&6&l[ＫＡＣオークション]");
                ChatUtil.send(p, "&e=======================");
                ChatUtil.send(p, "&a" + player.getName() + "&fさんがオークションを開始しました！");
                ChatUtil.send(p, "&eID&f: &f" + auction.getAuctionId());
                TextComponent itemLine =
                        new TextComponent(
                                color(
                                        "&eアイテム名&f: "
                                )
                        );

                TextComponent itemName =
                        new TextComponent(
                                color(
                                        "&f" + displayName
                                )
                        );

                itemName.setHoverEvent(
                        new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder(
                                        buildItemHover(item)
                                ).create()
                        )
                );

                itemLine.addExtra(itemName);

                p.spigot().sendMessage(itemLine);
                int amount = item.getAmount();
                if (amount > 1) {
                    ChatUtil.send(p, "&e個数&f: &f" + amount);
                }
                ChatUtil.send(p, "&e開始価格&f: &6" + String.format("%,d", startPrice) + "円");
                ChatUtil.send(p, "&e入札単位&f: &6" + String.format("%,d", bidUnit) + "円");
                ChatUtil.send(
                        p,
                        "&e自動入札機能&f: " +
                                (auction.isAutoBidEnabled() ? "&aON" : "&cOFF")
                );

                // =====================
                // クリック参加UI
                // =====================
                TextComponent join = new TextComponent("§a§l参加§fするにはこちらをクリック！");

                join.setClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/kac join " + auctionId
                ));

                join.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("§aクリックしてオークションに参加！").create()
                ));

                p.spigot().sendMessage(join);

                ChatUtil.send(p, "&e=======================");
            }

            player.sendMessage("オークションを作成しました ID:" + auctionId);

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("exlist")) {

            if (!(sender instanceof Player)) {
                return true;
            }

            Player player = (Player) sender;

            AuctionManager manager =
                    KurosioAuctionSystem.getInstance()
                            .getAuctionManager();

            String auctionId =
                    manager.getSellerAuction(
                            player.getUniqueId()
                    );

            if (auctionId == null) {

                auctionId =
                        manager.getJoinedAuction(
                                player.getUniqueId()
                        );
            }

            if (auctionId == null) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c参加中・出品中のオークションがありません。"
                ));

                return true;
            }

            AuctionData auction = KurosioAuctionSystem
                    .getInstance()
                    .getAuctionManager()
                    .getAuction(auctionId);

            if (auction == null) {
                player.sendMessage("オークション情報が見つかりません");
                return true;
            }

            ChatUtil.send(player, ChatUtil.PREFIX);
            ChatUtil.send(player, "&eID: &f" + auction.getAuctionId());
            ChatUtil.send(player, "&e開始価格: &6&l" + String.format("%,d", auction.getStartPrice()) + "円");
            ChatUtil.send(player, "&e現在価格: &6&l" + String.format("%,d", auction.getCurrentPrice()) + "円");
            ItemStack item = auction.getItem();
            ItemMeta meta = item.getItemMeta();

            String displayName = (meta != null && meta.hasDisplayName())
                    ? meta.getDisplayName()
                    : item.getType().name();

            TextComponent itemLine =
                    new TextComponent(
                            color(
                                    "&eアイテム&f: "
                            )
                    );

            TextComponent itemName =
                    new TextComponent(
                            color(
                                    "&f" + displayName
                            )
                    );

            itemName.setHoverEvent(
                    new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(
                                    ChatUtil.buildItemHover(item)
                            ).create()
                    )
            );

            itemLine.addExtra(itemName);

            player.spigot().sendMessage(itemLine);
            int amount = item.getAmount();
            if (amount > 1) {
                ChatUtil.send(player, "&e個数&f: &f" + amount);
            }

            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("bid")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("プレイヤーのみ実行可能");
                return true;
            }

            Player player = (Player) sender;

            // 現在のオークション取得
            AuctionManager manager = KurosioAuctionSystem.getInstance()
                    .getAuctionManager();


            String auctionId = manager.getJoinedAuction(player.getUniqueId());

            if (auctionId == null) {
                player.sendMessage("参加中のオークションがありません");
                return true;
            }


            AuctionData auction = manager.getAuction(auctionId);
            manager.recalculateWinner(auction);

            if (auction == null || !auction.isActive()) {
                player.sendMessage("オークションが存在しません");
                return true;
            }

            if (auction.getExcludedPlayer() != null
                    && auction.getExcludedPlayer().equals(player.getUniqueId())) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&cこのオークションには入札できません。"
                ));

                return true;
            }

            // =========================
// 出品者は入札不可
// =========================
            if (auction.getSellerUUID().equals(player.getUniqueId())) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c出品者は自分のオークションに入札できません。"
                ));

                return true;
            }


            if (player.getUniqueId().equals(auction.getHighestBidder())) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c既に最高入札者です。 &7(現在: &6" +
                                String.format("%,d", auction.getCurrentPrice()) +
                                "円&7)"
                ));

                return true;
            }

// 金額指定あり
            long newPrice;

            boolean firstBid = auction.getHighestBidder() == null;

            if (args.length >= 2) {

                try {
                    newPrice = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("金額が不正です");
                    return true;
                }

            } else {

                // /kac bid
                if (firstBid) {
                    newPrice = auction.getStartPrice();
                } else {
                    newPrice = auction.getCurrentPrice() + auction.getBidUnit();
                }
            }

            long bidUnit = auction.getBidUnit();
            long currentPrice = auction.getCurrentPrice();

// 最低入札額
            long minimumPrice;

            if (firstBid) {
                minimumPrice = auction.getStartPrice();
            } else {
                minimumPrice = currentPrice + bidUnit;
            }

// 最低入札額チェック
            if (newPrice < minimumPrice) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c最低入札額は &6" +
                                String.format("%,d", minimumPrice) +
                                "円&cです。"
                ));

                return true;
            }

// 入札単位チェック
            if (!firstBid) {

                long diff = newPrice - currentPrice;

                if (diff % bidUnit != 0) {

                    player.sendMessage(color(
                            ChatUtil.PREFIX +
                                    "&c入札額は &6" +
                                    String.format("%,d", bidUnit) +
                                    "円&c単位で入力してください。"
                    ));

                    return true;
                }
            }


            Long autoLimit =
                    manager.getAutoBids()
                            .get(player.getUniqueId());

            if (autoLimit != null) {

                // 自動入札額未満は禁止
                if (newPrice < autoLimit) {

                    player.sendMessage(color(
                            ChatUtil.PREFIX +
                                    "&c自動入札上限(&6" +
                                    String.format("%,d", autoLimit) +
                                    "円&c)以下では入札できません。"
                    ));

                    return true;
                }

                // 上限以上なら手動入札を優先
                manager.removeAutoBid(
                        player.getUniqueId()
                );

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&e自動入札を解除しました。"
                ));
            }

            double money = VaultManager.getEconomy().getBalance(player);

            if (money < newPrice) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c所持金が不足しています。"
                ));

                return true;
            } else {

                // 普通の入札
                auction.getHighestOffers().put(
                        player.getUniqueId(),
                        newPrice
                );


                manager.recalculateWinner(auction);

                auction.setLastAutoBid(false);
                auction.setLastBidTime(System.currentTimeMillis());

                KurosioAuctionSystem.getInstance().saveAuctions();
            }


            auction.setLastBidTime(System.currentTimeMillis());


            boolean autoBidTriggered =
                    processAutoBids(manager, auction);

            ChatUtil.send(
                    player,
                    ChatUtil.PREFIX +
                            "&a入札しました！ &6&l" +
                            String.format("%,d", auction.getCurrentPrice()) +
                            "円"
            );

            manager.notifyUpdate();

            if (!autoBidTriggered) {

                String autoLabel = "";

                for (UUID uuid : manager.getReceivers(auction)) {

                    Player target = Bukkit.getPlayer(uuid);
                    if (target == null) continue;

                    target.sendMessage(color(
                            "&c現在の入札額: &6" +
                                    String.format("%,d", auction.getCurrentPrice()) +
                                    "円" +
                                    autoLabel
                    ));
                }
            }

            return true;

        }

        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("プレイヤーのみ実行可能");
                return true;
            }

            Player player = (Player) sender;

            String auctionId = args[1];

            AuctionManager manager = KurosioAuctionSystem.getInstance()
                    .getAuctionManager();

            AuctionData auction = manager.getAuction(auctionId);

            if (auction == null) {
                player.sendMessage("そのオークションは存在しません");
                return true;
            }

            if (auction.getExcludedPlayer() != null
                    && auction.getExcludedPlayer().equals(player.getUniqueId())) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&cこのオークションには参加できません。"
                ));

                return true;
            }

            if (!auction.isActive()) {
                player.sendMessage("このオークションは終了しています");
                return true;
            }

            // 1人1参加制限
            if (manager.hasJoined(player.getUniqueId())) {
                player.sendMessage("既に別のオークションに参加しています");
                return true;
            }

            manager.registerJoin(player.getUniqueId(), auctionId);

            player.sendMessage(color(
                    ChatUtil.PREFIX + "&aオークションに参加しました！ &7ID:&f" + auctionId
            ));

            player.sendMessage(color(
                    "&e現在の入札額&f: &6" +
                            String.format("%,d", auction.getCurrentPrice()) +
                            "円"
            ));

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("プレイヤーのみ実行可能");
                return true;
            }

            Player player = (Player) sender;

            AuctionManager manager =
                    KurosioAuctionSystem.getInstance().getAuctionManager();

            String auctionId =
                    manager.getJoinedAuction(player.getUniqueId());

            if (auctionId == null) {
                player.sendMessage("参加中のオークションがありません");
                return true;
            }

            AuctionData auction =
                    manager.getAuction(auctionId);

            if (auction == null) {
                player.sendMessage("オークションが見つかりません");
                return true;
            }

            UUID uuid = player.getUniqueId();

            // =========================
            // 追加：出品者は退出禁止
            // =========================
            if (uuid.equals(auction.getSellerUUID())) {
                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c出品者は退出できません"
                ));
                return true;
            }

            // =========================
            // 最高入札者チェック（そのまま）
            // =========================
            if (uuid.equals(auction.getHighestBidder())) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c最高入札者は退出できません"
                ));
                return true;
            }

            manager.leaveAuction(uuid);

            player.sendMessage(color(
                    ChatUtil.PREFIX + "&cオークションから退出しました"
            ));

            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("autobid")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("プレイヤーのみ実行可能");
                return true;
            }

            Player player = (Player) sender;

            long limit;

            try {
                limit = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("金額が不正です");
                return true;
            }

            if (limit <= 0) {
                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c自動入札上限は1円以上を指定してください。"
                ));
                return true;
            }


            AuctionManager manager = KurosioAuctionSystem.getInstance().getAuctionManager();

            String auctionId = manager.getJoinedAuction(player.getUniqueId());

            if (auctionId == null) {
                player.sendMessage("参加中のオークションがありません");
                return true;
            }



            Long currentAuto =
                    manager.getAutoBids()
                            .get(player.getUniqueId());

            if (currentAuto != null
                    && limit < currentAuto) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c現在の自動入札上限より低い金額には変更できません。"
                ));

                return true;
            }

            AuctionData auction =
                    manager.getAuction(auctionId);

            if (auction == null) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&cオークション情報が見つかりません。"
                ));

                return true;
            }

            if (auction.getExcludedPlayer() != null
                    && auction.getExcludedPlayer().equals(player.getUniqueId())) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&cこのオークションでは自動入札できません。"
                ));

                return true;
            }

            // =========================
// 出品者は自動入札不可
// =========================
            if (auction.getSellerUUID().equals(player.getUniqueId())) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c出品者は自分のオークションに自動入札できません。"
                ));

                return true;
            }

            if (!auction.isAutoBidEnabled()) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&cこのオークションでは自動入札は使用できません。"
                ));

                return true;
            }

            if (limit < auction.getStartPrice()) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c開始価格未満は設定できません。"
                ));

                return true;
            }

            if (limit % auction.getBidUnit() != 0) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c入札単位に合わせて入力してください。"
                ));

                return true;
            }

            double money = VaultManager.getEconomy().getBalance(player);

            if (limit > money) {

                player.sendMessage(color(
                        ChatUtil.PREFIX +
                                "&c所持金を超える自動入札上限は設定できません。"
                ));

                return true;
            }

            manager.setAutoBid(
                    player.getUniqueId(),
                    limit
            );


// 自分が最高入札者でなければ即時自動入札を試行
            processAutoBids(
                    manager,
                    auction
            );

            manager.notifyUpdate();

            player.sendMessage(color(
                    ChatUtil.PREFIX +
                            "&a自動入札を設定しました！ &6上限&f:&6" +
                            String.format("%,d", limit) +
                            "円"
            ));

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("プレイヤーのみ実行可能");
                return true;
            }

            Player player = (Player) sender;

            AuctionManager manager =
                    KurosioAuctionSystem.getInstance().getAuctionManager();

            manager.notifyUpdate();

            String auctionId =
                    manager.getSellerAuction(player.getUniqueId());

            if (auctionId == null) {
                player.sendMessage("出品中のオークションはありません");
                return true;
            }

            AuctionData auction = manager.getAuction(auctionId);

            if (auction == null) {
                player.sendMessage("オークションが見つかりません");
                return true;
            }

            // アイテム返却・送信

            // 参加者退出
            KurosioAuctionSystem.getInstance()
                    .cancelAuction(
                            auction,
                            "出品者によって中止されました"
                    );

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {

            ChatUtil.send(sender, ChatUtil.PREFIX);
            ChatUtil.send(sender, "&e=======[&6&lコマンド一覧&e]=======");
            ChatUtil.send(sender, "&a/kac start <開始価格> [&cu:&a<入札単位>] [&cr:&a<半径>] [&cauto:&a<on/off>] [&cexp:&a<MCID>]");
            ChatUtil.send(sender, "&6&lオークションを開始");
            ChatUtil.send(sender, "&f入札単位・半径は&3任意&f。任意の半径内にｵｰｸｼｮﾝ開始を通知させます。");
            ChatUtil.send(sender, "&aauto:on&fにすると自動入札機能が使えるように設定されます。ﾃﾞﾌｫﾙﾄ:off");
            ChatUtil.send(sender, "&aexp:<MCID> &fを指定するとそのプレイヤーは参加・入札・自動入札できません。");
            ChatUtil.send(sender, "&a/kac join <ID>  &f-オークションへ参加");
            ChatUtil.send(sender, "開始通知の&3&lメッセージクリック&fでも参加できます。");
            ChatUtil.send(sender, "&a/kac leave      &f-オークションから退出");
            ChatUtil.send(sender, "&f※最高入札者の場合退出しても落札者となります。");
            ChatUtil.send(sender, "&a/kac bid        &f-入札単位分入札します。");
            ChatUtil.send(sender, "&a/kac bid [金額]  &f-入札します。");
            ChatUtil.send(sender, "&f※金額の誤入力に注意");
            ChatUtil.send(sender, "&a/kac autobid <上限額> &f-自動入札を設定します。");
            ChatUtil.send(sender, "&a/kac exlist     &f-参加・出品中のオークション情報を表示");
            ChatUtil.send(sender, "&a/kac cancel     &f-出品中オークションを中止 &c出品者のみ");
            ChatUtil.send(sender, "&e========================");

            return true;
        }
        return true;
    }

    private boolean processAutoBids(
            AuctionManager manager,
            AuctionData auction
    ) {

        if (auction == null || !auction.isActive()) {
            return false;
        }

        String auctionId = auction.getAuctionId();
        long bidUnit = auction.getBidUnit();

        Map<UUID, Long> limits = new HashMap<>();

// =========================
// 現在の最高入札者
// =========================
        UUID currentWinner = auction.getHighestBidder();

        if (currentWinner != null) {

            Long winnerAuto = manager.getAutoBids().get(currentWinner);

            if (winnerAuto != null) {
                limits.put(currentWinner, winnerAuto);
            } else {
                limits.put(currentWinner, auction.getCurrentPrice());
            }
        }

        // =========================
// 自動入札者追加
// =========================
        for (Map.Entry<UUID, Long> entry : manager.getAutoBids().entrySet()) {

            UUID uuid = entry.getKey();

            if (!auctionId.equals(manager.getJoinedAuction(uuid))) {
                continue;
            }

            // =========================
            // 所持金チェック
            // =========================
            double balance = VaultManager.getEconomy()
                    .getBalance(Bukkit.getOfflinePlayer(uuid));

            if (balance < entry.getValue()) {

                manager.removeAutoBid(uuid);
                auction.removeBidder(uuid);
                manager.recalculateWinner(auction);

                Player online = Bukkit.getPlayer(uuid);

                if (online != null) {
                    online.sendMessage(color(
                            ChatUtil.PREFIX +
                                    "&e所持金不足のため自動入札を解除しました。"
                    ));
                }

                continue;
            }

            limits.put(uuid, entry.getValue());
        }

        // =========================
        // 単独処理
        // =========================
        if (limits.size() == 1) {

            UUID only = limits.keySet().iterator().next();

            if (auction.getHighestBidder() == null) {

                auction.setCurrentPrice(
                        auction.getStartPrice()
                );

                auction.setHighestBidder(only);


                auction.setLastAutoBid(true);

                auction.setLastBidTime(
                        System.currentTimeMillis()
                );

                notifyAutoUpdate(
                        manager,
                        auction,
                        auction.getStartPrice(),
                        only
                );

                KurosioAuctionSystem.getInstance()
                        .saveAuctions();

                return true;
            }

            return false;
        }

        // =========================
// ソート
// =========================
        List<Map.Entry<UUID, Long>> sorted =
                new ArrayList<>(limits.entrySet());

        sorted.sort((a, b) -> {

            // まず上限額で比較
            int limitCompare =
                    Long.compare(
                            b.getValue(),
                            a.getValue()
                    );

            if (limitCompare != 0) {
                return limitCompare;
            }

            // 同額なら設定時刻が早い方を優先
            Long aTime =
                    manager.getAutoBidTime(
                            a.getKey()
                    );

            Long bTime =
                    manager.getAutoBidTime(
                            b.getKey()
                    );

            if (aTime == null) aTime = Long.MAX_VALUE;
            if (bTime == null) bTime = Long.MAX_VALUE;

            return Long.compare(
                    aTime,
                    bTime
            );
        });

        long beforePrice = auction.getCurrentPrice();
        UUID beforeWinner = auction.getHighestBidder();

// 上限だけ更新
        auction.getHighestOffers().put(
                sorted.get(0).getKey(),
                sorted.get(0).getValue()
        );

// ランキングから現在価格・勝者を再計算
        manager.recalculateWinner(auction);

        if (beforePrice == auction.getCurrentPrice()
                && Objects.equals(beforeWinner, auction.getHighestBidder())) {
            return false;
        }

        auction.setLastAutoBid(true);
        auction.setLastBidTime(System.currentTimeMillis());

        KurosioAuctionSystem.getInstance().saveAuctions();

        boolean autoTriggered = true;
        UUID topUser = auction.getHighestBidder();

        // =========================
        //  通知
        // =========================
        Set<UUID> receivers = new HashSet<>();

        receivers.addAll(manager.getAllJoinedPlayers(auctionId));
        receivers.add(auction.getSellerUUID());

        for (UUID uuid : receivers) {

            Player target = Bukkit.getPlayer(uuid);
            if (target == null) continue;

            target.sendMessage(color(
                            "&c現在の入札額: &6" +
                                    String.format("%,d", auction.getCurrentPrice()) +
                            "円" +
                            (autoTriggered ? " &7（自動入札）" : "")
            ));
        }

        // 勝者通知
        Player winner = Bukkit.getPlayer(topUser);

        if (winner != null) {
            winner.sendMessage(color(
                    ChatUtil.PREFIX +
                            "&aあなたの入札が更新されました！ &6" +
                            String.format("%,d", auction.getCurrentPrice()) +
                            "円"
            ));
        }

        return true;
    }

    private void notifyAutoUpdate(
            AuctionManager manager,
            AuctionData auction,
            long price,
            UUID winner
    ) {

        Player p = Bukkit.getPlayer(winner);

        if (p != null) {
            p.sendMessage(color(
                    ChatUtil.PREFIX +
                            "&aあなたの自動入札が反映されました！ &6" +
                            String.format("%,d", price) + "円"
            ));
        }

        String autoLabel = auction.isLastAutoBid() ? " &7（自動入札）" : "";

        for (UUID uuid : manager.getAllJoinedPlayers(auction.getAuctionId())) {

            Player target = Bukkit.getPlayer(uuid);
            if (target == null) continue;

            target.sendMessage(color(
                    "&c現在の入札額: &6" +
                            String.format("%,d", auction.getCurrentPrice()) +
                            "円" +
                            autoLabel
            ));
        }
    }

    public static String buildItemHover(
            ItemStack item
    ) {

        StringBuilder sb =
                new StringBuilder();

        ItemMeta meta =
                item.getItemMeta();

        // Lore表示
        if (meta != null && meta.hasLore()) {

            for (String line : meta.getLore()) {

                sb.append(
                        color(line)
                ).append("\n");
            }
        }

        // シュルカー中身表示
        if (meta instanceof BlockStateMeta) {

            BlockStateMeta blockMeta =
                    (BlockStateMeta) meta;

            if (blockMeta.getBlockState() instanceof ShulkerBox) {

                ShulkerBox shulker =
                        (ShulkerBox) blockMeta.getBlockState();

                Inventory inv =
                        shulker.getInventory();

                boolean foundItem = false;

                for (ItemStack content : inv.getContents()) {

                    if (content == null) {
                        continue;
                    }

                    if (!foundItem) {

                        if (sb.length() > 0) {
                            sb.append("\n");
                        }

                        sb.append("§e──── 内容物 ────\n");

                        foundItem = true;
                    }

                    ItemMeta contentMeta =
                            content.getItemMeta();

                    String name;

                    if (contentMeta != null
                            && contentMeta.hasDisplayName()) {

                        name = color(
                                contentMeta.getDisplayName()
                        );

                    } else {

                        name = content.getType().name();
                    }

                    sb.append(name)
                            .append(" §7×")
                            .append(content.getAmount())
                            .append("\n");
                }
            }
        }

        return sb.toString().trim();
    }
}
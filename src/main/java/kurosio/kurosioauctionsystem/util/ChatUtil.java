package kurosio.kurosioauctionsystem.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ChatUtil {

    public static final String PREFIX =
            "&6&l[ＫＡＣオークション] ";

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void send(CommandSender sender, String text) {
        sender.sendMessage(color(text));
    }

    public static TextComponent createItemHoverText(ItemStack item) {

        ItemMeta meta = item.getItemMeta();

        //  表示名
        String name = (meta != null && meta.hasDisplayName())
                ? meta.getDisplayName()
                : item.getType().name();

        TextComponent text = new TextComponent("§e§l" + name);

        //  Lore作成
        StringBuilder lore = new StringBuilder();

        if (meta != null && meta.hasLore()) {
            for (String line : meta.getLore()) {
                lore.append(line).append("\n");
            }
        } else {
            lore.append("No lore");
        }

        //  ホバー設定
        text.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(lore.toString()).create()
        ));

        return text;
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

                        sb.append("§7──── 内容物 ────\n");

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
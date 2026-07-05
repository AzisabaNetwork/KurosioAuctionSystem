package kurosio.kurosioauctionsystem.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Map;

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

        // エンチャント表示
        if (meta != null && !meta.getEnchants().isEmpty()) {

            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {

                sb.append(color("&9"))
                        .append(getEnchantName(entry.getKey()))
                        .append(" ")
                        .append(toRoman(entry.getValue()))
                        .append("\n");
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

    public static String getEnchantName(Enchantment enchant) {
        switch (enchant.getKey().getKey()) {
            case "sharpness": return "ダメージ増加";
            case "unbreaking": return "耐久力";
            case "mending": return "修繕";
            case "efficiency": return "効率強化";
            case "fortune": return "幸運";
            case "silk_touch": return "シルクタッチ";
            case "protection": return "ダメージ軽減";
            case "fire_protection": return "火炎耐性";
            case "blast_protection": return "爆発耐性";
            case "projectile_protection": return "飛び道具耐性";
            case "feather_falling": return "落下耐性";
            case "respiration": return "水中呼吸";
            case "aqua_affinity": return "水中採掘";
            case "thorns": return "棘の鎧";
            case "depth_strider": return "水中歩行";
            case "frost_walker": return "氷渡り";
            case "binding_curse": return "束縛の呪い";
            case "vanishing_curse": return "消滅の呪い";
            case "power": return "射撃ダメージ増加";
            case "punch": return "パンチ";
            case "flame": return "火矢";
            case "infinity": return "無限";
            case "looting": return "ドロップ増加";
            case "smite": return "アンデッド特効";
            case "bane_of_arthropods": return "虫特効";
            case "knockback": return "ノックバック";
            case "fire_aspect": return "火属性";
            case "sweeping": return "範囲ダメージ増加";
            case "luck_of_the_sea": return "宝釣り";
            case "lure": return "入れ食い";
            case "impaling": return "串刺し";
            case "loyalty": return "忠誠";
            case "channeling": return "召雷";
            case "riptide": return "激流";
            case "multishot": return "拡散";
            case "piercing": return "貫通";
            case "quick_charge": return "高速装填";
            default: return enchant.getKey().getKey();
        }
    }

    public static String toRoman(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(level);
        }
    }

}
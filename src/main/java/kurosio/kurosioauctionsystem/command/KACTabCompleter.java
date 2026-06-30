package kurosio.kurosioauctionsystem.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KACTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {

        List<String> list = new ArrayList<>();

        if (args.length == 1) {

            list.add("start");
            list.add("join");
            list.add("leave");
            list.add("bid");
            list.add("autobid");
            list.add("exlist");
            list.add("cancel");
            list.add("help");

            return list;
        }

        if (args[0].equalsIgnoreCase("start")) {

            // 開始価格
            if (args.length == 2) {
                return list; // 数値なので補完しない
            }

            Set<String> used = new HashSet<>();

            for (int i = 2; i < args.length - 1; i++) {
                String arg = args[i].toLowerCase();

                if (arg.startsWith("u:")) used.add("u:");
                else if (arg.startsWith("r:")) used.add("r:");
                else if (arg.startsWith("auto:")) used.add("auto:");
                else if (arg.startsWith("exp:")) used.add("exp:");
            }

            String current = args[args.length - 1];

            if (current.startsWith("auto:")) {
                if ("auto:".startsWith(current)) {
                    list.add("auto:on");
                    list.add("auto:off");
                } else {
                    if ("on".startsWith(current.substring(5).toLowerCase()))
                        list.add("auto:on");
                    if ("off".startsWith(current.substring(5).toLowerCase()))
                        list.add("auto:off");
                }
                return list;
            }

            if (current.toLowerCase().startsWith("exp:")) {

                String prefix = current.length() > 4
                        ? current.substring(4).toLowerCase()
                        : "";

                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getName().toLowerCase().startsWith(prefix)) {
                        list.add("exp:" + player.getName());
                    }
                });

                return list;
            }

            if (!used.contains("u:")) list.add("u:");
            if (!used.contains("r:")) list.add("r:");
            if (!used.contains("auto:")) list.add("auto:");
            if (!used.contains("exp:")) list.add("exp:");

            return list;
        }


        return list;
    }
}
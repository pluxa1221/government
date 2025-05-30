package org.government.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.government.Government;
import org.government.utils.Organization;

public class DepartmentChatCommand implements CommandExecutor {
    private final Government plugin;

    public DepartmentChatCommand(Government plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cИспользуйте: /radio <сообщение>");
            return true;
        }

        Organization org = plugin.getPlayerOrganization(player.getName());
        if (org == null) {
            player.sendMessage("§cВы не состоите в организации!");
            return true;
        }

        int rank = org.getRank(player.getName());
        if (rank < 8) {
            player.sendMessage("§cПисать в рацию могут только ранги 8-10!");
            return true;
        }

        String message = String.join(" ", args);
        String orgName = org.getName();
        String rankName = org.getRankName(rank);

        String radioMessage = String.format("§b[РАЦИЯ] §6[%s] §e[%s] §f%s: §r%s", orgName, rankName, player.getName(), message);

        // Отправить только игрокам, состоящим в любой организации
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getPlayerOrganization(p.getName()) != null) {
                p.sendMessage(radioMessage);
            }
        }
        return true;
    }
}
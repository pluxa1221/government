package org.government.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.government.Government;
import org.government.utils.Organization;

public class OrganizationChatCommand implements CommandExecutor {
    private final Government plugin;

    public OrganizationChatCommand(Government plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(plugin.getMessage("help.chat.organization"));
            return true;
        }

        Organization org = plugin.getPlayerOrganization(player.getName());
        if (org == null) {
            player.sendMessage(plugin.getMessage("organization.not_member"));
            return true;
        }

        String message = String.join(" ", args);
        String orgName = org.getName();
        String rankName = org.getRankName(org.getRank(player.getName()));

        String radioMessage = String.format("§a[РАЦИЯ] §6[%s] §e[%s] §f%s: §r%s", orgName, rankName, player.getName(), message);

        // Отправить только членам этой организации
        for (Player p : Bukkit.getOnlinePlayers()) {
            Organization otherOrg = plugin.getPlayerOrganization(p.getName());
            if (otherOrg != null && otherOrg.getName().equalsIgnoreCase(orgName)) {
                p.sendMessage(radioMessage);
            }
        }
        return true;
    }
}
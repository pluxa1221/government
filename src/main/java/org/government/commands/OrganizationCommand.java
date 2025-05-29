package org.government.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.government.Government;
import org.government.utils.Organization;

public class OrganizationCommand implements CommandExecutor {
    public final Government plugin;
    public OrganizationCommand(Government plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Используйте /org <create|invite|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("Используйте: /org create <название>");
                    return true;
                }
                if (plugin.getOrganization(player.getName()) != null) {
                    player.sendMessage("Вы уже состоите в организации.");
                    return true;
                }
                Organization org = new Organization(args[1], player.getName());
                plugin.organizations.put(org.getName(), org);
                plugin.saveOrganizations();
                player.sendMessage("Организация создана: " + org.getName());
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage("Используйте: /org invite <ник>");
                    return true;
                }
                Organization myOrg = plugin.getOrganization(player.getName());
                if (myOrg == null || !myOrg.getLeader().equals(player.getName())) {
                    player.sendMessage("Только лидер может приглашать игроков.");
                    return true;
                }
                myOrg.addMember(args[1]);
                plugin.saveOrganizations();
                player.sendMessage("Игрок " + args[1] + " добавлен в организацию " + myOrg.getName());
                break;
            case "list":
                player.sendMessage("Список организаций: " + plugin.organizations.keySet());
                break;
            default:
                player.sendMessage("Неизвестная команда.");
                break;
        }
        return true;
    }
}
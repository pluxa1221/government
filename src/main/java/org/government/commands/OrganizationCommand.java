package org.government.commands;

import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.government.Government;
import org.government.utils.Organization;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class OrganizationCommand implements CommandExecutor {
    public final Government plugin;
    public OrganizationCommand(Government plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender instanceof Player) {}
        else return false;

        Player player = (Player) sender;
        Organization org = plugin.getOrganization(player.getName());

        if (org != null) {
            player.sendMessage("Вы находитесь в организации " + org.getName());
        } else {
            player.sendMessage("Вы не находитесь в какой-либо организации.");
        }

        if (args[0] != null) {
            switch (args[0]) {
                case "create":
                    sender.sendMessage("Организация создана: " + createOrganization(args[1], sender.getName()).getName());
                case "invite":
                    if (org != null) {
                        org.addMember(args[1]);
                        sender.sendMessage("Игрок " + args[1] + " добавлен в организацию " + org.getName());
                        plugin.getServer().getPlayer(args[1]).sendMessage("Лидер организации " + org.getName() + " добавил вас");
                    } else {
                        sender.sendMessage("Вы не находитесь в какой-либо организации.");
                    }
                case "leader":
                    switch (args[1]) {
                        case "up":
                            org.upRank(args[2]);
                            sender.sendMessage("Ранг игрока " + args[2] + "успешно повышен");
                            plugin.getServer().getPlayer(args[2]).sendMessage("Лидер организации повысил ваш ранг");
                        case "invite":
                            if (org != null) {
                                org.addMember(args[1]);
                                sender.sendMessage("Игрок " + args[2] + " добавлен в организацию " + org.getName());
                                plugin.getServer().getPlayer(args[2]).sendMessage("Лидер организации " + org.getName() + " добавил вас");
                            } else {
                                sender.sendMessage("Вы не находитесь в какой-либо организации.");
                            }
                    }
                case "list":
                    sender.sendMessage("В данный момент существуют такие организации как:");
                    sender.sendMessage(plugin.organizations.keySet().toString());
            }

            plugin.saveOrganizations();
        }

        return true;
    }

    public Organization createOrganization(String name, String player) {
        // Create a new organization with the player as its leader
        Organization org = new Organization(name, player);
        plugin.organizations.put(org.getName(), org);

        // Add the player to the organization's members list
        org.getMembers().add(player);

        return org;

        // Save the data storage to file
//        saveOrganizations();
    }
}
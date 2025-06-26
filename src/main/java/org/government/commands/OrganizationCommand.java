package org.government.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.government.Government;
import org.government.integrations.LuckPermsIntegration;
import org.government.utils.Organization;

public class OrganizationCommand implements CommandExecutor {
    private final Government plugin;
    private final LuckPermsIntegration lpIntegration;

    public OrganizationCommand(Government plugin, LuckPermsIntegration lpIntegration) {
        this.plugin = plugin;
        this.lpIntegration = lpIntegration;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(plugin.getMessage("help.org.general"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessage("help.org.create"));
                    return true;
                }
                String orgName = args[1];
                if (plugin.getOrganization(orgName) != null) {
                    player.sendMessage(plugin.getMessage("organization.exists"));
                    return true;
                }
                if (plugin.getPlayerOrganization(player.getName()) != null) {
                    player.sendMessage(plugin.getMessage("organization.already_member"));
                    return true;
                }
                Organization newOrg = new Organization(orgName, player.getName(), plugin.getDefaultRanks());
                plugin.addOrganization(newOrg);
                lpIntegration.setOrganizationGroup(player, orgName, 10); // 10 — лидер
                player.sendMessage(plugin.getMessage("organization.created").replace("{org}", orgName));
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessage("help.org.invite"));
                    return true;
                }
                Organization org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage(plugin.getMessage("organization.not_member"));
                    return true;
                }
                if (!org.isLeader(player.getName())) {
                    player.sendMessage(plugin.getMessage("organization.not_leader"));
                    return true;
                }
                Player invited = Bukkit.getPlayer(args[1]);
                if (invited == null) {
                    player.sendMessage(plugin.getMessage("organization.player_not_found").replace("{player}", args[1]));
                    return true;
                }
                if (plugin.getPlayerOrganization(invited.getName()) != null) {
                    player.sendMessage(plugin.getMessage("organization.player_already_member").replace("{player}", invited.getName()));
                    return true;
                }
                org.addMember(invited.getName(), 1);
                plugin.updatePlayerOrg(invited.getName(), org.getName());
                lpIntegration.setOrganizationGroup(invited, org.getName(), 1);
                invited.sendMessage(plugin.getMessage("organization.invite_received").replace("{org}", org.getName()));
                player.sendMessage(plugin.getMessage("organization.player_joined"));
                break;

            case "promote":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessage("help.org.promote"));
                    return true;
                }
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage(plugin.getMessage("organization.not_member"));
                    return true;
                }
                if (!org.canPromote(player.getName())) {
                    player.sendMessage(plugin.getMessage("no_permission"));
                    return true;
                }
                String promotee = args[1];
                if (!org.hasMember(promotee)) {
                    player.sendMessage(plugin.getMessage("organization.player_not_in_your_org").replace("{player}", promotee));
                    return true;
                }
                int currentRank = org.getRank(promotee);
                if (currentRank >= 10) {
                    player.sendMessage(plugin.getMessage("organization.player_has_max_rank").replace("{player}", promotee));
                    return true;
                }
                org.setRank(promotee, currentRank + 1);
                Player promoteePlayer = Bukkit.getPlayer(promotee);
                if (promoteePlayer != null) {
                    lpIntegration.setOrganizationGroup(promoteePlayer, org.getName(), currentRank + 1);
                    promoteePlayer.sendMessage(plugin.getMessage("organization.player_promoted").replace("{rank}", String.valueOf(currentRank + 1)));
                }
                player.sendMessage(plugin.getMessage("organization.promoted").replace("{player}", promotee).replace("{rank}", String.valueOf(currentRank + 1)));
                break;

            case "demote":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessage("help.org.demote"));
                    return true;
                }
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage(plugin.getMessage("organization.not_member"));
                    return true;
                }
                if (!org.canDemote(player.getName())) {
                    player.sendMessage(plugin.getMessage("no_permission"));
                    return true;
                }
                String demotee = args[1];
                if (!org.hasMember(demotee)) {
                    player.sendMessage(plugin.getMessage("organization.player_not_in_your_org").replace("{player}", demotee));
                    return true;
                }
                currentRank = org.getRank(demotee);
                if (currentRank <= 1) {
                    player.sendMessage(plugin.getMessage("organization.player_has_min_rank").replace("{player}", demotee));
                    return true;
                }
                org.setRank(demotee, currentRank - 1);
                player.sendMessage(plugin.getMessage("organization.demoted").replace("{player}", demotee).replace("{rank}", String.valueOf(currentRank - 1)));
                Player demoteePlayer = Bukkit.getPlayer(demotee);
                if (demoteePlayer != null) {
                    demoteePlayer.sendMessage(plugin.getMessage("organization.player_demoted").replace("{rank}", String.valueOf(currentRank - 1)));
                }
                break;

            case "setrankname":
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessage("help.org.setrankname"));
                    return true;
                }
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage(plugin.getMessage("organization.not_member"));
                    return true;
                }
                if (!org.isLeader(player.getName())) {
                    player.sendMessage(plugin.getMessage("organization.not_leader"));
                    return true;
                }
                int rankNum;
                try {
                    rankNum = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getMessage("errors.invalid_rank_format"));
                    return true;
                }
                if (rankNum < 1 || rankNum > 10) {
                    player.sendMessage(plugin.getMessage("errors.invalid_rank_number").replace("{min}", "1").replace("{max}", "10"));
                    return true;
                }
                String newName = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
                org.setRankName(rankNum, newName);
                player.sendMessage(plugin.getMessage("organization.rank_name_set").replace("{rank}", String.valueOf(rankNum)).replace("{name}", newName));
                break;

            case "info":
                org = plugin.getPlayerOrganization(player.getName());

                if (org == null) {
                    player.sendMessage(plugin.getMessage("organization.not_member"));
                    return true;
                }

                player.sendMessage("§e--- Информация об организации ---");
                player.sendMessage("§7Название: §f" + org.getName());
                player.sendMessage("§7Лидер: §f" + org.getLeader());
                player.sendMessage("§7Участники:");

                for (String member : org.getMembers()) {
                    int mRank = org.getRank(member);
                    player.sendMessage(" §f" + member + " §7[§a" + mRank + "§7 - " + org.getRankName(mRank) + "§7]");
                }

                player.sendMessage("§e--------------------------------");

                break;

            case "list":
                player.sendMessage("§e--- Список организаций ---");
                for (Organization o : plugin.getOrganizations()) {
                    player.sendMessage("§f" + o.getName() + " §7(Лидер: §a" + o.getLeader() + "§7, Участников: §b" + o.getMembers().size() + "§7)");
                }
                break;

            case "leave":
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage(plugin.getMessage("organization.not_member"));
                    return true;
                }
                if (org.isLeader(player.getName())) {
                    player.sendMessage(plugin.getMessage("organization.leader_cannot_leave"));
                    return true;
                }
                org.removeMember(player.getName());
                lpIntegration.removeOrganizationGroups(player);
                player.sendMessage(plugin.getMessage("organization.left").replace("{org}", org.getName()));
                break;

            case "remove":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessage("help.org.remove"));
                    return true;
                }
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage("§cВы не состоите в организации!");
                    return true;
                }
                if (!org.isLeader(player.getName())) {
                    player.sendMessage("§cТолько лидер может исключать участников.");
                    return true;
                }
                String removee = args[1];
                if (!org.hasMember(removee)) {
                    player.sendMessage("§cЭтот игрок не состоит в вашей организации.");
                    return true;
                }
                if (org.isLeader(removee)) {
                    player.sendMessage("§cНельзя удалить лидера!");
                    return true;
                }
                org.removeMember(removee);
                Player removeePlayer = Bukkit.getPlayer(removee);
                if (removeePlayer != null) {
                    lpIntegration.removeOrganizationGroups(removeePlayer);
                    removeePlayer.sendMessage(plugin.getMessage("organization.removed").replace("{org}", org.getName()));
                }
                player.sendMessage(plugin.getMessage("organization.removed").replace("{player}", removee));
                break;

            default:
                player.sendMessage(plugin.getMessage("help.org.unknown_command"));
        }
        return true;
    }
}
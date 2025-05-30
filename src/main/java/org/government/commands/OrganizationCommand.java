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
            sender.sendMessage("§cКоманду может использовать только игрок.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§eИспользуйте: /org <create|invite|promote|demote|setrankname|info|list|leave|remove> ...");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cИспользуйте: /org create <название>");
                    return true;
                }
                String orgName = args[1];
                if (plugin.getOrganization(orgName) != null) {
                    player.sendMessage("§cОрганизация с таким названием уже существует!");
                    return true;
                }
                if (plugin.getPlayerOrganization(player.getName()) != null) {
                    player.sendMessage("§cВы уже состоите в организации!");
                    return true;
                }
                Organization newOrg = new Organization(orgName, player.getName(), plugin.getDefaultRanks());
                plugin.addOrganization(newOrg);
                lpIntegration.setOrganizationGroup(player, orgName, 10); // 10 — лидер
                player.sendMessage("§aОрганизация " + orgName + " успешно создана! Вы назначены лидером.");
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cИспользуйте: /org invite <ник>");
                    return true;
                }
                Organization org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage("§cВы не состоите в организации!");
                    return true;
                }
                if (!org.isLeader(player.getName())) {
                    player.sendMessage("§cТолько лидер может приглашать игроков.");
                    return true;
                }
                Player invited = Bukkit.getPlayer(args[1]);
                if (invited == null) {
                    player.sendMessage("§cИгрок не найден.");
                    return true;
                }
                if (plugin.getPlayerOrganization(invited.getName()) != null) {
                    player.sendMessage("§cЭтот игрок уже состоит в организации.");
                    return true;
                }
                org.addMember(invited.getName(), 1);
                plugin.updatePlayerOrg(invited.getName(), org.getName());
                lpIntegration.setOrganizationGroup(invited, org.getName(), 1);
                invited.sendMessage("§aВы были приглашены в организацию: §e" + org.getName());
                player.sendMessage("§aИгрок " + invited.getName() + " добавлен в организацию.");
                break;

            case "promote":
                if (args.length < 2) {
                    player.sendMessage("§cИспользуйте: /org promote <ник>");
                    return true;
                }
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage("§cВы не состоите в организации!");
                    return true;
                }
                if (!org.canPromote(player.getName())) {
                    player.sendMessage("§cНедостаточно прав для повышения.");
                    return true;
                }
                String promotee = args[1];
                if (!org.hasMember(promotee)) {
                    player.sendMessage("§cЭтот игрок не состоит в вашей организации.");
                    return true;
                }
                int currentRank = org.getRank(promotee);
                if (currentRank >= 10) {
                    player.sendMessage("§eЭтот игрок уже максимального ранга.");
                    return true;
                }
                org.setRank(promotee, currentRank + 1);
                Player promoteePlayer = Bukkit.getPlayer(promotee);
                if (promoteePlayer != null) {
                    lpIntegration.setOrganizationGroup(promoteePlayer, org.getName(), currentRank + 1);
                    promoteePlayer.sendMessage("§aВаш ранг в организации " + org.getName() + " повышен до " + (currentRank + 1) + "!");
                }
                player.sendMessage("§aИгрок " + promotee + " повышен до ранга " + (currentRank + 1));
                break;

            case "demote":
                if (args.length < 2) {
                    player.sendMessage("§cИспользуйте: /org demote <ник>");
                    return true;
                }
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage("§cВы не состоите в организации!");
                    return true;
                }
                if (!org.canDemote(player.getName())) {
                    player.sendMessage("§cНедостаточно прав для понижения.");
                    return true;
                }
                String demotee = args[1];
                if (!org.hasMember(demotee)) {
                    player.sendMessage("§cЭтот игрок не состоит в вашей организации.");
                    return true;
                }
                currentRank = org.getRank(demotee);
                if (currentRank <= 1) {
                    player.sendMessage("§eЭтот игрок уже минимального ранга.");
                    return true;
                }
                org.setRank(demotee, currentRank - 1);
                Player demoteePlayer = Bukkit.getPlayer(demotee);
                if (demoteePlayer != null) {
                    lpIntegration.setOrganizationGroup(demoteePlayer, org.getName(), currentRank - 1);
                    demoteePlayer.sendMessage("§cВаш ранг в организации " + org.getName() + " понижен до " + (currentRank - 1) + "!");
                }
                player.sendMessage("§aИгрок " + demotee + " понижен до ранга " + (currentRank - 1));
                break;

            case "setrankname":
                if (args.length < 3) {
                    player.sendMessage("§cИспользуйте: /org setrankname <номер> <название>");
                    return true;
                }
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage("§cВы не состоите в организации!");
                    return true;
                }
                if (!org.isLeader(player.getName())) {
                    player.sendMessage("§cТолько лидер может изменять названия рангов!");
                    return true;
                }
                int rankNum;
                try {
                    rankNum = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cВведите номер ранга числом!");
                    return true;
                }
                if (rankNum < 1 || rankNum > 10) {
                    player.sendMessage("§cРанг должен быть от 1 до 10.");
                    return true;
                }
                String newName = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
                org.setRankName(rankNum, newName);
                player.sendMessage("§aНазвание ранга " + rankNum + " установлено как: " + newName);
                break;

            case "info":
                org = plugin.getPlayerOrganization(player.getName());
                if (org == null) {
                    player.sendMessage("§cВы не состоите в организации!");
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
                    player.sendMessage("§cВы не состоите в организации!");
                    return true;
                }
                if (org.isLeader(player.getName())) {
                    player.sendMessage("§cЛидер не может покинуть организацию. Передайте лидерство или удалите организацию.");
                    return true;
                }
                org.removeMember(player.getName());
                lpIntegration.removeOrganizationGroups(player);
                player.sendMessage("§aВы вышли из организации " + org.getName());
                break;

            case "remove":
                if (args.length < 2) {
                    player.sendMessage("§cИспользуйте: /org remove <ник>");
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
                    removeePlayer.sendMessage("§cВы были исключены из организации " + org.getName());
                }
                player.sendMessage("§aИгрок " + removee + " исключён из организации.");
                break;

            default:
                player.sendMessage("§cНеизвестная подкоманда. Доступные: create, invite, promote, demote, setrankname, info, list, leave, remove");
        }
        return true;
    }
}
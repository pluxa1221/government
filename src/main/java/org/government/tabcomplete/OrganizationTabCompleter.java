package org.government.tabcomplete;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.government.Government;
import org.government.utils.Organization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrganizationTabCompleter implements TabCompleter {

    private final Government plugin;

    public OrganizationTabCompleter(Government plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList(
                    "create", "delete", "join", "leave", "promote", "demote", "list", "info", "setrank", "setname", "setleader"
            ));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if ("create".equals(sub)) {
                completions.add("<orgName>");
            } else if ("delete".equals(sub) || "info".equals(sub)) {
                for (Organization org : plugin.getOrganizations()) {
                    completions.add(org.getName());
                }
            } else if ("join".equals(sub)) {
                for (Organization org : plugin.getOrganizations()) {
                    completions.add(org.getName());
                }
            } else if ("promote".equals(sub) || "demote".equals(sub) || "setrank".equals(sub)) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    Organization org = plugin.getPlayerOrganization(p.getName());
                    if (org != null) completions.addAll(org.getMembers());
                }
            } else if ("setleader".equals(sub)) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    Organization org = plugin.getPlayerOrganization(p.getName());
                    if (org != null) completions.addAll(org.getMembers());
                }
            } else if ("setname".equals(sub)) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    Organization org = plugin.getPlayerOrganization(p.getName());
                    if (org != null) completions.add("<newName>");
                }
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if ("setrank".equals(sub)) {
                completions.add("1");
                completions.add("2");
                completions.add("3");
                completions.add("4");
                completions.add("5");
                completions.add("6");
                completions.add("7");
                completions.add("8");
                completions.add("9");
                completions.add("10");
            }
        }
        return filter(completions, args.length > 0 ? args[args.length-1] : "");
    }

    private List<String> filter(List<String> options, String arg) {
        String lower = arg.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String s : options) if (s.toLowerCase().startsWith(lower)) out.add(s);
        return out;
    }
}
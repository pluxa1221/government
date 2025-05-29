package org.government.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.government.Government;

import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Organization {
    private final String name;
    private final String leader;
    private final List<String> members;
    private final Government plugin;
    private HashMap<String, Integer> ranks;

    public Organization(String name, String leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.ranks = new HashMap<String, Integer>();
        this.plugin = Government.getInstance();

        setRank(leader, 10);
    }

    public String getName() {
        return name;
    }
    public String getLeader() {
        return leader;
    }
    public Integer getRank(String player) {
        return ranks.get(player);
    }
    public List<String> getMembers() {
        return members;
    }
    public void addMember(String player) {
        members.add(player);
        this.setRank(player, 1);
    }
    public void setRank(String player, Integer rank) {
        this.ranks.put(player, rank);
        plugin.luckperms.setGroup(this.name + "-" + rank, plugin.luckperms.getUser(player));
    }
    public void upRank(String player) {
        this.setRank(player, this.ranks.get(player));
    }
    public void setRanks(HashMap<String, Integer> ranks) {
        this.ranks = ranks;
    }
}

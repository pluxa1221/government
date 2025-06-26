package org.government.utils;

import org.bukkit.entity.Player;
import org.government.integrations.LuckPermsIntegration;

import java.util.*;

public class Organization {
    private String name;
    private String leader;
    private Map<String, Integer> members = new HashMap<>(); // name -> rank
    private Map<Integer, String> rankNames = new HashMap<>();
    private LuckPermsIntegration lp;

    public Organization(String name, String leader, Map<Integer, String> defaultRanks) {
        this.name = name;
        this.leader = leader;
        this.rankNames.putAll(defaultRanks);
        this.members.put(leader, 10); // лидер всегда 10 ранг
        this.lp = new LuckPermsIntegration(); // Инициализация интеграции LuckPerms
    }

    public String getName() { return name; }
    public String getLeader() { return leader; }

    public Set<String> getMembers() { return members.keySet(); }

    public boolean isLeader(String player) {
        return leader.equalsIgnoreCase(player);
    }

    public boolean hasMember(String player) {
        return members.containsKey(player);
    }

    public int getRank(String player) {
        return members.getOrDefault(player, 0);
    }

    public String getRankName(int rank) {
        return rankNames.getOrDefault(rank, "Ранг " + rank);
    }

    public void setRankName(int rank, String name) {
        rankNames.put(rank, name);
    }

    public void addMember(String player, int rank) {
        members.put(player, rank);
    }

    public void removeMember(String player) {
        members.remove(player);
    }

    public void setRank(String player, int rank) {
        if (members.containsKey(player)) {
            members.put(player, rank);
            Player bukkitPlayer = org.government.Government.getInstance().getServer().getPlayer(player);
            if (bukkitPlayer != null) {
                lp.setOrganizationGroup(bukkitPlayer, name, rank);
            }
        }
    }

    // Для проверки прав — можно сделать более сложную логику
    public boolean canPromote(String player) {
        return isLeader(player);
    }

    public boolean canDemote(String player) {
        return isLeader(player);
    }

    // Добавить этот метод для замены всех рангов
    public void setRanks(Map<Integer, String> ranks) {
        if (ranks != null) {
            rankNames.clear();
            rankNames.putAll(ranks);
        }
    }

    // Добавить этот метод для получения локальных названий рангов
    public Map<Integer, String> getLocalRankNames() {
        return new HashMap<>(rankNames);
    }
}
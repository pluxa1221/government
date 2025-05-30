package org.government.utils;

import org.government.Government;

import java.util.*;

public class Organization {
    private final String name;
    private final String leader;
    private final List<String> members;
    private final Map<String, Integer> ranks;
    private final Map<Integer, String> localRankNames; // индивидуальные названия

    public Organization(String name, String leader, Map<Integer, String> defaultRankNames) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.ranks = new HashMap<>();
        this.localRankNames = new HashMap<>(defaultRankNames);

        addMember(leader);
        setRank(leader, 10);
    }

    // --- Работа с участниками и рангами ---
    public void addMember(String player) {
        if (!members.contains(player)) {
            members.add(player);
            setRank(player, 1);
        }
    }

    public void setRank(String player, int rank) {
        ranks.put(player, rank);
    }

    public int getRank(String player) {
        return ranks.getOrDefault(player, 0);
    }

    // --- Работа с названиями рангов ---
    public String getRankName(int rank) {
        return localRankNames.getOrDefault(rank, Government.getInstance().getGlobalRankName(rank));
    }

    public void setRanks(Map<String, Integer> ranks) {
        this.ranks.clear();
        this.ranks.putAll(ranks);
    }

    public void setLocalRankName(int rank, String name) {
        localRankNames.put(rank, name);
    }

    public Map<Integer, String> getLocalRankNames() {
        return localRankNames;
    }

    // --- Прочее ---
    public String getName() { return name; }
    public String getLeader() { return leader; }
    public List<String> getMembers() { return members; }
}
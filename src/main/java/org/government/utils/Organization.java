package org.government.utils;

import java.util.ArrayList;
import java.util.List;

public class Organization {
    private final String name;
    private final String leader;
    private final List<String> members;

    public Organization(String name, String leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
    }

    public String getName() {
        return name;
    }
    public String getLeader() {
        return leader;
    }
    public List<String> getMembers() {
        return members;
    }

    public void addMember(String player) {
        if (!members.contains(player)) {
            members.add(player);
        }
    }
}
package org.government;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.government.commands.OrganizationCommand;
import org.government.utils.Organization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Government extends JavaPlugin {

    // Data storage for organizations
//    private static Government INSTANCE;
    public final HashMap<String, Organization> organizations = new HashMap<>();

    @Override
    public void onEnable() {
//        INSTANCE = this;

        getServer().getPluginManager().registerEvents(new events(this), this);

        getCommand("org").setExecutor(new OrganizationCommand(this));

        // Initialize some data storage (e.g., organizations)
        loadOrganizations();
    }

    @Override
    public void onDisable() {
        saveOrganizations();
    }

    // Method to get an organization by player
    public Organization getOrganization(String player) {
        for (Organization org : organizations.values()) {
            if (org.getMembers().contains(player)) {
                return org;
            }
        }

        return null;
    }



    private void loadOrganizations() {
        // Load the data storage from file (e.g., YAML or JSON)
        File customYml = new File(plugin.getDataFolder() + "/customYmlFile.yml");
        FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);

        // Запись данных в конфиг:
        customConfig.set("player." + player.getName(), 5);

        // Сохранение изменений:
        saveCustomYml(customConfig, customYml);

        if (orgFile.exists()) {
        for (String orgName : orgConfig.getConfigurationSection("organizations").getKeys(false)) {
            String path = "organizations." + orgName + ".";

            String leader = orgConfig.getString(path + "leader");
            List<String> members = new ArrayList<>(orgConfig.getConfigurationSection(path + "members").getKeys(false));
            HashMap<String, Integer> rank = new HashMap<>();

            for (String key : members) {
                rank.put(key, orgConfig.getInt(path + key + ".rank"));
            }

            Organization org = new Organization(orgName, leader);

            for (String member : members) {
                org.addMember(member);
            }

            org.setRanks(rank);

            organizations.put(org.getName(), org);
        }
        }
    }

    public void saveOrganizations() {
        // Save the data storage to file (e.g., YAML or JSON)
        for (Organization org : organizations.values()) {
            String path = "organizations." + org.getName() + ".";

            orgConfig.set(path + "leader", org.getLeader());

            for (String player : org.getMembers()) {
                orgConfig.set(path + "members." + player + ".rank", org.getRank(player));
            }
        }

        try {
            orgConfig.save(orgFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Government getInstance() {
        return INSTANCE;
    }
}

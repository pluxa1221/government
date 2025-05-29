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

    private static Government INSTANCE;
    public final HashMap<String, Organization> organizations = new HashMap<>();
    private File orgFile;
    private FileConfiguration orgConfig;

    @Override
    public void onEnable() {
        INSTANCE = this;
        getServer().getPluginManager().registerEvents(new events(this), this);
        getCommand("org").setExecutor(new OrganizationCommand(this));

        // Prepare file for storage
        orgFile = new File(getDataFolder(), "organizations.yml");
        if (!orgFile.exists()) {
            orgFile.getParentFile().mkdirs();
            saveResource("organizations.yml", false);
        }
        orgConfig = YamlConfiguration.loadConfiguration(orgFile);
        loadOrganizations();
    }

    @Override
    public void onDisable() {
        saveOrganizations();
    }

    public static Government getInstance() {
        return INSTANCE;
    }

    public Organization getOrganization(String player) {
        for (Organization org : organizations.values()) {
            if (org.getMembers().contains(player)) {
                return org;
            }
        }
        return null;
    }

    private void loadOrganizations() {
        organizations.clear();
        if (orgConfig.isConfigurationSection("organizations")) {
            for (String orgName : orgConfig.getConfigurationSection("organizations").getKeys(false)) {
                String path = "organizations." + orgName + ".";
                String leader = orgConfig.getString(path + "leader");
                List<String> members = orgConfig.getStringList(path + "members");
                Organization org = new Organization(orgName, leader);
                org.getMembers().addAll(members);
                organizations.put(org.getName(), org);
            }
        }
    }

    public void saveOrganizations() {
        orgConfig.set("organizations", null); // Reset
        for (Organization org : organizations.values()) {
            String path = "organizations." + org.getName() + ".";
            orgConfig.set(path + "leader", org.getLeader());
            orgConfig.set(path + "members", org.getMembers());
        }
        try {
            orgConfig.save(orgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
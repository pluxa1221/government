package org.government;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.government.commands.OrganizationCommand;
import org.government.utils.Organization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Government extends JavaPlugin {

    private static Government INSTANCE;
    public final HashMap<String, Organization> organizations = new HashMap<>();
    private File orgFile;
    private FileConfiguration orgConfig;
    private Map<Integer, String> globalRankNames = new HashMap<>();

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

        saveDefaultConfig();
        loadGlobalRankNames();
        loadOrganizations();

        startAutoSaveTask();
    }

    private void startAutoSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveOrganizations();
            }
        }.runTaskTimer(this, 6000L, 6000L); // 5 минут в тиках
    }

    public void loadGlobalRankNames() {
        globalRankNames.clear();
        if (getConfig().isConfigurationSection("ranks")) {
            for (String key : getConfig().getConfigurationSection("ranks").getKeys(false)) {
                int rank = Integer.parseInt(key);
                String name = getConfig().getString("ranks." + key);
                globalRankNames.put(rank, name);
            }
        }
    }
    public Map<Integer, String> getGlobalRankNames() {
        return globalRankNames;
    }
    public String getGlobalRankName(int rank) {
        return globalRankNames.getOrDefault(rank, "Неизвестно");
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
        if (orgFile.exists()) {
            for (String orgName : orgConfig.getConfigurationSection("organizations").getKeys(false)) {
                String path = "organizations." + orgName + ".";
                String leader = orgConfig.getString(path + "leader");
                List<String> members = new ArrayList<>(orgConfig.getConfigurationSection(path + "members").getKeys(false));
                HashMap<String, Integer> rank = new HashMap<>();
                for (String key : members) {
                    rank.put(key, orgConfig.getInt(path + "members." + key + ".rank"));
                }

                // Читаем индивидуальные названия рангов (если есть)
                Map<Integer, String> localRanks = new HashMap<>(getGlobalRankNames());
                if (orgConfig.isConfigurationSection(path + "ranks")) {
                    for (String rankKey : orgConfig.getConfigurationSection(path + "ranks").getKeys(false)) {
                        int rankNum = Integer.parseInt(rankKey);
                        String name = orgConfig.getString(path + "ranks." + rankKey);
                        localRanks.put(rankNum, name);
                    }
                }

                Organization org = new Organization(orgName, leader, localRanks);

                for (String member : members) {
                    org.addMember(member);
                }
                org.setRanks(rank);

                organizations.put(org.getName(), org);
            }
        }
    }

    public void saveOrganizations() {
        orgConfig.set("organizations", null); // Reset
        for (Organization org : organizations.values()) {
            String path = "organizations." + org.getName() + ".";
            orgConfig.set(path + "leader", org.getLeader());
            // Сохраняем участников и их ранги
            for (String player : org.getMembers()) {
                orgConfig.set(path + "members." + player + ".rank", org.getRank(player));
            }
            // Сохраняем индивидуальные ранги организации
            Map<Integer, String> localRanks = org.getLocalRankNames();
            for (Map.Entry<Integer, String> entry : localRanks.entrySet()) {
                orgConfig.set(path + "ranks." + entry.getKey(), entry.getValue());
            }
        }
        try {
            orgConfig.save(orgFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
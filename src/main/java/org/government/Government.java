package org.government;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.government.commands.DepartmentChatCommand;
import org.government.commands.OrganizationChatCommand;
import org.government.commands.OrganizationCommand;
import org.government.integrations.LuckPermsIntegration;
import org.government.utils.Organization;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Government extends JavaPlugin {
    private LuckPerms luckPerms;

    private static Government INSTANCE;

    private Map<Integer, String> globalRankNames = new HashMap<>();

    private File orgFile;
    private FileConfiguration orgConfig;

    private Map<String, Organization> organizations = new HashMap<>();
    private Map<String, String> playerOrgs = new HashMap<>(); // playerName -> orgName

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();

        INSTANCE = this;
        getServer().getPluginManager().registerEvents(new events(this), this);

        getCommand("org").setExecutor(new OrganizationCommand(this, new LuckPermsIntegration()));
        getCommand("departmentchat").setExecutor(new DepartmentChatCommand(this));
        getCommand("organizationchat").setExecutor(new OrganizationChatCommand(this));

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

    public LuckPerms getLuckPerms() {
        return luckPerms;
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
                    org.addMember(member, rank.get(member));
                }

                org.setRanks(localRanks);

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

    // Возвращает дефолтные ранги для новой организации
    public Map<Integer, String> getDefaultRanks() {
        Map<Integer, String> ranks = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            ranks.put(i, "Ранг " + i);
        }
        return ranks;
    }

    // Добавляет новую организацию
    public void addOrganization(Organization org) {
        organizations.put(org.getName(), org);
        for (String member : org.getMembers()) {
            playerOrgs.put(member, org.getName());
        }
    }

    // Получает организацию по названию
    public Organization getOrganization(String name) {
        return organizations.get(name);
    }

    // Получает организацию игрока
    public Organization getPlayerOrganization(String player) {
        String org = playerOrgs.get(player);
        if (org == null) return null;
        return organizations.get(org);
    }

    // Для поддержки /org list
    public Collection<Organization> getOrganizations() {
        return organizations.values();
    }

    // Позволяет обновлять playerOrgs при вступлении/выходе
    public void updatePlayerOrg(String player, String orgName) {
        if (orgName == null) playerOrgs.remove(player);
        else playerOrgs.put(player, orgName);
    }
}
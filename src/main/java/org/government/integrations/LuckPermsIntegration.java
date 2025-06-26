package org.government.integrations;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.track.Track;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.regex.Pattern;

public class LuckPermsIntegration {

    private final LuckPerms luckPerms;
    // Группы формата FSB-1, FSB-2 ... FSB-10
    private static final Pattern ORG_GROUP_PATTERN = Pattern.compile("^[a-zA-Z]+-\\d+$");

    public LuckPermsIntegration() {
        this.luckPerms = LuckPermsProvider.get();
    }

    /**
     * Устанавливает организационную группу игроку по треку или вручную.
     * Если трек существует, используется LuckPerms promote/demote.
     * @param player Bukkit Player
     * @param orgNameOriginal Название организации (например: "FSB")
     * @param rank Ранг (от 1 до 10)
     */
    public void setOrganizationGroup(Player player, String orgNameOriginal, int rank) {
        if (player == null || orgNameOriginal == null || rank <= 0) return;
        String org = transliterate(orgNameOriginal).toUpperCase().replaceAll("[^A-Z]", "");
        String groupName = org + "-" + rank;
        UUID uuid = player.getUniqueId();

        System.out.println("Устанавливаем группу " + groupName + " для игрока " + player.getName() + " (UUID: " + uuid + ")");

        // Сначала удаляем все старые орг-группы вручную (на всякий случай)
        removeOrganizationGroups(player);

        // Если трек существует -- назначаем группу по треку.
        Track track = luckPerms.getTrackManager().getTrack(org);
        if (track != null) {
            // Назначаем нужный ранг через track API
            User user = luckPerms.getUserManager().getUser(uuid);
            assert user != null;
            setRankViaTrack(user, track, groupName);
            return;
        }

        // Если трека нет, добавляем группу вручную
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(u -> {
                if (u != null) {
                    addGroupDirectly(u, groupName);
                }
            });
        } else {
            addGroupDirectly(user, groupName);
        }
    }

    /**
     * Удаляет все орг-группы формата ORGNAME-ЧИСЛО у игрока.
     */
    public void removeOrganizationGroups(Player player) {
        if (player == null) return;
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        user.data().clear(node -> isOrganizationGroup(node));
        luckPerms.getUserManager().saveUser(user);
    }

    /**
     * Проверяет, состоит ли игрок в орг-группе формата ORGNAME-ЧИСЛО.
     */
    public boolean hasOrganizationGroup(Player player) {
        if (player == null) return false;
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return false;
        return user.getNodes().stream().anyMatch(this::isOrganizationGroup);
    }

    // ====== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ======

    private boolean isOrganizationGroup(net.luckperms.api.node.Node node) {
        if (node instanceof InheritanceNode) {
            String group = ((InheritanceNode) node).getGroupName();
            return ORG_GROUP_PATTERN.matcher(group).matches();
        }
        return false;
    }

    // Примитивная транслитерация кириллицы в латиницу.
    public String transliterate(String text) {
        if (text == null) return "";
        String[][] table = {
                {"А", "A"}, {"Б", "B"}, {"В", "V"}, {"Г", "G"}, {"Д", "D"}, {"Е", "E"}, {"Ё", "E"},
                {"Ж", "ZH"}, {"З", "Z"}, {"И", "I"}, {"Й", "Y"}, {"К", "K"}, {"Л", "L"}, {"М", "M"},
                {"Н", "N"}, {"О", "O"}, {"П", "P"}, {"Р", "R"}, {"С", "S"}, {"Т", "T"}, {"У", "U"},
                {"Ф", "F"}, {"Х", "KH"}, {"Ц", "TS"}, {"Ч", "CH"}, {"Ш", "SH"}, {"Щ", "SCH"}, {"Ъ", ""},
                {"Ы", "Y"}, {"Ь", ""}, {"Э", "E"}, {"Ю", "YU"}, {"Я", "YA"},
                {"а", "a"}, {"б", "b"}, {"в", "v"}, {"г", "g"}, {"д", "d"}, {"е", "e"}, {"ё", "e"},
                {"ж", "zh"}, {"з", "z"}, {"и", "i"}, {"й", "y"}, {"к", "k"}, {"л", "l"}, {"м", "m"},
                {"н", "n"}, {"о", "o"}, {"п", "p"}, {"р", "r"}, {"с", "s"}, {"т", "t"}, {"у", "u"},
                {"ф", "f"}, {"х", "kh"}, {"ц", "ts"}, {"ч", "ch"}, {"ш", "sh"}, {"щ", "sch"}, {"ъ", ""},
                {"ы", "y"}, {"ь", ""}, {"э", "e"}, {"ю", "yu"}, {"я", "ya"}
        };
        String result = text;
        for (String[] pair : table) {
            result = result.replace(pair[0], pair[1]);
        }
        return result.replaceAll("[^A-Za-z]", "");
    }

    /**
     * Устанавливает нужную группу по треку, полностью очищая старые орг-группы и назначая нужную.
     * Если такой группы нет в треке — просто добавляет вручную.
     */
    private void setRankViaTrack(User user, Track track, String groupName) {
        // Удаляем все старые группы по этому треку
        user.data().clear(node -> isOrganizationGroup(node));
        // Проверяем, есть ли нужная группа в треке
        groupName = groupName.toLowerCase();
        if (track.getGroups().contains(groupName)) {
            // Добавляем нужную группу
            InheritanceNode node = InheritanceNode.builder(groupName).build();
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);
        }
    }

    /**
     * Добавляет игроку группу напрямую (без трека), стараясь избежать дублей.
     */
    private void addGroupDirectly(User user, String groupName) {
        user.data().clear(node -> isOrganizationGroup(node));
        InheritanceNode node = InheritanceNode.builder(groupName).build();
        user.data().add(node);
        luckPerms.getUserManager().saveUser(user);
    }
}
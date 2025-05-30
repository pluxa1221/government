package org.government.integrations;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.government.Government;

import java.util.UUID;
import java.util.regex.Pattern;

public class LuckPermsIntegration {

    private final LuckPerms luckPerms;
    private static final Pattern ORG_GROUP_PATTERN = Pattern.compile("^[A-Z0-9_-]+-\\d+$");

    public LuckPermsIntegration() {
        this.luckPerms = LuckPermsProvider.get();
    }

    /**
     * Установить LuckPerms-группу игроку по организации и рангу.
     * Старые орг-группы снимаются.
     * @param player - игрок
     * @param orgNameOriginal - название организации (любой язык)
     * @param rank - ранг
     */
    public void setOrganizationGroup(Player player, String orgNameOriginal, int rank) {
        if (player == null || orgNameOriginal == null || rank <= 0) return;
        UUID uuid = player.getUniqueId();
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            // Пробуем загрузить асинхронно (если нет в памяти)
            luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(u -> {
                if (u != null) {
                    setOrganizationGroupInternal(u, orgNameOriginal, rank);
                    luckPerms.getUserManager().saveUser(u);
                }
            });
            return;
        }
        setOrganizationGroupInternal(user, orgNameOriginal, rank);
        luckPerms.getUserManager().saveUser(user);
    }

    private void setOrganizationGroupInternal(User user, String orgNameOriginal, int rank) {
        // Удаляем старые орг-группы (fsb-1, fsb-2 и т.д.)
        user.data().clear(node -> {
            if (node instanceof InheritanceNode) {
                String group = ((InheritanceNode) node).getGroupName();
                return ORG_GROUP_PATTERN.matcher(group).matches();
            }
            return false;
        });

        // Добавляем новую группу
        String groupBase = transliterate(orgNameOriginal).toUpperCase().replaceAll("[^A-Z0-9_-]", "");
        String groupName = groupBase + "-" + rank;
        InheritanceNode node = InheritanceNode.builder(groupName).build();
        user.data().add(node);
    }

    /**
     * Удалить все LuckPerms-группы формата ОРГАНИЗАЦИЯ-РАНГ у игрока
     */
    public void removeOrganizationGroups(Player player) {
        if (player == null) return;
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        user.data().clear(node -> {
            if (node instanceof InheritanceNode) {
                String group = ((InheritanceNode) node).getGroupName();
                return ORG_GROUP_PATTERN.matcher(group).matches();
            }
            return false;
        });
        luckPerms.getUserManager().saveUser(user);
    }

    public void removeOrganizationGroups(String player) {
        if (player == null) return;
        User user = luckPerms.getUserManager().getUser(Government.getInstance().getServer().getOfflinePlayer(player).getUniqueId());
        if (user == null) return;
        user.data().clear(node -> {
            if (node instanceof InheritanceNode) {
                String group = ((InheritanceNode) node).getGroupName();
                return ORG_GROUP_PATTERN.matcher(group).matches();
            }
            return false;
        });
        luckPerms.getUserManager().saveUser(user);
    }

    /**
     * Транслитерация кириллицы в латиницу (простой вариант, можно доработать)
     */
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
        return result.replaceAll("[^A-Za-z0-9_-]", "");
    }

    /**
     * Проверяет, состоит ли игрок в группе формата ОРГАНИЗАЦИЯ-РАНГ
     */
    public boolean hasOrganizationGroup(Player player) {
        if (player == null) return false;
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return false;
        return user.getNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> ((InheritanceNode) node).getGroupName())
                .anyMatch(group -> ORG_GROUP_PATTERN.matcher(group).matches());
    }
}
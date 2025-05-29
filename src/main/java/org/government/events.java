package org.government;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.government.utils.Organization;

public class events implements Listener {
    public final Government plugin;
    public events(Government plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        Organization org = plugin.getOrganization(playerName);
        if (org != null) {
            event.getPlayer().sendMessage("Вы состоите в организации " + org.getName());
        }
    }
}
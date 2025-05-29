package org.government;

import org.bukkit.entity.Player;
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
        // Get the joined player
        Player joinedPlayer = event.getPlayer();

        // Check if the player is in an organization
        Organization org = plugin.getOrganization(joinedPlayer.getName());
        if (org != null) {
            // Send a message to the player with their organization's policies
            joinedPlayer.sendMessage("You are part of " + org.getName() + ". Your organization are: " + org.getName());
        }
    }
}

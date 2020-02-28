package cc.eumc.eusconnectivity;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

public final class EusConnectivity extends Plugin implements Listener {
    public int onlinePlayers = 0;
    public int maxPlayers = 0;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getProxy().getScheduler().schedule(this, new CountTask(this), 0, 10, TimeUnit.SECONDS);
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPing(ProxyPingEvent e) {
        ServerPing.Players players = e.getResponse().getPlayers();
        players.setOnline(onlinePlayers);
        players.setMax(maxPlayers);
        e.getResponse().setPlayers(players);
    }
}

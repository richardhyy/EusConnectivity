package cc.eumc.eusconnectivity;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class EusConnectivity extends Plugin implements Listener {
    public int onlinePlayers = 0;
    public int maxPlayers = 0;

    Map<String, ServerInfo> hostServerMap = new HashMap<>();
    Set<ProxiedPlayer> hasRedirected = new HashSet<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");


        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            loadHostServerMap(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getProxy().getScheduler().schedule(this, new CountTask(this), 0, 10, TimeUnit.SECONDS);
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    void loadHostServerMap(Configuration config) {
        getLogger().info("Loading hostname-server map...");
        for (String s : config.getSection("Settings.HostName-ServerMap").getKeys()) {
            ServerInfo serverInfo = getProxy().getServerInfo(s);
            if (serverInfo != null) {
                List<String> hostList = config.getStringList("Settings.HostName-ServerMap." + s);
                for (String host : hostList) {
                    hostServerMap.put(host, serverInfo);
                    getLogger().info(host + " ->" + serverInfo);
                }
            }
            else {
                getLogger().severe("Server `" + s + "' not found.");
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPing(ProxyPingEvent e) {
        ServerPing.Players players = e.getResponse().getPlayers();
        players.setOnline(onlinePlayers);
        players.setMax(maxPlayers);
        e.getResponse().setPlayers(players);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        hasRedirected.remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onConnect(ServerConnectEvent e) {
        if (hasRedirected.contains(e.getPlayer())) {
            return;
        }
        else {
            hasRedirected.add(e.getPlayer());
        }

        InetSocketAddress virtualHost = e.getPlayer().getPendingConnection().getVirtualHost();
        if (virtualHost == null) return;

        ServerInfo serverInfo = hostServerMap.get(virtualHost.getHostName());
        getLogger().info(e.getPlayer().getName() + " @ " + virtualHost.getHostName());
        if (serverInfo == null) return;

        getLogger().info("Redirecting: " + e.getPlayer().getName() + "(" + virtualHost.getHostName() + ") @ ->" + serverInfo);
        e.setTarget(serverInfo);
    }
}

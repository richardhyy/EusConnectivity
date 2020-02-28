package cc.eumc.eusconnectivity;

import com.google.gson.Gson;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;

import java.util.Map;

public class CountTask implements Runnable {
    EusConnectivity plugin;
    public CountTask(EusConnectivity instance) {
        this.plugin = instance;
    }

    @Override
    public void run() {
        Map<String, ServerInfo> serverInfoMap = plugin.getProxy().getServers();
        plugin.onlinePlayers = 0;
        plugin.maxPlayers = 0;
        serverInfoMap.forEach((name, server) -> {
            Callback<ServerPing> callback = (pingResult, error) -> {
                if (pingResult != null) {
                    plugin.onlinePlayers += pingResult.getPlayers().getOnline();
                    plugin.maxPlayers += pingResult.getPlayers().getMax();
                    //plugin.getLogger().info(name + ": " + pingResult.getPlayers().getOnline());
                }
            };
            server.ping(callback);
        });
    }
}

package fr.mrcubee.mapdisplay;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapDisplayPlugin extends JavaPlugin implements Listener {

    private Map<Player, MapFrame> players;

    @Override
    public void onEnable() {
        this.players = new LinkedHashMap<Player, MapFrame>();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) throws MalformedURLException {
        final int[] display = {0};
        MapFrame mapFrame = MapFrame.createMapFrame(event.getPlayer().getLocation().add(0, 1.5, 0), BlockFace.NORTH);

        if (mapFrame == null)
            return;
        this.players.put(event.getPlayer(), mapFrame);
        mapFrame.spawn(event.getPlayer());
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                switch (display[0]) {
                    case 0:
                        try {
                            mapFrame.drawn(event.getPlayer(), ImageIO.read(new URL("https://cravatar.eu/helmavatar/" + event.getPlayer().getUniqueId().toString() + "/600")));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        mapFrame.drawn(event.getPlayer(), MojangAPI.getPlayerSkinImage(event.getPlayer().getUniqueId()));
                        break;
                    default:
                        mapFrame.drawn(event.getPlayer(), null);
                        break;
                }
                display[0]++;
                if (display[0] > 1)
                    display[0] = 0;
            }
        }, 0L, 20L);
    }

    @EventHandler
    public void playerJoinEvent(PlayerQuitEvent event) {
        MapFrame mapFrame = this.players.remove(event.getPlayer());

        if (mapFrame != null)
            mapFrame.remove(event.getPlayer());
    }

}
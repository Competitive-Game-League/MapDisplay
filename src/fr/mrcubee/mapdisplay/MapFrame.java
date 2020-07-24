package fr.mrcubee.mapdisplay;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.awt.*;
import java.lang.reflect.Constructor;

public interface MapFrame {

    void spawn(Player player);
    void setLocation(Location location, BlockFace blockFace);
    Location getLocation();
    void drawn(Player player, Image image);
    void remove(Player player);

    static MapFrame createMapFrame(Location location, BlockFace blockFace) {
        String packageName;
        String packageVersion;
        Class<?> clazz;
        Constructor<?> constructor;
        MapFrame result;

        if (location == null || location.getWorld() == null || blockFace == null)
            return null;
        packageName = Bukkit.getServer().getClass().getPackage().getName();
        packageVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
        try {
            clazz = Class.forName("fr.mrcubee.mapdisplay." + packageVersion + ".CraftMapFrame");
        } catch (ClassNotFoundException e) {
            return null;
        }
        if (!MapFrame.class.isAssignableFrom(clazz))
            return null;
        try {
            constructor = clazz.getConstructor(Location.class, BlockFace.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
        try {
            result = (MapFrame) constructor.newInstance(location, blockFace);
        } catch (Exception e) {
            return null;
        }
        return (result);
    }
}
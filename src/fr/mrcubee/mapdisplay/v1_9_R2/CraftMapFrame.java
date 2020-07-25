package fr.mrcubee.mapdisplay.v1_9_R2;

import fr.mrcubee.mapdisplay.MapFrame;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.UUID;

public class CraftMapFrame implements MapFrame {

    private byte[] buffer;
    private EntityItemFrame entityItemFrame;

    private EnumDirection convert(BlockFace blockFace) {
        switch (blockFace) {
            case SOUTH:
                return EnumDirection.SOUTH;
            case EAST:
                return EnumDirection.EAST;
            case WEST:
                return EnumDirection.WEST;
            default:
                break;
        }
        return EnumDirection.NORTH;
    }

    public CraftMapFrame(Location location, BlockFace blockFace) {
        ItemStack itemStack;

        this.buffer = new byte[128 * 128];
        this.entityItemFrame = new EntityItemFrame(((CraftWorld) location.getWorld()).getHandle(), new BlockPosition(location.getX(), location.getY(), location.getZ()), convert(blockFace));
        itemStack = new ItemStack(Items.FILLED_MAP);
        itemStack.setData(UUID.randomUUID().hashCode());
        this.entityItemFrame.setItem(itemStack);
    }

    @Override
    public void setLocation(Location location, BlockFace blockFace) {
        if (location != null && location.getWorld() != null) {
            this.entityItemFrame.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            this.entityItemFrame.world = ((CraftWorld) location.getWorld()).getHandle();
        }
        if (blockFace != null)
            this.entityItemFrame.setDirection(convert(blockFace));
    }

    @Override
    public Location getLocation() {
        BlockPosition blockPosition = this.entityItemFrame.getBlockPosition();

        return new Location(this.entityItemFrame.world.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    @Override
    public void spawn(Player player) {
        PlayerConnection playerConnection;

        if (player == null)
            return;
        playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        if (playerConnection == null)
            return;
        playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.entityItemFrame.getId()));
        playerConnection.sendPacket(new PacketPlayOutSpawnEntity(this.entityItemFrame, 71));
        playerConnection.sendPacket(new PacketPlayOutEntityMetadata(this.entityItemFrame.getId(), this.entityItemFrame.getDataWatcher(), false));
        drawn(player, null);
    }

    @Override
    public void drawn(Player player, Image image) {
        PlayerConnection playerConnection;
        BufferedImage imageResized;
        byte[] bytes;

        if (player == null)
            return;
        playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        if (playerConnection == null)
            return;
        if (image == null) {
            for (int i = 0; i < 128 * 128; i++)
                buffer[i] = 0;
            playerConnection.sendPacket(new PacketPlayOutMap(this.entityItemFrame.getItem().getData(), (byte) 3, true, new ArrayList<MapIcon>(), this.buffer, 0, 0,  128, 128));
            return;
        }
        imageResized = MapPalette.resizeImage(image);
        if (imageResized == null)
            return;
        bytes = MapPalette.imageToBytes(imageResized);
        if (bytes == null)
            return;
        for(int x = 0; x < imageResized.getWidth(); ++x)
            for(int y = 0; y < imageResized.getHeight(); ++y)
                this.buffer[y * imageResized.getWidth() + x] = bytes[y * imageResized.getWidth() + x];
        playerConnection.sendPacket(new PacketPlayOutMap(this.entityItemFrame.getItem().getData(), (byte) 3, true, new ArrayList<MapIcon>(), this.buffer, 0, 0,  imageResized.getWidth(),  imageResized.getHeight()));
    }

    @Override
    public void remove(Player player) {
        PlayerConnection playerConnection;
        
        if (player == null)
            return;
        playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        if (playerConnection == null)
            return;
        playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.entityItemFrame.getId()));
    }
}
package com.SirBlobman.expansion.worldguard.olivolja3;

import com.SirBlobman.combatlogx.CombatLogX;
import com.SirBlobman.combatlogx.event.PlayerTagEvent;
import com.SirBlobman.combatlogx.event.PlayerUntagEvent;
import com.SirBlobman.combatlogx.utility.CombatUtil;
import com.SirBlobman.combatlogx.utility.PluginUtil;
import com.SirBlobman.combatlogx.utility.legacy.LegacyHandler;
import com.SirBlobman.expansion.worldguard.config.ConfigWG;
import com.SirBlobman.expansion.worldguard.olivolja3.packetwrapper.WrapperPlayServerBlockChange;
import com.SirBlobman.expansion.worldguard.utility.WGUtil;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ForceField implements Listener {

    private static final Plugin plugin = JavaPlugin.getPlugin(CombatLogX.class);

    private static Map<UUID, Set<Location>> fakeBlocks = new HashMap<>();

    public static void unregisterProtocol() {
        if(!PluginUtil.isEnabled("ProtocolLib")) return;
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.removePacketListeners(plugin);
    }

    public static void registerProtocol() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.BLOCK_CHANGE) {
            @Override
            public void onPacketSending(PacketEvent e) {
                if (e.isCancelled()) return;
                Player p = e.getPlayer();
                if(!CombatUtil.isInCombat(p)) return;
                WrapperPlayServerBlockChange block = new WrapperPlayServerBlockChange(e.getPacket());
                Location loc = e.getPacket().getBlockPositionModifier().read(0).toLocation(p.getWorld());
                if(ForceField.fakeBlocks.containsKey(p.getUniqueId()) && ForceField.isSafe(loc) && ForceField.isSafeSurround(loc) && ForceField.canPlace(loc) && ForceField.fakeBlocks.get(p.getUniqueId()).contains(loc)) {
                    block.setBlockData(new ForceField().wrappedData(block.getBlockData()));
                }
            }
        });
    }

    private WrappedBlockData wrappedData(WrappedBlockData data) {
        data.setType(ConfigWG.FORCEFIELD_MATERIAL);
        if(LegacyHandler.getMinorVersion() < 13) data.setData(ConfigWG.FORCEFIELD_MATERIAL_DATA);
        return data;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        fakeBlocks.remove(uuid);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location to = e.getTo();
        Location from = e.getFrom();

        if(!CombatUtil.isInCombat(p)) return;
        if(to.getBlock().equals(from.getBlock())) return;
        if(isSafe(p.getLocation())) return;

        updateForceField(p);
    }

    public void clearData() {
        fakeBlocks.clear();
    }

    public void updateForceField(Player p) {
        if(!CombatUtil.isInCombat(p)) return;
        if(isSafe(p.getLocation())) return;
        Set<Location> oldArea = new HashSet<>();
        Set<Location> area = getForceFieldArea(p);
        Set<Location> area2 = new HashSet<>(area);
        if(fakeBlocks.containsKey(p.getUniqueId())) {
            oldArea = fakeBlocks.get(p.getUniqueId());
            area.removeAll(oldArea);
            oldArea.removeAll(area2);
        }
        fakeBlocks.remove(p.getUniqueId());
        for(Location loc : oldArea) {
            resetBlock(p, loc);
        }

        for(Location loc : area) {
            sendForceField(p, loc);
        }

        fakeBlocks.put(p.getUniqueId(), area2);
    }

    @EventHandler
    public void onTag(PlayerTagEvent e) {
        Player p = e.getPlayer();

        if(isSafe(p.getLocation())) return;
        Set<Location> area = getForceFieldArea(p);
        fakeBlocks.put(p.getUniqueId(), area);
        for(Location loc : area) {
            sendForceField(p, loc);
        }
    }

    @EventHandler
    public void onUntag(PlayerUntagEvent e) {
        Player p = e.getPlayer();
        removeForceField(p);
    }

    public void removeForceField(Player p) {
        if(fakeBlocks.containsKey(p.getUniqueId())) {
            Set<Location> locations = new HashSet<>(fakeBlocks.get(p.getUniqueId()));
            fakeBlocks.remove(p.getUniqueId());
            for(Location loc : locations) {
                resetBlock(p, loc);
            }
        }
    }



    private Set<Location> getForceFieldArea(Player p) {
        Set<Location> area = new HashSet<>();
        Location pLoc = p.getLocation();
        int radius = ConfigWG.FORCEFIELD_SIZE;

        Location loc1 = pLoc.clone().add(radius, 0, radius);
        Location loc2 = pLoc.clone().subtract(radius, 0, radius);
        int topBlockX = loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int bottomBlockX = loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int topBlockZ = loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();
        int bottomBlockZ = loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                Location location = new Location(pLoc.getWorld(), (double) x, pLoc.getY(), (double) z);
                if(!isSafe(location)) continue;
                if (!isSafeSurround(location)) continue;
                for (int i = -radius; i < radius; i++) {
                    Location loc3 = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
                    loc3.setY(loc3.getY() + i);
                    if(!canPlace(loc3)) continue;
                    area.add(new Location(loc3.getWorld(), loc3.getBlockX(), loc3.getBlockY(), loc3.getBlockZ()));
                }
            }
        }
        return area;
    }

    public static boolean canPlace(Location loc) {
        Block b = loc.getBlock();
        Material material = b.getType();
        return !material.isSolid() || material.equals(Material.AIR);

    }

    public static boolean isSafe(Location loc) {
        return !(WGUtil.allowsPvP(loc) || WGUtil.allowsMobCombat(loc));
    }

    public static boolean isSafeSurround(Location loc) {
        Set<BlockFace> faces = new HashSet<>(Arrays.asList(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST));
        for(BlockFace face : faces) { if(!isSafe(loc.getBlock().getRelative(face).getLocation())) return true; }
        return false;
    }

    @SuppressWarnings("deprecation")
    private void sendForceField(Player p, Location loc) {
        if(LegacyHandler.getMinorVersion() >= 13) p.sendBlockChange(loc, ConfigWG.FORCEFIELD_MATERIAL.createBlockData());
        else p.sendBlockChange(loc, ConfigWG.FORCEFIELD_MATERIAL, ConfigWG.FORCEFIELD_MATERIAL_DATA);
    }

    @SuppressWarnings("deprecation")
    private void resetBlock(Player p, Location loc) {
        Block b = loc.getBlock();
        if(LegacyHandler.getMinorVersion() >= 13) p.sendBlockChange(loc, b.getBlockData());
        else p.sendBlockChange(loc, b.getType(), b.getData());
    }
}

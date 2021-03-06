package com.SirBlobman.expansion.worldguard.config;

import com.SirBlobman.combatlogx.CombatLogX;
import com.SirBlobman.combatlogx.config.Config;
import com.SirBlobman.combatlogx.event.PlayerTagEvent;
import com.SirBlobman.combatlogx.utility.PluginUtil;
import com.SirBlobman.combatlogx.utility.Util;
import com.SirBlobman.combatlogx.utility.legacy.LegacyHandler;
import com.SirBlobman.expansion.worldguard.CompatWorldGuard;
import com.SirBlobman.expansion.worldguard.olivolja3.ForceField;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class ConfigWG extends Config {
    public static double NO_ENTRY_KNOCKBACK_STRENGTH;
    public static int MESSAGE_COOLDOWN;
    public static boolean FORCEFIELD_ENABLED;
    public static int FORCEFIELD_SIZE;
    public static Material FORCEFIELD_MATERIAL;
    public static Byte FORCEFIELD_MATERIAL_DATA;
    private static YamlConfiguration config = new YamlConfiguration();
    private static String NO_ENTRY_MODE;
    private static String FORCEFIELD_MATERIAL_NAME;

    public static void load() {
        File folder = CompatWorldGuard.FOLDER;
        File file = new File(folder, "worldguard.yml");

        if (!file.exists()) copyFromJar("worldguard.yml", folder);
        config = load(file);
        defaults();
        updateMaterials();
        checkValidForceField();
    }

    private static void defaults() {
        NO_ENTRY_MODE = get(config, "world guard.no entry.mode", "KNOCKBACK");
        NO_ENTRY_KNOCKBACK_STRENGTH = get(config, "world guard.no entry.knockback power", 1.5D);
        MESSAGE_COOLDOWN = get(config, "world guard.no entry.message cooldown", 5);
        FORCEFIELD_ENABLED = get(config, "world guard.forcefield.enabled", false);
        FORCEFIELD_SIZE = get(config, "world guard.forcefield.size", 4);
        FORCEFIELD_MATERIAL_NAME = get(config, "world guard.forcefield.material", "GLASS");
        FORCEFIELD_MATERIAL = Material.GLASS;
        FORCEFIELD_MATERIAL_DATA = 0;
    }

    public static NoEntryMode getNoEntryMode() {
        if (NO_ENTRY_MODE == null || NO_ENTRY_MODE.isEmpty()) load();
        String mode = NO_ENTRY_MODE.toUpperCase();
        try {
            return NoEntryMode.valueOf(mode);
        } catch (Throwable ex) {
            String error = "Invalid Mode '" + NO_ENTRY_MODE + "' in 'worldguard.yml'. Valid modes are CANCEL, TELEPORT, KNOCKBACK, or KILL";
            Util.print(error);
            return NoEntryMode.CANCEL;
        }
    }

    public static void checkValidForceField() {
        if(!PluginUtil.isEnabled("CombatLogX")) return;
        Plugin plugin = JavaPlugin.getPlugin(CombatLogX.class);
        if (FORCEFIELD_ENABLED && !plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            plugin.getLogger().log(Level.WARNING, "ForceField is enabled but ProtocolLib isn't, disabling forcefield...");
            FORCEFIELD_ENABLED = false;
        }
        if(!FORCEFIELD_ENABLED) {
            for(RegisteredListener listener : PlayerTagEvent.getHandlerList().getRegisteredListeners()) {
                if(listener.getListener().getClass().getName().endsWith("olivolja3.ForceField")) {
                    ForceField forceField = new ForceField();
                    HandlerList.unregisterAll(plugin);
                    PluginUtil.regEvents(new CompatWorldGuard());
                    ForceField.unregisterProtocol();
                    Bukkit.getOnlinePlayers().forEach(forceField::removeForceField);
                    forceField.clearData();
                    return;
                }
            }

        } else {
            RegisteredListener[] registeredListeners = PlayerTagEvent.getHandlerList().getRegisteredListeners();
            for (RegisteredListener listener : registeredListeners) {
                if(!listener.getListener().equals(new ForceField())) {
                    ForceField forceField = new ForceField();
                    PluginUtil.regEvents(new ForceField());
                    ForceField.registerProtocol();
                    Bukkit.getOnlinePlayers().forEach(forceField::updateForceField);
                    return;
                }
            }
        }
    }

    private static void updateMaterials() {

        if(LegacyHandler.getMinorVersion() < 13 && FORCEFIELD_MATERIAL_NAME.contains(":")) {
            String[] materialStrings = FORCEFIELD_MATERIAL_NAME.split(":");
            Material material = Material.getMaterial(materialStrings[0]);
            if (material != null && material.isBlock()) {
                byte data = Byte.valueOf(materialStrings[1]);
                if (data > 15) data = 0;
                FORCEFIELD_MATERIAL_DATA = data;
                FORCEFIELD_MATERIAL = material;
            }
        } else {
            Material forceFieldMaterial = Material.getMaterial(FORCEFIELD_MATERIAL_NAME);
            if (forceFieldMaterial != null && forceFieldMaterial.isBlock()) FORCEFIELD_MATERIAL = forceFieldMaterial;
        }
    }

    public enum NoEntryMode {CANCEL, TELEPORT, KNOCKBACK, KILL}
}
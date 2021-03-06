package com.SirBlobman.combatlogx.config;

import com.SirBlobman.combatlogx.utility.Util;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigLang extends Config {
    private static final File FILE = new File(FOLDER, "language.yml");
    private static YamlConfiguration config = new YamlConfiguration();

    public static YamlConfiguration load() {
        if (!FILE.exists()) copyFromJar("language.yml", FOLDER);
        config = load(FILE);
        return config;
    }

    public static String get(String path) {
        if (config.isSet(path)) {
            String msg = config.getString(path);
            return Util.color(msg);
        } else return path;
    }

    public static String getWithPrefix(String path) {
        String msg = get(path);
        String prefix = get("messages.plugin prefix");
        String with = msg.isEmpty() ? "" : (prefix.isEmpty() ? msg : (prefix + " " + msg));
        return Util.color(with);
    }
}
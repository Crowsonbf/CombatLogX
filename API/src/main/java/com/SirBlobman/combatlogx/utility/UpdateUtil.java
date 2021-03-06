package com.SirBlobman.combatlogx.utility;

import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class UpdateUtil extends Util {
    private static final String SPIGOT_URL = "https://api.spigotmc.org/legacy/update.php?resource=31689";
    private static String spigotVersion = null;
    private static String pluginVersion = null;

    public static void checkForUpdates() {
        BS.runTaskAsynchronously(PLUGIN, () -> {
            String spigotVersion = getSpigotVersion();
            String pluginVersion = getPluginVersion();
            int pluginMajor = getPluginVersionMajor();
            int spigotMajor = getSpigotVersionMajor();

            if (pluginMajor == -1 || spigotMajor == -1) {
                String[] error = getErrorMessage();
                printNoPrefix(error);
            } else if (pluginMajor == spigotMajor) {
                int pluginMinor = getPluginVersionMinor();
                int spigotMinor = getSpigotVersionMinor();

                if (pluginMinor == spigotMinor) {
                    int pluginPatch = getPluginVersionPatch();
                    int spigotPatch = getSpigotVersionPatch();

                    if (pluginPatch == spigotPatch) {
                        int pluginExpan = getPluginVersionExpansion();
                        int spigotExpan = getSpigotVersionExpansion();

                        if (pluginExpan == spigotExpan) {
                            String[] msg = getUpdatedMessage();
                            printNoPrefix(msg);
                        } else if (pluginExpan > spigotExpan) {
                            String[] msg = getBetaMessage();
                            printNoPrefix(msg);
                        } else {
                            String[] msg = getUpdateMessage(pluginVersion, spigotVersion);
                            printNoPrefix(msg);
                        }
                    } else if (pluginPatch > spigotPatch) {
                        String[] msg = getBetaMessage();
                        printNoPrefix(msg);
                    } else {
                        String[] msg = getUpdateMessage(pluginVersion, spigotVersion);
                        printNoPrefix(msg);
                    }
                } else if (pluginMinor > spigotMinor) {
                    String[] msg = getBetaMessage();
                    printNoPrefix(msg);
                } else {
                    String[] msg = getUpdateMessage(pluginVersion, spigotVersion);
                    printNoPrefix(msg);
                }
            } else if (pluginMajor > spigotMajor) {
                String[] msg = getBetaMessage();
                printNoPrefix(msg);
            } else {
                String[] msg = getUpdateMessage(pluginVersion, spigotVersion);
                printNoPrefix(msg);
            }
        });
    }

    private static String[] getErrorMessage() {
        return color(
                "&8==============================================",
                "&eCombatLogX Update Checker",
                " ",
                "&cThere was an error checking for updates",
                "&8=============================================="
        );
    }

    private static String[] getBetaMessage() {
        return color(
                "&8==============================================",
                "&eCombatLogX Update Checker",
                " ",
                "&cYou are using a beta or bleeding-edge version.",
                "&cThanks for testing CombatLogX!",
                "&8=============================================="
        );
    }

    private static String[] getUpdatedMessage() {
        return color(
                "&8==============================================",
                "&eCombatLogX Update Checker",
                " ",
                "&aYou are using the latest version!",
                "&8=============================================="
        );
    }

    private static String[] getUpdateMessage(String pluginVersion, String spigotVersion) {
        return color(
                "&8==============================================",
                "&eCombatLogX Update Checker",
                " ",
                "&aThere is an update available!",
                "&e&lLatest Version: &a" + spigotVersion,
                "&e&lYour Version: &c" + pluginVersion,
                "&eGet it here: &bhttps://www.spigotmc.org/resources/combatlogx.31689/",
                "&8=============================================="
        );
    }

    public static String getSpigotVersion() {
        if (spigotVersion != null) return spigotVersion;
        else {
            try {
                print("Checking for updates using Spigot API...");
                URL url = new URL(SPIGOT_URL);
                URLConnection urlc = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) urlc;
                http.setRequestMethod("GET");

                InputStream is = http.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String version = br.readLine();
                br.close();
                isr.close();
                is.close();

                spigotVersion = version;
                return version;
            } catch (Throwable ex) {
                return null;
            }
        }
    }

    public static String getPluginVersion() {
        if (pluginVersion != null) return pluginVersion;
        else {
            PluginDescriptionFile pdf = PLUGIN.getDescription();
            String version = pdf.getVersion();
            if (version.contains("-")) {
                int indexOf = version.indexOf("-");
                version = version.substring(0, indexOf);
            }

            pluginVersion = version;
            return version;
        }
    }

    private static int[] getVersionAll(String version) {
        int[] fail = new int[]{-1, 0, 0, 0};
        if (version == null || version.isEmpty()) return fail;
        else {
            String[] split = version.split("\\.", 4);
            if (split.length != 4) return fail;
            else {
                int[] ii = new int[4];
                for (int part = 0; part < split.length; part++) {
                    String s = split[part];
                    s = s.replaceAll("[^0-9]", "");
                    int i;
                    try {
                        i = Integer.parseInt(s);
                    } catch (Throwable ex) {
                        i = -1;
                    }
                    ii[part] = i;
                }
                return ii;
            }
        }
    }

    private static int getPluginVersionMajor() {
        String version = getPluginVersion();
        int[] all = getVersionAll(version);
        return all[0];
    }

    private static int getPluginVersionMinor() {
        String version = getPluginVersion();
        int[] all = getVersionAll(version);
        return all[1];
    }

    private static int getPluginVersionPatch() {
        String version = getPluginVersion();
        int[] all = getVersionAll(version);
        return all[2];
    }

    private static int getPluginVersionExpansion() {
        String version = getPluginVersion();
        int[] all = getVersionAll(version);
        return all[3];
    }

    // Spigot Version Parts
    private static int getSpigotVersionMajor() {
        String version = getSpigotVersion();
        int[] all = getVersionAll(version);
        return all[0];
    }

    private static int getSpigotVersionMinor() {
        String version = getSpigotVersion();
        int[] all = getVersionAll(version);
        return all[1];
    }

    private static int getSpigotVersionPatch() {
        String version = getSpigotVersion();
        int[] all = getVersionAll(version);
        return all[2];
    }

    private static int getSpigotVersionExpansion() {
        String version = getSpigotVersion();
        int[] all = getVersionAll(version);
        return all[3];
    }
}
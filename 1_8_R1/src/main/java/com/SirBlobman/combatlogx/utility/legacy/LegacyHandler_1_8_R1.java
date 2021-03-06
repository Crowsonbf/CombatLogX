package com.SirBlobman.combatlogx.utility.legacy;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.SirBlobman.combatlogx.utility.PluginUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.inventivetalent.bossbar.BossBarAPI;

public class LegacyHandler_1_8_R1 extends LegacyHandler {
    @Override
    public double getMaxHealth(LivingEntity entity) {
        try {
            Class<?> class_entity = entity.getClass();
            Method method = class_entity.getMethod("getMaxHealth");
            return (double) method.invoke(entity);
        } catch (Throwable ex) {
            return 0.0D;
        }
    }

    @Override
    public void setMaxHealth(LivingEntity entity, double maxHealth) {
        try {
            Class<?> class_entity = entity.getClass();
            Class<?> class_double = Double.TYPE;
            Method method = class_entity.getMethod("setMaxHealth", class_double);
            method.invoke(entity, maxHealth);
        } catch (Throwable ignored) {

        }
        //entity.setMaxHealth(maxHealth);
    }

    @Override
    public void sendActionBar(Player player, String msg) {
        try {
            String json = "{\"text\": \"" + msg + "\"}";
            byte ACTION_BAR = 2;

            Class<?> class_IChatBaseComponent = Class.forName("net.minecraft.server.v1_8_R1.IChatBaseComponent");
            Class<?> class_ChatSerializer = Class.forName("net.minecraft.server.v1_8_R1.ChatSerializer");
            Class<?> class_CraftPlayer = Class.forName("org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer");
            Class<?> class_PacketPlayOutChat = Class.forName("net.minecraft.server.v1_8_R1.PacketPlayOutChat");
            Class<?> class_EntityPlayer = Class.forName("net.minecraft.server.v1_8_R1.EntityPlayer");
            Class<?> class_PlayerConnection = Class.forName("net.minecraft.server.v1_8_R1.PlayerConnection");
            Class<?> class_Packet = Class.forName("net.minecraft.server.v1_8_R1.Packet");

            Method method_a = class_ChatSerializer.getMethod("a", String.class);
            Object object_IChatBaseComponent = method_a.invoke(null, json);

            Constructor<?> constructor_PacketPlayOutChat = class_PacketPlayOutChat.getConstructor(class_IChatBaseComponent, Byte.TYPE);
            Object object_PacketPlayOutChat = constructor_PacketPlayOutChat.newInstance(object_IChatBaseComponent, ACTION_BAR);

            Object object_CraftPlayer = class_CraftPlayer.cast(player);
            Method method_getHandle = class_CraftPlayer.getMethod("getHandle");

            Object object_EntityPlayer = method_getHandle.invoke(object_CraftPlayer);
            Field field_playerConnection = class_EntityPlayer.getField("playerConnection");

            Object object_PlayerConnection = field_playerConnection.get(object_EntityPlayer);
            Method method_sendPacket = class_PlayerConnection.getMethod("sendPacket", class_Packet);
            method_sendPacket.invoke(object_PlayerConnection, object_PacketPlayOutChat);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendBossBar(Player player, String style, String color, String title, float progress) {
        if (PluginUtil.isEnabled("BossBarAPI", "inventivetalent")) {
            removeBossBar(player);
            BossBarAPI.setMessage(player, title);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeBossBar(Player player) {
        if (PluginUtil.isEnabled("BossBarAPI", "inventivetalent")) {
            BossBarAPI.removeBar(player);
        }
    }

    @Override
    public Objective createScoreboardObjective(Scoreboard scoreboard, String name, String criteria, String displayName) {
        Objective obj = scoreboard.registerNewObjective(name, criteria);
        obj.setDisplayName(displayName);
        return obj;
    }

}
package com.SirBlobman.combatlog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import com.SirBlobman.combatlog.compat.CustomBoss;
import com.SirBlobman.combatlog.compat.CustomScore;
import com.SirBlobman.combatlog.config.Config;
import com.SirBlobman.combatlog.listener.event.PlayerUntagEvent;
import com.SirBlobman.combatlog.listener.event.PlayerUntagEvent.UntagCause;
import com.SirBlobman.combatlog.utility.Util;
import com.google.common.collect.Maps;

public class Combat implements Runnable {
	private static final Server SERVER = Bukkit.getServer();
	private static final PluginManager PM = SERVER.getPluginManager();
	
	private static HashMap<Player, Long> inCombat = Maps.newHashMap();
	private static Map<Player, LivingEntity> enemies = Util.newMap();
	private static Map<Player, CustomScore> scores = Util.newMap();
	
	public static void add(Player p, LivingEntity enemy) {
		long time1 = System.currentTimeMillis();
		long time2 = Config.TIMER * 1000L;
		long time3 = time1 + time2;
		inCombat.put(p, time3);
		enemies.put(p, enemy);
	}
	
	public static void remove(Player p) {
		inCombat.remove(p);
		enemies.remove(p);
		if(scores.containsKey(p)) {
			CustomScore cs = scores.get(p);
			cs.close();
		}
		if(Config.BOSS_BAR) {
			CustomBoss.remove(p);
		}
		String expire = Util.format(Config.MSG_PREFIX + Config.MSG_EXPIRE);
		p.sendMessage(expire);
	}
	
	public static int timeLeft(Player p) {
		long time1 = System.currentTimeMillis();
		long time2 = inCombat.containsKey(p) ? inCombat.get(p) : 0L;
		if(time2 <= 0L) return 0;
		int time3 = (int) ((time2 - time1) / 1000);
		return time3;
	}
	
	public static boolean in(Player p) {
		return inCombat.containsKey(p);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		Map<Player, Long> c2 = (Map<Player, Long>) inCombat.clone();
		Set<Player> set = c2.keySet();
		for(Player p : set) {
			int time = timeLeft(p);
			if(time <= 0) {
				PlayerUntagEvent PUE = new PlayerUntagEvent(p, UntagCause.TIME);
				PM.callEvent(PUE);
			} else {
				if(Config.SCOREBOARD) {
					if(scores.containsKey(p)) {
						CustomScore cs = scores.get(p);
						cs.update();
						cs.changeEnemy(enemies.get(p));
					} else {
						CustomScore cs = new CustomScore(p, enemies.get(p));
						cs.update();
						scores.put(p, cs);
					}
				} if(Config.ACTION_BAR) {
					String action = Util.format(Config.MSG_ACTION_BAR, time);
					Util.action(p, action);
				} if(Config.BOSS_BAR) {
					Util.boss(p);
				}
			}
		}
	}
}
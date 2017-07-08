package com.SirBlobman.combatlog.compat;

import com.SirBlobman.combatlog.Combat;
import com.SirBlobman.combatlog.CombatLog;
import com.SirBlobman.combatlog.utility.LegacyUtil;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.external.EZPlaceholderHook;

public class CombatPlaceHolders extends EZPlaceholderHook {
	public CombatPlaceHolders() {super(CombatLog.instance, "combatlogx");}
	
	@Override
	public String onPlaceholderRequest(Player p, String id) {
		id = id.toLowerCase();
		if(id.equals("time_left")) {
			int time = Combat.timeLeft(p);
			String t = "" + time;
			return t;
		} else if(id.equals("enemy_name")) {
			Damageable le = Combat.enemy(p);
			if(le != null) {
				String name = LegacyUtil.name(le);
				return name;
			} else return "You don't have an enemy!";
		} else if(id.equals("enemy_health")) {
			Damageable le = Combat.enemy(p);
			if(le != null) {
				double health = le.getHealth();
				String h = "" + health;
				return h;
			} else return "0";
		} else return null;
	}
}
package com.SirBlobman.combatlogx.event;

import com.SirBlobman.combatlogx.event.PlayerTagEvent.TagReason;
import com.SirBlobman.combatlogx.event.PlayerTagEvent.TagType;
import com.SirBlobman.combatlogx.utility.CombatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerUntagEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final UntagReason reason;

    public PlayerUntagEvent(Player p, UntagReason reason) {
        super(p);
        this.reason = reason;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * @return The reason that the player was untagged<br/><br/>
     * <b>EXPIRE:</b> The player waited patiently until he was no longer in combat or he was untagged using the API<br/>
     * <b>EXPIRE_ENEMY_DEATH:</b> The player's enemy was killed or died.<br/>
     * <b>KICK:</b> The player was kicked from the server by a plugin or staff member during combat<br/>
     * <b>QUIT:</b> The player logged out during combat
     * @see CombatUtil#untag(Player)
     */
    public UntagReason getUntagReason() {
        return reason;
    }

    @Deprecated
    public void reTag() {
        CombatUtil.tag(getPlayer(), null, TagType.UNKNOWN, TagReason.UNKNOWN);
    }

    public enum UntagReason {EXPIRE, EXPIRE_ENEMY_DEATH, QUIT, KICK}
}
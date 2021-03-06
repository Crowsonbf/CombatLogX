package com.SirBlobman.combatlogx.event;

import com.SirBlobman.combatlogx.event.PlayerTagEvent.TagReason;
import com.SirBlobman.combatlogx.event.PlayerTagEvent.TagType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * This event fires just before a player gets put into combat<br/>
 * If you cancel this event then the player will not get tagged
 *
 * @author SirBlobman
 */
public class PlayerPreTagEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final LivingEntity enemy;
    private final TagType tagType;
    private final TagReason tagReason;
    private boolean cancelled = false;

    public PlayerPreTagEvent(Player p, LivingEntity enemy, TagType type, TagReason reason) {
        super(p);
        this.enemy = enemy;
        this.tagType = type;
        this.tagReason = reason;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * @return The enemy that will tag the player (can be null)
     */
    public LivingEntity getEnemy() {
        return enemy;
    }

    /**
     * @return The type of entity that will cause this player to be tagged
     */
    public TagType getTaggedBy() {
        return tagType;
    }

    /**
     * @return The reason that will cause this player to be tagged
     */
    public TagReason getTagReason() {
        return tagReason;
    }
}
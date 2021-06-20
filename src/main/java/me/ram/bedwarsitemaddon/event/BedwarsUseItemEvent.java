package me.ram.bedwarsitemaddon.event;

import io.github.bedwarsrel.game.Game;
import me.ram.bedwarsitemaddon.EnumItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class BedwarsUseItemEvent extends Event {
  private static HandlerList handlers = new HandlerList();
  
  private Game game;
  
  private Player player;
  
  private EnumItem itemtype;
  
  private ItemStack consumeitem;
  
  private Boolean cancelled = Boolean.valueOf(false);
  
  public BedwarsUseItemEvent(Game game, Player player, EnumItem itemtype, ItemStack consumeitem) {
    this.game = game;
    this.player = player;
    this.itemtype = itemtype;
    this.consumeitem = consumeitem;
  }
  
  public Game getGame() {
    return this.game;
  }
  
  public Player getPlayer() {
    return this.player;
  }
  
  public EnumItem getItemType() {
    return this.itemtype;
  }
  
  public ItemStack getConsumeItem() {
    return this.consumeitem;
  }
  
  public boolean isCancelled() {
    return this.cancelled.booleanValue();
  }
  
  public void setCancelled(boolean cancel) {
    this.cancelled = Boolean.valueOf(cancel);
  }
  
  public HandlerList getHandlers() {
    return handlers;
  }
  
  public static HandlerList getHandlerList() {
    return handlers;
  }
}

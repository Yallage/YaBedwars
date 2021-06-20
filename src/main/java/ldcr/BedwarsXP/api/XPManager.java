package ldcr.BedwarsXP.api;

import java.util.HashMap;
import java.util.UUID;
import ldcr.BedwarsXP.Config;
import ldcr.BedwarsXP.Utils.ActionBarUtils;
import org.bukkit.entity.Player;

public class XPManager {
  private static HashMap<String, XPManager> managerMap = new HashMap<>();
  
  private final HashMap<UUID, Integer> xp = new HashMap<>();
  
  public static XPManager getXPManager(String bedwarsGame) {
    if (!managerMap.containsKey(bedwarsGame))
      managerMap.put(bedwarsGame, new XPManager()); 
    XPManager manager = managerMap.get(bedwarsGame);
    return manager;
  }
  
  public static void reset(String bedwarsGame) {
    (getXPManager(bedwarsGame)).xp.clear();
    managerMap.remove(bedwarsGame);
  }
  
  public void updateXPBar(Player player) {
    player.setLevel(get(player));
  }
  
  private void set(Player player, int count) {
    this.xp.put(player.getUniqueId(), Integer.valueOf(count));
    updateXPBar(player);
  }
  
  private int get(Player player) {
    Integer value = this.xp.get(player.getUniqueId());
    if (value == null) {
      value = Integer.valueOf(0);
      this.xp.put(player.getUniqueId(), Integer.valueOf(0));
    } 
    return value.intValue();
  }
  
  public void setXP(Player player, int count) {
    set(player, count);
  }
  
  public int getXP(Player player) {
    return get(player);
  }
  
  public void addXP(Player player, int count) {
    set(player, get(player) + count);
  }
  
  public boolean takeXP(Player player, int count) {
    if (!hasEnoughXP(player, count))
      return false; 
    set(player, get(player) - count);
    return true;
  }
  
  public boolean hasEnoughXP(Player player, int count) {
    return (get(player) >= count);
  }
  
  private final HashMap<UUID, Long> messageTimeMap = new HashMap<>();
  
  private final HashMap<UUID, Integer> messageCountMap = new HashMap<>();
  
  public void sendXPMessage(Player player, int count) {
    if (!this.messageTimeMap.containsKey(player.getUniqueId()))
      this.messageTimeMap.put(player.getUniqueId(), Long.valueOf(System.currentTimeMillis())); 
    if (!this.messageCountMap.containsKey(player.getUniqueId()))
      this.messageCountMap.put(player.getUniqueId(), Integer.valueOf(0)); 
    if (System.currentTimeMillis() - ((Long)this.messageTimeMap.get(player.getUniqueId())).longValue() > 500L)
      this.messageCountMap.put(player.getUniqueId(), Integer.valueOf(0)); 
    this.messageTimeMap.put(player.getUniqueId(), Long.valueOf(System.currentTimeMillis()));
    int c = ((Integer)this.messageCountMap.get(player.getUniqueId())).intValue() + count;
    this.messageCountMap.put(player.getUniqueId(), Integer.valueOf(c));
    if (!Config.xpMessage.equals(""))
      ActionBarUtils.sendActionBar(player, Config.xpMessage.replaceAll("%xp%", Integer.toString(c))); 
  }
  
  public void sendMaxXPMessage(Player player) {
    if (!Config.maxXPMessage.equals(""))
      ActionBarUtils.sendActionBar(player, Config.maxXPMessage); 
  }
}

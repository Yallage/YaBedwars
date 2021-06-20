package me.ram.bedwarsscoreboardaddon.storage;

import io.github.bedwarsrel.game.Game;
import java.util.HashMap;
import java.util.Map;

public class PlayerGameStorage {
  private Game game;
  
  private Map<String, Integer> totalkills;
  
  private Map<String, Integer> kills;
  
  private Map<String, Integer> finalkills;
  
  private Map<String, Integer> dies;
  
  private Map<String, Integer> beds;
  
  public PlayerGameStorage(Game game) {
    this.game = game;
    this.totalkills = new HashMap<>();
    this.kills = new HashMap<>();
    this.finalkills = new HashMap<>();
    this.dies = new HashMap<>();
    this.beds = new HashMap<>();
  }
  
  public Game getGame() {
    return this.game;
  }
  
  public Map<String, Integer> getPlayerTotalKills() {
    return this.totalkills;
  }
  
  public Map<String, Integer> getPlayerKills() {
    return this.kills;
  }
  
  public Map<String, Integer> getPlayerFinalKills() {
    return this.finalkills;
  }
  
  public Map<String, Integer> getPlayerDies() {
    return this.dies;
  }
  
  public Map<String, Integer> getPlayerBeds() {
    return this.beds;
  }
}

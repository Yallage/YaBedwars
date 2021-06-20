package me.ram.bedwarsitemaddon.items;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameOverEvent;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.ram.bedwarsitemaddon.EnumItem;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamIronGolem implements Listener {
  private Map<Player, Long> cooldown = new HashMap<>();
  
  private Map<String, Map<IronGolem, Team>> Golems = new HashMap<>();
  
  private Map<String, Map<IronGolem, Integer>> Guardtime = new HashMap<>();
  
  @EventHandler
  public void onStart(BedwarsGameStartEvent e) {
    for (Player player : e.getGame().getPlayers()) {
      if (this.cooldown.containsKey(player))
        this.cooldown.remove(player); 
    } 
    final Game game = e.getGame();
    this.Golems.put(game.getName(), new HashMap<>());
    this.Guardtime.put(game.getName(), new HashMap<>());
    (new BukkitRunnable() {
        public void run() {
          if (game.getState() == GameState.RUNNING) {
            Map<IronGolem, Integer> nmap = new HashMap<>();
            Map<IronGolem, Integer> guardtime = (Map<IronGolem, Integer>)TeamIronGolem.this.Guardtime.get(game.getName());
            List<IronGolem> removelist = new ArrayList<>();
            for (IronGolem irongolem : guardtime.keySet()) {
              if (((Integer)guardtime.get(irongolem)).intValue() == 0) {
                irongolem.remove();
                removelist.add(irongolem);
                continue;
              } 
              if (((Integer)guardtime.get(irongolem)).intValue() > 0)
                nmap.put(irongolem, Integer.valueOf(((Integer)guardtime.get(irongolem)).intValue() - 1)); 
            } 
            for (IronGolem irongolem : removelist)
              guardtime.remove(irongolem); 
            TeamIronGolem.this.Guardtime.put(game.getName(), nmap);
          } else {
            cancel();
          } 
        }
      }).runTaskTimer(Main.getInstance(), 0L, 20L);
  }
  
  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    if (!Config.items_team_iron_golem_enabled)
      return; 
    Player player = e.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
    if (e.getItem() == null || e.getClickedBlock() == null || game == null)
      return; 
    if (!game.getPlayers().contains(player))
      return; 
    if (game.isSpectator(player))
      return; 
    if (game.getState() == GameState.RUNNING && (
      e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && 
      e.getItem().getType() == Material.valueOf(Config.items_team_iron_golem_item) && e.getItem().getDurability() == 0)
      if (System.currentTimeMillis() - ((Long)this.cooldown.getOrDefault(player, Long.valueOf(0L))).longValue() <= (Config.items_team_iron_golem_cooldown * 1000)) {
        e.setCancelled(true);
        player.sendMessage(Config.message_cooling.replace("{time}", (new StringBuilder(String.valueOf(((Config.items_team_iron_golem_cooldown * 1000) - System.currentTimeMillis() + ((Long)this.cooldown.getOrDefault(player, Long.valueOf(0L))).longValue()) / 1000L + 1L))).toString()));
      } else {
        ItemStack stack = e.getItem();
        BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.TeamIronGolem, stack);
        Bukkit.getPluginManager().callEvent((Event)bedwarsUseItemEvent);
        if (!bedwarsUseItemEvent.isCancelled()) {
          TakeItemUtil.TakeItem(player, e.getItem());
          SpawnIronGolem(player, e.getClickedBlock().getRelative(e.getBlockFace()).getLocation().add(0.5D, 0.0D, 0.5D));
        } 
      }  
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDamageGolem(EntityDamageByEntityEvent e) {
    if (!Config.items_team_iron_golem_enabled)
      return; 
    if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof IronGolem))
      return; 
    Player player = (Player)e.getDamager();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
    if (game == null)
      return; 
    if (game.getState() != GameState.RUNNING)
      return; 
    if (game.getPlayerTeam(player) == null)
      return; 
    if (game.isSpectator(player))
      return; 
    IronGolem irongoleme = (IronGolem)e.getEntity();
    if (!((Map)this.Golems.get(game.getName())).containsKey(irongoleme))
      return; 
    if (((Map)this.Golems.get(game.getName())).get(irongoleme) == game.getPlayerTeam(player))
      return; 
    e.setCancelled(false);
  }
  
  @EventHandler
  public void onDamagePlayer(EntityDamageByEntityEvent e) {
    if (!Config.items_team_iron_golem_enabled)
      return; 
    if (!(e.getDamager() instanceof IronGolem) || !(e.getEntity() instanceof Player))
      return; 
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer((Player)e.getEntity());
    if (game == null)
      return; 
    if (game.getState() == GameState.RUNNING)
      e.setDamage(Config.items_team_iron_golem_damage); 
  }
  
  @EventHandler
  public void onDeath(EntityDeathEvent e) {
    if (!Config.items_team_iron_golem_enabled)
      return; 
    if (e.getEntity() instanceof IronGolem)
      for (Map<IronGolem, Team> golems : this.Golems.values()) {
        if (golems.containsKey(e.getEntity())) {
          e.getDrops().clear();
          e.setDroppedExp(0);
          break;
        } 
      }  
  }
  
  @EventHandler
  public void onOver(BedwarsGameOverEvent e) {
    for (IronGolem irongolem : (this.Golems.get(e.getGame().getName())).keySet())
      irongolem.remove(); 
  }
  
  @EventHandler
  public void onDisable(PluginDisableEvent e) {
    if (e.getPlugin().equals(BedwarsRel.getInstance()) || e.getPlugin().equals(Main.getInstance()))
      for (Map<IronGolem, Team> golems : this.Golems.values()) {
        for (IronGolem irongolem : golems.keySet()) {
          if (!irongolem.isDead())
            irongolem.remove(); 
        } 
      }  
  }
  
  public void SpawnIronGolem(final Player player, Location location) {
    final IronGolem irongolem = (IronGolem)player.getWorld().spawn(location, IronGolem.class);
    irongolem.setMaxHealth(Config.items_team_iron_golem_health);
    irongolem.setHealth(irongolem.getMaxHealth());
    irongolem.setCustomNameVisible(true);
    final Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
    ((Map<IronGolem, Team>)this.Golems.get(game.getName())).put(irongolem, game.getPlayerTeam(player));
    ((Map<IronGolem, Integer>)this.Guardtime.get(game.getName())).put(irongolem, Integer.valueOf(Config.items_team_iron_golem_staytime));
    this.cooldown.put(player, Long.valueOf(System.currentTimeMillis()));
    (new BukkitRunnable() {
        public void run() {
          if (game.getState() == GameState.RUNNING) {
            if (irongolem.isDead()) {
              ((Map)TeamIronGolem.this.Guardtime.get(game.getName())).remove(irongolem);
              ((Map)TeamIronGolem.this.Golems.get(game.getName())).remove(irongolem);
              cancel();
              return;
            } 
            irongolem.setCustomName(Config.items_team_iron_golem_name.replace("{color}", (CharSequence)game.getPlayerTeam(player).getChatColor()).replace("{team}", game.getPlayerTeam(player).getName()).replace("{time}", (new StringBuilder(String.valueOf(((Integer)((Map)TeamIronGolem.this.Guardtime.get(game.getName())).get(irongolem)).intValue() + 1))).toString()));
            if (irongolem.getTarget() instanceof Player) {
              Player target = (Player)irongolem.getTarget();
              if (!target.isOnline()) {
                irongolem.setTarget(null);
              } else if (game.getPlayerTeam(target) == null) {
                irongolem.setTarget(null);
              } else if (game.isSpectator(target)) {
                irongolem.setTarget(null);
              } else if (target.getGameMode() == GameMode.SPECTATOR) {
                irongolem.setTarget(null);
              } 
            } 
            List<Player> players = new ArrayList<>();
            for (Team team : game.getTeams().values()) {
              if (team != game.getPlayerTeam(player))
                for (Player t : team.getPlayers())
                  players.add(t);  
            } 
            Player targetplayer = null;
            double range = 0.0D;
            for (Player p : players) {
              if (p.getGameMode() != GameMode.SPECTATOR && p.getLocation().getWorld() == irongolem.getLocation().getWorld()) {
                if (targetplayer == null) {
                  targetplayer = p;
                  range = p.getLocation().distance(irongolem.getLocation());
                  continue;
                } 
                if (range > p.getLocation().distance(irongolem.getLocation())) {
                  targetplayer = p;
                  range = p.getLocation().distance(irongolem.getLocation());
                } 
              } 
            } 
            irongolem.setTarget((LivingEntity)targetplayer);
          } else {
            cancel();
          } 
        }
      }).runTaskTimer(Main.getInstance(), 0L, 0L);
  }
}
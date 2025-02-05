package com.yallage.yabedwars.shop;

import com.yallage.yabedwars.YaBedwars;
import com.yallage.yabedwars.utils.ColorUtil;
import com.yallage.yabedwars.utils.ItemUtil;
import com.yallage.yabedwars.xpshop.ItemShop;
import com.yallage.yabedwars.xpshop.XPItemShop;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.utils.SoundMachine;
import io.github.bedwarsrel.villager.MerchantCategory;

import java.util.*;

import com.yallage.yabedwars.manager.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GHDShop implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpen(InventoryOpenEvent e) {
        if (YaBedwars.mode != 3)
            return;
        Player player = (Player) e.getPlayer();
        Inventory shop = e.getInventory();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game != null && shop.getName().equals(BedwarsRel._l(player, "ingame.shop.name"))) {
            if (shop.getSize() >= 54 && shop.getItem(53) != null)
                return;
            e.setCancelled(true);
            List<ItemStack> shops = new ArrayList<>();
            List<ItemStack> shopitems = new ArrayList<>();
            Map<String, ItemStack> resname = new HashMap<>();
            for (ItemStack res : getResource()) {
                if (res.getItemMeta().getDisplayName() == null) {
                    resname.put("null", new ItemStack(Material.AIR));
                    continue;
                }
                resname.put(res.getItemMeta().getDisplayName(), res);
            }
            resname.put("经验", new ItemStack(Material.EXP_BOTTLE));
            for (int i = 0; i < shop.getSize(); i++) {
                Boolean isShopItem = Boolean.FALSE;
                if (shop.getItem(i) != null && shop.getItem(i).getItemMeta().getLore() != null && shop.getItem(i).getItemMeta().getLore().size() > 0) {
                    String lore = shop.getItem(i).getItemMeta().getLore().get(shop.getItem(i).getItemMeta().getLore().size() - 1);
                    String[] args = lore.split(" ");
                    if (args.length > 1 && ColorUtil.remColor(args[0].replaceAll("\\d+", "")).length() == 0 && resname.containsKey(lore.substring(args[0].length() + 1))) {
                        shopitems.add(shop.getItem(i));
                        isShopItem = Boolean.TRUE;
                    }
                }
                if (shop.getItem(i) != null && !isOptionItem(shop.getItem(i)) && !isShopItem)
                    shops.add(shop.getItem(i));
            }
            int line1;
            line1 = shops.size() / 9;
            if (line1 * 9 < shops.size())
                line1++;
            int line2;
            line2 = shopitems.size() / 9;
            if (line2 * 9 < shopitems.size())
                line2++;
            if (line2 == 0)
                line2++;
            Inventory inventory = Bukkit.createInventory(null, (line1 + line2) * 18, BedwarsRel._l(player, "ingame.shop.name") + "§n§e§w");
            int slot = 0;
            for (ItemStack item : shops) {
                inventory.setItem(slot, item);
                slot++;
            }
            int line;
            line = shops.size() / 9;
            if (line * 9 < shops.size())
                line++;
            slot = line * 9 + 18;
            for (ItemStack shopitem : shopitems) {
                if (slot == 36)
                    slot = 45;
                if (slot > inventory.getSize())
                    break;
                inventory.setItem(slot, shopitem);
                String lore = shopitem.getItemMeta().getLore().get(shopitem.getItemMeta().getLore().size() - 1);
                String[] args = lore.split(" ");
                ItemStack resitem = resname.getOrDefault(lore.substring(args[0].length() + 1), new ItemStack(Material.AIR));
                resitem.setAmount(Integer.valueOf(ColorUtil.remColor(args[0])));
                ItemMeta resitemMeat = resitem.getItemMeta();
                resitemMeat.setDisplayName(lore + "§s§h§o§p§r§e§s");
                resitem.setItemMeta(resitemMeat);
                inventory.setItem(slot - 9, resitem);
                slot++;
            }
            ItemStack frame = getFrame(7);
            for (int k = line * 9; k < 9 + line * 9; k++)
                inventory.setItem(k, frame);
            if (shopitems.size() < 1 && shops.size() > 0)
                if (game.getNewItemShop(player) instanceof XPItemShop) {
                    XPItemShop itemShop = new XPItemShop(game.getNewItemShop(player).getCategories(), game);
                    MerchantCategory clickedCategory = itemShop.getCategoryByMaterial(shops.get(0).getType());
                    if (clickedCategory != null) {
                        itemShop.openBuyInventory(clickedCategory, player, game);
                        return;
                    }
                } else {
                    ItemShop itemShop = new ItemShop(game.getNewItemShop(player).getCategories());
                    MerchantCategory clickedCategory = itemShop.getCategoryByMaterial(shops.get(0).getType());
                    if (clickedCategory != null) {
                        itemShop.openBuyInventory(clickedCategory, player, game);
                        return;
                    }
                }
            player.openInventory(inventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (YaBedwars.mode != 3)
            return;
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getInventory();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && game != null && inventory.getName().equals(BedwarsRel._l(player, "ingame.shop.name") + "§n§e§w")) {
            e.setCancelled(true);
            if (e.getCurrentItem().getItemMeta().getItemFlags().contains(ItemFlag.HIDE_ENCHANTS))
                return;
            Map<String, ItemStack> resname = new HashMap<>();
            for (ItemStack res : getResource()) {
                if (res.getItemMeta().getDisplayName() != null)
                    resname.put(res.getItemMeta().getDisplayName(), res);
            }
            resname.put("经验", new ItemStack(Material.EXP_BOTTLE));
            if (e.getCurrentItem().getItemMeta().getDisplayName() != null && e.getCurrentItem().getItemMeta().getDisplayName().contains("§s§h§o§p§r§e§s"))
                return;
            Boolean isShopItem = Boolean.FALSE;
            if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta().getLore() != null && e.getCurrentItem().getItemMeta().getLore().size() > 0) {
                String lore = e.getCurrentItem().getItemMeta().getLore().get(e.getCurrentItem().getItemMeta().getLore().size() - 1);
                String[] args = lore.split(" ");
                if (args.length > 1 && ColorUtil.remColor(args[0].replaceAll("\\d+", "")).length() == 0 && resname.containsKey(lore.substring(args[0].length() + 1)))
                    isShopItem = Boolean.TRUE;
            }
            if (isShopItem) {
                if (e.isShiftClick()) {
                    int ba = 64 / e.getCurrentItem().getAmount();
                    buyItem(game, player, e.getCurrentItem(), resname, ba);
                } else {
                    buyItem(game, player, e.getCurrentItem(), resname, 1);
                }
            } else if (!Objects.requireNonNull(e.getCurrentItem()).isSimilar(getFrame(7)) && !e.getCurrentItem().isSimilar(getFrame(5))) {
                game.getNewItemShop(player).handleInventoryClick(e, game, player);
            }
        }
    }

    private void buyItem(Game game, Player player, ItemStack itemStack, Map<String, ItemStack> resname, int a) {
        String lore = itemStack.getItemMeta().getLore().get(itemStack.getItemMeta().getLore().size() - 1);
        String[] args = lore.split(" ");
        for (int i = 0; i < a; i++) {
            if (isEnough(game, player, lore.substring(args[0].length() + 1), Integer.valueOf(ColorUtil.remColor(args[0])), resname)) {
                takeItem(game, player, lore.substring(args[0].length() + 1), Integer.valueOf(ColorUtil.remColor(args[0])), resname);
                ItemStack item = itemStack.clone();
                List<String> lores = item.getItemMeta().getLore();
                lores.remove(lores.size() - 1);
                ItemMeta meta = item.getItemMeta();
                meta.setLore(lores);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
                if (i < 1)
                    player.playSound(player.getLocation(), SoundMachine.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), Float.valueOf("1.0"), Float.valueOf("1.0"));
                if (i < 1 && YaBedwars.message_buy.length() > 0) {
                    String name;
                    if (item.getItemMeta().getDisplayName() == null) {
                        name = ItemUtil.getRealName(item);
                    } else {
                        name = item.getItemMeta().getDisplayName();
                    }
                    player.sendMessage(YaBedwars.message_buy.replace("{item}", name));
                }
            } else if (i < 1) {
                player.sendMessage("§c" + ColorUtil.color(BedwarsRel._l(player, "errors.notenoughress")));
            }
        }
    }

    private boolean isEnough(Game game, Player player, String type, int amount, Map<String, ItemStack> resname) {
        if (type.equals("经验") && game.getNewItemShop(player) instanceof XPItemShop) {
            return XPManager.getXPManager(game.getName()).getXP(player) >= amount;
        } else {
            int k = 0;
            int i = (player.getInventory().getContents()).length;
            ItemStack[] stacks = player.getInventory().getContents();
            for (int j = 0; j < i; j++) {
                ItemStack stack = stacks[j];
                if (stack != null &&
                        stack.getType().equals(resname.get(type).getType()))
                    k += stack.getAmount();
            }
            return k >= amount;
        }
    }

    private void takeItem(Game game, Player player, String type, int amount, Map<String, ItemStack> resname) {
        if (type.equals("经验")) {
            XPManager.getXPManager(game.getName()).takeXP(player, amount);
        } else {
            int ta = amount;
            int i = (player.getInventory().getContents()).length;
            ItemStack[] stacks = player.getInventory().getContents();
            for (int j = 0; j < i; j++) {
                ItemStack stack = stacks[j];
                if (stack != null &&
                        stack.getType().equals(resname.get(type).getType()) && ta > 0) {
                    if (stack.getAmount() >= ta) {
                        stack.setAmount(stack.getAmount() - ta);
                        ta = 0;
                    } else if (stack.getAmount() < ta) {
                        ta -= stack.getAmount();
                        stack.setAmount(0);
                    }
                    player.getInventory().setItem(j, stack);
                }
            }
        }
    }

    private ItemStack getFrame(int damage) {
        ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) damage);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(getItemName(YaBedwars.item_frame) + "§f§r§a§m§e");
        itemMeta.setLore(getItemLore(YaBedwars.item_frame));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private List<ItemStack> getResource() {
        List<ItemStack> items = new ArrayList<>();
        ConfigurationSection config = BedwarsRel.getInstance().getConfig().getConfigurationSection("resource");
        for (String res : config.getKeys(false)) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) BedwarsRel.getInstance().getConfig().getList("resource." + res + ".item");
            for (Map<String, Object> resource : list) {
                ItemStack itemStack = ItemStack.deserialize(resource);
                items.add(itemStack);
            }
        }
        return items;
    }

    private Boolean isOptionItem(ItemStack item) {
        ItemStack slime = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta slimeMeta = slime.getItemMeta();
        slimeMeta.setDisplayName(BedwarsRel._l(Bukkit.getConsoleSender(), "ingame.shop.oldshop"));
        slimeMeta.setLore(new ArrayList());
        slime.setItemMeta(slimeMeta);
        if (item.isSimilar(slime))
            return Boolean.TRUE;
        ItemStack snow = new ItemStack(Material.SNOW_BALL, 1);
        ItemMeta snowMeta = snow.getItemMeta();
        snowMeta.setDisplayName(BedwarsRel._l(Bukkit.getConsoleSender(), "ingame.shop.newshop"));
        snowMeta.setLore(new ArrayList());
        snow.setItemMeta(snowMeta);
        if (item.isSimilar(snow))
            return Boolean.TRUE;
        ItemStack bucket = new ItemStack(Material.BUCKET, 1);
        ItemMeta bucketMeta = bucket.getItemMeta();
        bucketMeta.setDisplayName(ChatColor.AQUA + BedwarsRel._l(Bukkit.getConsoleSender(), "default.currently") + ": " + ChatColor.WHITE + BedwarsRel._l(Bukkit.getConsoleSender(), "ingame.shop.onestackpershift"));
        bucketMeta.setLore(new ArrayList());
        bucket.setItemMeta(bucketMeta);
        if (item.isSimilar(bucket))
            return Boolean.TRUE;
        ItemStack lavaBucket = new ItemStack(Material.LAVA_BUCKET, 1);
        ItemMeta lavaBucketMeta = lavaBucket.getItemMeta();
        lavaBucketMeta.setDisplayName(ChatColor.AQUA + BedwarsRel._l(Bukkit.getConsoleSender(), "default.currently") + ": " + ChatColor.WHITE + BedwarsRel._l(Bukkit.getConsoleSender(), "ingame.shop.fullstackpershift"));
        lavaBucketMeta.setLore(new ArrayList());
        lavaBucket.setItemMeta(lavaBucketMeta);
        if (item.isSimilar(lavaBucket))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    private String getItemName(List<String> list) {
        if (list.size() > 0)
            return list.get(0);
        return "§f";
    }

    private List<String> getItemLore(List<String> list) {
        List<String> lore = new ArrayList<>();
        if (list.size() > 1) {
            lore.addAll(list);
            lore.remove(0);
        }
        return lore;
    }
}

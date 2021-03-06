package us.creepermc.enchants.enchants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.HookManager;
import us.creepermc.enchants.objects.ValueEnchant;
import us.creepermc.enchants.utils.BlockUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutosellEnchant extends ValueEnchant {
	final Map<UUID, Integer> tasks = new HashMap<>();
	final Core core;
	HookManager manager;
	
	public AutosellEnchant(YamlConfiguration config, Core core) {
		super(config, "autosell");
		this.core = core;
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		manager = core.getManager(HookManager.class);
	}
	
	@Override
	public void deinitialize() {
		tasks.values().forEach(core.getServer().getScheduler()::cancelTask);
		tasks.clear();
		manager = null;
	}
	
	@Override
	public void apply(Player player, BlockBreakEvent event, int level) {
		if(tasks.containsKey(player.getUniqueId())) core.getServer().getScheduler().cancelTask(tasks.remove(player.getUniqueId()));
		else core.sendMsg(player, "AUTOSELL_ACTIVATED");
		tasks.put(player.getUniqueId(), new BukkitRunnable() {
			@Override
			public void run() {
				core.sendMsg(player, "AUTOSELL_DEACTIVATED");
				tasks.remove(player.getUniqueId());
			}
		}.runTaskLaterAsynchronously(core, (int) getModifier(level)).getTaskId());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void autosell(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(!tasks.containsKey(player.getUniqueId())) return;
		event.setCancelled(true);
		manager.sellItems(player, event.getBlock().getDrops(player.getItemInHand()));
		Block block = event.getBlock();
		BlockUtil.setBlockInNativeChunkSection(block.getWorld(), block.getX(), block.getY(), block.getZ(), 0, (byte) 0);
		if(manager.isMine(block.getLocation())) manager.sellPlayerMine(manager.getMine(block.getLocation()), player);
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if(!tasks.containsKey(player.getUniqueId())) return;
		core.getServer().getScheduler().cancelTask(tasks.remove(player.getUniqueId()));
	}
	
	@EventHandler
	public void kick(PlayerKickEvent event) {
		Player player = event.getPlayer();
		if(!tasks.containsKey(player.getUniqueId())) return;
		core.getServer().getScheduler().cancelTask(tasks.remove(player.getUniqueId()));
	}
}
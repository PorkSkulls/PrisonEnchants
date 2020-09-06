package us.creepermc.enchants.listeners;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.EnchantsInvManager;
import us.creepermc.enchants.managers.PickaxeManager;
import us.creepermc.enchants.templates.XListener;
import us.creepermc.enchants.utils.Util;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PickaxeListener extends XListener {
	EnchantsInvManager invManager;
	PickaxeManager pickaxeManager;
	
	public PickaxeListener(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		invManager = getCore().getManager(EnchantsInvManager.class);
		pickaxeManager = getCore().getManager(PickaxeManager.class);
	}
	
	@Override
	public void deinitialize() {
		invManager = null;
		pickaxeManager = null;
	}
	
	@EventHandler
	public void pickaxeInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_AIR) return;
		if(event.getItem() == null || !Util.isPickaxe(event.getMaterial())) return;
		Player player = event.getPlayer();
		pickaxeManager.initializePickaxe(player);
		invManager.openInventory(player);
	}
}
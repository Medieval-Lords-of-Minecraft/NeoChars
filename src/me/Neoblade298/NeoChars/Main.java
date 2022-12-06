package me.Neoblade298.NeoChars;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;

import me.Neoblade298.NeoProfessions.Managers.ProfessionManager;
import me.Neoblade298.NeoProfessions.PlayerProfessions.Profession;
import me.Neoblade298.NeoProfessions.PlayerProfessions.ProfessionType;

public class Main extends JavaPlugin implements Listener {

	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoChars Enabled");
		getServer().getPluginManager().registerEvents(this, this);

		// Get command listener
		this.getCommand("char").setExecutor(new Commands(this));
	}

	public void onDisable() {
		Bukkit.getServer().getLogger().info("NeoChars Disabled");
	}

	public void sendPlayerCard(CommandSender recipient, OfflinePlayer viewed) {
		if (SkillAPI.getPlayerData(viewed).getClass("class") == null) {
			sendMessage(recipient, "&cThis player has no class");
			return;
		}

		// Base class
		int pLvl = SkillAPI.getPlayerData(viewed).getClass("class").getLevel();
		String pClass = SkillAPI.getPlayerData(viewed).getClass("class").getData().getName();
		int xp = (int) SkillAPI.getPlayerData(viewed).getClass("class").getExp();
		int reqxp = SkillAPI.getPlayerData(viewed).getClass("class").getRequiredExp();
		PlayerData pData = SkillAPI.getPlayerData(viewed);
		sendMessage(recipient, "&7-- &e" + viewed.getName() + " &6[Lv " + pLvl + " " + pClass + "] &7(" + xp
				+ " / " + reqxp + " XP) --");
		
		// Professions
		HashMap<ProfessionType, Profession> account = ProfessionManager.getAccount(viewed.getUniqueId());
		if (account != null) {
			Profession harv = account.get(ProfessionType.HARVESTER);
			Profession stone = account.get(ProfessionType.STONECUTTER);
			Profession craft = account.get(ProfessionType.CRAFTER);
			Profession log = account.get(ProfessionType.LOGGER);
			String line = "&7-- &6[Lv " + harv.getLevel() + " " + harv.getType().getDisplay() + "] ";
			line += "&6[Lv " + log.getLevel() + " " + log.getType().getDisplay() + "] &7--";
			sendMessage(recipient, line);
			line = "&7-- &6[Lv " + stone.getLevel() + " " + stone.getType().getDisplay() + "] ";
			line += "&6[Lv " + craft.getLevel() + " " + craft.getType().getDisplay() + "] &7--";
			sendMessage(recipient, line);
		}
		
		// Attributes
		String attr = "&e" + pData.getAttribute("Strength") + " &cSTR&7 | &e"
				+ pData.getAttribute("Dexterity") + " &cDEX&7 | &e" + pData.getAttribute("Intelligence")
				+ " &cINT&7 | &e" + pData.getAttribute("Spirit") + " &cSPR&7 | &e" + pData.getAttribute("Endurance") +
				" &cEND&7";
		if (viewed instanceof Player) {
			attr += " | &e" + (int) ((Player) viewed).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + " &cHP";
		}
		sendMessage(recipient, attr);
				
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		// Make sure we're interacting with a player in the quest world
		if (!(e.getRightClicked() instanceof Player)
				|| !(e.getPlayer().getWorld().getName().equalsIgnoreCase("Argyll"))) {
			return;
		}
		Player clicked = (Player) e.getRightClicked();

		// Only let it happen once
		if (!(e.getHand() == EquipmentSlot.HAND)) {
			return;
		}

		// Make sure the player being clicked is not an NPC
		if (clicked.hasMetadata("NPC")) {
			return;
		}

		if (e.getPlayer().isSneaking()) {
			return;
		}

		sendPlayerCard(e.getPlayer(), clicked);
	}

	private void sendMessage(CommandSender s, String m) {
		s.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
	}
}
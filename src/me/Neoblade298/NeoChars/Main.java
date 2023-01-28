package me.Neoblade298.NeoChars;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Main extends JavaPlugin implements Listener {
	private static ArrayList<String> attrs = new ArrayList<String>();
	private static ArrayList<String> onlineAttrs = new ArrayList<String>();
	private static HashMap<String, String> attrKeyToDisplay = new HashMap<String, String>();
	private static final DecimalFormat df = new DecimalFormat("#.##");

	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoChars Enabled");
		getServer().getPluginManager().registerEvents(this, this);

		// Get command listener
		this.getCommand("char").setExecutor(new Commands(this));
		
		attrs.add("Strength");
		attrs.add("Dexterity");
		attrs.add("Intelligence");
		attrs.add("Spirit");
		attrs.add("Endurance");
		onlineAttrs.add("maxhp");
		// onlineAttrs.add("maxmp"); No way to get this via gear atm
		onlineAttrs.add("HealthRegen");
		onlineAttrs.add("ResourceRegen");
		
		attrKeyToDisplay.put("maxhp", "Max HP");
		attrKeyToDisplay.put("maxmp", "Bonus Resource");
		attrKeyToDisplay.put("HealthRegen", "Health Regen");
		attrKeyToDisplay.put("ResourceRegen", "Resource Regen");
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
		
		// Attributes
		String attr = "&e" + pData.getAttribute("Strength") + " &cSTR&7 | &e"
				+ pData.getAttribute("Dexterity") + " &cDEX&7 | &e" + pData.getAttribute("Intelligence")
				+ " &cINT&7 | &e" + pData.getAttribute("Spirit") + " &cSPR&7 | &e" + pData.getAttribute("Endurance") +
				" &cEND&7";
		if (viewed instanceof Player) {
			attr += " | &e" + (int) ((Player) viewed).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + " &cHP";
		}
		sendMessage(recipient, attr);
		
		ComponentBuilder b = new ComponentBuilder("§7[Hover to show complete stats] ")
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getStatsHover(pData, viewed))));
		if (viewed instanceof Player) {
			b.append(new TextComponent("§7[Hover to show professions]"), FormatRetention.NONE)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getProfessionsHover(viewed))));
		}
		recipient.spigot().sendMessage(b.create());
	}
	
	private String getStatsHover(PlayerData data, OfflinePlayer viewed) {
		String hover = "§cFull Stats:";
		for (String attr : attrs) {
			hover += "\n&6" + attr + "&7 - &e" + data.getAttribute(attr);
		}
		if (viewed instanceof Player) {
			for (String attr : onlineAttrs) {
				double effective = 0;
				hover += "\n&6" + attrKeyToDisplay.getOrDefault(attr, attr) + "&7 - &e" + data.getAttribute(attr);
				switch (attr) {
				case "HealthRegen":
					effective = data.getAttribute(attr) * 0.1;
					hover += " &7(&e+" + df.format(effective) + " hp/s&7)";
					break;
				case "ResourceRegen":
					effective = data.getAttribute(attr) * 0.01;
					hover += " &7(&e+" + df.format(effective) + "% r/s&7)";
					break;
				}
			}
		}
		return ChatColor.translateAlternateColorCodes('&', hover);
	}
	
	private String getProfessionsHover(OfflinePlayer viewed) {
		HashMap<ProfessionType, Profession> account = ProfessionManager.getAccount(viewed.getUniqueId());
		String line = "§cProfession Levels:";
		if (account != null) {
			Profession harv = account.get(ProfessionType.HARVESTER);
			Profession stone = account.get(ProfessionType.STONECUTTER);
			Profession craft = account.get(ProfessionType.CRAFTER);
			Profession log = account.get(ProfessionType.LOGGER);
			line += "\n§6Lv " + harv.getLevel() + " " + harv.getType().getDisplay();
			line += "\n§6Lv " + log.getLevel() + " " + log.getType().getDisplay();
			line += "\n§6Lv " + stone.getLevel() + " " + stone.getType().getDisplay();
			line += "\n§6Lv " + craft.getLevel() + " " + craft.getType().getDisplay();
		}
		return line;
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		if (!(e.getRightClicked() instanceof Player)) return;
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
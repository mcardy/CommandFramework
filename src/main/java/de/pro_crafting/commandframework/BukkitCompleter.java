package de.pro_crafting.commandframework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Command Framework - BukkitCompleter <br>
 * An implementation of the TabCompleter class allowing for multiple tab
 * completers per command
 * 
 * @author minnymin3
 * 
 */
public class BukkitCompleter implements TabCompleter {

	private Map<String, Entry<Method, Object>> completers = new HashMap<String, Entry<Method, Object>>();

	public void addCompleter(String label, Method m, Object obj) {
		completers.put(label, new AbstractMap.SimpleEntry<Method, Object>(m, obj));
	}

	@SuppressWarnings("unchecked")
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		for (int i = args.length; i >= 0; i--) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(label.toLowerCase());
			for (int x = 0; x < i; x++) {
				if (!args[x].equals("") && !args[x].equals(" ")) {
					buffer.append("." + args[x].toLowerCase());
				}
			}
			String cmdLabel = buffer.toString();
			if (completers.containsKey(cmdLabel)) {
				Entry<Method, Object> entry = completers.get(cmdLabel);
				try {
					List<String> labelParts = (List<String>) entry.getKey().invoke(entry.getValue(),
							new CommandArgs(sender, command, cmdLabel, args, cmdLabel.split("\\.").length - 1));
					if (labelParts == null || labelParts.size() == 0)
					{
						return null;
					}
					return labelParts;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
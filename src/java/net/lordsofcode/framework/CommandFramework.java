package net.lordsofcode.framework;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

public class CommandFramework {

	private Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
	private CommandMap map;
	private Plugin plugin;

	/**
	 * Initializes the command framework and sets up the command maps
	 * @param plugin
	 */
	public CommandFramework(Plugin plugin) {
		this.plugin = plugin;
		if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
			SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();
			try {
				Field field = SimplePluginManager.class.getDeclaredField("commandMap");
				field.setAccessible(true);
				map = (CommandMap) field.get(manager);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handles commands. Used in the onCommand method in your JavaPlugin class
	 * 
	 * @param sender
	 *            The {@link CommandSender} parsed from onCommand
	 * @param label
	 *            The label parsed from onCommand
	 * @param cmd
	 *            The {@link org.bukkit.command.Command} parsed from onCommand
	 * @param args
	 *            The arguments parsed from onCommand
	 * @return Always returns true for simplicity's sake in onCommand
	 */
	public boolean handleCommand(CommandSender sender, String label, org.bukkit.command.Command cmd, String[] args) {
		for (int i = args.length; i >= 0; i--) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(label);
			for (int x = 0; x < i; x++) {
				buffer.append("." + args[x]);
			}
			String cmdLabel = buffer.toString();
			if (commandMap.containsKey(cmdLabel)) {
				Entry<Method, Object> entry = commandMap.get(cmdLabel);
				Command command = entry.getKey().getAnnotation(Command.class);
				if (!sender.hasPermission(ChatColor.RED + command.permission())) {
					sender.sendMessage(command.noPerm());
					return true;
				}
				try {
					entry.getKey().invoke(entry.getValue(),
							new CommandArgs(sender, cmd, label, args, cmdLabel.split("\\.").length - 1));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
		return true;
	}

	/**
	 * Registers all command methods inside of the object. Similar to Bukkit's
	 * registerEvents method.
	 * 
	 * @param obj
	 *            The object to register the commands of
	 */
	public void registerCommands(Object obj) {
		for (Method m : obj.getClass().getMethods()) {
			if (m.getAnnotation(Command.class) != null) {
				Command command = m.getAnnotation(Command.class);
				if (m.getParameterTypes().length > 1 || m.getParameterTypes()[0] != CommandArgs.class) {
					System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
				}
				Entry<Method, Object> entry = new AbstractMap.SimpleEntry<Method, Object>(m, obj);
				commandMap.put(command.name(), entry);
				String cmdLabel = command.name().split("\\.")[0];
				register(cmdLabel);

				if (!command.description().equalsIgnoreCase("")) {
					map.getCommand(cmdLabel).setDescription(command.description());
				}
				if (!command.usage().equalsIgnoreCase("")) {
					map.getCommand(cmdLabel).setUsage(command.usage());
				}
				
				for (String str : command.aliases()) {
					commandMap.put(str, entry);
					String aliasLabel = str.split("\\.")[0];
					register(aliasLabel);

					if (!command.description().equalsIgnoreCase("")) {
						map.getCommand(aliasLabel).setDescription(command.description());
					}
					if (!command.usage().equalsIgnoreCase("")) {
						map.getCommand(aliasLabel).setUsage(command.usage());
					}
 				}
			}
		}
	}

	private void register(String label) {
		if (map.getCommand(label) == null) {
			org.bukkit.command.Command command = new DummyCommand(label, plugin);
			map.register(plugin.getName(), command);
		}
	}

	private void defaultCommand(CommandArgs args) {
		args.getSender().sendMessage(args.getLabel() + " is not handled! Oh noes!");
	}

}

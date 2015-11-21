package de.pro_crafting.commandframework;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

/**
 * Command Framework - CommandFramework <br>
 * The main command framework class used for controlling the framework.
 * 
 * @author minnymin3
 * 
 */
public class CommandFramework implements CommandExecutor {

	private Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
	private CommandMap map;
	private Plugin plugin;
	private String noPermMessage = null;

	private String inGameOnlyMessage;
	
	/**
	 * Initializes the command framework and sets up the command maps
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
		
		this.inGameOnlyMessage = "This command is only performable in game";
	}

	public String[] getCommandLabels()
	{
		return this.commandMap.keySet().toArray(new String[0]);
	}

	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		return handleCommand(sender, cmd, label, args);
	}
	
	/**
	 * Handles commands. Used in the onCommand method in your JavaPlugin class
	 * 
	 * @param sender The {@link org.bukkit.command.CommandSender} parsed from
	 *            onCommand
	 * @param cmd The {@link org.bukkit.command.Command} parsed from onCommand
	 * @param label The label parsed from onCommand
	 * @param args The arguments parsed from onCommand
	 * @return Always returns true for simplicity's sake in onCommand
	 */
	public boolean handleCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		for (int i = args.length; i >= 0; i--) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(label.toLowerCase());
			for (int x = 0; x < i; x++) {
				buffer.append(".").append(args[x].toLowerCase());
			}
			String cmdLabel = buffer.toString();
			if (commandMap.containsKey(cmdLabel)) {
				Method method = commandMap.get(cmdLabel).getKey();
				Object methodObject = commandMap.get(cmdLabel).getValue();
				Command command = method.getAnnotation(Command.class);
				if (command.permission() != null && !command.permission().isEmpty() && !sender.hasPermission(command.permission())) {
					if(!command.noPerm().equals("You do not have permission to perform that action") || this.noPermMessage == null){
						sender.sendMessage(command.noPerm());
						return true;
					}
					
					sender.sendMessage(this.noPermMessage);
					return true;
					
					
				}
				if (command.inGameOnly() && !(sender instanceof Player)) {
					sender.sendMessage(this.inGameOnlyMessage);
					return true;
				}
				try {
					method.invoke(methodObject, new CommandArgs(sender, cmd, label, args,
							cmdLabel.split("\\.").length - 1));
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
	
	public void setNoPermMessage(String message){
		this.noPermMessage = message;
	}
	
	public String getNoPermMessage(){
		return this.noPermMessage;
	}

	/**
	 * Registers all command and completer methods inside of the object. Similar
	 * to Bukkit's registerEvents method.
	 * 
	 * @param obj The object to register the commands of
	 */
	public void registerCommands(Object obj) {
		for (Method m : obj.getClass().getMethods()) {
			if (m.getAnnotation(Command.class) != null) {
				Command command = m.getAnnotation(Command.class);
				if (m.getParameterTypes().length != 1 || m.getParameterTypes()[0] != CommandArgs.class) {
					plugin.getLogger().warning("Unable to register command " + m.getName() +
							". Unexpected method arguments");
					continue;
				}
				registerCommand(command, command.name(), m, obj);
				for (String alias : command.aliases()) {
					registerCommand(command, alias, m, obj);
				}
			} else if (m.getAnnotation(Completer.class) != null) {
				Completer comp = m.getAnnotation(Completer.class);
				if (m.getParameterTypes().length > 1 || m.getParameterTypes().length == 0 || m.getParameterTypes()[0] != CommandArgs.class) {
					plugin.getLogger().warning("Unable to register tab completer " + m.getName() +
							". Unexpected method arguments");
					continue;
				}
				if (m.getReturnType() != List.class) {
					plugin.getLogger().warning("Unable to register command " + m.getName() +
							". Unexpected return type");
					continue;
				}
				registerCompleter(comp.name(), m, obj);
				for (String alias : comp.aliases()) {
					registerCompleter(alias, m, obj);
				}
			}
		}
	}

	/**
	 * Registers all the commands under the plugin's help
	 */
	public void registerHelp() {
		Set<HelpTopic> help = new TreeSet<HelpTopic>(HelpTopicComparator.helpTopicComparatorInstance());
		for (String s : commandMap.keySet()) {
			if (!s.contains(".")) {
				org.bukkit.command.Command cmd = map.getCommand(s);
				HelpTopic topic = new GenericCommandHelpTopic(cmd);
				help.add(topic);
			}
		}
		IndexHelpTopic topic = new IndexHelpTopic(plugin.getName(), "All commands for " + plugin.getName(), null, help,
				"Below is a list of all " + plugin.getName() + " commands:");
		Bukkit.getServer().getHelpMap().addTopic(topic);
	}

	public void registerCommand(Command command, String label, Method m, Object obj) {
		commandMap.put(label.toLowerCase(), new AbstractMap.SimpleEntry<Method, Object>(m, obj));
		commandMap.put(this.plugin.getName() + ':' + label.toLowerCase(), new AbstractMap.SimpleEntry<Method, Object>(m, obj));
		String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
		if (map.getCommand(cmdLabel) == null) {
			org.bukkit.command.Command cmd = new BukkitCommand(cmdLabel, this, plugin);
			map.register(plugin.getName(), cmd);
		}
		if (!command.description().equalsIgnoreCase("") && cmdLabel.equals(label)) {
			map.getCommand(cmdLabel).setDescription(command.description());
		}
		if (!command.usage().equalsIgnoreCase("") && cmdLabel.equals(label)) {
			map.getCommand(cmdLabel).setUsage(command.usage());
		}
	}

	public void registerCompleter(String label, Method m, Object obj) {
		String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
		if (map.getCommand(cmdLabel) == null) {
			org.bukkit.command.Command command = new BukkitCommand(cmdLabel, this, plugin);
			map.register(plugin.getName(), command);
		}
		if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
			BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);
			if (command.completer == null) {
				command.completer = new BukkitCompleter();
			}
			command.completer.addCompleter(label, m, obj);
		} else if (map.getCommand(cmdLabel) instanceof PluginCommand) {
			try {
				Object command = map.getCommand(cmdLabel);
				Field field = command.getClass().getDeclaredField("completer");
				field.setAccessible(true);
				if (field.get(command) == null) {
					BukkitCompleter completer = new BukkitCompleter();
					completer.addCompleter(label, m, obj);
					field.set(command, completer);
				} else if (field.get(command) instanceof BukkitCompleter) {
					BukkitCompleter completer = (BukkitCompleter) field.get(command);
					completer.addCompleter(label, m, obj);
				} else {
					plugin.getLogger().warning("Unable to register tab completer " + m.getName()
							+ ". A tab completer is already registered for that command!");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void defaultCommand(CommandArgs args) {
		args.getSender().sendMessage(args.getLabel() + " is not handled! Oh noes!");
	}

	public String getInGameOnlyMessage() {
		return this.inGameOnlyMessage;
	}

	public void setInGameOnlyMessage(String inGameOnlyMessage) {
		this.inGameOnlyMessage = inGameOnlyMessage;
	}
	
}

package de.pro_crafting.commandframework;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

/**
 * Command Framework - BukkitCommand <br>
 * An implementation of Bukkit's Command class allowing for registering of
 * commands without plugin.yml
 * 
 * @author minnymin3
 * 
 */
public class BukkitCommand extends org.bukkit.command.Command {

	private final Plugin owningPlugin;
	private CommandExecutor executor;
	protected BukkitCompleter completer;

	/**
	 * A slimmed down PluginCommand
	 * 
	 * @param name
	 * @param owner
	 */
	protected BukkitCommand(String label, Plugin owner) {
		super(label);
		this.executor = owner;
		this.owningPlugin = owner;
		this.usageMessage = "";
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		boolean success = false;

		if (!owningPlugin.isEnabled()) {
			return false;
		}

		if (!testPermission(sender)) {
			return true;
		}

		try {
			success = executor.onCommand(sender, this, commandLabel, args);
		} catch (Throwable ex) {
			throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin "
					+ owningPlugin.getDescription().getFullName(), ex);
		}

		if (!success && usageMessage.length() > 0) {
			for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
				sender.sendMessage(line);
			}
		}

		return success;
	}

	@Override
	public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws CommandException, IllegalArgumentException {
		Validate.notNull(sender, "Sender cannot be null");
		Validate.notNull(args, "Arguments cannot be null");
		Validate.notNull(alias, "Alias cannot be null");

		List<String> completions = null;
		try {
			if (completer != null) {
				completions = completer.onTabComplete(sender, this, alias, args);
			}
			if (completions == null && executor instanceof TabCompleter) {
				completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
			}
		} catch (Throwable ex) {
			StringBuilder message = new StringBuilder();
			message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
			for (String arg : args) {
				message.append(arg).append(' ');
			}
			message.deleteCharAt(message.length() - 1).append("' in plugin ")
					.append(owningPlugin.getDescription().getFullName());
			throw new CommandException(message.toString(), ex);
		}

		if (completions == null) {
			return super.tabComplete(sender, alias, args);
		}
		return completions;
	}

}

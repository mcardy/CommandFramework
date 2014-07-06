package de.pro_crafting.commandframework;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command Framework - CommandArgs <br>
 * This class is passed to the command methods and contains various utilities as
 * well as the command info.
 * 
 * @author minnymin3
 * 
 */
public class CommandArgs {

	private CommandSender sender;
	private org.bukkit.command.Command command;
	private String label;
	private String[] args;

	protected CommandArgs(CommandSender sender, org.bukkit.command.Command command, String label, String[] args,
			int subCommand) {
		String[] modArgs = new String[args.length - subCommand];
		for (int i = 0; i < args.length - subCommand; i++) {
			modArgs[i] = args[i + subCommand];
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append(label);
		for (int x = 0; x < subCommand; x++) {
			buffer.append("." + args[x]);
		}
		String cmdLabel = buffer.toString();
		this.sender = sender;
		this.command = command;
		this.label = cmdLabel;
		this.args = modArgs;
	}

	/**
	 * Gets the command sender
	 * 
	 * @return
	 */
	public CommandSender getSender() {
		return sender;
	}

	/**
	 * Gets the original command object
	 * 
	 * @return
	 */
	public org.bukkit.command.Command getCommand() {
		return command;
	}

	/**
	 * Gets the label including sub command labels of this command
	 * 
	 * @return Something like 'test.subcommand'
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Gets all the arguments after the command's label. ie. if the command
	 * label was test.subcommand and the arguments were subcommand foo foo, it
	 * would only return 'foo foo' because 'subcommand' is part of the command
	 * 
	 * @return
	 */
	public String[] getArgs() {
		return args;
	}

	public boolean isPlayer() {
		return sender instanceof Player;
	}

	public Player getPlayer() {
		if (sender instanceof Player) {
			return (Player) sender;
		} else {
			return null;
		}
	}

}

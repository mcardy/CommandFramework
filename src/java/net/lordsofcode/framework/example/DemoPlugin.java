package net.lordsofcode.framework.example;

import net.lordsofcode.framework.Command;
import net.lordsofcode.framework.CommandArgs;
import net.lordsofcode.framework.CommandFramework;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command Framework - Demo <br>
 * A demo plugin demonstrating how the command framework works
 * 
 * @author minnymin3
 * 
 */
public class DemoPlugin extends JavaPlugin {

	CommandFramework framework;

	public void onEnable() {
		framework = new CommandFramework(this);
		// This will register all the @Command methods in this class. It can be
		// done with any class
		// Note: Commands do not need to be registered in plugin.yml!
		framework.registerCommands(this);
	}

	/**
	 * Just copy and paste this into your main class
	 */
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		return framework.handleCommand(sender, label, command, args);
	}

	/**
	 * An example command. All commands are required to have the argument
	 * CommandArgs and only that argument
	 */
	@Command(name = "test", aliases = { "testing" }, description = "This is a test command", usage = "This is how you use it")
	public void test(CommandArgs args) {
		args.getSender().sendMessage("This is a test command");
	}

	@Command(name = "test.sub", permission = "test.test", aliases = { "test.subcommand", "testing.subcommand",
			"testing.sub" }, noPerm = "You do not have permission to test!")
	public void testSub(CommandArgs args) {
		args.getSender().sendMessage("This is a test subcommand");
	}

}

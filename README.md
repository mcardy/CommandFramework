I created a very lightweight annotation based command system that works similar to Bukkit's events system. It removes the necessity to add the command to your plugin.yml but will still allow you to set descriptions and usages through code.



Example:

    public class DemoPlugin extends JavaPlugin {

        CommandFramework framework;

        public void onEnable() {
            framework = new CommandFramework(this);
            // This will register all the @Command methods in this class. It can be done with any object
            // Note: Commands do not need to be registered in plugin.yml
            framework.registerCommands(this);
            
            // This will set the default error message, when a command is only executable per player
            framework.setInGameOnlyMessage("Der Command muss von einem Spieler ausgeführt werden.");
        }
    
        @Command(name = "test", aliases = { "testing" }, description = "This is a test command", usage = "This is how you use it", inGameOnly=true)
       public void test(CommandArgs args) {
            args.getSender().sendMessage("This is a test command");
        }
    }

This example will create a command called test and register it. It will set the description and usage of the command as well. Notice the aliases option. Aliases will be registered the same way as the regular command and are alternate commands that the method will be invoked with. Here is an example of a sub command:

    @Command(name = "test.sub", aliases = { "test.subcommand"})
    public void testSub(CommandArgs args) {
        args.getSender().sendMessage("This is a test subcommand");
    }

This will create a sub command of test and will be executed when someone sends the command '/test sub' or '/test subcommand'. Descriptions and usages also work with these.

The original Author of this was minnymin3

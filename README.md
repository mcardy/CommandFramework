I created a very lightweight annotation based command system that works similar to Bukkit's events system. It removes the necessity to add the command to your plugin.yml but will still allow you to set descriptions and usages through code.

This framework is now part of a bigger project called the MinezRC Framework. It is a framework that can be shaded into plugins to allow for all in one features from JSON messages to SignInput GUIs to packet receiving/sending. It can be found here: https://git.minezrc.net/minezrc-devs/framework

Example:

    public class DemoPlugin extends JavaPlugin {

        CommandFramework framework;

        public void onEnable() {
            framework = new CommandFramework(this);
            // This will register all the @Command methods in this class. It can be done with any object
            // Note: Commands do not need to be registered in plugin.yml
            framework.registerCommands(this);
        }
    
        @Command(name = "test", aliases = { "testing" }, description = "This is a test command", usage = "This is how you use it")
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

package invoker54.xpshop.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.Level;

public class GenerateCommand {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("xpshop")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .then(Commands.literal("generate")
                                .executes(GenerateCommand::generate))
        );
    }

    private static int generate(CommandContext<CommandSourceStack> commandContext){
        if (ShopGenerationEvent.isRunning.get()){
            LOGGER.error("Auto generation is already running.");
            return 1;
        }
//        if (BoolArgumentType.getBool(commandContext, "reset")){
//
//        }
        Level level = commandContext.getSource().getLevel();
        ShopGenerationEvent.initializeGenerator(level);
        return 1;
    }
}

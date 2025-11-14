package invoker54.xpshop.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.message.OpenShopMenuMsg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.network.PacketDistributor;

public class ShopMenuCommand {
    private static final ModLogger LOGGER = ModLogger.getLogger(ShopMenuCommand.class, XPShop.debugMode);
    private static final int PASS = 1;
    private static final int FAIL = 0;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("xpshop")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes(ShopMenuCommand::openMenu)
        );
    }

    private static int openMenu(CommandContext<CommandSourceStack> commandContext){
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> commandContext.getSource().getPlayer()),
                new OpenShopMenuMsg());
        return PASS;
    }
}

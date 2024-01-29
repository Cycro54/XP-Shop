
package invoker54.xpshop.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.WorldShopCapability;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncClientCapMsg;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RefreshShopCommand {
    public static final Logger LOGGER = LogManager.getLogger();
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("xpshop")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .then(Commands.literal("refresh")
                                .executes(RefreshShopCommand::refreshShop)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(RefreshShopCommand::refreshShop)))
        );
    }

    private static int refreshShop(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity caller;
        try {
            caller = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            ServerWorld level = commandContext.getSource().getLevel();
            if (level == null) return 1;

            WorldShopCapability.getShopCap(ServerLifecycleHooks.getCurrentServer().overworld()).refreshDeals();
            level.getServer().getPlayerList().broadcastMessage(new TranslationTextComponent("xp_shop.chat.shop.refresh.all"), ChatType.CHAT, Util.NIL_UUID);
            return 1;
        }

        if (caller.isDeadOrDying()){
            caller.server.getPlayerList().broadcastMessage(
                    caller.getDisplayName().copy().append(new TranslationTextComponent("xp_shop.chat.shop.cant_open_shop")), ChatType.CHAT, Util.NIL_UUID);
            return 1;
        }

        //Refresh that players stock
        ShopCapability cap = ShopCapability.getShopCap(caller);
        cap.refreshStock(true);
        NetworkHandler.sendToPlayer(caller, new SyncClientCapMsg(cap.writeNBT()));
        caller.sendMessage(new TranslationTextComponent("xp_shop.chat.shop.refresh"), Util.NIL_UUID);
        return 1;
    }
}

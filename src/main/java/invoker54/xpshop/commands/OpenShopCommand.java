package invoker54.xpshop.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.xpshop.client.screen.SellContainer;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.OpenBuyScreenMsg;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

public class OpenShopCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("xpshop")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .then(Commands.literal("buy")
                                .executes(OpenShopCommand::OpenBuyScreen)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(OpenShopCommand::OpenBuyScreen)))
                        .then(Commands.literal("sell")
                                .executes(OpenShopCommand::OpenSellScreen)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(OpenShopCommand::OpenSellScreen)))
        );
    }

    private static int OpenSellScreen(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity caller;
        try {
            caller = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!(commandContext.getSource().getEntity() instanceof PlayerEntity)){
                return 1;
            }
            caller = commandContext.getSource().getPlayerOrException();
        }

        if (caller.isDeadOrDying()){

            caller.server.getPlayerList().broadcastMessage(
                    caller.getDisplayName().copy().append(new TranslationTextComponent("xp_shop.chat.shop.cant_open_shop")), ChatType.CHAT, Util.NIL_UUID);
            return 1;
        }

        //Open the Sell Container
        NetworkHooks.openGui(caller, new SimpleNamedContainerProvider((id, playerInv, player) -> {
            return new SellContainer(id, playerInv, true);
        }, new StringTextComponent("Items To Sell")), (buffer -> buffer.writeBoolean(true)));
        return 1;
    }

    private static int OpenBuyScreen(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity caller;
        try {
            caller = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!(commandContext.getSource().getEntity() instanceof PlayerEntity)){
                return 1;
            }
            caller = commandContext.getSource().getPlayerOrException();
        }

        if (caller.isDeadOrDying()){

            caller.server.getPlayerList().broadcastMessage(
                    caller.getDisplayName().copy().append(new TranslationTextComponent("xp_shop.chat.shop.cant_open_shop")), ChatType.CHAT, Util.NIL_UUID);
            return 1;
        }

        NetworkHandler.sendToPlayer(caller, new OpenBuyScreenMsg());
        return 1;
    }
}

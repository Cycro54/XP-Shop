package invoker54.xpshop.common.init;

import com.mojang.brigadier.CommandDispatcher;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.commands.OpenShopCommand;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class CommandInit {

    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event){
        CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();

        OpenShopCommand.register(commandDispatcher);
    }
}

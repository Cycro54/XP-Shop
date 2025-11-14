package invoker54.xpshop.init;

import com.mojang.brigadier.CommandDispatcher;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.commands.ShopMenuCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class CommandInit {

    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event){
        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();

        ShopMenuCommand.register(commandDispatcher);
    }
}

package invoker54.xpshop.client.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ClientUtil;
import invoker54.xpshop.client.keybinds.CustomKeybind;
import invoker54.xpshop.client.keybinds.KeybindsInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InputEvents {

    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent event){
        onInput();
    }

    @SubscribeEvent
    public static void onMousePress(InputEvent.MouseInputEvent event){
        onInput();
    }

    private static void onInput(){
        if (ClientUtil.mC.level == null) return;

        for (CustomKeybind cKeyBind : KeybindsInit.shopBinds){
            if (cKeyBind.keyBind.isDown()){
                cKeyBind.pressed();
                break;
            }
        }
    }
}

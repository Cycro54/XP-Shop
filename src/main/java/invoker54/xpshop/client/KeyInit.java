package invoker54.xpshop.client;

import invoker54.invocore.client.keybind.CustomKeybind;
import invoker54.invocore.client.keybind.KeybindsInit;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screen.ShopScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = XPShop.MOD_ID)
public class KeyInit {
    public static CustomKeybind shopKey;

    public static void registerKeys(FMLClientSetupEvent event){
        //Open/Close Shop
        shopKey = KeybindsInit.addBind(new CustomKeybind("open_shop", GLFW.GLFW_KEY_GRAVE_ACCENT, XPShop.MOD_ID,
                (action) ->{
                    if (action != GLFW.GLFW_PRESS) return;

                    if (ExtraUtil.mC.screen == null){
                        ExtraUtil.mC.setScreen(new ShopScreen(false));
                    }
                    else if (ExtraUtil.mC.screen instanceof ShopScreen){
                        ExtraUtil.mC.setScreen(null);
                    }
                }));
    }
}

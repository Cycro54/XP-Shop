package invoker54.xpshop.client.keybinds;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ClientUtil;
import invoker54.xpshop.client.screen.ShopScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = XPShop.MOD_ID)
public class KeybindsInit {
    public static final ArrayList<CustomKeybind> shopBinds = new ArrayList<>();
    public static CustomKeybind shopKey;

    public static void registerKeys(FMLClientSetupEvent event){
        //Open/Close Shop
        shopKey = new CustomKeybind("open_shop", GLFW.GLFW_KEY_GRAVE_ACCENT, () ->{
            if (ClientUtil.mC.screen == null){
                ClientUtil.mC.setScreen(new ShopScreen());
            }
            else if (ClientUtil.mC.screen instanceof ShopScreen){
                ClientUtil.mC.setScreen(null);
            }
        });
        shopBinds.add(shopKey);
    }
}

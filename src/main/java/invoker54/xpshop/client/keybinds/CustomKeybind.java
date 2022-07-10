package invoker54.xpshop.client.keybinds;

import invoker54.xpshop.XPShop;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class CustomKeybind {
    public KeyBinding keyBind;
    public IClicked iClicked;

    public CustomKeybind(String name, int key, IClicked iClicked){
        keyBind = new KeyBinding("key." + XPShop.MOD_ID + "." + name, key,"key.category." + XPShop.MOD_ID);
        //keyBind = new KeyBinding(name, key,"XP Shop");
        ClientRegistry.registerKeyBinding(keyBind);
        this.iClicked = iClicked;
    }

    public void pressed(){
        iClicked.onClick();
    }

    public interface IClicked {
        void onClick();
    }
}

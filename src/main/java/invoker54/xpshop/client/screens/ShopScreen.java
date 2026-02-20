package invoker54.xpshop.client.screens;

import invoker54.invocore.client.util.InvoText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ShopScreen extends InvoScreen{
    public ShopScreen(Component pTitle) {
        super(InvoText.literal(pTitle.getString()));
    }
}

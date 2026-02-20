package invoker54.xpshop.client.widgets.property;

import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.buttons.InvoButton;
import invoker54.xpshop.common.data.shops.TabShop;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ShopPropertyWidget extends PropertyWidget{
    public TabShop dataCopy;
    public InvoButton myButton;

    public ShopPropertyWidget(TabShop dataCopy, InvoScreen screen, InvoZone widgetZone, Component pMessage) {
        super(screen, widgetZone);
        this.dataCopy = dataCopy;
//        this.myButton = new InvoButton();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }
}

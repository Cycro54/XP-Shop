package invoker54.xpshop.client.widgets.property;

import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.client.widgets.InvoWidget;
import net.minecraft.network.chat.Component;

public abstract class PropertyWidget extends InvoWidget {
    public PropertyWidget(InvoZone widgetZone, Component pMessage) {
        super(widgetZone, pMessage);
    }
}

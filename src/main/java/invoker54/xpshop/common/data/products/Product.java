package invoker54.xpshop.common.data.products;

import invoker54.invocore.client.util.InvoText;
import invoker54.xpshop.common.data.BasicData;
import invoker54.xpshop.common.data.containers.MapContainer;

import java.util.List;

public abstract class Product extends BasicData {

    @Override
    public String getGeneralType() {
        return Product.class.getSimpleName();
    }

    public abstract boolean canBuy();

    public abstract List<InvoText> getToolTip();

    public abstract void purchase();
}
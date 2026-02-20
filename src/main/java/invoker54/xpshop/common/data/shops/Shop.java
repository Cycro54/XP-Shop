package invoker54.xpshop.common.data.shops;

import invoker54.invocore.client.util.InvoText;
import invoker54.xpshop.common.data.BasicData;

public abstract class Shop extends BasicData {

    @Override
    public String getGeneralType() {
        return Shop.class.getSimpleName();
    }

}
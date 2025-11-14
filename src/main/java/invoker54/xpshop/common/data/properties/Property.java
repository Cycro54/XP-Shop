package invoker54.xpshop.common.data.properties;

import invoker54.xpshop.common.data.BasicData;
import invoker54.xpshop.common.data.products.Product;

public abstract class Property extends BasicData {

    @Override
    public String getGeneralType() {
        return Property.class.getSimpleName();
    }

    public abstract void execute(Product product);
}
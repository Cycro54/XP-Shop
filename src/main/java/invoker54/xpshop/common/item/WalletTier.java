package invoker54.xpshop.common.item;

import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.config.ShopConfig;

public enum WalletTier {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR;

    public int getMax(){
//        if (ShopConfig.walletAmount.isEmpty()) return 0;

        if (ShopConfig.walletAmount.size() > this.ordinal()){
            return ShopConfig.walletAmount.get(this.ordinal());
        }

        else {
            return Integer.MAX_VALUE;
        }
    }
}

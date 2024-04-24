package invoker54.xpshop.item;

import invoker54.xpshop.config.XPShopConfig;

public enum WalletTier {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR;

    public int getMax(){
        switch (this){
            case ZERO -> {
                return XPShopConfig.tierZero;
            }
            case ONE -> {
                return XPShopConfig.tierOne;
            }
            case TWO -> {
                return XPShopConfig.tierTwo;
            }
            case THREE -> {
                return XPShopConfig.tierThree;
            }
            case FOUR -> {
                return XPShopConfig.tierFour;
            }
        }
        return XPShopConfig.tierZero;
    }
}

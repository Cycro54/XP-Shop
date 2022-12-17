package invoker54.xpshop.common.init;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.item.WalletItem;
import invoker54.xpshop.common.item.WalletTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemInit {
    public static ItemGroup myItemGroup = new ItemGroup("xp_shop") {
        public ItemStack makeIcon() {
            return ItemInit.XP_TRADER.getDefaultInstance();
        }
    };

    private static final Logger LOGGER = LogManager.getLogger();
    public static ArrayList<Item> items = new ArrayList<>();

    public static Item addItem(Item item, String name){
        item.setRegistryName(XPShop.MOD_ID, name);
        items.add(item);
        return item;
    }
//    public static final Item WOOD_PAXEL_FAKE = addItem(new Item(getDefault(true)), "utility/wood_paxel_fake");

    //This is for the XP Shop Trader
    public static final Item XP_TRADER = addItem(new Item(getDefault(true)), "xp_trader");
    public static final Item WALLET_1 = addItem(new WalletItem(WalletTier.ONE, getDefault(true)), "wallet_1");
    public static final Item WALLET_2 = addItem(new WalletItem(WalletTier.TWO, getDefault(true)), "wallet_2");
    public static final Item WALLET_3 = addItem(new WalletItem(WalletTier.THREE, getDefault(true)), "wallet_3");
    public static final Item WALLET_4 = addItem(new WalletItem(WalletTier.FOUR, getDefault(true)), "wallet_4");
    public static final Item UPGRADE_OPTIONS = addItem(new Item(getDefault(true)), "upgrade_options");
    public static final Item UPGRADE_UNIVERSAL_BUY = addItem(new Item(getDefault(true)), "upgrade_universal_buy");
    public static final Item UPGRADE_UNIVERSAL_SELL = addItem(new Item(getDefault(true)), "upgrade_universal_sell");
    public static final Item UPGRADE_TRANSFER = addItem(new Item(getDefault(true)), "upgrade_transfer");
    public static final Item UPGRADE_FEE = addItem(new Item(getDefault(true)), "upgrade_fee");

    //TODO: Place this in the mod
//    public static final Item UPGRADE_WEALTHY = addItem(new Item(getDefault(true)), "upgrade_wealthy");

    public static Item.Properties getDefault(boolean placeInGroup) {
        if (!placeInGroup) return new Item.Properties();
        
        return new Item.Properties().tab(myItemGroup);
    }

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> itemRegistryEvent){
        IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();
        for (Item item: items){
            registry.register(item);
        }
    }
}

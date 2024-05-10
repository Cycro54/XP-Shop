package invoker54.xpshop.init;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.item.WalletItem;
import invoker54.xpshop.item.WalletTier;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemInit {
    // Create a Deferred Register to hold Items which will all be registered under the "xp_shop" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, XPShop.MOD_ID);

    //Regular item
    public static RegistryObject<Item> XP_TRADER = ITEMS.register("xp_trader",()-> new Item(defaultProp()));

    //Wallets
    public static final RegistryObject<Item> WALLET_1 = ITEMS.register("wallet_1",()-> new WalletItem(WalletTier.ONE, defaultProp()));
    public static final RegistryObject<Item> WALLET_2 = ITEMS.register("wallet_2",()-> new WalletItem(WalletTier.TWO, defaultProp()));
    public static final RegistryObject<Item> WALLET_3 = ITEMS.register("wallet_3",()-> new WalletItem(WalletTier.THREE, defaultProp()));
    public static final RegistryObject<Item> WALLET_4 = ITEMS.register("wallet_4",()-> new WalletItem(WalletTier.FOUR, defaultProp()));

    private static Item.Properties defaultProp() {
        return new Item.Properties().tab(XPShop.itemGroup);
    }
}

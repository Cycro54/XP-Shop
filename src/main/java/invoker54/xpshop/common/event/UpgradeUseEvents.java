package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.init.ItemInit;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncClientCapMsg;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class UpgradeUseEvents {

    @SubscribeEvent
    public static void useOptionsUpgrade(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled()) return;
        if (event.getWorld().isClientSide) return;
        ItemStack itemStack = event.getItemStack();

        if (itemStack.getItem() != ItemInit.UPGRADE_OPTIONS) return;

        ShopCapability playerCap = ShopCapability.getShopCap(event.getPlayer());
        if (playerCap.optionUpgrade) {
            event.getPlayer().displayClientMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), false);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.optionUpgrade = true;

        //finally update the clients shop capability
        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientCapMsg(playerCap.writeNBT()));
    }

    @SubscribeEvent
    public static void useBuyUpgrade(PlayerInteractEvent.RightClickItem event){
        if (event.isCanceled()) return;
        if (event.getWorld().isClientSide) return;
        ItemStack itemStack = event.getItemStack();

        if (itemStack.getItem() != ItemInit.UPGRADE_UNIVERSAL_BUY) return;

        ShopCapability playerCap = ShopCapability.getShopCap(event.getPlayer());
        if (playerCap.buyUpgrade) {
            event.getPlayer().displayClientMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), false);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.buyUpgrade = true;

        //finally update the clients shop capability
        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientCapMsg(playerCap.writeNBT()));
    }

    @SubscribeEvent
    public static void useSellUpgrade(PlayerInteractEvent.RightClickItem event){
        if (event.isCanceled()) return;
        if (event.getWorld().isClientSide) return;
        ItemStack itemStack = event.getItemStack();

        if (itemStack.getItem() != ItemInit.UPGRADE_UNIVERSAL_SELL) return;

        ShopCapability playerCap = ShopCapability.getShopCap(event.getPlayer());
        if (playerCap.sellUpgrade) {
            event.getPlayer().displayClientMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), false);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.sellUpgrade = true;

        //finally update the clients shop capability
        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientCapMsg(playerCap.writeNBT()));
    }

    @SubscribeEvent
    public static void useTransferUpgrade(PlayerInteractEvent.RightClickItem event){
        if (event.isCanceled()) return;
        if (event.getWorld().isClientSide) return;
        ItemStack itemStack = event.getItemStack();

        if (itemStack.getItem() != ItemInit.UPGRADE_TRANSFER) return;

        ShopCapability playerCap = ShopCapability.getShopCap(event.getPlayer());
        if (playerCap.transferUpgrade) {
            event.getPlayer().displayClientMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), false);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.transferUpgrade = true;

        //finally update the clients shop capability
        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientCapMsg(playerCap.writeNBT()));
    }

    //TODO: Place this in the mod
//    @SubscribeEvent
//    public static void useWealthyUpgrade(PlayerInteractEvent.RightClickItem event){
//        if (event.isCanceled()) return;
//        if (event.getWorld().isClientSide) return;
//        ItemStack itemStack = event.getItemStack();
//
//        if (itemStack.getItem() != ItemInit.UPGRADE_WEALTHY) return;
//
//        ShopCapability playerCap = ShopCapability.getShopCap(event.getPlayer());
//        if (playerCap.wealthyUpgrade) {
//            event.getPlayer().displayClientMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), false);
//            return;
//        }
//
//        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
//        playerCap.wealthyUpgrade = true;
//
//        //finally update the clients shop capability
//        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientCapMsg(playerCap.writeNBT()));
//    }
}

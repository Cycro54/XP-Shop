package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.init.ItemInit;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncClientCapMsg;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
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
        if (playerCap == null) return;
        if (playerCap.optionUpgrade) {
            event.getPlayer().sendMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), Util.NIL_UUID);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.optionUpgrade = true;
        event.getPlayer().sendMessage(
                new TranslationTextComponent("xp_shop.chat.unlock.upgrade_options"), Util.NIL_UUID);

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
        if (playerCap == null) return;
        if (playerCap.buyUpgrade) {
            event.getPlayer().sendMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), Util.NIL_UUID);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.buyUpgrade = true;
        event.getPlayer().sendMessage(
                new TranslationTextComponent("xp_shop.chat.unlock.upgrade_universal_buy"), Util.NIL_UUID);

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
        if (playerCap == null) return;
        if (playerCap.sellUpgrade) {
            event.getPlayer().sendMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), Util.NIL_UUID);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.sellUpgrade = true;
        event.getPlayer().sendMessage(
                new TranslationTextComponent("xp_shop.chat.unlock.upgrade_universal_sell"), Util.NIL_UUID);

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
        if (playerCap == null) return;
        if (playerCap.transferUpgrade) {
            event.getPlayer().sendMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), Util.NIL_UUID);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.transferUpgrade = true;
        event.getPlayer().sendMessage(
                new TranslationTextComponent("xp_shop.chat.unlock.upgrade_transfer"), Util.NIL_UUID);

        //finally update the clients shop capability
        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientCapMsg(playerCap.writeNBT()));
    }

    @SubscribeEvent
    public static void useFeeUpgrade(PlayerInteractEvent.RightClickItem event){
        if (event.isCanceled()) return;
        if (event.getWorld().isClientSide) return;
        ItemStack itemStack = event.getItemStack();

        if (itemStack.getItem() != ItemInit.UPGRADE_FEE) return;

        ShopCapability playerCap = ShopCapability.getShopCap(event.getPlayer());
        if (playerCap == null) return;
        if (playerCap.feeUpgrade) {
            event.getPlayer().sendMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), Util.NIL_UUID);
            return;
        }

        if (!event.getPlayer().isCreative()) itemStack.shrink(1);
        playerCap.feeUpgrade = true;
        event.getPlayer().sendMessage(
                new TranslationTextComponent("xp_shop.chat.unlock.upgrade_fee"), Util.NIL_UUID);

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
//            event.getPlayer().sendMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), Util.NIL_UUID);
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

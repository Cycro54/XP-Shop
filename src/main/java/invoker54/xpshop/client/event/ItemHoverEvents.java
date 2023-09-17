package invoker54.xpshop.client.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.init.ItemInit;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, value = Dist.CLIENT)
public class ItemHoverEvents {
    public static IFormattableTextComponent getUnlockTXT(){
        return new TranslationTextComponent("item.xp_shop.unlocked")
                .withStyle(TextFormatting.BOLD).withStyle(TextFormatting.GREEN);
    }
    
    @SubscribeEvent
    public static void hoverOptions(ItemTooltipEvent event){
        if (ClientUtil.getPlayer() == null) return;
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_OPTIONS) return;
        ShopCapability playerCap = ShopCapability.getShopCap(ClientUtil.getPlayer());
        if (playerCap == null) return;

        if (ShopCapability.getShopCap(ClientUtil.getPlayer()).optionUpgrade) {
            event.getToolTip().add(getUnlockTXT());
        }
        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_options"));
    }

    @SubscribeEvent
    public static void hoverBuy(ItemTooltipEvent event){
        if (ClientUtil.getPlayer() == null) return;
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_UNIVERSAL_BUY) return;
        ShopCapability playerCap = ShopCapability.getShopCap(ClientUtil.getPlayer());
        if (playerCap == null) return;

        if (ShopCapability.getShopCap(ClientUtil.getPlayer()).buyUpgrade) {
            event.getToolTip().add(getUnlockTXT());
        }
        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_universal_buy"));
    }

    @SubscribeEvent
    public static void hoverSell(ItemTooltipEvent event){
        if (ClientUtil.getPlayer() == null) return;
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_UNIVERSAL_SELL) return;
        ShopCapability playerCap = ShopCapability.getShopCap(ClientUtil.getPlayer());
        if (playerCap == null) return;


        if (ShopCapability.getShopCap(ClientUtil.getPlayer()).sellUpgrade) {
            event.getToolTip().add(getUnlockTXT());
        }
        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_universal_sell"));
    }

    @SubscribeEvent
    public static void hoverTransfer(ItemTooltipEvent event){
        if (ClientUtil.getPlayer() == null) return;
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_TRANSFER) return;
        ShopCapability playerCap = ShopCapability.getShopCap(ClientUtil.getPlayer());
        if (playerCap == null) return;

        if (ShopCapability.getShopCap(ClientUtil.getPlayer()).transferUpgrade) {
            event.getToolTip().add(getUnlockTXT());
        }
        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_transfer"));
    }

    @SubscribeEvent
    public static void hoverFee(ItemTooltipEvent event){
        if (ClientUtil.getPlayer() == null) return;
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_FEE) return;
        ShopCapability playerCap = ShopCapability.getShopCap(ClientUtil.getPlayer());
        if (playerCap == null) return;

        if (ShopCapability.getShopCap(ClientUtil.getPlayer()).feeUpgrade) {
            event.getToolTip().add(getUnlockTXT());
        }
        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_fee"));
    }

    //TODO: Place this in the mod

//    @SubscribeEvent
//    public static void hoverWealthy(ItemTooltipEvent event){
//        if (event.getItemStack().getItem() != ItemInit.UPGRADE_WEALTHY) return;
//
//        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_wealthy"));
//    }
}

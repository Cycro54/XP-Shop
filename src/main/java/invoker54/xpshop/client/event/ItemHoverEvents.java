package invoker54.xpshop.client.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.init.ItemInit;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, value = Dist.CLIENT)
public class ItemHoverEvents {

    @SubscribeEvent
    public static void hoverOptions(ItemTooltipEvent event){
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_OPTIONS) return;

        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_options"));
    }

    @SubscribeEvent
    public static void hoverBuy(ItemTooltipEvent event){
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_UNIVERSAL_BUY) return;

        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_universal_buy"));
    }

    @SubscribeEvent
    public static void hoverSell(ItemTooltipEvent event){
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_UNIVERSAL_SELL) return;

        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_universal_sell"));
    }

    @SubscribeEvent
    public static void hoverTransfer(ItemTooltipEvent event){
        if (event.getItemStack().getItem() != ItemInit.UPGRADE_TRANSFER) return;

        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_transfer"));
    }

    //TODO: Place this in the mod

//    @SubscribeEvent
//    public static void hoverWealthy(ItemTooltipEvent event){
//        if (event.getItemStack().getItem() != ItemInit.UPGRADE_WEALTHY) return;
//
//        event.getToolTip().add(new TranslationTextComponent("xp_shop.desc.upgrade_wealthy"));
//    }
}

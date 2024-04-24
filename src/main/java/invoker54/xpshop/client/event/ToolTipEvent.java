package invoker54.xpshop.client.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationCopyEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, value = Dist.CLIENT)
public class ToolTipEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onToolTip(ItemTooltipEvent event){
        if (true)return;
        PriceList priceList = ShopGenerationCopyEvent.getPriceList(event.getItemStack());
        if (priceList.getFullPrice(false) != 0){
            event.getToolTip().addAll(priceList.getTooltip(Screen.hasShiftDown()));
            if (ShopGenerationCopyEvent.isValidEntry(event.getItemStack())){
                event.getToolTip().add(Component.literal("VALID ENTRY").withStyle(ChatFormatting.GREEN));
            }
            else {
                event.getToolTip().add(Component.literal("BAD ENTRY").withStyle(ChatFormatting.RED));
            }
        }

        if (ShopGenerationCopyEvent.getMatchingItemStack(event.getItemStack(), ShopGenerationCopyEvent.blackListedItems) != null){
            event.getToolTip().add(Component.literal("BANNED").withStyle(ChatFormatting.DARK_RED));
        }
    }
}

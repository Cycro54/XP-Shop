package invoker54.xpshop.common.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.WorldShopCapability;
import invoker54.xpshop.common.init.ItemInit;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.OpenSellContainerMsg;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class XPTraderEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    //This is for if the player clicks on a wandering trader
    @SubscribeEvent
    public static void clickOnWanderer(PlayerInteractEvent.EntityInteract event){
        if (event.isCanceled()) return;
        ItemStack stack = event.getPlayer().getMainHandItem();
        if (stack.isEmpty()) return;

        if (!(event.getTarget() instanceof WanderingTraderEntity)) return;
        boolean flag = (stack.getItem() == ItemInit.XP_TRADER);

        //If they have the limited XP Trader, they may only use parts of the shop
        if (event.getWorld().isClientSide) {
            if (stack.getItem() == ItemInit.XP_TRADER) {
                ExtraUtil.openShop(true);
            }
        }
        if (flag){
            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);
        }
    }

    //This is for if the player clicks on a villager
    @SubscribeEvent
    public static void clickOnVillager(PlayerInteractEvent.EntityInteract event){
        if (event.isCanceled()) return;
        ItemStack stack = event.getPlayer().getMainHandItem();
        if (stack.isEmpty()) return;

        if (!(event.getTarget() instanceof VillagerEntity) || (event.getTarget() instanceof WanderingTraderEntity)) return;
        boolean flag = (stack.getItem() == ItemInit.XP_TRADER);

        if (event.getWorld().isClientSide) {
            if (stack.getItem() == ItemInit.XP_TRADER) {
                ExtraUtil.openShopFee(true);
            }
        }
        if (flag){
            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);
        }
    }

    //This is for if the player clicks on nothing with a XPTrader in hand
    @SubscribeEvent
    public static void clickOnEmpty(PlayerInteractEvent.RightClickItem event){
        if (!event.getWorld().isClientSide) return;
        if (event.isCanceled()) return;
        if (!(event.getItemStack().getItem() == ItemInit.XP_TRADER)) return;

        //If they don't have universal sell/buy, return.
        ShopCapability cap = ShopCapability.getShopCap(ClientUtil.getPlayer());
        if (!cap.buyUpgrade && !cap.sellUpgrade && !ExtraUtil.getPlayer().isCreative()) return;

        //Open the fee screen if they haven't paid the fee already
        ExtraUtil.openShopFee(false);
    }

    //This is for if the player clicks on a wandering trader
    @SubscribeEvent
    public static void clickOnPlayer(PlayerInteractEvent.EntityInteract event){
        if (event.isCanceled()) return;
        ItemStack stack = event.getPlayer().getMainHandItem();
        if (stack.isEmpty()) return;

        if (!(event.getTarget() instanceof PlayerEntity)) return;
        boolean flag = (stack.getItem() == ItemInit.XP_TRADER);
        if (!ShopCapability.getShopCap(event.getPlayer()).transferUpgrade) return;

        //If they have the limited XP Trader, they may only use parts of the shop
        if (event.getWorld().isClientSide) {
            if (stack.getItem() == ItemInit.XP_TRADER) {
                ExtraUtil.openXPTransfer(event.getTarget().getId());
            }
        }
        if (flag){
            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);
        }
    }
}

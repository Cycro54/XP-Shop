package invoker54.xpshop.common.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.WorldShopCapability;
import invoker54.xpshop.common.init.ItemInit;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.OpenSellContainerMsg;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
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
                WorldShopCapability cap = WorldShopCapability.getShopCap(ClientUtil.getWorld());
                ExtraUtil.openShop(cap.getBuyEntries(event.getPlayer()), true);
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

        //Grab their cap
        ShopCapability cap = ShopCapability.getShopCap(event.getPlayer());

        //Now see if they have the universal sell or buy upgrade (buy first)
        if (cap.buyUpgrade || ExtraUtil.mC.player.isCreative()){
            WorldShopCapability worldCap = WorldShopCapability.getShopCap(ClientUtil.getWorld());
            ExtraUtil.openShop(worldCap.getBuyEntries(event.getPlayer()), false);
        }

        else if (cap.sellUpgrade){
            XPShop.LOGGER.debug("WILL THIS OPEN CREATIVE MENU?: " + (ExtraUtil.mC.player.isCreative()));
            NetworkHandler.INSTANCE.sendToServer(new OpenSellContainerMsg(false));
        }
    }

    //This is for if the player clicks on a wandering trader
    @SubscribeEvent
    public static void clickOnPlayer(PlayerInteractEvent.EntityInteract event){
        if (event.isCanceled()) return;
        ItemStack stack = event.getPlayer().getMainHandItem();
        if (stack.isEmpty()) return;

        if (!(event.getTarget() instanceof PlayerEntity)) return;
        boolean flag = (stack.getItem() == ItemInit.XP_TRADER);

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

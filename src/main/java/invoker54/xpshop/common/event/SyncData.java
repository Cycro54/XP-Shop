package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.ShopProvider;
import invoker54.xpshop.common.api.WanderShopProvider;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncClientCapMsg;
import invoker54.xpshop.common.network.msg.SyncClientShopMsg;
import invoker54.xpshop.common.network.msg.SyncServerShopMsg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class SyncData {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void attachPlayerCaps(AttachCapabilitiesEvent<Entity> event){

        if (event.getObject() instanceof PlayerEntity){
            event.addCapability(XPShop.XPSHOP_LOC,new ShopProvider(event.getObject().level));
        }

    }

    @SubscribeEvent
    public static void attachWanderVillagerCaps(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof WanderingTraderEntity){
            event.addCapability(XPShop.XPSHOP_LOC,new WanderShopProvider(event.getObject().level));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.Clone event){
        if (!event.isWasDeath()) return;

        ShopCapability oldCap = ShopCapability.getShopCap(event.getOriginal());
        ShopCapability newCap = ShopCapability.getShopCap(event.getPlayer());

        newCap.readNBT(oldCap.writeNBT());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        //Sync shop
        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientShopMsg(ShopData.serialize()));
        //Now give player cap data
        NetworkHandler.sendToPlayer(event.getPlayer(),
                new SyncClientCapMsg(ShopCapability.getShopCap(event.getPlayer()).writeNBT()));
    }

    @SubscribeEvent
    public static void onLogOut(ClientPlayerNetworkEvent.LoggedOutEvent event){
        if (event.getNetworkManager() == null) return;

        NetworkHandler.INSTANCE.sendToServer(
                new SyncServerShopMsg(ShopCapability.getShopCap(event.getPlayer()).writeNBT()));
    }

    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event){
        if (((ServerWorld)event.getWorld()).dimension() != World.OVERWORLD) return;
        LOGGER.debug("Saving shop data");
        ShopData.writeFile();
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event){
        if(event.getWorld().isClientSide()) return;

        if(((ServerWorld)event.getWorld()).dimension() == World.OVERWORLD) {
            LOGGER.debug("Reading shop data");
            ShopData.readFile();
        }
    }
}

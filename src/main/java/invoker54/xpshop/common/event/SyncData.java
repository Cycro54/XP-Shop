package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.ShopProvider;
import invoker54.xpshop.common.api.WorldShopProvider;
import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncClientCapMsg;
import invoker54.xpshop.common.network.msg.SyncClientShopMsg;
import invoker54.xpshop.common.network.msg.SyncConfigMsg;
import invoker54.xpshop.common.network.msg.SyncWorldShopRequestMsg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
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
    public static void attachWorldCaps(AttachCapabilitiesEvent<World> event){
//        if (event.getObject().dimension().equals(World.OVERWORLD)){
//            event.addCapability(XPShop.XPSHOP_LOC,new WanderShopProvider(event.getObject()));
//        }
        event.addCapability(XPShop.XPSHOP_LOC,new WorldShopProvider(event.getObject()));
    }

    @SubscribeEvent
    public static void trackPlayer(EntityJoinWorldEvent event){
        if (event.isCanceled()) return;
        Entity joinEntity = event.getEntity();
        if (!(joinEntity instanceof PlayerEntity)) return;

//        LOGGER.error("FOUND A PLAYER ");
        ShopCapability playerCap = ShopCapability.getShopCap((LivingEntity) joinEntity);
        if (playerCap == null){
            throw new NullPointerException();
        }
        //This is for asking for the world shop
        if (event.getWorld().isClientSide){
            NetworkHandler.sendToServer(new SyncWorldShopRequestMsg(joinEntity.level.dimension().getRegistryName()));
        }
        //This is for asking for the players shop data (like leftover xp or wallet upgrade
        else {
            NetworkHandler.sendToPlayer((PlayerEntity) joinEntity,
                    new SyncClientCapMsg(playerCap.writeNBT()));
        }
        if (!event.getWorld().isClientSide) return;
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

        //Also sync config
        ShopConfig.bakeCommonConfig();
        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncConfigMsg(ShopConfig.serialize()));

        //Now give player cap data
        NetworkHandler.sendToPlayer(event.getPlayer(),
                new SyncClientCapMsg(ShopCapability.getShopCap(event.getPlayer()).writeNBT()));
    }

    @SubscribeEvent
    public static void onUpdateConfig(TickEvent.ServerTickEvent event){
        if (event.type == TickEvent.Type.CLIENT) return;
        if (event.phase == TickEvent.Phase.START) return;
        if (ShopConfig.isDirty()){
            //Then finally send the config data to all players
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncConfigMsg(ShopConfig.serialize()));

            ShopConfig.markDirty(false);
        }
    }

//    @SubscribeEvent
//    public static void onLogOut(ClientPlayerNetworkEvent.LoggedOutEvent event){
//        if (event.getNetworkManager() == null) return;
//
//        NetworkHandler.sendToServer(
//                new SyncServerShopMsg(ShopCapability.getShopCap(event.getPlayer()).writeNBT()));
//    }

    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event){
        if (((ServerWorld)event.getWorld()).dimension() != World.OVERWORLD) return;
//        LOGGER.debug("Saving shop data");
        ShopData.writeFile();
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event){
        if(event.getWorld().isClientSide()) return;

        if(((ServerWorld)event.getWorld()).dimension() == World.OVERWORLD) {
//            LOGGER.debug("Reading shop data");
            ShopData.readFile();
        }
    }
}

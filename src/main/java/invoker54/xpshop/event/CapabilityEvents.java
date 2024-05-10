package invoker54.xpshop.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.api.PlayerShopProvider;
import invoker54.xpshop.api.WorldShopProvider;
import invoker54.xpshop.capability.WorldShopCapability;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class CapabilityEvents {

    // Directly reference a slf4j logger
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);

    @SubscribeEvent
    public static void attachWorldCap(net.minecraftforge.event.AttachCapabilitiesEvent<Level> event) {
        if (event.getObject().dimension() == Level.OVERWORLD) {
            event.addCapability(WorldShopProvider.WORLD_DATA_LOCATION, new WorldShopProvider());
        }
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event){
        if (!(event.getLevel() instanceof Level)) return;
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (((Level)event.getLevel()).dimension() != Level.OVERWORLD) return;
        LOGGER.debug("RESETTING WORLD SHOP INFO");
        WorldShopCapability.classShopMap.clear();
        WorldShopCapability.shopEntryMap.clear();
        WorldShopCapability.categoryEntryMap.clear();
        WorldShopCapability.itemEntryMap.clear();
    }

    @SubscribeEvent
    public static void attachPlayerCap(net.minecraftforge.event.AttachCapabilitiesEvent<Player> event){
        event.addCapability(PlayerShopProvider.PLAYER_DATA_LOCATION, new PlayerShopProvider(event.getObject()));
    }

    @SubscribeEvent
    public static void syncOnPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        if (ShopGenerationEvent.isRunning.get()) return;
        WorldShopCapability.syncInitialCapToClient(event.getEntity());
    }

//    @SubscribeEvent
//    public static void onTick(TickEvent.PlayerTickEvent event){
//        if (event.phase == TickEvent.Phase.END) return;
//        if (event.side == LogicalSide.CLIENT) return;
//
//        ServerLevel level = (ServerLevel) event.player.level;
//
//        long count = getBlockPositions(level, event.player, Optional.of(OrePlacements.ORE_DIAMOND_LARGE.get())).count();
//        if (count != 0) LOGGER.info("Diamond size: " + count);
//        count = getBlockPositions(level, event.player, Optional.of(OrePlacements.ORE_IRON_UPPER.get())).count();
//        if (count != 0) LOGGER.info("Iron size: " + count);
//        count = getBlockPositions(level, event.player, Optional.of(OrePlacements.ORE_COAL_UPPER.get())).count();
//        if (count != 0) LOGGER.info("Coal size: " + count);
//        count = getBlockPositions(level, event.player, Optional.of(OrePlacements.ORE_COPPER_LARGE.get())).count();
//        if (count != 0) LOGGER.info("Copper size: " + count);
//    }

//    public static Stream<BlockPos> getBlockPositions(ServerLevel level, Player player, Optional<PlacedFeature> optional){
//        ArrayList<BlockPos> posList = new ArrayList<>();
//        for (int x = (int) player.position().x - 16; x < player.position().x; x++){
//            for (int y = 0; y < player.position().y; y++){
//                for (int z = (int) player.position().z - 16; z < player.position().z; z++){
//                    posList.add(new BlockPos(x,y,z));
//                }
//            }
//        }
//        Stream<BlockPos> posStream = posList.stream();
//
//        PlacementContext context = new PlacementContext(level, level.getChunkSource().getGenerator(), optional);
//        if (optional.isEmpty()){
//            LOGGER.error("Optional was empty, it failed!");
//            return posStream;
//        }
//
//        for(PlacementModifier placementmodifier : optional.get().placement()) {
//            posStream = posStream.flatMap((blockPos) -> {
//                return placementmodifier.getPositions(context, level.random, blockPos);
//            });
//        }
//
//        return posStream;
//    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event){
        if (!event.isWasDeath()) return;
        if (event.isCanceled()) return;


    }
}

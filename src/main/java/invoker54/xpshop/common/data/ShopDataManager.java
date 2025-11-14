package invoker54.xpshop.common.data;

import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.containers.AbstractContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class ShopDataManager implements INBTSerializable<CompoundTag> {
    private static final ModLogger LOGGER = ModLogger.getLogger(ShopDataManager.class, XPShop.debugMode);
    public static final String SPECIFIC_TYPE = "SPECIFIC_TYPE";
    public static final String GENERAL_TYPE = "GENERAL_TYPE";

    private static boolean buildingShop = false;

    private static final Map<UUID, BasicData> dataMap = new HashMap<>();
    //This will be used by the Player capability and List containers
    private static final Map<String, AbstractContainer> specificTypeMap = new HashMap<>();
    //This will be used for things like selecting a Shop type, choosing a property, etc.
    private static final Map<String, List<AbstractContainer>> generalTypeMap = new HashMap<>();

    public static AbstractContainer getType(CompoundTag tag){
        AbstractContainer container = getType(tag.getString(SPECIFIC_TYPE));
        container.deserializeNBT(tag);
        return container;
    }

    public static AbstractContainer getType(String specificType){
        AbstractContainer instance = specificTypeMap.getOrDefault(specificType, null);
        //TODO: REMOVE THIS LATER TO MAKE THE GAME NOT CRASH
        if (instance == null) throw new NullPointerException("[XP SHOP] Specific type" + "'"+specificType+"'"+ " is missing!");
        return instance.copy();
    }

    public static <R extends AbstractContainer> R getType(Class<R> classType){
        AbstractContainer container = getType(classType.getSimpleName());
        return classType.cast(container);
    }

    public static <R extends AbstractContainer> List<R> getTypes(Class<R> classType){
        List<AbstractContainer> instance = generalTypeMap.getOrDefault(classType.getSimpleName(), null);
        //TODO: REMOVE THIS LATER TO MAKE THE GAME NOT CRASH
        if (instance == null) throw new NullPointerException("[XP SHOP] General type" + "'"+classType.getSimpleName()+"'"+ " is missing!");
        List<R> newList = new ArrayList<>();
        for (AbstractContainer container : instance){
            if (!classType.isInstance(container)) continue;
            newList.add(classType.cast(container));
        }
        return newList;
    }

    public static <R extends AbstractContainer> void addType(R instance){
        if (specificTypeMap.containsKey(instance.getSpecificType())) throw new RuntimeException("[XP SHOP] Specific type" + "'"+instance.getSpecificType()+"'"+ " already exists!");
        specificTypeMap.put(instance.getSpecificType(), instance);
        generalTypeMap.putIfAbsent(instance.getGeneralType(), new ArrayList<>());
        generalTypeMap.get(instance.getGeneralType()).add(instance);
    }

    public static void removeData(UUID id){
        dataMap.remove(id);
    }

    public static void addData(UUID id, CompoundTag dataTag){
        AbstractContainer dataType = getType(dataTag);
        if (!(dataType instanceof BasicData)){
            LOGGER.error("[XP SHOP] Data received is not of type BasicData \n" +
                    "Type: " + dataType.getSpecificType() + " \n" + dataTag);
            return;
        }
        BasicData actualData = (BasicData) dataType;
        actualData.deserializeNBT(dataTag);
        dataMap.put(id, actualData);
    }

    public static BasicData getDataByID(UUID id){
        return dataMap.get(id);
    }

    public static <R extends BasicData> List<R> getDataListByType(Class<R> classType){
        return dataMap.values().stream().filter(classType::isInstance).map(classType::cast).collect(Collectors.toList());
    }

    public static void clearAllData(){
        dataMap.clear();
    }

    public static boolean isBuildingShop(){
        return buildingShop;
    }

    public static void setBuildingMode(boolean isBuilding){
        buildingShop = isBuilding;
    }

    public static void writeFiles(){

    }

    public static void readFiles(){

    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }

}
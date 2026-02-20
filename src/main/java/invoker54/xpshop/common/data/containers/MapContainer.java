package invoker54.xpshop.common.data.containers;

import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MapContainer extends AbstractContainer {
    public static final ModLogger LOGGER = ModLogger.getLogger(MapContainer.class, XPShop.debugMode);
    public final Map<String, AbstractContainer> saveMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <R extends AbstractContainer> R save(String id, @NotNull R instance){
        this.saveMap.put(id, instance.copy());
        AbstractContainer value = this.saveMap.get(id);
        try {
            return (R) value.copy();
        }
        catch (Exception e){
            e.printStackTrace();
            LOGGER.error("[XP SHOP] [MAP CONTAINER] Can't cast " + value.getClass() + " to " + instance.getClass().getSimpleName());
            throw new ClassCastException();
        }
    }

    public boolean remove(String id){
        return (this.saveMap.remove(id) != null);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag mainTag = new CompoundTag();
        this.saveMap.forEach((key, value) -> mainTag.put(key, value.serializeNBT()));
        return mainTag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        List<String> badKeys = new ArrayList<>(this.saveMap.keySet());
        badKeys.removeAll(tag.getAllKeys());
        badKeys.forEach(this.saveMap::remove);

        tag.getAllKeys().forEach(key -> this.save(key, ShopDataManager.getType(tag.getCompound(key))));
        this.saveMap.forEach((key, value) -> value.deserializeNBT(tag.getCompound(key)));
    }
}

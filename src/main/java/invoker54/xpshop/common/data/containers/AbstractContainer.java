package invoker54.xpshop.common.data.containers;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AbstractContainer implements INBTSerializable<CompoundTag> {
    public static final String DATA = "DATA";
    public static final String GENERAL_TYPE = "GENERAL_TYPE";
    public static final String SPECIFIC_TYPE = "SPECIFIC_TYPE";
    public static final String MOD_ID = "MOD_ID";

    public abstract AbstractContainer copy();

    public String getSpecificType() {
        return this.getClass().getSimpleName();
    }

    public String getGeneralType(){
        return AbstractContainer.class.getSimpleName();
    }

    public abstract String getModId();

    public abstract void deserializeNBT(CompoundTag tag);
}
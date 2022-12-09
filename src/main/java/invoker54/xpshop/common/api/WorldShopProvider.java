package invoker54.xpshop.common.api;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldShopProvider implements ICapabilitySerializable<INBT> {
    public static final byte COMPOUND_NBT_ID = new CompoundNBT().getId();

    public WorldShopProvider(World level){
        WorldShopCapability = new WorldShopCapability(level);
    }

    //region Capability setup
    //This is where all of the fallen capability data is
    @CapabilityInject(WorldShopCapability.class)
    public static Capability<WorldShopCapability> XPSHOPDATA = null;

    private final static String XPSHOP_NBT = "xpWanderShopData";

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {


        if (XPSHOPDATA == capability) {
            return LazyOptional.of(() -> WorldShopCapability).cast();
            // why are we using a lambda?  Because LazyOptional.of() expects a NonNullSupplier interface.  The lambda automatically
            //   conforms itself to that interface.  This save me having to define an inner class implementing NonNullSupplier.
            // The explicit cast to LazyOptional<T> is required because our CAPABILITY_ELEMENTAL_FIRE can't be typed.  Our code has
            //   checked that the requested capability matches, so the explict cast is safe (unless you have mixed them up)
        }

        return LazyOptional.empty();


        //return LazyOptional.empty();
        // Note that if you are implementing getCapability in a derived class which implements ICapabilityProvider
        // eg you have added a new MyEntity which has the method MyEntity::getCapability instead of using AttachCapabilitiesEvent to attach a
        // separate class, then you should call
        // return super.getCapability(capability, facing);
        //   instead of
        // return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        CompoundNBT nbtData = new CompoundNBT();
        INBT shopNBT = XPSHOPDATA.writeNBT(WorldShopCapability, null);
        nbtData.put(XPSHOP_NBT, shopNBT);
        return  nbtData;
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt.getId() != COMPOUND_NBT_ID) {
            //System.out.println("Unexpected NBT type:"+nbt);
            return;  // leave as default in case of error
        }
        //System.out.println("I ran for deserializing");
        CompoundNBT nbtData = (CompoundNBT) nbt;
        XPSHOPDATA.readNBT(WorldShopCapability, null, nbtData.getCompound(XPSHOP_NBT));
    }

    //This is where the current capability is stored to read and write
    private WorldShopCapability WorldShopCapability;
}

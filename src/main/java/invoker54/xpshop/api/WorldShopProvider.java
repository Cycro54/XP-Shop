package invoker54.xpshop.api;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.capability.WorldShopCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldShopProvider  implements ICapabilitySerializable<CompoundTag> {
    public static Capability<WorldShopCapability> SHOP_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation WORLD_DATA_LOCATION = new ResourceLocation(XPShop.MOD_ID, "world_shop_data");

    private WorldShopCapability worldShopCapability;
    private final LazyOptional<WorldShopCapability> optionalData = LazyOptional.of(() -> worldShopCapability);

    public WorldShopProvider(){
        this.worldShopCapability = new WorldShopCapability();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return SHOP_DATA.orEmpty(cap, this.optionalData);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.worldShopCapability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.worldShopCapability.deserializeNBT(nbt);
    }
}
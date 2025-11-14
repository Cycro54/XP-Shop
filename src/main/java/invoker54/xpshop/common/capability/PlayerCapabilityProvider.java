package invoker54.xpshop.common.capability;

import invoker54.xpshop.XPShop;
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

public class PlayerCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

    public PlayerCapabilityProvider(){
        playerCapability = new PlayerCapability();
    }

    public static final ResourceLocation STORAGE_LOCATION = ResourceLocation.fromNamespaceAndPath(XPShop.MOD_ID, "player_data");

    public static Capability<PlayerCapability> PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    private PlayerCapability playerCapability;
    private final LazyOptional<PlayerCapability> optionalData = LazyOptional.of(() -> playerCapability);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T > cap, @Nullable Direction side) {
        return PLAYER_DATA.orEmpty(cap, this.optionalData);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.playerCapability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.playerCapability.deserializeNBT(tag);
    }
}

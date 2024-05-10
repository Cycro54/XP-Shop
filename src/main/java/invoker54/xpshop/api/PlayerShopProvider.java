package invoker54.xpshop.api;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.capability.PlayerShopCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerShopProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<PlayerShopCapability> PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation PLAYER_DATA_LOCATION = new ResourceLocation(XPShop.MOD_ID, "player_shop_data");

    private PlayerShopCapability playerShopCapability;
    private final LazyOptional<PlayerShopCapability> optionalData = LazyOptional.of(() -> playerShopCapability);

    public PlayerShopProvider(Player player){
        this.playerShopCapability = new PlayerShopCapability(player);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PLAYER_DATA.orEmpty(cap, this.optionalData);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.playerShopCapability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.playerShopCapability.deserializeNBT(nbt);
    }
}

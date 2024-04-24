package invoker54.xpshop.item;

import invoker54.xpshop.capability.PlayerShopCapability;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WalletItem extends Item {
    private final WalletTier tier;

    public WalletItem(WalletTier tier, Properties props) {
        super(props);
        this.tier = tier;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack walletStack = player.getItemInHand(hand);
        PlayerShopCapability.getDataCap(player).ifPresent((cap) ->{
            boolean flag = cap.upgradeWallet(this.tier);
            if (flag && !player.isCreative()) walletStack.shrink(1);
        });
        return super.use(level, player, hand);
    }
}

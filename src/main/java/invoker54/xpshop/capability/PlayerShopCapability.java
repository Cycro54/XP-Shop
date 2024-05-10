package invoker54.xpshop.capability;

import invoker54.xpshop.api.PlayerShopProvider;
import invoker54.xpshop.item.WalletTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerShopCapability implements INBTSerializable<CompoundTag> {
    //Do not save this. it's unnecessary
    private final Map<Item, Integer> itemMap = new HashMap<>();
    private final Player player;
    private final List<Integer> unlockedItems = new ArrayList<>();
    private final List<Integer> unlockedUpgrades = new ArrayList<>();
    private float leftoverXP;
    private float traderXP;
    private WalletTier walletTier;
    //These are the upgrades

    public PlayerShopCapability(Player player){
        this.player = player;
    }

    public static @NotNull LazyOptional<PlayerShopCapability> getDataCap(Player player){
        return player.getCapability(PlayerShopProvider.PLAYER_DATA);
    }

    public boolean upgradeWallet(WalletTier newTier){
        int result = this.walletTier.compareTo(newTier);

        if (result < -1){
            player.sendSystemMessage(Component.translatable("xp_shop.chat.tier_low"));
            return false;
        }
        else if (result == -1){
            player.sendSystemMessage(Component.translatable("xp_shop.chat.unlock.upgrade_wallet").append("" + newTier.getMax()));
            this.walletTier = newTier;
            return true;
        }
        else {
            player.sendSystemMessage(Component.translatable("xp_shop.chat.have_upgrade"));
            return false;
        }
    }

    public void setLeftoverXP(float newValue){
        this.leftoverXP = newValue;
    }
    public Float getLeftoverXP(){
        return leftoverXP;
    }

    public void unlockShopItem(Integer id){
        if (!this.unlockedItems.contains(id))
            this.unlockedItems.add(id);
    }

    public Integer getItemCount(ItemStack itemStack){
        return itemMap.get(itemStack.getItem());
    }

    public void countInventoryItems(){
        Inventory playerInv = player.getInventory();
        ArrayList<Item> countedItems = new ArrayList<>();

        for(ItemStack itemStack : playerInv.items){
            if (itemStack.isEmpty()) continue;
            if (countedItems.contains(itemStack.getItem())) continue;

            itemMap.compute(itemStack.getItem(), ((key,value) -> {
                if (value == null) value = 0;
                int newCount = playerInv.countItem(key);
                countedItems.add(key);

                return Math.max(newCount, value);
            }));
        }

    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
package invoker54.xpshop.common.api;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.data.CategoryEntry;
import invoker54.xpshop.common.data.ShopData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public class WanderShopCapability {
    protected final String ENTRY_SIZE = "ENTRY_SIZE";
    protected World level;
    public ArrayList<BuyEntry> buyEntries;
    public WanderShopCapability(){}
    public WanderShopCapability(World level) {
        this.level = level;
        buyEntries = new ArrayList<>();

        //Don't do calculations on the client
        if (level.isClientSide) return;

        //Grab all the categories then remove them until we reach our max category count
        ArrayList<CategoryEntry> categoryEntries = new ArrayList<>(ShopData.catEntries);
        while (categoryEntries.size() > ShopConfig.randomCategoryCount && ShopConfig.randomCategoryCount != 0){
            categoryEntries.remove(level.random.nextInt(categoryEntries.size()));
        }

        //Next grab all the items in those categories
        for (CategoryEntry catEntry : categoryEntries){
            buyEntries.addAll(catEntry.entries);
        }
        //Shuffle them
        Collections.shuffle(buyEntries, level.random);
        //remove them until we reach our max buy entry count
        while (buyEntries.size() > ShopConfig.randomBuyEntryCount && ShopConfig.randomBuyEntryCount != 0){
            buyEntries.remove(level.random.nextInt(buyEntries.size()));
        }
    }

    public static WanderShopCapability getShopCap(LivingEntity traderEntity){
        return traderEntity.getCapability(WanderShopProvider.XPSHOPDATA).orElseThrow(NullPointerException::new);
    }

    public CompoundNBT writeNBT(){
        CompoundNBT mainNBT = new CompoundNBT();

        //Save each buy entry item so we can look for it later
        for (int a = 0; a < buyEntries.size(); a++){
            mainNBT.put(""+a, buyEntries.get(a).item.serializeNBT());
        }

        //Finally save the size
        mainNBT.putInt(ENTRY_SIZE, buyEntries.size());

        return mainNBT;
    }

    public void readNBT (CompoundNBT mainNBT){
        ArrayList<ItemStack> savedItems = new ArrayList<>();
        int count = mainNBT.getInt(ENTRY_SIZE);

        //Grab the saved items
        for (int a = 0; a < count; a++){
            savedItems.add(ItemStack.of((CompoundNBT) mainNBT.get(""+a)));
        }

        //Next look for those items in the buyEntries list
        for (ItemStack savedItem : savedItems){
            for (BuyEntry entry : ShopData.buyEntries){
                if (savedItem.sameItem(entry.item)){
                    buyEntries.add(entry);
                    break;
                }
            }
        }

    }

    public static class ShopNBTStorage implements Capability.IStorage<WanderShopCapability>{

        @Nullable
        @Override
        public INBT writeNBT(Capability<WanderShopCapability> capability, WanderShopCapability instance, Direction side) {
            return instance.writeNBT();
        }

        @Override
        public void readNBT(Capability<WanderShopCapability> capability, WanderShopCapability instance, Direction side, INBT nbt) {
            CompoundNBT mainNbt = (CompoundNBT) nbt;

            instance.readNBT(mainNbt);
        }


    }
}

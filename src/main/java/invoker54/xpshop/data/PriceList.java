package invoker54.xpshop.data;

import invoker54.xpshop.config.XPShopConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static invoker54.xpshop.event.generation.ShopGenerationEvent.df;

public class PriceList implements INBTSerializable<CompoundTag> {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    //region strings
    private final String STAT_COMPOUND = "STAT_COMPOUND";
    private final String RECIPE_COMPOUND = "RECIPE_COMPOUND";
    private final String JITTER_DOUBLE = "JITTER_DOUBLE";
    private final String RARITY_MULTIPLIER_DOUBLE = "RARITY_MULTIPLIER_DOUBLE";
    public static final String NAME_STRING = "NAME_STRING";
    public static final String LOW_PRICE_DOUBLE = "LOW_PRICE_DOUBLE";
    public static final String HIGH_PRICE_DOUBLE = "HIGH_PRICE_DOUBLE";
    public static final PriceInfo emptyPrice = new PriceInfo("Empty", 0, 0);
    //endregion

    //region Constant value
    private static final float addedRange = 0.2F;
    //endregion


    private final List<PriceInfo> statList = new ArrayList<>();
    private final List<PriceInfo> recipeList = new ArrayList<>();
    private double priceJitter;
    private double rarityMultiplier;
    private double fullPrice = 0;
    private double statPrice = 0;
    private PriceInfo myCompiledInfo;
    private boolean changed = true;

    public PriceList(ItemStack priceStack) {
        //This is for the rarity multiplier and price-jitter
        this.rarityMultiplier = 1 + (XPShopConfig.xpRarityMultiplier * priceStack.getRarity().ordinal());
        double min = XPShopConfig.priceJitter * -1;
        double max = XPShopConfig.priceJitter;
        this.priceJitter = min + RandomSource.create().nextDouble() * (max - min);
    }

    public PriceList(CompoundTag tag) {
        this.deserializeNBT(tag);
    }

    public List<Component> getTooltip(boolean holdingCrouch) {
        if (!XPShopConfig.debugMode.get()) return new ArrayList<>();
        List<Component> list = new ArrayList<>();
        list.add(Component.literal((holdingCrouch ? "Jitter" : "") + " Full price: " + this.getFullPrice(holdingCrouch)));
//        list.add(Component.literal((holdingCrouch ? "Jitter":"")+" Sell price: " + this.getFullPrice(holdingCrouch)));
//        list.add(Component.literal("Counter: " + this.priceCounter));

        list.add(Component.literal("Stats:"));
        for (PriceInfo pair : this.statList) {
            list.add(Component.literal(pair.toString()));
        }
        list.add(Component.literal("Recipes:"));
        for (PriceInfo pair : this.recipeList) {
            list.add(Component.literal(pair.toString()));
        }

        return list;
    }

    public boolean isEmpty() {
        return this.statList.isEmpty() && this.recipeList.isEmpty();
    }

    public boolean hasStats() {
        return !this.statList.isEmpty();
    }

    public void clearRecipes() {
        this.recipeList.clear();
    }

    public boolean hasRecipes() {
        return !this.recipeList.isEmpty();
    }

    public void addStat(String statType, double price) {
        this.statList.add(new PriceInfo(statType, price, price));
        changed = true;
    }

    public void addRecipe(PriceInfo priceInfo) {
        this.recipeList.add(priceInfo);
        changed = true;
//        LOGGER.warn("Price Type: " + priceType + ", Price added: " + price + ", Price Counter: " + counter);
    }

    public void calculateFinalPrice() {
        List<PriceInfo> allPrices =
                new ArrayList<>(this.statList);
        allPrices.addAll(this.recipeList);
        this.fullPrice = 0;
        this.statPrice = 0;
        myCompiledInfo = compilePriceListData("", allPrices);
        if (myCompiledInfo.highPrice == 0) return;

        //Get the average
        this.fullPrice = myCompiledInfo.getAverage();
//        this.statPrice = this.statPrice/counter;
    }

    public static void sortAndRemoveOutliers(List<PriceInfo> listToProcess) {
        if (listToProcess.size() <= 2) return;

        PriceInfo highInfo = listToProcess.get(0);
        PriceInfo lowInfo = listToProcess.get(0);
        double median = 0;
        List<Pair<Double, PriceInfo>> numberList = new ArrayList<>();
        for (PriceInfo info : listToProcess) {
            numberList.add(Pair.of(info.lowPrice, info));
            if (info.lowPrice != info.highPrice) numberList.add(Pair.of(info.highPrice, info));

            if (info.highPrice > highInfo.highPrice) highInfo = info;
            if (info.lowPrice < lowInfo.lowPrice) lowInfo = info;
        }
        numberList.sort(Comparator.comparingDouble(Pair::getLeft));

        int splitSize = (numberList.size() - 1) / 2;
        if (numberList.size() % 2 == 0) {
            splitSize = numberList.size() / 2;
            median = numberList.get(splitSize).getLeft() + numberList.get(splitSize - 1).getLeft();
            median /= 2;
        } else median = numberList.get(splitSize).getLeft();

        double medianPercentage = (median) / highInfo.highPrice;
        double range = medianPercentage;
        double max = (medianPercentage + range) * highInfo.highPrice;

        for (PriceInfo pair : new ArrayList<>(listToProcess)) {
            if (pair.highPrice <= max) continue;
            if (pair.lowPrice <= max) continue;
            listToProcess.remove(pair);
        }
    }

    public double getFullPrice(boolean applyJitter) {
        if (this.changed) {
            changed = false;
            calculateFinalPrice();
        }
        if (this.fullPrice == 0) return 0F;
        double finalPrice = this.fullPrice * rarityMultiplier;
        return Math.max(Double.parseDouble(df.format(applyJitter ? finalPrice + (finalPrice * this.priceJitter) : finalPrice)), 1);
    }

    public PriceInfo getFinalPriceInfo() {
        getFullPrice(false);
        return this.myCompiledInfo;
    }

    public List<PriceInfo> getStatInfo() {
        return new ArrayList<>(this.statList);
    }

    public static PriceInfo compilePriceListData(String name, List<PriceInfo> priceInfoList) {
        if (priceInfoList.isEmpty()) {
//            LOGGER.info("Price list was empty, returning empty price info");
            return emptyPrice;
        }

        double lowPrice = priceInfoList.get(0).lowPrice;
        double highPrice = priceInfoList.get(0).highPrice;

        for (PriceInfo priceInfo : priceInfoList) {
            if (priceInfo.lowPrice < lowPrice) lowPrice = priceInfo.lowPrice;
            if (priceInfo.highPrice > highPrice) highPrice = priceInfo.highPrice;
        }

        return new PriceInfo(name, lowPrice, highPrice);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        //This will contain all the stats
        CompoundTag statTag = serializePriceInfo(this.statList);
        tag.put(STAT_COMPOUND, statTag);

        CompoundTag recipeTag = serializePriceInfo(this.recipeList);
        tag.put(RECIPE_COMPOUND, recipeTag);

        tag.putDouble(JITTER_DOUBLE, this.priceJitter);
        tag.putDouble(RARITY_MULTIPLIER_DOUBLE, this.rarityMultiplier);
        return tag;
    }

    @NotNull
    private CompoundTag serializePriceInfo(List<PriceInfo> priceInfoList) {
        CompoundTag priceInfoTag = new CompoundTag();
        //This will contain all the pair info
        for (PriceInfo pair : priceInfoList) {
            CompoundTag pairTag = new CompoundTag();
            pairTag.putString(NAME_STRING, pair.name);
            pairTag.putDouble(LOW_PRICE_DOUBLE, pair.lowPrice);
            pairTag.putDouble(HIGH_PRICE_DOUBLE, pair.highPrice);
            priceInfoTag.put(priceInfoTag.size() + "", pairTag);
        }
        return priceInfoTag;
    }

    private void deserializePriceInfo(List<PriceInfo> priceInfoList, CompoundTag pairTagPackage) {
        priceInfoList.clear();
        for (String pairString : pairTagPackage.getAllKeys()) {
            CompoundTag pairTag = pairTagPackage.getCompound(pairString);
            priceInfoList.add(new PriceInfo(
                    pairTag.getString(NAME_STRING),
                    pairTag.getDouble(LOW_PRICE_DOUBLE),
                    pairTag.getDouble(HIGH_PRICE_DOUBLE)
            ));
        }
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        CompoundTag statTag = tag.getCompound(STAT_COMPOUND);
        deserializePriceInfo(this.statList, statTag);

        CompoundTag recipeTag = tag.getCompound(RECIPE_COMPOUND);
        deserializePriceInfo(this.recipeList, recipeTag);

        this.priceJitter = tag.getDouble(JITTER_DOUBLE);
        this.rarityMultiplier = tag.getDouble(RARITY_MULTIPLIER_DOUBLE);
    }

    public record PriceInfo(String name, double lowPrice, double highPrice) {

        @Override
        public String toString() {
            if (this.lowPrice == this.highPrice) return ("[" + name + ", Price: " + df.format(this.lowPrice) + "]");
            return ("[" + name + ", Low: " + df.format(this.lowPrice) + ", High: " + df.format(this.highPrice) + "]");
        }

        public interface Operation {
            double calculate(double x);
        }

        public PriceInfo add(PriceInfo info) {
            return calculate((x -> x + info.lowPrice), (y -> y + info.highPrice));
        }

        public PriceInfo combine(PriceInfo otherInfo) {
            return compilePriceListData(this.name, List.of(otherInfo, this));
        }

        public PriceInfo calculate(Operation operation) {
            return this.calculate(operation, operation);
        }

        public PriceInfo calculate(Operation op1, Operation op2) {
//            LOGGER.warn("Old Info: " + this);
            try {
                PriceInfo newInfo = new PriceInfo(this.name, op1.calculate(lowPrice), op2.calculate(highPrice));
                return newInfo;
            } catch (Exception e) {
                LOGGER.error("Couldn't complete operation");
                return this;
            }
//            LOGGER.warn("New Info: " + newInfo);

        }

        public double getAverage() {
            return (lowPrice + highPrice) / 2D;
        }
    }


}
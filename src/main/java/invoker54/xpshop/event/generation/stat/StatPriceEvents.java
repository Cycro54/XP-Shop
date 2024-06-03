package invoker54.xpshop.event.generation.stat;

import com.google.common.collect.Multimap;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.CategoryEntry;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import invoker54.xpshop.util.MiniTicker;
import invoker54.xpshop.util.ModStopWatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class StatPriceEvents {

    private static LivingEntity dummyEntity;
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    private static final float xpPerFoodEffect = 0.015F;

    public static void setDummyEntity(LivingEntity entity){
        dummyEntity = entity;
    }
    public static LivingEntity getDummyEntity(){
        return dummyEntity;
    }

    private static final Map<String, MiniTicker<?>> tickerMap = new HashMap<>();

    public static void resetTicker(String name){
        MiniTicker<?> ticker = tickerMap.getOrDefault(name,
                ModStopWatch.getTimer(XPShop.MOD_ID, name, ModStopWatch.Time.LONGEST).ticker());
        if (!tickerMap.containsKey(name)) tickerMap.put(name, ticker);

        ticker.reset();
    }

    public static void recordTicker(String name, String desc){
        MiniTicker<?> ticker = tickerMap.getOrDefault(name,
                ModStopWatch.getTimer(XPShop.MOD_ID, name, ModStopWatch.Time.LONGEST).ticker());


        String finalDesc = ticker.record(desc);
        if (!finalDesc.isEmpty()) LOGGER.warn(finalDesc);
    }

    public static void printAllTickers(){
        for (var entry : tickerMap.entrySet()){
            List<String> times = entry.getValue().getOwner().grabTimes(true);
            if (times.isEmpty()) continue;
            LOGGER.warn(times.get(times.size()-1));
        }
    }

    @SubscribeEvent
    public static void getArmorPrice(PriceEvent event){
        if (!(event.getCurrentItem().getItem() instanceof ArmorItem armorItem)) return;

        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.ARMOR);
        if (XPShopConfig.xpPerArmor == 0) return;
        resetTicker("armorPrice");

        int defense = armorItem.getDefense();
        float toughness = armorItem.getToughness();
        float finalPrice = (float) ((defense + toughness) * XPShopConfig.xpPerArmor);

        event.getPriceList().addStat("Armor value", finalPrice);
        recordTicker("armorPrice", "Armor cost: " + (float) ((defense + toughness) * XPShopConfig.xpPerArmor));
    }

    @SubscribeEvent
    public static void getDurabilityPrice(PriceEvent event){
        if (!event.getCurrentItem().isDamageableItem()) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.WEAPONS_TOOLS);
        if (XPShopConfig.xpPerDurability == 0) return;
        resetTicker("durabilityPrice");

        float finalPrice = (float) (event.getCurrentItem().getMaxDamage() * XPShopConfig.xpPerDurability);

        event.getPriceList().addStat("Durability value", finalPrice);
        recordTicker("durabilityPrice", "Durability cost: " + finalPrice);
    }

    @SubscribeEvent
    public static void getDamagePrice(PriceEvent event) {
        if (!(event.getCurrentItem().getItem() instanceof TieredItem)) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.WEAPONS_TOOLS);
        if (XPShopConfig.xpPerDamage == 0) return;
        resetTicker("damagePrice");

        LivingEntity dummyEntity = getDummyEntity();
        ItemStack currentItem = event.getCurrentItem();
        double highestDamage = 0;

        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            double newDamage = 0;

            Multimap<Attribute, AttributeModifier> multimap = currentItem.getAttributeModifiers(equipmentslot);
            if (!multimap.isEmpty()) {

                for (Map.Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
                    AttributeModifier attributemodifier = entry.getValue();
                    if (attributemodifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
//                        LOGGER.warn("ATTRIBUTE VALUE: " + attributemodifier.getAmount());
//                        LOGGER.warn("DUMMY ATTACK VALUE: " + dummyEntity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));

                        newDamage = attributemodifier.getAmount();
                        newDamage += dummyEntity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                        newDamage += EnchantmentHelper.getDamageBonus(currentItem, MobType.UNDEFINED);

                        if (highestDamage < newDamage) {
                            highestDamage = newDamage;
                        }
                    }
                }
            }
        }

        if (highestDamage <= 1) return;

        float finalPrice = (float) (highestDamage * XPShopConfig.xpPerDamage);

        event.getPriceList().addStat("Damage value", finalPrice);
        recordTicker("damagePrice", "Attack: " + highestDamage + ", Cost: " + finalPrice);
    }

    @SubscribeEvent
    public static void getEnchantPrice(PriceEvent event){
        resetTicker("enchantPrice");
        Map<Enchantment, Integer> enchantMap = EnchantmentHelper.getEnchantments(event.getCurrentItem());
        if (enchantMap.isEmpty()) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.ENCHANTMENTS);

        if (XPShopConfig.xpPerEnchantLvl == 0) return;

        float totalCost = 0;

        for (Map.Entry<Enchantment, Integer> entry : enchantMap.entrySet() ){
            totalCost += (float) (entry.getValue() * XPShopConfig.xpPerEnchantLvl);
        }

        event.getPriceList().addStat("Enchant value", totalCost);
        recordTicker("enchantPrice", "Enchant value: " + totalCost);
//        LOGGER.warn("Enchant cost: " + totalCost);
    }

    @SubscribeEvent
    public static void getFoodPrice(PriceEvent event){
        if (!event.getCurrentItem().isEdible()) return;
        FoodProperties foodProperties = event.getCurrentItem().getFoodProperties(null);
        if (foodProperties == null) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.FOOD);
        if (XPShopConfig.xpPerFood == 0) return;
        resetTicker("foodPrice");

        float foodTotal = (foodProperties.getNutrition() + foodProperties.getSaturationModifier());
        foodTotal = (float) (foodTotal * XPShopConfig.xpPerFood);
        float effectTotal = 0;
        for (var effectPair:  foodProperties.getEffects()){
            float probability = effectPair.getSecond();
            MobEffectInstance instance = effectPair.getFirst();
            int amp = instance.getAmplifier() + 1;
            int duration = instance.getDuration();

            if (!instance.getEffect().isBeneficial()) probability *= -1;

            effectTotal += (float) (amp * duration * xpPerFoodEffect * XPShopConfig.xpPerFood * probability);
        }

        float finalPrice = Math.max(foodTotal/2F, foodTotal + effectTotal);
        event.getPriceList().addStat("Food value", finalPrice);

        recordTicker("foodPrice", "Effect count: "+foodProperties.getEffects().size()+", Food Price: " + finalPrice);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void getBasicResourcePrice(PriceEvent event) {
        PriceList priceList = event.getPriceList();
        if (!priceList.isEmpty()) return;
        resetTicker("resourcePrice");
        ItemStack currItem = event.getCurrentItem();

        Set<String> itemStringTags = new HashSet<>();
        itemStringTags.addAll(currItem.getTags().map(TagKey::location).
                map(ResourceLocation::toString).
                map((s)->s.toLowerCase(Locale.ROOT)).toList());
        itemStringTags.addAll(Block.byItem(currItem.getItem()).
                        defaultBlockState().getTags().
                        map(TagKey::location).
                map(ResourceLocation::toString)
                .map((s)->s.toLowerCase(Locale.ROOT)).toList());
        if (itemStringTags.isEmpty()) return;
        int count = 0;
        int sum = 0;
        for (String resourceString : ShopGenerationEvent.basicResourceMap.keySet()){
            for (String itemString : itemStringTags){
                if (itemString.contains(resourceString)){
                    count += 1;
                    sum += ShopGenerationEvent.basicResourceMap.get(resourceString);
                }
            }
        }

        if (count != 0){
            ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.BASIC_RESOURCES);
            priceList.addStat("Basic Resource", (float) (sum/count));

            recordTicker("resourcePrice", "Item string count: "+ itemStringTags.size()
                    +", resource count: " + count + ", price: " + (sum/count));
        }

    }
}

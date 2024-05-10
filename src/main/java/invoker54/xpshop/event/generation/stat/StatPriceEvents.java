package invoker54.xpshop.event.generation.stat;

import com.google.common.collect.Multimap;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.CategoryEntry;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import invoker54.xpshop.event.generation.recipe.PriceRecipeEvent;
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

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    @SubscribeEvent
    public static void getArmorPrice(PriceEvent event){
        ModStopWatch stopWatch = ModStopWatch.getTimer(XPShop.MOD_ID, "armorPrice", ModStopWatch.Time.LONGEST);
        MiniTicker<?> ticker = stopWatch.ticker();
        ticker.reset();
        if (event instanceof PriceRecipeEvent) return;
        if (!(event.getCurrentItem().getItem() instanceof ArmorItem armorItem)) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.ARMOR);
        if (XPShopConfig.xpPerArmor == 0) return;

        LOGGER.info("Pricing armor item");


        int defense = armorItem.getDefense();
        float toughness = armorItem.getToughness();

        event.getPriceList().addStat("Armor value", (float) ((defense + toughness) * XPShopConfig.xpPerArmor));
        ticker.record("Armor cost: " + (float) ((defense + toughness) * XPShopConfig.xpPerArmor));
    }

    @SubscribeEvent
    public static void getDurabilityPrice(PriceEvent event){
        if (event instanceof PriceRecipeEvent) return;
        if (!event.getCurrentItem().isDamageableItem()) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.WEAPONS_TOOLS);
        if (XPShopConfig.xpPerDurability == 0) return;
        LOGGER.info("Pricing durability item");


        event.getPriceList().addStat("Durability value", (float) (event.getCurrentItem().getMaxDamage() * XPShopConfig.xpPerDurability));
        LOGGER.warn("Durability cost: " + (float) (event.getCurrentItem().getMaxDamage() * XPShopConfig.xpPerDurability));
    }

    @SubscribeEvent
    public static void getDamagePrice(PriceEvent event) {
        if (event instanceof PriceRecipeEvent) return;
        if (!(event.getCurrentItem().getItem() instanceof TieredItem)) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.WEAPONS_TOOLS);
        if (XPShopConfig.xpPerDamage == 0) return;

        LOGGER.info("Pricing damage item");

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
                        LOGGER.warn("ATTRIBUTE VALUE: " + attributemodifier.getAmount());
                        LOGGER.warn("DUMMY ATTACK VALUE: " + dummyEntity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));

                        newDamage = attributemodifier.getAmount();
                        newDamage += dummyEntity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                        newDamage += (double) EnchantmentHelper.getDamageBonus(currentItem, MobType.UNDEFINED);

                        if (highestDamage < newDamage) {
                            highestDamage = newDamage;
                        }
                    }
                }
            }

            if (newDamage != 0) {
                LOGGER.warn("Attack Damage: " + newDamage);
            }
        }

        if (highestDamage <= 1) {
            LOGGER.warn("This is not a damage item, skip...");
            return;
        }
        event.getPriceList().addStat("Damage value", (float) (highestDamage * XPShopConfig.xpPerDamage));
        LOGGER.warn("Attack: " + highestDamage + ", Cost: " + (float) (highestDamage * XPShopConfig.xpPerDamage));
    }

    @SubscribeEvent
    public static void getEnchantPrice(PriceEvent event){
        if (event instanceof PriceRecipeEvent) return;
        Map<Enchantment, Integer> enchantMap = EnchantmentHelper.getEnchantments(event.getCurrentItem());
        if (enchantMap.isEmpty()) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.ENCHANTMENTS);

        if (XPShopConfig.xpPerEnchantLvl == 0) return;

//        LOGGER.info("Pricing enchant item");

        float totalCost = 0;

        for (Map.Entry<Enchantment, Integer> entry : enchantMap.entrySet() ){
            totalCost += (float) (entry.getValue() * XPShopConfig.xpPerEnchantLvl);
        }

        event.getPriceList().addStat("Enchant value", totalCost);
//        LOGGER.warn("Enchant cost: " + totalCost);
    }

    @SubscribeEvent
    public static void getFoodPrice(PriceEvent event){
        if (event instanceof PriceRecipeEvent) return;
        if (!event.getCurrentItem().isEdible()) return;
        FoodProperties foodProperties = event.getCurrentItem().getFoodProperties(null);
        if (foodProperties == null) return;
        ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.FOOD);
        if (XPShopConfig.xpPerFood == 0) return;
        LOGGER.info("Pricing food item");

        float foodTotal = (foodProperties.getNutrition() + foodProperties.getSaturationModifier());
        LOGGER.warn("Amount of food: " + foodTotal);
        foodTotal = (float) (foodTotal * XPShopConfig.xpPerFood);
        LOGGER.warn("Base food to xp: " + foodTotal);
        float effectTotal = 0;
        for (var effectPair:  foodProperties.getEffects()){
            float probability = effectPair.getSecond();
            MobEffectInstance instance = effectPair.getFirst();
            int amp = instance.getAmplifier() + 1;
            int duration = instance.getDuration();

            if (!instance.getEffect().isBeneficial()) probability *= -1;

            effectTotal += (float) (amp * duration * xpPerFoodEffect * XPShopConfig.xpPerFood * probability);
            LOGGER.warn("Effect Total: " + effectTotal);
        }
        event.getPriceList().addStat("Food value", Math.max(foodTotal/2F, foodTotal + effectTotal));

        LOGGER.warn("Food cost: " + Math.max(foodTotal, foodTotal + effectTotal));
    }

//    @SubscribeEvent
//    public static void getOrePrice(PriceEvent event){
//        if (event instanceof PriceRecipeCopyEvent) return;
//        Block block = Block.byItem(event.getCurrentItem().getItem());
//        if (!ShopGenerationCopyEvent.oreList.contains(block)) return;
//        if (XPShopConfig.xpOreStep == 0) return;
//        LOGGER.info("Pricing ore item");
//
//        event.addToSum(ShopGenerationCopyEvent.oreMap.get(block));
//        LOGGER.warn("Ore cost: " + ShopGenerationCopyEvent.oreMap.get(block));
//    }
//
//    @SubscribeEvent
//    public static void getDropPrice(PriceEvent event){
//        if (event instanceof PriceRecipeCopyEvent) return;
//        ItemStack dropStack = ShopGenerationCopyEvent.getMatchingItemStack(event.getCurrentItem(), ShopGenerationCopyEvent.dropsMap.keySet());
//        if (dropStack == null) return;
//        if (ShopGenerationCopyEvent.dropsMap.get(dropStack) == 0) return;
//        LOGGER.info("Pricing drop item");
//
//        event.addToSum(ShopGenerationCopyEvent.dropsMap.get(dropStack));
//        LOGGER.warn("Drop cost: " + ShopGenerationCopyEvent.dropsMap.get(dropStack));
//    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void getBasicResourcePrice(PriceEvent event) {
        if (event instanceof PriceRecipeEvent) return;
        PriceList priceList = event.getPriceList();
        if (!priceList.isEmpty()) return;
        ItemStack currItem = event.getCurrentItem();

        Set<String> itemStringTags = new HashSet<>();
        itemStringTags.addAll(currItem.getTags().map(TagKey::location).map(ResourceLocation::toString).toList());
        itemStringTags.addAll(Block.byItem(currItem.getItem()).
                        defaultBlockState().getTags().
                        map(TagKey::location).
                map(ResourceLocation::toString).toList());
//        currItem.getTags().toList().forEach((tagKey) -> {
//            String locationString = tagKey.location().toString().toLowerCase(Locale.ROOT);
//            itemStringTags.add(locationString);
//        });
//        Block.byItem(currItem.getItem()).defaultBlockState().getTags().toList().forEach((tagKey) -> {
//            String locationString = tagKey.location().toString().toLowerCase(Locale.ROOT);
//            itemStringTags.add(locationString);
//        });
        if (itemStringTags.isEmpty()) return;
        else LOGGER.info("Pricing possible basic resource item");
        int count = 0;
        int sum = 0;
        for (String resourceString : ShopGenerationEvent.basicResourceMap.keySet()){
            for (String itemString : itemStringTags){
                if (itemString.toLowerCase(Locale.ROOT).contains(resourceString)){
                    count += 1;
                    sum += ShopGenerationEvent.basicResourceMap.get(resourceString);
                }
            }
        }

        if (count != 0){
            ShopGenerationEvent.assignCategory(event.getCurrentItem(), CategoryEntry.BASIC_RESOURCES);
            priceList.addStat("Basic Resource", (float) (sum/count));
            LOGGER.warn("Basic resource average cost: " + (sum/count));
        }
    }
}

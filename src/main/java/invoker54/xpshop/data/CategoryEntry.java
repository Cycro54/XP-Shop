package invoker54.xpshop.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.INBTSerializable;

import static invoker54.xpshop.event.generation.ShopGenerationCopyEvent.getOrCreateCategory;

public class CategoryEntry implements INBTSerializable<CompoundTag> {
    public static final CategoryEntry ODDITY = new CategoryEntry(-1, 0, "Oddity", new ItemStack(Items.BEDROCK));
    public static final CategoryEntry BASIC_RESOURCES = getOrCreateCategory( 1, "Basic Resources", new ItemStack(Items.OAK_LOG));
    public static final CategoryEntry CRAFTABLES = getOrCreateCategory( 2, "Craftables", new ItemStack(Items.CRAFTING_TABLE));
    public static final CategoryEntry ENCHANTMENTS = getOrCreateCategory( 3, "Enchantments", new ItemStack(Items.ENCHANTED_BOOK));
    public static final CategoryEntry MOB_DROPS = getOrCreateCategory( 4, "Mob Drops", new ItemStack(Items.ENDER_PEARL));
    public static final CategoryEntry ORE_DROPS = getOrCreateCategory( 5, "Ore Drops", new ItemStack(Items.COAL));
    public static final CategoryEntry POTIONS = getOrCreateCategory(6, "Potions", new ItemStack(Items.POTION));
    public static final CategoryEntry FOOD = getOrCreateCategory(7, "Food", new ItemStack(Items.COOKED_BEEF));
    public static final CategoryEntry ARMOR = getOrCreateCategory( 8, "Armor", new ItemStack(Items.DIAMOND_CHESTPLATE));
    public static final CategoryEntry WEAPONS_TOOLS = getOrCreateCategory( 9, "Weapons & Tools", new ItemStack(Items.IRON_SWORD));

    private int categoryId;
    private int priority;
    private String categoryName;
    private ItemStack displayItem;

    public CategoryEntry(Integer categoryId, Integer priority, String categoryName, ItemStack displayItem) {
        this.categoryId = categoryId;
        this.priority = priority;
        this.categoryName = categoryName;
        this.displayItem = displayItem;
    }

    public CategoryEntry(CompoundTag tag) {
        this.deserializeNBT(tag);
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public int getPriority() {
        return this.priority;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem.copy();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag categoryEntryTag = new CompoundTag();
        categoryEntryTag.putInt("categoryId", this.categoryId);
        categoryEntryTag.putInt("priority", this.priority);
        categoryEntryTag.putString("categoryName", this.categoryName);
        categoryEntryTag.put("displayItem", this.displayItem.serializeNBT());

        return categoryEntryTag;
    }

    @Override
    public void deserializeNBT(CompoundTag categoryEntryTag) {
        this.categoryId = categoryEntryTag.getInt("categoryId");
        this.priority = categoryEntryTag.getInt("priority");
        this.categoryName = categoryEntryTag.getString("categoryName");
        this.displayItem = ItemStack.of(categoryEntryTag.getCompound("displayItem"));
    }
}

package invoker54.xpshop.event.generation.recipe;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class IngredientEvents {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void craftIngredients(GatherIngredientsEvent event){
        if (event.isDone()) return;
        Recipe<?> recipe = event.getRecipe();
        event.setIngredients(recipe.getIngredients());
    }

    @SubscribeEvent
    public static void furnaceIngredients(GatherIngredientsEvent event){
        if (event.isDone()) return;
        Recipe<?> recipe = event.getRecipe();
        if (recipe.getType() != RecipeType.SMELTING && recipe.getType() != RecipeType.BLASTING) return;

        for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack stack : ingredient.getItems()) {
                if (stack.isDamageableItem() || stack.getItem() instanceof TieredItem) {
                    LOGGER.error("Probably a smelting tool recipe, skipping...");
                    event.setCanceled(true);
                    return;
                }
            }
        }
        List<Ingredient> ingredients = recipe.getIngredients();
        ingredients.add(Ingredient.of(Items.OAK_PLANKS));
        event.setIngredients(ingredients);
    }

    @SubscribeEvent
    public static void smithIngredients(GatherIngredientsEvent event){
        if (event.isDone()) return;
        Recipe<?> recipe = event.getRecipe();
        if (recipe.getType() != RecipeType.SMITHING) return;
        UpgradeRecipe trueRecipe = (UpgradeRecipe) recipe;

        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(trueRecipe.base);
        ingredients.add(trueRecipe.addition);

        event.setIngredients(ingredients);
    }
}

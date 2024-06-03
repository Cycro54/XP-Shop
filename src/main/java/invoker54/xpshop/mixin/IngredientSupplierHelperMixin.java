package invoker54.xpshop.mixin;

import invoker54.xpshop.event.generation.ShopGenerationEvent;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.recipes.RecipeLayoutBuilder;
import mezz.jei.library.gui.recipes.layout.builder.IRecipeLayoutSlotSource;
import mezz.jei.library.ingredients.IIngredientSupplier;
import mezz.jei.library.util.IngredientSupplierHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static invoker54.xpshop.event.generation.ShopGenerationEvent.craftResultMap;

@Pseudo
@Mixin(IngredientSupplierHelper.class)
public abstract class IngredientSupplierHelperMixin {
//    @Shadow
//    public static @Nullable <T> IIngredientSupplier getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory, IIngredientManager ingredientManager) {
//        return null;
//    }

    @Inject(
            remap = false,
            method = "getIngredientSupplier(Ljava/lang/Object;Lmezz/jei/api/recipe/category/IRecipeCategory;Lmezz/jei/api/runtime/IIngredientManager;)Lmezz/jei/library/ingredients/IIngredientSupplier;",
            at = {
                    @At(value = "RETURN")
            }
    )
    private static <T> void getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory, IIngredientManager ingredientManager, CallbackInfoReturnable<IIngredientSupplier> cir) {

        if (cir.getReturnValue() == null) return;
        IIngredientSupplier supplier = cir.getReturnValue();
        if (!(supplier instanceof RecipeLayoutBuilder layoutBuilder)) return;

        RecipeType<?> recipeType = recipeCategory.getRecipeType();
        if (recipeType == RecipeTypes.ANVIL) return;
        if (recipeType == RecipeTypes.INFORMATION) return;
        if (recipeType == RecipeTypes.COMPOSTING) return;
        if (recipeType == RecipeTypes.FUELING) return;

        ShopGenerationEvent.addTask(() -> {
            List<IRecipeLayoutSlotSource> slotSources = ((RecipeLayoutBuilderMixin) layoutBuilder).getSlots();
            List<ItemStack> outputs = new ArrayList<>();
            List<Ingredient> inputs = new ArrayList<>();

            //this is for the brewing recipes.
            boolean hasPotion = false;

            for (IRecipeLayoutSlotSource slot : slotSources) {
                if (!slot.getIngredientTypes().allMatch(type -> type == VanillaTypes.ITEM_STACK)) return null;
                List<ItemStack> stackList = slot.getIngredients(VanillaTypes.ITEM_STACK).toList();
                if (stackList.isEmpty()) continue;

                switch (slot.getRole()) {
                    case INPUT -> {
                        if (stackList.stream().anyMatch(stack -> stack.getItem() instanceof PotionItem)) {
                            if (hasPotion) continue;
                            hasPotion = true;
                        }
                        inputs.add(Ingredient.of(stackList.stream()));
                    }
                    case OUTPUT -> outputs.addAll(stackList);
                }
            }

            if (inputs.isEmpty() || outputs.isEmpty()) return null;

            for (ItemStack resultStack : outputs) {
                ItemStack mapStack = ShopGenerationEvent.getMatchingItemStack(resultStack, craftResultMap.keySet());
                List<ShopGenerationEvent.RecipeInfo> recipeInfoList;

                if (mapStack == null) {
                    recipeInfoList = new ArrayList<>();
                    craftResultMap.put(resultStack, recipeInfoList);
                } else recipeInfoList = craftResultMap.get(mapStack);

                recipeInfoList.add(new ShopGenerationEvent.RecipeInfo(recipeType.getUid().getPath(), resultStack, inputs));
            }

            return null;
        });
    }

}


package invoker54.xpshop.event.generation.recipe;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class GatherIngredientsEvent extends Event {
    private final Recipe<?> recipe;
    private final List<Ingredient> ingredients = new ArrayList<>();
    public GatherIngredientsEvent(Recipe<?> recipe){
        this.recipe = recipe;
    }
    public Recipe<?> getRecipe(){
        return this.recipe;
    }

    public void setIngredients(List<Ingredient> list){
        this.ingredients.clear();
        this.ingredients.addAll(list);
    }

    public List<Ingredient> getIngredients(){
        return this.ingredients;
    }

    public boolean isDone() {
        return !this.ingredients.isEmpty() || this.isCanceled();
    }
}

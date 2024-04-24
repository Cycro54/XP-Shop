//package invoker54.xpshop.event.generation.recipe;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import invoker54.xpshop.event.generation.stat.PriceEvent;
//import net.minecraft.world.item.ItemStack;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static invoker54.xpshop.event.generation.ShopGenerationEvent.*;
//public class PriceRecipeEvent extends PriceEvent {
//    private final List<ItemStack> itemsBeingChecked;
//    private final InitialRecipeData initialRecipeData;
//    private final List<ItemStack> validIngredients;
//    private final List<ItemStack> invalidIngredients;
//    private final List<ItemStack> allIngredients;
//    private final AtomicDouble counter;
//    private boolean hasBadRecipes = false;
//    private double sum = 0;
//    public PriceRecipeEvent(ItemStack currentItem, AtomicDouble counter, List<ItemStack> itemsBeingChecked) {
//        super(currentItem);
//
//        this.counter = counter;
//
//        this.itemsBeingChecked = new ArrayList<>(itemsBeingChecked);
//        if (getMatchingItemStack(currentItem, this.itemsBeingChecked) == null) this.itemsBeingChecked.add(currentItem);
//
//        this.validIngredients = new ArrayList<>();
//        this.invalidIngredients = new ArrayList<>();
//        this.allIngredients = new ArrayList<>();
//        this.allIngredients.add(currentItem);
//        ItemStack matchingStack = getMatchingItemStack(currentItem, craftResultMap.keySet());
//        if (matchingStack != null){
//            this.initialRecipeData = craftResultMap.get(matchingStack);
//            recordIngredientsFromList(this.initialRecipeData.allIngredients());
//        }
//        else this.initialRecipeData = null;
//    }
//    public InitialRecipeData getRecipeData(){
//        return this.initialRecipeData;
//    }
//    public void setHasBadRecipes(){
//        hasBadRecipes = true;
//    }
//    public boolean hasBadRecipes(){
//        return this.hasBadRecipes;
//    }
//    public List<ItemStack> getItemList(){
//        return new ArrayList<>(this.itemsBeingChecked);
//    }
//    public AtomicDouble getPriceCounter(){return this.counter;}
//    public boolean wasFullyPriced(){
//        //If invalidIngredients list is only 1 higher than validIngredients list, the item was successfully priced.
//        return this.invalidIngredients.isEmpty();
//    }
//    public void recordIngredientsFromList(List<ItemStack> ingredients){
//        for (ItemStack ingredientStack : ingredients){
//         recordIngredient(ingredientStack);
//        }
//    }
//    public void recordIngredientsFromEvent(PriceRecipeEvent event){
//        for (ItemStack ingredientStack : event.allIngredients){
//            recordIngredient(ingredientStack);
//        }
//    }
//    public void recordIngredient(ItemStack ingredientStack){
//        if (getMatchingItemStack(ingredientStack, allIngredients) == null) {
//            this.allIngredients.add(ingredientStack);
//
//            if (getMatchingItemStack(ingredientStack, itemsBeingChecked) == null){
//                this.validIngredients.add(ingredientStack);
//            }
//            else this.invalidIngredients.add(ingredientStack);
//        }
//    }
//    public List<ItemStack> getValidIngredients(){
//        return this.validIngredients;
//    }
//    public List<ItemStack> getInvalidIngredients(){
//        return this.invalidIngredients;
//    }
//    public void addToSum(double sum){
//        this.sum += sum;
//    }
//    public double getSum(){return this.sum;}
//}

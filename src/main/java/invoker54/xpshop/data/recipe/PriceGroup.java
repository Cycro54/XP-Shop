package invoker54.xpshop.data.recipe;

import invoker54.xpshop.data.PriceList;

import java.util.ArrayList;
import java.util.List;

public class PriceGroup {
    private final ItemData itemData;
    private final List<PriceGroup> groups;
    private final GroupOperation op;
    private PriceList.PriceInfo priceInfo;
    private final List<ItemData> ingredientList;
    
    public PriceGroup(ItemData itemData, List<PriceGroup> groups, GroupOperation op, List<ItemData> ingredientList){
        this.itemData = itemData;
        this.groups = groups;
        this.op = op;
        this.priceInfo = null;
        if (ingredientList != null) this.ingredientList = ingredientList;
        else this.ingredientList = getIngredients();
    }
    
    public PriceList.PriceInfo getResult(){
        if (priceInfo != null) return priceInfo;

        List<PriceList.PriceInfo> infoList = new ArrayList<>();
        if (this.itemData != null && this.itemData.baseInfo.highPrice() != 0) infoList.add(this.itemData.baseInfo);
        groups.forEach((group) -> infoList.add(group.getResult()));
        priceInfo = op.calculate(infoList);
        return priceInfo;
    }

    public ArrayList<ItemData> getIngredients(){
        ArrayList<ItemData> newList = new ArrayList<>();
        if (this.ingredientList == null){
            if (this.itemData != null) newList.add(this.itemData);
            for (PriceGroup group : this.groups){
                for (ItemData ingredient : group.getIngredients()){
                    if (!newList.contains(ingredient)) newList.add(ingredient);
                }
            }
            return newList;
        }

        return new ArrayList<>(this.ingredientList);
    }

    public PriceGroup duplicate(List<ItemData> invalidList, List<ItemData> checkList){
        if (checkList == null) checkList = new ArrayList<>();
        List<PriceGroup> myGroups = new ArrayList<>();
        boolean hasChanged = false;

        for (PriceGroup priceGroup : this.groups){
            if (invalidList.contains(this.itemData)){
                hasChanged = true;
                myGroups.add(new PriceGroup(null, new ArrayList<>(), priceGroup.op, checkList));
            }
            else{
                if (this.itemData != null) checkList.add(this.itemData);
                PriceGroup similarGroup = priceGroup.duplicate(invalidList, checkList);
                if (similarGroup != priceGroup) hasChanged = true;
                myGroups.add(similarGroup);
            }
        }

        return hasChanged ? new PriceGroup(this.itemData, myGroups, this.op, checkList) : this;
    }

    public interface GroupOperation{
        PriceList.PriceInfo calculate(List<PriceList.PriceInfo> infoList);
    }
}

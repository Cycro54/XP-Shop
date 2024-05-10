package invoker54.xpshop.data.recipe;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import net.minecraft.world.item.Item;
import oshi.jna.platform.mac.SystemB;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class PriceGroup {
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    private final ItemData itemData;
    private final Set<PriceGroup> groups;
    private final GroupOperation op;
    private PriceList.PriceInfo priceInfo;
    public PriceGroup(ItemData itemData, @Nonnull Set<PriceGroup> groups, GroupOperation op){
        this.itemData = itemData;
        this.groups = groups;
        this.op = op;
        this.priceInfo = null;
    }

    public PriceGroup setOp(PriceGroup opGroup){
        return new PriceGroup(this.itemData, this.groups, opGroup.op);
    }

    public boolean sameOps(PriceGroup group){
        return this.op == group.op;
    }

    public void setGroups(List<PriceGroup> groupList){
        this.groups.clear();
        this.groups.addAll(groupList);
    }
    public PriceList.PriceInfo getResult(){
        if (priceInfo != null){
            return priceInfo;
        }
        if (groups.isEmpty()){
            this.priceInfo = PriceList.emptyPrice;
            return priceInfo;
        }

        List<PriceList.PriceInfo> infoList = new ArrayList<>();
        if (this.itemData != null && this.itemData.baseInfo.highPrice() != 0) infoList.add(this.itemData.baseInfo);
        groups.forEach((group) -> infoList.add(group.getResult()));
        this.priceInfo = op.calculate(infoList);

        return priceInfo;
    }
    
    public Set<PriceGroup> getGroups(){
        return this.groups;
    }

    public static PriceGroup duplicate(PriceGroup selectedGroup, Set<ItemData> checkList, @Nonnull Set<ItemData> prevInvalidSet) {
        boolean iHaveItem = (selectedGroup.itemData != null);
        ItemData.PriceBranch branch = null;

        if (iHaveItem) {
            branch = selectedGroup.itemData.getBranch(checkList, selectedGroup);
            if (branch != null){
                prevInvalidSet = new HashSet<>(branch.invalidList());
                selectedGroup = branch.group();
            }
        }

        Set<ItemData> myInvalidSet = (iHaveItem && branch == null) ? new HashSet<>() : prevInvalidSet;
        Set<ItemData> itemsToRemove = new HashSet<>();
        selectedGroup.groups.forEach((group) -> {
            if (group.itemData == null) return;
            if (!checkList.contains(group.itemData)) {
                itemsToRemove.add(group.itemData);
            }
        });
        checkList.addAll(itemsToRemove);
        Set<PriceGroup> newGroups = new HashSet<>();

        for (PriceGroup childGroup : selectedGroup.groups) {
            //This will happen if we already have the item
            if (childGroup.itemData != null && !itemsToRemove.contains(childGroup.itemData)) {
                myInvalidSet.add(childGroup.itemData);
            } else {
                PriceGroup similarGroup = duplicate(childGroup, checkList, myInvalidSet);
                newGroups.add(similarGroup);
            }
        }

        PriceGroup finalGroup;
        //This will happen if all the groups stayed the same.
        if (selectedGroup.groups.size() == newGroups.size() && newGroups.containsAll(selectedGroup.groups)) {
            finalGroup = selectedGroup;
        } else {
            finalGroup = new PriceGroup(selectedGroup.itemData, newGroups, selectedGroup.op);
            if (iHaveItem && (branch == null || branch.invalidList().size() != myInvalidSet.size())) {
                selectedGroup.itemData.addBranch(myInvalidSet, finalGroup);
            }
        }

        checkList.removeAll(itemsToRemove);
        return finalGroup;
    }

    public interface GroupOperation{
        PriceList.PriceInfo calculate(List<PriceList.PriceInfo> infoList);
    }
}
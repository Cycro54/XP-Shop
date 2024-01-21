package invoker54.xpshop.common.data;

import invoker54.xpshop.XPShop;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ShopData {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    protected static final File filePath = new File(getPath().toUri());
    public static final ArrayList<CategoryEntry> catEntries = new ArrayList<>();
    public static final Map<ItemStack, SellEntry> sellEntries = new ConcurrentHashMap<>();
    public static final ArrayList<BuyEntry> buyEntries = new ArrayList<>();

    public static CompoundNBT serialize(){
        CompoundNBT mainNBT = new CompoundNBT();

        //region Shop NBT
        //This is where Categories will reside
        CompoundNBT shopNBT = new CompoundNBT();
        for (int a = 0; a < catEntries.size(); a++){
            CompoundNBT catEntryNBT = new CompoundNBT();
            //Name of the category
            catEntryNBT.putString("name", catEntries.get(a).categoryName);

            //category item
            catEntryNBT.put("categoryItem", catEntries.get(a).categoryItem.serializeNBT());

            CategoryEntry catEntry = catEntries.get(a);
            for (int b = 0; b < catEntry.entries.size(); b++){
                BuyEntry entry =  catEntry.entries.get(b);

                //Label each buyEntryNBT by number
                catEntryNBT.put(String.valueOf(b), entry.serialize());
            }
            //Label how many entries there are for this category
            catEntryNBT.putInt("size",catEntry.entries.size());

            //Finally place this catEntry into the shopNBT by number
            shopNBT.put(String.valueOf(a), catEntryNBT);
        }

        //Record the number of category entries inside shopNBT
        shopNBT.putInt("size", catEntries.size());

        //Place shopNBT inside mainNBT
        mainNBT.put("shopNBT", shopNBT);
        //endregion
        ArrayList<SellEntry> tempList = new ArrayList<>(sellEntries.values());
        //region Sell NBT
        //This is where the sell entries will reside
        CompoundNBT sellNBT = new CompoundNBT();
        for (int a = 0; a < tempList.size(); a++){
            CompoundNBT sellEntryNBT = new CompoundNBT();

            sellEntryNBT.put("item", tempList.get(a).item.save(new CompoundNBT()));
            sellEntryNBT.putFloat("sellPrice", tempList.get(a).getSellPrice());

            //Add this sellEntryNBT to sellNBT
            sellNBT.put(String.valueOf(a),sellEntryNBT);
        }
        //Record the number of sellentries there are
        sellNBT.putInt("size", tempList.size());

        //Place sellNBT inside of mainNBT
        mainNBT.put("sellNBT", sellNBT);
        //endregion

//        LOGGER.error("THIS IS HOW BIG SHOP NBT IS: " + shopNBT.);
        return mainNBT;
    }

    public static void deserialize(CompoundNBT mainNBT){

//        XPShop.LOGGER.debug("Start Deserialize");
        //region Shop NBT
        catEntries.clear();
        buyEntries.clear();
        CompoundNBT shopNBT = (CompoundNBT) mainNBT.get("shopNBT");

        for (int a = 0; a < shopNBT.getInt("size"); a++){
            CompoundNBT catEntryNBT = (CompoundNBT) shopNBT.get(String.valueOf(a));

            CategoryEntry catEntry = new CategoryEntry();

            catEntry.categoryName = catEntryNBT.getString("name");
//            LOGGER.debug("Cat name: " + catEntry.categoryName);

            catEntry.categoryItem = ItemStack.of((CompoundNBT) catEntryNBT.get("categoryItem"));
//            LOGGER.debug("Cat item: " + catEntry.categoryItem.getDisplayName().getString());

            for (int b = 0; b < catEntryNBT.getInt("size"); b++){
                CompoundNBT buyEntryNBT = (CompoundNBT) catEntryNBT.get(String.valueOf(b));

                //Assign all the values
                BuyEntry buyEntry = new BuyEntry(buyEntryNBT, catEntry);

                //Finally, add to the category buy entry list
                catEntry.entries.add(buyEntry);
                //Also add to buyEntries list for easy access
                buyEntries.add(buyEntry);

//                if (buyEntry.item.getItem() instanceof EnchantedBookItem){
//                    LOGGER.debug(EnchantedBookItem.getEnchantments(buyEntry.item));
//                }
            }
            catEntry.entries.sort(Comparator.comparing(b -> b.item.getHoverName().getString()));

            //Add the categories to the catEntries list
            catEntries.add(catEntry);
        }
        buyEntries.sort(Comparator.comparing(b -> b.item.getHoverName().getString()));

        //endregion

        //region Sell NBT
        sellEntries.clear();
        CompoundNBT sellNBT = (CompoundNBT) mainNBT.get("sellNBT");

        for (int a = 0; a < sellNBT.getInt("size"); a++){
            CompoundNBT sellEntryNBT = (CompoundNBT) sellNBT.get(String.valueOf(a));

            SellEntry sellEntry = new SellEntry(
                    ItemStack.of((CompoundNBT) sellEntryNBT.get("item")),
                    sellEntryNBT.getFloat("sellPrice")
            );

            //Finally add to the sellEntries list
            sellEntries.put(sellEntry.item, sellEntry);
        }
        //endregion

//        XPShop.LOGGER.debug("End Deserialize");
    }

    protected static Path getPath(){
        return FMLPaths.CONFIGDIR.get().resolve("xp_shop_data.nbt");
    }
    public static void grabDefault() throws IOException {

//        ResourceLocation DEFAULT_LOC = new ResourceLocation(XPShop.MOD_ID, "xp_shop_data.nbt");
        try {
            InputStream in = XPShop.class.getClassLoader().getResource("assets/xp_shop/xp_shop_data.nbt").openStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            LOGGER.debug("START");

            CompoundNBT nbt = CompressedStreamTools.readCompressed(in);

            deserialize(nbt);
//            LOGGER.debug("END");
        }
        catch (Exception ex) {
            LOGGER.error("THERE WAS AN ERROR!");
            LOGGER.debug(ex);
        }




//        deserialize(CompressedStreamTools.readCompressed(new File(
//                XPShop.class.getResource("xp_shop_data.nbt").toURI()
//        )));
    }

    public static void writeFile(){
        try {
            if (!filePath.isFile()) {
                Files.createDirectories(getPath().getParent());
            }

            CompoundNBT mainNBT = serialize();

            CompressedStreamTools.writeCompressed(mainNBT, filePath);
        } catch (Exception ex) {
            LOGGER.debug(ex);
        }
    }

    public static void readFile(){
        try {
            if (!filePath.isFile()) {
                Files.createDirectories(getPath().getParent());
                ShopData.grabDefault();
            }
            else {
                CompoundNBT mainNBT = CompressedStreamTools.readCompressed(filePath);
                deserialize(mainNBT);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }

    }

    public static ItemStack getMatchingStack(ItemStack stackToFind, Collection<ItemStack> stackList) {
        ItemStack alikeStack = null;
        if (stackToFind == null) return null;

        for (ItemStack stack : stackList) {
            boolean sameItem = stackToFind.sameItem(stack);
            boolean matchingTags = ItemStack.tagMatches(stackToFind, stack);
            if (sameItem && matchingTags) return stack;
            if (sameItem && sortaMatchTags(stack, stackToFind)) alikeStack = stack;
        }
        return alikeStack;
    }

    public static boolean sortaMatchTags(ItemStack stackA, ItemStack stackB){
        if (!stackA.hasTag() && !stackB.hasTag()) return true;
        if (!stackA.hasTag() && stackB.hasTag()) return true;
        if (stackA.hasTag() && !stackB.hasTag()) return false;

        CompoundNBT nbtA = stackA.getOrCreateTag();
        CompoundNBT nbtB = stackB.getOrCreateTag();

        if (nbtA.size() > nbtB.size()){
            nbtA = stackB.getOrCreateTag();
            nbtB = stackA.getOrCreateTag();
        }

        for (String entry : nbtA.getAllKeys()){
            if (!nbtB.contains(entry)) return false;
            if (!nbtA.contains(entry)) continue;
            if (!nbtA.get(entry).equals(nbtB.get(entry))) return false;
        }

        return true;
    }
}
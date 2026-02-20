package invoker54.xpshop.common.datagen;

import invoker54.xpshop.XPShop;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class XPShopLanguageprovider extends LanguageProvider {

    public static final List<Pair<String, String>> translationList = new ArrayList<>();

    public static final String containerCategory = XPShop.MOD_ID + ".containers";
    public static final String defaultInvoTextContainer = addTranslation(containerCategory + ".invotext.default", "&dHello world :D");

    public static final String shopCategory = containerCategory + ".shop";

    public static final String tabShopCategory = shopCategory + ".tab_shop";
    public static final String tabShopTypeName = addTranslation(tabShopCategory + ".type_name", "Tab Shop");
    public static final String tabShopTypeDescription = addTranslation(tabShopCategory + ".type_description", "(Default) Allows you to have multiple shop sections");

    public static final String shopSectionCategory = shopCategory + ".section";

    public static final String categorySectionCategory = shopSectionCategory + ".category";
    public static final String categorySectionTypeName = addTranslation(categorySectionCategory + ".type_name", "Category Section");
    public static final String categorySectionTypeDescription = addTranslation(categorySectionCategory + ".type_description", "(Default) Products will be in list view and sorted by category");

    public static final String sectionProductCategory = shopSectionCategory + ".product";

    public static final String categoryProductCategory = sectionProductCategory + ".category";
    public static final String categoryProductTypeName = addTranslation(categoryProductCategory + ".type_name", "Category Product");
    public static final String categoryProductTypeDescription = addTranslation(categoryProductCategory + ".type_description",
            "Modular product that shows the name, icon, costs (among other shtuff)");

    public static final String productPropertyCategory = sectionProductCategory + ".property";

    public static final String lootItemPropertyCategory = productPropertyCategory + ".loot_item";
    public static final String lootItemPropertyTypeName = addTranslation(lootItemPropertyCategory + ".type_name", "Loot");
    public static final String lootItemPropertyTypeDescription = addTranslation(lootItemPropertyCategory + ".type_description", "Grabs things randomly from a list");

    public static final String itemPropertyCategory = productPropertyCategory + ".item";
    public static final String itemPropertyTypeName = addTranslation(itemPropertyCategory + ".type_name", "Item");
    public static final String itemPropertyTypeDescription = addTranslation(itemPropertyCategory + ".type_description", "Gives a chosen itemstack");

    public static final String commandPropertyCategory = productPropertyCategory + ".command";
    public static final String commandPropertyTypeName = addTranslation(commandPropertyCategory + ".type_name", "Command");
    public static final String commandPropertyTypeDescription = addTranslation(commandPropertyCategory + ".type_description", "Runs a command");

    public static final String screenCategory = XPShop.MOD_ID + ".screen";

    public static final String propertyScreenCategory = screenCategory + ".properties";
    public static final String shopCreateProperties = addTranslation(propertyScreenCategory + ".shop_create", "Create Shop");
    public static final String shopSelectProperties = addTranslation(propertyScreenCategory + ".shop_select", "Select Shop");
    public static final String shopEditProperties = addTranslation(propertyScreenCategory + ".shop_edit", "Edit Shop ([%1$s])");

    public static final String widgetCategory = screenCategory + ".widget";
    public static final String defaultWidgetText = addTranslation(widgetCategory + ".default", "Default widget thing");

    public static final String popupCategory = widgetCategory + ".popup";
    public static final String renamePopup = addTranslation(popupCategory + ".rename", "Rename");
    public static final String editPopup = addTranslation(popupCategory + ".edit", "Edit...");
    public static final String duplicatePopup = addTranslation(popupCategory + ".duplicate", "Duplicate");
    public static final String removePopup = addTranslation(popupCategory + ".remove", "Remove");

    public static final String buttonCategory = widgetCategory + ".button";
    public static final String defaultButtonText = addTranslation(buttonCategory + ".default", "[PlaceHolder]");
    public static final String creatShopButtonText = addTranslation(buttonCategory + ".shop_create", "Create new shop");

    public XPShopLanguageprovider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    protected static String addTranslation(String key, String value){
        translationList.add(Pair.of(key, value));
        return key;
    }

//    protected static String comment(String key, String comment){
//        translationList.add(Pair.of("__comment", comment));
//        return key;
//    }

    @Override
    protected void addTranslations() {
        for (var pair : translationList){
            this.add(pair.getLeft(), pair.getRight());
        }
    }
}

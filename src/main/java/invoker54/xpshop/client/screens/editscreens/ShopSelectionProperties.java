package invoker54.xpshop.client.screens.editscreens;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.InvoTheme;
import invoker54.xpshop.client.widgets.InvoList;
import invoker54.xpshop.client.widgets.buttons.ImageTextButton;
import invoker54.xpshop.client.widgets.buttons.InvoButton;
import invoker54.xpshop.common.data.BasicData;
import invoker54.xpshop.common.data.ShopDataManager;
import invoker54.xpshop.common.data.shops.Shop;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import org.lwjgl.glfw.GLFW;

public class ShopSelectionProperties {
    private static final ModLogger LOGGER = ModLogger.getLogger(ShopSelectionProperties.class, XPShop.debugMode);

    public static void open(){
        try {
            PropertyScreen shopSelectionScreen = new PropertyScreen(InvoText.translate(XPShopLanguageprovider.shopSelectProperties),
                    (screen -> {
                        InvoList propertyList = screen.getPropertyList();

                        InvoText.Properties properties = new InvoText.Properties().setMaxTextSize(18).setMinTextSize(9);
                        InvoButton.Builder createShopButton = new ImageTextButton.Builder().setIconImage(InvoTheme.getAddIcon())
                                .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT, (isClick, button, pMouseX, pMouseY) ->
                                {
                                    if (!isClick) return false;
                                    ShopCreationProperties.open(screen);
//                                    LOGGER.warn("Hello, it's working :3");
                                    return true;
                                }).setMessage(properties.text(InvoText.translate(XPShopLanguageprovider.creatShopButtonText)));
                        propertyList.addEntry(createShopButton.build(screen));

//                        propertyList.addEntry(createShopButton.setMessage(properties.text(InvoText.literal("Create new Shop 1"))).build(screen));
                    }));

            ClientUtil.getMinecraft().setScreen(shopSelectionScreen);

            //I need a create new shop button
            //It will be an image text button that has a plus sign and says "create shop"
            //I have the button
            //and I have invo popup

            for (Shop shop : ShopDataManager.getDataListByType(Shop.class)) {
                //I need to make a shop button that has 3 quick settings (edit, duplicate, remove)
                //And opens the actual shop on left click

                //First the actual shop button

                //Left click should open the shop

                //Right click should set the popup


//                InvoButton shopButton = shopButtonBuilder.build(shopSelectionScreen, selectionList.getEntryZone());
//                selectionList.addEntry(selectionList.getEntryZone(), (entry -> shopButton.setZone(entry.getZoneCopy())),List.of(shopButton));
            }

            LOGGER.error("What's the screen? " + (ClientUtil.getMinecraft().screen.getTitle().getString()));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ImageTextButton.Builder fromBasicData(BasicData data){
        ImageTextButton.Builder buttonBuilder = new ImageTextButton.Builder();
        buttonBuilder.setIconImage(InvoImage.fromTag(data.getIcon()));
        buttonBuilder.setMessage(data.getName());

        return buttonBuilder;
    }

//    public static InvoButton.Builder makeShopPropertyButton(PropertyScreen screen, Shop shop){
//        InvoButton.Builder builder = fromBasicData(shop);
//
//        //Left click to open shop
//        builder.setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
//                ((isClick, button, pMouseX, pMouseY) -> {
//                    if (!isClick) return false;
//                    ShopScreenInit.openShop(shop);
//                    return true;
//                }));
//
//        //Right click for popup
//        builder.setButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT,
//                ((isClick, button, pMouseX, pMouseY) -> {
//                    if (!isClick) return false;
//                    InvoPopup.Builder popUpBuilder = new InvoPopup.Builder(screen, (float) pMouseX, (float) pMouseY);
//
//                    //Rename Button
//                    InvoText renameText = InvoText.translate(XPShopLanguageprovider.renamePopup).setArgsAndCopy(shop.getSpecificType());
//                    renameText.getProperties().setMaxSplits(2);
//
//                    InvoZone widgetZone = popUpBuilder.getEntryWidgetZone();
//                    widgetZone.setWidth((float) MathUtil.clamp(ClientUtil.getFont().width(renameText.getText()),
//                            widgetZone.width(), screen.getOriginalZone().width()/2F));
//
//                    InvoButton renameButton = new ImageTextButton.Builder()
//                            .setIconImage(InvoImage.fromTexture(ResourceUtil.create(XPShop.MOD_ID, "edit_text")))
//                            .setMessage(renameText)
//                            .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
//                                    (isClickEdit, buttonEdit, pMouseXEdit, pMouseYEdit)  ->
//                                    {
//                                        if (!isClickEdit) return false;
////                                        InvoPopup.Builder popUpBuilder = new InvoPopup.Builder(screen, (float) pMouseX, (float) pMouseY);
//
//
//                                        return true;
//                                    }).build(screen, widgetZone);
//                    popUpBuilder.addEntry(widgetZone,
//                            (entry -> renameButton.setZone(entry.getZoneCopy())), List.of(renameButton));
//
//                    //Edit button
//                    InvoText editText = InvoText.translate(XPShopLanguageprovider.editPopup).setArgsAndCopy(shop.getSpecificType());
//                    editText.getProperties().setMaxSplits(2);
//
//                    widgetZone = popUpBuilder.getEntryWidgetZone();
//                    widgetZone.setWidth((float) MathUtil.clamp(ClientUtil.getFont().width(editText.getText()),
//                            widgetZone.width(), screen.getOriginalZone().width()/2F));
//
//                    InvoButton editButton = new ImageTextButton.Builder()
//                            .setIconImage(InvoImage.fromTexture(ResourceUtil.create(XPShop.MOD_ID, "cog_wheel")))
//                            .setMessage(editText)
//                            .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
//                                    (isClickEdit, buttonEdit, pMouseXEdit, pMouseYEdit)  ->
//                                    {
//                                        if (!isClickEdit) return false;
////                                        PropertyScreen shopSettingsScreen =
////                                                new PropertyScreen(InvoText.translate(XPShopLanguageprovider.editPopup).setArgsAndCopy(shop.getSpecificType()));
//
//                                        return true;
//                                    }).build(screen, widgetZone);
//                    popUpBuilder.addEntry(widgetZone,
//                            (entry -> editButton.setZone(entry.getZoneCopy())), List.of(editButton));
//
//                    //Duplicate Button
//                    InvoText duplicateText = InvoText.translate(XPShopLanguageprovider.duplicatePopup).setArgsAndCopy(shop.getSpecificType());
//                    duplicateText.getProperties().setMaxSplits(2);
//
//                    widgetZone = popUpBuilder.getEntryWidgetZone();
//                    widgetZone.setWidth((float) MathUtil.clamp(ClientUtil.getFont().width(duplicateText.getText()),
//                            widgetZone.width(), screen.getOriginalZone().width()/2F));
//
//                    InvoButton duplicateButton = new ImageTextButton.Builder()
//                            .setIconImage(InvoImage.fromTexture(ResourceUtil.create(XPShop.MOD_ID, "duplicate")))
//                            .setMessage(duplicateText)
//                            .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
//                                    (isClickEdit, buttonEdit, pMouseXEdit, pMouseYEdit)  ->
//                                    {
//                                        if (!isClickEdit) return false;
//
//                                        return true;
//                                    }).build(screen, widgetZone);
//                    popUpBuilder.addEntry(widgetZone,
//                            (entry -> duplicateButton.setZone(entry.getZoneCopy())), List.of(duplicateButton));
//
//                    //Remove Button
//                    InvoText removeText = InvoText.translate(XPShopLanguageprovider.removePopup).setArgsAndCopy(shop.getSpecificType());
//                    removeText.getProperties().setMaxSplits(2);
//
//                    widgetZone = popUpBuilder.getEntryWidgetZone();
//                    widgetZone.setWidth((float) MathUtil.clamp(ClientUtil.getFont().width(removeText.getText()),
//                            widgetZone.width(), screen.getOriginalZone().width()/2F));
//
//                    InvoButton removeButton = new ImageTextButton.Builder()
//                            .setIconImage(InvoImage.fromTexture(ResourceUtil.create(XPShop.MOD_ID, "trash_can")))
//                            .setMessage(removeText)
//                            .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
//                                    (isClickEdit, buttonEdit, pMouseXEdit, pMouseYEdit)  ->
//                                    {
//                                        if (!isClickEdit) return false;
//                                        //Remove function here!
////                                        NetworkHandler.INSTANCE.sendToServer(new ModifyShopDataMsg(shop.getID().toString(), null));
////                                        Have to get the property screen to update...
//                                        return true;
//                                    }).build(screen, widgetZone);
//                    popUpBuilder.addEntry(widgetZone,
//                            (entry -> removeButton.setZone(entry.getZoneCopy())), List.of(removeButton));
//
//                    screen.setPopup(popUpBuilder.build());
//                    return true;
//                }));
//
//        return builder;
//    }
}

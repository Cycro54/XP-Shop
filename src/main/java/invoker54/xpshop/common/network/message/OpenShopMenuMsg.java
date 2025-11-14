package invoker54.xpshop.common.network.message;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.util.MathUtil;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.editscreens.PropertyScreen;
import invoker54.xpshop.client.widgets.buttons.ImageTextButton;
import invoker54.xpshop.client.widgets.buttons.InvoButton;
import invoker54.xpshop.client.widgets.popup.InvoPopup;
import invoker54.xpshop.common.data.BasicData;
import invoker54.xpshop.common.data.ShopDataManager;
import invoker54.xpshop.common.data.shops.Shop;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.init.ShopScreenInit;
import net.minecraftforge.network.NetworkEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Supplier;

public class OpenShopMenuMsg {
    public static final String shopsPropertyTitle = "xp_shop.screen.properties.shop";
    public static final String shopSettingsPropertyTitle = "xp_shop.screen.properties.shop_settings";

    //This is how the Network Handler will handle the message
    public static void handle(OpenShopMenuMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            PropertyScreen shopSelectionScreen = new PropertyScreen(InvoText.translate(shopsPropertyTitle));

            for (Shop shop : ShopDataManager.getDataListByType(Shop.class)) {
                //I need to make a shop button that has 3 quick settings (edit, duplicate, remove)
                //And opens the actual shop on left click

                //First the actual shop button

                //Left click should open the shop

                //Right click should set the popup


                InvoButton shopButton = shopButtonBuilder.build(shopSelectionScreen, selectionList.getEntryZone());
                selectionList.addEntry(selectionList.getEntryZone(), (entry -> shopButton.setZone(entry.getZoneCopy())),List.of(shopButton));
            }

            ClientUtil.getMinecraft().setScreen(shopSelectionScreen);
        });
        context.setPacketHandled(true);
    }

    public static ImageTextButton.Builder fromBasicData(BasicData data){
        ImageTextButton.Builder buttonBuilder = new ImageTextButton.Builder();
        buttonBuilder.setIconImage(InvoImage.fromTag(data.getIcon()));
        buttonBuilder.setMessage(data.getName());

        return buttonBuilder;
    }

    public static InvoButton.Builder makeShopPropertyButton(PropertyScreen screen, Shop shop){
        InvoButton.Builder builder = fromBasicData(shop);

        //Left click to open shop
        builder.setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
                ((isClick, button, pMouseX, pMouseY) -> {
                    if (!isClick) return false;
                    ShopScreenInit.openShop(shop);
                    return true;
                }));

        //Right click for popup
        builder.setButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                ((isClick, button, pMouseX, pMouseY) -> {
                    if (!isClick) return false;
                    InvoPopup.Builder popUpBuilder = new InvoPopup.Builder(screen, (float) pMouseX, (float) pMouseY);

                    //Rename Button
                    InvoText renameText = InvoText.translate("xp_shop.property.edit").setArgs(shop.getSpecificType());
                    renameText.getProperties().setMaxSplits(2);

                    InvoZone widgetZone = popUpBuilder.getEntryWidgetZone();
                    widgetZone.setWidth((float) MathUtil.clamp(ClientUtil.getFont().width(renameText.getText()),
                            widgetZone.width(), screen.getOriginalZone().width()/2F));

                    InvoButton renameButton = new ImageTextButton.Builder()
                            .setIconImage(InvoImage.fromSprite(ResourceUtil.create(XPShop.MOD_ID, "edit_text")))
                            .setMessage(renameText)
                            .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
                                    (isClickEdit, buttonEdit, pMouseXEdit, pMouseYEdit)  ->
                                    {
                                        if (!isClickEdit) return false;
                                        InvoPopup.Builder popUpBuilder = new InvoPopup.Builder(screen, (float) pMouseX, (float) pMouseY);


                                        return true;
                                    }).build(screen, widgetZone);
                    popUpBuilder.addEntry(widgetZone,
                            (entry -> renameButton.setZone(entry.getZoneCopy())), List.of(renameButton));

                    //Edit button
                    InvoText editText = InvoText.translate("xp_shop.property.edit").setArgs(shop.getSpecificType());
                    editText.getProperties().setMaxSplits(2);

                    widgetZone = popUpBuilder.getEntryWidgetZone();
                    widgetZone.setWidth((float) MathUtil.clamp(ClientUtil.getFont().width(editText.getText()),
                            widgetZone.width(), screen.getOriginalZone().width()/2F));

                    InvoButton editButton = new ImageTextButton.Builder()
                            .setIconImage(InvoImage.fromSprite(ResourceUtil.create(XPShop.MOD_ID, "cog_wheel")))
                            .setMessage(editText)
                            .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
                                    (isClickEdit, buttonEdit, pMouseXEdit, pMouseYEdit)  ->
                                    {
                                        if (!isClickEdit) return false;
                                        PropertyScreen shopSettingsScreen =
                                                new PropertyScreen(InvoText.translate(shopSettingsPropertyTitle).setArgs(shop.getSpecificType()));

                                        return true;
                                    }).build(screen, widgetZone);
                    popUpBuilder.addEntry(widgetZone,
                            (entry -> editButton.setZone(entry.getZoneCopy())), List.of(editButton));

                    //Duplicate Button
                    InvoText duplicateText = InvoText.translate("xp_shop.property.duplicate").setArgs(shop.getSpecificType());
                    duplicateText.getProperties().setMaxSplits(2);

                    widgetZone = popUpBuilder.getEntryWidgetZone();
                    widgetZone.setWidth((float) MathUtil.clamp(ClientUtil.getFont().width(duplicateText.getText()),
                            widgetZone.width(), screen.getOriginalZone().width()/2F));

                    InvoButton duplicateButton = new ImageTextButton.Builder()
                            .setIconImage(InvoImage.fromSprite(ResourceUtil.create(XPShop.MOD_ID, "duplicate")))
                            .setMessage(duplicateText)
                            .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
                                    (isClickEdit, buttonEdit, pMouseXEdit, pMouseYEdit)  ->
                                    {
                                        if (!isClickEdit) return false;

                                        return true;
                                    }).build(screen, widgetZone);
                    popUpBuilder.addEntry(widgetZone,
                            (entry -> duplicateButton.setZone(entry.getZoneCopy())), List.of(duplicateButton));

                    //Remove Button
                    InvoText removeText = InvoText.translate("xp_shop.property.duplicate").setArgs(shop.getSpecificType());
                    removeText.getProperties().setMaxSplits(2);

                    widgetZone = popUpBuilder.getEntryWidgetZone();
                    widgetZone.setWidth((float) MathUtil.clamp(ClientUtil.getFont().width(removeText.getText()),
                            widgetZone.width(), screen.getOriginalZone().width()/2F));

                    InvoButton removeButton = new ImageTextButton.Builder()
                            .setIconImage(InvoImage.fromSprite(ResourceUtil.create(XPShop.MOD_ID, "trash_can")))
                            .setMessage(removeText)
                            .setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT,
                                    (isClickEdit, buttonEdit, pMouseXEdit, pMouseYEdit)  ->
                                    {
                                        if (!isClickEdit) return false;
                                        //Remove function here!
                                        NetworkHandler.INSTANCE.sendToServer(new ModifyShopDataMsg(shop.getID(), null));
                                        Have to get the property screen to update...
                                        return true;
                                    }).build(screen, widgetZone);
                    popUpBuilder.addEntry(widgetZone,
                            (entry -> removeButton.setZone(entry.getZoneCopy())), List.of(removeButton));

                    screen.setPopup(popUpBuilder.build());
                    return true;
                }));

        return builder;
    }
}
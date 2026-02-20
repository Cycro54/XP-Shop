package invoker54.xpshop.client.screens.editscreens;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.widgets.InvoList;
import invoker54.xpshop.client.widgets.buttons.ImageTextButton;
import invoker54.xpshop.client.widgets.buttons.InvoButton;
import invoker54.xpshop.common.data.ShopDataManager;
import invoker54.xpshop.common.data.shops.Shop;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ShopCreationProperties {
    private static final ModLogger LOGGER = ModLogger.getLogger(ShopCreationProperties.class, XPShop.debugMode);

    public static void open(PropertyScreen shopSelectionScreen) {
        try {
            PropertyScreen shopCreationScreen = new PropertyScreen(InvoText.translate(XPShopLanguageprovider.shopCreateProperties),
                    (propertyScreen -> {
                        InvoList invoList = propertyScreen.getPropertyList();
                        List<Shop> shopList = ShopDataManager.getTypes(Shop.class);
                        for (Shop shop : shopList) {
                            ImageTextButton.Builder shopButtonBuilder = new ImageTextButton.Builder();
                            shopButtonBuilder
                                    .setIconImage(InvoImage.fromTag(shop.getTypeIcon())).
                                    setMessage(shop.getTypeName()).
                                    setTooltip(shop.getTypeDescription()).
                            setButton(GLFW.GLFW_MOUSE_BUTTON_LEFT, (isClick, button, pMouseX, pMouseY) ->
                            {
                                if (!isClick) return false;

                                return true;
                            });

                            InvoButton shopButton = shopButtonBuilder.build(propertyScreen);
                            invoList.addEntry(shopButton);
                        }
                    }));

            ClientUtil.getMinecraft().setScreen(shopCreationScreen);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

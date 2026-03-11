package invoker54.xpshop.client.screens.editscreens;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.xpshop.client.widgets.InvoList;
import invoker54.xpshop.client.widgets.InvoTextBox;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.ChatFormatting;

public class ExampleTextProperties {
    public static void open() {
        try {
            PropertyScreen shopCreationScreen = new PropertyScreen(InvoText.translate(XPShopLanguageprovider.shopCreateProperties),
                    (propertyScreen -> {
                        InvoList invoList = propertyScreen.getPropertyList();
                        InvoText message = InvoText.literal("Wazzap");
                        InvoText placeHolder = InvoText.literal("blah blah blah").withStyle(false, ChatFormatting.RED);
                        InvoTextBox textBox = new InvoTextBox(propertyScreen, invoList.getEntryZone(false).splitHeight(1,2),
                                invoList.getEntryZone(true).splitHeight(1, 3), message, placeHolder, ClientUtil.getFont(), 4);
                        invoList.addEntry(textBox);
                    }));

            ClientUtil.getMinecraft().setScreen(shopCreationScreen);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

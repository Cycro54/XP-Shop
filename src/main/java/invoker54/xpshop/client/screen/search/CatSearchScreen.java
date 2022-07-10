package invoker54.xpshop.client.screen.search;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ClientUtil;
import invoker54.xpshop.client.screen.ui.TextBoxUI;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.data.CategoryEntry;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncServerShopMsg;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CatSearchScreen extends SearchScreen {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private final ITextComponent ghostTitleText = new TranslationTextComponent("AddCatScreen.title_text");
    private TextBoxUI titleBox;
    private CategoryEntry targEntry;

    public CatSearchScreen(Screen prevScreen, CategoryEntry entry) {
        super(prevScreen, null);

        targEntry = entry;

        this.onDone = (button) ->
        {
            if (targEntry == null)
                targEntry = new CategoryEntry();

            targEntry.categoryName = this.titleBox.getValue();
            targEntry.categoryItem = this.chosenItem;
            if(!ShopData.catEntries.contains(targEntry))
            ShopData.catEntries.add(targEntry);

            ClientUtil.mC.setScreen(prevScreen);
            NetworkHandler.INSTANCE.sendToServer(new SyncServerShopMsg(ShopData.serialize()));
        };
    }

    @Override
    public void init() {
        super.init();
        XPShop.LOGGER.debug("Starting CAT SCREEN");

        //Make title field
        this.titleBox = new TextBoxUI(this.font, halfWidthSpace + 11, halfHeightSpace + 154,
                72, 11, ghostTitleText, TextBoxUI.defOutColor, TextBoxUI.defInColor);
        this.children.add(this.titleBox);

        if (targEntry != null) {
            searchBox.y = halfHeightSpace + 35 - searchBox.getHeight() - 2;
            titleBox.setValue(targEntry.categoryName);
            chosenItem = targEntry.categoryItem;
            //Delete button
            addButton(new ClientUtil.SimpleButton(halfWidthSpace + 9, halfHeightSpace + 4, 40, 16, ITextComponent.nullToEmpty("Delete"),
                    (button) -> {
                        //Remove all of this categories buy entries
                        for (BuyEntry buyEntry : targEntry.entries) {
                            ShopData.buyEntries.remove(buyEntry);
                        }
                        //Finally delete the category
                        XPShop.LOGGER.debug("Does cat entries have this? " + ShopData.catEntries.contains(targEntry));
                        ShopData.catEntries.remove(targEntry);
                        XPShop.LOGGER.debug("Does it have it now? " + ShopData.catEntries.contains(targEntry));

                        //Now go back to the previous screen
                        ClientUtil.mC.setScreen(prevScreen);

                        NetworkHandler.INSTANCE.sendToServer(new SyncServerShopMsg(ShopData.serialize()));
                        for (CategoryEntry catEntry : ShopData.catEntries) {
                            XPShop.LOGGER.debug(catEntry.categoryName);
                        }
                    }));
        }
    }

    @Override
    public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        super.render(stack, xMouse, yMouse, partialTicks);

        //Now for title
        this.titleBox.render(stack, xMouse, yMouse, partialTicks);
    }

    @Override
    public boolean charTyped(char p_231042_1_, int p_231042_2_) {
        if (this.titleBox.charTyped(p_231042_1_, p_231042_2_)) {

            return true;
        } else {
            return super.charTyped(p_231042_1_, p_231042_2_);
        }
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
            if (this.titleBox.isFocused()){
            //String s = this.titleBox.getValue();
            if (this.titleBox.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_)) {

                return true;
            }
        }

        return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

    @Override
    public boolean isDone() {
        if (this.titleBox.getValue().isEmpty()){
            return false;
        }

        return super.isDone();
    }
}

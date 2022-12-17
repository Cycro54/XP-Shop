package invoker54.xpshop.client.screen.search;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.client.screen.ui.TextBoxUI;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.data.ShopData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public class ItemSearchScreen extends InvSearchScreen {

    public ItemSearchScreen(Screen prevScreen, IChooseItem onDone) {
        super(prevScreen, onDone);
    }
    private final int priceSetColor = new Color(155, 97, 255,255).getRGB();

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private final ITextComponent ghostCountText = new TranslationTextComponent("SearchItemScreen.item_size_text");
    public TextBoxUI countBox;
    @Override
    public void init() {
        super.init();

        //Make title field
        this.countBox = new TextBoxUI(this.font, halfWidthSpace + 11, halfHeightSpace + 154,
                72, 11, ghostCountText, TextBoxUI.defOutColor, TextBoxUI.defInColor);
        addWidget(this.countBox);
    }

    @Override
    public void tick() {
        super.tick();
        this.countBox.tick();
    }

    @Override
    public void renderItemSlot(MatrixStack stack, ItemStack item, int x, int y, boolean inBounds) {
        for (BuyEntry entry : ShopData.buyEntries){
            if (ExtraUtil.itemsMatch(entry.item, item))
                ExtraUtil.blitColor(stack,x, 16, y, 16, priceSetColor);
        }
//
//        if (ShopData.sellEntries.containsKey(item.getItem()))
//            ExtraUtil.blitColor(stack,x, 16, y, 16, priceSetColor);

        super.renderItemSlot(stack, item, x, y, inBounds);
    }

    @Override
    public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        super.render(stack, xMouse, yMouse, partialTicks);

        //Now for title
        this.countBox.render(stack, xMouse, yMouse, partialTicks);
    }

    @Override
    public boolean charTyped(char character, int keyCode) {
        if (countBox.isFocused() && chosenItem != null) {
            //Check if character is parsable
            if (!NumberUtils.isParsable(String.valueOf(character))) return false;

            //Parse the int and make sure it fits in
            if (Integer.parseInt(countBox.getValue() + character) > chosenItem.getMaxStackSize()) return false;

            if (countBox.charTyped(character,keyCode)) return true;
        }
        else {
            return super.charTyped(character, keyCode);
        }

        return false;
    }

//    @Override
//    public boolean keyPressed(int keyCode, int p_231046_2_, int p_231046_3_) {
//        if (this.countBox.isFocused()){
//            //String s = this.titleBox.getValue();
//            if (this.countBox.keyPressed(keyCode, p_231046_2_, p_231046_3_)) {
//                return true;
//            }
//        }
//
//        return super.keyPressed(keyCode, p_231046_2_, p_231046_3_);
//    }

    @Override
    public boolean mouseClicked(double xMouse, double yMouse, int mouseButton) {
       if (chosenItem != null && !countBox.getValue().isEmpty() && chosenItem.getMaxStackSize() < Integer.parseInt(countBox.getValue())){
        countBox.setValue(String.valueOf(chosenItem.getMaxStackSize()));
       }

       return super.mouseClicked(xMouse, yMouse, mouseButton);
    }

    @Override
    public boolean isDone() {
        if (this.countBox.getValue().isEmpty()){
            return false;
        }

        return super.isDone();
    }

    @Override
    public void onDone() {
        this.chosenItem.setCount(Integer.parseInt(countBox.getValue()));
        this.onDone.onItemChosen(this.chosenItem);
    }
}

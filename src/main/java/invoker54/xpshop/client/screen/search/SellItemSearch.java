package invoker54.xpshop.client.screen.search;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.client.screen.ui.TextBoxUI;
import invoker54.xpshop.common.data.SellEntry;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncServerShopMsg;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ISearchTree;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static invoker54.xpshop.client.screen.ShopScreen.SHOP_LOCATION;
import static invoker54.xpshop.common.data.ShopData.getMatchingStack;

public class SellItemSearch  extends SearchScreen{

    private final ITextComponent ghostPriceText = new TranslationTextComponent("SellItemScreen.ghost_text");
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean showSellOnly = false;
    private boolean fullStack = false;
    private TextBoxUI priceBox;
    protected boolean shiftHeld = false;

    private final int priceSetColor = new Color(155, 97, 255,255).getRGB();

    public SellItemSearch(Screen prevScreen, IChooseItem onDone) {
        super(prevScreen, onDone);
    }

    @Override
    public void init() {
        super.init();

        cancelButton.active = false;
        cancelButton.hidden = true;
        doneButton.active = false;
        doneButton.hidden = true;

        searchBox.y = halfHeightSpace + 35 - searchBox.getHeight() - 2;

        //Switch button
        addButton(new ExtraUtil.SimpleButton(halfWidthSpace + 9, halfHeightSpace + 4,40,16, ITextComponent.nullToEmpty("Switch"),
                (button) -> {
                    showSellOnly = !showSellOnly;
                    refreshSearchResults();
                }));

        //Full Stack bool button
        addButton(new ExtraUtil.SimpleButton(halfWidthSpace + 11 + 72 + 10, halfHeightSpace + 154,90,16, ITextComponent.nullToEmpty("Full Stack: " + (fullStack)),
                (button) -> {
                    fullStack = !fullStack;
                    button.setMessage(ITextComponent.nullToEmpty("Full Stack: " + (fullStack)));
                }));

        //Change to Buy screen button
        ExtraUtil.SimpleButton buyButton = new ExtraUtil.SimpleButton(halfWidthSpace + 3,halfHeightSpace + 177,14,21, null,(button) ->{
            //If the player is in creative, set the screen to add sell item screen
            ExtraUtil.mC.setScreen(prevScreen);
        });
        buyButton.hidden = true;
        addButton(buyButton);



        //Make price field
        if (this.priceBox == null) {
            this.priceBox = new TextBoxUI(this.font, halfWidthSpace + 11, halfHeightSpace + 154,
                    72, 11, ghostPriceText, TextBoxUI.defOutColor, TextBoxUI.defInColor);
        }
        this.priceBox.x = halfWidthSpace + 11;
        this.priceBox.y = halfHeightSpace + 154;
        if (this.priceBox.getValue().isEmpty()) this.priceBox.setValue("0");
        this.children.add(this.priceBox);
    }

    @Override
    protected void renderTooltip(MatrixStack stack, ItemStack item, int xMouse, int yMouse) {
        SellEntry targEntry = null;
        ItemStack matchingStack = getMatchingStack(item, ShopData.sellEntries.keySet());

        if (matchingStack != null)
            targEntry = ShopData.sellEntries.get(matchingStack);

        List<ITextComponent> textList = getTooltipFromItem(item);

        if (targEntry != null) {
            String sellTxt = "\247a\247lSELL PRICE: " + "\247r" + (targEntry.getSellPrice());
            textList.add(ITextComponent.nullToEmpty(sellTxt));
        }
        if (!ItemTags.getAllTags().getMatchingTags(item.getItem()).isEmpty()){
            textList.add(ITextComponent.nullToEmpty("SHIFT + CLICK to add by tag"));
        }
        renderComponentTooltip(stack, textList, xMouse, yMouse);
    }

    @Override
    protected void refreshSearchResults() {
        if (showSellOnly) {
            searchList.clear();
//            LOGGER.debug("Refreshing items");
            this.scrollOffs = 0;

            String s = this.searchBox.getValue();

            ISearchTree<ItemStack> isearchtree;
            if (s.startsWith("@")) {
                s = s.substring(1);
                isearchtree = this.minecraft.getSearchTree(SearchTreeManager.CREATIVE_TAGS);
                this.updateVisibleTags(s);
            } else {
                isearchtree = this.minecraft.getSearchTree(SearchTreeManager.CREATIVE_NAMES);
            }
            ArrayList<ItemStack> tempList = new ArrayList<>(isearchtree.search(s.toLowerCase(Locale.ROOT)));
            ArrayList<SellEntry> foundEntries = new ArrayList<>();

            for (ItemStack itemStack : tempList){
                ItemStack matchingStack = getMatchingStack(itemStack, ShopData.sellEntries.keySet());
                if (matchingStack != null)
                    foundEntries.add(ShopData.sellEntries.get(matchingStack));
            }

            //Now sort that list
            foundEntries.sort((s1, s2) -> Float.compare(s2.getSellPrice(), s1.getSellPrice()));

            for(SellEntry entry : foundEntries) searchList.add(entry.item);

//            LOGGER.debug("The size of the search list is2: " + searchList.size());
//            for (ItemStack item : searchList) {
//                LOGGER.debug(item.getDisplayName().getString());
//            }

            recalcItemRenderList();
        }

        else {
            super.refreshSearchResults();
        }
    }

    @Override
    public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        super.render(stack, xMouse, yMouse, partialTicks);

        //Next bind the shop texture
        ExtraUtil.TEXTURE_MANAGER.bind(SHOP_LOCATION);

        //Render buy flag
        ExtraUtil.blitImage(stack,halfWidthSpace + 3, 14,halfHeightSpace + 176,21,162, 28, 177, 42,256);
        //Render Sell flag
        ExtraUtil.blitImage(stack,halfWidthSpace + 3 + 14, 14,halfHeightSpace + 176,28,134, 28, 177, 56,256);

        String txtToRender = showSellOnly ? "Sell Entries" : "All Items";

        drawCenteredString(stack,font, txtToRender, halfWidthSpace + (189/2), halfHeightSpace + 4, TextFormatting.WHITE.getColor());

        this.priceBox.render(stack, xMouse, yMouse, partialTicks);
    }

    @Override
    public void renderItemSlot(MatrixStack stack, ItemStack item, int x, int y, boolean inBounds) {
        ItemStack matchingStack = getMatchingStack(item, ShopData.sellEntries.keySet());
        if (matchingStack != null)
            ExtraUtil.blitColor(stack,x, 16, y, 16, priceSetColor);

        super.renderItemSlot(stack, item, x, y, inBounds);
    }

    @Override
    public boolean mouseClicked(double xMouse, double yMouse, int mouseButton) {
        if (hoverItem != null) {
            if (mouseButton == 0) {
                chosenItem = null;

                //This is for tags
                if (shiftHeld){
                    //If the price box is empty, set it to 0
                    if (priceBox.getValue().isEmpty()) priceBox.setValue("0");

                    if (!NumberUtils.isParsable(priceBox.getValue())) return super.mouseClicked(xMouse,yMouse,mouseButton);
                    float sellPrice = Float.parseFloat(priceBox.getValue())/(fullStack ? hoverItem.getMaxStackSize() : 1);
                    ClientUtil.mC.setScreen(new TagSearchScreen(hoverItem.getItem(), sellPrice, this));
                    shiftHeld = false;
                    return true;
                }

                ItemStack matchingStack = getMatchingStack(hoverItem, ShopData.sellEntries.keySet());

                if (matchingStack != null){
                    SellEntry entry = ShopData.sellEntries.get(matchingStack);
                    if (NumberUtils.isParsable(priceBox.getValue()))
                        entry.setPrice(Float.parseFloat(priceBox.getValue())/(fullStack ? hoverItem.getMaxStackSize() : 1));
                }
                else {
                    ItemStack itemStack = new ItemStack(hoverItem.getItem());
                    float sellPrice;

                    if (!NumberUtils.isParsable(priceBox.getValue())) priceBox.setValue(priceBox.getValue() + "0");

                    sellPrice = Float.parseFloat(priceBox.getValue());

                    if (fullStack) sellPrice = sellPrice/(float) hoverItem.getMaxStackSize();

                    ShopData.sellEntries.put(itemStack, new SellEntry(itemStack, sellPrice));
                }
//                refreshSearchResults();
                NetworkHandler.sendToServer(new SyncServerShopMsg(ShopData.serialize()));
            }
            else if (mouseButton == 1){
                //This is for tags
                if (shiftHeld){
                    ClientUtil.mC.setScreen(new TagSearchScreen(hoverItem.getItem(), 0, this));
                    shiftHeld = false;
                    return true;
                }

                ItemStack matchingStack = getMatchingStack(hoverItem, ShopData.sellEntries.keySet());

                if (matchingStack != null){
                    ShopData.sellEntries.remove(matchingStack);
                    NetworkHandler.sendToServer(new SyncServerShopMsg(ShopData.serialize()));
                }
            }

            return true;
        }
        return super.mouseClicked(xMouse,yMouse,mouseButton);
    }

    @Override
    public boolean charTyped(char character, int keyCode) {
        if (priceBox.isFocused()) {

            if (character == '.') {
                if (priceBox.getValue().contains(".")) {
                    return false;
                } else if (priceBox.getValue().isEmpty()) {
                    priceBox.charTyped('0', InputMappings.getKey("key.keyboard.keypad.0").getValue());
                }
                return priceBox.charTyped(character,keyCode);
            }
            //Check if character is parsable
            if (!NumberUtils.isParsable(String.valueOf(character))) return super.charTyped(character, keyCode);

            return priceBox.charTyped(character, keyCode);
        }
        else {
            return super.charTyped(character, keyCode);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int p_231046_2_, int p_231046_3_) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT){
            shiftHeld = true;
        }

        if (this.priceBox.isFocused()){
            //String s = this.titleBox.getValue();
            if (this.priceBox.keyPressed(keyCode, p_231046_2_, p_231046_3_)) {
                return true;
            }
        }

        return super.keyPressed(keyCode, p_231046_2_, p_231046_3_);
    }

    @Override
    public boolean keyReleased(int keyCode, int p_223281_2_, int p_223281_3_) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT){
            shiftHeld = false;
        }

        return super.keyReleased(keyCode, p_223281_2_, p_223281_3_);
    }
}

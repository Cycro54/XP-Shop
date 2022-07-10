package invoker54.xpshop.client.screen.search;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.xpshop.client.ClientUtil;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.math.NumberUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static invoker54.xpshop.client.screen.ShopScreen.SHOP_LOCATION;

public class SellItemSearch  extends SearchScreen{

    private final ITextComponent ghostPriceText = new TranslationTextComponent("SellItemScreen.ghost_text");
    private boolean showSellOnly = false;
    private boolean fullStack = false;
    private TextBoxUI priceBox;

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
        addButton(new ClientUtil.SimpleButton(halfWidthSpace + 9, halfHeightSpace + 4,40,16, ITextComponent.nullToEmpty("Switch"),
                (button) -> {
                    showSellOnly = !showSellOnly;
                    refreshSearchResults();
                }));

        //Full Stack bool button
        addButton(new ClientUtil.SimpleButton(halfWidthSpace + 11 + 72 + 10, halfHeightSpace + 154,90,16, ITextComponent.nullToEmpty("Full Stack: " + (fullStack)),
                (button) -> {
                    fullStack = !fullStack;
                    button.setMessage(ITextComponent.nullToEmpty("Full Stack: " + (fullStack)));
                }));

        //Change to Buy screen button
        ClientUtil.SimpleButton buyButton = new ClientUtil.SimpleButton(halfWidthSpace + 3,halfHeightSpace + 177,14,21, null,(button) ->{
            //If the player is in creative, set the screen to add sell item screen
            ClientUtil.mC.setScreen(prevScreen);
        });
        buyButton.hidden = true;
        addButton(buyButton);



        //Make price field
        this.priceBox = new TextBoxUI(this.font, halfWidthSpace + 11, halfHeightSpace + 154,
                72, 11, ghostPriceText, TextBoxUI.defOutColor, TextBoxUI.defInColor);
        this.children.add(this.priceBox);
    }

    @Override
    protected void renderTooltip(MatrixStack stack, ItemStack item, int xMouse, int yMouse) {
        SellEntry targEntry = null;

        if (ShopData.sellEntries.containsKey(item.getItem()))
            targEntry = ShopData.sellEntries.get(item.getItem());


        if (targEntry == null) {
            super.renderTooltip(stack, item, xMouse, yMouse);
        }
        else {
            List<ITextComponent> textList = getTooltipFromItem(item);

            String sellTxt = "\247a\247lSELL PRICE: " + "\247r" + (targEntry.getSellPrice());
            textList.add(ITextComponent.nullToEmpty(sellTxt));
            renderComponentTooltip(stack, textList, xMouse, yMouse);
        }
    }

    @Override
    protected void refreshSearchResults() {
        if (showSellOnly) {
            searchList.clear();
            LOGGER.debug("Refreshing items");
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
                if (ShopData.sellEntries.containsKey(itemStack.getItem()))
                    foundEntries.add(ShopData.sellEntries.get(itemStack.getItem()));
            }

            //Now sort that list
            foundEntries.sort((s1, s2) -> Float.compare(s2.getSellPrice(), s1.getSellPrice()));

            for(SellEntry entry : foundEntries) searchList.add(entry.item);

            LOGGER.debug("The size of the search list is2: " + searchList.size());
            for (ItemStack item : searchList) {
                LOGGER.debug(item.getDisplayName().getString());
            }

            recalcItemRenderList();
        }

        else {
            super.refreshSearchResults();
        }
    }

    @Override
    public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        super.render(stack, xMouse, yMouse, partialTicks);

        this.priceBox.render(stack, xMouse, yMouse, partialTicks);

        //Next bind the shop texture
        ClientUtil.TEXTURE_MANAGER.bind(SHOP_LOCATION);

        //Render buy flag
        ClientUtil.blitImage(stack,halfWidthSpace + 3, 14,halfHeightSpace + 176,21,162, 28, 177, 42,256);
        //Render Sell flag
        ClientUtil.blitImage(stack,halfWidthSpace + 3 + 14, 14,halfHeightSpace + 176,28,134, 28, 177, 56,256);

        String txtToRender = showSellOnly ? "Sell Entries" : "All Items";

        drawCenteredString(stack,font, txtToRender, halfWidthSpace + (189/2), halfHeightSpace + 4, TextFormatting.WHITE.getColor());
    }

    @Override
    public void renderItemSlot(MatrixStack stack, ItemStack item, int x, int y, boolean inBounds) {
        if (ShopData.sellEntries.containsKey(item.getItem()))
        ClientUtil.blitColor(stack,x, 16, y, 16, priceSetColor);

        super.renderItemSlot(stack, item, x, y, inBounds);
    }

    @Override
    public boolean mouseClicked(double xMouse, double yMouse, int mouseButton) {
        if (hoverItem != null) {
            if (mouseButton == 0) {
                chosenItem = null;
                if (ShopData.sellEntries.containsKey(hoverItem.getItem())){
                    SellEntry entry = ShopData.sellEntries.get(hoverItem.getItem());
                    if (NumberUtils.isParsable(priceBox.getValue()))
                        entry.setPrice(Float.parseFloat(priceBox.getValue())/(fullStack ? hoverItem.getMaxStackSize() : 1));
                }
                else {
                    ItemStack itemStack = new ItemStack(hoverItem.getItem());
                    float sellPrice;

                    if (!NumberUtils.isParsable(priceBox.getValue())) priceBox.setValue(priceBox.getValue() + "0");

                    sellPrice = Float.parseFloat(priceBox.getValue());

                    if (fullStack) sellPrice = sellPrice/(float) hoverItem.getMaxStackSize();

                    ShopData.sellEntries.put(itemStack.getItem(), new SellEntry(itemStack, sellPrice));
                }
                refreshSearchResults();
                NetworkHandler.INSTANCE.sendToServer(new SyncServerShopMsg(ShopData.serialize()));
            }
            else if (mouseButton == 1){
                if (ShopData.sellEntries.containsKey(hoverItem.getItem())){
                    ShopData.sellEntries.remove(hoverItem.getItem());
                    NetworkHandler.INSTANCE.sendToServer(new SyncServerShopMsg(ShopData.serialize()));
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
            if (!NumberUtils.isParsable(String.valueOf(character))) return false;

            return priceBox.charTyped(character, keyCode);
        }
        else {
            return super.charTyped(character, keyCode);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int p_231046_2_, int p_231046_3_) {
        if (this.priceBox.isFocused()){
            //String s = this.titleBox.getValue();
            if (this.priceBox.keyPressed(keyCode, p_231046_2_, p_231046_3_)) {
                return true;
            }
        }

        return super.keyPressed(keyCode, p_231046_2_, p_231046_3_);
    }
}

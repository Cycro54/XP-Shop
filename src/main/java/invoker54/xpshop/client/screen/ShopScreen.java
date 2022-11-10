package invoker54.xpshop.client.screen;

import com.ibm.icu.text.ArabicShaping;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.client.KeyInit;
import invoker54.xpshop.client.event.RenderXPEvent;
import invoker54.xpshop.client.screen.search.CatSearchScreen;
import invoker54.xpshop.client.screen.search.SellItemSearch;
import invoker54.xpshop.client.screen.ui.TextBoxUI;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.data.CategoryEntry;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.BuyItemMsg;
import invoker54.xpshop.common.network.msg.OpenSellContainerMsg;
import invoker54.xpshop.common.network.msg.UnlockItemMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class ShopScreen extends Screen {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation SHOP_LOCATION = new ResourceLocation(XPShop.MOD_ID,"textures/gui/screen/base_shop_layout.png");
    public static ArrayList<BuyEntry> localBuyEntries;
    public static ArrayList<CategoryEntry> localCatEntries;
    private ArrayList<Button> catButtons = new ArrayList<>();
    private ArrayList<Button> itemButtons = new ArrayList<>();
    private int origButtonY;
    private int CatOffset;
    private int maxCatOffset;
    private int ItemOffset;
    private int maxItemOffset;
    private int pageIndex = 1;
    private ExtraUtil.Bounds catBounds;
    private ExtraUtil.Bounds itemBounds;

    private TextBoxUI searchBox;

    private final int imageWidth = 169;
    private final int imageHeight = 177;
    int halfWidthSpace;
    int halfHeightSpace;

    public ShopScreen(){
        this(null);
    }
    public ShopScreen(@Nullable ArrayList<BuyEntry> buyEntries) {
        super(new TranslationTextComponent("shopScreen.shop_text"));
        localBuyEntries = buyEntries == null ? ShopData.buyEntries : buyEntries;
        localCatEntries = new ArrayList<>();
    }

    @Override
    //This is where I set all initial stuff, like buttons, or positions
    public void init() {
        //XPShop.LOGGER.debug("Start Shop screen");
        //XPShop.LOGGER.debug("I am the main screen right?" + (ClientUtil.mC.screen == this));
        catButtons.clear();

        halfWidthSpace = (width - imageWidth) /2;
        halfHeightSpace = (height - imageHeight) /2;
        origButtonY = halfHeightSpace + 29 + 1;

        searchBox = new TextBoxUI(font, halfWidthSpace + 49,halfHeightSpace + 28 - 16, 109,10,ITextComponent.nullToEmpty("Search..."),
                TextBoxUI.defOutColor,TextBoxUI.defInColor);
        this.children.add(this.searchBox);
        this.buttons.add(searchBox);

        ExtraUtil.SimpleButton sellButton = new ExtraUtil.SimpleButton(halfWidthSpace + 3 + 14,halfHeightSpace + imageHeight,14,21, null,(button) ->{
            XPShop.LOGGER.debug("WILL THIS OPEN CREATIVE MENU?: " + (ExtraUtil.mC.player.isCreative()));
            //If the player is in creative, set the screen to add sell item screen
            if (ExtraUtil.mC.player.isCreative()){
                ExtraUtil.mC.setScreen(new SellItemSearch(this,(Iitem -> {})));
            }

            //Else, open up a temporary container to put the shtuff in
            else {

                NetworkHandler.INSTANCE.sendToServer(new OpenSellContainerMsg());
            }
        });
        addButton(sellButton);

        catBounds = new ExtraUtil.Bounds(halfWidthSpace + 9, 33, halfHeightSpace + 29,139);
        itemBounds = new ExtraUtil.Bounds(halfWidthSpace + 51, 115, halfHeightSpace + 29,139);

        //region category buttons
        maxCatOffset = 0;

        if (ExtraUtil.mC.player.isCreative()) {
            catButtons.add(addButton(new ExtraUtil.AddButton(halfWidthSpace + 9, origButtonY + maxCatOffset, 26, 26,
                    "Add+", catBounds, (button) -> {
                ExtraUtil.mC.setScreen(new CatSearchScreen(this, null));
                //LOGGER.debug("I want you to change the current category later.");
            })));
            maxCatOffset += 26 + 1;
        }


        //I need to grab a list of category from the buy entries then sort them.
        for (BuyEntry entry : localBuyEntries){
            if (!localCatEntries.contains(entry.parentCategory)){
             localCatEntries.add(entry.parentCategory);
            }
        }
        //Sort that list by name
        localCatEntries.sort(Comparator.comparing(item -> item.categoryName));

        //This is old stuff
        CategoryEntry catEntry;
        for (int i = 0; i < localCatEntries.size(); ++i)
        //Lets get cat buttons
        {
            catEntry = localCatEntries.get(i);
            //XPShop.LOGGER.debug( "SHOPDATA has this category right? " + (ShopData.catEntries.contains(catEntry)));
            int index = catButtons.size();
            LOGGER.debug("WHATS THE CURRENT INDEX? " + index);
            catButtons.add(addButton(new CategoryButton(halfWidthSpace + 9, origButtonY + maxCatOffset, 26, 26,
                    catEntry, catBounds, (button) -> {
                //turn back on the prev category
                catButtons.get(pageIndex).active = true;

                //Change the current page
                pageIndex = index;

                //turn off the new category
                button.active = false;

                //Finally refresh the shop items
                refreshItemList("");
            })));

            //this is how many pixels high each button is.
            maxCatOffset += 26 + 1;
        }

        //Make sure the selected cat button is disabled
        if (catButtons.size() != 1 && catButtons.get(pageIndex) instanceof CategoryButton) {
//            if (!catButtons.get(pageIndex).isFocused()) catButtons.get(pageIndex).changeFocus(true);
            catButtons.get(pageIndex).active = false;
            XPShop.LOGGER.debug("I AM DISABLING A BUTTON");
        }

        //This will give us the real max Offset
        maxCatOffset -= 139;
        //endregion

        refreshItemList("");
        XPShop.LOGGER.debug("Finish Shop screen");
    }

    public void refreshItemList(String customSearch){
        //XPShop.LOGGER.debug("Refreshing item list with value: " + customSearch);

        for (Button button : itemButtons){
            buttons.remove(button);
            children.remove(button);
        }

        itemButtons.clear();

        //region item buttons
        maxItemOffset = 0;
        ItemOffset = 0;

        if (localCatEntries.isEmpty()) return;

        if (customSearch.isEmpty()) {
            if (catButtons.size() <= pageIndex) {
                pageIndex = localCatEntries.size() - 1;
            }
            //Use this since the very first item in the category list is an "add category" button.
            int adjustedPageIndex = pageIndex - 1;
            
            //Add item button
            if (ExtraUtil.mC.player.isCreative()) {
                itemButtons.add(addButton(new ExtraUtil.AddButton(halfWidthSpace + 51, origButtonY + maxItemOffset, 106, 22,
                        "Add an item+", itemBounds, (button) -> {
                    ExtraUtil.mC.setScreen(new AddItemScreen(this, localCatEntries.get(adjustedPageIndex), null));
                    //LOGGER.debug("I want you to add items in a bit.");
                })));
                maxItemOffset += 22 + 1;
            }

            if (localCatEntries.size() > adjustedPageIndex) {

                CategoryEntry catEntry = localCatEntries.get(adjustedPageIndex);
                for (int i = 0; i < catEntry.entries.size(); ++i) {
                    if (!localBuyEntries.contains(catEntry.entries.get(i))) continue;
                    itemButtons.add(addButton(new PriceButton(halfWidthSpace + 51, origButtonY + maxItemOffset, 106, 22,
                            itemBounds, catEntry.entries.get(i))));

                    maxItemOffset += 22 + 1;
                }
            }
        }

        else {
            customSearch = customSearch.toLowerCase(Locale.ROOT);
            Pattern pattern = Pattern.compile(customSearch);
            for (BuyEntry entry : localBuyEntries){
                if (pattern.matcher(entry.item.getHoverName().getString().toLowerCase(Locale.ROOT)).find()){
                        itemButtons.add(addButton(new PriceButton(halfWidthSpace + 51, origButtonY + maxItemOffset, 106, 22,
                                itemBounds, entry)));
                        maxItemOffset += 22 + 1;
                }
            }
        }

        maxItemOffset -= 139;
        //endregion
    }

    @Override
    //This is where everything gets rendered
    public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        //First render background
        super.renderBackground(stack);
        //Next bind the shop texture
        ExtraUtil.TEXTURE_MANAGER.bind(SHOP_LOCATION);
        Minecraft.getInstance().getTextureManager().bind(SHOP_LOCATION);
        //Now Render shop
        ExtraUtil.blitImage(stack, halfWidthSpace, imageWidth,
                halfHeightSpace,  178, 0, imageWidth, 0, imageHeight, 256);

        //Render buy flag
        ExtraUtil.blitImage(stack,halfWidthSpace + 3, 14,halfHeightSpace + imageHeight,28,190, 28, imageHeight, 56,256);
        //Render Sell flag
        ExtraUtil.blitImage(stack,halfWidthSpace + 3 + 14, 14,halfHeightSpace + imageHeight,21,106, 28, imageHeight, 42,256);

        //Render ScrollBars
        //Note: as soon as there are more than 5 buttons, the scrollbar will be needed
        renderScrollBar(stack, halfWidthSpace + 36, halfHeightSpace + 29, false);
        //Note As soon as there are more than 14 buttons for items, the scrollbar will be needed
        renderScrollBar(stack, halfWidthSpace + 159, halfHeightSpace + 29, true);

        Button hoverButton = null;
        int offset = 0;

        //Start scissor test
        ExtraUtil.beginCrop(halfWidthSpace + 9, 26, halfHeightSpace + 29,139, true);
        for (int a = 0; a < catButtons.size(); a++){
            Button button = catButtons.get(a);

            button.y = origButtonY + offset + a - CatOffset;

            button.render(stack, xMouse, yMouse, partialTicks);

            if(button.isHovered() && !(button instanceof ExtraUtil.AddButton)) hoverButton = button;

            offset += button.getHeight();
        }
        offset = 0;
        ExtraUtil.endCrop();

        ExtraUtil.beginCrop(halfWidthSpace + 51, 108, halfHeightSpace + 29,139, true);
        for (int a = 0; a < itemButtons.size(); a++){
            Button button = itemButtons.get(a);

            button.y = origButtonY + offset + a - ItemOffset;

            button.render(stack, xMouse, yMouse, partialTicks);

            if(button.isHovered() && !(button instanceof ExtraUtil.AddButton)) hoverButton = button;

            offset += button.getHeight();
        }
        ExtraUtil.endCrop();

        //Draw Experience amount
        String totalXP = Integer.toString(minecraft.player.totalExperience);
        int xPos = halfWidthSpace + imageWidth - font.width(totalXP) - 2;
        int yPos = halfHeightSpace - 10;
        RenderXPEvent.renderXPAmount(stack, font, totalXP, xPos, yPos);

        //Render XP Orb
        ExtraUtil.TEXTURE_MANAGER.bind(SHOP_LOCATION);
        ExtraUtil.blitImage(stack,xPos - 8,7,
                yPos, 7,106,7,249,7,256);

        searchBox.render(stack,xMouse,yMouse,partialTicks);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (hoverButton != null){
            if (hoverButton instanceof ExtraUtil.ItemButton){
                List<ITextComponent> textList = new ArrayList<>();
                if(ExtraUtil.mC.player.isCreative())
                textList.add(ITextComponent.nullToEmpty("\247cRight Click to edit!"));
                textList.add(hoverButton.getMessage());
                renderComponentTooltip(stack, textList, xMouse, yMouse);
            }

            else if (hoverButton instanceof PriceButton){
                PriceButton pButton = (PriceButton) hoverButton;
                List<ITextComponent> textList = getTooltipFromItem(pButton.entry.item);
                if (ExtraUtil.mC.player.isCreative())
                textList.add(0, ITextComponent.nullToEmpty("\247cRight Click to edit!"));

                if (pButton.myStock != null && pButton.myStock.stockLeft < pButton.entry.limitStock) {
                    String stockTxt = "\247c Refresh in: " + pButton.myStock.checkStock(pButton.entry);
                    textList.add(ITextComponent.nullToEmpty(stockTxt));
                }
                renderComponentTooltip(stack, textList, xMouse, yMouse);
            }
        }
    }

//    public boolean mouseDragged(double origX, double origY, int mouseButton, double distanceX, double distanceY) {
//
//        if (ClientUtil.inBounds((float) origX, (float) origY, halfWidth + 36, 6, halfHeight + 29, 139)){
//            CatOffset =
//        }
//
//        else if (ClientUtil.inBounds((float) origX, (float) origY, halfWidth + 159, 6, halfHeight + 29, 139)){
//
//        }
//
//        return super.mouseDragged(origX, origY, mouseButton, distanceX, distanceY);
//    }

    @Override
    public boolean charTyped(char p_231042_1_, int p_231042_2_) {
        String s = this.searchBox.getValue();
        if (this.searchBox.charTyped(p_231042_1_, p_231042_2_)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
                this.refreshItemList(searchBox.getValue());
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int b, int c) {
        if (this.searchBox.isFocused()) {
            String s = this.searchBox.getValue();
            if (this.searchBox.keyPressed(keyCode, b, c)) {
                this.refreshItemList(searchBox.getValue());
                return true;
            }
        }
        else if (this.getFocused() == null) {
            if (minecraft.options.keyInventory.getKey().getValue() == keyCode ||
                    KeyInit.shopKey.keyBind.getKey().getValue() == keyCode) {
                minecraft.setScreen(null);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                minecraft.setScreen(null);
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.getFocused() != null)   this.setFocused(null);

        return super.keyPressed(keyCode, b, c);
    }

    public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
        return super.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_);
    }

    public boolean mouseScrolled(double xMouse, double yMouse, double scrollValue) {
        scrollValue = -MathHelper.sign(scrollValue);

        if (ExtraUtil.inBounds((float) xMouse, (float) yMouse, catBounds)){
            if (canscroll(false) == false) return false;

            CatOffset = MathHelper.clamp(CatOffset + 13 * (int) scrollValue, 0,maxCatOffset);
        }

        else if(ExtraUtil.inBounds((float) xMouse, (float) yMouse, itemBounds)){
            if (canscroll(true) == false) return false;

            ItemOffset = MathHelper.clamp(ItemOffset + 13 * (int) scrollValue, 0, maxItemOffset);
        }

        else {return false;}


        return true;
    }

//    @Override
//    public boolean mouseClicked(double xMouse, double yMouse, int mouseButton) {
//        if (this.getFocused() != null) this.getFocused().mouseClicked(xMouse, yMouse, mouseButton);
//
//        if (!super.mouseClicked(xMouse, yMouse, mouseButton)){
//            this.setFocused((IGuiEventListener) null);
//            return false;
//        }
//        else{
//            return true;
//        }
//    }

    public void renderScrollBar(MatrixStack stack, int x, int y, boolean isItemBar){
        //This is how far the scroll bar can move before stopping
        int pixelSpace = 113;

        float u;
        float imageWidth = 6;
        float v = imageHeight;
        float imageHeight = 27;

        if (canscroll(isItemBar)){
            if(isItemBar) {y += pixelSpace * (ItemOffset/(float)maxItemOffset);}
            else  {y += pixelSpace * (CatOffset/(float)maxCatOffset);}

            u = 0;
        }
        else{
            u = 6;
        }

        ExtraUtil.blitImage(stack, x, (int) imageWidth, y, (int) imageHeight, u, imageWidth, v, imageHeight, 256);
    }

    private boolean canscroll(boolean isItemBar){
        if (isItemBar) return maxItemOffset > 0;

        return maxCatOffset > 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class PriceButton extends Button{

        public BuyEntry entry;
        String txtToRender;
        ExtraUtil.Bounds bounds;
        protected ShopCapability playerCap;
        protected ShopCapability.Stock myStock;
        protected int shadeColor = new Color(0, 0, 0, 194).getRGB();


        public PriceButton(int x, int y, int width, int height, ExtraUtil.Bounds bounds, BuyEntry entry) {
            super(x, y, width, height, ITextComponent.nullToEmpty(entry.item.getDisplayName().getString()), (button) -> {
                ShopCapability cap = ShopCapability.getShopCap(ExtraUtil.mC.player);
                ShopCapability.Stock stock = cap.grabStock(entry);
                //First check if item has a lock item and if it's locked
                if(!cap.isUnlocked(entry)) {
                    //if it does, check if player has enough of that item
                    if (ExtraUtil.mC.player.inventory.countItem(entry.lockItem.getItem()) >= entry.lockItem.getCount()) {
                        //Now make sure the item is now unlocked for this player
                        cap.unlockItem(entry.item);

                        //Send a msg to unlock this item for the player on the server
                        NetworkHandler.INSTANCE.sendToServer(new UnlockItemMsg(entry.serialize()));

                        //Make sure to unlock it for the player on the client as well
                        cap.unlockItem(entry.item);

                        //Reset the screen
                        ((ShopScreen) ExtraUtil.mC.screen).init();
                    }
                    return;
                }

                if (stock != null && stock.stockLeft == 0) return;

                //If not enough xp, deny buy
                if (ExtraUtil.mC.player.totalExperience < entry.buyPrice) return;

                //Reduce stock
                if(stock != null) stock.reduceStock();

                //Now start to buy the item
                NetworkHandler.INSTANCE.sendToServer(new BuyItemMsg(entry.serialize()));
            });
            this.playerCap = ShopCapability.getShopCap(ExtraUtil.mC.player);
            this.myStock = playerCap.grabStock(entry);
            this.visible = true;
            this.entry = entry;
            txtToRender = this.entry.item.getHoverName().getString();
            this.bounds = bounds;

            if(font.width(txtToRender) >= 83){
                txtToRender = font.plainSubstrByWidth(txtToRender,83 - font.width("-"));
                txtToRender += "-";
            }

        }

        @Override
        public void renderButton(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
            ExtraUtil.TEXTURE_MANAGER.bind(SHOP_LOCATION);
            if (this.isHovered){
                this.isHovered = ExtraUtil.inBounds(xMouse, yMouse, bounds);
            }

            int i = this.isHovered ? 22 : 0;

            //Render button
            ExtraUtil.blitImage(stack, this.x,  this.width, this.y, this.height,
                    0, this.width, 212 + i, this.height, 256);

            int j = getFGColor();
            //Render item text
            //103 - font.width(txtToRender)
            font.drawShadow(stack, txtToRender, this.x + 6 + 16, this.y + 3, j | MathHelper.ceil(this.alpha * 255.0F) << 24);

//            //left part of the button
//            this.blit(stack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            //Render item
            itemRenderer.renderAndDecorateItem(entry.item, this.x + 3, this.y + 3);
            //Render item decor
            if (!entry.item.isDamageableItem()) {
                itemRenderer.renderGuiItemDecorations(font, entry.item, this.x + 3, this.y + 3);
            }

            //Render count
            //font.drawShadow(stack,String.valueOf(entry.buyPrice), this.x + 3 + 15, this.y + 3 + 15, j | MathHelper.ceil(this.alpha * 255.0F) << 24);

            int color = ExtraUtil.mC.player.totalExperience >= entry.buyPrice ?
                    TextFormatting.GREEN.getColor() : TextFormatting.RED.getColor();

            int priceSpotX = this.x + this.width - 3 - font.width(String.valueOf(entry.buyPrice));
            int priceSpotY = this.y + this.height - 10;
            //Render price
            font.drawShadow(stack,String.valueOf(entry.buyPrice), priceSpotX, priceSpotY, color);

            //Render stock left if stock is limited
            if (myStock != null) {
                font.drawShadow(stack, "Stock: " + myStock.stockLeft,
                        this.x + 6 + 16, priceSpotY, color);
            }

            ExtraUtil.TEXTURE_MANAGER.bind(SHOP_LOCATION);
            //Render XP Orb
            ExtraUtil.blitImage(stack,priceSpotX - 8,7,
                    priceSpotY, 7,106,7,249,7,256);

            //stock shtuff
            if (myStock != null && myStock.stockLeft == 0){
                //Render bg shade
                RenderSystem.disableDepthTest();
                ExtraUtil.blitColor(stack,this.x,this.width,this.y,this.height, shadeColor);
                RenderSystem.enableDepthTest();

                //Out of stock txt
                drawCenteredString(stack, font,"Out of stock!", this.x + (this.width/2), this.y + 2, TextFormatting.RED.getColor());

                //Time left
                drawCenteredString(stack, font, myStock.checkStock(entry), this.x + (this.width/2), priceSpotY, TextFormatting.RED.getColor());
            }

            //locked
            else if (!playerCap.isUnlocked(entry)){
                //Render lock bg
                RenderSystem.disableDepthTest();
                ExtraUtil.blitColor(stack,this.x,this.width,this.y,this.height, shadeColor);
                RenderSystem.enableDepthTest();
                //Now render item
                //Render item
                itemRenderer.renderAndDecorateItem(entry.lockItem, this.x + ((this.width - 16)/2), this.y + 3);
                //Render item decor
                if (!entry.item.isDamageableItem()) {
                    itemRenderer.renderGuiItemDecorations(font, entry.lockItem, this.x + ((this.width - 16)/2), this.y + 3);
                }

                color = ExtraUtil.mC.player.inventory.countItem(entry.lockItem.getItem()) >= entry.lockItem.getCount() ?
                        TextFormatting.GREEN.getColor() : TextFormatting.RED.getColor();

                //Unlock Text
                font.drawShadow(stack, "Unlock?", this.x + ((this.width - 16)/2) + 18, this.y + 6, color);
            }
        }

        @Override
        protected boolean isValidClickButton(int buttonPressed) {
            return buttonPressed < 2;
        }

        @Override
        public void onClick(double xMouse, double yMouse) {
            super.onClick(xMouse, yMouse);
        }

        @Override
        public boolean mouseClicked(double xMouse, double yMouse, int buttonPressed) {
            //If right click, I want you to edit this entry
            if(buttonPressed == 1 && this.clicked(xMouse,yMouse)){
                this.playDownSound(ExtraUtil.mC.getSoundManager());

                if (ExtraUtil.mC.player.isCreative()) {
                    ExtraUtil.mC.setScreen(new AddItemScreen(ExtraUtil.mC.screen, localCatEntries.get(pageIndex - 1), entry));
                    return true;
                }
            }

            return super.mouseClicked(xMouse, yMouse, buttonPressed);
        }
    }

    private class CategoryButton extends ExtraUtil.ItemButton {
        private CategoryEntry entry;

        public CategoryButton(int x, int y, int width, int height, CategoryEntry entry, ExtraUtil.Bounds bounds, IPressable onPress) {
            super(x, y, width, height, entry.categoryName, entry.categoryItem, bounds, onPress);

            this.entry = entry;
        }

        @Override
        protected boolean isValidClickButton(int buttonPressed) {
            return buttonPressed < GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
        }

//        @Override
//        public void onClick(double xMouse, double yMouse) {
//            super.onClick(xMouse, yMouse);
//        }
//
//        @Override
//        public boolean isHovered() {
//            return this.isHovered;
//        }

        @Override
        public boolean mouseClicked(double xMouse, double yMouse, int buttonPressed) {
            //If right click, I want you to edit this entry
            if(ExtraUtil.mC.player.isCreative() && buttonPressed == 1 && this.clicked(xMouse,yMouse)){
                this.playDownSound(ExtraUtil.mC.getSoundManager());
                ExtraUtil.mC.setScreen(new CatSearchScreen(ExtraUtil.mC.screen, entry));
                return true;
            }

            return super.mouseClicked(xMouse, yMouse, buttonPressed);
        }
    }
}
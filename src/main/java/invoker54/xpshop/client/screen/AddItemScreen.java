package invoker54.xpshop.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.client.KeyInit;
import invoker54.xpshop.client.screen.search.ItemSearchScreen;
import invoker54.xpshop.client.screen.ui.TextBoxUI;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.data.CategoryEntry;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncServerShopMsg;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;

public class AddItemScreen extends Screen {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public NewItemList list;
    protected Screen prevScreen;
    protected CategoryEntry categoryEntry;
    private int greyColor = new Color(78, 79, 80,255).getRGB();
    private int whiteColor = new Color(255, 255, 255,255).getRGB();
    private int lightBlueColor = new Color(152, 199, 213,255).getRGB();
    private int transparentRedColor = new Color(255, 45, 45, 105).getRGB();

    int widthSpace;
    int heightSpace;
    int halfWidthSpace;
    int halfHeightSpace;
    ExtraUtil.Bounds bounds;
    BuyEntry targetEntry;
    private ExtraUtil.SimpleButton doneButton;
    private ClientUtil.SimpleButton duplicateButton;

    public AddItemScreen(Screen prevScreen, CategoryEntry categoryEntry, BuyEntry entry){
        super(ITextComponent.nullToEmpty(null));
        this.prevScreen = prevScreen;
        this.categoryEntry = categoryEntry;
        bounds = new ExtraUtil.Bounds();
        targetEntry = entry;
        if(targetEntry == null) entry = new BuyEntry();

        list = new NewItemList(this.width, halfHeightSpace + 29, this.halfHeightSpace + 168);
        list.addEntry(new ItemEntry("Item: ", this, "item", entry.item));
        list.addEntry(new IntEntry("Buy Price: ", "buyPrice", entry.buyPrice));
        list.addEntry(new IntEntry("Stock: ", "limitStock", entry.limitStock));
        list.addEntry(new ItemEntry("Lock Item: ", this, "lockItem", entry.lockItem));
        list.addEntry(new BoolEntry("Always Show: ", "alwaysShow", entry.alwaysShow));
        list.setRenderTopAndBottom(false);
        list.setRenderBackground(false);

    }

    protected void init() {
        super.init();

        this.addWidget(this.list);
        heightSpace = this.height - 177;
        halfHeightSpace = heightSpace / 2;

        list.recalcWidth();
        //9 is left side of list container, 11 is right side of list container
        widthSpace = this.width - (9 + 11 + list.getWidth());
        halfWidthSpace = widthSpace / 2;
        list.updatePosition(halfWidthSpace + 9, halfHeightSpace + 29, 139);
        bounds.adjustBounds(halfWidthSpace + 9, list.getWidth(), halfHeightSpace + 29, 139);

        //Done button
        doneButton = addButton(new ExtraUtil.SimpleButton(halfWidthSpace + 5, halfHeightSpace + 4, 40, 16, ITextComponent.nullToEmpty("Done"),
                (button) -> createNewEntry(false)));

        //Cancel Button
        if(targetEntry == null)
            addButton(new ExtraUtil.SimpleButton(halfWidthSpace + (9 + 11 + list.getWidth()) - 40 - 5,
                halfHeightSpace + 4, 40, 16, ITextComponent.nullToEmpty("Cancel"),
                (button) -> ExtraUtil.mC.setScreen(prevScreen)));

//        XPShop.LOGGER.debug("Is the target entry null? " + (targetEntry == null));

        if (targetEntry != null) {
            //Delete button
            addButton(new ExtraUtil.SimpleButton(halfWidthSpace + (9 + 11 + list.getWidth()) - 40 - 5,
                    halfHeightSpace + 4, 40, 16, ITextComponent.nullToEmpty("Delete"),
                    (button) -> {
                        ShopData.buyEntries.remove(targetEntry);
                        categoryEntry.entries.remove(targetEntry);
                        ExtraUtil.mC.setScreen(prevScreen);

                        NetworkHandler.sendToServer(new SyncServerShopMsg(ShopData.serialize()));
                    }));

            //Duplicate button (This will create a new entry but will stay on the add item screen)
            duplicateButton = addButton(new ExtraUtil.SimpleButton(halfWidthSpace + (9 + 11 + list.getWidth()) - 52 - font.width("Duplicate"),
                    halfHeightSpace + 4, font.width("Duplicate") + 4, 16, ITextComponent.nullToEmpty("Duplicate"),
                    (button) -> createNewEntry(true)));
        }

        //Check if there are duplicates
        checkForDuplicate();
    }
    protected void createNewEntry(boolean duplicate){
        //We have to find the category first and foremost
        for (CategoryEntry entry : ShopData.catEntries){
            if (!Objects.equals(entry.categoryName, categoryEntry.categoryName)) continue;
            if (!ExtraUtil.itemsMatch(entry.categoryItem, categoryEntry.categoryItem)) continue;

            categoryEntry = entry;
        }

        BuyEntry newEntry = new BuyEntry(list.saveData(), categoryEntry);

        //If it's empty, then DONT go through with this
        if (newEntry.item.isEmpty()) return;

        //If we have a target entry, delete it if it matches the current item OR we are not duplicating.
        if (targetEntry != null && ExtraUtil.itemsMatch(targetEntry.item, newEntry.item) || !duplicate) {
            ShopData.buyEntries.remove(targetEntry);
            categoryEntry.entries.remove(targetEntry);
        }

        //First let's go through every entry and make sure the item being sold doesn't already exist
        for (BuyEntry entry : ShopData.buyEntries) {
            if (ExtraUtil.itemsMatch(entry.item, newEntry.item) && entry != targetEntry) {
                ShopData.buyEntries.remove(entry);
                categoryEntry.entries.remove(entry);
                break;
            }
        }

        categoryEntry.entries.add(newEntry);
        ShopData.buyEntries.add(newEntry);
        if (!duplicate) ExtraUtil.mC.setScreen(prevScreen);
        else{
            checkForDuplicate();
            targetEntry = newEntry;
        }

        //If the category no longer exists, add it back, then sync
        boolean foundCategory = false;
        for (CategoryEntry catEntry : ShopData.catEntries){
            //If the item doesn't match, continue
            if (!catEntry.categoryItem.sameItem(categoryEntry.categoryItem)) continue;
            //If the name doesn't match, continue
            if (!Objects.equals(catEntry.categoryName, categoryEntry.categoryName)) continue;

            foundCategory = true;
            break;
        }
        if (!foundCategory) ShopData.catEntries.add(categoryEntry);

        NetworkHandler.sendToServer(new SyncServerShopMsg(ShopData.serialize()));
    }

    public boolean mouseScrolled(double xMouse, double yMouse, double scrollValue) {
        return list.mouseScrolled(xMouse, yMouse,scrollValue);
    }

    public boolean isDone(){
       if (!((ItemEntry)list.children().get(0)).itemButton.displayItem.isEmpty()) {
           return true;
       }

        return false;
    }

    @Override
    public void render(@Nonnull MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        ExtraUtil.beginCrop(halfWidthSpace + 9, list.getWidth() + 6, halfHeightSpace + 29,139, true);
        this.list.render(stack, xMouse, yMouse, partialTicks);
        ExtraUtil.endCrop();


        ExtraUtil.TEXTURE_MANAGER.bind(ShopScreen.SHOP_LOCATION);
        //Render left part of GUI
        ExtraUtil.blitImage(stack, halfWidthSpace, 9, halfHeightSpace, 177, 171, 9, 0, 177, 256);
        //Render middle
        ExtraUtil.blitImage(stack, halfWidthSpace + 9, list.getWidth(), halfHeightSpace, 177, 181, 10, 0, 177, 256);
        //Render right part of GUI
        ExtraUtil.blitImage(stack, halfWidthSpace + 9 + list.getWidth(), 9, halfHeightSpace, 177, 192, 11, 0, 177, 256);

        doneButton.active = isDone();

        super.render(stack, xMouse, yMouse, partialTicks);
        if (duplicateButton == null) return;

        //Put a red box over duplicate if the item already exists
        if (!duplicateButton.active){
            ClientUtil.blitColor(stack, duplicateButton.x, duplicateButton.getWidth(),
                    duplicateButton.y, duplicateButton.getHeight(), this.transparentRedColor);
        }
    }

    public void checkForDuplicate(){
        if (duplicateButton == null) return;

        ItemStack targetItem = ((ItemEntry)this.list.children().get(0)).itemButton.displayItem;
        for (BuyEntry entry : ShopData.buyEntries) {
            if (ExtraUtil.itemsMatch(entry.item, targetItem)) {
                duplicateButton.active = false;
                return;
            }
        }
        duplicateButton.active = true;
    }

//    @Override
//    public boolean mouseReleased(double xMouse, double yMouse, int mouseButton) {
//        return list.mouseReleased(xMouse, yMouse, mouseButton);
//    }
//
//    @Override
//    public boolean mouseDragged(double xOrigin, double yOrigin, int mouseButton, double xDistance, double yDistance) {
//        return list.mouseDragged(xOrigin, yOrigin, mouseButton, xDistance, yDistance);
//    }

    public boolean charTyped(char character, int keyCode) {
        return list.charTyped(character, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int b, int c) {
        if (minecraft.options.keyInventory.getKey().getValue() == keyCode ||
                KeyInit.shopKey.keyBind.getKey().getValue() == keyCode) {
            minecraft.setScreen(null);
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ESCAPE){
            minecraft.setScreen(prevScreen);
            return true;
        }
//        return list.keyPressed(keyCode, b, c);
    return super.keyPressed(keyCode, b, c);
    }

    public class NewItemList extends AbstractList<ListEntry>  {

        //Height is used for how long you want the top and bottom panels to be if you had
        //renderTopAndBottom set to true.
        public NewItemList(int width, int y0, int y1) {
            super(ExtraUtil.mC, width, 0, y0, y1, ListEntry.height);
        }

        public void recalcWidth(){
            int width = 0;

            for (ListEntry entry : this.children()){
                if (entry.getWidth() > width){
                    width = entry.getWidth();
                }
            }

            //Set width
            this.width = width;
        }

        public void updatePosition(int x0, int y0, int height) {
            this.setLeftPos(x0);
            this.y0 = y0;
            this.y1 = y0 + height;
        }

        @Override
        public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
            ExtraUtil.blitColor(stack,x0,getWidth(),y0, y1 - y0, lightBlueColor);

            super.render(stack, xMouse, yMouse, partialTicks);
        }

        @Override
        public int getRowLeft() {
            return x0;
        }

        @Override
        public int addEntry(@Nonnull ListEntry entry) {
            return super.addEntry(entry);
        }

        @Override
        protected int getScrollbarPosition() {
            return x0 + getRowWidth();
        }

                @Override
        public int getRowWidth() {
            return this.width;
        }

        public CompoundNBT saveData(){
            CompoundNBT nbt = new CompoundNBT();

            for (ListEntry entry: this.children()){
                entry.save(nbt);
            }

            return nbt;
        }
    }

    public class ListEntry extends AbstractList.AbstractListEntry<ListEntry> {
        protected int width;
        protected int origWidth;
        protected static final int height = 30;
        protected final int middleSpace = 20;
        protected final int padding = 5;
        String txtToRender;
        String nbtString;

        public ListEntry(String txtToRender, String nbtString) {
            this.txtToRender = txtToRender;
            this.nbtString = nbtString;
            this.width = padding + ExtraUtil.mC.font.width(txtToRender) + middleSpace + padding;
            origWidth = this.width;
        }

        public int getWidth(){
            return this.width;
        }

        public CompoundNBT save(CompoundNBT nbt){
            return null;
        }

        @Override
        public void render(MatrixStack stack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int xMouse, int yMouse, boolean isMouseOver, float partialTicks
        ) {
            //Entry background
            ExtraUtil.blitColor(stack,rowLeft, rowWidth, rowTop, rowHeight, greyColor);

            //LOGGER.debug("MY HEIGHT IS EXACTLY: " + rowHeight);

            //Text to render
            float ySpot = rowTop + (rowHeight - font.lineHeight)/2f;
            font.draw(stack,txtToRender,rowLeft + padding, ySpot, whiteColor);
        }
    }
    public class BoolEntry extends ListEntry {
        protected final int buttonWidth = 50;
        protected final int buttonHeight = 18;
        protected boolean enabled;
        protected ClientUtil.SimpleButton boolButton;

        public BoolEntry(String txtToRender, String nbtString, boolean enabled) {
            super(txtToRender, nbtString);

            this.width += buttonWidth;
            this.enabled = enabled;
            this.boolButton = new ClientUtil.SimpleButton(0, 0, buttonWidth, buttonHeight,
                    ITextComponent.nullToEmpty("" + enabled), (button) -> changeButton());
            addButton(this.boolButton);
        }

        public void changeButton(){
            this.enabled = !this.enabled;
            this.boolButton.setMessage(ITextComponent.nullToEmpty("" + this.enabled));
//            LOGGER.warn("BOOL IS CHANGING TO: " + this.enabled);
        }

        @Override
        public CompoundNBT save(CompoundNBT nbt) {
             nbt.putBoolean(nbtString,this.enabled);
             return nbt;
        }

        @Override
        public boolean mouseClicked(double xMouse, double yMouse, int button) {
            return boolButton.mouseClicked(xMouse, yMouse, button);
        }

        @Override
        public void render(MatrixStack stack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int xMouse, int yMouse, boolean isMouseOver, float partialTicks) {
            super.render(stack, index, rowTop, rowLeft, rowWidth, rowHeight, xMouse, yMouse, isMouseOver, partialTicks);

            this.boolButton.x = rowLeft + rowWidth - padding - this.boolButton.getWidth();
            this.boolButton.y = rowTop + (rowHeight - this.boolButton.getHeight())/2;

            this.boolButton.render(stack, xMouse, yMouse, partialTicks);
        }
    }
    public class IntEntry extends ListEntry {

        public TextBoxUI textBox;

        public IntEntry(String txtToRender, String nbtString, int defaultNumber) {
            super(txtToRender, nbtString);

            textBox = new TextBoxUI(ExtraUtil.mC.font, 0,0,60,11, ITextComponent.nullToEmpty("0"), TextBoxUI.defOutColor, TextBoxUI.defInColor);
            if(defaultNumber != 0) textBox.setValue("" + defaultNumber);
            width += textBox.getWidth();
            addWidget(textBox);
        }

        @Override
        public CompoundNBT save(CompoundNBT nbt) {
            try {
                nbt.putInt(nbtString, Integer.parseInt(textBox.getValue().trim()));
            }
            catch (Exception e){
                LOGGER.debug("Couldn't parse: " + nbtString);
                LOGGER.debug(e);
                nbt.putInt(nbtString, 0);
            }
            return nbt;
        }

        @Override
        public void render(MatrixStack stack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int xMouse, int yMouse, boolean isMouseOver, float partialTicks) {
            super.render(stack, index, rowTop, rowLeft, rowWidth, rowHeight, xMouse, yMouse, isMouseOver, partialTicks);

            textBox.x = rowLeft + rowWidth - padding - textBox.getWidth();
            textBox.y = rowTop + (rowHeight - textBox.getHeight())/2;

            textBox.render(stack,xMouse, yMouse, partialTicks);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public boolean mouseClicked(double xMouse, double yMouse, int button) {
            return textBox.mouseClicked(xMouse, yMouse, button);
        }

        @Override
        public boolean charTyped(char character, int keyCode) {
            if (!NumberUtils.isParsable(String.valueOf(character))) return false;

            return textBox.charTyped(character, keyCode);
        }

        @Override
        public boolean keyPressed(int keyCode, int p_231046_2_, int p_231046_3_) {
            return textBox.keyPressed(keyCode, p_231046_2_, p_231046_3_);
        }
    }
    public class ItemEntry extends ListEntry {
        protected final int buttonWidth = 26;
        protected final int buttonHeight = 26;
        protected ExtraUtil.ItemButton itemButton;

        public ItemEntry(String txtToRender, Screen prevScreen, String nbtString, ItemStack defaultItem) {
            super(txtToRender, nbtString);

            if (defaultItem == null) defaultItem = ItemStack.EMPTY;

            this.width += buttonWidth;

            ItemStack finalDefaultItem = defaultItem;
            itemButton = new ExtraUtil.ItemButton(bounds,
                    (button) -> {
                        ItemSearchScreen screen = new ItemSearchScreen(prevScreen, ((item) -> {
                                    ((ExtraUtil.ItemButton) button).displayItem = item;

                                    ExtraUtil.mC.setScreen(prevScreen);
                                }));
                        ExtraUtil.mC.setScreen(screen);
                        if (!finalDefaultItem.isEmpty()) screen.countBox.setValue("" + finalDefaultItem.getCount());
                    });

            itemButton.displayItem = defaultItem;
            itemButton.setWidth(buttonWidth);
            itemButton.setHeight(buttonHeight);
            addButton(itemButton);
        }

        @Override
        public boolean mouseClicked(double xMouse, double yMouse, int button) {
            return itemButton.mouseClicked(xMouse, yMouse, button);
        }

        @Override
        public CompoundNBT save(CompoundNBT nbt) {
            CompoundNBT cNBT = itemButton.displayItem.save(new CompoundNBT());

            nbt.put(nbtString, cNBT);

            return nbt;
        }

        @Override
        public void render(MatrixStack stack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int xMouse, int yMouse, boolean isMouseOver, float partialTicks) {
            super.render(stack, index, rowTop, rowLeft, rowWidth, rowHeight, xMouse, yMouse, isMouseOver, partialTicks);

            itemButton.x = rowLeft + rowWidth - padding - itemButton.getWidth();
            itemButton.y = rowTop;

            itemButton.render(stack, xMouse, yMouse, partialTicks);
            //Render item decor
            if (!itemButton.displayItem.isDamageableItem()) {
                itemRenderer.renderGuiItemDecorations(font, itemButton.displayItem, itemButton.x + 5, itemButton.y + 5);
            }
            //itemRenderer.renderAndDecorateItem(itemButton.displayItem, itemButton.x, itemButton.y);
        }
    }
}

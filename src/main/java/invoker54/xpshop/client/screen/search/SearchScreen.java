package invoker54.xpshop.client.screen.search;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.client.KeyInit;
import invoker54.xpshop.client.screen.ui.TextBoxUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.util.ISearchTree;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;

public class SearchScreen extends Screen {
    protected Screen prevScreen;

    protected SearchScreen(ITextComponent p_i51108_1_, Screen prevScreen) {
        super(p_i51108_1_);
    }

    // Directly reference a log4j logger.
    protected static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation SELECT_ITEM = new ResourceLocation(XPShop.MOD_ID, "textures/gui/screen/select_item_screen.png");
    protected TextBoxUI searchBox;
    protected int scrollOffs = 0;
    //private final List<ItemStack> itemList = new ArrayList<>();
    protected final List<ItemStack> searchList = new ArrayList<>();
    private final List<ItemStack> itemsToRender = new ArrayList<>();
    protected ItemStack hoverItem = null;
    public ItemStack chosenItem = null;
    private final int itemHoverColor = new Color(160, 160, 160,100).getRGB();
    private final int itemBGColor = new Color(194, 225, 237,255).getRGB();
    private final int chosenItemBGColor = new Color(50, 255, 35,255).getRGB();
    private final Map<ResourceLocation, ITag<Item>> visibleTags = Maps.newTreeMap();

    private final ITextComponent ghostSearchText = new TranslationTextComponent("AddCatScreen.search_text");
    private final ITextComponent doneTxt = new TranslationTextComponent("AddCatScreen.done_txt");
    private final ITextComponent cancelTxt = new TranslationTextComponent("AddCatScreen.cancel_txt");

    protected ExtraUtil.SimpleButton doneButton;
    protected ExtraUtil.SimpleButton cancelButton;
    protected IChooseItem onDone;

    int widthSpace;
    int heightSpace;
    int halfWidthSpace;
    int halfHeightSpace;

    public SearchScreen(Screen prevScreen, IChooseItem onDone) {
        super(new TranslationTextComponent("shopScreen.shop_text"));
        this.prevScreen = prevScreen;
        this.onDone = onDone;
    }

    public void onDone() {
        this.onDone.onItemChosen(this.chosenItem);
    }



    //Note: I can render 70 items at once

    @Override
    //This is where I set all initial stuff, like buttons, or positions
    public void init() {
        super.init();

        widthSpace = (width - 189);
        heightSpace = (height - 177);
        halfWidthSpace = widthSpace /2;
        halfHeightSpace = heightSpace /2;

        //Make search field
        ExtraUtil.mC.keyboardHandler.setSendRepeatsToGui(true);
        if (searchBox == null){
            this.searchBox = new TextBoxUI(this.font, halfWidthSpace + 12, halfHeightSpace + 17,
                    160, 11, ghostSearchText, TextBoxUI.defOutColor, TextBoxUI.defInColor);
        }
        addWidget(this.searchBox);

        //Done button
        doneButton = addButton(new ExtraUtil.SimpleButton(halfWidthSpace + 96, halfHeightSpace + 151, 40, 16, doneTxt, (IPressable) -> {
            onDone();
        }));

        //Cancel button
        cancelButton = addButton(new ExtraUtil.SimpleButton(halfWidthSpace + 139, halfHeightSpace + 151, 40, 16, cancelTxt, (button) ->
        {
            ExtraUtil.mC.setScreen(prevScreen);
        }));

        if (searchList.isEmpty()) refreshSearchResults();
    }

//    private void fillItemList(){
//        NonNullList<ItemStack> tempList = NonNullList.create();
//
//        //First fill temp list
//        for (ItemGroup group: ItemGroup.TABS){
//            try {
//                LOGGER.debug("What tab am I adding? : " + group.getDisplayName().getString());
//
//                if (group == ItemGroup.TAB_HOTBAR) continue;
//                if (group == ItemGroup.TAB_SEARCH) continue;
//
//                group.fillItemList(tempList);
//            } catch (RuntimeException | LinkageError e) {
//                LOGGER.error("fill item list had an error, this is the list that caused it: {}", group, e);
//            }
//        }
//
//        //Next make a couple checks
//        for (ItemStack item: tempList){
//            if (item.getItem() == Items.PLAYER_HEAD) continue;
//            if (item.isEmpty()) continue;
//            LOGGER.debug("Does list contain " + item.getDisplayName().getString() + "? " + tempList.contains(item));
//
//            itemList.add(item);
//        }
//
//        refreshSearchResults();
//
//
//    }

    @Override
    //This is where everything gets rendered
    public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {

        //First render background
        super.renderBackground(stack);

        //Render item bg
        ExtraUtil.blitColor(stack, halfWidthSpace + 9,171, halfHeightSpace + 35, 110, itemBGColor);

        //Render items next
        renderItemList(stack, xMouse, yMouse, partialTicks);

        //then render select screen
        ExtraUtil.TEXTURE_MANAGER.bind(SELECT_ITEM);

        ExtraUtil.blitImage(stack, halfWidthSpace,189, halfHeightSpace, 176, 0,
                189, 0, 176, 256);

        super.render(stack, xMouse, yMouse, partialTicks);

        //Now for the search bar
        this.searchBox.render(stack, xMouse, yMouse, partialTicks);

        this.doneButton.active = isDone();

        //Finally render the tooltip for the item I am hovering over
        if (this.hoverItem != null){
            renderTooltip(stack, this.hoverItem, xMouse, yMouse);
        }

        //Start scissor test
//        RenderSystem.enableScissor((int) ((halfWidth + 9) * scale), (int) ((halfHeight + 9) * scale),
//                (int) ((187) * scale), (int) (140 * scale));
//        RenderSystem.disableScissor();
    }

    @Override
    public void tick() {
        this.searchBox.tick();
    }

    @Override
    public void resize(Minecraft mC, int width, int height) {
        super.resize(mC, width, height);

        widthSpace = (width - 189);
        heightSpace = (height - 177);
        halfWidthSpace = widthSpace /2;
        halfHeightSpace = heightSpace /2;
    }

    public void renderItemList(MatrixStack stack, int xMouse, int yMouse, float partialTicks){
        ExtraUtil.Bounds bounds = new ExtraUtil.Bounds();
        this.hoverItem = null;

        int origX = halfWidthSpace + 10;
        int origY = halfHeightSpace + 36;
        int space = 17;
        int itemSlot = 0;

        int x = 0;
        int y = 0;

        //ClientUtil.beginCrop(halfWidth + 9, 171, halfHeight + 31, 110);
        //Next render the items (rows)
        for (int row = 0; row < 7; row++){
            itemSlot = row * 10;
            for (int column = 0; column < 10; column++){
                if (itemsToRender.size() <= itemSlot + column) break;

                x = origX + (space * column);
                y = origY + (space * row);

                bounds.adjustBounds(x, 16, y,16);

                this.itemRenderer.renderAndDecorateItem(itemsToRender.get(itemSlot + column), x, y);

                if (row == 6) continue;

                boolean inBounds = ExtraUtil.inBounds(xMouse, yMouse, bounds);

                renderItemSlot(stack,itemsToRender.get(itemSlot + column), x, y, inBounds);
            }

        }
        //ClientUtil.endCrop();
    }

    public void renderItemSlot(MatrixStack stack, ItemStack item, int x, int y, boolean inBounds){
        //If chosen item, make slot grey and end
        if (item.equals(chosenItem)){
            ExtraUtil.blitColor(stack,x, 16, y, 16, chosenItemBGColor);
            return;
        }

        //If mouse not in bounds, return.
        if (!inBounds) return;

        this.hoverItem = item;

        //Make the slot greyish to show that it is being hovered over
        ExtraUtil.blitColor(stack,x, 16, y, 16, itemHoverColor);
    }

    public boolean mouseClicked(double xMouse, double yMouse, int mouseButton) {
        //
        if (this.getFocused() != null && this.getFocused().mouseClicked(xMouse, yMouse, mouseButton)) return true;

        if (hoverItem != null && mouseButton == 0) {
            chosenItem = hoverItem;
        }
        else if (mouseButton == 1){
            chosenItem = ItemStack.EMPTY;
        }
        return super.mouseClicked(xMouse, yMouse, mouseButton);
//        if(!super.mouseClicked(xMouse, yMouse, mouseButton)){
//            setFocused((IGuiEventListener) null);
//            return false;
//        }
//        else {
//            return true;
//        }
    }

    public boolean mouseScrolled(double xMouse, double yMouse, double scrollValue) {
        scrollValue = -MathHelper.sign(scrollValue);

        LOGGER.debug("Is scrolloffset larger than searchlist size? " + ((scrollOffs * 10) + 60) + ">=" + searchList.size());
        LOGGER.debug((scrollOffs * 10) + 60 >= searchList.size());

        if ((scrollOffs * 10) + 60 >= searchList.size() && scrollValue >= 1) return false;
        if (scrollOffs == 0 && scrollValue < 0) return false;

        scrollOffs += scrollValue;

        recalcItemRenderList();

        return true;
    }

    public boolean isDone(){
        if (this.chosenItem == null){
            return false;
        }

        return true;
    }

    protected void recalcItemRenderList(){
        itemsToRender.clear();
        for (int a = 0; a < 70; a++){
            if (searchList.size() <= (a + (scrollOffs * 10))) break;

            itemsToRender.add(searchList.get(a + (scrollOffs * 10)));
        }
    }

    @Override
    public boolean charTyped(char p_231042_1_, int p_231042_2_) {

        String s = this.searchBox.getValue();
        if (this.searchBox.charTyped(p_231042_1_, p_231042_2_)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
                this.refreshSearchResults();
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
                if (!Objects.equals(s, this.searchBox.getValue())) {
                    this.refreshSearchResults();
                }

                return true;
            }
        }
        else if (this.getFocused() == null) {
            if (minecraft.options.keyInventory.getKey().getValue() == keyCode ||
                    KeyInit.shopKey.keyBind.getKey().getValue() == keyCode) {
                minecraft.setScreen(null);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                minecraft.setScreen(prevScreen);
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.getFocused() != null)   this.setFocused(null);
        return super.keyPressed(keyCode, b, c);
    }

    public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
        return super.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_);
    }

    protected void refreshSearchResults() {
        searchList.clear();
        //LOGGER.debug("Refreshing items");
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

        searchList.addAll(isearchtree.search(s.toLowerCase(Locale.ROOT)));

        //LOGGER.debug("The size of the search list is2: " + searchList.size());
        for(ItemStack item : searchList){
            //LOGGER.debug(item.getDisplayName().getString());
        }

        recalcItemRenderList();
//        if (!this.searchBox.getValue().isEmpty()) {
//                searchList.addAll(itemList);
//            LOGGER.debug("The size of the search list is1: " + searchList.size());
//            for(ItemStack item : searchList){
//                LOGGER.debug(item.getDisplayName().getString());
//            }
//                //TODO: Make this a SearchTree not a manual search
//                String search = this.searchBox.getValue().toLowerCase(Locale.ROOT);
//                java.util.Iterator<ItemStack> itr = searchList.iterator();
//                while (itr.hasNext()) {
//                    ItemStack stack = itr.next();
//                    boolean matches = false;
//                    for (ITextComponent line : stack.getTooltipLines(ClientUtil.mC.player, ClientUtil.mC.options.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL)) {
//                        if (TextFormatting.stripFormatting(line.getString()).toLowerCase(Locale.ROOT).contains(search)) {
//                            matches = true;
//                            break;
//                        }
//                    }
//                    if (!matches)
//                        itr.remove();
//                }
//            LOGGER.debug("The size of the search list is2: " + searchList.size());
//            for(ItemStack item : searchList){
//                LOGGER.debug(item.getDisplayName().getString());
//            }
//            }


//        if (s.isEmpty()) {
//            searchList.addAll(itemList);
//            LOGGER.debug("The size of the search list is1: " + searchList.size());
//            for(ItemStack item : searchList){
//                LOGGER.debug(item.getDisplayName().getString());
//            }
//        } else {
//            ISearchTree<ItemStack> isearchtree;
//            if (s.startsWith("@")) {
//                s = s.substring(1);
//                isearchtree = this.minecraft.getSearchTree(SearchTreeManager.CREATIVE_TAGS);
//                this.updateVisibleTags(s);
//            } else {
//                isearchtree = this.minecraft.getSearchTree(SearchTreeManager.CREATIVE_NAMES);
//            }
//
//            searchList.addAll(isearchtree.search(s.toLowerCase(Locale.ROOT)));
//
//            LOGGER.debug("The size of the search list is2: " + searchList.size());
//            for(ItemStack item : searchList){
//                LOGGER.debug(item.getDisplayName().getString());
//            }
//        }

//        LOGGER.debug("The size of the search list is: " + searchList.size());
//        for(ItemStack item : searchList){
//            LOGGER.debug(item.getDisplayName());
//        }

        //Original Shtuff from CreativeScreen
//        (this.menu).items.clear();
//        this.visibleTags.clear();
//
//        ItemGroup tab = ItemGroup.TABS[selectedTab];
//        if (tab.hasSearchBar() && tab != ItemGroup.TAB_SEARCH) {
//            tab.fillItemList(menu.items);
//            if (!this.searchBox.getValue().isEmpty()) {
//                //TODO: Make this a SearchTree not a manual search
//                String search = this.searchBox.getValue().toLowerCase(Locale.ROOT);
//                java.util.Iterator<ItemStack> itr = menu.items.iterator();
//                while (itr.hasNext()) {
//                    ItemStack stack = itr.next();
//                    boolean matches = false;
//                    for (ITextComponent line : stack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL)) {
//                        if (TextFormatting.stripFormatting(line.getString()).toLowerCase(Locale.ROOT).contains(search)) {
//                            matches = true;
//                            break;
//                        }
//                    }
//                    if (!matches)
//                        itr.remove();
//                }
//            }
//            this.scrollOffs = 0.0F;
//            menu.scrollTo(0.0F);
//            return;
//        }
//
//        String s = this.searchBox.getValue();
//        if (s.isEmpty()) {
//            for(Item item : Registry.ITEM) {
//                item.fillItemCategory(ItemGroup.TAB_SEARCH, (this.menu).items);
//            }
//        } else {
//            ISearchTree<ItemStack> isearchtree;
//            if (s.startsWith("#")) {
//                s = s.substring(1);
//                isearchtree = this.minecraft.getSearchTree(SearchTreeManager.CREATIVE_TAGS);
//                this.updateVisibleTags(s);
//            } else {
//                isearchtree = this.minecraft.getSearchTree(SearchTreeManager.CREATIVE_NAMES);
//            }
//
//            (this.menu).items.addAll(isearchtree.search(s.toLowerCase(Locale.ROOT)));
//        }
//
//        this.scrollOffs = 0.0F;
//        this.menu.scrollTo(0.0F);
    }

    protected void updateVisibleTags(String p_214080_1_) {
        int i = p_214080_1_.indexOf(58);
        Predicate<ResourceLocation> predicate;
        if (i == -1) {
            predicate = (p_214084_1_) -> {
                return p_214084_1_.getPath().contains(p_214080_1_);
            };
        } else {
            String s = p_214080_1_.substring(0, i).trim();
            String s1 = p_214080_1_.substring(i + 1).trim();
            predicate = (p_214081_2_) -> {
                return p_214081_2_.getNamespace().contains(s) && p_214081_2_.getPath().contains(s1);
            };
        }

        ITagCollection<Item> itagcollection = ItemTags.getAllTags();
        itagcollection.getAvailableTags().stream().filter(predicate).forEach((p_214082_2_) -> {
            ITag itag = this.visibleTags.put(p_214082_2_, itagcollection.getTag(p_214082_2_));
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public interface IChooseItem {
        void onItemChosen(ItemStack item);
    }
}

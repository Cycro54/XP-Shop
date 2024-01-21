package invoker54.xpshop.client.screen.search;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.SellEntry;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncServerShopMsg;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class TagSearchScreen extends Screen {
    private int whiteColor = new Color(255, 255, 255,255).getRGB();
    //Where all the tags shall sit
    protected ClientUtil.Image tag_background = new ClientUtil.Image(new ResourceLocation(XPShop.MOD_ID,"textures/gui/screen/tag_background.png"),
            0, 137, 0, 166, 256);
    
    
    private static final Logger LOGGER = LogManager.getLogger();
    protected final float sellPrice;
    protected final Item searchItem;
    protected Screen prevScreen;
    protected ClientUtil.SimpleList myList;

    public TagSearchScreen(Item searchItem, float sellPrice, SellItemSearch sellScreen){
        super(ITextComponent.nullToEmpty(""));
        this.searchItem = searchItem;
        this.sellPrice = sellPrice;
        this.prevScreen = sellScreen;
    }

    @Override
    protected void init() {
        //Center the glyph container image
        tag_background.centerImageX(0, width);
        tag_background.centerImageY(0, height);

        //Create the list container
        myList = new ClientUtil.SimpleList(tag_background.x0 + 5, tag_background.getWidth() - 10,
                tag_background.y0 + 5, tag_background.getHeight() - 10, this.width, this.height, tag_background);
        this.addWidget(myList);

        //Now start to gather data
        //First grab all the tags
        Collection<ResourceLocation> tags = ItemTags.getAllTags().getMatchingTags(searchItem);

        //Then place all the tag toggle buttons inside that new list
        for(ResourceLocation location : tags) {
            //The name of the tag
            ITextComponent tagText = ITextComponent.nullToEmpty(location.getPath());
            //The entry for the list
            ToggleButton toggleButton = new ToggleButton(location, 0,0, font.width(tagText.getString()) + 16, 18,
                    tagText, (button) -> ((ToggleButton)button).pushed = !((ToggleButton)button).pushed);
            this.addButton(toggleButton);
            myList.addEntry(new ToggleEntry(myList, 18,toggleButton));
        }
        //Finally place the done button right below it all
        this.addButton(new ClientUtil.SimpleButton(tag_background.x0,
                tag_background.y0 + tag_background.getHeight() + 2, tag_background.getWidth(), 18,
                ITextComponent.nullToEmpty("Done"), (button) -> {

            //This list will be for all the items we added already
            ArrayList<Item> addedItems = new ArrayList<>();

            //Now begin going through all those items
            for (ClientUtil.ListEntry entry : myList.children()) {
                if (!(entry instanceof ToggleEntry)) {
                    LOGGER.error("ONE OF THE TOGGLE ENTRIES IS NOT A TOGGLE ENTRY!!");
                    LOGGER.error(entry.getClass());
                    continue;
                }
                if (!((ToggleEntry) entry).toggleButton.pushed) continue;

                ITag<Item> currTag = ItemTags.getAllTags().getTag(((ToggleEntry) entry).toggleButton.tag);
                if (currTag == null) {
                    LOGGER.error("THIS TAG HAS NO ITEMS?!");
                    LOGGER.error(((ToggleEntry) entry).toggleButton.tag);
                    continue;
                }

                //Begin going through all the items under this tag
                for (Item item : currTag.getValues()) {
//                    LOGGER.warn("CURRENT TAG: " + ((ToggleEntry) entry).toggleButton.tag);
                    //Make sure we don't already have the item in the list
                    if (!addedItems.contains(item)) {
                        //For adding sell entries
                        if (this.sellPrice != 0) {
                            ItemStack stack = new ItemStack(item);
                            ShopData.sellEntries.put(stack, new SellEntry(stack, this.sellPrice));
                        }
                        //For removing sell entries
                        else {
                            ItemStack matchingStack = ShopData.getMatchingStack(new ItemStack(item), ShopData.sellEntries.keySet());
                            if (matchingStack != null) ShopData.sellEntries.remove(matchingStack);
                        }

                        //Make sure to add the item to the addedItems list to know which items we went through already
                        addedItems.add(item);
                    }
                }
            }

            //Sync that list to the servers list
            NetworkHandler.sendToServer(new SyncServerShopMsg(ShopData.serialize()));

            //Finally go back to the sell screen
            ClientUtil.mC.setScreen(this.prevScreen);
        }));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(MatrixStack stack) {
        super.renderBackground(stack);

        //Then render the glyph container
        tag_background.RenderImage(stack);

        //Finally render the select tags text
        String text = "Select Tags";
        int x = tag_background.x0 + (tag_background.getWidth() - font.width(text))/2;
        int y = tag_background.y0 - 16;
        ClientUtil.mC.font.draw(stack, text, x, y, whiteColor);
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        for (net.minecraft.client.gui.widget.Widget button : this.buttons) {
            button.render(stack, mouseX, mouseY, partialTicks);
        }
        this.myList.render(stack, mouseX, mouseY, partialTicks);
    }

    protected static class ToggleEntry extends ClientUtil.ListEntry{
        public ToggleButton toggleButton;

        public ToggleEntry(ClientUtil.SimpleList parent, int height, ToggleButton toggleButton) {
            super(parent, height);
            this.toggleButton = toggleButton;
            this.heightPadding = 6;
        }

        @Override
        public void render(MatrixStack stack, int index, int y0, int x0, int rowWidth, int rowHeight, int xMouse, int yMouse, boolean isMouseOver, float partialTicks) {
            super.render(stack, index, y0, x0, rowWidth, rowHeight, xMouse, yMouse, isMouseOver, partialTicks);

            //Get the extra space in between the buttons and divide it by 3.
            int extraSpace = (rowWidth - toggleButton.getWidth())/2;
            toggleButton.x = x0 + extraSpace;
            toggleButton.y = y0 + ((rowHeight - toggleButton.getHeight())/2);
        }
    }
    protected static class ToggleButton extends ClientUtil.SimpleButton{
        public boolean pushed = false;
        public ResourceLocation tag;

        public ToggleButton(ResourceLocation tag, int x, int y, int width, int height, ITextComponent textComponent, IPressable onPress) {
            super(x, y, width, height, textComponent, onPress);
            this.tag = tag;
        }

        public boolean isPushed(){
            return pushed;
        }
        public ResourceLocation grabLocation(){
            return tag;
        }

        @Override
        public void renderButton(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
            if (!this.hidden) {
                FontRenderer fontrenderer = ClientUtil.mC.font;
                ClientUtil.TEXTURE_MANAGER.bind(WIDGETS_LOCATION);
                int i = this.getYImage(this.isHovered());
                if (pushed) i = 0;
                i = 46 + i * 20;
                ClientUtil.blitImage(stack, this.x, this.width / 2, this.y, this.height, 0.0F, (float)this.width / 2.0F, (float)i, 20.0F, 256.0F);
                ClientUtil.blitImage(stack, this.x + this.width / 2, this.width / 2, this.y, this.height, (float)(200 - this.width / 2), (float)(this.width / 2), (float)i, 20.0F, 256.0F);
                int j = this.getFGColor();
                drawCenteredString(stack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
            }
        }
    }
}

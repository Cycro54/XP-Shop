package invoker54.xpshop.client.screens.searchscreens;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.client.InvoTheme;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.InvoList;
import invoker54.xpshop.client.widgets.InvoTextBox;
import invoker54.xpshop.client.widgets.buttons.ImageTextButton;
import invoker54.xpshop.client.widgets.buttons.InvoButton;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ResourceSearchScreen<R extends InvoResource> extends InvoScreen {
    protected static float padding = -2;

    protected ResourceSearcher<R> searcher;
    protected R selectedResource;
    protected Consumer<R> acceptCallback;

    protected ImageTextButton presetButton;
    protected ImageTextButton editButton;
    protected ImageTextButton removeButton;

    protected InvoTextBox searchBox;
    protected ImageTextButton categoryButton;
    protected ImageTextButton sortButton;
    protected boolean sortAToZ = true;

    protected InvoList resourceList;

    protected ImageTextButton acceptButton;
    protected ImageTextButton cancelButton;

    public ResourceSearchScreen(InvoText pTitle, InvoScreen previousScreen, ResourceSearcher<R> searcher, R selectedResource,
                                Consumer<R> acceptCallback) {
        super(pTitle, previousScreen);
        this.searcher = searcher;
        this.selectedResource = selectedResource;
        this.acceptCallback = acceptCallback;

        this.presetButton = new ImageTextButton.Builder().setIconImage(InvoTheme.getDuplicateIcon())
                .setButton(0, this.searcher.createPreset(this)).setMessage(XPShopLanguageprovider.createPresetButton)
                .build(this);

        this.editButton = new ImageTextButton.Builder().setIconImage(InvoTheme.getEditIcon())
                .setButton(0, this.searcher.editPreset(this)).setMessage(XPShopLanguageprovider.editPresetButton)
                .build(this);

        this.removeButton = new ImageTextButton.Builder().setIconImage(InvoTheme.getRemoveIcon())
                .setButton(0, this.searcher.removePreset(this)).setMessage(XPShopLanguageprovider.removePresetButton)
                .build(this);

        this.searchBox = new InvoTextBox(this, this.getZoneCopy(), InvoText.literal(""), XPShopLanguageprovider.searchBarText,
                ClientUtil.getFont(), 4);

        this.categoryButton = new ImageTextButton.Builder().setIconImage(this.selectedResource.getCategory().getIcon())
                .setButton(0, this.openCategoryPopup()).build(this);
        this.sortButton = new ImageTextButton.Builder().setIconImage(this.sortAToZ ? InvoTheme.sortAIcon() : InvoTheme.sortZIcon())
                .setTooltip(this.selectedResource.getCategory().getName()).setButton(0,
                        ((isClick, button, pMouseX, pMouseY) -> this.sortAToZ = !this.sortAToZ));
    }

    @Override
    protected void init() {
        this.setZone(this.getOriginalZone().splitHeight(5,4).splitWidth(3,2).center(this.getOriginalZone()));

        super.init();
    }

    public void renderPropertySide(PoseStack stack, InvoZone propertyZone){
        propertyZone.inflate(-padding);
        propertyZone.splitHeight(5,1);
        //Resource's name
        InvoZone nameZone = propertyZone.copy().splitHeight(2,1);
        this.selectedResource.getName().render(stack, nameZone.copy().inflate(-padding), false);
        propertyZone.shiftXY(0,nameZone.height());

        //Resources picture
        InvoZone pictureZone = propertyZone.copy().setWidth(propertyZone.height());
        InvoTheme.getBackground().render(stack, pictureZone);
        this.selectedResource.getImage().render(stack, pictureZone.copy().inflate(-padding));

        //The 3 buttons
        InvoZone buttonZone = propertyZone.copy().shiftWH(-pictureZone.width(),0)
                .shiftXY(pictureZone.width(), 0).splitHeight(3,1);
        this.presetButton.setZone(buttonZone.copy().inflate(-padding));

        buttonZone.shiftXY(0, buttonZone.height());
        this.editButton.setZone(buttonZone.copy().inflate(-padding));

        buttonZone.shiftXY(0, buttonZone.height());
        this.removeButton.setZone(buttonZone.copy().inflate(-padding));

        //The space for the resource's properties
        InvoZone infoZone = propertyZone.shiftXY(0, propertyZone.height()).splitHeight(1, 3);
        this.selectedResource.renderInfo(stack, infoZone);
    }

    public void renderSearchSide(PoseStack stack, InvoZone searchZone){
        searchZone.inflate(-padding);
        searchZone.splitHeight(7,1);
        //7 pieces
        //Resource type name
        InvoZone nameZone = searchZone.copy().splitHeight(2,1);
        this.getMessage().render(stack, nameZone.copy().inflate(-padding), false);

        //resource search things
        //Search bar
        InvoZone barZone = nameZone.copy().gridShift(0,1).shiftWH(-nameZone.height() * 2, 0);
        this.searchBox.setZone(barZone.inflate(-padding));

        //Category button
        InvoZone categoryZone = barZone.copy().setWidth(barZone.height()).setX(barZone.right());
        this.categoryButton.setZone(categoryZone.copy().inflate(-padding));

        //Sort button
        InvoZone sortZone = categoryZone.gridShift(1,0);
        this.sortButton.setZone(sortZone.copy().inflate(-padding));

        //Resource list
        searchZone.gridShift(0, 1);
        InvoZone listZone = searchZone.copy().splitHeight(1, 5);
        this.resourceList.setZone(listZone.copy().inflate(-padding));

        //Accept Button
        searchZone.shiftXY(0, listZone.height());
        InvoZone acceptButton = searchZone.copy().splitWidth(3,1).gridShift(1,0);
        this.acceptButton.setZone(acceptButton.copy().inflate(-padding));

        //Cancel Button
        this.cancelButton.setZone(acceptButton.copy().gridShift(1,0).inflate(-padding));
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics) {
        InvoTheme.getBackground().render(pGuiGraphics.pose(), this.getZoneCopy());
        InvoZone propertyZone = this.getZoneCopy().splitWidth(10, 4);
        this.renderPropertySide(pGuiGraphics.pose(), propertyZone);

        InvoZone searchZone = this.getZoneCopy().splitWidth(10, 6).setX(propertyZone.right());
        this.renderSearchSide(pGuiGraphics.pose(), searchZone);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}

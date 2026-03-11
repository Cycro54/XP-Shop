package invoker54.xpshop.client.screens;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.common.capability.PlayerCapability;
import invoker54.xpshop.common.data.shops.TabShop;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

public class TabShopScreen extends ShopScreen {

    public final TabShop tabDataCopy;

//    public InvoHorizontalList tabList;
    public PlayerCapability playerCap;
    public InvoZone fullZone;
    public InvoZone shopZone;
    public InvoZone sectionZone;

    //This is basic background for now

    public TabShopScreen(TabShop tabDataCopy) {
        super(tabDataCopy.getName().getText(false));
        this.tabDataCopy = tabDataCopy.copy();
        this.playerCap = PlayerCapability.get(ClientUtil.getPlayer());
    }

    @Override
    protected void init() {
        super.init();
        this.fullZone = new InvoZone(0, this.width, 0, this.height);
        this.shopZone = this.fullZone.copy().splitWidth(1, 0.5F).splitHeight(1, 0.8F).center(this.fullZone);
        sectionZone = this.shopZone.copy().splitHeight(1, 0.7F).setY(this.shopZone.down());

        InvoZone tabZone = this.shopZone.splitHeight(1, 0.1F).setDown(this.sectionZone.y());
//        this.tabList = new InvoHorizontalList(tabZone);

        //In init, I should be allowed to resize the InvoHorizontalList

    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics) {
        super.renderBackground(pGuiGraphics);

        //Renders the background
//        InvoZone cutOutZone = sectionBackgroundImage.getRenderZone().copy().inflate(-4,-4);
//        InvoZone renderZone = this.sectionZone.copy().inflate(4, 4);
//        sectionBackgroundImage.renderNineSlice(pGuiGraphics.pose(), cutOutZone, renderZone, true, InvoImage.NineSliceType.TILE);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        //This will render after
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> @NotNull T addRenderableWidget(@NotNull T pWidget) {
        return super.addRenderableWidget(pWidget);
    }
}

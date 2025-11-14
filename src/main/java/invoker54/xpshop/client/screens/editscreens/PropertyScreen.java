package invoker54.xpshop.client.screens.editscreens;

import invoker54.invocore.client.util.InvoImage;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.InvoList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PropertyScreen extends InvoScreen {
    private InvoList propertyList;

    public static InvoImage sectionBackgroundImage = InvoImage.fromSprite(
            ResourceUtil.create(XPShop.MOD_ID, "shop/section_background"));

    public PropertyScreen(InvoText pTitle) {
        super(pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.setZone(this.getZoneCopy().splitHeight(4,3).splitWidth(4,1));

        this.propertyList = new InvoList(this, this.getZoneCopy(), this.getZoneCopy().splitHeight(8,1), 10);
        this.addRenderableWidget(this.propertyList);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics) {
        //Renders the background
        InvoZone cutOutZone = sectionBackgroundImage.getRenderZone().copy().inflate(-4);
        InvoZone renderZone = this.sectionZone.copy().inflate(4);
        sectionBackgroundImage.renderNineSlice(pGuiGraphics.pose(), cutOutZone, renderZone, true, InvoImage.NineSliceType.TILE);
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

    public InvoList getPropertyList(){
        return this.propertyList;
    }


}

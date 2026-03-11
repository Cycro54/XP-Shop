package invoker54.xpshop.client.widgets.popup;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.invoimage.InvoImageTexture;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.InvoList;
import invoker54.xpshop.client.widgets.InvoWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.List;
import java.util.function.Consumer;

public class InvoPopup extends InvoWidget {

    public InvoList list;
    public InvoImage backgroundImage;
    public float horizontalPadding;
    public float verticalPadding;
    public float maxListHeight;

    public InvoPopup(InvoScreen screen, InvoZone widgetZone, InvoPopup.Builder builder){
        super(screen, widgetZone);
        this.list = builder.list;
        this.backgroundImage = builder.backgroundImage;
        this.horizontalPadding = builder.horizontalPadding;
        this.verticalPadding = builder.verticalPadding;
        this.maxListHeight = builder.maxListHeight;

        this.list.setZone(this.getZoneCopy().inflate(-this.horizontalPadding, -this.verticalPadding));
        this.entryList.add(this.list);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (backgroundImage instanceof InvoImageTexture){
//            ((InvoImageTexture)backgroundImage).renderTiles(pGuiGraphics.pose(), this.getZoneCopy(), false, false);
        }
        else {
            backgroundImage.render(pGuiGraphics.pose());
        }

        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (!this.getZoneCopy().inBounds(new Vector2f((float) pMouseX, (float) pMouseY))){
            this.getScreen().setPopup(null);
            return false;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public static class Builder{
        public InvoList list;
        public InvoImage backgroundImage;
        public float horizontalPadding;
        public float verticalPadding;
        public float maxListHeight;

        public Builder(InvoScreen screen, float pMouseX, float pMouseY){
            InvoZone maxListZone = screen.getZoneCopy().splitHeight(3, 2).splitWidth(10, 1)
                    .setX(pMouseX).setY(pMouseY);
        }

        public Builder(InvoScreen screen, InvoZone maxListZone){
         this.list = new InvoList(screen, maxListZone.splitHeight(1,2),
                 maxListZone, maxListZone.splitHeight(8,1),
                 (int) maxListZone.copy().splitWidth(10,1).width());
         this.backgroundImage = InvoImage.fromTexture(ResourceUtil.create(XPShop.MOD_ID, "shop/section_background"));
         this.horizontalPadding = 4;
         this.verticalPadding = maxListZone.height()/2F;
         this.maxListHeight = screen.getZoneCopy().splitHeight(4,3).height();
        }

        public Builder addEntry(InvoZone widgetZone, Consumer<InvoList.InvoListEntry> consumer, List<InvoWidget> list) {
            this.list.addEntry(widgetZone, consumer, list);
            return this;
        }

        public Builder setBackgroundImage(InvoImage backgroundImage) {
            this.backgroundImage = backgroundImage;
            return this;
        }

        public Builder setHorizontalPadding(float horizontalPadding) {
            this.horizontalPadding = horizontalPadding;
            return this;
        }

        public Builder setVerticalPadding(float verticalPadding) {
            this.verticalPadding = verticalPadding;
            return this;
        }

        public Builder setMaxListHeight(float maxListHeight) {
            this.maxListHeight = maxListHeight;
            return this;
        }

        public InvoZone getEntryWidgetZone(boolean max) {
            return this.list.getEntryZone(max).copy();
        }

        public InvoPopup build() {
            InvoZone listZone = this.list.getZoneCopy();
            Vector2f topLeft = listZone.topLeft();

            listZone.setHeight(Math.min(this.maxListHeight, this.list.getFullWidgetHeight()));
            InvoZone popupZone = listZone.copy().inflate(this.horizontalPadding, this.verticalPadding)
                    .setX(topLeft.x).setY(topLeft.y)
                    .setBound(this.list.screen.getOriginalZone());
            return new InvoPopup(this.list.screen, popupZone, this);
        }
    }
}

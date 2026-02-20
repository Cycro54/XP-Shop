package invoker54.xpshop.client.widgets;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.invoimage.InvoImageColor;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.MathUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.InvoTheme;
import invoker54.xpshop.client.screens.InvoScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InvoList extends InvoWidget {
    private static final ModLogger LOGGER = ModLogger.getLogger(InvoList.class, XPShop.debugMode);

    public float yOffset;
    public float scrollPercentage;
    private final int barWidth;
    private final int padding = 2;
    private boolean isScrolling = false;

    private static InvoImageColor example;

    private final List<InvoListEntry> fullList;
    //Dynamic width will always be true, the entries should always be the same width as the list, they are only containers for the actual widgets.
    //public boolean dynamicWidth = true;
    public InvoZone entryZone;
    public InvoZone maxWidgetZone;

    public InvoList(InvoScreen screen, InvoZone widgetZone, InvoZone maxWidgetZone, InvoZone entryZone, int barWidth) {
        super(screen, widgetZone);
        this.maxWidgetZone = maxWidgetZone;
        this.entryZone = entryZone;
        this.fullList = new ArrayList<>();
        this.scrollPercentage = entryZone.height()/this.maxWidgetZone.height();
        this.barWidth = barWidth;

        LOGGER.info("What's my zone? " + this.getZoneCopy());
    }

    public void addEntry(InvoWidget widget){
        addEntry(widget.getZoneCopy(), entry -> {
            widget.setZone(entry.getZoneCopy());
        }, List.of(widget));
    }

    public void addEntry(InvoZone entryZone, Consumer<InvoListEntry> widgetBuilder, List<InvoWidget> list) {
        InvoListEntry entry = new InvoListEntry(this.screen, entryZone, this, widgetBuilder, list);
        this.addEntry(entry);
    }

    public void addEntry(InvoListEntry entry){
        float lastZoneBottomY = this.fullList.isEmpty() ? this.getY() : this.fullList.get(this.fullList.size()-1).getZoneCopy().down();

        entryZone = entryZone.copy().setWidth((float) MathUtil.clamp(entryZone.width(), this.getWidth(),
                this.maxWidgetZone.width())).setY(lastZoneBottomY);
        entry.setZone(entryZone);

        this.fullList.add(entry);

        InvoZone adjustedZone = this.getZoneCopy();
        LOGGER.error("What's current zone: " + adjustedZone);
        LOGGER.error("Full Widget Height: " + this.getFullWidgetHeight());
        LOGGER.error("MaxWidgetZone: " + this.maxWidgetZone);

        adjustedZone.setWidth((float) MathUtil.clamp(adjustedZone.width(), entry.getWidth(), this.maxWidgetZone.width())).
                setHeight((float) MathUtil.clamp(this.getFullWidgetHeight(), adjustedZone.height(), this.maxWidgetZone.height())).
                center(this.screen.getOriginalZone());

        this.setZone(adjustedZone);
    }

    public void removeEntry(InvoListEntry entry){
        if (!this.entryList.remove(entry)) return;
        this.setZone(this.getZoneCopy());
    }

    public InvoZone getEntryZone(){
        return this.entryZone.copy();
    }

    public float getFullWidgetHeight(){
        float fullHeight = 0;
        for (InvoListEntry entry : this.fullList){
            fullHeight += entry.getHeight() + this.padding;
        }
        if (fullHeight != 0) fullHeight -= padding;

        return fullHeight;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        boolean childScrolled = super.mouseScrolled(pMouseX, pMouseY, pDelta);
        if (childScrolled) return true;

        Vector2f mousePos = new Vector2f((float) pMouseX, (float) pMouseY);
        if (!this.getZoneCopy().inBounds(mousePos)) return false;
        if (this.getFullWidgetHeight() < this.getHeight()) return false;
        this.shiftYOffset((float) ((this.scrollPercentage * this.getHeight()) * pDelta));
//        this.setZone(this.getZoneCopy());
        return true;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        Vector2f mousePoint = new Vector2f((float) pMouseX, (float) pMouseY);
//        if (!this.getZoneCopy().inBounds(mousePoint)) return false;
        if (getScrollBackgroundZone().inBounds(mousePoint) && pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT){
            this.isScrolling = true;
            LOGGER.info("I am enabling scroll");
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
//        LOGGER.info("I am releasing scroll");
        this.isScrolling = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.isScrolling){
            double actualDragAmount = pDragY/getScrollBackgroundZone().inflate(0,-1).height();
            actualDragAmount = actualDragAmount * this.getFullWidgetHeight();
            this.shiftYOffset((float) -(actualDragAmount));
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    public void shiftYOffset(float offset){
//        LOGGER.error("What's the full height: " + this.getFullWidgetHeight());
//        LOGGER.error("Offset: " + offset);
        this.yOffset = (float) MathUtil.clamp(this.yOffset + offset,
                this.getZoneCopy().height() - this.getFullWidgetHeight(), 0);
//        LOGGER.warn("New Offset: "+ this.yOffset);
        this.setZone(this.getZoneCopy());
    }

    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick){
        InvoTheme.getBackground().render(pGuiGraphics.pose(), this.getZoneCopy());
//        InvoImageColor.fromColor(Color.RED).render(pGuiGraphics.pose(), this.getZoneCopy());
//        InvoImage image = InvoTheme.getBackground().canvas(InvoTheme.getBackground().getMainZoneCopy());
//        image.setMainZone(image.getMainZoneCopy().setX(100), false);
//        image.render(pGuiGraphics.pose());
//        InvoImage.fromColor(Color.RED).render(pGuiGraphics.pose(), this.getZoneCopy());
//        example.render(pGuiGraphics.pose());
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.enableScissor(this.getX(), this.getY(),
                this.getX() + this.getWidth(), this.getY() + this.getHeight());

        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.disableScissor();

        this.renderScrollBar(pGuiGraphics, pMouseX, pMouseY);
    }

    public void renderScrollBar(GuiGraphics guiGraphics, int pMouseX, int pMouseY){
        if (this.getFullWidgetHeight() < this.getHeight()) return;

        InvoZone scrollBackgroundZone = this.getScrollBackgroundZone();
        //Scroll background
        InvoImage.fromColor(Color.black).render(guiGraphics.pose(), scrollBackgroundZone);
        //Scroll block
        InvoZone blockZone = getScrollBlockZone();

        Color blockColor = blockZone.inBounds(new Vector2f(pMouseX, pMouseY))
                || this.isScrolling ? Color.gray : Color.WHITE;
        InvoImage.fromColor(blockColor).render(guiGraphics.pose(), blockZone);
//        LOGGER.warn("Is this running");
    }

    public InvoZone getScrollBackgroundZone(){
        return this.getZoneCopy().setWidth(this.barWidth).setRight(this.getZoneCopy().right());
    }

    public InvoZone getScrollBlockZone(){
        InvoZone scrollBackgroundZone = this.getScrollBackgroundZone();
        float maxOffset = this.getFullWidgetHeight() - this.getZoneCopy().height();
        float offsetPercentage = this.yOffset / maxOffset;
        float scrollBlockHeight = scrollBackgroundZone.height() * (this.getHeight()/this.getFullWidgetHeight());
        float emptyHeight = (scrollBackgroundZone.height() - scrollBlockHeight);
        return scrollBackgroundZone.copy().setHeight(scrollBlockHeight).
                shiftXY(0, - (emptyHeight * offsetPercentage)).inflate(-1,-1);
    }

    @Override
    public void setZone(InvoZone newZone) {
        super.setZone(newZone);
        if (this.entryZone == null) return;
//        this.entryZone.setWidth(newZone.width() - this.barWidth);

        this.entryList.clear();
        float fullWidgetHeight = this.getFullWidgetHeight();
        boolean isAboveMax = fullWidgetHeight > this.maxWidgetZone.height();
//        LOGGER.warn("1Y Offset: " + this.yOffset);
//        LOGGER.error("Involist what's my zone? " + this.getZoneCopy());

//        LOGGER.error("2Y Offset: " + this.yOffset);
        float currentY = this.yOffset + this.getZoneCopy().y();
//        LOGGER.error("Involist what's currentY? " + currentY);

//        boolean isMaxHeight = this.getZoneCopy().height() == Math.floor(this.maxWidgetZone.height());
//        LOGGER.info("What's combined widget height? " + fullWidgetHeight);
//        LOGGER.warn("My zone: " + this.getZoneCopy());
//        LOGGER.warn("Max zone: " + this.maxWidgetZone);

//        LOGGER.error("START THE TRIALS!");
        for (InvoListEntry entry : this.fullList){
//            LOGGER.error("Before entry zone: " + entry.getZoneCopy());
            InvoZone updatedZone = entry.getZoneCopy().setY(currentY).setWidth(newZone.width()).centerX(newZone.middleX());
            if (isAboveMax) updatedZone.shiftWH(-this.barWidth - 1,0);
            entry.setZone(updatedZone);
//            LOGGER.warn("UpdatedZone: " + entry.getZoneCopy());
//            LOGGER.warn("NewZone: " + this.getZoneCopy());
//            entry.setZone(entry.getZoneCopy().setY(currentY).setWidth(newZone.width()).centerX(newZone.middleX()));
//            LOGGER.warn("After entry zone: " + entry.getZoneCopy());
            currentY = entry.getZoneCopy().down() + padding;

//            LOGGER.warn("EntryZone: " + entry.getZoneCopy() + ", ListZone: " + newZone + " N: " + count);
//            LOGGER.info("Is it in the bounds? " + (entry.getZoneCopy().inBounds(newZone, false))
//                    + (" : "+entry.getZoneCopy().y()) + (" | TOP: "+newZone.y()) + " BOT: " + (newZone.down()) + " |N: " + count);
//            count++;

            if (entry.getZoneCopy().inBounds(this.getZoneCopy(), false)){
//                LOGGER.warn("I was inside!");
                this.entryList.add(entry);
//                entry.readjustWidgets();
//                LOGGER.warn("After adjustment: " + entry.getZoneCopy());
//                LOGGER.info("It's in the zone");
            }
//            else {
//                LOGGER.warn("what was outside?");
//                LOGGER.warn("Top Left: " + (entry.getZoneCopy().topLeft()) + (newZone.topLeft()));
//                LOGGER.warn("Top Right: " + (entry.getZoneCopy().topRight()) + (newZone.topRight()));
//                LOGGER.warn("Bottom Left: " + (entry.getZoneCopy().bottomLeft()) + (newZone.bottomLeft()));
//                LOGGER.warn("Bottom Right: " + (entry.getZoneCopy().bottomRight()) + (newZone.bottomRight()));
//
//            }
        }
        this.entryZone.setWidth(newZone.width()).center(newZone).setY(currentY);

    }

    public static class InvoListEntry extends InvoWidget{
        public final InvoList parent;
        public final Consumer<InvoListEntry> widgetSizer;

        public InvoListEntry(InvoScreen screen, InvoZone widgetZone, InvoList parent,
                             Consumer<InvoListEntry> widgetSizer, List<InvoWidget> widgetList) {
            super(screen, widgetZone);
            this.parent = parent;
            this.widgetSizer = widgetSizer;
            this.entryList.addAll(widgetList);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            for (InvoWidget widget : this.entryList){
                widget.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }
//            InvoImageColor.fromColor(Color.GRAY).render(pGuiGraphics.pose(), this.getZoneCopy().intersect(this.parent.getZoneCopy()));
//            LOGGER.info("Entry zone: " + this.getZoneCopy());
//            LOGGER.info("Button zone: " + this.entryList.get(0).getZoneCopy());
//            LOGGER.info("Image zone: " + ((InvoButton)this.entryList.get(0)).getBackgroundImage().getMainZoneCopy());
        }

        @Override
        public void setZone(InvoZone zone) {
            super.setZone(zone);
            if (this.widgetSizer != null) this.widgetSizer.accept(this);
        }

        //        public void readjustWidgets(){
////            InvoZone currentZone = this.getZoneCopy();
////            InvoZone combinedWidgetZone = currentZone.copy();
////            for (InvoWidget widget : this.widgetList){
////                combinedWidgetZone.merge(widget.getZoneCopy());
////            }
////            if (combinedWidgetZone.inBounds(currentZone,true)) return;
////            this.setZone(combinedWidgetZone);
////            this.parent.setZone(this.parent.getZoneCopy());
//        }
    }
}
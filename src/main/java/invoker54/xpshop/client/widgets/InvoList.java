package invoker54.xpshop.client.widgets;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.util.MathUtil;
import invoker54.xpshop.client.screens.InvoScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InvoList extends InvoWidget {
    public float yOffset;
    public int scrollPercentage;
    private int barWidth;

    private float combinedWidgetHeight;

    private final List<InvoListEntry> fullList;
    //Dynamic width will always be true, the entries should always be the same width as the list, they are only containers for the actual widgets.
    //public boolean dynamicWidth = true;
    public InvoZone entryZone;

    public InvoList(InvoScreen screen, InvoZone widgetZone, InvoZone entryZone, int barWidth) {
        super(screen, widgetZone);
        this.barWidth = barWidth;
        this.fullList = new ArrayList<>();
        this.scrollPercentage = (int) entryZone.height();
        this.entryZone = entryZone;
    }

    public void addEntry(InvoZone entryZone, Consumer<InvoListEntry> widgetBuilder, List<InvoWidget> list) {
        InvoListEntry entry = new InvoListEntry(this.screen, entryZone, this, widgetBuilder, list);

        float lastZoneBottomY = this.fullList.isEmpty() ? this.getY() : this.fullList.get(0).getZoneCopy().down();
        this.fullList.add(entry);
        this.combinedWidgetHeight += entry.getHeight();
        entry.setZone(entry.getZoneCopy().setY(lastZoneBottomY));

        float entryWidth = entry.getWidth();
        float listWidth = this.getWidth();
        int entryWidthDifference = (int) (entry.getZoneCopy().width() - this.entryZone.width());

        if (entryWidth > listWidth){
            this.setZone(this.getZoneCopy().inflate(entryWidthDifference/2F, 0).center(this.getZoneCopy()));
        } else if (entryWidth < listWidth) {
            entry.setZone(entry.getZoneCopy().inflate(entryWidthDifference/2F,0).center(this.getZoneCopy()));
        }
    }

    public void removeEntry(InvoListEntry entry){
        if (!this.widgetList.remove(entry)) return;
        this.combinedWidgetHeight -= entry.getHeight();
        this.setZone(this.getZoneCopy());
    }

    public InvoZone getEntryZone(){
        return this.entryZone.copy();
    }

    public float getCombinedWidgetHeight(){
        return this.combinedWidgetHeight;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        boolean childScrolled = super.mouseScrolled(pMouseX, pMouseY, pDelta);
        if (childScrolled) return true;

        Vector2f mousePos = new Vector2f((float) pMouseX, (float) pMouseY);
        if (!this.getZoneCopy().inBounds(mousePos)) return false;
        if (this.combinedWidgetHeight < this.getHeight()) return false;

        int scrollAmount = (int) ((this.scrollPercentage * this.getHeight()) * pDelta);
        this.yOffset = (float) MathUtil.clamp(this.yOffset + scrollAmount, (this.getZoneCopy().height() - this.combinedWidgetHeight),0);

        this.setZone(this.getZoneCopy());
        return true;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.enableScissor(this.getX(), this.getY(),
                this.getX() + this.getWidth(), this.getY() + this.getHeight());

        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.disableScissor();

        this.renderScrollBar(pGuiGraphics, pMouseX, pMouseY);
    }

    public void renderScrollBar(GuiGraphics guiGraphics, int pMouseX, int pMouseY){
        if (this.combinedWidgetHeight < this.getHeight()) return;

        InvoZone scrollBackgroundZone = this.getScrollZone();
        //Scroll background
        ClientUtil.blitColor(guiGraphics.pose(), scrollBackgroundZone, Color.black.getRGB());
        //Scroll block
        InvoZone blockZone = scrollBackgroundZone.copy().
                setHeight(this.getHeight()/this.combinedWidgetHeight).inflate(-1,-1);

        Color blockColor = blockZone.inBounds(new Vector2f(pMouseX, pMouseY)) ? Color.gray : Color.WHITE;

        ClientUtil.blitColor(guiGraphics.pose(), blockZone, blockColor.getRGB());
    }

    public InvoZone getScrollZone(){
        return this.getZoneCopy().setWidth(this.barWidth).setRight(this.getZoneCopy().right());
    }

    @Override
    public void setZone(InvoZone newZone) {
        super.setZone(newZone);
        this.entryZone.setWidth(newZone.width());

        this.widgetList.clear();
        this.yOffset = (float) MathUtil.clamp(this.yOffset, (this.getZoneCopy().height() - this.combinedWidgetHeight),0);
        float currentY = this.yOffset;
        for (InvoWidget entry : this.widgetList){
            entry.setZone(entry.getZoneCopy().setY(currentY).setWidth(newZone.width()).centerX(newZone.middleX()));
            currentY = entry.getZoneCopy().down();
            if (entry.getZoneCopy().inBounds(newZone, false)) this.widgetList.add(entry);
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
            this.widgetList.addAll(widgetList);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            this.readjustZone();

            for (InvoWidget widget : this.widgetList){
                widget.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }
        }

        public void readjustZone(){
            InvoZone currentZone = this.getZoneCopy();
            InvoZone combinedWidgetZone = currentZone.copy();
            for (InvoWidget widget : this.widgetList){
                combinedWidgetZone.merge(widget.getZoneCopy());
            }
            if (combinedWidgetZone.inBounds(currentZone,true)) return;
            this.setZone(combinedWidgetZone);
            this.parent.setZone(this.parent.getZoneCopy());
        }

        @Override
        public void setZone(InvoZone newZone) {
//            InvoZone oldZone = this.getZoneCopy();
            super.setZone(newZone);
            this.widgetSizer.accept(this);
        }
    }
}

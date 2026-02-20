package invoker54.xpshop.client.widgets;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class InvoWidget extends AbstractWidget implements ContainerEventHandler, InvoZoneHandler {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShop.debugMode);
    protected final List<InvoWidget> entryList;
    public final InvoScreen screen;
    protected InvoText pMessage;
    protected InvoZone trueZone;

    public InvoWidget(InvoScreen screen, InvoZone widgetZone) {
        this(screen, widgetZone, InvoText.translate(XPShopLanguageprovider.defaultWidgetText));
    }

    public InvoWidget(InvoScreen screen, InvoZone widgetZone, InvoText pMessage) {
        super(0,0,0,0, pMessage.getText());
        this.screen = screen;
        this.setZone(widgetZone);
        this.entryList = new ArrayList<>();
        this.pMessage = pMessage;
    }

    public void setZone(InvoZone zone){
        this.setX((int) zone.x());
        this.setY((int) zone.y());
        this.setWidth((int) zone.width());
        this.setHeight((int) zone.height());
        this.trueZone = zone.copy();
    }

    @Override
    public InvoZone getZoneCopy() {
        return this.trueZone.copy();
    }

    public InvoScreen getScreen(){
        return this.screen;
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        for (InvoWidget widget : this.entryList){
            widget.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return this.entryList;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return ContainerEventHandler.super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return ContainerEventHandler.super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return ContainerEventHandler.super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean isDragging() {
        return false;
    }

    @Override
    public void setDragging(boolean pIsDragging) {

    }

    @Override
    public @Nullable GuiEventListener getFocused() {
        GuiEventListener focusedEventListener = this.screen.getFocused();
        return focusedEventListener == this ? null : focusedEventListener;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener pFocused) {
        this.screen.setFocused(pFocused);
    }

    public void removeFromScreen(){
        this.screen.removeWidget(this);
    }

    public void removeWidget(GuiEventListener... eventListeners){
        Arrays.stream(eventListeners).iterator().forEachRemaining(this.screen::removeWidget);
    }
}

package invoker54.xpshop.client.widgets;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.client.screens.InvoScreen;
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
    public final List<InvoWidget> widgetList;
    public final InvoScreen screen;
    public InvoText pMessage;

    public InvoWidget(InvoScreen screen, InvoZone widgetZone) {
        this(screen, widgetZone, InvoText.translate("xp_shop.screen.widget.invowidget.default"));
    }

    public InvoWidget(InvoScreen screen, InvoZone widgetZone, InvoText pMessage) {
        super(0,0,0,0, pMessage.getText());
        this.screen = screen;
        this.setZone(widgetZone);
        this.widgetList = new ArrayList<>();
        this.pMessage = pMessage;
    }

    public void setZone(InvoZone zone){
        this.setX((int) zone.x());
        this.setY((int) zone.y());
        this.setWidth((int) zone.width());
        this.setHeight((int) zone.height());
    }

    public InvoZone getZoneCopy(){
        return new InvoZone(this.getX(), this.getWidth(), this.getY(), this.getHeight());
    }

    public InvoScreen getScreen(){
        return this.screen;
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        for (InvoWidget widget : this.widgetList){
            widget.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return this.widgetList;
    }

    @Override
    public boolean isDragging() {
        return false;
    }

    @Override
    public void setDragging(boolean pIsDragging) {}

    @Override
    public @Nullable GuiEventListener getFocused() {return null;}

    @Override
    public void setFocused(@Nullable GuiEventListener pFocused) {}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    public void removeFromScreen(){
        this.screen.removeWidget(this);
    }

    public void removeWidget(GuiEventListener... eventListeners){
        Arrays.stream(eventListeners).iterator().forEachRemaining(this.screen::removeWidget);
    }
}

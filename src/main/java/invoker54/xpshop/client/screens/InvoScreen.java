package invoker54.xpshop.client.screens;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.client.widgets.InvoZoneHandler;
import invoker54.xpshop.client.widgets.popup.InvoPopup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public class InvoScreen extends Screen implements InvoZoneHandler {
    public InvoPopup popup;
    private final InvoZone mainZone = new InvoZone(0,0,0,0);
    private InvoText pTitle;

    protected InvoScreen(InvoText pTitle) {
        super(pTitle.getText());
        this.pTitle = pTitle;
    }

    @Override
    protected void init() {
        super.init();
        this.setZone(new InvoZone(0, this.width, 0, this.height));
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.popup != null) return this.popup.mouseScrolled(pMouseX, pMouseY, pDelta);
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.popup != null) return this.popup.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (this.popup != null) return this.popup.mouseReleased(pMouseX, pMouseY, pButton);
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.popup != null) return this.popup.mouseClicked(pMouseX, pMouseY, pButton);
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void removeWidget(@NotNull GuiEventListener pListener) {
        super.removeWidget(pListener);
    }

    public void setPopup(InvoPopup popup){
        if (popup != null){
            this.addRenderableWidget(popup);
            this.popup = popup;
            this.setFocused(popup);
        }
        else {
            this.removeWidget(this.popup);
            this.popup = null;
            this.setFocused(null);
        }
    }

    public InvoZone getOriginalZone(){
        return new InvoZone(0, this.width, 0, this.height);
    }

    public void setZone(InvoZone newZone){
        this.mainZone.copy(newZone);
    }

    public InvoZone getZoneCopy(){
        return this.mainZone.copy();
    }
}

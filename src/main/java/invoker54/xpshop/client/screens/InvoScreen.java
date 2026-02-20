package invoker54.xpshop.client.screens;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.widgets.InvoZoneHandler;
import invoker54.xpshop.client.widgets.popup.InvoPopup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public class InvoScreen extends Screen implements InvoZoneHandler {
    private static final ModLogger LOGGER = ModLogger.getLogger(InvoScreen.class, XPShop.debugMode);

    public InvoPopup popup;
    public InvoText pTitle;
    public final InvoZone trueZone = new InvoZone(0,0,0,0);

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
//        LOGGER.error("Screen is releasing!");
        if (this.popup != null) return this.popup.mouseReleased(pMouseX, pMouseY, pButton);
        boolean isConsumed = this.getFocused() != null && this.getFocused().mouseReleased(pMouseX, pMouseY, pButton);
        if (!isConsumed) isConsumed = super.mouseReleased(pMouseX, pMouseY, pButton);
        return isConsumed;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.popup != null) return this.popup.mouseClicked(pMouseX, pMouseY, pButton);
        for(GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        this.setFocused(null);
        return false;
//        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
//        LOGGER.warn("is there something focused? " + (this.getFocused() != null));
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void removeWidget(@NotNull GuiEventListener pListener) {
        super.removeWidget(pListener);
    }

    public InvoText getMessage() {
        return this.pTitle;
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
        this.trueZone.copy(newZone);
    }

    @Override
    public InvoZone getZoneCopy() {
        return this.trueZone.copy();
    }
}

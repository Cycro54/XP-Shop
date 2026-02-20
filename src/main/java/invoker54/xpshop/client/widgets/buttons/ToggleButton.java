package invoker54.xpshop.client.widgets.buttons;

import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.InvoWidget;
import net.minecraft.client.gui.GuiGraphics;

public class ToggleButton extends InvoWidget {

    protected boolean toggled = false;
    protected InvoButton trueButton;
    protected InvoButton falseButton;

    public ToggleButton(InvoScreen screen, InvoZone widgetZone, InvoButton trueButton, InvoButton falseButton) {
        this(screen, widgetZone, trueButton, falseButton, false);
    }

    public ToggleButton(InvoScreen screen, InvoZone widgetZone, InvoButton trueButton, InvoButton falseButton, boolean toggled) {
        super(screen, widgetZone);
        this.trueButton = trueButton;
        this.falseButton = falseButton;
        this.toggled = toggled;
    }

    public boolean isToggled(){
        return this.toggled;
    }

    public InvoButton getSelectedButton(){
        return this.toggled ? trueButton : falseButton;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return getSelectedButton().mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return getSelectedButton().mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public void setZone(InvoZone zone) {
        super.setZone(zone);
        this.trueButton.setZone(zone);
        this.falseButton.setZone(zone);
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.getSelectedButton().renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}

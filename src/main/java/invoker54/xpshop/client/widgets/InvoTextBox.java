package invoker54.xpshop.client.widgets;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.util.MathUtil;
import invoker54.xpshop.client.screens.InvoScreen;

public class InvoTextBox extends InvoWidget{
    public final float textHeight;
    public final int maxBreaks;
    public final int heightPadding = 1;
    public String value = "";

    public final InvoZone startZone;
    public final InvoZone endZone;

    public InvoTextBox(InvoScreen screen, InvoZone widgetZone) {
        super(screen, widgetZone);
        InvoText.literal("FAfa");
    }

    public InvoText getInvoText(){

    }

    @Override
    public void setZone(InvoZone zone) {
        zone.setWidth((float) MathUtil.clamp(zone.width(), startZone.width(), endZone.width()));
        zone.setHeight((float) MathUtil.clamp(zone.height(), startZone.height(), endZone.height()));
        super.setZone(zone);
    }

    //ALRIGHT
    //StartZone(mainZone) - this is what it will be before text is added
    //EndZone - This is what it will be at the max
    //HeightPadding - Space on top of each text line
    //
}
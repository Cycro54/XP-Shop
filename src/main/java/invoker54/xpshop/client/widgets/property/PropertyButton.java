package invoker54.xpshop.client.widgets.property;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.buttons.InvoButton;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PropertyButton extends InvoButton {
    protected final Supplier<InvoText> propGetter;
    protected final Consumer<?> propSetter;

    protected PropertyButton(InvoScreen screen, InvoZone zone, PropertyButton.Builder builder) {
        super(screen, zone, builder);
        this.propGetter = builder.propGetter;
        this.propSetter = builder.propSetter;
    }

    //Name
    //Description
    //

    public static PropertyButton text(InvoScreen screen, InvoText propName, Supplier<InvoText> propGetter, Consumer<InvoText> propSetter, OnPush openEditor){
        PropertyButton.Builder builder = new PropertyButton.Builder();
         builder.setGetter(propGetter).setSetter(propSetter).setMessage(propName)
                 .setButton(0, openEditor).setButton(1, openEditor);
         return builder.build(screen);
    }


    @Override
    public void renderBackground(PoseStack stack) {
        super.renderBackground(stack);
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
//        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        //Background
        //name on the left

    }

    public static class Builder extends InvoButton.Builder {
        protected Supplier<InvoText> propGetter;
        protected Consumer<?> propSetter;

        public Builder(){
            super();
        }

        public PropertyButton.Builder setGetter(Supplier<InvoText> propGetter){
            this.propGetter = propGetter;
            return this.getBuilder();
        }

        public InvoButton.Builder setSetter(Consumer<?> propSetter){
            this.propSetter = propSetter;
            return this.getBuilder();
        }

        @Override
        public PropertyButton.Builder getBuilder() {
            return this;
        }

        @Override
        public PropertyButton build(InvoScreen screen) {
            InvoZone fullZone = new InvoZone(0,0,0,18);
            fullZone.setWidth(ClientUtil.getFont().width(this.message.getText(false)) * 2);
//            fullZone.shiftWH(fullZone.height(), 0);
            return this.build(screen, fullZone);
        }

        @Override
        public PropertyButton build(InvoScreen screen, InvoZone widgetZone) {
            return new PropertyButton(screen, widgetZone, this);
        }
    }
}

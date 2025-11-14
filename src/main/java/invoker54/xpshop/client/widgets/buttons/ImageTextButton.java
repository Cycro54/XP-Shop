package invoker54.xpshop.client.widgets.buttons;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.client.screens.InvoScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class ImageTextButton extends InvoButton {
    public InvoImage iconImage;
    public int padding;
    
    private ImageTextButton(InvoScreen screen, InvoZone zone){
        this(screen, zone, new Builder());
    }

    private ImageTextButton(InvoScreen screen, InvoZone zone, Builder builder) {
        super(screen, zone, builder);
        this.iconImage = builder.iconImage;
        this.padding = builder.padding;
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        PoseStack stack = pGuiGraphics.pose();

        this.renderBackground(stack);

        InvoZone buttonZone = this.getZoneCopy();

        InvoZone imageZone = buttonZone.copy().setWidth(buttonZone.height());
        this.iconImage.render(stack, imageZone.inflate(-this.padding));

        InvoZone textZone = buttonZone.copy().setWidth(buttonZone.width() - imageZone.width()).setX(imageZone.right()).inflate(-this.padding);
        this.pMessage.render(stack, textZone);
    }

    public static class Builder extends InvoButton.Builder {
        private InvoImage iconImage;
        private int padding;

        public Builder(){
            super();
            this.iconImage = InvoImage.fromSprite(ResourceUtil.create("dirt.png"));
            this.padding = 1;
        }
        
        public Builder setIconImage(InvoImage iconImage) {
            this.iconImage = iconImage;
            return this.getBuilder();
        }
        
        public Builder setPadding(int padding) {
            this.padding = padding;
            return this.getBuilder();
        }

        @Override
        public ImageTextButton.Builder getBuilder() {
            return this;
        }

        @Override
        public ImageTextButton build(InvoScreen screen, InvoZone widgetZone) {
            return new ImageTextButton(screen, widgetZone, this);
        }


    }
}

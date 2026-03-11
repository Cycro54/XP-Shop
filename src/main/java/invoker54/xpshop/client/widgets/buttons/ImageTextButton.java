package invoker54.xpshop.client.widgets.buttons;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.InvoScreen;
import net.minecraft.client.gui.GuiGraphics;

public class ImageTextButton extends InvoButton {
    private static final ModLogger LOGGER = ModLogger.getLogger(ImageTextButton.class, XPShop.debugMode);

    public InvoImage iconImage;
    public int padding;
    
    private ImageTextButton(InvoScreen screen, InvoZone zone){
        this(screen, zone, new Builder());
    }

    private ImageTextButton(InvoScreen screen, InvoZone zone, Builder builder) {
        super(screen, zone, builder);
        this.iconImage = builder.iconImage.copy();
        this.padding = builder.padding;
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
//        LOGGER.warn("Render button");
        PoseStack stack = pGuiGraphics.pose();

//        InvoImageColor.fromColor(Color.RED).render(stack, this.getBackgroundImage().getFullZone());
        this.renderBackground(stack);

        InvoZone buttonZone = this.getBackgroundImage().getMainZoneCopy();
//        LOGGER.warn("What's the background: " + buttonZone);

        InvoZone imageZone = buttonZone.copy().setWidth(buttonZone.height());
        this.iconImage.render(stack, imageZone.inflate(-this.padding));

        InvoZone textZone = buttonZone.copy().setWidth(buttonZone.width() - imageZone.width()).setX(imageZone.right()).inflate(-this.padding);
        this.getText().render(stack, textZone, false);
//        LOGGER.warn("Finish button");
    }

    public static class Builder extends InvoButton.Builder<Builder> {
        private InvoImage iconImage;
        private int padding;

        public Builder(){
            super();
            this.iconImage = InvoImage.fromTexture(ResourceUtil.create("dirt.png"));
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
        public ImageTextButton build(InvoScreen screen) {
            InvoZone fullZone = new InvoZone(0,0,0,18);
            fullZone.setWidth(ClientUtil.getFont().width(this.message.getText(false)));
            fullZone.setWidth(fullZone.width() + fullZone.height());
            return this.build(screen, fullZone);
        }

        @Override
        public ImageTextButton build(InvoScreen screen, InvoZone widgetZone) {
            return new ImageTextButton(screen, widgetZone, this);
        }
    }
}

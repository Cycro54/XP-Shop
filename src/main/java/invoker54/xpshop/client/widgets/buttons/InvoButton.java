package invoker54.xpshop.client.widgets.buttons;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.invoimage.InvoImageSprite;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.InvoWidget;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;
import java.util.Map;

public class InvoButton extends InvoWidget {
    public final Map<Integer, OnPush> pushMap;

    public InvoImage clickedImage;
    public InvoImage disabledImage;
    public InvoImage normalImage;
    public InvoImage hoveredImage;

    public boolean isClicked = false;

    protected InvoButton(InvoScreen screen, InvoZone zone){
        this(screen, zone, new InvoButton.Builder());
    }

    protected InvoButton(InvoScreen screen, InvoZone zone, InvoButton.Builder builder) {
        super(screen, zone, builder.message);
        this.pushMap = builder.pushMap;
        this.pMessage = builder.message;
        this.clickedImage = builder.clickedImage;
        this.disabledImage = builder.disabledImage;
        this.normalImage = builder.normalImage;
        this.hoveredImage = builder.hoveredImage;
    }

    public void playClickSound(){
        this.playDownSound(ClientUtil.getMinecraft().getSoundManager());
    }

    public void playReleaseSound(){
        this.playDownSound(ClientUtil.getMinecraft().getSoundManager());
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (!this.clicked(pMouseX, pMouseY)) return false;
        this.isClicked = true;
        return onInteract(true, pButton, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (!this.isClicked) return false;
        this.isClicked = false;
        return onInteract(false, pButton, pMouseX, pMouseY);
    }

    public OnPush getPush(Integer button){
        return this.pushMap.getOrDefault(button, null);
    }

    public boolean onInteract(boolean isClick, int pButton, double pMouseX, double pMouseY){
        if ((!this.active || !this.visible)) return false;
        OnPush push = this.getPush(pButton);
        if (push == null) return false;
        return push.clicked(isClick, this, pMouseX, pMouseY);
    }

    public InvoImage getBackgroundImage(){
        if (!this.active) return this.disabledImage;
        if (this.isClicked) return this.clickedImage;
        if (this.isHovered()) return this.hoveredImage;
        return this.normalImage;
    }

    public void renderBackground(PoseStack stack){
        InvoImage selectedImage = this.getBackgroundImage();

        InvoZone cutOutZone = selectedImage.getRenderZone().copy().inflate(-4,-4);
        InvoZone renderZone = this.getZoneCopy();
        if (selectedImage instanceof InvoImageSprite sprite){
            sprite.renderNineSlice(stack, cutOutZone, renderZone, true, InvoImageSprite.NineSliceType.TILE);
        }
        else {
            selectedImage.render(stack, renderZone);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        PoseStack stack = pGuiGraphics.pose();

        this.renderBackground(stack);

        this.pMessage.render(stack, this.getZoneCopy());
    }

    public interface OnPush{
        boolean clicked(boolean isClick, InvoButton button, double pMouseX, double pMouseY);
    }

    public static class Builder {
        public static InvoImageSprite defaultImage = InvoImage.fromSprite(ResourceUtil.create("gui/widgets.png"));

        private final Map<Integer, OnPush> pushMap = new HashMap<>();

        private InvoText message;
        private InvoImage clickedImage;
        private InvoImage disabledImage;
        private InvoImage normalImage;
        private InvoImage hoveredImage;

        public Builder() {
            this.message = InvoText.translate("xp_shop.screen.widget.invowidget.default");

            this.clickedImage = defaultImage.crop(
                    defaultImage.getRenderZone().copy().setWidth(200).setHeight(20).shift(0, 46));
            this.disabledImage = this.clickedImage.copy();

            this.normalImage = this.disabledImage.copy();
            ((InvoImageSprite) this.normalImage).getImageZone().shift(0, 20);

            this.hoveredImage = this.normalImage.copy();
            ((InvoImageSprite) this.hoveredImage).getImageZone().shift(0, 20);
        }

        public Builder setButton(Integer button, OnPush push){
            this.pushMap.put(button, push);
            return this.getBuilder();
        }

        public Builder setMessage(InvoText message) {
            this.message = message;
            return this.getBuilder();
        }

        public Builder setClickedImage(InvoImage clickedImage) {
            this.clickedImage = clickedImage;
            return this.getBuilder();
        }

        public Builder setDisabledImage(InvoImage disabledImage) {
            this.disabledImage = disabledImage;
            return this.getBuilder();
        }

        public Builder setNormalImage(InvoImage normalImage) {
            this.normalImage = normalImage;
            return this.getBuilder();
        }

        public Builder setHoveredImage(InvoImage hoveredImage) {
            this.hoveredImage = hoveredImage;
            return this.getBuilder();
        }
        
        public Builder getBuilder(){
            return this;
        }

        public InvoButton build(InvoScreen screen, InvoZone widgetZone){
            return new InvoButton(screen, widgetZone, this);
        }

        public InvoButton build(InvoScreen screen){
            InvoZone textZone = new InvoZone(0,0,0,9);
            textZone.setWidth(ClientUtil.getFont().width(this.message.getText()));
            return this.build(screen, textZone);
        }
    }
}

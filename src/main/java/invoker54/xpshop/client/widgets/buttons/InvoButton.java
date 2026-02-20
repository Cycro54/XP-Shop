package invoker54.xpshop.client.widgets.buttons;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.invoimage.*;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.InvoTheme;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.InvoWidget;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import org.jline.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoButton extends InvoWidget {
    private static final ModLogger LOGGER = ModLogger.getLogger(InvoButton.class, XPShop.debugMode);

    public final Map<Integer, OnPush> pushMap;

    public InvoImageCanvas clickedImage;
    public InvoImageCanvas disabledImage;
    public InvoImageCanvas normalImage;
    public InvoImageCanvas hoveredImage;

    public InvoText hoveredMessage;
    public InvoText disabledMessage;

    public boolean isClicked = false;

    protected InvoButton(InvoScreen screen, InvoZone zone){
        this(screen, zone, new InvoButton.Builder());
    }

    protected InvoButton(InvoScreen screen, InvoZone zone, InvoButton.Builder builder) {
        super(screen, zone, builder.message.deepCopy());
        if (builder.hoveredMessage == null) builder.setHoveredMessage(this.pMessage.deepCopy().withStyle(false, ChatFormatting.YELLOW));
        this.hoveredMessage = builder.hoveredMessage;
        if (builder.disabledMessage == null) builder.setHoveredMessage(this.pMessage.deepCopy().withStyle(false, ChatFormatting.BLACK));
        this.disabledMessage = builder.disabledMessage;
        this.pushMap = builder.pushMap;
        this.clickedImage = builder.clickedImage.copy();
        this.disabledImage = builder.disabledImage.copy();
        this.normalImage = builder.normalImage.copy();
        this.hoveredImage = builder.hoveredImage.copy();
        this.setTooltip(builder.tooltip);
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

    public List<InvoImageCanvas> getAllBackgrounds(){
        return new ArrayList<>(List.of(this.clickedImage, this.disabledImage, this.normalImage, this.hoveredImage));
    }

    public InvoImageCanvas getBackgroundImage(){
        if (!this.active) return this.disabledImage;
        if (this.isClicked) return this.clickedImage;
        if (this.isHovered()) return this.hoveredImage;
        return this.normalImage;
    }

    public InvoText getText(){
        if (!this.active) return this.disabledMessage;
        if (this.isHovered()) return this.hoveredMessage;
        return this.pMessage;
    }

    @Override
    public void setZone(InvoZone zone) {
        super.setZone(zone);
        if (this.clickedImage == null) return;
//        LOGGER.warn("Start Button");
        for (InvoImageCanvas imageCanvas : this.getAllBackgrounds()){
//            LOGGER.info("Image 1: " + imageCanvas.getFullZone());
            imageCanvas.setFullZone(zone.copy());
//            LOGGER.info("Image 2: " + imageCanvas.getFullZone());
        }
//        LOGGER.error("End Button");
//        LOGGER.warn("background: " + this.getZoneCopy());
    }

    public void renderBackground(PoseStack stack){
        this.getBackgroundImage().render(stack);
//        LOGGER.error("What's my new zone? " + this.getBackgroundImage().getFullZone());
//        LOGGER.error("Also what's my text? " + this.getMessage().getString());
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        PoseStack stack = pGuiGraphics.pose();
        this.renderBackground(stack);
        this.getText().render(stack, this.getBackgroundImage().getMainZoneCopy());
    }

    public interface OnPush{
        boolean clicked(boolean isClick, InvoButton button, double pMouseX, double pMouseY);
    }

    public static class Builder {
        protected final Map<Integer, OnPush> pushMap = new HashMap<>();

        protected InvoText message;
        protected InvoText hoveredMessage;
        protected InvoText disabledMessage;
        protected InvoImageCanvas clickedImage;
        protected InvoImageCanvas disabledImage;
        protected InvoImageCanvas normalImage;
        protected InvoImageCanvas hoveredImage;
        protected Tooltip tooltip;

        public Builder() {
            this.message = InvoText.translate(XPShopLanguageprovider.defaultWidgetText);

            this.clickedImage = InvoTheme.getBackground();
            this.disabledImage = this.clickedImage.copy();
            this.normalImage = this.disabledImage.copy();
            this.hoveredImage = this.normalImage.copy();
        }

        public Builder setButton(Integer button, OnPush push){
            this.pushMap.put(button, push);
            return this.getBuilder();
        }

        public Builder setMessage(InvoText message) {
            this.message = message;
            return this.getBuilder();
        }

        public Builder setHoveredMessage(InvoText hoveredMessage){
            this.hoveredMessage = hoveredMessage;
            return this.getBuilder();
        }

        public Builder setDisabledMessage(InvoText disabledMessage){
            this.disabledMessage = disabledMessage;
            return this.getBuilder();
        }

        public Builder setClickedImage(InvoImageCanvas clickedImage) {
            this.clickedImage = clickedImage;
            return this.getBuilder();
        }

        public Builder setDisabledImage(InvoImageCanvas disabledImage) {
            this.disabledImage = disabledImage;
            return this.getBuilder();
        }

        public Builder setNormalImage(InvoImageCanvas normalImage) {
            this.normalImage = normalImage;
            return this.getBuilder();
        }

        public Builder setHoveredImage(InvoImageCanvas hoveredImage) {
            this.hoveredImage = hoveredImage;
            return this.getBuilder();
        }

        public Builder setTooltip(InvoText text){
            this.tooltip = Tooltip.create(text.getText());
            return this.getBuilder();
        }
        
        public Builder getBuilder(){
            return this;
        }

        public InvoButton build(InvoScreen screen, InvoZone widgetZone){
            return new InvoButton(screen, widgetZone, this);
        }

        public InvoButton build(InvoScreen screen){
            InvoZone textZone = new InvoZone(0,0,0,18);
            textZone.setWidth(ClientUtil.getFont().width(this.message.getText()));
            return this.build(screen, textZone);
        }
    }
}

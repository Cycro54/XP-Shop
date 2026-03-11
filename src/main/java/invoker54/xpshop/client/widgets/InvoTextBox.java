package invoker54.xpshop.client.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.invoimage.InvoImageColor;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.MathUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.InvoScreen;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Vector2f;

import java.awt.*;
import java.util.function.Consumer;

public class InvoTextBox extends InvoWidget{
    protected static final ModLogger LOGGER = ModLogger.getLogger(InvoTextBox.class, XPShop.debugMode);

    private final InvoText placeholder;
    private final Font font;
    private final InvoTextField textField;
    private int tick;
    public float yOffset;
    public float xOffset;
    public int barLength;
    public Long prevTick = 0L;
    public boolean xScrolling = false;
    public boolean yScrolling = false;

    public InvoTextBox(InvoScreen screen, InvoZone widgetZone, InvoText pMessage, InvoText placeholder, Font font, int barLength) {
        super(screen, startZone, pMessage);
        this.barLength = barLength;
        this.minZone = startZone.copy();
        this.maxZone = maxZone.copy();
        this.placeholder = placeholder;
        this.placeholder.getProperties().setTxtAlignment(TextUtil.TextAlign.TOP_LEFT).setMaxSplits(1).setTextSize(9).setPadding(2).setKeepFormatCodes(true);
//        this.viewer = this.placeholder.getTextViewer(true, this.getZoneCopy());
        this.font = font;
        this.textField = new InvoTextField("", this.placeholder.getProperties(), this.getActualZone(), ClientUtil.getFont());
//        this.textField.setCursorListener(this::scrollToCursor);
//        LOGGER.error("what's the padding:" + this.textField.getProperties().getPadding());
//        LOGGER.error("What's text field full zone: " + this.textField.getFullZoneCopy());
//        LOGGER.error("What's text field regular zone: " + this.textField.getZoneCopy());
//        LOGGER.error("What's my actual zone: " + this.getActualZone());
//        LOGGER.error("What's my regular zone: " + this.getZoneCopy());
    }

    public void setCharacterLimit(int limit) {
        this.textField.setCharacterLimit(limit);
    }

    public void setValueListener(Consumer<String> listener) {
        this.textField.setValueListener(listener);
    }

    public void setValue(String value) {
        this.textField.setValue(value);
    }

    public String getValue() {
        return this.textField.value();
    }

    public void tick() {
        if (ClientUtil.getMinecraft().level.getGameTime() == prevTick) return;
        prevTick = ClientUtil.getMinecraft().level.getGameTime();
        ++this.tick;
    }

    public void updateWidgetNarration(NarrationElementOutput p_259393_) {
        p_259393_.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
    }

    public void renderHScrollBar(PoseStack stack, int pMouseX, int pMouseY){
        if (this.getMaxYOffset() == 0) return;
//        LOGGER.debug("What's max Y Offset: " + this.getMaxYOffset());

        InvoZone scrollBackgroundZone = this.getHScrollBackgroundZone();

        //Scroll background
        InvoImage.fromColor(Color.DARK_GRAY).render(stack, scrollBackgroundZone);
        //Scroll block
        InvoZone blockZone = getHScrollBlockZone();

        Color blockColor = blockZone.inBounds(new Vector2f(pMouseX, pMouseY))
                || this.yScrolling ? Color.gray : Color.WHITE;
        InvoImage.fromColor(blockColor).render(stack, blockZone);
//        LOGGER.warn("Is this running");
    }

    public void renderWScrollBar(PoseStack stack, int pMouseX, int pMouseY){
        if (this.getMaxXOffset() == 0) return;
//        LOGGER.debug("What's max X Offset: " + this.getMaxXOffset());
//        LOGGER.error("My zone:" + this.getActualZone());
////        LOGGER.warn("text zone:" + this.textField.getZoneCopy());
//        LOGGER.warn("text zone:" + (this.textField.getFullZoneCopy().inflate(-this.placeholder.getProperties().getPadding()).height()
//                == this.getActualZone().inflate(-this.placeholder.getProperties().getPadding()).height()));
//        InvoImage.fromColor(Color.RED).render(stack, this.textField.getTextZones(0, this.textField.getMaxDisplayIndex()).get(0).minWidth(10));

        InvoZone scrollBackgroundZone = this.getWScrollBackgroundZone();

        //Scroll background
        InvoImage.fromColor(Color.DARK_GRAY).render(stack, scrollBackgroundZone);
        //Scroll block
        InvoZone blockZone = getWScrollBlockZone();

        Color blockColor = blockZone.inBounds(new Vector2f(pMouseX, pMouseY))
                || this.xScrolling ? Color.gray : Color.WHITE;
        InvoImage.fromColor(blockColor).render(stack, blockZone);
//        LOGGER.warn("Is this running");
    }

    public InvoZone getHScrollBackgroundZone(){
        return this.getActualZone()
                .setWidth(this.barLength).setRight(this.getZoneCopy().right());
    }

    public InvoZone getHScrollBlockZone(){
        InvoZone scrollBackgroundZone = this.getHScrollBackgroundZone();

        float offsetPercentage = this.yOffset / this.getMaxYOffset();
        float scrollBlockHeight = scrollBackgroundZone.height() * (this.getActualZone().height()/this.textField.getFullZoneCopy().height());
        float emptyHeight = (scrollBackgroundZone.height() - scrollBlockHeight);
        return scrollBackgroundZone.copy().setHeight(scrollBlockHeight).
                shiftXY(0, (emptyHeight * offsetPercentage)).inflate(-1);
    }

    public InvoZone getWScrollBackgroundZone() {
        return this.getActualZone()
                .setHeight(this.barLength).setDown(this.getZoneCopy().down());
    }

    public InvoZone getWScrollBlockZone(){
        InvoZone scrollBackgroundZone = this.getWScrollBackgroundZone();

        float offsetPercentage = this.xOffset / this.getMaxXOffset();
        float scrollBlockWidth = scrollBackgroundZone.width() * (this.getActualZone().width()/this.textField.getFullZoneCopy().width());
        float emptyWidth = (scrollBackgroundZone.width() - scrollBlockWidth);
        return scrollBackgroundZone.copy().setWidth(scrollBlockWidth).
                shiftXY(emptyWidth * offsetPercentage, 0).inflate(-1);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
//        LOGGER.warn("Whats delta:"+pDelta);
//        LOGGER.warn("Whats line height:"+this.textField.getLineHeight());
        this.setYOffset(this.yOffset + (this.textField.getLineHeight() * pDelta));
        return true;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        Vector2f mousePoint = new Vector2f((float) pMouseX, (float) pMouseY);
//        if (this.getFocused() == this) this.setFocused(null);

        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        } else if (this.getZoneCopy().inBounds(mousePoint) && pButton == 0) {
            this.setFocused(this);
            if (this.getHScrollBackgroundZone().inBounds(mousePoint)) this.yScrolling = true;
            if (this.getWScrollBackgroundZone().inBounds(mousePoint)) this.xScrolling = true;

            if (this.getActualZone().inBounds(mousePoint)) {
                this.textField.setSelecting(Screen.hasShiftDown());
                this.seekCursorScreen(pMouseX - this.xOffset, pMouseY - this.yOffset);
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.xScrolling = false;
        this.yScrolling = false;
        return this.isFocused();
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
//        LOGGER.error("This is in zone");
        Vector2f mousePoint = new Vector2f((float) pMouseX, (float) pMouseY);
        if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
//            LOGGER.error("this is true...");
            return true;
        } else if (this.getZoneCopy().inBounds(mousePoint) && pButton == 0) {

            if (!this.xScrolling && !this.yScrolling) {
                this.textField.setSelecting(true);
                this.seekCursorScreen(pMouseX, pMouseY);
                this.textField.setSelecting(Screen.hasShiftDown());
            }
            else {

            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
//        LOGGER.warn("Keycode! " + pKeyCode);
        //        if (!consumed) return false;
        //        this.setZone(this.getZoneCopy());
        boolean consumed = this.textField.keyPressed(pKeyCode);
        if (consumed) this.shiftByCursor();
        return consumed;
    }

    @Override
    public boolean charTyped(char letter, int p_239388_) {
//        LOGGER.warn("typing: " + letter);
        if (this.visible && this.isFocused() && SharedConstants.isAllowedChatCharacter(letter)) {
            String s = this.textField.value();
            this.textField.insertText(Character.toString(letter));
//            this.adjustOffsets();
//            if (this.textField.getFullZoneCopy().height() != this.getZoneCopy().height()) {
//                this.setZone(this.textField.getFullZoneCopy());
//            }
            this.shiftByCursor();

            return true;
        } else {
            return false;
        }
    }

    protected void shiftByCursor(){
        InvoZone cursorZone = this.textField.getTextZone(this.textField.cursor().displayIndex);

        float yDifference = cursorZone.copy().shiftXY(this.xOffset, this.yOffset).setBound(this.getActualZone()).y() - cursorZone.y();
        this.setYOffset(yDifference);

        float xDifference = cursorZone.copy().shiftXY(this.xOffset, this.yOffset).setBound(this.getActualZone()).x() - cursorZone.x();
        this.setXOffset(xDifference);
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        InvoZone zoneCopy = this.getZoneCopy();
//        pGuiGraphics.enableScissor((int) zoneCopy.x(), (int) zoneCopy.y() + 1, (int) zoneCopy.right(), (int) zoneCopy.down());
        tick();
        PoseStack stack = pGuiGraphics.pose();
        InvoImage.fromColor(Color.BLUE).render(stack, this.textField.getFullZoneCopy());
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        String s = this.getValue();
        if (s.isEmpty() && !this.isFocused()) {
            this.placeholder.render(stack, this.getActualZone(), false);
        } else {
            int cursorIndex = this.textField.cursor().displayIndex;
            boolean showCursor = this.isFocused() && this.tick / 6 % 2 == 0;
            boolean cursorInText = cursorIndex != this.textField.getMaxDisplayIndex();

            InvoZone currentZone = this.getActualZone();
            boolean canSee = currentZone.inBounds(this.getActualZone(),false);
            if (!canSee) return;
            this.textField.render(stack, this.textField.getPaddedZoneCopy().shiftXY(this.xOffset, this.yOffset));
//            this.textField.render(stack);

            if (showCursor){
                if (cursorInText){
                    InvoImage.fromColor(Color.YELLOW).render(stack, this.textField.getTextZone(this.textField.cursor().displayIndex)
                            .shiftXY(this.xOffset, this.yOffset).setWidth(1));
                }
                else {
                    InvoZone zone = this.textField.getTextZone(this.textField.getMaxDisplayIndex());
                    zone.shiftWH(zone.width(), 0);
                    this.placeholder.getProperties().copy().setPadding(0).text(InvoText.literal("_")).render(stack,
                            zone.shiftXY(this.xOffset, this.yOffset), true);
                }

            }

            if (this.textField.hasSelection()) {
                InvoTextField.StringView selectionView = this.textField.getSelected();
                this.textField.getTextZones(selectionView.startCursor().displayIndex, selectionView.endCursor().displayIndex)
                        .forEach(zone ->  {
                            zone.shiftXY(this.xOffset, this.yOffset).minWidth(3);
                            this.renderHighlight(pGuiGraphics,
                                    (int) zone.x(), (int) zone.y(), (int) zone.right(), (int) zone.down());
                        });
            }

        }
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
//        pGuiGraphics.disableScissor();
    }

    public void renderBackground(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        InvoImageColor.fromColor(new Color(0,0,0, 100)).render(graphics.pose(), this.getZoneCopy());
        renderHScrollBar(graphics.pose(), pMouseX, pMouseY);
        renderWScrollBar(graphics.pose(), pMouseX, pMouseY);
        if (this.textField.hasCharacterLimit()) {
            int i = this.textField.characterLimit();
            MutableComponent component = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), i);

            InvoZone errorZone = this.getZoneCopy().shiftWH(0, this.getZoneCopy().height() + 4);
            this.placeholder.getProperties().setTxtAlignment(TextUtil.TextAlign.MID_RIGHT)
                    .text(InvoText.component(component)).render(graphics.pose(), errorZone, false);
            graphics.drawString(this.font, component, this.getX() + this.width - this.font.width(component), this.getY() + this.height + 4, 10526880);
        }
//        InvoImageColor.fromColor(Color.green).render(stack, this.maxZone);
    }

    private void renderHighlight(GuiGraphics p_282092_, int x0, int y0, int x1, int y1) {
        p_282092_.fill(RenderType.guiTextHighlight(), x0, y0, x1, y1, -16776961);
    }

    private float getMaxYOffset(){
//        LOGGER.error("My height: " + this.getActualZone().height());
//        LOGGER.error("Text height: " + this.textField.getFullZoneCopy().height());

        return Math.min(this.getActualZone().height() - (this.textField.getFullZoneCopy().height()), 0);
    }

    private void setYOffset(double yOffset){
//        LOGGER.warn("Max offset: " + getMaxOffset());
        this.yOffset = (float) MathUtil.clamp(yOffset, this.getMaxYOffset(), 0);
//        LOGGER.warn("Current YOffset: " + this.yOffset);
    }

    private float getMaxXOffset(){
        return Math.min(this.getActualZone().width() - (this.textField.getFullZoneCopy().width()), 0);
    }

    private void setXOffset(double xOffset){
//        LOGGER.warn("Max offset: " + getMaxOffset());
        this.xOffset = (float) MathUtil.clamp(xOffset, this.getMaxXOffset(), 0);
//        LOGGER.warn("Current YOffset: " + this.yOffset);
    }

    private void seekCursorScreen(double pMouseX, double pMouseY) {
//        double d0 = pMouseX - (double)this.getX() - padding;
//        double d1 = pMouseY - (double)this.getY() - padding + this.yOffset;
        this.textField.seekCursor(Whence.ABSOLUTE, this.textField.getDisplayIndex((float) pMouseX, (float) pMouseY));
//        LOGGER.debug(this.textField.cursor().displayIndex + "");
    }


    public InvoZone getActualZone(){
        return this.getZoneCopy().shiftWH(-1f - (float) this.barLength, -1f - (float) this.barLength);
    }

    @Override
    public void setZone(InvoZone zone) {
        if (this.minZone == null){
//            LOGGER.warn("Null");
            super.setZone(zone);
            return;
        }

        InvoZone limitedZone = zone.copy().stretch(false);
        limitedZone.setWidth((float) MathUtil.clamp(zone.width(), minZone.width(), maxZone.width()));
        limitedZone.setHeight((float) MathUtil.clamp(zone.height(), minZone.height(), maxZone.height()));
//        LOGGER.warn("Limited zone: " + limitedZone);
//        limitedZone.center(zone);
        super.setZone(limitedZone.copy());
        this.textField.setZone(this.getActualZone());
//        LOGGER.warn("Text field x: " + this.textField.getZoneCopy().width());
//        LOGGER.warn("Text field full x: " + this.textField.getFullZoneCopy().width());
//        LOGGER.warn("My x: " + this.getActualZone().width());

//        LOGGER.error("Was it empty? " + this.textField.value().isEmpty());
//        this.textField.setZone(this.getZoneCopy());
//        this.viewer = this.placeholder.getProperties().text(this.getValue())
//                .getTextViewer(true, this.getZoneCopy().inflate(-padding));
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int a = 0; a < this.viewer.textLines().size(); a++) {
//            stringBuilder.append(this.viewer.textLines().get(a).getA().getString());
//            if (a + 1 != this.viewer.textLines().size()) stringBuilder.append("\n");
//        }
//
//        if (stringBuilder.toString().length() == this.textField.value().length()) return;
//        LOGGER.error("What's textfield size? " + this.textField.value().length());
//        LOGGER.error("What's viewer size? " + (this.viewer.maxIndex()));
//        LOGGER.error("Current index: " + this.textField.cursor());
//
//        int value = this.textField.cursor() + 1;
//        this.textField.setValue(stringBuilder.toString());
//        this.textField.seekCursor(Whence.ABSOLUTE, value);
    }
}
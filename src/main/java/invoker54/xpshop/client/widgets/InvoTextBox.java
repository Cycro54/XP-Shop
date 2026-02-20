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
import net.minecraft.client.gui.components.MultilineTextField;
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

    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int TEXT_COLOR = -2039584;
    private static final int PLACEHOLDER_TEXT_COLOR = -857677600;
    private final InvoText placeholder;
    private TextUtil.TextViewer viewer;
    private final Font font;
    private final MultilineTextField textField;
    protected final InvoZone minZone;
    protected final InvoZone maxZone;
    protected static final int padding = 2;
    private int tick;
    public float yOffset;
    public int barWidth;
    public float maxTextHeight;
    public Long prevTick = 0L;
    public boolean isScrolling = false;

    public InvoTextBox(InvoScreen screen, InvoZone startZone, InvoZone maxZone, InvoText pMessage, InvoText placeholder, Font font, int barWidth) {
        super(screen, startZone, pMessage);
        this.minZone = startZone.copy();
        this.maxZone = maxZone.copy();
        this.placeholder = placeholder;
        this.font = font;
        this.textField = new MultilineTextField(font, (int) (maxZone.copy().inflate(-2).shiftWH(-barWidth, 0).width()));
        this.textField.setCursorListener(this::scrollToCursor);

        this.placeholder.getProperties().setTxtAlignment(TextUtil.TextAlign.TOP_LEFT).setTextSize(9);
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

    public void renderScrollBar(PoseStack stack, int pMouseX, int pMouseY){
        if (this.maxTextHeight < this.getHeight()) return;

        InvoZone scrollBackgroundZone = this.getScrollBackgroundZone();
        //Scroll background
        InvoImage.fromColor(Color.black).render(stack, scrollBackgroundZone);
        //Scroll block
        InvoZone blockZone = getScrollBlockZone();

        Color blockColor = blockZone.inBounds(new Vector2f(pMouseX, pMouseY))
                || this.isScrolling ? Color.gray : Color.WHITE;
        InvoImage.fromColor(blockColor).render(stack, blockZone);
//        LOGGER.warn("Is this running");
    }

    public InvoZone getScrollBackgroundZone(){
        return this.getZoneCopy().setWidth(this.barWidth).setRight(this.getZoneCopy().right());
    }

    public InvoZone getScrollBlockZone(){
        InvoZone scrollBackgroundZone = this.getScrollBackgroundZone();
        float maxOffset = this.maxTextHeight - this.getZoneCopy().height();
        float offsetPercentage = this.yOffset / maxOffset;
        float scrollBlockHeight = scrollBackgroundZone.height() * (this.getHeight()/this.maxTextHeight);
        float emptyHeight = (scrollBackgroundZone.height() - scrollBlockHeight);
        return scrollBackgroundZone.copy().setHeight(scrollBlockHeight).
                shiftXY(0, - (emptyHeight * offsetPercentage)).inflate(-1,-1);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        Vector2f mousePoint = new Vector2f((float) pMouseX, (float) pMouseY);
//        if (this.getFocused() == this) this.setFocused(null);

        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        } else if (this.getZoneCopy().inflate(-padding).inBounds(mousePoint) && pButton == 0) {
            this.setFocused(this);
            this.textField.setSelecting(Screen.hasShiftDown());
            this.seekCursorScreen(pMouseX, pMouseY);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        Vector2f mousePoint = new Vector2f((float) pMouseX, (float) pMouseY);
        if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
            return true;
        } else if (this.getZoneCopy().inflate(-padding).inBounds(mousePoint) && pButton == 0) {
            this.textField.setSelecting(true);
            this.seekCursorScreen(pMouseX, pMouseY);
            this.textField.setSelecting(Screen.hasShiftDown());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        LOGGER.warn("Keycode! " + pKeyCode);
        boolean consumed = this.textField.keyPressed(pKeyCode);
        if (!consumed) return false;
        this.setZone(this.getZoneCopy());
        return true;
    }

    @Override
    public boolean charTyped(char letter, int p_239388_) {
        LOGGER.warn("typing: " + letter);
        if (this.visible && this.isFocused() && SharedConstants.isAllowedChatCharacter(letter)) {
            this.textField.insertText(Character.toString(letter));

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        tick();
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        PoseStack stack = pGuiGraphics.pose();

        String s = this.textField.value();
        InvoZone paddedZone = this.getZoneCopy().inflate(-padding);
        if (s.isEmpty() && !this.isFocused()) {
            InvoImage.fromColor(Color.RED).render(stack, paddedZone);
            this.placeholder.render(pGuiGraphics.pose(), paddedZone);
            LOGGER.debug("what's padded zone: " + paddedZone);
//            pGuiGraphics.pose().translate(80, 40, 0);
//            pGuiGraphics.drawWordWrap(ClientUtil.getFont(), Component.literal("blah blah blah"), (int)paddedZone.x(), (int)paddedZone.y() + 40, 200,  new Color(61, 135, 135,233).getRGB());
//            pGuiGraphics.drawString(ClientUtil.getFont(), "blah blah blah", 20, 20,  new Color(61, 135, 135,233).getRGB());
        } else {
            int currIndex = this.textField.cursor();
            boolean showCursor = this.isFocused() && this.tick / 6 % 2 == 0;
            boolean cursorInText = currIndex < s.length();
            int currentY = this.getY() + padding;

            InvoZone currentZone = this.getZoneCopy();
            for(MultilineTextField.StringView multilinetextfield$stringview : this.textField.iterateLines()) {
                boolean canSee = currentZone.inBounds(this.getZoneCopy(),false);
                if (showCursor && cursorInText && currIndex >= multilinetextfield$stringview.beginIndex() && currIndex <= multilinetextfield$stringview.endIndex()) {
                    if (canSee) {
//                        j = pGuiGraphics.drawString(this.font, s.substring(multilinetextfield$stringview.beginIndex(), currIndex), this.getX() + padding, currentY, -2039584) - 1;
//                        pGuiGraphics.fill(j, currentY - 1, j + 1, currentY + 1 + 9, -3092272);
//                        new Color(-3092272);
                        String s1 = s.substring(multilinetextfield$stringview.beginIndex(), multilinetextfield$stringview.endIndex());
                        this.placeholder.getProperties().text(InvoText.literal(s1))
                                .render(pGuiGraphics.pose(), this.getZoneCopy().setHeight(9).shiftXY(0, currentY));
                        this.viewer.getTextZones(currIndex,currIndex).forEach(zone ->
                                InvoImage.fromColor(new Color(-3092272)).render(pGuiGraphics.pose(), zone.setWidth(80)));
//                        pGuiGraphics.drawString(this.font, s.substring(currIndex, multilinetextfield$stringview.endIndex()), j, currentY, -2039584);
                    }
                } else {
                    if (canSee) {
//                        LOGGER.error("Is not end");
//                        LOGGER.error("rendering");
                        String s1 = s.substring(multilinetextfield$stringview.beginIndex(), multilinetextfield$stringview.endIndex());
//                        InvoImage.fromColor(Color.RED).render(pGuiGraphics.pose(), this.getZoneCopy().setHeight(9).shiftXY(0, currentY));
                        this.placeholder.getProperties().text(InvoText.literal(s1)).render(pGuiGraphics.pose(), this.getZoneCopy().setHeight(9).shiftXY(0, currentY));
//                        j = pGuiGraphics.drawString(this.font, "d", this.getX() + padding, currentY + this.getY(), -2039584) - 1;
                    }

//                    k = currentY;
                }

                currentZone.shiftXY(0,9);
            }

            if (showCursor && !cursorInText && this.getZoneCopy().setY(9)
                    .inBounds(this.getZoneCopy(), false)) {
//                LOGGER.warn("Text thingd");
                int maxIndex = this.textField.value().length();
                InvoZone zone = this.viewer.getTextZones(maxIndex, maxIndex).get(0);
                zone.shiftWH(zone.width(), 0);
                this.placeholder.getProperties().text(InvoText.literal("_")).render(pGuiGraphics.pose(),
                        zone);
//                pGuiGraphics.drawString(this.font, "_", j, k, -3092272);
            }

            if (this.textField.hasSelection()) {
                MultilineTextField.StringView multilinetextfield$stringview2 = this.textField.getSelected();
                int k1 = this.getX() + padding;
                currentY = this.getY() + padding;

                currentZone = this.getZoneCopy().setY(currentY);
                for(MultilineTextField.StringView multilinetextfield$stringview1 : this.textField.iterateLines()) {
                    if (multilinetextfield$stringview2.beginIndex() > multilinetextfield$stringview1.endIndex()) {
                        currentY += 9;
                    } else {
                        if (multilinetextfield$stringview1.beginIndex() > multilinetextfield$stringview2.endIndex()) {
                            break;
                        }

                        if (currentZone.inBounds(this.getZoneCopy(), false)) {
                            int i1 = this.font.width(s.substring(multilinetextfield$stringview1.beginIndex(), Math.max(multilinetextfield$stringview2.beginIndex(), multilinetextfield$stringview1.beginIndex())));
                            int j1;
                            if (multilinetextfield$stringview2.endIndex() > multilinetextfield$stringview1.endIndex()) {
                                j1 = this.width - padding;
                            } else {
                                j1 = this.font.width(s.substring(multilinetextfield$stringview1.beginIndex(), multilinetextfield$stringview2.endIndex()));
                            }

                            this.renderHighlight(pGuiGraphics, k1 + i1, currentY, k1 + j1, currentY + 9);
                        }

                        currentZone.shiftXY(0,9);
                    }
                }
            }

        }
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

//    @Override
//    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
//        tick();
//        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
//
//        String s = this.textField.value();
//        InvoZone paddedZone = this.getZoneCopy().inflate(-padding);
//        if (s.isEmpty() && !this.isFocused()) {
//            this.placeholder.render(pGuiGraphics.pose(), paddedZone);
////            LOGGER.debug("what's padded zone: " + paddedZone);
////            pGuiGraphics.pose().translate(80, 40, 0);
////            pGuiGraphics.drawWordWrap(ClientUtil.getFont(), Component.literal("blah blah blah"), (int)paddedZone.x(), (int)paddedZone.y() + 40, 200,  new Color(61, 135, 135,233).getRGB());
////            pGuiGraphics.drawString(ClientUtil.getFont(), "blah blah blah", 20, 20,  new Color(61, 135, 135,233).getRGB());
//        } else {
//            int i = this.textField.cursor();
//            boolean flag = this.isFocused() && this.tick / 6 % 2 == 0;
//            boolean flag1 = i < s.length();
//            int j = 0;
//            int k = 0;
//            int l = this.getY() + padding;
//
//            InvoZone currentZone = this.getZoneCopy();
//            for(MultilineTextField.StringView multilinetextfield$stringview : this.textField.iterateLines()) {
//                boolean flag2 = currentZone.inBounds(this.getZoneCopy(),false);
//                if (flag && flag1 && i >= multilinetextfield$stringview.beginIndex() && i <= multilinetextfield$stringview.endIndex()) {
//                    if (flag2) {
//
//                        j = pGuiGraphics.drawString(this.font, s.substring(multilinetextfield$stringview.beginIndex(), i), this.getX() + padding, l, -2039584) - 1;
//                        pGuiGraphics.fill(j, l - 1, j + 1, l + 1 + 9, -3092272);
//                        new Color(-3092272);
//                        this.viewer.getTextZones().forEach(zone -> InvoImage.fromColor(new Color(-3092272)).render(pGuiGraphics.pose(), zone.setWidth(1)));
//                        pGuiGraphics.drawString(this.font, s.substring(i, multilinetextfield$stringview.endIndex()), j, l, -2039584);
//                    }
//                } else {
//                    if (flag2) {
////                        LOGGER.error("rendering");
//                        String s1 = s.substring(multilinetextfield$stringview.beginIndex(), multilinetextfield$stringview.endIndex());
//                        InvoImage.fromColor(Color.RED).render(pGuiGraphics.pose(), this.getZoneCopy().setHeight(9).shiftXY(0, l));
//                        this.placeholder.getProperties().text(InvoText.literal(s1)).render(pGuiGraphics.pose(), this.getZoneCopy().setHeight(9).shiftXY(0, l));
////                        j = pGuiGraphics.drawString(this.font, "d", this.getX() + padding, l + this.getY(), -2039584) - 1;
//                    }
//
//                    k = l;
//                }
//
//                currentZone.shiftXY(0,9);
//            }
//
//            if (flag && !flag1 && this.getZoneCopy().shiftXY(0, 9 * k)
//                    .inBounds(this.getZoneCopy(), false)) {
//                pGuiGraphics.drawString(this.font, "_", j, k, -3092272);
//            }
//
//            if (this.textField.hasSelection()) {
//                MultilineTextField.StringView multilinetextfield$stringview2 = this.textField.getSelected();
//                int k1 = this.getX() + padding;
//                l = this.getY() + padding;
//
//                currentZone = this.getZoneCopy().setY(l);
//                for(MultilineTextField.StringView multilinetextfield$stringview1 : this.textField.iterateLines()) {
//                    if (multilinetextfield$stringview2.beginIndex() > multilinetextfield$stringview1.endIndex()) {
//                        l += 9;
//                    } else {
//                        if (multilinetextfield$stringview1.beginIndex() > multilinetextfield$stringview2.endIndex()) {
//                            break;
//                        }
//
//                        if (currentZone.inBounds(this.getZoneCopy(), false)) {
//                            int i1 = this.font.width(s.substring(multilinetextfield$stringview1.beginIndex(), Math.max(multilinetextfield$stringview2.beginIndex(), multilinetextfield$stringview1.beginIndex())));
//                            int j1;
//                            if (multilinetextfield$stringview2.endIndex() > multilinetextfield$stringview1.endIndex()) {
//                                j1 = this.width - padding;
//                            } else {
//                                j1 = this.font.width(s.substring(multilinetextfield$stringview1.beginIndex(), multilinetextfield$stringview2.endIndex()));
//                            }
//
//                            this.renderHighlight(pGuiGraphics, k1 + i1, l, k1 + j1, l + 9);
//                        }
//
//                        currentZone.shiftXY(0,9);
//                    }
//                }
//            }
//
//        }
//        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
//    }

    public void renderBackground(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick){
        renderScrollBar(graphics.pose(), pMouseX, pMouseY);
        if (this.textField.hasCharacterLimit()) {
            int i = this.textField.characterLimit();
            MutableComponent component = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), i);

            InvoZone errorZone = this.getZoneCopy().shiftWH(0,this.getZoneCopy().height() + 4);
            this.placeholder.getProperties().setTxtAlignment(TextUtil.TextAlign.MID_RIGHT)
                    .text(InvoText.component(component)).render(graphics.pose(), errorZone);
            graphics.drawString(this.font, component, this.getX() + this.width - this.font.width(component), this.getY() + this.height + 4, 10526880);
        }
//        InvoImageColor.fromColor(Color.green).render(stack, this.maxZone);
        InvoImageColor.fromColor(Color.BLACK).render(graphics.pose(), this.getZoneCopy());
    }

    private void renderHighlight(GuiGraphics p_282092_, int p_282814_, int p_282908_, int p_281451_, int p_281765_) {
        p_282092_.fill(RenderType.guiTextHighlight(), p_282814_, p_282908_, p_281451_, p_281765_, -16776961);
    }

    private void scrollToCursor() {
        double d0 = this.yOffset;
        MultilineTextField.StringView multilinetextfield$stringview = this.textField.getLineView((int)(d0 / 9.0D));
        if (this.textField.cursor() <= multilinetextfield$stringview.beginIndex()) {
            d0 = (double)(this.textField.getLineAtCursor() * 9);
        } else {
            MultilineTextField.StringView multilinetextfield$stringview1 = this.textField.getLineView((int)((d0 + (double)this.height) / 9.0D) - 1);
            if (this.textField.cursor() > multilinetextfield$stringview1.endIndex()) {
                d0 = (double)(this.textField.getLineAtCursor() * 9 - this.height + 9 + (padding * 2));
            }
        }

        this.setyOffset(d0);
    }

    private void setyOffset(double yOffset){
        this.yOffset = (float) MathUtil.clamp(yOffset,
                this.getZoneCopy().height() - this.maxTextHeight, 0);
    }

    private void seekCursorScreen(double pMouseX, double pMouseY) {
        double d0 = pMouseX - (double)this.getX() - padding;
        double d1 = pMouseY - (double)this.getY() - padding + this.yOffset;
        this.textField.seekCursorToPoint(d0, d1);
        LOGGER.debug(this.textField.cursor() + "");
    }



    @Override
    public void setZone(InvoZone zone) {
        if (this.minZone == null){
            super.setZone(zone);
            return;
        }

        InvoZone limitedZone = zone.copy();
        limitedZone.setWidth((float) MathUtil.clamp(zone.width(), minZone.width(), maxZone.width()));
        limitedZone.setHeight((float) MathUtil.clamp(zone.height(), minZone.height(), maxZone.height()));
        limitedZone.center(zone);
        super.setZone(limitedZone);

        LOGGER.error("Was it empty? " + this.textField.value().isEmpty());
        this.viewer = this.placeholder.getProperties()
                .text(InvoText.literal(this.textField.value().isEmpty() ? "_" : this.textField.value()))
                .getTextViewer(true, this.getZoneCopy().inflate(-padding));

    }
}
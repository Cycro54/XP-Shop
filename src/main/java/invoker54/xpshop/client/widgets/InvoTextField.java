package invoker54.xpshop.client.widgets;

import com.google.common.annotations.VisibleForTesting;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextViewer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

public class InvoTextField extends TextViewer {
    public static final int NO_CHARACTER_LIMIT = Integer.MAX_VALUE;
    private static final int LINE_SEEK_PIXEL_BIAS = 2;
    private final Font font;
//    private final List<InvoTextField.StringView> displayLines = Lists.newArrayList();
//    private String value;
//    private InvoText.Properties properties;
    private final IndexCursor cursor;
    private final IndexCursor selectCursor;
    private boolean selecting;
    private int characterLimit = Integer.MAX_VALUE;
//    private final int width;
    private Consumer<String> valueListener = (p_239235_) -> {
    };
    private Runnable cursorListener = () -> {
    };

    public InvoTextField(String originalText, InvoText.Properties textProperties, InvoZone textZone, Font font) {
        super(originalText, textProperties, textZone);
        this.font = font;
        this.cursor = new IndexCursor(this, 0,0);
        this.selectCursor = this.cursor.copy();
    }

    public int characterLimit() {
        return this.characterLimit;
    }

    public void setCharacterLimit(int pCharacterLimit) {
        if (pCharacterLimit < 0) {
            throw new IllegalArgumentException("Character limit cannot be negative");
        } else {
            this.characterLimit = pCharacterLimit;
        }
    }

    public boolean hasCharacterLimit() {
        return this.characterLimit != Integer.MAX_VALUE;
    }

    public void setValueListener(Consumer<String> pValueListener) {
        this.valueListener = pValueListener;
    }

    public void setCursorListener(Runnable pCursorListener) {
        this.cursorListener = pCursorListener;
    }

    public void setValue(String pFullText) {
//        LOGGER.debug("What's current index: " + this.cursor);
        int oldCount = this.getMaxStringIndex();
//        LOGGER.warn("Old count: " + oldCount);
        this.updateText(pFullText);
//        LOGGER.warn("new count: " + this.getMaxDisplayIndex());
        int difference = (this.getMaxStringIndex() - oldCount);
        if (this.cursor.stringIndex < this.selectCursor.stringIndex) difference = 0;

        this.cursor.shiftByString(difference);
        this.selectCursor.copy(this.cursor);

//        LOGGER.warn("New cursor index:"+this.cursor);
//        LOGGER.warn("This zone: " + this.getZoneCopy());
//        LOGGER.warn("This text:" + this.originalText);
//        LOGGER.warn("HOw long is string: " + this.originalText.length());
//        for (var line : this.displayLines) {
//            LOGGER.warn("Length: " + line.getString().length());
//        }
//        LOGGER.warn("How many lines: " + this.displayLines.size());
//        this.onValueChange();
    }

    public String value() {
        return this.getOriginalText();
    }

    public void insertText(String pText) {
        if (!pText.isEmpty() || this.hasSelection()) {
            String $$1 = this.truncateInsertionText(SharedConstants.filterText(pText, true));
            StringView selected = this.getSelected();

//            LOGGER.error("Original text:"+this.getOriginalText());
//            LOGGER.error("Original text index:"+this.getMaxOriginalIndex());
//            LOGGER.error("Display lines:"+this.getDisplayLineCount());
//            LOGGER.error("Display count:"+this.getMaxDisplayIndex());
//            LOGGER.error("s2 begin:"+$$2.beginDisplayIndex);
//            LOGGER.error("s2 end:"+$$2.endDisplayIndex);
//            LOGGER.error("Display count: " + this.displayLines.size());
//            LOGGER.error("Char width count: " + this.charWidthList.size());

            String oldText = this.getOriginalText();
//            int stringBeginIndex = this.getStringIndex($$2.beginDisplayIndex());
//            int stringEndIndex = this.getStringIndex($$2.endDisplayIndex());
            oldText = (new StringBuilder(oldText)).replace(selected.startCursor.stringIndex,
                    selected.endCursor.stringIndex, $$1).toString();
//            LOGGER.error("New Text: " + oldText);
            this.setValue(oldText);
//            this.cursor = $$2.beginDisplayIndex + $$1.length();
//            this.selectCursor = this.cursor;
//            this.onValueChange();
        }
    }

    public void deleteText(int pLength) {
        if (!this.hasSelection()) {
            this.selectCursor.shiftByString(pLength);
//            LOGGER.warn("Current: " + this.cursor);
//            LOGGER.warn("length: " + pLength);
//            LOGGER.warn("cursor: " + this.selectCursor);
        }

        this.insertText("");
    }

    public IndexCursor cursor() {
        return this.cursor;
    }

    public void setSelecting(boolean pSelecting) {
        this.selecting = pSelecting;
    }

    public StringView getSelected() {
        return new StringView(this.selectCursor, this.cursor);
    }

    public int getLineCount() {
        return this.getDisplayLineCount();
    }

    public StringView getDisplayLine(int rowIndex){
        //abcds
        //abcdes
        //
        int endIndex = -1;
        for (int a = 0; a < rowIndex+1; a++) {
            endIndex += this.charWidthList.get(a).size();
        }
        int startIndex = endIndex - (this.charWidthList.get(rowIndex).size() - 1);

        return new StringView(IndexCursor.byDisplayIndex(startIndex, this),
                IndexCursor.byDisplayIndex(endIndex, this));
    }

    public int getLineAtCursor() {
        for (int a = 0; a < this.getLineCount(); a++) {
            StringView view = this.getDisplayLine(a);
            if (a > view.endCursor.displayIndex) continue;
            return a;
        }
        return this.getLineCount()-1;
    }

    public void seekCursor(Whence pWhence, int pPosition) {
        switch (pWhence) {
            case ABSOLUTE -> this.cursor.setDisplayIndex(pPosition);
            case RELATIVE -> this.cursor.shiftByDisplay(pPosition);
            case END -> this.cursor.setDisplayIndex(this.maxDisplayIndex);
        }

//        this.cursor = Mth.clamp(this.cursor, 0, this.maxDisplayIndex);
        this.cursorListener.run();
        if (!this.selecting) {
            this.selectCursor.copy(this.cursor);
        }
    }

//    public void seekCursorLine(int pOffset) {
//        if (pOffset != 0) {
//            int $$1 = this.font.width(this.value.substring(this.getCursorLineView().beginDisplayIndex, this.cursor)) + 2;
//            stringDisplayView $$2 = this.getCursorLineView(pOffset);
//            int $$3 = this.font.plainSubstrByWidth(this.value.substring($$2.beginDisplayIndex, $$2.endStringindex), $$1).length();
//            this.seekCursor(Whence.ABSOLUTE, $$2.beginDisplayIndex + $$3);
//        }
//    }

//    public void seekCursorToPoint(double pX, double pY) {
//
//    }

    public boolean keyPressed(int pKeyCode) {
        this.selecting = Screen.hasShiftDown();
        if (Screen.isSelectAll(pKeyCode)) {
            this.cursor.setDisplayIndex(this.maxDisplayIndex);
            this.selectCursor.setDisplayIndex(0);
            return true;
        } else if (Screen.isCopy(pKeyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
            return true;
        } else if (Screen.isPaste(pKeyCode)) {
            this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        } else if (Screen.isCut(pKeyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
            this.insertText("");
            return true;
        } else {
            switch (pKeyCode) {
                case 257:
                case 335:
                    this.insertText("\n");
                    return true;
                case 259:
                    if (Screen.hasControlDown()) {
                        StringView $$3 = this.getPreviousWord();
                        this.deleteText($$3.startCursor.displayIndex - this.cursor.displayIndex);
                    } else {
                        this.deleteText(-1);
                    }

                    return true;
                case 261:
                    if (Screen.hasControlDown()) {
                        StringView $$4 = this.getNextWord();
                        this.deleteText($$4.startCursor.displayIndex - this.cursor.displayIndex);
                    } else {
                        this.deleteText(1);
                    }

                    return true;
                case 262:
                    if (Screen.hasControlDown()) {
                        StringView $$2 = this.getNextWord();
                        this.seekCursor(Whence.ABSOLUTE, $$2.startCursor.displayIndex);
                    } else {
                        this.seekCursor(Whence.RELATIVE, 1);
                    }

                    return true;
                case 263:
                    if (Screen.hasControlDown()) {
                        StringView $$1 = this.getPreviousWord();
                        this.seekCursor(Whence.ABSOLUTE, $$1.startCursor.displayIndex);
                    } else {
                        this.seekCursor(Whence.RELATIVE, -1);
                    }

                    return true;
                case 264:
                    if (!Screen.hasControlDown()) {
                        InvoZone zone = this.getTextZone(this.cursor.displayIndex).gridShift(0,1);
                        this.seekCursor(Whence.ABSOLUTE, this.getDisplayIndex(zone.middleX(), zone.middleY()));
                    }

                    return true;
                case 265:
                    if (!Screen.hasControlDown()) {
                        InvoZone zone = this.getTextZone(this.cursor.displayIndex).gridShift(0,-1);
                        this.seekCursor(Whence.ABSOLUTE, this.getDisplayIndex(zone.middleX(), zone.middleY()));
                    }

                    return true;
                case 266:
                    this.seekCursor(Whence.ABSOLUTE, 0);
                    return true;
                case 267:
                    this.seekCursor(Whence.END, 0);
                    return true;
                case 268:
                    if (Screen.hasControlDown()) {
                        this.seekCursor(Whence.ABSOLUTE, 0);
                    } else {
                        this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().startCursor.displayIndex);
                    }

                    return true;
                case 269:
                    if (Screen.hasControlDown()) {
                        this.seekCursor(Whence.END, 0);
                    } else {
                        this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().startCursor.displayIndex);
                    }

                    return true;
                default:
                    return false;
            }
        }
    }

//    public Iterable<stringDisplayView> iterateLines() {
//        return this.displayLines;
//    }

    public boolean hasSelection() {
        return this.selectCursor.stringIndex != this.cursor.stringIndex;
    }

//    @VisibleForTesting
    public String getSelectedText() {
        StringView $$0 = this.getSelected();
        return this.getOriginalText().substring($$0.startCursor.stringIndex, $$0.endCursor.stringIndex);
    }

    private StringView getCursorLineView() {
        return this.getDisplayLine(this.getLineAtCursor());
    }

//    private stringDisplayView getCursorLineView(int pOffset) {
//        int $$1 = this.getLineAtCursor();
//        if ($$1 < 0) {
//            int var10002 = this.cursor;
//            throw new IllegalStateException("Cursor is not within text (cursor = " + var10002 + ", length = " + this.value.length() + ")");
//        } else {
//            return (stringDisplayView)this.displayLines.get(Mth.clamp($$1 + pOffset, 0, this.getLineCount() - 1));
//        }
//    }

    @VisibleForTesting
    public StringView getPreviousWord() {
        if (this.getMaxDisplayIndex() == 0) {
            return StringView.EMPTY;
        } else {
            int $$0;
            for($$0 = Mth.clamp(this.cursor.displayIndex, 0, this.maxDisplayIndex - 1); $$0 > 0 && Character.isWhitespace(this.originalText.charAt(this.getStringIndex($$0 - 1))); --$$0) {
            }

            while($$0 > 0 && !Character.isWhitespace(this.originalText.charAt(this.getStringIndex($$0 - 1)))) {
                --$$0;
            }

            return new StringView(this.cursor.copy().setDisplayIndex($$0), this.cursor.copy().setDisplayIndex(this.getWordEndPosition($$0)));
        }
    }

    @VisibleForTesting
    public StringView getNextWord() {
        if (this.getMaxDisplayIndex() == 0) {
            return StringView.EMPTY;
        } else {
            int $$0;
            for($$0 = Mth.clamp(this.cursor.displayIndex, 0, this.maxDisplayIndex - 1); $$0 < this.maxDisplayIndex && !Character.isWhitespace(this.originalText.charAt(this.getStringIndex($$0))); ++$$0) {
            }

            while($$0 < this.maxDisplayIndex && Character.isWhitespace(this.originalText.charAt(this.getStringIndex($$0)))) {
                ++$$0;
            }

            return new StringView(this.cursor.copy().setDisplayIndex($$0), this.cursor.copy().setDisplayIndex(this.getWordEndPosition($$0)));
        }
    }

    private int getWordEndPosition(int pCursor) {
        int $$1;
        for($$1 = pCursor; $$1 < this.maxDisplayIndex && !Character.isWhitespace(this.originalText.charAt(this.getStringIndex($$1))); ++$$1) {
        }

        return $$1;
    }

//    private void onValueChange() {
//        this.reflowDisplayLines();
//        this.valueListener.accept(this.value);
//        this.cursorListener.run();
//    }

//    private void reflowDisplayLines() {
//        this.displayLines.clear();
//        if (this.value.isEmpty()) {
//            this.displayLines.add(stringDisplayView.EMPTY);
//        } else {
//            this.font.getSplitter().splitLines(this.value, this.getZoneCopy().width(), Style.EMPTY, false, (p_239846_, p_239847_, p_239848_) -> this.displayLines.add(new stringDisplayView(p_239847_, p_239848_)));
//            if (this.value.charAt(this.value.length() - 1) == '\n') {
//                this.displayLines.add(new stringDisplayView(this.value.length(), this.value.length()));
//            }
//
//        }
//    }

    private String truncateFullText(String pFullText) {
        return this.hasCharacterLimit() ? StringUtil.truncateStringIfNecessary(pFullText, this.characterLimit, false) : pFullText;
    }

    private String truncateInsertionText(String pText) {
        if (this.hasCharacterLimit()) {
            int $$1 = this.characterLimit - this.getOriginalText().length();
            return StringUtil.truncateStringIfNecessary(pText, $$1, false);
        } else {
            return pText;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record StringView(IndexCursor startCursor, IndexCursor endCursor)
    {
        public static StringView EMPTY = new StringView(new IndexCursor(null, 0,0),
                new IndexCursor(null, 0,0));
        public StringView(IndexCursor startCursor, IndexCursor endCursor){
            this.startCursor = startCursor.stringIndex < endCursor.stringIndex ? startCursor : endCursor;
            this.endCursor = startCursor.stringIndex > endCursor.stringIndex ? startCursor : endCursor;
        }
    }
}

package invoker54.xpshop.client.screens.editscreens;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.invoimage.InvoImageColor;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.InvoList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.Consumer;

public class PropertyScreen extends InvoScreen {
    protected static final ModLogger LOGGER = ModLogger.getLogger(PropertyScreen.class, XPShop.debugMode);

    protected InvoList propertyList;
    protected final Consumer<PropertyScreen> consumer;

    public PropertyScreen(InvoText pTitle, Consumer<PropertyScreen> consumer) {
        super(pTitle);
        this.consumer = consumer;
    }

    @Override
    protected void init() {
        super.init();
        this.setZone(this.getOriginalZone().splitHeight(4,3).splitWidth(3,1).center(this.getOriginalZone()));

        InvoZone maxListZone = this.getOriginalZone().splitHeight(5,4).splitWidth(3,2).center(this.getOriginalZone());
        InvoZone startListZone = maxListZone.copy().splitHeight(2,1).splitWidth(2,1).center(this.getOriginalZone());
        InvoZone entryZone = this.getZoneCopy().splitHeight(8,1).center(this.getOriginalZone());

        if (this.propertyList == null){
            this.propertyList = new InvoList(this, startListZone, maxListZone, entryZone,6);
            consumer.accept(this);
        }
        this.propertyList.maxWidgetZone.copy(maxListZone);
        this.propertyList.setZone(this.propertyList.getZoneCopy().center(this.getZoneCopy()));
//        this.propertyList.setZone(this.getZoneCopy());
        this.addRenderableWidget(this.propertyList);

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics) {
        InvoImageColor.fromColor(new Color(0, 0, 0, 163)).render(pGuiGraphics.pose(),
                this.getOriginalZone());

        InvoZone titleZone = this.getOriginalZone().splitWidth(3,1).
                setHeight((this.getOriginalZone().height() - this.propertyList.maxWidgetZone.height())/2).
                setY(10).centerX(this.getOriginalZone().middleX());
        InvoImage.fromColor(new Color(0,0,0,180)).render(pGuiGraphics.pose(), titleZone);
        InvoText.Properties properties = new InvoText.Properties();
        properties.deserializeNBT(this.getMessage().getProperties().serializeNBT());
        properties.setShadow(false).setPadding(2).setMaxSplits(2).setMinTextSize(1).setTxtAlignment(TextUtil.TextAlign.MID);
        properties.text(this.getMessage()).render(pGuiGraphics.pose(), titleZone);
        //Renders the background
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
//        this.getPropertyList().render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        //This will render after
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> @NotNull T addRenderableWidget(@NotNull T pWidget) {
        return super.addRenderableWidget(pWidget);
    }

    public InvoList getPropertyList(){
        return this.propertyList;
    }
}

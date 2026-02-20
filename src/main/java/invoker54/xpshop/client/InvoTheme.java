package invoker54.xpshop.client;

import invoker54.invocore.client.invoimage.ImageOperation;
import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.invoimage.InvoImageCanvas;
import invoker54.invocore.client.invoimage.InvoImageTexture;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;

import java.util.Collections;

public class InvoTheme {
    private static InvoImageTexture background;

    private static InvoImageTexture editIcon;

    private static InvoImageTexture duplicateIcon;

    private static InvoImageTexture textIcon;

    private static InvoImageTexture addIcon;

    private static InvoImageTexture removeIcon;

    public static void init() {
        background = InvoImage.fromTexture(
                ResourceUtil.create(XPShop.MOD_ID, "gui/section_background"));
        background.setOuterOperation(ImageOperation.TILE);
        background.setSubImageZone(background.getImageZoneCopy().inflate(-4, -4));


        editIcon = InvoImage.fromTexture(
                ResourceUtil.create(XPShop.MOD_ID, "gui/cog_wheel"));

        duplicateIcon = InvoImage.fromTexture(
                ResourceUtil.create(XPShop.MOD_ID, "gui/duplicate"));

        textIcon = InvoImage.fromTexture(
                ResourceUtil.create(XPShop.MOD_ID, "gui/edit_text"));

        addIcon = InvoImage.fromTexture(
                ResourceUtil.create(XPShop.MOD_ID, "gui/add"));

        removeIcon = InvoImage.fromTexture(
                ResourceUtil.create(XPShop.MOD_ID, "gui/trash_can"));
    }

    public static InvoImageCanvas getBackground() {
        return background.canvas(background.getSubRenderZone(background.getAdjustedImageZone()));
    }

    public static InvoImage getEditIcon() {
        return editIcon.copy();
    }

    public static InvoImage getDuplicateIcon() {
        return duplicateIcon.copy();
    }

    public static InvoImage getTextIcon() {
        return textIcon.copy();
    }

    public static InvoImage getAddIcon() {
        return addIcon.copy();
    }

    public static InvoImage getRemoveIcon() {
        return removeIcon.copy();
    }
}

package invoker54.xpshop.client;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.invoimage.InvoImageCanvas;
import invoker54.invocore.client.invoimage.InvoImageColor;
import invoker54.invocore.client.invoimage.InvoImageTexture;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;

import java.awt.*;
import java.util.List;

public class InvoTheme {
    private static InvoImageCanvas background;

    private static InvoImageTexture editIcon;

    private static InvoImageTexture duplicateIcon;

    private static InvoImageTexture textIcon;

    private static InvoImageTexture addIcon;

    private static InvoImageTexture removeIcon;

    public static void init() {
        //Background
        InvoImageColor green = InvoImage.fromColor(new Color(74, 218, 64, 181));
        InvoImageColor fadeBlack = InvoImageColor.fromColor(new Color(0, 0, 0, 130));
        fadeBlack.setMainZone(fadeBlack.getMainZoneCopy().inflate(-1), false);
        background = green.canvas(fadeBlack.getMainZoneCopy(), List.of(fadeBlack));


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
        return background.copy();
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

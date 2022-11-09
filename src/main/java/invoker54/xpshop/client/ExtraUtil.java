package invoker54.xpshop.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.ClientUtil;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static invoker54.xpshop.client.screen.ShopScreen.SHOP_LOCATION;

public class ExtraUtil extends ClientUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    public static class ItemButton extends Button{

        public ItemStack displayItem;
        Bounds bounds;
        public ItemButton(int x, int y, int width, int height, String name, ItemStack item, Bounds bounds, IPressable onPress) {
            super(x, y, width, height, ITextComponent.nullToEmpty(name), onPress);
            this.displayItem = item;
            this.visible = true;
            this.bounds = bounds;
        }

        public ItemButton(Bounds bounds, IPressable onPress){
            super(0, 0, 0, 0, ITextComponent.nullToEmpty(null), onPress);
            this.bounds = bounds;
            displayItem = ItemStack.EMPTY;
        }

        @Override
        public void renderButton(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
            ExtraUtil.TEXTURE_MANAGER.bind(SHOP_LOCATION);
            if (this.isHovered){
                this.isHovered = ExtraUtil.inBounds(xMouse, yMouse, bounds);
            }

            int i = this.isHovered ? 26 : 0;

            if (!this.active) i = 52;


            //Render button
            ExtraUtil.blitImage(stack, this.x,  this.width, this.y, this.height,
                    230, this.width, i, this.height, 256);
//            //left part of the button
//            this.blit(stack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);

            ITEM_RENDERER.renderAndDecorateItem(displayItem,this.x + 5,this.y + 5);
        }

        @Override
        public boolean mouseClicked(double xMouse, double yMouse, int mouseButton) {
            if (!inBounds((float) xMouse, (float) yMouse,bounds)) return false;

            return super.mouseClicked(xMouse, yMouse, mouseButton);
        }
    }

    public static class AddButton extends ClientUtil.SimpleButton {
        Bounds bounds;

        public AddButton(int x, int y, int width, int height, String name, Bounds bounds, Button.IPressable onPress) {
            super(x, y, width, height, ITextComponent.nullToEmpty(name), onPress);
            this.bounds = bounds;
            this.visible = true;
        }

        @Override
        public boolean mouseClicked(double xMouse, double yMouse, int mouseButton) {
            if (!inBounds((float) xMouse, (float) yMouse,bounds)) return false;

            return super.mouseClicked(xMouse, yMouse, mouseButton);
        }
    }
}

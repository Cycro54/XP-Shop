package invoker54.xpshop.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.client.screen.SellContainerScreen;
import invoker54.xpshop.client.screen.ShopFeeScreen;
import invoker54.xpshop.client.screen.ShopScreen;
import invoker54.xpshop.client.screen.XPTransferScreen;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.WorldShopCapability;
import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.OpenSellContainerMsg;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static invoker54.xpshop.ContainerInit.sellContainerType;
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

    //Checks if items match without paying attention to count
    public static boolean itemsMatch(ItemStack one, ItemStack two){
        return one.sameItem(two) && ItemStack.tagMatches(one, two);
    }
    public static void openShop(boolean clickedWanderer){
        WorldShopCapability worldCap = WorldShopCapability.getShopCap(ClientUtil.getWorld());
        ShopCapability playerCap = ShopCapability.getShopCap(ClientUtil.getPlayer());
        if (playerCap == null) return;

        if (clickedWanderer || playerCap.buyUpgrade || ClientUtil.getPlayer().isCreative()) {
            ClientUtil.mC.setScreen(new ShopScreen(worldCap.getBuyEntries(ClientUtil.getPlayer()), clickedWanderer));
        }
        else{
            NetworkHandler.sendToServer(new OpenSellContainerMsg(clickedWanderer));
        }
    }
    public static void openXPTransfer(int playerID){
        ClientUtil.mC.setScreen(new XPTransferScreen(playerID));
    }
    public static void openShopFee(boolean clickedWanderer){
        ShopCapability cap = ShopCapability.getShopCap(ClientUtil.getPlayer());
        if (cap == null) return;

        if (cap.getShopTimeLeft() > 0 || cap.feeUpgrade || ExtraUtil.getPlayer().isCreative() || ShopConfig.shopFee == 0){
            openShop(clickedWanderer);
        }
        else {
            ClientUtil.mC.setScreen(new ShopFeeScreen(clickedWanderer));
        }

    }

    public static void registerContainerScreen(){
        ScreenManager.register(sellContainerType, SellContainerScreen::new);
    }
}

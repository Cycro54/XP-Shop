package invoker54.xpshop.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.client.event.RenderXPEvent;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.WorldShopCapability;
import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.OpenSellContainerMsg;
import invoker54.xpshop.common.network.msg.UnlockShopMsg;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ShopFeeScreen extends Screen {

    int fee;
    private ClientUtil.SimpleButton yesButton;
    private boolean clickedWanderer;

    public ShopFeeScreen(boolean clickedWanderer) {
        super(new TranslationTextComponent("ShopFeeScreen.shop_text"));
        this.clickedWanderer = clickedWanderer;
    }

    public ClientUtil.Image feeBackground = new ClientUtil.Image(new ResourceLocation(XPShop.MOD_ID,"textures/gui/screen/xp_transfer_screen.png"),
            0, 170, 79, 78, 256);

    @Override
    protected void init() {
        super.init();

        PlayerEntity player = ClientUtil.getPlayer();
        ShopCapability playerCap = ShopCapability.getShopCap(player);

        this.fee = Math.min(ShopConfig.shopFee,(playerCap.getPlayerTier().getMax()/ 6));
        
        feeBackground.centerImageX(0, this.width);
        feeBackground.centerImageY(0, this.height);

        //No button
        this.addButton(new ClientUtil.SimpleButton( feeBackground.x0 + 98,feeBackground.y0 + 50, 61, 20, ITextComponent.nullToEmpty("No"), (button) -> {
            ClientUtil.mC.setScreen(null);
        }));

        //Yes button
        yesButton = this.addButton(new ClientUtil.SimpleButton(feeBackground.x0 + 12, feeBackground.y0 + 50, 61, 20, ITextComponent.nullToEmpty("Yes"), (button) -> {
            if (player.totalExperience < fee) return;
            NetworkHandler.sendToServer(new UnlockShopMsg());

            if (clickedWanderer || playerCap.buyUpgrade || ExtraUtil.mC.player.isCreative()) {
                ClientUtil.mC.setScreen(
                        new ShopScreen(WorldShopCapability.getShopCap(ClientUtil.getWorld()).getBuyEntries(player), clickedWanderer));
            }
            else{
//                XPShop.LOGGER.debug("WILL THIS OPEN CREATIVE MENU?: " + (ExtraUtil.mC.player.isCreative()));
                NetworkHandler.sendToServer(new OpenSellContainerMsg(clickedWanderer));
            }
        }));
    }

    @Override
    public void renderBackground(MatrixStack stack) {
        super.renderBackground(stack);

        feeBackground.RenderImage(stack);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        yesButton.active = (ClientUtil.getPlayer().totalExperience > fee);

        //Pay
        int txtX = feeBackground.centerOnImageX(ClientUtil.mC.font.width("Pay"));
        int txtY = feeBackground.y0 + 6;
        ClientUtil.mC.font.draw(stack, "Pay", txtX, txtY, TextFormatting.WHITE.getColor());

        //XP AMOUNT
        txtX = feeBackground.centerOnImageX(ClientUtil.mC.font.width("" + this.fee));
        txtY = feeBackground.y0 + 6 + 12;
        RenderXPEvent.renderXPAmount(stack, ClientUtil.mC.font, "" + this.fee, txtX, txtY);

        //To unlock shop
        txtX = feeBackground.centerOnImageX(ClientUtil.mC.font.width("To unlock shop? (for now)"));
        txtY = feeBackground.y0 + 6 + 12 + 12;
        ClientUtil.mC.font.draw(stack, "To unlock shop? (for now)", txtX, txtY, TextFormatting.WHITE.getColor());

        super.render(stack, mouseX, mouseY, partialTicks);
    }
}

package invoker54.xpshop.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ClientUtil;
import invoker54.xpshop.client.event.RenderXPEvent;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.data.SellEntry;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.ClearSellContainerMsg;
import invoker54.xpshop.common.network.msg.SyncServerCapMsg;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.List;

public class SellContainerScreen extends ContainerScreen<SellContainer> {

    protected final ResourceLocation SELL_LOCATION = new ResourceLocation(XPShop.MOD_ID,"textures/gui/screen/sell_container_screen.png");
    //public static final ContainerType<MerchantContainer> MERCHANT = register("merchant", MerchantContainer::new);

    protected final int sellableColor = new Color(109, 255, 68, 213).getRGB();
    protected final int unSellableColor = new Color(54, 54, 54, 184).getRGB();

    private final int offsetY = 30;
    int halfWidthSpace;
    int halfHeightSpace;

    int tempLvl;
    float tempProgress;
    float tempTotal;

    public SellContainerScreen(SellContainer inst, PlayerInventory inv, ITextComponent title) {
        super(inst, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222 + offsetY;
    }

    @Override
    protected void init() {
        super.init();

        halfWidthSpace = (this.width - imageWidth)/2;
        halfHeightSpace = (this.height - imageHeight)/2;

        titleLabelX = 7;
        titleLabelY = 5;

        inventoryLabelX = 7;
        inventoryLabelY = 129;

        //This is the sell button
        addButton(new ClientUtil.SimpleButton(halfWidthSpace + 115, halfHeightSpace + 122,54,13, ITextComponent.nullToEmpty("Sell"), (button) -> {
            ShopCapability cap = ShopCapability.getShopCap(ClientUtil.mC.player);

            float totalExp = menu.totalExtraXP + cap.getLeftOverXP();
            cap.setLeftOverXP(totalExp - ((int)totalExp));
            ClientUtil.mC.player.giveExperiencePoints((int)totalExp);
            menu.tempInv.clearContent();
            NetworkHandler.INSTANCE.sendToServer(new ClearSellContainerMsg());
            NetworkHandler.INSTANCE.sendToServer(new SyncServerCapMsg(cap.writeNBT()));
        }));

        //Change to Buy screen button
        ClientUtil.SimpleButton buyButton = new ClientUtil.SimpleButton(halfWidthSpace + 3,halfHeightSpace + imageHeight - offsetY,14,21, null,(button) ->{
            //If the player is in creative, set the screen to add sell item screen
            ClientUtil.mC.setScreen(new ShopScreen());
        });
        buyButton.hidden = true;
        addButton(buyButton);

        //Now to set the initial values for the xp
        tempLvl = ClientUtil.mC.player.experienceLevel;
        tempProgress = ClientUtil.mC.player.experienceProgress;
        tempTotal = ClientUtil.mC.player.totalExperience + ShopCapability.getShopCap(ClientUtil.mC.player).getLeftOverXP();

        this.menu.tempInv.addListener((container) -> recalculateXP());
    }

    public void recalculateXP(){
        ShopCapability cap = ShopCapability.getShopCap(ClientUtil.mC.player);
        tempLvl = 0;
        tempTotal = ClientUtil.mC.player.totalExperience + cap.getLeftOverXP() + menu.totalExtraXP;
        tempProgress = (int)tempTotal / (float)RenderXPEvent.getXpNeededForNextLevel(tempLvl);

        while(this.tempProgress >= 1.0F) {
            this.tempProgress = (this.tempProgress - 1.0F) * (float)RenderXPEvent.getXpNeededForNextLevel(tempLvl);
            this.tempLvl++;
            this.tempProgress /= RenderXPEvent.getXpNeededForNextLevel(tempLvl);
        }
    }

    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int xMouse, int yMouse) {
        renderBackground(stack);

        ClientUtil.TEXTURE_MANAGER.bind(SELL_LOCATION);
        //Render the bg
        ClientUtil.blitImage(stack,halfWidthSpace,imageWidth,halfHeightSpace,imageHeight,0,imageWidth,0,imageHeight,256);

        drawCenteredString(stack,font,"Total",halfWidthSpace + (imageWidth/2),halfHeightSpace + 91,TextFormatting.WHITE.getColor());

        renderExperienceBar(stack);

        //region Render the flags next
        ClientUtil.TEXTURE_MANAGER.bind(ShopScreen.SHOP_LOCATION);

        //Render buy flag
        ClientUtil.blitImage(stack,halfWidthSpace + 3, 14,halfHeightSpace + imageHeight - offsetY,21,162, 28, 177, 42,256);
        //Render Sell flag
        ClientUtil.blitImage(stack,halfWidthSpace + 3 + 14, 14,halfHeightSpace + imageHeight - offsetY,28,134, 28, 177, 56,256);
        //endregion

        //Now render green slots for sellable items
        for (int a = 0; a < menu.slots.size(); a++){
            Slot slot = menu.getSlot(a);

            if (!slot.hasItem()) continue;

            if (ShopData.sellEntries.containsKey(slot.getItem().getItem())){
                ClientUtil.blitColor(stack,slot.x + halfWidthSpace, 16, slot.y + halfHeightSpace, 16, sellableColor);
            }
            else {
                ClientUtil.blitColor(stack,slot.x + halfWidthSpace, 16, slot.y + halfHeightSpace, 16, unSellableColor);
            }
        }
    }

    @Override
    public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        super.render(stack, xMouse, yMouse, partialTicks);
        this.renderTooltip(stack,xMouse,yMouse);
    }

    protected void renderExperienceBar(MatrixStack stack) {
        ShopCapability shopCap = ShopCapability.getShopCap(ClientUtil.mC.player);
        ClientUtil.mC.getProfiler().push("expBar");
        ClientUtil.TEXTURE_MANAGER.bind(AbstractGui.GUI_ICONS_LOCATION);
        int i = RenderXPEvent.getXpNeededForNextLevel(tempLvl);
        int x = halfWidthSpace + (imageWidth /2);
        int y = halfHeightSpace + 110;

        if (i > 0) {
            //Render bg of XP bar
            ClientUtil.blitImage(stack,x - (160/2),160,y,5,0,182,64, 5, 256);
            float p = tempProgress;
            if (p > 0) {
                ClientUtil.blitImage(stack,x - (160/2), (int) (160 * p),y,5,0,182 * p,69, 5, 256);
            }
        }

        this.minecraft.getProfiler().pop();
        if (tempLvl > 0) {
            y = y - 8;
            x = halfWidthSpace + ((imageWidth - font.width(String.valueOf(tempLvl))) /2);
            this.minecraft.getProfiler().push("expLevel");
            String s = "" + tempLvl;
            String extra = (getMenu().totalExtraXP != 0 ? " + " + getMenu().totalExtraXP : "");
            s += "  " + "(" + tempTotal + ")";
            RenderXPEvent.renderXPAmount(stack,font,s,x,y);
            this.minecraft.getProfiler().pop();
        }

    }

    @Override
    protected void renderTooltip(MatrixStack stack, int xMouse, int yMouse) {
        if (this.minecraft.player.inventory.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            //this.renderTooltip(p_230459_1_, this.hoveredSlot.getItem(), p_230459_2_, p_230459_3_);
            SellEntry targEntry = null;
            ItemStack item = this.hoveredSlot.getItem();

            if (ShopData.sellEntries.containsKey(item.getItem()))
                targEntry = ShopData.sellEntries.get(item.getItem());

            if (targEntry == null) {
                super.renderTooltip(stack, item, xMouse, yMouse);
            }
            else {
                List<ITextComponent> textList = getTooltipFromItem(item);

                String sellTxt = "\247a\247lSELL PRICE: " + "\247r" + targEntry.calcPrice(item.getCount());
                textList.add(ITextComponent.nullToEmpty(sellTxt));
                renderComponentTooltip(stack, textList, xMouse, yMouse);
            }
        }
    }
}

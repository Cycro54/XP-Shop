package invoker54.xpshop.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screen.ui.TextBoxUI;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.TradeXPMsg;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.math.NumberUtils;

import java.awt.*;

public class XPTransferScreen extends Screen {

    public int invisible = new Color(0,0,0,0).getRGB();
    public int grey = new Color(61, 61, 61, 255).getRGB();
    public final int otherPlayerID;
    
    public TextBoxUI tradeAmount;

    public XPTransferScreen(int otherPlayerID) {
        super(new TranslationTextComponent("XPTransferScreen.shop_text"));
        this.otherPlayerID = otherPlayerID;
    }

    public ClientUtil.Image tradeBackground = new ClientUtil.Image(new ResourceLocation(XPShop.MOD_ID,"textures/gui/screen/xp_transfer_screen.png"),
            0, 170, 0, 78, 256);


    @Override
    public void tick() {
        super.tick();
        tradeAmount.tick();

        LivingEntity otherPlayer = (LivingEntity) ClientUtil.mC.level.getEntity(otherPlayerID);

        if (!(otherPlayer instanceof PlayerEntity)) ClientUtil.mC.setScreen(null);
        if (!otherPlayer.isAlive()) ClientUtil.mC.setScreen(null);
        if (otherPlayer.distanceTo(ClientUtil.mC.player) > 10) ClientUtil.mC.setScreen(null);
    }

    @Override
    protected void init() {
        super.init();

        InvoZone tradeBgZone = tradeBackground.getRenderZone();
        tradeBgZone.centerX(this.width/2F);
        tradeBgZone.centerY(this.height/2F);

        //Text box widget
        tradeAmount = this.addButton(new TextBoxUI(ClientUtil.mC.font, (int) (tradeBgZone.x() + 63), (int) (tradeBgZone.y() + 30), 44, 9,
                ITextComponent.nullToEmpty("Enter XP"), invisible, invisible));

        //Cancel button
        this.addButton(new ClientUtil.SimpleButton((int) (tradeBgZone.x() + 12), (int) (tradeBgZone.y() + 50), 61, 20, ITextComponent.nullToEmpty("Cancel"), (button) -> {
            ClientUtil.mC.setScreen(null);
        }));

        //Done button
        this.addButton(new ClientUtil.SimpleButton((int) (tradeBgZone.x() + 98), (int) (tradeBgZone.y() + 50), 61, 20, ITextComponent.nullToEmpty("Done"), (button) -> {
            if (!NumberUtils.isParsable(tradeAmount.getValue())) return;
            NetworkHandler.sendToServer(new TradeXPMsg(otherPlayerID, Integer.parseInt(tradeAmount.getValue())));
            ClientUtil.mC.setScreen(null);
        }));
    }

    @Override
    public void renderBackground(MatrixStack stack) {
        super.renderBackground(stack);

        tradeBackground.render(stack);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        int txtX = (int) ((int) tradeBackground.getRenderZone().middleX()-(ClientUtil.mC.font.width("GIVE XP")/2F));
        int txtY = (int) (tradeBackground.getRenderZone().y() + 6);
        ClientUtil.mC.font.draw(stack, "GIVE XP", txtX, txtY, grey);

        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(char character, int keyCode){
        if (tradeAmount.isFocused()) {
            //Check if character is parsable
            if (!NumberUtils.isParsable(String.valueOf(character))) return false;

            //Parse the int and make sure it fits in
            if (Integer.parseInt(tradeAmount.getValue() + character) > ClientUtil.mC.player.totalExperience){
                tradeAmount.setValue(ClientUtil.mC.player.totalExperience + "");
                return false;
            }

            if (tradeAmount.charTyped(character,keyCode)) return true;
        }
        else {
            return super.charTyped(character, keyCode);
        }

        return false;
    }
}

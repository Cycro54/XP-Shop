package invoker54.xpshop.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ExtraUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, value = Dist.CLIENT)
public class RenderXPEvent {

    @SubscribeEvent
    public static void onXPRender(RenderGameOverlayEvent.Post event){

        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;

        FontRenderer font = ExtraUtil.mC.font;

        MainWindow window = event.getWindow();


        
        //String to render
        String xpAmount = ExtraUtil.formatValue(ExtraUtil.mC.player.totalExperience);
        if(ExtraUtil.mC.options.keyShift.isDown()) xpAmount = Integer.toString(ExtraUtil.mC.player.totalExperience);

        //X position
        int xPos = (window.getGuiScaledWidth() - font.width(xpAmount)) / 2;

        //Y position

        int yPos = window.getGuiScaledHeight() - 31 - 4 - 12;
        //If creative, change yPos to be same as vanilla xp number spot
        if (ExtraUtil.mC.player.isCreative()) yPos += 12;

        //Matrix stack
        MatrixStack stack = event.getMatrixStack();
        
        //Now finally render it.
        renderXPAmount(stack,font,xpAmount,xPos,yPos);
    }

    public static void renderXPAmount(MatrixStack stack, FontRenderer font, String text,int xPos, int yPos){
        //Now finally render it.
        font.draw(stack, text, (float)(xPos + 1), (float)yPos, 0);
        font.draw(stack, text, (float)(xPos - 1), (float)yPos, 0);
        font.draw(stack, text, (float)xPos, (float)(yPos + 1), 0);
        font.draw(stack, text, (float)xPos, (float)(yPos - 1), 0);
        font.draw(stack, text, (float)xPos, (float)yPos, 8453920);
    }

    public static int getXpNeededForNextLevel(int lvl){
        if (lvl >= 30) {
            return 112 + (lvl - 30) * 9;
        } else {
            return lvl >= 15 ? 37 + (lvl - 15) * 5 : 7 + lvl * 2;
        }
    }
}

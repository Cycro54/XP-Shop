package invoker54.xpshop.client.screen.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.xpshop.client.ClientUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class TextBoxUI extends TextFieldWidget {

    public static final int defInColor = new Color(215, 178, 123,255).getRGB();
    public static final int defOutColor = new Color(253, 138, 57,255).getRGB();
    protected final int ghostTxtColor = new Color(89, 89, 89,255).getRGB();
    int borderColor;
    int innerColor;

    public TextBoxUI(FontRenderer font, int x, int y, int width, int height, ITextComponent ghostTxt, int borderColor, int innerColor) {
        super(font, x, y, width, height, null, ghostTxt);

        this.borderColor = borderColor;
        this.innerColor = innerColor;
        this.setBordered(false);
        this.setMaxLength(50);
        this.setVisible(true);
        this.setTextColor(16777215);
    }

    @Override
    public void renderButton(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        ClientUtil.blitColor(stack,this.x - 1,this.width + 2, this.y - 1, this.height + 2,borderColor);
        ClientUtil.blitColor(stack,this.x,this.width, this.y, this.height, innerColor);

        super.renderButton(stack, xMouse, yMouse, partialTicks);

        ClientUtil.beginCrop(this.x,this.width, this.y, this.height, true);
        if (this.getValue().isEmpty()){
            ClientUtil.mC.font.draw(stack, this.getMessage(),
                    this.x,this.y + ((this.height - 9)/2f),ghostTxtColor);
        }
        ClientUtil.endCrop();
    }

    @Override
    public boolean keyPressed(int keyCode, int x, int y) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE){
            this.setFocus(false);
        }
        return super.keyPressed(keyCode, x, y);
    }
}

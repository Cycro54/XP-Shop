package invoker54.xpshop.client.screen.search;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.xpshop.client.ClientUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Locale;

public class InvSearchScreen extends SearchScreen {

    PlayerInventory inventory;
    private boolean inventoryMode = false;

    public InvSearchScreen(Screen prevScreen, IChooseItem onDone) {
        super(prevScreen, onDone);

        inventory = ClientUtil.mC.player.inventory;
    }

    @Override
    public void init() {
        super.init();

        searchBox.y = halfHeightSpace + 35 - searchBox.getHeight() - 2;

        addButton(new ClientUtil.SimpleButton(halfWidthSpace + 9, halfHeightSpace + 4,40,16, ITextComponent.nullToEmpty("Switch"),
                (button) -> {
                    inventoryMode = !inventoryMode;
                    refreshSearchResults();
                }));
    }

    @Override
    protected void refreshSearchResults() {

        if (inventoryMode) {
            searchList.clear();
            LOGGER.debug("Refreshing items");
            this.scrollOffs = 0;

            String s = this.searchBox.getValue();

            for (ItemStack item : inventory.items) {
                if (item.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))) {

                    if (item.getItem() == Items.AIR) continue;

                    searchList.add(item);
                }
            }

            LOGGER.debug("The size of the search list is2: " + searchList.size());
            for (ItemStack item : searchList) {
                LOGGER.debug(item.getDisplayName().getString());
            }

            recalcItemRenderList();
        }

        else {
            super.refreshSearchResults();
        }
    }

    @Override
    public void render(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
        super.render(stack, xMouse, yMouse, partialTicks);

        String txtToRender = inventoryMode ? "Inventory" : "All Items";

        drawCenteredString(stack,font, txtToRender, halfWidthSpace + (189/2), halfHeightSpace + 4, TextFormatting.WHITE.getColor());
    }
}

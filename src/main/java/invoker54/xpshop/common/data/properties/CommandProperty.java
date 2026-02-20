package invoker54.xpshop.common.data.properties;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.InvoText;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.containers.AbstractContainer;
import invoker54.xpshop.common.data.products.Product;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CommandProperty extends Property{
    @Override
    public void execute(Product product) {

    }

    @Override
    public AbstractContainer copy() {
        return null;
    }

    @Override
    public String getModId() {
        return XPShop.MOD_ID;
    }

    @Override
    public InvoText getTypeName() {
        return InvoText.translate(XPShopLanguageprovider.commandPropertyTypeName);
    }

    @Override
    public CompoundTag getTypeIcon() {
        return InvoImage.fromItem(new ItemStack(Items.COMMAND_BLOCK)).serializeNBT();
    }

    @Override
    public InvoText getTypeDescription() {
        return InvoText.translate(XPShopLanguageprovider.commandPropertyTypeDescription);
    }
}

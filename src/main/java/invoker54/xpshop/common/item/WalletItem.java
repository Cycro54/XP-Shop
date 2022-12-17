package invoker54.xpshop.common.item;

import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncClientCapMsg;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WalletItem extends Item {

    private static final Logger LOGGER = LogManager.getLogger();
    private final WalletTier WALLET_TIER;

    public WalletItem(WalletTier tier, Properties builder){
        super(builder);

        this.WALLET_TIER = tier;
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemStack = playerIn.getItemInHand(handIn);

        if (worldIn.isClientSide()) return ActionResult.consume(itemStack);

        //Now let's start checking!
        //First grab the shop cap
        ShopCapability cap = ShopCapability.getShopCap(playerIn);
        int myTier = this.WALLET_TIER.ordinal();
        int playerTier = cap.getPlayerTier().ordinal();

        //If there tier is too low, tell em that
        if (playerTier < myTier - 1){
            playerIn.sendMessage(new TranslationTextComponent("xp_shop.chat.tier_low"), Util.NIL_UUID);
            return ActionResult.fail(itemStack);
        }

        //If there tier is too high, tell em that
        else if (playerTier >= myTier){
            playerIn.sendMessage(new TranslationTextComponent("xp_shop.chat.have_upgrade"), Util.NIL_UUID);
            return ActionResult.fail(itemStack);
        }

        else {
            playerIn.sendMessage(new TranslationTextComponent("xp_shop.chat.unlock.upgrade_wallet").append(this.WALLET_TIER.getMax() + ""), Util.NIL_UUID);
        }

        //Else, upgrade their tier
        cap.setPlayerTier(this.WALLET_TIER);

        if (!playerIn.isCreative()) itemStack.shrink(1);

        //Then sync their cap data to client
        NetworkHandler.sendToPlayer(playerIn, new SyncClientCapMsg(cap.writeNBT()));

        return ActionResult.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World worldIn, List<ITextComponent> txtList, ITooltipFlag flag) {

        //This will add in the amount of xp that will be available
        txtList.add(new StringTextComponent(new TranslationTextComponent("xp_shop.desc.wallet").getString() + " " + this.WALLET_TIER.getMax()));
    }
}

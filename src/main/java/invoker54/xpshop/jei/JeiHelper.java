package invoker54.xpshop.jei;

import invoker54.xpshop.XPShop;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JeiHelper implements IModPlugin {
    private static IJeiRuntime runtime = null;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(XPShop.MOD_ID, "helper");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        IModPlugin.super.onRuntimeUnavailable();
    }

    public static IJeiRuntime getRuntime(){
        return runtime;
    }
}

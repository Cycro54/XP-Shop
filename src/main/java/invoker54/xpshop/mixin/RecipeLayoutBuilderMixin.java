package invoker54.xpshop.mixin;

import mezz.jei.library.gui.recipes.RecipeLayoutBuilder;
import mezz.jei.library.gui.recipes.layout.builder.IRecipeLayoutSlotSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RecipeLayoutBuilder.class)
public interface RecipeLayoutBuilderMixin {
    @Accessor(value = "slots", remap = false)
    List<IRecipeLayoutSlotSource> getSlots();
}

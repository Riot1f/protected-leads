package com.r1ftstudios.protectedleads.mixin;

import com.r1ftstudios.protectedleads.Protection;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * checkBurnOut is the method that consumes a neighbouring block once fire has
 * caught it. Cancelling it for a single protected position leaves fire spread
 * and every other block untouched.
 */
@Mixin(FireBlock.class)
public class FireBlockMixin {

   @Inject(method = "checkBurnOut", at = @At("HEAD"), cancellable = true)
   private void protectedleads$dontBurnProtected(Level level, BlockPos pos, int chance,
                                                 RandomSource random, int age, CallbackInfo ci) {
      if (Protection.isProtected(level, pos)) {
         ci.cancel();
      }
   }
}

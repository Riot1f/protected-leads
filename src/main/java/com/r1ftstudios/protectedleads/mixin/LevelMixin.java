package com.r1ftstudios.protectedleads.mixin;

import com.r1ftstudios.protectedleads.Protection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The catch-all. Everything that removes a block and gives a reason - withers,
 * ravagers, mod machinery, anything calling destroyBlock - lands here. Only
 * positions that are actually protected are affected, so nothing else in the
 * world behaves differently.
 */
@Mixin(Level.class)
public class LevelMixin {

   @Inject(
      method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
      at = @At("HEAD"),
      cancellable = true)
   private void protectedleads$guardProtected(BlockPos pos, boolean drop, Entity breaker, int limit,
                                              CallbackInfoReturnable<Boolean> cir) {
      Level self = (Level) (Object) this;
      if (self.isClientSide()) {
         return;
      }
      if (!Protection.isProtected(self, pos)) {
         return;
      }
      if (!Protection.mayModify(breaker, self, pos)) {
         cir.setReturnValue(false);
      }
   }
}

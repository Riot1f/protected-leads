package com.r1ftstudios.protectedleads.mixin;

import com.r1ftstudios.protectedleads.Protection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Belt and braces for the knot itself: the Fabric attack callback only covers
 * players punching it. This covers everything else that can deal damage to an
 * entity - arrows, blasts, other mods - so a protected knot cannot be knocked
 * off by anyone but its owner.
 */
@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin {

   @Shadow protected BlockPos pos;

   @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
   private void protectedleads$protectKnot(ServerLevel level, DamageSource source, float amount,
                                           CallbackInfoReturnable<Boolean> cir) {
      if (!((Object) this instanceof LeashFenceKnotEntity)) {
         return;
      }
      if (this.pos == null || !Protection.isProtected(level, this.pos)) {
         return;
      }
      if (!Protection.mayModify(source == null ? null : source.getEntity(), level, this.pos)) {
         cir.setReturnValue(false);
      }
   }
}

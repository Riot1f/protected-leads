package com.r1ftstudios.protectedleads.mixin;

import com.r1ftstudios.protectedleads.ProtectedLeadsData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Explosions still happen normally - only the protected positions are dropped
 * from the list of blocks the blast is allowed to remove, so everything around
 * a protected fence still blows up.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {

   @Shadow @Final private ServerLevel level;

   @Inject(method = "calculateExplodedPositions", at = @At("RETURN"), cancellable = true)
   private void protectedleads$spareProtected(CallbackInfoReturnable<List<BlockPos>> cir) {
      List<BlockPos> positions = cir.getReturnValue();
      if (positions == null || positions.isEmpty()) {
         return;
      }
      ProtectedLeadsData data = ProtectedLeadsData.get(this.level);
      if (data.isEmpty()) {
         return;
      }
      List<BlockPos> kept = null;
      for (int i = 0; i < positions.size(); ++i) {
         BlockPos pos = positions.get(i);
         if (data.isProtected(pos)) {
            if (kept == null) {
               kept = new ArrayList<>(positions.subList(0, i));
            }
         } else if (kept != null) {
            kept.add(pos);
         }
      }
      if (kept != null) {
         cir.setReturnValue(kept);
      }
   }
}

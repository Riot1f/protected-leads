package com.r1ftstudios.protectedleads.mixin;

import com.r1ftstudios.protectedleads.ProtectedLeadsData;
import com.r1ftstudios.protectedleads.Protection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** a piston must not be able to shove or crush a protected block */
@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin {

   @Shadow @Final private Level level;

   @Shadow public abstract List<BlockPos> getToPush();

   @Shadow public abstract List<BlockPos> getToDestroy();

   @Inject(method = "resolve", at = @At("RETURN"), cancellable = true)
   private void protectedleads$refuseProtected(CallbackInfoReturnable<Boolean> cir) {
      if (!Boolean.TRUE.equals(cir.getReturnValue())) {
         return;
      }
      ServerLevel server = Protection.serverLevel(this.level);
      if (server == null) {
         return;
      }
      ProtectedLeadsData data = ProtectedLeadsData.get(server);
      if (data.isEmpty()) {
         return;
      }
      for (BlockPos pos : this.getToPush()) {
         if (data.isProtected(pos)) {
            cir.setReturnValue(false);
            return;
         }
      }
      for (BlockPos pos : this.getToDestroy()) {
         if (data.isProtected(pos)) {
            cir.setReturnValue(false);
            return;
         }
      }
   }
}

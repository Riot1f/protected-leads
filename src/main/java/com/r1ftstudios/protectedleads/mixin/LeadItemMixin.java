package com.r1ftstudios.protectedleads.mixin;

import com.r1ftstudios.protectedleads.Protection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The one place ownership is created.
 *
 * LeadItem.bindPlayerMobs is what actually ties the player's leashed mobs to a
 * block - it creates the knot if there is not one already and returns a
 * consuming result only when at least one mob was attached. Hooking its return
 * means a claim is only ever made when a lead really went on, never when
 * somebody just right clicks an empty fence.
 */
@Mixin(LeadItem.class)
public class LeadItemMixin {

   @Inject(method = "bindPlayerMobs", at = @At("HEAD"), cancellable = true)
   private static void protectedleads$denyForeignAttach(Player player, Level level, BlockPos pos,
                                                        CallbackInfoReturnable<InteractionResult> cir) {
      if (level.isClientSide()) {
         return;
      }
      if (!Protection.mayModify(player, level, pos)) {
         // silent: looks to the player like the fence simply did nothing
         cir.setReturnValue(InteractionResult.PASS);
      }
   }

   @Inject(method = "bindPlayerMobs", at = @At("RETURN"))
   private static void protectedleads$claimOnAttach(Player player, Level level, BlockPos pos,
                                                    CallbackInfoReturnable<InteractionResult> cir) {
      if (level.isClientSide() || player == null) {
         return;
      }
      InteractionResult result = cir.getReturnValue();
      if (result == null || !result.consumesAction()) {
         return;
      }
      Protection.claim(level, pos, player.getUUID());
   }
}

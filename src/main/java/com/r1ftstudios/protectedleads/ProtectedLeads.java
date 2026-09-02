package com.r1ftstudios.protectedleads;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protected Leads.
 *
 * Whoever ties the first lead to a block owns that block and its leash knot,
 * permanently, until they break it themselves (or an operator clears it).
 */
public final class ProtectedLeads implements ModInitializer {

   public static final String MOD_ID = "protectedleads";
   public static final Logger LOGGER = LoggerFactory.getLogger("Protected Leads");

   @Override
   public void onInitialize() {
      // right clicking the knot is how a lead is taken back off a fence
      UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
         if (level.isClientSide() || !(entity instanceof LeashFenceKnotEntity knot)) {
            return InteractionResult.PASS;
         }
         BlockPos pos = knot.blockPosition();
         return Protection.mayModify(player, level, pos) ? InteractionResult.PASS : InteractionResult.FAIL;
      });

      // punching the knot destroys it and drops the lead
      AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
         if (level.isClientSide() || !(entity instanceof LeashFenceKnotEntity knot)) {
            return InteractionResult.PASS;
         }
         BlockPos pos = knot.blockPosition();
         return Protection.mayModify(player, level, pos) ? InteractionResult.PASS : InteractionResult.FAIL;
      });

      // breaking the supporting block would take the knot with it
      PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
         Protection.mayModify(player, level, pos));

      // the owner (or an op) removing their own block clears the claim
      PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
         if (!level.isClientSide()) {
            Protection.release(level, pos);
         }
      });

      CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
         ProtectedLeadsCommand.register(dispatcher));

      LOGGER.info("Protected Leads ready");
   }
}

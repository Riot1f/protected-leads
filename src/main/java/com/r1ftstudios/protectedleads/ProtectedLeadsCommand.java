package com.r1ftstudios.protectedleads;

import com.mojang.brigadier.CommandDispatcher;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** /protectedleads info | remove - operator recovery, nothing more */
public final class ProtectedLeadsCommand {

   private static final double REACH = 8.0D;

   private ProtectedLeadsCommand() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(Commands.literal("protectedleads")
         .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
         .then(Commands.literal("info").executes(ctx -> info(ctx.getSource())))
         .then(Commands.literal("remove").executes(ctx -> remove(ctx.getSource()))));
   }

   /** the block the operator is looking at, or the block a targeted knot sits on */
   private static BlockPos targetPos(ServerPlayer player) {
      HitResult hit = player.pick(REACH, 0.0F, false);
      if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
         return blockHit.getBlockPos();
      }
      if (hit instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof LeashFenceKnotEntity knot) {
         return knot.blockPosition();
      }
      return null;
   }

   private static int info(CommandSourceStack source) {
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         source.sendFailure(Component.literal("Run this as a player, looking at the block."));
         return 0;
      }
      ServerLevel level = player.level() instanceof ServerLevel s ? s : source.getLevel();
      BlockPos pos = targetPos(player);
      if (pos == null) {
         source.sendFailure(Component.literal("Not looking at a block."));
         return 0;
      }
      UUID owner = Protection.ownerOf(level, pos);
      if (owner == null) {
         source.sendSuccess(() -> Component.literal(
            describe(pos) + " is not protected."), false);
         return 0;
      }
      String name = resolveName(source, owner);
      source.sendSuccess(() -> Component.literal(
         describe(pos) + " is protected by " + name + " (" + owner + ")."), false);
      return 1;
   }

   private static int remove(CommandSourceStack source) {
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         source.sendFailure(Component.literal("Run this as a player, looking at the block."));
         return 0;
      }
      ServerLevel level = player.level() instanceof ServerLevel s ? s : source.getLevel();
      BlockPos pos = targetPos(player);
      if (pos == null) {
         source.sendFailure(Component.literal("Not looking at a block."));
         return 0;
      }
      if (Protection.release(level, pos)) {
         source.sendSuccess(() -> Component.literal(
            "Removed lead protection from " + describe(pos) + "."), true);
         return 1;
      }
      source.sendFailure(Component.literal(describe(pos) + " is not protected."));
      return 0;
   }

   private static String describe(BlockPos pos) {
      return pos.getX() + " " + pos.getY() + " " + pos.getZ();
   }

   private static String resolveName(CommandSourceStack source, UUID owner) {
      try {
         ServerPlayer online = source.getServer().getPlayerList().getPlayer(owner);
         if (online != null) {
            return online.getName().getString();
         }
      } catch (Throwable ignored) {
         // fall through
      }
      return "offline player";
   }
}

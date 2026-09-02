package com.r1ftstudios.protectedleads;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** every protection decision in the mod goes through here */
public final class Protection {

   private Protection() {
   }

   public static ServerLevel serverLevel(Level level) {
      return level instanceof ServerLevel server ? server : null;
   }

   public static boolean isProtected(Level level, BlockPos pos) {
      ServerLevel server = serverLevel(level);
      if (server == null || pos == null) {
         return false;
      }
      ProtectedLeadsData data = ProtectedLeadsData.get(server);
      return !data.isEmpty() && data.isProtected(pos);
   }

   public static UUID ownerOf(Level level, BlockPos pos) {
      ServerLevel server = serverLevel(level);
      if (server == null || pos == null) {
         return null;
      }
      return ProtectedLeadsData.get(server).getOwner(pos);
   }

   /**
    * Operators bypass protection. 26.2 replaced integer permission levels with
    * a PermissionSet, so this asks the player's own command source rather than
    * the old hasPermissions(int).
    */
   public static boolean isOperator(Player player) {
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return false;
      }
      try {
         return serverPlayer.createCommandSourceStack().permissions()
            .hasPermission(Permissions.COMMANDS_GAMEMASTER);
      } catch (Throwable ignored) {
         return false;
      }
   }

   /** true when this player may change whatever is at pos */
   public static boolean mayModify(Player player, Level level, BlockPos pos) {
      if (player == null) {
         return false;
      }
      UUID owner = ownerOf(level, pos);
      if (owner == null) {
         return true;
      }
      return owner.equals(player.getUUID()) || isOperator(player);
   }

   /** same question for an arbitrary entity source, e.g. explosion or projectile damage */
   public static boolean mayModify(Entity entity, Level level, BlockPos pos) {
      UUID owner = ownerOf(level, pos);
      if (owner == null) {
         return true;
      }
      if (entity instanceof Player player) {
         return owner.equals(player.getUUID()) || isOperator(player);
      }
      return false;
   }

   public static void claim(Level level, BlockPos pos, UUID owner) {
      ServerLevel server = serverLevel(level);
      if (server != null && pos != null && owner != null) {
         ProtectedLeadsData.get(server).claim(pos, owner);
      }
   }

   public static boolean release(Level level, BlockPos pos) {
      ServerLevel server = serverLevel(level);
      return server != null && pos != null && ProtectedLeadsData.get(server).release(pos);
   }
}

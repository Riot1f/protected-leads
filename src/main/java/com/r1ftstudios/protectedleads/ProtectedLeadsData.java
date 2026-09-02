package com.r1ftstudios.protectedleads;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Per-dimension record of which block positions are protected and by whom.
 *
 * This is a real {@link SavedData}, so it is written into the dimension's
 * data/ folder with the rest of the world save and survives restarts, chunk
 * unloads, the owner logging out and the leashed animal despawning. Nothing
 * here is derived from the currently attached entity - ownership is whoever
 * tied the first lead to the block, stored by UUID, forever.
 */
public final class ProtectedLeadsData extends SavedData {

   public record Entry(BlockPos pos, UUID owner) {
      public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
         UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner)
      ).apply(instance, Entry::new));
   }

   public static final Codec<ProtectedLeadsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Entry.CODEC.listOf().optionalFieldOf("claims", List.of()).forGetter(ProtectedLeadsData::toEntries)
   ).apply(instance, ProtectedLeadsData::fromEntries));

   public static final SavedDataType<ProtectedLeadsData> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath(ProtectedLeads.MOD_ID, "claims"),
      ProtectedLeadsData::new,
      CODEC,
      DataFixTypes.LEVEL);

   private final Map<BlockPos, UUID> owners = new HashMap<>();

   public ProtectedLeadsData() {
   }

   private static ProtectedLeadsData fromEntries(List<Entry> entries) {
      ProtectedLeadsData data = new ProtectedLeadsData();
      for (Entry entry : entries) {
         if (entry.pos() != null && entry.owner() != null) {
            data.owners.put(entry.pos().immutable(), entry.owner());
         }
      }
      return data;
   }

   private List<Entry> toEntries() {
      List<Entry> entries = new ArrayList<>(this.owners.size());
      for (Map.Entry<BlockPos, UUID> e : this.owners.entrySet()) {
         entries.add(new Entry(e.getKey(), e.getValue()));
      }
      return entries;
   }

   /** the live store for this dimension, created on first use */
   public static ProtectedLeadsData get(ServerLevel level) {
      return level.getDataStorage().computeIfAbsent(TYPE);
   }

   public boolean isEmpty() {
      return this.owners.isEmpty();
   }

   public UUID getOwner(BlockPos pos) {
      return this.owners.get(pos);
   }

   public boolean isProtected(BlockPos pos) {
      return this.owners.containsKey(pos);
   }

   /** claims a position; does nothing if it is already claimed by anyone */
   public boolean claim(BlockPos pos, UUID owner) {
      BlockPos key = pos.immutable();
      if (this.owners.containsKey(key)) {
         return false;
      }
      this.owners.put(key, owner);
      this.setDirty();
      return true;
   }

   public boolean release(BlockPos pos) {
      if (this.owners.remove(pos.immutable()) != null) {
         this.setDirty();
         return true;
      }
      return false;
   }

   public int size() {
      return this.owners.size();
   }
}

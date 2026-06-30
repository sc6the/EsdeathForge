package net.labymod.labyconnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.labymod.core.LabyModCore;
import net.labymod.labyconnect.packets.PacketUserTracker;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

public class LabyConnectUserTracker {
   private static final long LIST_UPDATE_INTERVAL = 10000L;
   private static final long ENTITY_UPDATE_INTERVAL = 3000L;
   private final LabyConnect labyConnect;
   private final Map<PacketUserTracker.EnumTrackingAction, List<PacketUserTracker.PlayerEntityMeta>> listBuffer = new HashMap<>();
   private final Map<PacketUserTracker.EnumTrackingAction, List<PacketUserTracker.PlayerEntityMeta>> entityBuffer = new HashMap<>();
   private long nextTimeDrainList = -1L;
   private long nextTimeDrainEntities = -1L;
   private boolean empty = true;

   public LabyConnectUserTracker(LabyConnect labyConnect) {
      this.labyConnect = labyConnect;

      for (PacketUserTracker.EnumTrackingAction action : PacketUserTracker.EnumTrackingAction.values()) {
         this.listBuffer.put(action, new ArrayList<>());
         this.entityBuffer.put(action, new ArrayList<>());
      }
   }

   public void onLoadWorld() {
      this.clearBuffers();
   }

   public void onDisconnectServer() {
      this.clearBuffers();
   }

   public void onLabyConnectConnect() {
      this.clearBuffers();
      NetHandlerPlayClient connection = LabyModCore.getMinecraft().getConnection();
      if (connection != null) {
         for (NetworkPlayerInfo playerInfo : connection.getPlayerInfoMap()) {
            if (this.isPlayerInfoValid(playerInfo)) {
               this.listBuffer.get(PacketUserTracker.EnumTrackingAction.ADD).add(new PacketUserTracker.PlayerEntityMeta(playerInfo.getGameProfile()));
               this.empty = false;
            }
         }
      }

      LabyModCore.WorldAdapter world = LabyModCore.getMinecraft().getWorld();
      if (world != null) {
         for (EntityPlayer player : world.j) {
            this.entityBuffer.get(PacketUserTracker.EnumTrackingAction.ADD).add(new PacketUserTracker.PlayerEntityMeta(player.getUniqueID()));
            this.empty = false;
         }
      }
   }

   public void onGameTick() {
      if (this.labyConnect.isOnline()) {
         if (this.nextTimeDrainList < System.currentTimeMillis() || this.listBuffer.get(PacketUserTracker.EnumTrackingAction.ADD).size() > 10) {
            this.nextTimeDrainList = System.currentTimeMillis() + 10000L;
            this.drain(PacketUserTracker.EnumTrackingChannel.LIST, this.listBuffer);
         }

         if (this.nextTimeDrainEntities < System.currentTimeMillis() || this.entityBuffer.get(PacketUserTracker.EnumTrackingAction.ADD).size() > 10) {
            this.nextTimeDrainEntities = System.currentTimeMillis() + 3000L;
            this.drain(PacketUserTracker.EnumTrackingChannel.ENTITIES, this.entityBuffer);
         }
      }
   }

   private void drain(PacketUserTracker.EnumTrackingChannel channel, Map<PacketUserTracker.EnumTrackingAction, List<PacketUserTracker.PlayerEntityMeta>> map) {
      for (Entry<PacketUserTracker.EnumTrackingAction, List<PacketUserTracker.PlayerEntityMeta>> entry : map.entrySet()) {
         List<PacketUserTracker.PlayerEntityMeta> buffer = entry.getValue();
         if (!buffer.isEmpty()) {
            this.labyConnect
               .getClientConnection()
               .sendPacket(new PacketUserTracker(channel, entry.getKey(), buffer.toArray(new PacketUserTracker.PlayerEntityMeta[0])));
            buffer.clear();
         }
      }
   }

   public void onPlayerInfoAdd(NetworkPlayerInfo playerInfo) {
      if (this.isPlayerInfoValid(playerInfo)) {
         this.update(
            this.listBuffer,
            new PacketUserTracker.PlayerEntityMeta(playerInfo.getGameProfile()),
            PacketUserTracker.EnumTrackingAction.REMOVE,
            PacketUserTracker.EnumTrackingAction.ADD,
            true
         );
      }
   }

   public void onPlayerInfoRemove(UUID uniqueId) {
      if (this.isUniqueIdValid(uniqueId)) {
         this.update(
            this.listBuffer,
            new PacketUserTracker.PlayerEntityMeta(uniqueId),
            PacketUserTracker.EnumTrackingAction.ADD,
            PacketUserTracker.EnumTrackingAction.REMOVE,
            false
         );
      }
   }

   public void onVisiblePlayer(UUID uniqueId) {
      if (this.isUniqueIdValid(uniqueId)) {
         this.update(
            this.entityBuffer,
            new PacketUserTracker.PlayerEntityMeta(uniqueId),
            PacketUserTracker.EnumTrackingAction.REMOVE,
            PacketUserTracker.EnumTrackingAction.ADD,
            true
         );
      }
   }

   public void onEntityDestruct(UUID uniqueId) {
      if (this.isUniqueIdValid(uniqueId)) {
         this.update(
            this.entityBuffer,
            new PacketUserTracker.PlayerEntityMeta(uniqueId),
            PacketUserTracker.EnumTrackingAction.ADD,
            PacketUserTracker.EnumTrackingAction.REMOVE,
            false
         );
      }
   }

   private void update(
      Map<PacketUserTracker.EnumTrackingAction, List<PacketUserTracker.PlayerEntityMeta>> buffers,
      PacketUserTracker.PlayerEntityMeta uniqueId,
      PacketUserTracker.EnumTrackingAction cleanTarget,
      PacketUserTracker.EnumTrackingAction actionTarget,
      boolean forceAdd
   ) {
      if (this.labyConnect.isOnline()) {
         List<PacketUserTracker.PlayerEntityMeta> buffer = buffers.get(cleanTarget);
         if (buffer.contains(uniqueId)) {
            buffer.remove(uniqueId);
            if (!forceAdd) {
               return;
            }
         }

         List<PacketUserTracker.PlayerEntityMeta> target = buffers.get(actionTarget);
         if (!target.contains(uniqueId)) {
            target.add(uniqueId);
         }

         this.empty = false;
      }
   }

   private void clearBuffers() {
      for (List<PacketUserTracker.PlayerEntityMeta> buffer : this.listBuffer.values()) {
         buffer.clear();
      }

      for (List<PacketUserTracker.PlayerEntityMeta> buffer : this.entityBuffer.values()) {
         buffer.clear();
      }

      if (this.labyConnect.isOnline() && !this.empty) {
         this.labyConnect
            .getClientConnection()
            .sendPacket(new PacketUserTracker(PacketUserTracker.EnumTrackingChannel.LIST, PacketUserTracker.EnumTrackingAction.CLEAR));
         this.labyConnect
            .getClientConnection()
            .sendPacket(new PacketUserTracker(PacketUserTracker.EnumTrackingChannel.ENTITIES, PacketUserTracker.EnumTrackingAction.CLEAR));
      }

      this.empty = true;
   }

   private boolean isPlayerInfoValid(NetworkPlayerInfo playerInfo) {
      UUID uuid = playerInfo.getGameProfile().getId();
      return this.isUniqueIdValid(uuid);
   }

   private boolean isUniqueIdValid(UUID uniqueId) {
      return uniqueId.getMostSignificantBits() != 0L && uniqueId.getLeastSignificantBits() != 0L
         ? (uniqueId.getMostSignificantBits() >> 12 & 15L) == 4L
         : false;
   }
}

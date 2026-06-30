package net.labymod.labyconnect.packets;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.Base64.Decoder;
import net.labymod.labyconnect.handling.PacketHandler;

public class PacketUserTracker extends Packet {
   private static final Decoder BASE64_DECODER = Base64.getDecoder();
   private PacketUserTracker.EnumTrackingChannel channel;
   private PacketUserTracker.EnumTrackingAction action;
   private PacketUserTracker.PlayerEntityMeta[] users;

   public PacketUserTracker() {
   }

   public PacketUserTracker(PacketUserTracker.EnumTrackingChannel channel, PacketUserTracker.EnumTrackingAction action) {
      this(channel, action, new PacketUserTracker.PlayerEntityMeta[0]);
   }

   public PacketUserTracker(
      PacketUserTracker.EnumTrackingChannel channel, PacketUserTracker.EnumTrackingAction action, PacketUserTracker.PlayerEntityMeta[] users
   ) {
      this.channel = channel;
      this.action = action;
      this.users = users;
   }

   @Override
   public void read(PacketBuf buf) {
      buf.readByte();
      this.channel = PacketUserTracker.EnumTrackingChannel.values()[buf.readByte()];
      this.action = PacketUserTracker.EnumTrackingAction.values()[buf.readByte()];
      if (this.action != PacketUserTracker.EnumTrackingAction.CLEAR) {
         this.users = new PacketUserTracker.PlayerEntityMeta[buf.readInt()];

         for (int i = 0; i < this.users.length; i++) {
            this.users[i] = new PacketUserTracker.PlayerEntityMeta(buf.readLong(), buf.readLong());
            if (this.channel == PacketUserTracker.EnumTrackingChannel.LIST && this.action == PacketUserTracker.EnumTrackingAction.ADD) {
               this.users[i].setCape(buf.readByte());
            }
         }
      }

      if (this.action == PacketUserTracker.EnumTrackingAction.UPDATE) {
         buf.readByte();
      }
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeByte(5);
      buf.writeByte(this.channel.ordinal());
      buf.writeByte(this.action.ordinal());
      if (this.action != PacketUserTracker.EnumTrackingAction.CLEAR) {
         buf.writeInt(this.users.length);

         for (PacketUserTracker.PlayerEntityMeta user : this.users) {
            buf.writeLong(user.getMostSignificantBits());
            buf.writeLong(user.getLeastSignificantBits());
            if (this.channel == PacketUserTracker.EnumTrackingChannel.LIST && this.action == PacketUserTracker.EnumTrackingAction.ADD) {
               buf.writeByte(user.getCape());
            }
         }
      }

      if (this.action == PacketUserTracker.EnumTrackingAction.UPDATE) {
         buf.writeByte(0);
      }
   }

   @Override
   public void handle(PacketHandler packetHandler) {
   }

   public PacketUserTracker.EnumTrackingChannel getChannel() {
      return this.channel;
   }

   public PacketUserTracker.EnumTrackingAction getAction() {
      return this.action;
   }

   public PacketUserTracker.PlayerEntityMeta[] getUsers() {
      return this.users;
   }

   public static enum CapeType {
      UNKNOWN(1, null),
      VANILLA(2, "f9a76537647989f9a0b6d001e320dac591c359e9e61a31f4ce11c88f207f0ad4"),
      MIGRATOR(3, "2340c0e03dd24a11b15a8b33c2a7e9e32abb2051b2481d0ba7defd635ca7a933");

      private final int type;
      private final String hash;

      private CapeType(int type, String hash) {
         this.type = type;
         this.hash = hash;
      }

      public int getType() {
         return this.type;
      }

      public String getHash() {
         return this.hash;
      }

      public static PacketUserTracker.CapeType getByType(int type) {
         for (PacketUserTracker.CapeType capeType : values()) {
            if (capeType.getType() == type) {
               return capeType;
            }
         }

         return UNKNOWN;
      }

      public static PacketUserTracker.CapeType getByHash(String hash) {
         for (PacketUserTracker.CapeType capeType : values()) {
            if (capeType.getHash().equals(hash)) {
               return capeType;
            }
         }

         return UNKNOWN;
      }

      public static PacketUserTracker.CapeType find(String json) {
         if (json.contains("\"CAPE\" :")) {
            for (PacketUserTracker.CapeType type : values()) {
               if (type.getHash() != null && json.contains(type.getHash())) {
                  return type;
               }
            }

            return UNKNOWN;
         } else {
            return null;
         }
      }
   }

   public static enum EnumTrackingAction {
      ADD,
      REMOVE,
      UPDATE,
      CLEAR,
      SYNC;
   }

   public static enum EnumTrackingChannel {
      ENTITIES,
      LIST;
   }

   public static class PlayerEntityMeta {
      private final UUID uuid;
      private byte cape = 0;

      public PlayerEntityMeta(UUID uuid) {
         this.uuid = uuid;
      }

      public PlayerEntityMeta(long mostSignificantBits, long leastSignificantBits) {
         this.uuid = new UUID(mostSignificantBits, leastSignificantBits);
      }

      public PlayerEntityMeta(UUID uuid, byte cape) {
         this.uuid = uuid;
         this.cape = cape;
      }

      public PlayerEntityMeta(GameProfile profile) {
         this.uuid = profile.getId();

         try {
            Collection<Property> textures = profile.getProperties().get("textures");
            Iterator var3 = textures.iterator();
            if (var3.hasNext()) {
               Property texture = (Property)var3.next();
               String json = new String(PacketUserTracker.BASE64_DECODER.decode(texture.getValue()));
               PacketUserTracker.CapeType type = PacketUserTracker.CapeType.find(json);
               if (type != null) {
                  this.cape = (byte)type.getType();
               }
            }
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      }

      public void setCape(byte cape) {
         this.cape = cape;
      }

      @Override
      public int hashCode() {
         return this.uuid.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof PacketUserTracker.PlayerEntityMeta ? this.uuid.equals(((PacketUserTracker.PlayerEntityMeta)obj).uuid) : false;
      }

      public long getMostSignificantBits() {
         return this.uuid.getMostSignificantBits();
      }

      public long getLeastSignificantBits() {
         return this.uuid.getLeastSignificantBits();
      }

      public byte getCape() {
         return this.cape;
      }

      public UUID getUuid() {
         return this.uuid;
      }
   }
}

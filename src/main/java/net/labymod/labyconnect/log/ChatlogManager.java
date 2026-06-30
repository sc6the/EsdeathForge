package net.labymod.labyconnect.log;

import com.mojang.authlib.GameProfile;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.labymod.labyconnect.user.ChatUser;
import net.labymod.labyconnect.user.UserStatus;
import net.labymod.main.Source;
import net.labymod.support.util.Debug;

public class ChatlogManager {
   private final int MAX_LOG_MESSAGE_COUNT = 1000;
   private List<SingleChat> chats = new ArrayList<>();

   public SingleChat getChat(ChatUser user) {
      for (SingleChat chat : this.chats) {
         if (chat.getChatPartner().equals(user)) {
            return chat.apply(user);
         }
      }

      SingleChat singleChat = new SingleChat(this.chats.size(), user, new ArrayList<>());
      this.chats.add(singleChat);
      return singleChat;
   }

   public void loadChatlogs(UUID accountUUID) {
      this.chats.clear();
      File chatlogFile = new File(Source.FILE_CHATLOG, accountUUID + ".log");
      if (chatlogFile.exists()) {
         try {
            DataInputStream dis = new DataInputStream(new FileInputStream(chatlogFile));
            int total = dis.readInt();

            for (int i = 0; i < total; i++) {
               dis.readInt();
               String name = this.readString(dis);
               UUID uuid = new UUID(dis.readLong(), dis.readLong());
               ArrayList<MessageChatComponent> messageArray = new ArrayList<>();
               int totalMessages = dis.readInt();
               if (totalMessages < 1000) {
                  for (int b = 0; b < totalMessages; b++) {
                     String sender = this.readString(dis);
                     long time = dis.readLong();
                     String message = this.readString(dis);
                     messageArray.add(new MessageChatComponent(sender, time, message));
                  }
               }

               GameProfile dummyGameProfile = new GameProfile(uuid, name);
               SingleChat dummySingleChat = this.getChat(
                  new ChatUser(dummyGameProfile, UserStatus.OFFLINE, "", null, 0, System.currentTimeMillis(), 0L, "", 0L, 0L, 0, false)
               );
               dummySingleChat.getMessages().addAll(messageArray);
            }

            dis.close();
         } catch (Exception var15) {
            chatlogFile.delete();
            var15.printStackTrace();
         }

         Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Loaded " + this.chats.size() + " chats!");
      }
   }

   public void saveChatlogs(UUID accountUUID) {
      Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Save chat log file for " + accountUUID.toString());
      File chatlogFile = new File(Source.FILE_CHATLOG, accountUUID + ".log");
      if (!chatlogFile.getParentFile().exists()) {
         chatlogFile.getParentFile().mkdirs();
      }

      if (!chatlogFile.exists()) {
         Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Create new log file for " + accountUUID.toString());

         try {
            chatlogFile.createNewFile();
         } catch (IOException var15) {
            var15.printStackTrace();
         }
      }

      try {
         List<SingleChat> chatTemp = new ArrayList<>(this.chats);
         Iterator<SingleChat> iterator = chatTemp.iterator();

         while (iterator.hasNext()) {
            if (iterator.next().getChatPartner().isParty()) {
               iterator.remove();
            }
         }

         DataOutputStream dos = new DataOutputStream(new FileOutputStream(chatlogFile));
         dos.writeInt(chatTemp.size());

         for (SingleChat chat : chatTemp) {
            dos.writeInt(chat.getId());
            GameProfile profile = chat.getChatPartner().getGameProfile();
            this.writeString(dos, profile.getName());
            dos.writeLong(profile.getId().getMostSignificantBits());
            dos.writeLong(profile.getId().getLeastSignificantBits());
            List<MessageChatComponent> messageArray = chat.getMessages();
            int size = messageArray.size();
            int count = size;
            boolean flag = size > 300;
            dos.writeInt(flag ? 300 : size);

            for (int b = 0; b < size; b++) {
               if (flag && count > 300) {
                  count--;
               } else {
                  MessageChatComponent component = messageArray.get(b);
                  this.writeString(dos, component.getSender());
                  dos.writeLong(component.getSentTime());
                  this.writeString(dos, component.getMessage());
               }
            }
         }

         dos.flush();
         dos.close();
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Saved " + this.chats.size() + " chats!");
   }

   private void writeString(DataOutputStream dos, String string) {
      try {
         byte[] bytes = string.getBytes(Charset.forName("UTF8"));
         dos.writeInt(bytes.length);

         for (byte b : bytes) {
            dos.writeByte(b);
         }
      } catch (IOException var8) {
         var8.printStackTrace();
      }
   }

   private String readString(DataInputStream dis) {
      try {
         int length = dis.readInt();
         byte[] bytes = new byte[length];

         for (int i = 0; i < length; i++) {
            bytes[i] = dis.readByte();
         }

         return new String(bytes, Charset.forName("UTF8"));
      } catch (Exception var5) {
         return "";
      }
   }

   public int getMAX_LOG_MESSAGE_COUNT() {
      return 1000;
   }

   public List<SingleChat> getChats() {
      return this.chats;
   }
}

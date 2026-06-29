package net.labymod.addons.voicechat.audio.opus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

public class ServiceClient {
   private Process process;
   private DataOutputStream dataOutputStream;
   private DataInputStream dataInputStream;

   public ServiceClient(List<String> command) throws Throwable {
      this.process = new ProcessBuilder(command).start();
      this.dataOutputStream = new DataOutputStream(this.process.getOutputStream());
      this.dataInputStream = new DataInputStream(this.process.getInputStream());
   }

   public boolean isRunning() {
      return this.process.isAlive();
   }

   public void destroy() throws Throwable {
      if (this.isRunning()) {
         this.process.destroyForcibly();
      }
   }

   public DataOutputStream getDataOutputStream() {
      return this.dataOutputStream;
   }

   public DataInputStream getDataInputStream() {
      return this.dataInputStream;
   }
}

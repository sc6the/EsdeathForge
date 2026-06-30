package net.labymod.labyconnect;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import net.labymod.labyconnect.handling.PacketDecoder;
import net.labymod.labyconnect.handling.PacketEncoder;
import net.labymod.labyconnect.handling.PacketPrepender;
import net.labymod.labyconnect.handling.PacketSplitter;

public class ClientChannelInitializer extends ChannelInitializer<NioSocketChannel> {
   private LabyConnect labyConnect;
   private ClientConnection clientConnection;

   public ClientChannelInitializer(LabyConnect labyConnect, ClientConnection clientConnection) {
      this.labyConnect = labyConnect;
      this.clientConnection = clientConnection;
   }

   protected void initChannel(NioSocketChannel channel) throws Exception {
      this.clientConnection.setNioSocketChannel(channel);
      channel.pipeline()
         .addLast("timeout", new ReadTimeoutHandler(120L, TimeUnit.SECONDS))
         .addLast("splitter", new PacketPrepender())
         .addLast("decoder", new PacketDecoder(this.labyConnect))
         .addLast("prepender", new PacketSplitter())
         .addLast("encoder", new PacketEncoder(this.labyConnect))
         .addLast(new ChannelHandler[]{this.getClientConnection()});
   }

   public ClientConnection getClientConnection() {
      return this.clientConnection;
   }
}

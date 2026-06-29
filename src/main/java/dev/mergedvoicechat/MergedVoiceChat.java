package dev.mergedvoicechat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.addons.voicechat.listener.MessageSendListener;
import net.labymod.addons.voicechat.listener.PluginMessageListener;
import net.labymod.addons.voicechat.listener.ServerJoinListener;
import net.labymod.addons.voicechat.listener.ServerMessageListener;
import net.labymod.addons.voicechat.listener.ServerQuitListener;
import net.labymod.utils.ServerData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

import java.util.EnumMap;

@Mod(modid = MergedVoiceChat.MODID, name = "Merged VoiceChat", version = MergedVoiceChat.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class MergedVoiceChat {

    public static final String MODID = "mergedvoicechat";
    public static final String VERSION = "0.1.0-dev";

    @Mod.Instance(MODID)
    public static MergedVoiceChat INSTANCE;

    public final VoiceChat voiceChat = new VoiceChat();
    public dev.mergedvoicechat.gui.SpeakerHud speakerHud;
    public dev.mergedvoicechat.gui.NameplateSpeakerIcon nameplateIcon;

    private ServerJoinListener serverJoinListener;
    private ServerQuitListener serverQuitListener;
    private MessageSendListener messageSendListener;
    private ServerMessageListener serverMessageListener;
    private PluginMessageListener pluginMessageListener;

    public static EnumMap<Side, FMLEmbeddedChannel> LMC_CHANNEL;
    public static EnumMap<Side, FMLEmbeddedChannel> LM3_CHANNEL;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.setFile(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        voiceChat.init();
        Config.load(voiceChat);

        serverJoinListener = new ServerJoinListener(voiceChat);
        serverQuitListener = new ServerQuitListener(voiceChat);
        messageSendListener = new MessageSendListener(voiceChat);
        serverMessageListener = new ServerMessageListener(voiceChat);
        pluginMessageListener = new PluginMessageListener(voiceChat);

        MinecraftForge.EVENT_BUS.register(voiceChat);
        MinecraftForge.EVENT_BUS.register(this);

        speakerHud = new dev.mergedvoicechat.gui.SpeakerHud(voiceChat);
        MinecraftForge.EVENT_BUS.register(speakerHud);
        // The floating nameplate speaker icon is replaced by the 3D player-model glow outline,
        // rendered with an offscreen framebuffer + glow/outline shaders (Raven-bS PlayerESP style).
        MinecraftForge.EVENT_BUS.register(new dev.mergedvoicechat.gui.SpeakerOutline(voiceChat));
        // SVC-style corner mic-state icon (Simple Voice Chat HUD look)
        MinecraftForge.EVENT_BUS.register(new dev.mergedvoicechat.gui.SvcIconHud(voiceChat));

        Keybinds.register();
        LMC_CHANNEL = NetworkRegistry.INSTANCE.newChannel("LMC", new LMCInboundHandler());
        LM3_CHANNEL = NetworkRegistry.INSTANCE.newChannel("labymod3:main", new LMCInboundHandler());
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        net.minecraft.client.multiplayer.ServerData mc = Minecraft.getMinecraft().getCurrentServerData();
        ServerData sd = (mc == null)
            ? new ServerData("singleplayer", 0, "singleplayer")
            : new ServerData(mc.serverIP, 25565, mc.serverName);
        serverJoinListener.accept(sd);
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        serverQuitListener.accept(null);
        Config.save(voiceChat);
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        // Outgoing /voicemute interception requires a Mixin into GuiChat.sendChatMessage. Not yet wired.
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Disable VoiceChat's internal Keyboard.isKeyDown checks; we drive state via Forge KeyBindings.
        voiceChat.keyPushToTalk      = -1;
        voiceChat.keyToggleVoiceChat = -1;
        voiceChat.permaVoiceKey      = -1;

        boolean inGui = Minecraft.getMinecraft().currentScreen != null;

        // PTT: held while keybind is down and no GUI is open.
        boolean ptt = !inGui && Keybinds.PUSH_TO_TALK.getKeyCode() != 0 && Keybinds.PUSH_TO_TALK.isKeyDown();
        if (voiceChat.permaVoiceEnabled && voiceChat.permaVoiceActive) ptt = true;
        if (!voiceChat.pushToTalkPressed && ptt) voiceChat.cleanup = true;
        voiceChat.pushToTalkPressed = ptt;
        if (voiceChat.getMicrophone() != null && ptt && voiceChat.testingMicrophone) voiceChat.testingMicrophone = false;

        // Toggle voice chat (rising edge).
        if (Keybinds.TOGGLE_VOICE.isPressed()) {
            voiceChat.enabled = !voiceChat.enabled;
        }

        // Toggle PermaVoice (rising edge).
        if (Keybinds.TOGGLE_PERMAVOICE.isPressed()) {
            voiceChat.permaVoiceActive = !voiceChat.permaVoiceActive;
            if (voiceChat.permaVoiceChatMessages && Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new net.minecraft.util.ChatComponentText(
                        "§ePermaVoice §8» §" + (voiceChat.permaVoiceActive ? "aON" : "cOFF")));
            }
        }

        // Open settings GUI (when no other screen is up).
        if (Keybinds.OPEN_SETTINGS.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
            Minecraft.getMinecraft().displayGuiScreen(new dev.mergedvoicechat.gui.GuiSettings(voiceChat));
        }

        // Open per-player volume GUI for the entity we're looking at.
        if (Keybinds.OPEN_VOLUME_GUI.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
            net.minecraft.util.MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
            if (mop != null && mop.entityHit instanceof net.minecraft.entity.player.EntityPlayer) {
                net.minecraft.entity.player.EntityPlayer p = (net.minecraft.entity.player.EntityPlayer) mop.entityHit;
                Minecraft.getMinecraft().displayGuiScreen(
                    new dev.mergedvoicechat.gui.GuiVolume(voiceChat, p.getUniqueID(), p.getName()));
            }
        }

        // Drive the visible-players update.
        if (voiceChat.getServerIncomingPacketListener() != null) {
            try { voiceChat.getServerIncomingPacketListener().accept(null); } catch (Throwable t) { t.printStackTrace(); }
        }
    }

    /** Send a JSON-bodied LMC message on both legacy "LMC" and modern "labymod3:main". */
    public static void sendLmcJson(String key, String json) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        try {
            io.netty.buffer.ByteBuf buf = Unpooled.buffer();
            new PacketBuffer(buf).writeString(key).writeString(json);
            byte[] payload = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), payload);

            sendOn(LMC_CHANNEL, "LMC", payload);
            sendOn(LM3_CHANNEL, "labymod3:main", payload);
        } catch (Throwable t) { t.printStackTrace(); }
    }

    private static void sendOn(EnumMap<Side, FMLEmbeddedChannel> channelMap, String channelName, byte[] payload) {
        if (channelMap == null) return;
        FMLEmbeddedChannel ch = channelMap.get(Side.CLIENT);
        if (ch == null) return;
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(Unpooled.wrappedBuffer(payload)), channelName);
        ch.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        ch.writeOutbound(pkt);
    }

    @io.netty.channel.ChannelHandler.Sharable
    private final class LMCInboundHandler extends io.netty.channel.SimpleChannelInboundHandler<FMLProxyPacket> {
        @Override
        protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, FMLProxyPacket msg) {
            try {
                PacketBuffer in = new PacketBuffer(msg.payload());
                String key = in.readStringFromBuffer(Short.MAX_VALUE);
                String json = in.readStringFromBuffer(Short.MAX_VALUE);
                if ("MC|Brand".equals(key)) {
                    pluginMessageListener.receiveMessage(key, in);
                    return;
                }
                JsonElement element = new JsonParser().parse(json);
                serverMessageListener.onServerMessage(key, element);
            } catch (Throwable t) { t.printStackTrace(); }
        }
    }
}

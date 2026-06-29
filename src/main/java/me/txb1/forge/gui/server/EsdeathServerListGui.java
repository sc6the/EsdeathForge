package me.txb1.forge.gui.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.txb1.EsdeathClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenAddServer;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

// Esdeath server list — replaces vanilla GuiMultiplayer, styled like the Esdeath resource-pack GUI.
// Right-click a row for the context menu (Join / Pin / Delete / Edit); drag rows to reorder, but a
// pinned server can never cross below an unpinned one (and vice versa). Server names support '&'
// colour codes. Add/Direct-Connect reuse the vanilla dialogs. ESC returns to the parent screen.
public class EsdeathServerListGui extends GuiScreen {
   private static final int ROW_H = 36;
   private static final int B_DIRECT = 201, B_ADD = 202, B_REFRESH = 205, B_VIA = 206;
   private static final ResourceLocation SERVER_ICONS = new ResourceLocation("textures/gui/server_selection.png");
   private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

   private static final int CTX_W = 84, CTX_ROW_H = 13;

   private final GuiScreen parent;
   private ServerList servers;

   private final List<ServerData> view = new ArrayList<ServerData>(); // display order (pinned first)
   private final List<ServerData> shown = new ArrayList<ServerData>(); // view filtered by the search box
   private String search = "";
   private int selected = -1;
   private int scroll;
   private int listTop, listBottom, listX, listW;

   // drag state
   private boolean dragging;
   private int dragFrom = -1;
   private int dragMouseY;
   private int pressY;

   // double-click-to-join tracking
   private long lastClickMs;
   private int lastClickRow = -1;

   // right-click context menu
   private boolean ctxOpen;
   private int ctxTarget = -1;
   private int ctxX, ctxY;

   // pending dialog actions resolved in confirmClicked-style callbacks
   private ServerData addingServer;
   private ServerData editingTarget; // the actual ServerData being edited (search-safe, not an index)
   private boolean editing;
   private boolean directConnect;

   public EsdeathServerListGui(GuiScreen parent) {
      this.parent = parent;
   }

   @Override
   public void initGui() {
      this.servers = new ServerList(this.mc);
      this.servers.loadServerList();
      this.listX = 24;
      this.listW = this.width - 48;
      this.listTop = 52;
      this.listBottom = this.height - 36;
      rebuildView();
      refreshPings();

      // Single bottom bar: Add Server | Direct Connect | Refresh (3 equal buttons, centred).
      this.buttonList.clear();
      int cx = this.width / 2;
      int bot = this.height - 28;
      this.buttonList.add(new GuiButton(B_ADD, cx - 154, bot, 100, 20, "Add Server"));
      this.buttonList.add(new GuiButton(B_DIRECT, cx - 50, bot, 100, 20, "Direct Connect"));
      this.buttonList.add(new GuiButton(B_REFRESH, cx + 54, bot, 100, 20, "Refresh"));
      // ViaVersion (ViaForge) protocol selector — only when ViaForge is installed. Top-right corner.
      if (me.txb1.extras.via.ViaForgeBridge.isPresent()) {
         this.buttonList.add(new GuiButton(B_VIA, this.width - 124, 6, 118, 20, viaLabel()));
      }
   }

   private String viaLabel() {
      String v = me.txb1.extras.via.ViaForgeBridge.targetVersionName();
      return "ViaVersion: " + (v.isEmpty() ? "?" : v);
   }

   private void rebuildView() {
      this.view.clear();
      List<ServerData> pinned = new ArrayList<ServerData>();
      List<ServerData> rest = new ArrayList<ServerData>();
      for (int i = 0; i < this.servers.countServers(); i++) {
         ServerData sd = this.servers.getServerData(i);
         (PinnedServers.isPinned(sd.serverIP) ? pinned : rest).add(sd);
      }
      this.view.addAll(pinned);
      this.view.addAll(rest);
      rebuildShown();
   }

   // shown = view filtered by the search box (name / ip / motd). With an empty query it mirrors view
   // exactly (same order + refs), so drag-reorder against shown indices == view indices.
   private void rebuildShown() {
      this.shown.clear();
      String q = this.search.toLowerCase().trim();
      for (ServerData sd : this.view) {
         if (q.isEmpty()
            || (sd.serverName != null && sd.serverName.toLowerCase().contains(q))
            || (sd.serverIP != null && sd.serverIP.toLowerCase().contains(q))
            || (sd.serverMOTD != null && sd.serverMOTD.toLowerCase().contains(q))) {
            this.shown.add(sd);
         }
      }
   }

   private void refreshPings() {
      for (ServerData sd : this.view) {
         sd.pingToServer = -2L;
         sd.serverMOTD = EnumChatFormatting.GRAY + "Pinging...";
         final ServerData fsd = sd;
         // Self-contained SLP ping per server (see EsdeathPinger) — writes MOTD/ping straight onto
         // the ServerData; no main-thread pump required.
         Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
               EsdeathPinger.ping(fsd);
            }
         }, "Esdeath Server Pinger");
         t.setDaemon(true);
         t.start();
      }
   }

   @Override
   public void updateScreen() {
   }

   @Override
   protected void actionPerformed(GuiButton b) throws IOException {
      switch (b.id) {
         case B_DIRECT:
            this.directConnect = true;
            this.editing = false;
            this.addingServer = new ServerData("Server", "", false);
            this.mc.displayGuiScreen(new GuiScreenServerList(this, this.addingServer));
            break;
         case B_ADD:
            this.directConnect = false;
            this.editing = false;
            this.addingServer = new ServerData("Minecraft Server", "", false);
            this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.addingServer));
            break;
         case B_REFRESH:
            this.servers.loadServerList();
            rebuildView();
            refreshPings();
            break;
         case B_VIA:
            me.txb1.extras.via.ViaForgeBridge.openSelector(this);
            break;
      }
   }

   private void join(int idx) {
      if (idx >= 0 && idx < this.shown.size()) {
         ServerData sd = this.shown.get(idx);
         EsdeathConnect.connect(this, sd.serverName, sd.serverIP);
      }
   }

   private void editAt(int idx) {
      if (idx < 0 || idx >= this.shown.size()) {
         return;
      }
      this.editing = true;
      this.selected = idx;
      ServerData s = this.shown.get(idx);
      this.editingTarget = s;
      this.addingServer = new ServerData(s.serverName, s.serverIP, false);
      this.addingServer.copyFrom(s);
      this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.addingServer));
   }

   private void deleteAt(int idx) {
      if (idx < 0 || idx >= this.shown.size()) {
         return;
      }
      ServerData sd = this.shown.get(idx);
      for (int i = 0; i < this.servers.countServers(); i++) {
         if (this.servers.getServerData(i) == sd) {
            this.servers.removeServerData(i);
            break;
         }
      }
      this.servers.saveServerList();
      this.selected = -1;
      rebuildView();
   }

   // index of the first unpinned row (== number of pinned rows, since pinned float to the top).
   private int firstUnpinned() {
      for (int i = 0; i < this.view.size(); i++) {
         if (!PinnedServers.isPinned(this.view.get(i).serverIP)) {
            return i;
         }
      }
      return this.view.size();
   }

   // called back by the vanilla add/direct dialogs
   public void confirmClicked(boolean confirmed, int id) {
      if (confirmed && this.addingServer != null) {
         if (this.directConnect) {
            // direct connect: just join, don't add to the list
            EsdeathConnect.connect(this.parent, this.addingServer.serverName, this.addingServer.serverIP);
            this.addingServer = null;
            return;
         }
         if (this.editing && this.editingTarget != null) {
            this.editingTarget.copyFrom(this.addingServer);
         } else {
            this.servers.addServerData(this.addingServer);
         }
         this.editingTarget = null;
         this.editing = false;
         this.servers.saveServerList();
         rebuildView();
         refreshPings();
      }
      this.addingServer = null;
      this.mc.displayGuiScreen(this);
   }

   private void persistOrder() {
      // write the current view order (minus the pin float) back into ServerList
      List<ServerData> ordered = new ArrayList<ServerData>(this.view);
      while (this.servers.countServers() > 0) {
         this.servers.removeServerData(this.servers.countServers() - 1);
      }
      for (ServerData sd : ordered) {
         this.servers.addServerData(sd);
      }
      this.servers.saveServerList();
   }

   @Override
   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int wheel = Mouse.getEventDWheel();
      if (wheel != 0) {
         closeContext();
         this.scroll = clampScroll(this.scroll + (wheel > 0 ? -ROW_H : ROW_H));
      }
   }

   private void openContext(int mx, int my, int row) {
      this.ctxOpen = true;
      this.ctxTarget = row;
      int h = 4 * CTX_ROW_H + 2;
      this.ctxX = Math.min(mx, this.width - CTX_W - 2);
      this.ctxY = Math.min(my, this.height - h - 2);
   }

   private void closeContext() {
      this.ctxOpen = false;
      this.ctxTarget = -1;
   }

   // context-menu rows: Join / Pin|Unpin / Delete / Edit
   private String ctxItem(int i) {
      boolean pinned = this.ctxTarget >= 0 && this.ctxTarget < this.shown.size()
         && PinnedServers.isPinned(this.shown.get(this.ctxTarget).serverIP);
      switch (i) {
         case 0: return "§aJoin";
         case 1: return pinned ? "§eUnpin" : "§ePin";
         case 2: return "§cDelete";
         case 3: return "§7Edit";
         default: return "";
      }
   }

   private int ctxItemAt(int mx, int my) {
      if (mx < this.ctxX || mx > this.ctxX + CTX_W) {
         return -1;
      }
      for (int i = 0; i < 4; i++) {
         int rowY = this.ctxY + 1 + i * CTX_ROW_H;
         if (my >= rowY && my < rowY + CTX_ROW_H) {
            return i;
         }
      }
      return -1;
   }

   private int clampScroll(int s) {
      int content = this.shown.size() * ROW_H;
      int max = Math.max(0, content - (this.listBottom - this.listTop));
      return Math.max(0, Math.min(max, s));
   }

   private int rowAt(int mouseY) {
      if (mouseY < this.listTop || mouseY >= this.listBottom) {
         return -1;
      }
      int idx = (mouseY - this.listTop + this.scroll) / ROW_H;
      return idx >= 0 && idx < this.shown.size() ? idx : -1;
   }

   @Override
   protected void mouseClicked(int mx, int my, int btn) throws IOException {
      // context menu eats all clicks while open
      if (this.ctxOpen) {
         if (btn == 0) {
            int item = ctxItemAt(mx, my);
            int target = this.ctxTarget;
            closeContext();
            if (item >= 0 && target >= 0 && target < this.shown.size()) {
               switch (item) {
                  case 0: join(target); break;
                  case 1:
                     PinnedServers.toggle(this.shown.get(target).serverIP);
                     rebuildView();
                     break;
                  case 2: deleteAt(target); break;
                  case 3: editAt(target); break;
               }
            }
         } else {
            closeContext();
         }
         return;
      }

      super.mouseClicked(mx, my, btn);
      int row = rowAt(my);
      if (row >= 0 && mx >= this.listX && mx <= this.listX + this.listW) {
         if (btn == 1) {
            this.selected = row;
            openContext(mx, my, row);
            return;
         }
         this.selected = row;
         // double-click a row to join it
         long now = System.currentTimeMillis();
         if (row == this.lastClickRow && now - this.lastClickMs < 300L) {
            this.lastClickRow = -1;
            join(row);
            return;
         }
         this.lastClickRow = row;
         this.lastClickMs = now;
         // drag-reorder only when not filtering (shown == view); otherwise indices wouldn't map back
         if (this.search.isEmpty()) {
            this.dragFrom = row;
            this.pressY = my;
            this.dragMouseY = my;
         }
      }
   }

   @Override
   protected void mouseClickMove(int mx, int my, int btn, long time) {
      this.dragMouseY = my;
      if (!this.dragging && this.dragFrom >= 0 && Math.abs(my - this.pressY) > 4) {
         this.dragging = true;
      }
   }

   @Override
   protected void mouseReleased(int mx, int my, int btn) {
      super.mouseReleased(mx, my, btn);
      if (this.dragging && this.dragFrom >= 0) {
         int target = (my - this.listTop + this.scroll) / ROW_H;
         target = Math.max(0, Math.min(this.view.size() - 1, target));
         // Clamp into the dragged row's own section: pinned rows stay above the divider,
         // unpinned rows stay below it (a pin can never sink past an unpinned server).
         int floor = firstUnpinned();
         if (this.dragFrom < floor) {
            target = Math.min(target, floor - 1);
         } else {
            target = Math.max(target, floor);
         }
         target = Math.max(0, Math.min(this.view.size() - 1, target));
         if (target != this.dragFrom) {
            ServerData moved = this.view.remove(this.dragFrom);
            this.view.add(target, moved);
            this.selected = target;
            persistOrder();
            rebuildShown();
         }
      }
      this.dragging = false;
      this.dragFrom = -1;
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (this.ctxOpen && keyCode == 1) { // ESC closes the context menu first
         closeContext();
         return;
      }
      if (keyCode == 1) {
         this.mc.displayGuiScreen(this.parent);
         return;
      }
      // Delete key removes the selected server
      if ((keyCode == org.lwjgl.input.Keyboard.KEY_DELETE) && this.selected >= 0 && this.selected < this.shown.size()) {
         deleteAt(this.selected);
         return;
      }
      // search box typing
      if (keyCode == org.lwjgl.input.Keyboard.KEY_BACK) {
         if (this.search.length() > 0) {
            this.search = this.search.substring(0, this.search.length() - 1);
            this.selected = -1;
            this.scroll = 0;
            rebuildShown();
         }
         return;
      }
      if (typedChar >= 32 && this.search.length() < 32) {
         this.search += typedChar;
         this.selected = -1;
         this.scroll = 0;
         rebuildShown();
         return;
      }
      super.keyTyped(typedChar, keyCode);
   }

   @Override
   public void drawScreen(int mx, int my, float pt) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, "§b§lServers", this.width / 2, 12, -1);

      // search box
      int sbW = 200;
      int sbX = this.width / 2 - sbW / 2;
      int sbY = 28;
      Gui.drawRect(sbX, sbY, sbX + sbW, sbY + 14, 0x80000000);
      Gui.drawRect(sbX, sbY, sbX + sbW, sbY + 1, 0xFF555555);
      String shown = this.search.isEmpty() ? "§7Search servers..." : this.search + (System.currentTimeMillis() % 1000 < 500 ? "_" : "");
      this.fontRendererObj.drawStringWithShadow(shown, sbX + 5, sbY + 3, -1);

      // list rows (skip the dragged row's normal slot; draw it under the cursor instead)
      for (int i = 0; i < this.shown.size(); i++) {
         int ry = this.listTop + i * ROW_H - this.scroll;
         if (ry + ROW_H <= this.listTop || ry >= this.listBottom) {
            continue;
         }
         if (this.dragging && i == this.dragFrom) {
            continue;
         }
         drawRow(this.shown.get(i), this.listX, ry, i == this.selected, mx, my);
      }
      if (this.dragging && this.dragFrom >= 0 && this.dragFrom < this.shown.size()) {
         drawRow(this.shown.get(this.dragFrom), this.listX, this.dragMouseY - ROW_H / 2, true, mx, my);
      }

      super.drawScreen(mx, my, pt);

      if (this.ctxOpen) {
         drawContextMenu(mx, my);
      }
   }

   private void drawContextMenu(int mx, int my) {
      int h = 4 * CTX_ROW_H + 2;
      Gui.drawRect(this.ctxX - 1, this.ctxY - 1, this.ctxX + CTX_W + 1, this.ctxY + h + 1, 0xFF000000);
      Gui.drawRect(this.ctxX, this.ctxY, this.ctxX + CTX_W, this.ctxY + h, 0xF0181818);
      for (int i = 0; i < 4; i++) {
         int rowY = this.ctxY + 1 + i * CTX_ROW_H;
         boolean hover = mx >= this.ctxX && mx <= this.ctxX + CTX_W && my >= rowY && my < rowY + CTX_ROW_H;
         if (hover) {
            Gui.drawRect(this.ctxX, rowY, this.ctxX + CTX_W, rowY + CTX_ROW_H, 0x66FFFFFF);
         }
         this.fontRendererObj.drawStringWithShadow(ctxItem(i), this.ctxX + 6, rowY + 3, -1);
      }
   }

   private void drawRow(ServerData sd, int x, int y, boolean sel, int mx, int my) {
      boolean hov = my >= y && my < y + ROW_H && mx >= x && mx <= x + this.listW && my >= this.listTop && my < this.listBottom;
      Gui.drawRect(x, Math.max(y, this.listTop), x + this.listW, Math.min(y + ROW_H, this.listBottom),
         sel ? 0x803366CC : (hov ? 0x55FFFFFF : 0x40000000));

      // favicon (or default)
      ResourceLocation icon = ServerIcons.get(sd);
      GlStateManager.color(1F, 1F, 1F, 1F);
      this.mc.getTextureManager().bindTexture(icon != null ? icon : SERVER_ICONS);
      if (icon != null) {
         Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0F, 0F, 32, 32, 32F, 32F);
      } else {
         Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0F, 0F, 32, 32, 256F, 256F);
      }

      boolean pinned = PinnedServers.isPinned(sd.serverIP);
      String name = (pinned ? "§e★ §r" : "") + ColorUtil.translate(sd.serverName);
      int tx = x + 38;
      this.fontRendererObj.drawStringWithShadow(name, tx, y + 3, 0xFFFFFFFF);
      String motd = sd.serverMOTD == null ? "" : sd.serverMOTD.replace('\n', ' ');
      this.fontRendererObj.drawStringWithShadow(trim(ColorUtil.translate(motd), this.listW - 60), tx, y + 14, 0xFFAAAAAA);

      // ping
      drawPing(sd, x + this.listW - 18, y + 4);
   }

   private void drawPing(ServerData sd, int x, int y) {
      int v;
      if (sd.pingToServer < 0L) {
         v = 5; // unknown/cross
      } else if (sd.pingToServer < 150L) {
         v = 0;
      } else if (sd.pingToServer < 300L) {
         v = 1;
      } else if (sd.pingToServer < 600L) {
         v = 2;
      } else if (sd.pingToServer < 1000L) {
         v = 3;
      } else {
         v = 4;
      }
      GlStateManager.color(1F, 1F, 1F, 1F);
      this.mc.getTextureManager().bindTexture(ICONS);
      this.drawTexturedModalRect(x, y, 0, 176 + v * 8, 10, 8);
   }

   private String trim(String s, int max) {
      if (this.fontRendererObj.getStringWidth(s) <= max) {
         return s;
      }
      while (s.length() > 1 && this.fontRendererObj.getStringWidth(s + "...") > max) {
         s = s.substring(0, s.length() - 1);
      }
      return s + "...";
   }

   public ServerList getServers() {
      return this.servers;
   }

   // Entry point for the "Add to Server list" button on the vanilla Direct-Connect dialog: opens the
   // add-server GUI with the typed address pre-filled, then drops back into this list once saved.
   // Self-contained (uses Minecraft.getMinecraft()) so it works before this screen is ever displayed.
   public void beginAddFromAddress(String ip) {
      Minecraft mc = Minecraft.getMinecraft();
      if (this.servers == null) {
         this.servers = new ServerList(mc);
         this.servers.loadServerList();
      }
      this.directConnect = false;
      this.editing = false;
      this.addingServer = new ServerData("Minecraft Server", ip == null ? "" : ip, false);
      mc.displayGuiScreen(new GuiScreenAddServer(this, this.addingServer));
   }
}

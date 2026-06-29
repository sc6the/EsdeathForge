package me.proxycracked.universalaccountmanager.auth;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SessionManager {
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static Field sessionField = null;

  private static Field getField() {
    if (sessionField == null) {
      try {
        sessionField = Minecraft.class.getDeclaredField("session");   // deobf MCP name (fallback finds by type)
        sessionField.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(sessionField, sessionField.getModifiers() & ~Modifier.FINAL);
      } catch (Exception e) {
        try {
          for (Field f : Minecraft.class.getDeclaredFields()) {
            if (f.getType().isAssignableFrom(Session.class)) {
              sessionField = f;
              sessionField.setAccessible(true);
              break;
            }
          }
        } catch (Exception ignored) {
          sessionField = null;
        }
      }
    }
    return sessionField;
  }

  public static Session get() {
    return mc.getSession();
  }

  public static void set(Session session) {
    try {
      Field f = getField();
      if (f != null) f.set(mc, session);
    } catch (Exception ignored) {
      //
    }
  }
}

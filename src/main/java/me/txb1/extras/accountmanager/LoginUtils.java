package me.txb1.extras.accountmanager;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import java.net.Proxy;
import net.minecraft.util.Session;

public class LoginUtils {

   public static Session createSession(String var0, String var1, Proxy var2) {
      YggdrasilAuthenticationService var3 = new YggdrasilAuthenticationService(var2, "");
      YggdrasilUserAuthentication var4 = (YggdrasilUserAuthentication)var3.createUserAuthentication(Agent.MINECRAFT);
      var4.setUsername(var0);
      var4.setPassword(var1);

      try {
         var4.logIn();
      } catch (AuthenticationException var7) {
         return null;
      }

               try {
            return new Session(var4.getSelectedProfile().getName(), var4.getSelectedProfile().getId().toString(), var4.getAuthenticatedToken(), "mojang");
         } catch (NullPointerException var6) {
            return null;
         }
      
   }

}

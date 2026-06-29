package me.txb1.forge.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

// Tolerate slightly-malformed pack.mcmeta files. Some packs ship a "description" string containing a
// RAW newline/tab (e.g. a two-line description), which is invalid JSON — a string literal can't hold
// unescaped control characters — so vanilla's strict parse throws and the pack is rejected as having
// an "invalid pack.mcmeta". We parse strictly first (valid packs are untouched); on failure we replace
// raw control chars (NOT the two-char "\n" escape sequence) with spaces and retry, then hand the
// parsed object back to the metadata serializer exactly as vanilla would.
@Mixin(AbstractResourcePack.class)
public abstract class MixinAbstractResourcePack {

   @Inject(method = "readMetadata", at = @At("HEAD"), cancellable = true)
   private static void esdeath$lenientMcmeta(IMetadataSerializer serializer, InputStream in, String section,
                                             CallbackInfoReturnable<IMetadataSection> cir) {
      String text;
      try {
         text = IOUtils.toString(in, "UTF-8");
      } catch (Throwable t) {
         return; // couldn't read -> let vanilla handle it
      } finally {
         IOUtils.closeQuietly(in);
      }
      JsonObject json;
      try {
         json = new JsonParser().parse(text).getAsJsonObject(); // strict first: valid packs unchanged
      } catch (Throwable strict) {
         String fixed = text.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
         try {
            json = new JsonParser().parse(fixed).getAsJsonObject();
         } catch (Throwable t2) {
            return; // still unparseable -> let vanilla throw its usual error (stream already read)
         }
      }
      cir.setReturnValue(serializer.parseMetadataSection(section, json));
   }
}

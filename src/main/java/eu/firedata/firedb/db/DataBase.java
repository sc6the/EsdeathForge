package eu.firedata.firedb.db;

import eu.firedata.firedb.FireDB;
import eu.firedata.firedb.db.type.Type;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class DataBase {
   private FireDB instance;
   private File file;
   private String data;

   public Object getObject(String var1) {
      try {
         String var2 = String.valueOf(new StringBuilder().append("\\{").append(var1));
         String var3 = this.data.split(var2)[1].split("}")[0];
         String var4 = var3.split("\\|")[1];
         return this.encode(var4);
      } catch (Exception var6) {
         return null;
      }
   }

   public boolean saveObject(String var1, Object var2) {
      var1 = var1.replaceAll("\\{", "");
      var1 = var1.replaceAll("}", "");
      var1 = var1.replaceAll("\\|", "");

      try {
         if ((this.data.contains(String.valueOf(new StringBuilder().append("{").append(var1).append("|"))))) {
            String var10 = String.valueOf(
               new StringBuilder()
                  .append("{")
                  .append(var1)
                  .append("|")
                  .append(
                     this.data.split(String.valueOf(new StringBuilder().append("\\{").append(var1).append("\\|")))[1]
                        .split("}")[0]
                  )
                  .append("}")
            );
            String var4 = this.data.split(String.valueOf(new StringBuilder().append("\\{").append(var1).append("\\|")))[1]
               .split("}")[0];
            String var5 = var10.replace(var4, this.decode(var2));
            this.data = this.data.replace(var10, var5);
            return true;
         } else {
            String var3 = this.decode(var2);
            if ((this.data.endsWith("}"))) {
               this.data = String.valueOf(
                  new StringBuilder().append(this.data).append("\n{").append(var1).append("|").append(var3).append("}")
               );
               
            } else {
               this.data = String.valueOf(
                  new StringBuilder().append("{").append(var1).append("|").append(var3).append("}")
               );
            }

            return true;
         }
      } catch (Exception var6) {
         var6.printStackTrace();
         return false;
      }
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public void push() {
      try {
         boolean var11 = false /* VF: Semaphore variable */;

         try (PrintWriter var1 = new PrintWriter(this.file)) {
            var11 = true;
            String var3 = this.data;
            var3 = var3.replaceAll("}\n", "}");
            var3 = var3.replaceAll("}", "}\n");
            var1.println(var3);
            var11 = false;
         }

      } catch (Exception var16) {
         var16.printStackTrace();
         return;
      }

               ;
      
   }

   public String decode(Object var1) {
      return this.instance.getSerializer().getStringFromObject(var1);
   }

   public void pull() {
      String var1 = "";

      label26: {
         try {
            FileInputStream var2 = new FileInputStream(this.file);
            BufferedReader var3 = new BufferedReader(new InputStreamReader(var2));
            StringBuilder var5 = new StringBuilder("");

            String var4;
            while (((var4 = var3.readLine()) != null)) {
               var5.append(var4);
               
            }

            var1 = String.valueOf(var5);
            var2.close();
            String var6 = String.valueOf(var5);
            var1 = var6;
         } catch (Exception var7) {
            break label26;
         }

      }

      this.data = var1;
   }

   public Object encode(String var1) {
      return this.instance.getSerializer().getObjectFromString(var1);
   }

   public ArrayList<Type> getObjects() {
      String[] var1 = this.data.split("}");
      ArrayList var2 = new ArrayList();
      String[] var3 = var1;
      int var4 = var1.length;
      int var5 = 0;

      while (((var5) < (var4))) {
         String var6 = var3[var5];
         var2.add(var6.split("\\{")[1]);
         var5++;
         
      }

      ArrayList var7 = new ArrayList();
      Iterator var8 = var2.iterator();

      while ((var8.hasNext())) {
         String var9 = (String)var8.next();
         var7.add(new Type(var9.split("\\|")[0], var9.split("\\|")[1], this.instance));
         
      }

      return var7;
   }

   public DataBase(File var1, FireDB var2) {
      this.data = "";
      this.file = var1;
      this.instance = var2;
      this.pull();
   }

}

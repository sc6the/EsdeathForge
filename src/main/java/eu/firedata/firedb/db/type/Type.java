package eu.firedata.firedb.db.type;

import eu.firedata.firedb.FireDB;

public class Type {
   private String name;
   private FireDB instance;
   private String val;

   public String getname() {
      return this.name;
   }

   public Object getObject() {
      return this.instance.getDataBase().encode(this.val);
   }

   public Type(String var1, String var2, FireDB var3) {
      this.name = var1;
      this.val = var2;
      this.instance = var3;
   }

   public void editTo(Object var1) {
      this.instance.getDataBase().saveObject(this.name, var1);
      this.val = this.instance.getSerializer().getStringFromObject(var1);
   }
}

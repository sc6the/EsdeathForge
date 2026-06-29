package eu.firedata.firedb;

import eu.firedata.firedb.db.DataBase;
import eu.firedata.firedb.serializer.Serializer;
import java.io.File;

public class FireDB {
   private Serializer serializer;
   private File databaseFile;
   private DataBase dataBase;
   private FireDB instance;

   public FireDB(String var1, String var2) throws Exception {
      this.databaseFile = new File(String.valueOf(new StringBuilder().append(var1).append(var2).append(".firedb")));
      if (!(this.databaseFile.exists()) || !(this.databaseFile.isFile())) {
         this.databaseFile.createNewFile();
      }

      this.dataBase = new DataBase(this.databaseFile, this);
      this.serializer = new Serializer();
      this.instance = this;
   }

   public FireDB getCurrentInstance() {
      return this.instance;
   }

   public DataBase getDataBase() {
      return this.dataBase;
   }

   public Serializer getSerializer() {
      return this.serializer;
   }

}

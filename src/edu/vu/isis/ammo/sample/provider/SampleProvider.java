// THIS IS GENERATED CODE, YOU SHOULD COPY THIS FOR YOUR HAND EDITS
package edu.vu.isis.ammo.sample.provider;

import android.content.Context;

/**
 * Implements and overrides those elements not completed
 * 
 * @author <yourself>
 *    
 */
public class SampleProvider extends SampleProviderBase {
   
   protected class SampleCpDatabaseHelper extends SampleProviderBase.SampleDatabaseHelper {
      protected SampleCpDatabaseHelper(Context context) { super(context, SampleSchema.DATABASE_VERSION); }

/**
   @Override
   protected void preloadTables(SQLiteDatabase db) {
      db.execSQL("INSERT INTO \""+Tables.*_TBL+"\" (" + *Schema.*+") "+"VALUES ('" + *TableSchema.* + "');");
   }
*/
   }
   
   // ===========================================================
   // Content Provider Overrides
   // ===========================================================

   @Override
   public boolean onCreate() {
       super.onCreate();
       this.openHelper = new SampleCpDatabaseHelper(getContext());
           
       return true;
   }

   @Override
   protected boolean createDatabaseHelper() {
      return false;
   }
   
}
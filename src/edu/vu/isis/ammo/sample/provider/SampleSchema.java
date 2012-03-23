
package edu.vu.isis.ammo.sample.provider;

public class SampleSchema extends SampleSchemaBase {

   public static final int DATABASE_VERSION = 1;

      public static class LocationTableSchema extends LocationTableSchemaBase {

         protected LocationTableSchema() { super(); }
      /**
        Add relation constants as appropriate.
        i.e.
        public static final String <NAME> = "<sponsor>.provider.<name>.<table>.action.<NAME>";
        e.g.
        public static final String CONSTANT = "edu.vu.isis.ammo.sample.provider.sample_cp.pli.action.CONSTANT";

        public static final String PRIORITY_SORT_ORDER = 
               PliTableSchemaBase.EXPIRATION + " DESC, " +
               PliTableSchemaBase.MODIFIED_DATE + " DESC ";
      */
      }

      
}
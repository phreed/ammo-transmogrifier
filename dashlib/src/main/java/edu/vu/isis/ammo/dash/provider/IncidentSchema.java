/*Copyright (C) 2010-2012 Institute for Software Integrated Systems (ISIS)
This software was developed by the Institute for Software Integrated
Systems (ISIS) at Vanderbilt University, Tennessee, USA for the 
Transformative Apps program under DARPA, Contract # HR011-10-C-0175.
The United States Government has unlimited rights to this software. 
The US government has the right to use, modify, reproduce, release, 
perform, display, or disclose computer software or computer software 
documentation in whole or in part, in any manner and for any 
purpose whatsoever, and to have or authorize others to do so.
*/
package edu.vu.isis.ammo.dash.provider;

public class IncidentSchema extends IncidentSchemaBase {

   public static final int DATABASE_VERSION = 4;

      public static class MediaTableSchema extends MediaTableSchemaBase {

         protected MediaTableSchema() { } // no instantiation

	 public static final String IMAGE_DATA_TYPE = "image/jpeg";
         public static final String AUDIO_DATA_TYPE = "audio/basic";
         public static final String TEXT_DATA_TYPE = "text/plain";
         public static final String VIDEO_DATA_TYPE = "video/3gpp";
         public static final String TEMPLATE_DATA_TYPE = "text/template";
      }    
      public static class EventTableSchema extends EventTableSchemaBase {

         public static final String TIGR_TOPIC = "ammo/edu.vu.isis.ammo.map.object";
         protected EventTableSchema() { super(); }

         public static final int STATUS_DRAFT = 1;
         public static final int STATUS_LOCAL_PENDING = 2;
         public static final int STATUS_SENT = 3;
         public static final int _DISPOSITION_DRAFT = 4;
         public static final int _DISPOSITION_LOCAL_PENDING = 5;
      }    
      public static class CategoryTableSchema extends CategoryTableSchemaBase {

         public static final String RELOAD = "edu.vu.isis.ammo.dash.provider.incident.category.action.RELOAD";
         public static final String RELOAD_FINISHED = "edu.vu.isis.ammo.dash.RELOAD_FINISHED";
         
         protected CategoryTableSchema() { super(); }
      }    
}

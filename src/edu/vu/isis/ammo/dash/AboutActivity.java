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
package edu.vu.isis.ammo.dash;


import android.os.Bundle;
import android.app.Activity;

import edu.vu.isis.ammo.dash.R;


/**
 * Show details about the app, such as version number.
 */
public class AboutActivity extends Activity
{
    //public static final Logger logger = LoggerFactory.getLogger(AboutActivity.class);
//    private static final String TAG = "Dash:About";

    @Override
        public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.about_activity);
    }

    @Override
        public void onStart() {
        super.onStart();	
    }

    @Override
        public void onStop() {
        super.onStop();
    }

    @Override
        public void onDestroy() {
        super.onDestroy();
    }
}

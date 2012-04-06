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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used for logging the workflow of the app. Any class can log to this workflow.
 * @author demetri
 *
 */
public class WorkflowLogger {
	public static final Logger logger = LoggerFactory.getLogger("class.Workflow");
		
	public static void log(String message) {
		logger.debug(message);
	}
}

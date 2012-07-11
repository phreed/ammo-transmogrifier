
Vanderbilt Dash
===============

Description
-----------

The Dash application (formerly QuickReports) allows the end-user to quickly 
create spotreport-like notices with attached media and broadcast 
them to other users via a subscription mechanism in the AMMO infrastructure.

The primary purpose of this project is to illustrate the proper use 
of the Ammo middleware.


Installation
------------

The install can be performed using maven from the top level directory.
mvn install;
mvn --projects dash android:deploy

Usage
-----
	Prerequisites
	------------------------
	Before using this application, please make 
	sure that the AmmoCore is started and is connected 
	to the Gateway service. Please see README.


	Starting Dash
	------------------------
	Go to <Home> (press Home button)->All Apps. 
	Select the Dash icon.


	Create a Report
	------------------------
	1. Enter (optional) text comments in the textbox at the top of the screen
	2. Optionally attach media by pressing one of the media buttons below the textbox.  The buttons will allow you to capture media (picture, video, audio) via medium-appropriate applications, and will return you to the main screen when done.
	3. Note that only one media item may be attached to each report.
	9. Press <Post> 
	

	View Report Queue
	------------------------
	1. The "Queue" bar at the bottom of the Dash screen can be dragged upwards to show the queue of pending messages. 
	2. When you are done, close the queue viewer by dragging the Queue bar back to the bottom of the screen.


	General Notes
	------------------------
	- Each dash report can contain one comment and one media item.
	- Timestamp and location are recorded at the time the report is posted.
	- The only form currently supported is MEDEVAC.
	- A browser is available to view all reports currently on the device (both received and queued reports). 
	- From the browser, media items attached to reports can be viewed by long-pressing on a report and then selecting the "Preview" option.
	- Selecting the Queue bar on the main screen will show all reports enqueued to the Gateway.

	Media Specs
	------------------------
	- Audio recordings are limited to 30 seconds to save bandwidth.
	- Video recordings are limited to 60 seconds to save bandwidth.


	


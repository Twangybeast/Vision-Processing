# Vision-Processing

An old vision processing algorithm I made for robotics competitions. This code is no longer maintained.

The old readme is information is as follows:

Main Classes:  
PiTest - Takes a picture and processes it, printing the results to the console  
Test - Should no longer be used, replaced by PictureController  
VisionProcessing - Code to put on the raspberry pi during competitions, takes a picture and saves to network tables  
PictureController - Best class here, automatically downloads pictures, choose an initial picture to start from with the console, and browse with arrow keys.  Automatically launches the custom GUI.  
  
  
Note:  
For PictureController, the blue square represents where the robot will aim at. The red lines show where the corners of the target are, according to the computer.  

# MobileServiceTool  
this branch is for android part code  
The code for the web pages is in here:  
https://github.com/chinapumpkin/web
##Purpose    
Purpose of this study is to design teleguidance for elders and study the current tech- nology that could help senior walking around the zone around their house and get necessary survival assistance.   
##Components   
1. Android phone   
2. Pupil headset  
    a smart glasses which capture senior citizen’s font view.  
3. Indicator-based Glasses  
    The indicator-based glasses can receive the bit array data through Bluetooth connection from Android application. By analysis the data from the smart phone, the colors, frequency, brightness and blinking time of light in the indicator-based glasses can be set in time.  
4. OldBirds  
   
5. Web server part   
    The web server part is transfer station between Android phone and OldBirds part.  
##Function  
1. provide guidance for multiple seniors based on  their location.    
2. support real-time streaming function.    
##Requirements
* up Android 4.1  
    Because I use MediaCodec API and the buffer-to-buffer method to encode video data

##Further Development
1.The application need to support two cameras: world camera and eye camera of Pupil headset, recording and transferring. Therefore, world camera and eye camera need to be connected by a USB hub.    
2.I also recommend that someone combined Pupil headset and Indicator-based glasses in hardware layer. The Bluetooth connection consumes quite much electricity. If the Indicator-based glasses can transfer data through USB the usability of this system will be improved.

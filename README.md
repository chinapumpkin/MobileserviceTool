# MobileServiceTool
this branch is for android part code  
The code for the web pages is in here:  
https://github.com/chinapumpkin/web
##Function
##Requirements
* up Android 4.1
    Because I use MediaCodec API and the buffer-to-buffer method to encode video data
```java
//Callback of all speeches
soapbox.onreceiveallspeeches = function (speeches) {
    //It would be an array like below, noted  that the value of "submit_info" field would preserve as whatever you send when you submit
    //   [ {
    //       "speech_id": "10/09/2014 12:00", "submit_info": {"lefttime": XXX, "topic": XXX ...}
    //     },
    //     {
    //        "speech_id": "10/09/2016 12:00", "submit_info": {"lefttime": XXX, "topic": XXX ...}
    //     },
    //      ...
    //   ]
    console.log(speeches);
```
##Further Development
1.The application need to support two cameras: world camera and eye camera of Pupil headset, recording and transferring. Therefore, world camera and eye camera need to be connected by a USB hub.
2.I also recommend that someone combined Pupil headset and Indicator-based glasses in hardware layer. The Bluetooth connection consumes quite much electricity. If the Indicator-based glasses can transfer data through USB the usability of this system will be improved.

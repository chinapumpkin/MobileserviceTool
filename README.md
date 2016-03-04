# MobileServiceTool
this branch is for android part code  
The code for the web pages is in here:  
https://github.com/chinapumpkin/web
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

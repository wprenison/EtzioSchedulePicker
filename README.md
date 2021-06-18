# dialTimePicker
A custom time picker library for Android.
<br>
This library was forked from [ugurtekbas's library.](https://github.com/ugurtekbas/dialTimePicker)
<br>
Calculations in the main class mostly based on [erz05's view.](https://github.com/erz05/TimePicker)

<H2>Images</H2>
<img width="270px" height="585" src="/images/Etzio1.jpg" />
<img width="270px" height="585" src="/images/Etzio2.jpg" />
<br>

<H2>Usage</H2>
Gradle Import:

Add the specific repository to your build file:
```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}
```
Add the dependency in your build file (do not forget to specify the correct qualifier, usually 'aar'):
```groovy
dependencies {
    compile 'https://github.com/wprenison/EtzioTimePicker:{Latest Version}'
}
```

```xml

<picker.ugurtekbas.com.Picker.Picker
        android:id="@+id/picker"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />

<!-- or with some attributes in xml -->        
<picker.ugurtekbas.com.Picker.Picker
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/amPicker"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_centerInParent="true"
        app:hourFormat="false"
        app:canvasColor="#ffffff"
        app:textColor="#000000"
        app:trackSize="20dp"
        app:dialRadius="60dp"
        />
```

```java
Picker picker = (Picker) findViewById(R.id.picker);
//or
Picker picker = new Picker(context);

//Set background color
picker.setCanvasColor(Color.WHITE);
//Set dial color
picker.setDialColor(Color.ORANGE);
//Set clock color
picker.setClockColor(Color.CYAN);
//Set clock face color
picker.setClockFaceColor(Color.WHITE);
//Set text color
picker.setTextColor(Color.BLACK);
//Enable 24 or 12 hour clock
picker.setHourFormat(true);
//Get current hour
picker.getCurrentHour();
//Get current minutes
picker.getCurrentMin();
//Diasble/enable the picker
picker.setEnabled(false);
//Set dial's size
picker.setTrackSize(20);
//Set adjuster's size
picker.setDialRadiusDP(60);
//Initialize picker's time
picker.setTime(9,0);
//Set picker's time with calendar object
picker.setTime(Calendar.getInstance());
//Set dialAdjust to adjust time by touching on clock's dial
picker.setDialAdjust(true);
//Set TimeChangedListener
picker.setTimeChangedListener(this);

//TimeChangeListener method
public void timeChanged(Date date){
}

```
<H2>License</H2>
    Copyright 2021 Weylin Renison

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

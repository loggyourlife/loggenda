<img src="/images/loggenda.gif" width="400px" alt="Loggenda Logo" /> 

[![Download](https://api.bintray.com/packages/logg/loggenda/loggenda/images/download.svg) ](https://bintray.com/logg/loggenda/loggenda/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache-blue.svg)](https://github.com/loggyourlife/loggenda/blob/master/LICENSE)


**Loggenda** is a customizable event calendar widget for Android. 
The widget allows you to track past activities more effectively. It is suitable for use now and past dates. No improvements have been made yet to use for future dates. It is written entirely with **Kotlin**.


It was originally designed and developed for use in [Logg Android App](https://play.google.com/store/apps/details?id=com.digieggs.deathstar.logg). After that, We thought we could make it better together. We made it customizable and published it as open source. 


## Getting Started
Add the repository to your project **build.gradle**:
``` gradle
repositories {
  jcenter()
  maven {
    url "https://jitpack.io"
  }
}
```
And add the library to your module **build.gradle**:
``` gradle
dependencies {
  implementation 'com.logg.loggenda:loggenda:0.1.0'
}
```
Loggenda uses [Joda-Time](https://github.com/JodaOrg/joda-time).
## How to use?
Add the widget your layout. The width of the widget must be full screen(match_parent or match_constraint) at this time.
``` xml
<com.logg.loggenda.widget.Loggenda
        android:id="@+id/loggenda"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```
After show the widget with Joda-Time "LocalDate".
``` kotlin
loggenda.show(LocalDate.now(), supportFragmentManager)
```
For advanced use, you can check [the sample](https://github.com/loggyourlife/loggenda/blob/master/app/src/main/java/com/logg/loggenda/sample/MainActivity.kt).

---

Thanks to [Didem Erişkin](https://www.behance.net/eriskindidem) and [Serkan Mercan](http://serkanmercan.com) for logo design and animation.

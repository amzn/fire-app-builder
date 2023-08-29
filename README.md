## Amazon Appstore Fire App Builder 

Fire App Builder provides a Java-based framework that you can use to easily and quickly build streaming media Android apps for Amazon Fire TV.


## Prerequisites

* [**Android Studio**](http://developer.android.com/sdk/index.html)
* [**Java Development Kit (JDK) 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Virtual (TV emulator on Android Studio) or physical device ([**Fire TV**](https://www.amazon.com/firetv) or [**Fire TV Stick**](https://www.amazon.com/firetvstick))
* **Media feed with necessary elements.** If you want to use your own custom media feed, you will need a media feed (in either JSON or XML format) with video assets as well as the following feed elements: title, ID, description, URL, card image, and background image. Any video format supported by [Exoplayer](https://google.github.io/ExoPlayer/supported-formats.html) is compatible with Fire App Builder



## ✅ Features

![Demo Screen](../assets/fire-app-splash.jpg?raw=true)
* **Five screens**: Splash screen, Home (two layouts), Content Details, Content Renderer, and Search.
* **Search functionality and search results**: Text search within your app. Also includes intent filters to integrate with the global Fire TV search if your media is integrated into the Amazon Catalog.
* **Exoplayer-based Amazon media player for streaming media**: The media player includes closed [caption support](https://developer.amazon.com/docs/fire-app-builder/caption-support.html) (in-band and out-band), HTTP Live Streaming (HLS), bandwidth settings, and more.
* **Components for ads, analytics, authorization, and in-app purchasing**: More than 10 components that you can easily plug into your app and configure through XML files. Some of these components include Amazon in-app purchases, Login with Amazon, Facebook Login, Omniture Analytics, Flurry Analytics, Adobe Pass Authentication, Freewheel ads, and VAST 2.0 ads.



## 💻 Building the Fire App Builder

1. Clone the following repository:
    `git clone git@github.com:amzn/fire-app-builder.git`
2. Start Android Studio
3. At the Welcome screen, click **Open an existing Android Studio project**.
4. Browse to the directory where you downloaded the fire-app-builder GitHub project. Inside the Fire App Builder project folder, select the **Application** folder, and then click **Open**.
5. Build → Make Project
    1. Try Build → Clean Project and Rebuild Project if nothing happens
6. Run the app 
7. Within your emulator, under Your apps, you will see the Fire App Builder. When you click on it, it will load the app!     

![Demo Screen](../assets/fire-app-builder.gif?raw=true)

## 🔥 Using Fire App Builder

Change the appearance.

* Change app loco, icon, font
* Change different screens (splash, home)
* Change homepage layout, sidebar


Add [components](https://developer.amazon.com/docs/fire-app-builder/interfaces-and-components.html) for more functionality.  No need to write your Java code, just customize various string values that have been extracted out of the code into XML files.

* Ads Interface (`IAds`): Used for displaying ads to users
* Analytics Interface (`IAnalytics`): Used for implementing analytics
* Authentication Interface (`IAuthentication`): Used for authenticating or authorizing user access to media
* Purchase Interface (`IPurchase`): Used for in-app purchasing
* UAMP (`UAMP`): Used for playing media. (UAMP stands for Universal Android Media Player.)




## 🆘 Get support

If you found a bug or want to suggest a new [feature/use case/sample], please [file an issue.](https://github.com/amzn/fire-app-builder/issues)
If you have questions, comments, or need help with code, we're here to help:

* on Twitter at [@AmazonAppDev](https://twitter.com/AmazonAppDev)
* on Stack Overflow at the [amazon-appstore](https://stackoverflow.com/questions/tagged/amazon-appstore) tag

Sign up to [stay updated with the developer newsletter.](https://m.amazonappservices.com/subscribe-newsletter)

## ✍️ Author
[@_yoolivia](https://twitter.com/_yoolivia?)

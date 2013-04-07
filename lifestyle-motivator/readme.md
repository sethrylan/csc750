
### Introduction ###

The purpose of the lifestyle motivator is to help an individual maintain a healthy lifestyle.
Here context is used to suggest what activity to perform; to suggest 1) indoor or outdoor activities depending on the weather and 2) activities depending on the facilities available nearby.

Click the "Start Background Service" button to be notified of nearby indoor or outdoor activities such as parks and gyms, depending on the weather conditions. The service will run in the background with minimal GPS and network activity, even when the application is exited. Select the notification or reopen the application to see available activities.

Supporting Tropos models are in the /models folder.


### Installation ###

Add local.properties file with the contents to the project's root directory
'''
sdk.dir=<ANDROID_SDK_LOCATION>
'''

e.g., 
'''
sdk.dir=C:/adt-bundle-windows-x86_64/sdk
'''

From the project's root project directory, run
'''
gradlew clean installDebug
'''

The application will be installed as "Lifestyle Motivator".

To uninstall:
'''
gradlew uninstallDebug
'''



### Developement Setup ###

Requires gradle 1.3.
For Eclispe integration, the Android/Google API jars must be manually added to the classpath.

Run unit tests with
'''
gradle unitTest
'''
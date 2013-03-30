TODO:

Preferences:
http://stackoverflow.com/questions/2857121/where-to-store-android-preference-keys
http://stackoverflow.com/questions/5246741/android-get-keys-from-preferences-xml

The purpose of the lifestyle motivator is to help an individual maintain a healthy lifestyle.
In the current setting, many users procrastinate or find excuses not to work out. In many
cases, a user might want workout, but can't find the right activity or a right buddy. The
lifestyle motivator application will suggest a user 1) activities to perform, and 2) buddies to
perform those activities with.



There are several such applications. However, many of them rely on a centralized archi-
tecture (server) assumed to contain information about all users. Our target is to build the
application in a way that respects a user's autonomy and to promote heterogeneity across
users. There are several ways context can influence this application.

Context can be used to decide when to suggest and activity to the user. For example,
suggest depending on 1) how active the user was recently 2) how is the user feeling
(physical activity can be a natural boost to mood), and so on.

Context can be used to suggest what activity to perform. For example, to suggest 1)
indoor or outdoor activities depending on the weather, 2) activities depending on the
facilities available nearby, and so on.


Context can also be used to suggest a buddy. For example, 1) those who are not busy
and plan to participate in an activity, 2) those who are easy to coordinate with (e.g.,
people nearby), 3) those who are skilled at the activity, and so on.


### Installation

Add local.properties file with the contents
'''
sdk.dir=<ANDROID_SDK_LOCATION>
'''

From the root project directory, run
'''
gradlew installDebug
'''
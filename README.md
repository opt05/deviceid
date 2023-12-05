# Device Info

[![Android CI - Master](https://github.com/opt05/deviceid/actions/workflows/android-master.yml/badge.svg)](https://github.com/opt05/deviceid/actions/workflows/android-master.yml) [![Android CI - Develop](https://github.com/opt05/deviceid/actions/workflows/android-develop.yml/badge.svg)](https://github.com/opt05/deviceid/actions/workflows/android-develop.yml)

This open source android project provides a quick panel to display the following information quick; IMEI/MEID, Device Model, Android ID, Wi-Fi MAC, Bluetooth MAC, Android Version, Android Build Version and Screen Density. More features to come soon.

<a href='https://play.google.com/store/apps/details?id=com.cwlarson.deviceid&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="200"/></a>

## License

Licensed under the MIT License. For complete licensing information see [LICENSE.md](https://github.com/opt05/deviceid/blob/master/LICENSE.md) in this project.

## What is this repository for?

This is a repository for the Device Info Android app that displays specific info about your Android device

## Build Tools

* Android Studio Hedgehog (Gradle 8.2)
* AndroidX/Jetpack
* Android 5.0 (API 21) or above (built against 14/API 34)
* Kotlin 1.9

## Android Permissions

1. ACCESS_COARSE_LOCATION - to read Wi-Fi SSID and BSSID
2. ACCESS_FINE_LOCATION - to read Wi-Fi SSID and BSSID
3. ACCESS_NETWORK_STATE - to read SIM state
4. ACCESS_WIFI_STATE - to read Wi-Fi MAC address
5. BLUETOOTH - to read Bluetooth MAC address
6. BLUETOOTH_CONNECT - to read Bluetooth Hostname
7. READ_GSERVICES - to read Google Services Framework (GSF) ID
8. READ_PHONE_STATE - to read IMEI/MEID and voicemail number
9. READ_PHONE_NUMBER - to read Phone number

## Bugs and Features

If you find a bug or need a feature, please report it to [https://github.com/opt05/deviceid/issues](https://github.com/opt05/deviceid/issues). Please keep in mind that this is a free software with very few volunteers working on it in their free time. You should also check if the fix or feature is already mentioned in the issues list. In many cases a stacktrace from crash is appreciated. If you can't get it via IDE, you can try aLogcat.

## Contribution guidelines

Contribution is very welcome! If you want to add a feature or fix a bug yourself, please fork the repository, do your changes and send a pull request

## Who do I talk to?

Repo owner

---

<sup>Google Play and the Google Play logo are trademarks of Google LLC.</sup>

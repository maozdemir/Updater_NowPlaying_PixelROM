
SET app_package=com.contested.zeroiq.sense
SET dir_app_name=NowPlaying
SET MAIN_ACTIVITY=UpdaterActivity


SET ADB=adb
SET ADB_SH=adb:: shell su -c

set path_sysapp=/system/priv-app
SET apk_host=%CD%\app\build\outputs\apk\debug\app-debug.apk
set apk_name=%dir_app_name%.apk
set apk_target_dir=%path_sysapp%/%dir_app_name%
set apk_target_sys=%apk_target_dir%/%apk_name%


DEL  %apk_host%
call gradlew assembleDebug

:: Install APK: using adb su
::%ADB_SH% mount -o rw,remount /system
%ADB_SH% chmod 777 /system/lib/
::%ADB_SH% mkdir -p /sdcard/tmp
::%ADB_SH% mkdir -p %apk_target_dir%
adb push %apk_host% %apk_target_sys% 
::%ADB_SH% mv /sdcard/tmp/%apk_name% %apk_target_sys%
::%ADB_SH% rmdir /sdcard/tmp

:: Give permissions
%ADB_SH% chmod 755 %apk_target_dir%
%ADB_SH% chmod 644 %apk_target_sys%

::Unmount system
%ADB_SH% mount -o remount,ro /

:: Stop the app
%ADB_SH% shell am force-stop %app_package%

:: Re execute the app
%ADB% shell am start -n \"%app_package%/%app_package%.%MAIN_ACTIVITY%\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
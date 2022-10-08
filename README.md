# Flashlight-Tiramisu
This app allows you to adjust the flashlight brightness on Android 13. 

Android 13 introduces the getTorchStrengthLevel and turnOnTorchWithStrengthLevel methods to the CameraManager class. The first method returns the brightness level of the LED flash, while the second method sets the brightness level of the LED flash from a minimum of ‘1’ to a maximum determined by the hardware. Previously, apps could only toggle the flashlight on or off using the setTorchMode API, but with these new APIs in Android 13, apps can more granularly control the flashlight brightness.

There is one caveat to this feature, however. Not every device running Android 13 will support controlling the flashlight brightness. Apps can determine if a device supports flashlight brightness control by using CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL. If a value greater than 1 is returned, then that’s the maximum brightness level that the LED flash can be set to. If the value is equal to 1, then the device doesn’t support flashlight brightness control.

The reason support for this feature will be limited is that it will require an update to the camera hardware abstraction layer (HAL). A HAL is the software that defines the interface between the OS and the underlying hardware. In order for the OS to control the LED flash hardware, there needs to be a HAL that defines what commands the OS can issue to control the hardware. On Android, the camera provider HAL allows for direct control of the flash unit of camera devices, a feature that was introduced with version 2.4 of the HAL. The camera provider HAL enumerates and opens individual camera devices, while the camera device HAL is used to operate individual camera devices.

Description taken from [this article](https://blog.esper.io/android-13-flashlight-brightness-control/)

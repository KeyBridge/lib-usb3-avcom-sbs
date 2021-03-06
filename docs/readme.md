# Avcom SBS Sensor Board Configuration Notes

## Configure Linux USB Port Permissions

History
 - 04/25/2014 - updated and extended
 - 05/26/2010 - created

In Ubuntu (and other Debian derivatives) there is a legacy UDEV rule that grabs all FTDI devices.
Linux thinks this is a "Watts Up? Pro" Devices that measure power at the plug level

    SUBSYSTEM=="tty", SUBSYSTEMS=="usb", ATTRS{idVendor}=="0403", ATTRS{idProduct}=="6001", ATTRS{serial}=="A80?????", ENV{UPOWER_VENDOR}="Watts Up, Inc.", ENV{UPOWER_PRODUCT}="Watts Up? Pro", ENV{UP_MONITOR_TYPE}="wup"

Fix this by removing this rule and resetting udev or rebooting the system. All should be fine.

1) Remove the upower rule

    % sudo rm /lib/udev/rules.d/95-upower-wup.rules

2) Add a permissive udev rule

    98-avcom-sensor.rules
    SUBSYSTEM=="tty", SUBSYSTEMS=="usb", ATTRS{idVendor}=="0403", ATTRS{idProduct}=="6001", ATTRS{serial}=="A80?????", MODE="0666", ENV{SENSOR_VENDOR}="Avcom of VA, Inc.", ENV{SENSOR_PRODUCT}="Spectrum Analyzer", ENV{SENSOR_TYPE}="sbs"

3) Restart udev

    % sudo restart udev

4) unplug and re-insert the USB cable

The file /dev/ttyUSB0 port should have permissions 0666.

FINALLY, and this is important, add dialout to the user group or add a GROUP entry to the udev command above

   usermod -a -G dialout {user}

For example, we include a rule for a certain device (a USB-serial device) that identifies itself (through USB device protocol) with the vendor ID 0403 and product ID 6001 to use the group "users" and the permissions mode 0666 via this rule in /etc/udev/rules.d/40-persistent-ftdi.rules:

    ATTRS{idVendor}=="0403",ATTRS{idProduct}=="6001",MODE="0666",GROUP="users"

Device vendor and product identifiers etc. can usually be seen in the kernel log messages viewable with the dmesg command.

Find the udev properties that are unique to each FTDI device. In the terminal, type this:
$ udevadm info -a -p $(udevadm info -q path -n ttyUSB0) | egrep -i "ATTRS{serial}|ATTRS{idVendor}|ATTRS{idProduct}" -m 3

The output will look something like this:

    ATTRS{idVendor}=="0403"
    ATTRS{idProduct}=="6001"
    ATTRS{serial}=="A80080VO"

Record all the values in the quotes – you’ll need them in the next steps.

Step 4: Create the udev file.

In the terminal, create a file named 10-ftdi.rules in /etc/udev/rules.d with 644 permissions.
$ sudo touch /etc/udev/rules.d/10-ftdi.rules && sudo chmod 644 /etc/udev/rules.d/10-ftdi.rules
Open the file with your favorite text editor:

    $ sudo vim /etc/udev/rules.d/10-ftdi.rules

and copy this line in:

    BUS=="usb", SYSFS{idProduct}=="IDPRODUCT", SYSFS{idVendor}=="IDVENDOR", SYSFS{serial}=="SERIAL", NAME="DEVICENAME"

Step 5: Remove your FTDI cable and plug it back in (this will remount it using your new rule). It should now be mounted in /dev/DEVICENAME, where DEVICENAME is what you put in the NAME field in the udev rule.

Step 6: Repeat this process for your other FTDI devices. Consecutive udev rules can be placed on the next line in the same file – no need to create another one.

This quick and easy trick lets you assign memorable mountpoints to otherwise identical devices – a godsend when you’ve got a few too many to work with.

Going forward, check and remove any competing rules that match the FTDI port as follows:

    % udevadm info -a -p $(udevadm info -q path -n /dev/ttyUSB0)

Look for the following configuration values

    ATTRS{idVendor}=="0403"
    ATTRS{idProduct}=="6001"
    ATTRS{serial}=="A80080VO" (optional)

Basically, when a device is sort of a non-standard device (this is a little hard for me to explain), in particular not a usb file system, the mount rules in /etc/udev/rules.d/40-basic-permissions.rules apply. Now, be aware that the syntax changed going from 7.10 to 8.04, and right now I can't recall the pre-8.04 syntax. To fix my problem, I created a file called 41-cvs-permissions.rules - just be sure the number is above 40 and doesn't conflict with an existing file. Note that I created this file while running as root, so be sure to match it's permissions to those of the 40-basic-permissions.rules file.

My file contains the following - hopefully you'll now see the correlation of the numbers:

# USB devices (usbfs replacement)

    SUBSYSTEM=="usb", ENV{DEVTYPE}=="usb_device",SYSFS{idVendor}=="0dca" , SYSFS{idProduct}=="0027", MODE="0666"
    SUBSYSTEM=="usb", ENV{DEVTYPE}=="usb_device",SYSFS{idVendor}=="167b" , SYSFS{idProduct}=="0101", MODE="0666"

It's also quite possible that the USB device is getting mounted as owned by root, particularly if this is a somewhat unique device. Working with hacked CVS one time use cameras I had a similar problem. The solution was to create another permissions file which mounted the device for world access (I'm the only one on my PC - you could make it mount to a certain group and put yourself in that group).

In order to try to set that up for you to test, please be sure the device is plugged in and then do the following in a terminal window and post the output back here.

(1) lsusb

Hopefully you will see your device listed, though it may just be a blank name.

(2) For that device:

lsusb -v -d xxxxx where the xxxxx are the last 2 sets of numbers on the list.

3) Watch syslog and look for the device attachment event

    % tail -f syslog

    usb 7-2: new full-speed USB device number 3 using uhci_hcd
    ftdi_sio 7-2:1.0: FTDI USB Serial Device converter detected
    usb 7-2: Detected FT232RL
    usb 7-2: Number of endpoints 2
    usb 7-2: Endpoint 1 MaxPacketSize 64
    usb 7-2: Endpoint 2 MaxPacketSize 64
    usb 7-2: Setting MaxPacketSize 64
    usb 7-2: FTDI USB Serial Device converter now attached to ttyUSB0
    mtp-probe: checking bus 7, device 3: "/sys/devices/pci0000:00/0000:00:1d.1/usb7/7-2"
    mtp-probe: bus: 7, device: 3 was not an MTP device

$ lsusb

    Bus 007 Device 003: ID 0403:6001 Future Technology Devices International, Ltd FT232 USB-Serial (UART) IC

$ lsusb -v -d 0403:6001

    Bus 007 Device 003: ID 0403:6001 Future Technology Devices International, Ltd FT232 USB-Serial (UART) IC
    Couldn't open device, some information will be missing
    Device Descriptor:
      bLength                18
      bDescriptorType         1
      bcdUSB               2.00
      bDeviceClass            0 (Defined at Interface level)
      bDeviceSubClass         0
      bDeviceProtocol         0
      bMaxPacketSize0         8
      idVendor           0x0403 Future Technology Devices International, Ltd
      idProduct          0x6001 FT232 USB-Serial (UART) IC
      bcdDevice            6.00
      iManufacturer           1
      iProduct                2
      iSerial                 3
      bNumConfigurations      1
      Configuration Descriptor:
        bLength                 9
        bDescriptorType         2
        wTotalLength           32
        bNumInterfaces          1
        bConfigurationValue     1
        iConfiguration          0
        bmAttributes         0xa0
          (Bus Powered)
          Remote Wakeup
        MaxPower               90mA
        Interface Descriptor:
          bLength                 9
          bDescriptorType         4
          bInterfaceNumber        0
          bAlternateSetting       0
          bNumEndpoints           2
          bInterfaceClass       255 Vendor Specific Class
          bInterfaceSubClass    255 Vendor Specific Subclass
          bInterfaceProtocol    255 Vendor Specific Protocol
          iInterface              2
          Endpoint Descriptor:
            bLength                 7
            bDescriptorType         5
            bEndpointAddress     0x81  EP 1 IN
            bmAttributes            2
              Transfer Type            Bulk
              Synch Type               None
              Usage Type               Data
            wMaxPacketSize     0x0040  1x 64 bytes
            bInterval               0
          Endpoint Descriptor:
            bLength                 7
            bDescriptorType         5
            bEndpointAddress     0x02  EP 2 OUT
            bmAttributes            2
              Transfer Type            Bulk
              Synch Type               None
              Usage Type               Data
            wMaxPacketSize     0x0040  1x 64 bytes
            bInterval               0


# References

http://aeturnalus.com/robotics/mapping-ftdi-to-files-with-udev/


# usb3-avcom-sbs

A Java device driver to access the AVCOM RSA-2500 SBS single board sensor.

Note: This device driver software enables device control and data retrieval.
This software does NOT provide for data interpretation, visualization or processing.
Those functions are left as exercises for the user.... (See the _References_
section below for some utilities).

## Background

The RSA-2500B-SBS is a compact (5”x7”) spectrum analyzer board manufactured
by [Avcom of Virginia](http://www.avcomofva.com). It is intended to be integrated into
other equipment; ie. inside a small satellite terminal, or as an integral part
of the satellite antenna itself.

The single board design requires only external +15-24VDC/9W and a wideband input signal
(5MHz to 2500MHz) and is available with USB, Ethernet and Serial communication ports.

This Java software library implements USB communication and control of the device.

# Overview

Avcom SBS devices use a FTDI UART integrated circuit chip to provide USB serial
communications. The **usb3-ftdi** driver library is used to read data from and
write data to the Avcom SBS device.

This library sits atop the **usb3-ftdi** driver, which itself sits atop the
**javax-usb3** system library, to implement device control and data parsing.

<pre>
    +----------------------------+
    |       usb3-avcom-sbs       |    Device control and data parsing
    +----------------------------+
    +----------------------------+
    |       usb3-ftdi            |    FTDI UART serial communications
    +----------------------------+
    +----------------------------+
    |       javax-usb3           |    USB system access
    +----------------------------+
</pre>

# How to Use

The Avcom SBS spectrum analyzer operates as follows:
  1) Write a setting configuration
    a) Write a data request
    b) Read a data response
    c) Repeat step (a) for another sweep or step (1) to change the configuration

All device operations are **asynchronous** and implemented through the
**AvcomSBS** utility class.

## Getting started

When the AvcomSBS software is initialized a default (full sweep) setting is
automatically written to the device and a thread is started to automatically
stream spectrum sensor data from the device and forward the data to all
registered listeners.

To receive spectrum sensor data register your application with the
**AvcomSBS** utility as a _IDatagramListener_ instance.

To write a settings configuration use the **AvcomSBS.setSettings(...)** method.

In your Avcom Controller instance:
```java
    // Get the FTDI port. See the usb3-ftdi library for details.
    FTDI ftdi = new FTDI(....
    AvcomSBS avcom = new AvcomSBS(ftdi);

    // Add your IDatagramListener instance.
    avcom.addListener( ... );

    // Start the Avcom device streaming thread.
    avcom.start();
    Thread.sleep(15000);

    // (Optionally) Set a new settings configuration
    avcom.setSettings(new SettingsRequest ...

    // Stop the Avcom device streaming thread when done.
    avcom.stop();
```

Separately, in your IDatagramListener instance:
```java
    public void onDatagram(IDatagram datagram) {
    System.out.println(datagram.toString()
    }
```

See the class _Test_AvcomSBS_ in the unit test directory for a complete example.


# Requirements / Dependencies

This project requires **javax-usb3**, which is a JNI wrapper for libusb 1.x and
with run-time implementations for Linux (actively supported) plus OSX and Windows
(cross compiled).

This project required **usb3-ftdi**, which is a Java device driver for FTDI UART
chips.

# License

Apache 2.0.

# References
- [Device User Guide](docs/Avcom RSA - User Guide (v6).pdf)
- [Device Spec. Sheet](docs/Avcom RSA-2500B-SBS_SpecSheet.pdf)
- [Device USB Protocol](docs/Avcom Remote Spectrum Analyzer Protocol.pdf)
- [GUI User Guide](docs/Avcom Master GUI User Guide (v3).pdf  )

# Contact

Contact [Key Bridge](http://keybridgeglobal.com) for more information about this
and related software.

Contact [Avcom of Virginia](http://www.avcomofva.com) for custom mounting configurations
for adapting the RSA-2500B-SBS into your system.
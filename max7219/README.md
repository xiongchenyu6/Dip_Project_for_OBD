# MAX7219 Driver

Interfacing LED matrix displays with the MAX7219 driver
[[PDF datasheet](https://raw.github.com/rm-hull/max7219/master/docs/MAX7219-datasheet.pdf)]
in Python using hardware SPI on the Raspberry Pi. The particular LED matrix I bought
can be acquired for a few pounds from
[Banggood](http://www.banggood.com/MAX7219-Dot-Matrix-Module-DIY-Kit-SCM-Control-Module-For-Arduino-p-72178.html?currency=GBP).
Likewise 7-segment displays are available from [Ali-Express](http://www.aliexpress.com/item/MAX7219-Red-Module-8-Digit-7-Segment-Digital-LED-Display-Tube-For-Arduino-MCU/1449630475.html).
There are many other outlets selling both types of devices on Ebay and other such places.

This library has recently had a major overhaul, and is not compatible with the earlier version.
It now supports:

* multiple cascaded devices
* LED matrix and seven-segement variants

![max7219 matrix](https://raw.githubusercontent.com/rm-hull/max7219/master/docs/images/devices.jpg)

### Python Usage

For the matrix device, initialize the `matrix` class:

```python
import max7219.led as led

device = led.matrix()
device.show_message("Hello world!")
```

For the 7-segment devce, initialize the `sevensegment` class:

```python
import max7219.led as led

device = led.sevensegment()
device.write_number(deviceId=0, value=3.14159)
```

The MAX7219 chipset supports a serial 16-bit register/data buffer which is
clocked in on pin DIN every time the clock edge falls, and clocked out on DOUT
16.5 clock cycles later. This allows multiple devices to be chained together.

When initializing cascaded devices, it is necessary to specify a `cascaded=...`
parameter, and generally methods which target specific devices will expect a
`deviceId=...` parameter, counting from zero.

For more information, see https://max7219.readthedocs.io/

![max7219 sevensegment](https://raw.githubusercontent.com/rm-hull/max7219/master/docs/images/IMG_2810.JPG)

### Pre-requisites

By default, the SPI kernel driver is **NOT** enabled on the Raspberry Pi Raspian image.
You can confirm whether it is enabled using the shell commands below:

    $ dmesg | grep spi
    [    3.769841] bcm2708_spi bcm2708_spi.0: master is unqueued, this is deprecated
    [    3.793364] bcm2708_spi bcm2708_spi.0: SPI Controller at 0x20204000 (irq 80)

And that the devices are successfully installed in _/dev_:

    $ ls -l /dev/spi*
    crw------- 1 root root 153, 0 Jan  1  1970 /dev/spidev0.0
    crw------- 1 root root 153, 1 Jan  1  1970 /dev/spidev0.1

If you have no _/dev/spi*_ files and nothing is showing using `dmesg` then this
implies the kernel SPI driver is not loaded. Enable the SPI as follows (steps
taken from https://learn.sparkfun.com/tutorials/raspberry-pi-spi-and-i2c-tutorial#spi-on-pi):

1. Run `sudo raspi-config`
2. Use the down arrow to select _9 Advanced Options_
3. Arrow down to _A6 SPI._
4. Select **yes** when it asks you to enable SPI,
5. Also select **yes** when it asks about automatically loading the kernel module.
6. Use the right arrow to select the **&lt;Finish&gt;** button.
7. Select **yes** when it asks to reboot.

![image](https://cloud.githubusercontent.com/assets/1915543/16681787/b615b20c-44ee-11e6-9533-b0dce2b007b1.png)

After rebooting re-check that the `dmesg | grep spi` command shows whether
SPI driver is loaded before proceeding.

### GPIO pin-outs

The breakout board has two headers to allow daisy-chaining:

| Board Pin | Name | Remarks     | RPi Pin | RPi Function      |
|----------:|:-----|:------------|--------:|-------------------|
| 1         | VCC  | +5V Power   | 2       | 5V0               |
| 2         | GND  | Ground      | 6       | GND               |
| 3         | DIN  | Data In     | 19      | GPIO 10 (MOSI)    |
| 4         | CS   | Chip Select | 24      | GPIO 8 (SPI CE0)  |
| 5         | CLK  | Clock       | 23      | GPIO 11 (SPI CLK) |

**NOTE:** See below for cascading/daisy-chaining, power supply and level-shifting.

### Installing the library

Install the latest version of the library directly from [PyPI](https://pypi.python.org/pypi?:action=display&name=max7219):

    $ sudo apt-get install python-dev python-pip
    $ sudo pip install max7219

Alternatively, clone the code from github:

    $ git clone https://github.com/rm-hull/max7219.git

Next, follow the specific steps below for your OS.

#### For Raspian:

    $ cd max7219
    $ sudo apt-get install python-dev python-pip
    $ sudo pip install spidev
    $ sudo python setup.py install

#### For Arch Linux:

    # cd max7219
    # pacman -Sy base-devel python2
    # pip install spidev
    # python2 setup.py install

### Cascading, power supply & level shifting

The MAX7219 chip supports cascading devices by connecting the DIN of one chip to the DOUT
of another chip. For a long time I was puzzled as to why this didnt seem to work properly
for me, despite spending a lot of time investigating and always assuming it was a bug in
code.

* Because the Raspberry PI can only supply a limited amount of power from the 5V rail,
  it is recommended that any LED matrices are powered separately by a 5V supply, and grounded
  with the Raspberry PI. It is possible to power one or two LED matrices directly from a
  Raspberry PI, but any more is likely to cause intermittent faults & crashes.

* Also because the GPIO ports used for SPI are 3.3V, a simple level shifter (as per the diagram
  below) should be employed on the DIN, CS and CLK inputs to boost the levels to 5V. Again it
  is possible to drive them directly by the 3.3V GPIO pins, it is just outside tolerance, and
  will result in intermittent issues.

![max7219 levelshifter](https://raw.githubusercontent.com/rm-hull/max7219/master/docs/images/level-shifter.jpg)

Despite the above two points, I still had no success getting cascaded matrices
to work properly.  Revisiting the wiring, I had connected the devices in serial
connecting the out pins of one device to the in pins of another. This just
produced garbled images.

Connecting the CLK lines on the input side all together worked first time. I
can only assume that there is some noise on the clock line, or a dry solder
joint somewhere.

![max7219 cascaded](https://raw.githubusercontent.com/rm-hull/max7219/master/docs/images/matrix_cascaded.jpg)

## Examples

Run the example code as follows:

    $ sudo python examples/matrix_test.py

or

    $ sudo python examples/sevensegment_test.py

*NOTE:* By default, SPI is only accessible by root (hence using `sudo` above). Follow these
instructions to create an spi group, and adding your user to that group, so you don't have to
run as root: http://quick2wire.com/non-root-access-to-spi-on-the-pi

## References

* http://hackaday.com/2013/01/06/hardware-spi-with-python-on-a-raspberry-pi/
* http://gammon.com.au/forum/?id=11516
* http://louisthiery.com/spi-python-hardware-spi-for-raspi/
* http://www.brianhensley.net/2012/07/getting-spi-working-on-raspberry-pi.html
* http://raspi.tv/2013/8-x-8-led-array-driven-by-max7219-on-the-raspberry-pi-via-python
* http://quick2wire.com/non-root-access-to-spi-on-the-pi

## License

### The MIT License (MIT)

Copyright (c) 2016 Richard Hull

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

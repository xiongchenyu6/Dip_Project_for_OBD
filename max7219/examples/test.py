#!/usr/bin/env python

import max7219.led as led
import time
from max7219.font import proportional, SINCLAIR_FONT, TINY_FONT, CP437_FONT
from random import randrange

device = led.matrix(cascaded=16,vertical=True)

device.orientation(180)
device.show_message("12345678987654323456ASDFGHJKLQWERTYUIOP!@#$%^&*()<>?[}|7",delay=0.5)



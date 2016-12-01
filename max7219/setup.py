#!/usr/bin/env python

from distutils.core import setup,Extension
setup(
    name = "max7219",
    version = "0.1.6",
    author = "Richard Hull",
    author_email = "richard.hull@destructuring-bind.org",
    description = ("A small library to drive a MAX7219 LED serializer using hardware spidev"),
    license = "MIT",
    keywords = "raspberry pi rpi led max7219 matrix seven segment",
    url = "https://github.com/rm-hull/max7219",
    packages=['max7219'],
)

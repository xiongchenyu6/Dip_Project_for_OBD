Overall Setup:

1. Install Raspbian OS
2. Connect RPI to monitor via HDMI cable and enable all ports and interfaces
3. Use Putty.exe(serial speed: 115200) and connect RPI to Windows via USB-TTL serialcable(Optional)

USB-TTL serial cable PIN configuration:

Red-VCC  BLACK-GND  WHITE-TX  GREEN-RX 

 

 

**RPI Environment Setup for OBD-LED Matrix(python): **[https://github.com/Pbartek/pyobd-pi](https://github.com/Pbartek/pyobd-pi)

 

**RPI Environment Setup for OBD-Webserver(nodejs):**


1.  Install NodeJs via NPM: 


```
curl -sL https://deb.nodesource.com/setup_7.x | sudo -E bash –
```

```
sudo apt-get install -y nodejs
```

2. Install npm serialport

```
sudo npm install serialport --unsafe-perm --build-from-source
```

 

 

**Download source code from github: **

git clone [https://github.com/xiongchenyu6/Dip_Project_for_OBD.git](https://github.com/xiongchenyu6/Dip_Project_for_OBD.git)

 

**Max7219 Config: **
```
cd Dip_Project_for_OBD/

cd max7219/

sudo python setup.py install
```


**Webserver Config:**
```
cd~

cd Dip_Project_for_OBD/

cd mapServer/

npm install
```


**Gulp Config:**
```
sudo npm install --global gulp-cli
```




To start the webserver, enter(within “mapserver” folder): gulp

To start the LED Matrix, run “Dip_Project_for_OBD/pyobd-pi/obd_recorder.py”
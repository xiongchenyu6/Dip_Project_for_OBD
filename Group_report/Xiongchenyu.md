# DIP REPORT

## Index
1. Collect data from _OBD_
	* serial-obd
	
2. Display data 
	* LED Matrices 
	* SPI communication
	* Show Message
	* Invert Matrics
	
3. Web server to process OBD data and android data
	* RPI settings
		* Network settings
		* Blue tooth setthings
		* Node js server
			* Expressjs
	* Team work building
		* Front-End Automation
			* Gulp
		* Github
		
4. Reference link

---

 
**1. Collect data from _OBD_**

![](obd.png)

We use OBD simulator to simulate the engines status and tansfer data from ELM327 microcontroller(the controller for the OBD simulator) to USB.To communicate with ELM327 to computer through serial connection ,we need to set the baud rate to **38400**,**8** data bits,**no parity** bits and **1** stop bit. And we need to use command **ls -l ttyUSB\*** to check the correct mount location.There are two command sets supported by ELM327, one is the PID command set which is used to communicate with a vehicle or the OBD-­‐‑II simulator,(for example, the OBD command‘0100’ requests the informationof availability of PIDs **[01 -­‐‑20] in Mode 1, in which ’01’ indicates Mode 01 and ’00’ means PID 00). 

Another kind of command is the Hayes AT command set which is considered as the internal commands,(for example, AT command‘atz’ resets the ELM327 chip and all setting are returned to their default values.) Because ELM327 is not case-­‐‑sensitiveand will ignore the spacingso the commands ‘AT Z’, ‘atz’, and ‘AtZ’ are the same to an ELM327

The response to OBD command received from ELM327 are hexadecimal digits in pairs. The first 4 bits will repeat the command andthe rest of data is the requested data from OBD. Because of the response echo the command, the mode value in the response would be added with 40 to distinguish with a command. For example, the response to “0100” may be “41 00 BF 9F B9 90’’. The first byte’41’ represent Mode 01 and “00” represent PID 00. The rest 4   bytes in digital bits are a series of 0(not supported) and 1   (supported) to indicate whether the correspond PID is supported.


Mode(hex)   | Description
--- | --- 
01 | Show current data 
02 | Show freeze frame data 
03 | Show stored Diagnostic Trouble Codes
04 | Clear Diagnostic Trouble Codes and stored value
05 | Test results,oxygen sensor monitoring (non CAN only)
06 | Test results,other component/system monitoring (Test results,oxygen sensor monitoring for CAN only)
07 | Show pending Diagnostic Trouble Codes (detected during current or last driving cycle)
08 | Control operation of on-board component/system
09 | Request vehicle information
0A | Permanent Diagnostic Trouble Codes (DTCs)(Cleared DTCs)

Based on top theory we choose  to use [serial-obd] ,a open source nodejs library to communicate.Here is the code block we use to connect to ELM327.

```javascrpt 
  this.serial.on('open', function () {
        self.connected = true;

        self.write('ATZ');
        //Turns off echo.
        self.write('ATE0');
        //Turns off extra line feed and carriage return
        self.write('ATL0');
        //This disables spaces in in output, which is faster!
        self.write('ATS0');
        //Turns off headers and checksum to be sent.
        self.write('ATH0');
        //Turn adaptive timing to 2. This is an aggressive learn curve for adjusting the timeout. Will make huge difference on slow systems.
        self.write('ATAT2');
        //Set timeout to 10 * 4 = 40msec, allows +20 queries per second. This is the maximum wait-time. ATAT will decide if it should wait shorter or not.
        self.write('ATST0A');
        //Set the protocol to automatic.
        self.write('ATSP0');

        //Event connected
        self.emit('connected');
    });

```
Here is the code to read data from ELM327
```javescript
    this.serial.on('data', function (data) {
        var currentString, arrayOfCommands;
        currentString = self.receivedData + data.toString('utf8'); // making sure it's a utf8 string

        arrayOfCommands = currentString.split('>');

        var forString;
        if (arrayOfCommands.length < 2) {
            self.receivedData = arrayOfCommands[0];
        } else {
            for (var commandNumber = 0; commandNumber < arrayOfCommands.length; commandNumber++) {
                forString = arrayOfCommands[commandNumber];
                if (forString === '') {
                    continue;
                }

                var multipleMessages = forString.split('\r');
                for (var messageNumber = 0; messageNumber < multipleMessages.length; messageNumber++) {
                    var messageString = multipleMessages[messageNumber];
                    if (messageString === '') {
                        continue;
                    }

                    self.emit('debug', 'in    ' + messageString);

                    var reply;
                    reply = parseOBDCommand(messageString);
                    self.emit('dataReceived', reply);

                    if (self.awaitingReply == true) {
                        self.awaitingReply = false;
                        self.emit('processQueue');
                    }
                    self.receivedData = '';
                }
            }
        }
    });
```
---
**2. Display data **

Here is our LED matrics design by our groupmates,after the discussion we deside to use raspberry pi as our platform to build our project and after the research form [Raspberry webpage][RPI SPI] 




[I'm an inline-style link](https://www.google.com)

[serial-obd]:https://github.com/EricSmekens/node-serial-obd
[RPI SPI]:https://www.raspberrypi.org/documentation/hardware/raspberrypi/spi/README.md




'use strict';

var OBDReader = require('serial-obd');
var express = require('express');
var app = express();
var router = express.Router();
var info={};
var options = {};
options.baudrate =38400 ;
var serialOBDReader = new OBDReader('/dev/ttyUSB0', options);
var dataReceivedMarker = {};
var vss =0,rpm=0;

app.set("jsonp callback", true);

app.use(express.static('public'));

app.get('/', function(req, res) {
    res.sendfile('./public/index.html');
});

app.get('/data',function (req,res) {
    info=req.query;
    console.log(info);

    res.send('Got it');
});

app.get('/info',function (req,res) {
    //console.log(info);
    info.vss = vss;
    info.rpm=rpm;
    res.jsonp(info);
});
app.listen(5000,function () {
    console.log('Listening on port 5000');
});

serialOBDReader.on('dataReceived', function (data) {
    console.log(data);
    if(data['name']='vss'&&isNaN(data['value'])==false){vss = data['value'];}
    if(data['name']='rpm'&&isNaN(data['value'])==false){rpm = data['value'];}
    console.log(vss);
    dataReceivedMarker = data;
});

serialOBDReader.on('connected', function (data) {
    //this.requestValueByName("vss"); //vss = vehicle speed sensor

    this.addPoller("vss");
    this.addPoller("rpm");
    //this.addPoller("temp");
    //this.addPoller("load_pct");
    //this.addPoller("map");
    //this.addPoller("frp");

    this.startPolling(1000);
});


serialOBDReader.connect();

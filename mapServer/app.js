'use strict';

 var express = require('express');
 var app = express();
 var router = express.Router();
 var info;
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
        res.jsonp(info);
});
app.listen(5000,function () {
        console.log('Listening on port 5000');
});


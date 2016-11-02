'use strict';

 var express = require('express');
 var app = express();
 var router = express.Router();

 app.use(express.static('public'));

 app.get('/', function(req, res) {
     res.sendfile('./public/index.html');
     });

app.get('/data',function (req,res) {
    console.log(req.query);
    res.send('Got it');
})

app.listen(5000,function () {
        console.log('Listening on port 5000');
});


const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const url = require('url');
const GeoPoint = require('geopoint')
var mysql = require('mysql');

var con = mysql.createConnection({
  host: "vancouver-vehicular-crimes.c4ztbtstxmqe.us-east-1.rds.amazonaws.com",
  user: "admin",
  password: "debbysucks"
});


const app = express();


// three parameters for this request:
// lon : the longitude as a double
// lat : the latitude as a double
// distance : the distance, in miles, as a double
app.get('/', (req, res) => {
    console.log(req.params)
    var lat = req.query.lat
    var lon = req.query.lon
    var distance = req.query.distance

    con.query(`SELECT *, (
                3959 * acos (
                cos ( radians(${lat}) )
                * cos( radians( lat ) )
                * cos( radians( lng ) - radians(${lon}) )
                + sin ( radians(${lat}) )
                * sin( radians( lat ) )
                )
                ) AS distance
                FROM crimes.vehicular
                HAVING distance < ${distance}
                ORDER BY distance`, 
            (err, result, fields) => {
                if (err) throw err;
                
                var crimesPerHour = (result.length / 140160)

                response = {'count': result.length,
                            'crimesPerHour': crimesPerHour,
                            'probabilityOfACrimeInTheNextHour': crimesPerHour * Math.exp(-1.0 * crimesPerHour),
                            }
                console.log(response)
                res.send(response)
            });
});

const PORT = process.env.PORT || 8000;

app.listen(PORT, () => console.log(`Engines running on port ${PORT}`));

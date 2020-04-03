const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const url = require('url');
const GeoPoint = require('geopoint')

const app = express();

app.get('/', (req, res) => {
    console.log(req.params)
    var lat = req.query.lat
    var lon = req.query.lon
    console.log(`lat: ${req.query.lat}`)
    console.log(`lon: ${req.query.lon}`)
    current_point = new GeoPoint(parseFloat(req.query.lat), parseFloat(req.query.lon))
    console.log(current_point)
    coords = current_point.boundingCoordinates(5)
    var min_lat = coords[0].latitude()
    var min_lon = coords[0].longitude()
    var max_lat = coords[1].latitude()
    var max_lon = coords[1].longitude()
    console.log(min_lat)
    console.log(min_lon)
    console.log(max_lat)
    console.log(max_lon)

    

});

const PORT = process.env.PORT || 8000;

app.listen(PORT, () => console.log(`Engines running on port ${PORT}`));

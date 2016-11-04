var polyline = {};
window.Code = "";
var map;
var markersArray = [];
var styleArray = [
    {
        featureType: 'all',
        stylers: [
            { saturation: -80 },
        ]
    },{
        featureType: 'road.arterial',
        elementType: 'geometry',
        stylers: [
            { hue: '#00ffee' },
            { saturation: 50 }
        ]
    },{
        featureType: 'poi.business',
        elementType: 'labels',
        stylers: [
            { visibility: 'off' }
        ]
    },
    {
        featureType: "all",
        elementType: "labels.text.fill",
        stylers: [
            {
                color: "#8CF0F7"
            },
            {
                lightness: 80
            },

        ]
    },
    {
        featureType: "all",
        elementType: "labels.text.stroke",
        stylers: [
            {
                color: "#000000"
            },
            {
                lightness: 13
            }
        ]
    },
    {
        featureType: "administrative",
        elementType: "geometry",
        stylers: [
            {
                visibility: "off"
            }
        ]
    },
    {
        featureType: "administrative",
        elementType: "geometry.fill",
        stylers: [
            {
                "color": "#000000"
            }
        ]
    },
    {
        featureType: "administrative",
        elementType: "geometry.stroke",
        stylers: [
            {
                color: "#144b53"
            },
            {
                lightness: 14
            },
            {
                weight: 1.4
            }
        ]
    },
    {
        featureType: "administrative",
        elementType: "labels",
        stylers: [
            {
                visibility: "off"
            }
        ]
    },
    {
        featureType: "administrative.country",
        elementType: "labels",
        stylers: [
            {
                visibility: "off"
            }
        ]
    },
    {
        featureType: "landscape",
        elementType: "all",
        stylers: [
            {
                color: "#08304b"
            }

        ]
    },
    {
        featureType: "landscape",
        elementType: "labels",
        stylers: [
            {
                visibility: "off"
            }
        ]
    },
    {
        featureType: "poi",
        elementType: "geometry",
        stylers: [
            {
                color: "#0c4152"
            },
            {
                lightness: 5
            },
            {
                visibility: "off"
            }
        ]
    },
    {
        featureType: "road.highway",
        elementType: "geometry.fill",
        stylers: [
            {
                color: "#B2B1B2"
            }
        ]
    },
    {
        featureType: "road.highway",
        elementType: "geometry.stroke",
        stylers: [
            {
                color: "#0b434f"
            },
            {
                lightness: 25
            }
        ]
    },
    {
        featureType: "road.arterial",
        elementType: "geometry.fill",
        stylers: [
            {
                color: "#405837"
            }
        ]
    },
    {
        featureType: "road.arterial",
        elementType: "geometry.stroke",
        stylers: [
            {
                color: "#0b3d51"
            },
            {
                lightness: 16
            }
        ]
    },
    {
        featureType: "road.local",
        elementType: "geometry",
        stylers: [
            {
                color: "#366F21"
            }
        ]
    },
    {
        featureType: "water",
        elementType: "all",
        stylers: [
            {
                color: "#66B6E4"
            }
        ]
    }

]

/**
 * Decodes to a [latitude, longitude] coordinates array.
 *
 * This is adapted from the implementation in Project-OSRM.
 *
 * @param {String} str
 * @param {Number} precision
 * @returns {Array}
 *
 * @see https://github.com/Project-OSRM/osrm-frontend/blob/master/WebContent/routing/OSRM.RoutingGeometry.js
 */

polyline.decode = function(str, precision) {
    var index = 0,
        lat = 0,
        lng = 0,
        coordinates = [],
        shift = 0,
        result = 0,
        byte = null,
        latitude_change,
        longitude_change,
        factor = Math.pow(10, precision || 5);

    // Coordinates have variable length when encoded, so just keep
    // track of whether we've hit the end of the string. In each
    // loop iteration, a single coordinate is decoded.
    while (index < str.length) {

        // Reset shift, result, and byte
        byte = null;
        shift = 0;
        result = 0;

        do {
            byte = str.charCodeAt(index++) - 63;
            result |= (byte & 0x1f) << shift;
            shift += 5;
        } while (byte >= 0x20);

        latitude_change = ((result & 1) ? ~(result >> 1) : (result >> 1));

        shift = result = 0;

        do {
            byte = str.charCodeAt(index++) - 63;
            result |= (byte & 0x1f) << shift;
            shift += 5;
        } while (byte >= 0x20);

        longitude_change = ((result & 1) ? ~(result >> 1) : (result >> 1));

        lat += latitude_change;
        lng += longitude_change;

        coordinates.push([lat / factor, lng / factor]);
    }

    return coordinates;
};


function initialize() {

    //document.getElementById('mytxt').value = Route;
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 0, lng: -180},
        zoom:18,
        styles: styleArray
    });
};


function Displayspeed(Speed){
    document.getElementById('myTextarea').value = Speed + "km/h";
}
var Curret_Speed = 30.2;
Displayspeed(Curret_Speed);


var redraw = function(Code){
    var newCode = Code||'}tgG_pzwRA@A?A?A@A?A?A?A?AAA?A??AA?A??AAAA??AA??AAA?A?AAA?A?A?A?A?A@A?A@A?A@A@A@A@?@A@?@A@?@?@?@?@?@?@@@?@@@??@@?@@`A}@n@i@lAaAx@_@PGNIn@YnBm@dBa@|AU^ID?ZEb@C\\@J?rA@P@`DB|DBnEHp@Bh@AFAjAG';
    window.Code = newCode;
    var Route =polyline.decode(window.Code);
    var myLines=new Array();
    var source = {lat: Route[0][0],lng: Route[0][1]};
    var destination = {lat: Route[Route.length-1][0],lng: Route[Route.length-1][1]};
    console.log(map);
    map.panTo(source);
    for(var i =0;i<Route.length;i++)
    {
        myLines.push(new google.maps.LatLng(Route[i][0],Route[i][1]));
    }

    var myPath=new google.maps.Polyline({
        path:myLines,
        strokeColor:"#FD0202",
        strokeOpacity:0.8,
        strokeWeight:10
    });

    clearOverlays();
    deleteOverlays();
    addMarker(source);
    addMarker(destination);
    showOverlays();
    myPath.setMap(map);
    //
}


function loop (data){
Displayspeed(data.vss);
    // it worked!
    // it worked!
    console.log(JSON.parse(data['key'])['map']['polyline']);
    redraw(JSON.parse(data['key'])['map']['polyline']);
};

function loopFunction() {
    var script = document.createElement('script');
    script.src = 'http://localhost:4000/info?callback=loop'
    document.getElementsByTagName('head')[0].appendChild(script);
    document.getElementsByTagName('head')[0].removeChild(script);
}

setInterval(loopFunction,300);
//(function() {
function addMarker(location) {
    marker = new google.maps.Marker({
        position: location,
        map: map,
        animation: google.maps.Animation.BOUNCE

    });
    markersArray.push(marker);

}

// Removes the overlays from the map, but keeps them in the array
function clearOverlays() {
    if (markersArray) {
        for (i in markersArray) {
            markersArray[i].setMap(null);

        }

    }
}

// Shows any overlays currently in the array
function showOverlays() {
    if (markersArray) {
        for (i in markersArray) {
            markersArray[i].setMap(map);

        }

    }
}

// Deletes all markers in the array by removing references to them
function deleteOverlays() {
    if (markersArray) {
        for (i in markersArray) {
            markersArray[i].setMap(null);

        }
        markersArray.length = 0;

    }
}
// }
// }
// }

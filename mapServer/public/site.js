     
   var polyline = {};

   function py2_round(value) {
      // Google's polyline algorithm uses the same rounding strategy as Python 2, which is different from JS for negative values
      return Math.floor(Math.abs(value) + 0.5) * Math.sign(value);
   }

  function encode(current, previous, factor) {
      current = py2_round(current * factor);
      previous = py2_round(previous * factor);
      var coordinate = current - previous;
      coordinate <<= 1;
      if (current - previous < 0) {
          coordinate = ~coordinate;
      }
      var output = '';
      while (coordinate >= 0x20) {
          output += String.fromCharCode((0x20 | (coordinate & 0x1f)) + 63);
          coordinate >>= 5;
      }
      output += String.fromCharCode(coordinate + 63);
      return output;
  }

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

  /**
   * Encodes the given [latitude, longitude] coordinates array.
   *
   * @param {Array.<Array.<Number>>} coordinates
   * @param {Number} precision
   * @returns {String}
   */
  polyline.encode = function(coordinates, precision) {
      if (!coordinates.length) { return ''; }

      var factor = Math.pow(10, precision || 5),
          output = encode(coordinates[0][0], 0, factor) + encode(coordinates[0][1], 0, factor);

      for (var i = 1; i < coordinates.length; i++) {
          var a = coordinates[i], b = coordinates[i - 1];
          output += encode(a[0], b[0], factor);
          output += encode(a[1], b[1], factor);
      }

      return output;
  };

  function flipped(coords) {
      var flipped = [];
      for (var i = 0; i < coords.length; i++) {
          flipped.push(coords[i].slice().reverse());
      }
      return flipped;
  }

  /**
   * Encodes a GeoJSON LineString feature/geometry.
   *
   * @param {Object} geojson
   * @param {Number} precision
   * @returns {String}
   */
  polyline.fromGeoJSON = function(geojson, precision) {
      if (geojson && geojson.type === 'Feature') {
          geojson = geojson.geometry;
      }
      if (!geojson || geojson.type !== 'LineString') {
          throw new Error('Input must be a GeoJSON LineString');
      }
      return polyline.encode(flipped(geojson.coordinates), precision);
  };

  /**
   * Decodes to a GeoJSON LineString geometry.
   *
   * @param {String} str
   * @param {Number} precision
   * @returns {Object}
   */
  polyline.toGeoJSON = function(str, precision) {
      var coords = polyline.decode(str, precision);
      return {
          type: 'LineString',
          coordinates: flipped(coords)
      };
  };

  if (typeof module === 'object' && module.exports) {
      module.exports = polyline;
  }



  //document.getElementById('mytxt').value = Route;


  var map;

  function initialize() {
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

    var code ='}tgG_pzwRA@A?A?A@A?A?A?A?AAA?A??AA?A??AAAA??AA??AAA?A?AAA?A?A?A?A?A@A?A@A?A@A@A@A@?@A@?@A@?@?@?@?@?@?@@@?@@@??@@?@@`A}@n@i@lAaAx@_@PGNIn@YnBm@dBa@|AU^ID?ZEb@C\\@J?rA@P@`DB|DBnEHp@Bh@AFAjAG';
    var Route =polyline.decode(code);
      
    var myLines=new Array();
    var source = {lat: Route[0][0],lng: Route[0][1]};
    var destination = {lat: Route[Route.length-1][0],lng: Route[Route.length-1][1]};

    map = new google.maps.Map(document.getElementById('map'), {
      center: source,
      zoom:18,
      styles: styleArray
    });
    



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

    var marker = new google.maps.Marker({
      position: source,
      map: map,
      animation: google.maps.Animation.BOUNCE
    });

    var marker = new google.maps.Marker({
      position: destination,
      map: map,
      animation: google.maps.Animation.BOUNCE
    });
    myPath.setMap(map); 
    google.maps.event.addDomListener(window, 'load', initialize);
  } 





  /*var polygons = [
        {lat: 1.352, lng: 103.672},
        {lat: 1.332, lng: 103.682},
        {lat: 1.342, lng: 103.692}
        ];
   var drawpolygons = new google.maps.Polygon({
      paths: polygons,
      strokeColor: '#FF0000',
      strokeOpacity: 0.8,
      strokeWeight: 3,
      fillColor: '#FF0000',
      fillOpacity: 0.35
    });
  drawpolygons.setMap(map);
   infoWindow = new google.maps.InfoWindow;
  */

 
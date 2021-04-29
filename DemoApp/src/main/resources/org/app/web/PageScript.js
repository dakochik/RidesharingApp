const map = L.map('map').setView([42.2, -87.1], 7);

// Adding Voyager Basemap
L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager_nolabels/{z}/{x}/{y}.png', {
    maxZoom: 18
}).addTo(map);

// Adding Voyager Labels
L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager_only_labels/{z}/{x}/{y}.png', {
    maxZoom: 18,
    zIndex: 10
}).addTo(map);

var client = new carto.Client({
    apiKey: '1dfba9e5fb93bade9610d6c49e070d65f5760ddb',
    username: 'dakochik'
});

drawMap(100, "")

function drawMap(number, options) {
    let flag1 = document.getElementById('c_b_1').checked;
    let flag2 = document.getElementById('c_b_2').checked;
    let flag3 = document.getElementById('c_b_3').checked;

    let tripID = document.getElementById('trip_id').value;

    client.removeLayers(client.getLayers());
    var arr = [];

    if (flag1) {
        const dataset1 = new carto.source.SQL(`
                    SELECT *
                    FROM chicago_5000_filtered`
            + options +
            ` LIMIT ` + number.toString());
        const style1 = new carto.style.CartoCSS(`
            #layer {
            line-width: 1;
            line-color: #040BD4;
            line-opacity: 0.5;
            }
            `);
        const layer1 = new carto.layer.Layer(dataset1, style1, {
            featureOverColumns: (['trip_start_timestamp', 'pickup_centroid_location', 'dropoff_centroid_location'])
        });
        arr.push(layer1);
        setOverView(layer1);
    }

    if (flag2) {
        const dataset2 = new carto.source.SQL(`
                    SELECT *
                    FROM chicago_5000_cars`
            + options +
            ` LIMIT ` + number.toString());
        const style2 = new carto.style.CartoCSS(`
            #layer {
            line-width: 1;
            line-color: #A80003;
            line-opacity: 0.5;
            }
            `);
        const layer2 = new carto.layer.Layer(dataset2, style2, {
            featureOverColumns: (['trip_start_timestamp', 'pickup_centroid_location', 'dropoff_centroid_location'])
        });
        arr.push(layer2);
        setOverView(layer2);
    }

    if (flag3) {
        const dataset3 = new carto.source.SQL(`
                    SELECT *
                    FROM chicago_5000_requests`
            + options +
            ` LIMIT ` + number.toString());
        const style3 = new carto.style.CartoCSS(`
            #layer {
            line-width: 1;
            line-color: #00A82A;
            line-opacity: 0.5;
            }
            `);
        const layer3 = new carto.layer.Layer(dataset3, style3, {
            featureOverColumns: (['trip_start_timestamp', 'pickup_centroid_location', 'dropoff_centroid_location'])
        });
        arr.push(layer3);
        setOverView(layer3);
    }

    if (tripID != null && tripID != "") {
        const dataset4 = new carto.source.SQL(`
                    SELECT *
                    FROM chicago_5000_cars
                    WHERE trip_id = '` + tripID + `'` +
            `UNION SELECT *
                FROM chicago_5000_cars
                WHERE trip_id in
                (SELECT trip_id
                FROM chicago_5000_requests
                WHERE request_id = '` + tripID + `')`);
        const style4 = new carto.style.CartoCSS(`
            #layer {
            line-width: 3;
            line-color: #FF00B7;
            line-opacity: 1;
            }
            `);
        const layer4 = new carto.layer.Layer(dataset4, style4, {
            featureOverColumns: (['trip_start_timestamp', 'pickup_centroid_location', 'dropoff_centroid_location'])
        });
        arr.push(layer4);
        setOverView(layer4);
    }

    client.addLayers(arr);

    client.getLeafletLayer().addTo(map);
}

function prepareFiltering() {
    let maxNumb = document.getElementById("trips_numb").value;
    let dataLowerBound = document.getElementById("trips_time_begin").value
    let dataUpperBound = document.getElementById("trips_time_end").value
    if (dataLowerBound != "" && dataUpperBound != "") {
        if (toDate(dataLowerBound, "h:m") > toDate(dataUpperBound, "h:m")) {
            alert("Time window lower bound can't be bigger than upper bound!")
        } else {
            alert("between '" + dataLowerBound + "' and '" + dataUpperBound + "'")
            drawMap(maxNumb, " WHERE cast(trip_start_timestamp as time) between '" + dataLowerBound + "' and '" + dataUpperBound + "'")
        }
    } else {
        drawMap(maxNumb, "");
    }
}

function toDate(dStr, format) {
    var now = new Date();
    if (format == "h:m") {
        now.setHours(dStr.substr(0, dStr.indexOf(":")));
        now.setMinutes(dStr.substr(dStr.indexOf(":") + 1));
        now.setSeconds(0);
        return now;
    } else
        return "Invalid Format";
}

function setOverView(layer) {
    const popup = L.popup({closeButton: false});
    layer.on(carto.layer.events.FEATURE_OVER, featureEvent => {
        popup.setLatLng(featureEvent.latLng);
        if (!popup.isOpen()) {
            let content = '<p>' + '<span style="font-weight: bold">Date: </span>'
                + (featureEvent.data.trip_start_timestamp == null ? "no info" : featureEvent.data.trip_start_timestamp) + '</p>';
            content += '<p>' + '<span style="font-weight: bold">Origin: </span>'
                + featureEvent.data.pickup_centroid_location + '</p>';
            content += '<p>' + '<span style="font-weight: bold">Destination: </span>'
                + featureEvent.data.dropoff_centroid_location + '</p>';
            popup.setContent(content);
            popup.openOn(map);
        }
    });
}

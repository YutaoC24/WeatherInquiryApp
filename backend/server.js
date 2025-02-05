var express = require('express');
var path = require('path');
var https = require('https');
var cors = require('cors');
const { MongoClient } = require("mongodb");
const multer = require('multer');

const app = express()
app.use(cors());

var mongoString = 'mongodb+srv://tomorrowIOuser:b7YTcE7vSHqowZYo@cluster0.seves.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0';
var database;
const DATABASENAME = 'tomorrowIOdb'
const COLLECTIONNAME = 'tomorrowIOcollection'
const client = new MongoClient(mongoString);

async function run() {
  try {
    database = client.db(DATABASENAME);
    const current_col = database.collection(COLLECTIONNAME);
    // Query for a movie that has the title 'Back to the Future'
    const query = { city: 'Shanghai' };
    const col = await current_col.findOne(query);
    console.log(col);
  } finally {
    // Ensures that the client will close when you finish/error
    await client.close();
  }
}

async function fetch_all() {
    var results;
    try {
        await client.connect();
        database = client.db(DATABASENAME);
        const current_col = database.collection(COLLECTIONNAME);
        // Query for a movie that has the title 'Back to the Future'
        const col = await current_col.find();
        results = await col.toArray();
      } finally {
        // Ensures that the client will close when you finish/error
        await client.close();
        return results;
      }
}

async function remove_one(lat, lng) {
    try {
        await client.connect();
        database = client.db(DATABASENAME);
        const current_col = database.collection(COLLECTIONNAME);
        // Query for a movie that has the title 'Back to the Future'
        current_col.deleteOne({lat: lat, lng: lng});
      } finally {
        // Ensures that the client will close when you finish/error
        await client.close();
        return;
      }
}

async function add_one(city, state, lat, lng) {
    try {
        await client.connect();
        database = client.db(DATABASENAME);
        const current_col = database.collection(COLLECTIONNAME);
        // Query for a movie that has the title 'Back to the Future'
        current_col.insertOne({city: city, state: state, lat: lat, lng: lng});
      } finally {
        // Ensures that the client will close when you finish/error
        await client.close();
        return;
      }
}

async function check_one(lat, lng) {
    var results;
    var result;
    try {
        await client.connect();
        database = client.db(DATABASENAME);
        const current_col = database.collection(COLLECTIONNAME);
        // Query for a movie that has the title 'Back to the Future'
        result = await current_col.findOne({lat: lat, lng: lng});
        console.log(result);
      } finally {
        // Ensures that the client will close when you finish/error
        await client.close();
        return result;
      }
}

app.get('/', (req, res) => {
    res.send('hello world');
    console.log(__dirname)
})

app.get('/api/favoriteAll', (req, res) => {
    fetch_all().then(result => {
        console.log(result);
        res.json(result);
    })
    }
)

app.get('/api/checkFavorite/:lat/:lng', (req, res) => {
    const lat_to_check = req.params.lat;
    const lng_to_check = req.params.lng;
    check_one(lat_to_check, lng_to_check).then(result => {
        if (result != null) {
            res.send('yes');
        } else {
            res.send('no');
        }
    })
})

app.get('/api/removeFavorite/:lat/:lng', (req, res) => {
    const lat_to_remove = req.params.lat;
    const lng_to_remove = req.params.lng;
    console.log(lat_to_remove);
    remove_one(lat_to_remove, lng_to_remove);
    res.send('removed');
})

app.get('/api/addFavorite/:city/:state/:lat/:lng', (req, res) => {
    const city_to_add = req.params.city;
    const state_to_add = req.params.state;
    const lat_to_add = req.params.lat;
    const lng_to_add = req.params.lng;
    add_one(city_to_add, state_to_add, lat_to_add, lng_to_add);
    res.send('added');
})

app.get('/api/autoDetect', async (req, res) => {
    result = await getIPData();
    res.setHeader('Content-Type', 'text/plain')
    
    res.send(result.loc + "," + result.city + "," + result.region);
})

app.get('/api/autoComplete/:input', (req, res) => {
    const input_value = req.params.input;
    (async () => {
        try {
            const res2 = await fetch('https://maps.googleapis.com/maps/api/place/autocomplete/json?language=en&components=country:us&types=(cities)&input=' + input_value + '&key=AIzaSyADHfUZE92mQDKuJFFmE_4BXEE5GsGg05k');
            const headerDate = res2.headers && res2.headers.get('date') ? res2.headers.get('date') : 'no response date';
            console.log('Status Code:', res2.status);
            console.log('Date in Response header:', headerDate);
            
            const predictions = await res2.json();
            console.log(predictions)

            res.setHeader('Content-Type', 'application/json');
            res.end(JSON.stringify(predictions))
            
            /*
            for(user of users) {
                console.log(`Got user with id: ${user.id}, name: ${user.name}`);
            }
            */
        } catch (err) {
            console.log(err.message); //can be console.error
        }
      })();
})

app.get('/api/geocoding/:street/:city/:state', (req, res) => {
    const street = req.params.street;
    const city = req.params.city;
    const state = req.params.state;
    (async () => {
        try {
            url_for_geo = 'https://maps.googleapis.com/maps/api/geocode/json?address=' + street + '+' + city + '+' + state
                            +'&key=AIzaSyADHfUZE92mQDKuJFFmE_4BXEE5GsGg05k'
            console.log(url_for_geo);
            const res2 = await fetch(url_for_geo);
            const headerDate = res2.headers && res2.headers.get('date') ? res2.headers.get('date') : 'no response date';
            console.log('Status Code:', res2.status);
            console.log('Date in Response header:', headerDate);
            
            const geo_info = await res2.json();
            console.log(geo_info)
            var str_to_send;
            if (geo_info.results.length == 0) {
                str_to_send = 'failure';
            } else {
                str_to_send = geo_info.results[0].geometry.location.lat + ',' + geo_info.results[0].geometry.location.lng;
            }
            res.setHeader('Content-Type', 'text/plain')
            res.send(str_to_send);
            
            /*
            for(user of users) {
                console.log(`Got user with id: ${user.id}, name: ${user.name}`);
            }
            */
        } catch (err) {
            console.log(err.message); //can be console.error
        }
      })();
})

app.get('/api/tomorrowIO/:lat/:lng', (req, res) => {
    const lat = req.params.lat;
    const lng = req.params.lng;
    (async () => {
        try {
            url_for_table = 'https://api.tomorrow.io/v4/timelines?'
                            + 'location=' + lat + '%2C%20' + lng
                            + '&fields=weatherCode'
                            + '&fields=temperatureMin'
                            + '&fields=temperatureMax'
                            + '&fields=temperatureApparent'
                            + '&fields=windSpeed'
                            + '&fields=precipitationType'
                            + '&fields=precipitationProbability'
                            + '&fields=humidity'
                            + '&fields=visibility'
                            + '&fields=sunriseTime'
                            + '&fields=sunsetTime'
                            + '&fields=cloudCover'
                            + '&units=imperial'
                            + '&timesteps=1d'
                            + '&apikey=jMJyqDMlExeP1boJqmRMooPzKTIJDPlZ'
            console.log(url_for_table)
            const res2 = await fetch(url_for_table);
            const headerDate = res2.headers && res2.headers.get('date') ? res2.headers.get('date') : 'no response date';
            console.log('Status Code:', res2.status);
            console.log('Date in Response header:', headerDate);
            
            const predictions = await res2.json();
            console.log(predictions)

            res.setHeader('Content-Type', 'application/json');
            res.end(JSON.stringify(predictions))
            
            /*
            for(user of users) {
                console.log(`Got user with id: ${user.id}, name: ${user.name}`);
            }
            */
        } catch (err) {
            console.log(err.message); //can be console.error
        }
      })();
})

app.get('/api/tomorrowIOChart/:lat/:lng', (req, res) => {
    const lat = req.params.lat;
    const lng = req.params.lng;
    (async () => {
        try {
            url_for_table = 'https://api.tomorrow.io/v4/timelines?'
                            + 'location=' + lat + '%2C%20' + lng
                            + '&fields=humidity'
                            + '&fields=temperature'
                            + '&fields=pressureSeaLevel'
                            + '&fields=windDirection'
                            + '&fields=windSpeed'
                            + '&units=imperial'
                            + '&timesteps=1h'
                            + '&apikey=jMJyqDMlExeP1boJqmRMooPzKTIJDPlZ'
            console.log(url_for_table)
            const res2 = await fetch(url_for_table);
            const headerDate = res2.headers && res2.headers.get('date') ? res2.headers.get('date') : 'no response date';
            console.log('Status Code:', res2.status);
            console.log('Date in Response header:', headerDate);
            
            const predictions = await res2.json();
            console.log(predictions)

            res.setHeader('Content-Type', 'application/json');
            res.end(JSON.stringify(predictions))
            
            /*
            for(user of users) {
                console.log(`Got user with id: ${user.id}, name: ${user.name}`);
            }
            */
        } catch (err) {
            console.log(err.message); //can be console.error
        }
      })();
})

//Helper function to fetch the IP data from client
async function getIPData() {
    const url = 'https://ipinfo.io/?token=e2012e79286493'
    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Response status: ${response.status}`);
      }
  
      const json = await response.json();
      console.log(json)
      return json
    } catch (error) {
      console.error(error.message);
      return -1
    }
  }

const PORT = process.env.PORT || 8080
app.listen(PORT, _ => {
    console.log(`App deloyed at Port ${PORT}`);
    run().catch(console.dir);
})

import { Component, OnInit, Renderer2, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import { HttpClient } from '@angular/common/http';
import {FormBuilder, Validators, ReactiveFormsModule, FormGroup} from '@angular/forms';
import { Observable, startWith, map} from 'rxjs';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { HighchartsChartModule } from 'highcharts-angular';
import { GoogleMapsModule } from "@angular/google-maps";
import Highcharts from 'highcharts';
import more from 'highcharts/highcharts-more';
import highchartsBellCurve from 'highcharts/modules/histogram-bellcurve';

if (typeof Highcharts === 'object') {
  highchartsBellCurve(Highcharts); // Execute the bell curve module
  more(Highcharts);
}

const weatherCode:Record<string, Array<string>> = {
  "0": ["Unknown", "Unknown"],
  "1000": ["Clear", 'clear_day.svg'],
  "1100": ["Mostly Clear", 'mostly_clear_day.svg'],
  "1101": ["Partly Cloudy", 'partly_cloudy_day.svg'],
  "1102": ["Mostly Cloudy", 'mostly_cloudy.svg'],
  "1001": ["Cloudy", 'cloudy.svg'],
  "2000": ["Fog", 'fog.svg'],
  "2100": ["Light Fog", 'fog_light.svg'],
  "4000": ["Drizzle", 'drizzle.svg'],
  "4001": ["Rain", 'rain.svg'],
  "4200": ["Light Rain", 'rain_light.svg'],
  "4201": ["Heavy Rain", 'rain_heavy.svg'],
  "5000": ["Snow", 'snow.svg'],
  "5001": ["Flurries", 'flurries.svg'],
  "5100": ["Light Snow", 'snow_light.svg'],
  "5101": ["Heavy Snow", 'snow_heavy.svg'],
  "6000": ["Freezing Drizzle", 'freezing_drizzle.svg'],
  "6001": ["Freezing Rain", 'freezing_rain.svg'],
  "6200": ["Light Freezing Rain", 'freezing_rain_light.svg'],
  "6201": ["Heavy Freezing Rain", 'reezing_rain_heavy.svg'],
  "7000": ["Ice Pellets", 'ice_pellets.svg'],
  "7101": ["Heavy Ice Pellets", 'ice_pellets_heavy.svg'],
  "7102": ["Light Ice Pellets", 'ice_pellets_light.svg'],
  "8000": ["Thunderstorm", 'tstorm.svg']
}

const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']

const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    ReactiveFormsModule,
    MatAutocompleteModule,
    HighchartsChartModule,
    GoogleMapsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  animations: [
    trigger('slidingState', [
      state('left', style({ transform: 'translateX(0)' })),
      state('right', style({ transform: 'translateX(-120%)' })),
      transition('* => *', animate(300))
    ]),
    trigger('slidingState2', [
      state('left', style({ transform: 'translateX(120%)' })),
      state('right', style({ transform: 'translateX(0%)' })),
      transition('* => *', animate(300))
    ])
  ]
})
export class AppComponent implements OnInit{
  title = 'angular';
  private fb = inject(FormBuilder)
  private http = inject(HttpClient) 
  auto_detect = false;
  LocationForm!: FormGroup;
  filtered_cities!: Observable<string[]>
  city_state_pairs!: Observable<city_state_pair[]>
  isHighcharts = typeof Highcharts === 'object';
  chartConstructor: string = 'chart';
  Highcharts: typeof Highcharts = Highcharts;
  temp_array: Array<Array<number>> = [[1, 2, 3], [2, 3, 4]];
  hideGeneral: boolean = false;
  hidePanel: boolean = true;
  hideState1: boolean = (this.hideGeneral || this.hidePanel);
  hideState2: boolean = (!this.hideGeneral || this.hidePanel);
  hideState3: boolean = true;
  weather_array: Array<day_weather> = []
  weather_array2: Array<day_weather2> = []
  index_array: Array<number> = [0, 1, 2, 3, 4, 5, 6, 7]
  chartOptions: any = {};
  chartOptions2: any = {};
  fav_list: Array<fav> = [];
  hideProgressBar2: boolean = true;
  current_is_fav: boolean = false;
  current_lat: number = 0;
  current_lng: number = 0;
  show_error: boolean = false;
  show_sorry: boolean = false;
  search_started = false;
  clear_after_search = false;
  error_state = false;
  //chartOptions: Highcharts.Options = 

  left = true;
  constructor(private renderer: Renderer2) {}
  get stateName() {
    return this.left ? 'left' : 'right';
  }
  toggle() {
    this.left = !this.left;
  }

  street: String = '';
  city: String = '';
  state: String = '';

  map_positions = {
    lat: 0,
    lng: 0,
  }

  tweet_message = "https://twitter.com/intent/post?"
  
  options: google.maps.MapOptions = {
    mapId: "DEMO_MAP_ID",
    center: { lat: 0, lng: 0},
    zoom: 25,
  };

  general_to_detail() {
    this.toggle();
    this.hideGeneral = true;
    //this.hideState1 = (this.hideGeneral || this.hidePanel);
    //this.hideState2 = (!this.hideGeneral || this.hidePanel);
    this.hideState1 = true;
    this.hideState2 = false;
  }

  detail_to_general() {
    this.toggle();
    this.hideGeneral = false;
    //this.hideState1 = (this.hideGeneral || this.hidePanel);
    //this.hideState2 = (!this.hideGeneral || this.hidePanel);
    this.hideState1 = false;
    this.hideState2 = true;
  }

  delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  transformDataToHighcharts() {
    const chartData = this.temp_array.map(item => [item[0], item[1], item[2]]);

    this.chartOptions = {
        chart: {
          width: typeof window === "undefined" ? 1000 : window.innerWidth * 0.7,
          type: 'arearange',
          zooming: {
              type: 'x'
          },
          scrollablePlotArea: {
              minWidth: 800,
              scrollPositionX: 1
          }
        },
        title: {
            text: 'Temperature Ranges(Min, Max)'
        },
        xAxis: {
            type: 'datetime',
            accessibility: {
                rangeDescription: 'Range: Jan 1st 2017 to Dec 31 2017.'
            }
        },
        yAxis: {
            title: {
                text: null
            }
        },
        tooltip: {
            shared: true,
            valueSuffix: '°F',
            xDateFormat: '%A, %b %e'
        },
        legend: {
            enabled: false
        },
        series: [{
          type: 'arearange',
          name: 'Temperatures',
          data: chartData,
          color: {
              linearGradient: {
                  x1: 0,
                  x2: 0,
                  y1: 0,
                  y2: 1
              },
              stops: [
                  [0, '#FFA500'],
                  [1, '#75abf4']
                ]
            }
        }]
      };
    }

  //static readonly state_names: { [id: string] : string; } = {};

  // populate the state value to the state field when city is selected from drop down
  populateState(pair: city_state_pair){
    this.LocationForm.controls['state'].setValue(pair.state);
  }

  // display only the city value from city_state_pair in auto complete drop down
  displayFn(pair: city_state_pair): string {
    if (pair == null) {
      return '';
    }
    return pair.city;
  }

  display_result(event: any) {
    event.target.classList.add('active');
    document.getElementById('favorite_button')?.classList.remove('active');
    this.hideProgressBar2 = true;
    if (this.error_state) {
      this.show_error = true;
      this.show_sorry = false;
      return;
    }
    if (this.clear_after_search) {
      this.show_sorry = false;
      return;
    }
    this.hidePanel = false;
    this.hideState1 = (this.hideGeneral || this.hidePanel);
    this.hideState2 = (!this.hideGeneral || this.hidePanel);
    if (!this.search_started) {
      this.hideState1 = true;
    }
    this.hideState3 = true;
    this.show_sorry = false;
    this.show_error = false;
    return;
  }

  async display_favorites(event: any) {
    console.log('favorites!');
    event.target.classList.add('active');
    document.getElementById('result_button')?.classList.remove('active');
    this.hidePanel = true;
    this.hideState1 = (this.hideGeneral || this.hidePanel);
    this.hideState2 = (!this.hideGeneral || this.hidePanel);
    this.hideState3 = false;
    this.show_sorry = false;
    this.show_error = false;
    try {
      let result = await this.get_all_favorites();
    } catch (e:unknown) {
      console.log(e);
    }
    //console.log(this.fav_list);
    return;
  }

  // get all the favorite entries stored by mongodb
  async get_all_favorites() {
    this.hideState3 = true;
    var favorite_url = 'https://multiweatherview1234.wl.r.appspot.com/api/favoriteAll';
    // show the progress bar when loading the favorite list
    this.hideProgressBar2 = false;
    try {this.http.get(favorite_url).subscribe(result => {
      let results: Array<fav> = Object.values(result);
      if (results.length == 0) {
        this.show_sorry = true;
        this.hideProgressBar2 = true;
        return;
      }
      this.fav_list = [];
      document.querySelectorAll(".favorite_row_content").forEach(el => el.remove());
      results.forEach(element => {
        this.fav_list.push(element);
      });
      this.hideState3 = false;
      // log the favorite list result
      console.log(this.fav_list);
      // hide the progress bar when the loading is finished
      this.hideProgressBar2 = true;
      //console.log(this.fav_list);
      this.show_favorites();
    })} catch (e:unknown) {
      this.hideProgressBar2 = true;
      this.show_error = true;
    }
  }

  show_favorites() {
    let list: Array<fav> = this.fav_list;
    list.forEach((element, index) => {

      let new_div = this.renderer.createElement('div');
      let new_index = this.renderer.createElement('div');
      this.renderer.addClass(new_index, 'col-1');
      new_index.innerHTML = String(index + 1);
      let new_city = this.renderer.createElement('div');
      this.renderer.addClass(new_city, 'col-5');
      this.renderer.addClass(new_city, 'favorite_text')
      new_city.innerHTML = element.city;
      this.renderer.listen(new_city, 'click', () => {
        this.search_start2(this.fav_list[index].city, this.fav_list[index].state, +this.fav_list[index].lat, +this.fav_list[index].lng)
      })
      let new_state = this.renderer.createElement('div');
      this.renderer.addClass(new_state, 'favorite_text')
      this.renderer.addClass(new_state, 'col-5');
      new_state.innerHTML = element.state;
      this.renderer.listen(new_state, 'click', () => {
        this.search_start2(this.fav_list[index].city, this.fav_list[index].state, +this.fav_list[index].lat, +this.fav_list[index].lng)
      })
      let new_bin = this.renderer.createElement('div');
      this.renderer.addClass(new_bin, 'col-1');
      let new_bin_icon = this.renderer.createElement('span');
      this.renderer.addClass(new_bin_icon, 'bi');
      this.renderer.addClass(new_bin_icon, 'bi-trash-fill');
      this.renderer.addClass(new_bin_icon, 'clickable_bin');
      this.addDynamicClickHandler(new_bin_icon, index);
      this.renderer.appendChild(new_bin, new_bin_icon);
      this.renderer.appendChild(new_div, new_index);
      this.renderer.appendChild(new_div, new_city);
      this.renderer.appendChild(new_div, new_state);
      this.renderer.appendChild(new_div, new_bin);
      this.renderer.addClass(new_div, "favorite_row_content");
      this.renderer.addClass(new_div, "row");
      this.renderer.addClass(new_div, "justify-content-start");
      document.getElementById('container_favorite')?.appendChild(new_div);
    })
  }

  // for removing favorite
  addDynamicClickHandler(element: HTMLElement, index: number): void {
    this.renderer.listen(element, 'click', () => {
      this.removeFavorite(index);
      // check if the current viewing location is removed with click on trash bin
      if (this.fav_list[index].lat == String(this.current_lat) && this.fav_list[index].lng == String(this.current_lng)) {
        this.current_is_fav = false;
        const ele = document.getElementById('star_button2') as HTMLElement;
        ele.style.color = 'white';
      }
      this.fav_list.splice(index, 1);
      //delete this.fav_list[index];
      document.querySelectorAll(".favorite_row_content").forEach(el => el.remove());
      this.show_favorites();
    });
  }

  addFavorite() {
    // if the current browsing location is added to favorite
    if (this.current_is_fav) {
      this.current_is_fav = false;
      const ele = document.getElementById('star_button2') as HTMLElement;
      ele.style.color = 'white';
      // remove it from favorite list
      let index_to_remove = -1;
      this.fav_list.forEach((element, index) => {
        if ((+element.lat == this.current_lat) && (+element.lng == this.current_lng)) {
          index_to_remove = index;
        }
      })
      this.removeFavorite(index_to_remove);
      this.fav_list.splice(index_to_remove, 1);
      return;
    } else {
      this.current_is_fav = true;
      const ele = document.getElementById('star_button2') as HTMLElement;
      ele.style.color = 'rgb(251, 225, 27)';
      let url = 'https://multiweatherview1234.wl.r.appspot.com/api/addFavorite/' + String(this.city) + '/' + String(this.state) + '/' + String(this.current_lat) + '/' + String(this.current_lng);
      try {this.http.get(url, {responseType: "text"}).subscribe(result => {
        console.log(result);
      })} catch (e:unknown) {
        this.show_error = true;
        console.log(e);
      }
      var new_fav: fav = {
        city: String(this.city),
        state: String(this.state),
        lat: String(this.current_lat),
        lng: String(this.current_lng)
      }
      this.fav_list.push(new_fav);
      return;
    }
  }

  removeFavorite(index: number) {
    let lat = this.fav_list[index].lat;
    let lng = this.fav_list[index].lng;
    let url = 'https://multiweatherview1234.wl.r.appspot.com/api/removeFavorite/' + String(lat) + '/' + String(lng);
    try {this.http.get(url, {responseType: "text"}).subscribe(result => {
      console.log(result);
    })} catch (e:unknown) {
      this.show_error = true;
      console.log(e);
    }
  }

  check_if_favorite(lat: number, lng: number) {
    let url = 'https://multiweatherview1234.wl.r.appspot.com/api/checkFavorite/' + String(lat) + '/' + String(lng);
    try {this.http.get(url, {responseType: "text"}).subscribe(result => {
      if (result == 'yes') {
        this.current_is_fav = true;
        const ele = document.getElementById('star_button2') as HTMLElement;
        ele.style.color = 'rgb(251, 225, 27)';
      } else {
        this.current_is_fav = false;
        const ele = document.getElementById('star_button2') as HTMLElement;
        ele.style.color = 'white';
      }
    })} catch (e:unknown) {
      this.show_error = true;
      console.log(e);
    }
  }

  set_google_map(lat_in: number, lng_in: number) {
    this.options = {
      mapId: "DEMO_MAP_ID",
      center: { lat: lat_in, lng: lng_in},
      zoom: 13,
    };
    this.map_positions = {
      lat: lat_in,
      lng: lng_in
    }
  }

  // clear everything
  clear_all() {
    let street = document.getElementById('street') as HTMLInputElement;
    street.value = '';
    let city = document.getElementById('city') as HTMLInputElement;
    city.value = '';
    let state = document.getElementById('state') as HTMLSelectElement;
    state.value = '';
    let select_button = document.getElementById('auto_detect_button') as HTMLInputElement;
    select_button.checked = false;
    this.auto_detect = false;
    this.LocationForm.controls['street'].enable();
    this.LocationForm.controls['city'].enable();
    this.LocationForm.controls['state'].enable();
    this.hideState1 = true;
    this.hideState2 = true;
    this.hideState3 = true;
    this.show_error = false;
    this.show_sorry = false;
    this.hideProgressBar2 = true;
    document.getElementById('search_button')?.setAttribute("disabled", "");

    this.LocationForm.reset();
    this.LocationForm.controls['street'].setValue('');
    this.LocationForm.controls['city'].setValue('');
    this.LocationForm.controls['state'].setValue('');
    if (this.search_started) {
      this.clear_after_search = true;
    }
  }

  // start searching
  search_start() {
    // clear all uncleaned weather table rows
    document.querySelectorAll(".day_content_row").forEach(el => el.remove());
    document.querySelectorAll(".day_content_row_hr").forEach(el => el.remove());

    this.search_started = true;
    this.hideProgressBar2 = false;
    this.clear_after_search = false;
    this.hideState1 = (this.hideGeneral || this.hidePanel);
    this.hideState2 = (!this.hideGeneral || this.hidePanel);
    this.hideState3 = true;

    var auto_detect_IP_url = 'https://multiweatherview1234.wl.r.appspot.com/api/autoDetect'
    // check if auto detect IP enabled
    if (this.auto_detect) {
      try {this.http.get(auto_detect_IP_url, {responseType: "text"}).subscribe(result => {
        var lat_lng_str = <string>result
        var lat = lat_lng_str.split(',')[0]
        var lng = lat_lng_str.split(',')[1]
        let city_name = lat_lng_str.split(',')[2]
        let state_name = lat_lng_str.split(',')[3]
        let myContainer = document.getElementById('result_title') as HTMLInputElement;
        myContainer.innerHTML = "Forecast at " + city_name + ", " + state_name;
        this.city = city_name;
        this.state = state_name;
        this.current_lat = +lat;
        this.current_lng = +lng;
        this.check_if_favorite(+lat, +lng);
        this.callTomorrowIO(lat, lng);
        this.callTomorrowIOChart(lat, lng);
        var lat_num: number = +lat;
        var lng_num: number = +lng;
        this.set_google_map(lat_num, lng_num);
        this.hideState1 = false;
        this.hideProgressBar2 = true;
      })} catch (e:unknown) {
        console.log(e);
        this.show_error = true;
        this.hideProgressBar2 = true;
      }
    // if not auto IP, use geocoding with google map api
    } else {
      let url = 'https://multiweatherview1234.wl.r.appspot.com/api/geocoding/'
      var city_name: string;
      url += this.LocationForm.controls['street'].value + "/";
      this.street = this.LocationForm.controls['street'].value;
      if (typeof this.LocationForm.controls['city'].value === 'string') {
        url += this.LocationForm.controls['city'].value + "/";
        city_name = this.LocationForm.controls['city'].value;
      } else {
        url += this.LocationForm.controls['city'].value.city + "/";
        city_name = this.LocationForm.controls['city'].value.city;
      }
      this.city = city_name;
      url += this.LocationForm.controls['state'].value;
      this.state = this.LocationForm.controls['state'].value;
      try {this.http.get(url, {responseType: "text"}).subscribe(lat_lng => {
        console.log(lat_lng);
        if (lat_lng == 'failure') {
          // to-do: implement error ui here
          this.show_error = true;
          this.hideProgressBar2 = true;
          return;
        }
        let lat_lng_str = <string>lat_lng
        var lat = lat_lng_str.split(',')[0]
        var lng = lat_lng_str.split(',')[1]
        let myContainer = document.getElementById('result_title') as HTMLInputElement;
        var sel = document.getElementById("state") as HTMLSelectElement;
        var text= sel?.options[sel.selectedIndex].text;  
        myContainer.innerHTML = "Forecast at " + city_name + ", " + text;
        this.check_if_favorite(+lat, +lng);
        this.callTomorrowIO(lat, lng);
        this.callTomorrowIOChart(lat, lng);
        this.current_lat = +lat;
        this.current_lng = +lng;
        var lat_num: number = +lat;
        var lng_num: number = +lng;
        this.set_google_map(lat_num, lng_num);
        this.hideState1 = false;
        this.hideProgressBar2 = true;
      })} catch (e:unknown) {
        console.log(e);
        this.show_error = true;
        this.hideProgressBar2 = true;
      }
    }
  }

  // start searching
  search_start2(city: string, state: string, lat: number, lng: number) {
    // clear all uncleaned weather table rows
    document.querySelectorAll(".day_content_row").forEach(el => el.remove());
    document.querySelectorAll(".day_content_row_hr").forEach(el => el.remove());

    this.hideProgressBar2 = false;
    this.search_started = true;
    this.clear_after_search = false;
    document.getElementById('favorite_button')?.classList.remove('active');
    document.getElementById('result_button')?.classList.add('active');

    let myContainer = document.getElementById('result_title') as HTMLInputElement;
    myContainer.innerHTML = "Forecast at " + city + ", " + state;
    // given that the selected one from favorite list must be a favorite
    this.current_is_fav = true;
    const ele = document.getElementById('star_button2') as HTMLElement;
    ele.style.color = 'rgb(251, 225, 27)';
    try {
      this.callTomorrowIO(String(lat), String(lng));
      this.callTomorrowIOChart(String(lat), String(lng));
      this.current_lat = lat;
      this.current_lng = lng;
      var lat_num: number = lat;
      var lng_num: number = lng;
      this.set_google_map(lat_num, lng_num);
      //this.hideState1 = false;
      this.hideProgressBar2 = true;
      this.hideState1 = false;
      this.hideState2 = (!this.hideGeneral || this.hidePanel);
      this.hideState3 = true;
      console.log(this.hidePanel);
      console.log(this.hideGeneral);
    } catch (e:unknown) {
      console.log(e);
      this.show_error = true;
      this.hideProgressBar2 = true;
    }
  }

  callTomorrowIO(lat: string, lng: string) {
    let tomorrow_io_url = 'https://multiweatherview1234.wl.r.appspot.com/api/tomorrowIO/' + lat + '/' + lng
    console.log(tomorrow_io_url)
    try {this.http.get(tomorrow_io_url).subscribe(weather_info => {
      //console.log(weather_info)
      interface week_weather{
        data: data;
      }
      interface data{
        timelines: Array<time>;
      }
      interface time{
        intervals: Array<day_weather>;
      }
      interface day_weather{
        startTime: string;
        values: values;
      }
      interface values{
        humidity: number;
        precipitationProbability: number;
        sunriseTime: string;
        sunsetTime: string;
        temperatureMax: number;
        temperatureMin: number;
        temperatureApparent: number;
        visibility: number;
        weatherCode: string;
        windSpeed: number;
        cloudCover: number;
      }
      let weather_info_obj = <week_weather>weather_info;
      // get an array of day weather objects
      if (weather_info_obj.data == undefined) {
        this.show_error = true;
        this.hideProgressBar2 = true;
        this.hideState1 = true;
        this.hideState2 = true;
        return;
      }
      let day_weather_array = weather_info_obj.data.timelines[0].intervals;

      this.weather_array = day_weather_array;
      this.appendToTable(day_weather_array);
      this.createTempChart(day_weather_array);
      this.appendToDetail(day_weather_array[0]);
      this.addToTwitter(day_weather_array[0]);
    })} catch (e:unknown) {
      console.log('tomorrowio failed');
    }
  }

  callTomorrowIOChart(lat: string, lng: string) {
    let tomorrow_io_url = 'https://multiweatherview1234.wl.r.appspot.com/api/tomorrowIOChart/' + lat + '/' + lng
    try {this.http.get(tomorrow_io_url).subscribe(weather_info => {
      console.log(weather_info)
      interface week_weather2{
        data: data2;
      }
      interface data2{
        timelines: Array<time2>;
      }
      interface time2{
        intervals: Array<day_weather2>;
      }
      interface day_weather2{
        startTime: string;
        values: values2;
      }
      interface values2{
        humidity: number;
        pressureSeaLevel: number;
        temperature: number;
        windDirection: number;
        windSpeed: number;
      }
      let weather_info_obj = <week_weather2>weather_info;
      // get an array of day weather objects
      if (weather_info_obj.data == undefined) {
        this.show_error = true;
        this.hideProgressBar2 = true;
        this.hideState1 = true;
        this.hideState2 = true;
        this.error_state = true;
        return;
      }
      let day_weather_array = weather_info_obj.data.timelines[0].intervals;
      this.weather_array2 = day_weather_array;
      console.log(this.weather_array2);

      var new_meteo = new Meteogram(this.weather_array2, 'a');
      this.chartOptions2 = new_meteo.getChartOptions();
      console.log(this.chartOptions2);
      if (this.chartOptions2.series !== undefined) {
        this.chartOptions2 = { ...this.chartOptions2 };
       }
    })} catch (e:unknown) {
      console.log('tomorrowiochart failed');
    }
  }

  addToTwitter(weather_info: day_weather) {
    let date = new Date(weather_info.startTime);
    let weekday = days[Number(date.getDay())];
    let day = date.getDate();
    let month = months[Number(date.getMonth())];
    let year = date.getFullYear();
    let date_str = weekday + ', ' + month + '. ' + day + ', ' + year;
    let current_temp = String(weather_info.values.temperatureApparent);

    let message = 'The temperature in ' + (this.street == '' ? '' : this.street + ', ') +
      this.city + ', ' + this.state + ' on ' + date_str + ' is ' + current_temp + '°F and the conditions are ' +
      weatherCode[weather_info.values.weatherCode as keyof Record<string, Array<string>>][0];
    let message_reassemble = message.split(' ').join('+');
    this.tweet_message += 'text= ' + message_reassemble;
    this.tweet_message += '&hashtags=CSCI571WeatherForecast';
    console.log(this.tweet_message);
  }

  appendToDetail(weather_info: day_weather) {

    console.log('new detail appended')
    
    // first write today's date to detail panel
    let date = new Date(weather_info.startTime);
    let weekday = days[Number(date.getDay())];
    let day = date.getDate();
    let month = months[Number(date.getMonth())];
    let year = date.getFullYear();
    let date_str = weekday + ', ' + month + '.' + day + ', ' + year;
    let title = document.getElementById('detail_title') as HTMLElement;
    title.innerHTML = date_str;

    // then write status to panel
    let status = weatherCode[weather_info.values.weatherCode as keyof Record<string, Array<string>>][0]
    let detail_status = document.getElementById('detail_status') as HTMLElement;
    detail_status.innerHTML = status;

    // write max temp
    let temp_max = weather_info.values.temperatureMax;
    let detail_temp_max = document.getElementById('detail_temp_max') as HTMLElement;
    detail_temp_max.innerHTML = String(temp_max) + '&deg;F';

    // write min temp
    let temp_min = weather_info.values.temperatureMin;
    let detail_temp_min = document.getElementById('detail_temp_min') as HTMLElement;
    detail_temp_min.innerHTML = String(temp_min) + '&deg;F';

    // write apparent temp
    let temp_app = weather_info.values.temperatureApparent;
    let detail_temp_app = document.getElementById('detail_temp_app') as HTMLElement;
    detail_temp_app.innerHTML = String(temp_app) + '&deg;F';

    // write sun rise time
    let sun_rise = new Date(weather_info.values.sunriseTime).getHours();
    let sun_rise_t = document.getElementById('sun_rise_t') as HTMLElement;
    sun_rise_t.innerHTML = String(sun_rise) + ' AM';

    // write sun set time
    let sun_set = new Date(weather_info.values.sunsetTime).getHours() - 12;
    let sun_set_t = document.getElementById('sun_set_t') as HTMLElement;
    sun_set_t.innerHTML = String(sun_set) + ' PM';

    // write humidity
    let hum = weather_info.values.humidity;
    let humidity = document.getElementById('humidity') as HTMLElement;
    humidity.innerHTML = String(hum) + '%';

    // write wind speed
    let speed = weather_info.values.windSpeed;
    let wind_speed = document.getElementById('wind_speed') as HTMLElement;
    wind_speed.innerHTML = String(speed) + ' mph';

    // write visibility
    let vis = weather_info.values.visibility;
    let visibility = document.getElementById('visibility') as HTMLElement;
    visibility.innerHTML = String(vis) + 'mi';

    // write cloud cover
    let cc = weather_info.values.cloudCover;
    let cloud_cover = document.getElementById('cloud_cover') as HTMLElement;
    cloud_cover.innerHTML = String(cc) + "%";
  }

  createTempChart(day_weather_array: Array<day_weather>) {
    this.temp_array = [];
    day_weather_array.forEach((element) => {
      let date = new Date(element.startTime);
      let time = date.getTime()
      this.temp_array.push([time, element.values.temperatureMin, element.values.temperatureMax])
    });
    //console.log(this.temp_array)
    //console.log(this.chartOptions.series)
    if (this.chartOptions.series !== undefined && this.chartOptions.series[0].type === 'arearange') {
     this.chartOptions.series[0].data = this.temp_array;
     this.chartOptions = { ...this.chartOptions };
    }
  }

  appendToTable(day_weather_array: Array<day_weather>) {
    var i = 1
    day_weather_array.forEach((element) => {
      // write the date to the date column
      let date = new Date(element.startTime);
      let weekday = days[Number(date.getDay())];
      let day = date.getDate();
      let month = months[Number(date.getMonth())];
      let year = date.getFullYear();
      let date_str = weekday + ', ' + month + '.' + day + ', ' + year;
      //console.log(date_str);
      // create all html tags to wrap around given values
      var new_div = this.renderer.createElement('div');
      this.renderer.addClass(new_div, "row");
      this.renderer.addClass(new_div, "day_content_row");
      new_div.style.width = "110%";

      var new_index = this.renderer.createElement('div');
      this.renderer.addClass(new_index, "col-1");
      this.renderer.addClass(new_index, "day_view_content");
      this.renderer.addClass(new_index, "h6");
      new_index.style.width = "3%";
      new_index.innerHTML = String(i)
      var new_date = this.renderer.createElement('div');
      new_date.style.width = "23%";
      if (i == 1) {
        this.renderer.listen(new_date, 'click', () => {
          this.appendToDetail(this.weather_array[0]);
          this.hideState1 = true;
          this.hideState2 = false;
          this.toggle();
        });
      } else if (i == 2) {
        this.renderer.listen(new_date, 'click', () => {
          this.appendToDetail(this.weather_array[1]);
          this.hideState1 = true;
          this.hideState2 = false;
          this.toggle();
        });
      } else if (i == 3) {
        this.renderer.listen(new_date, 'click', () => {
          this.appendToDetail(this.weather_array[2]);
          this.hideState1 = true;
          this.hideState2 = false;
          this.toggle();
        });
      } else if (i == 4) {
        this.renderer.listen(new_date, 'click', () => {
          this.appendToDetail(this.weather_array[3]);
          this.hideState1 = true;
          this.hideState2 = false;
          this.toggle();
        });
      } else if (i == 5) {
        this.renderer.listen(new_date, 'click', () => {
          this.appendToDetail(this.weather_array[4]);
          this.hideState1 = true;
          this.hideState2 = false;
          this.toggle();
        });
      } else if (i == 6) {
        this.renderer.listen(new_date, 'click', () => {
          this.appendToDetail(this.weather_array[5]);
          this.hideState1 = true;
          this.hideState2 = false;
          this.toggle();
        });
      } else {
        this.renderer.listen(new_date, 'click', () => {
          this.appendToDetail(this.weather_array[6]);
          this.hideState1 = true;
          this.hideState2 = false;
          this.toggle();
        });
      }
      this.renderer.addClass(new_date, 'clickable_date');
      this.renderer.addClass(new_date, 'col-3');
      this.renderer.addClass(new_date, 'day_view_content');
      this.renderer.addClass(new_date, 'h6');
      i += 1;
    
      new_date.innerHTML = date_str;

      var new_status = this.renderer.createElement('div');
      this.renderer.addClass(new_status, "col-2");
      this.renderer.addClass(new_status, "day_view_content");
      this.renderer.addClass(new_status, "h6");
      var new_img = this.renderer.createElement('img');
      new_img.src = '/assets/' + weatherCode[element.values.weatherCode as keyof Record<string, Array<string>>][1];
      new_img.style.width = "28px";
      this.renderer.appendChild(new_status, new_img);
      new_status.insertAdjacentHTML('beforeend', weatherCode[element.values.weatherCode as keyof Record<string, Array<string>>][0]);
      

      var new_high_t = this.renderer.createElement('div');
      this.renderer.addClass(new_high_t, "col-2");
      this.renderer.addClass(new_high_t, "day_view_content");
      this.renderer.addClass(new_high_t, "h6");
      new_high_t.innerHTML = String(element.values.temperatureMax);
      var new_low_t = this.renderer.createElement('div');
      this.renderer.addClass(new_low_t, "col-2");
      this.renderer.addClass(new_low_t, "day_view_content");
      this.renderer.addClass(new_low_t, "h6");
      new_low_t.innerHTML = String(element.values.temperatureMin);
      var new_wind_speed = this.renderer.createElement('div');
      this.renderer.addClass(new_wind_speed, "col-2");
      this.renderer.addClass(new_wind_speed, "day_view_content");
      this.renderer.addClass(new_wind_speed, "h6");
      new_wind_speed.innerHTML = String(element.values.windSpeed)

      this.renderer.appendChild(new_div, new_index);
      this.renderer.appendChild(new_div, new_date);
      this.renderer.appendChild(new_div, new_status);
      this.renderer.appendChild(new_div, new_high_t);
      this.renderer.appendChild(new_div, new_low_t);
      this.renderer.appendChild(new_div, new_wind_speed);

      var new_hr = this.renderer.createElement('hr');
      new_hr.classList.add("hr", "day_content_row_hr");
      if (i != 2) {
        document.getElementById('day')?.appendChild(new_hr);
      }
      document.getElementById('day')?.appendChild(new_div);
    })
    return;
  }

  autoDetect(event: any) {
    if (event.target.checked) {
      this.LocationForm.controls['street'].disable();
      this.LocationForm.controls['city'].disable();
      this.LocationForm.controls['state'].disable();
      this.auto_detect = true
      document.getElementById('search_button')?.removeAttribute("disabled");
    } else {
      this.LocationForm.controls['street'].enable();
      this.LocationForm.controls['city'].enable();
      this.LocationForm.controls['state'].enable();
      this.auto_detect = false
    }
  }

  validation_check() {
    console.log('checking')
    if (this.LocationForm.controls['street'].value.trim() != "" && this.LocationForm.controls['state'].value.trim() != "") {
      if (typeof this.LocationForm.controls['city'].value === 'string') {
        if (this.LocationForm.controls['city'].value.trim() != "") {
          document.getElementById('search_button')?.removeAttribute("disabled");
        } else {
          document.getElementById('search_button')?.setAttribute("disabled", "");
        }
      } else {
        if (this.LocationForm.controls['city'].value.city.trim() != "") {
          document.getElementById('search_button')?.removeAttribute("disabled");
        } else {
          document.getElementById('search_button')?.setAttribute("disabled", "");
        }
      }
    } else {
      document.getElementById('search_button')?.setAttribute("disabled", "");
    }
  }

  ngOnInit(): void {

    this.transformDataToHighcharts();

    // create the location form and initially require all the fields
    this.LocationForm = this.fb.group({
      street: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required]
    })

    // add event listener to change in street input
    this.LocationForm.controls['street'].valueChanges.subscribe(value => {
      if (value == null) {
        return;
      }
      if (String(value).trim() == '') {
        document.getElementById('search_button')?.setAttribute("disabled", "");
      }
      this.validation_check();
    })

    // add event listener to change in state input
    this.LocationForm.controls['state'].valueChanges.subscribe(value => {
      if (value == null) {
        return;
      }
      if (String(value).trim() == '') {
        document.getElementById('search_button')?.setAttribute("disabled", "");
      }
      this.validation_check();
    })

    // add event listener to change in state input
    this.LocationForm.controls['city'].valueChanges.subscribe(value => {
      if (value == null) {
        return;
      }
      if (String(value).trim() == '') {
        document.getElementById('search_button')?.setAttribute("disabled", "");
      }
      this.validation_check();
    })

    // add event listener to change in city input
    this.city_state_pairs = this.LocationForm.controls['city'].valueChanges.pipe(
      startWith(''),
      map(value => {
      //var auto_complete_url =  'http://localhost:4200/autoComplete/' + value
      var auto_complete_url = 'https://multiweatherview1234.wl.r.appspot.com/api/autoComplete/' + value
      var pairs = Array<city_state_pair>()
      if (String(value).trim() == '') {
        if (typeof document !== 'undefined') {
          document.getElementById('search_button')?.setAttribute("disabled", "");
          this.validation_check();
        }
        return pairs;
      }
      try {this.http.get(auto_complete_url).subscribe(auto_complete_choices => {
        // process the auto complete choices returned from google map api
        //console.log(auto_complete_choices)
        
        interface term {
          offset: number;
          value: string;
        }

        interface prediction{
          //description: String;
          //match: Array<any>;
          //structured_formatting: Object;
          terms: Array<term>;
          //types: Array<String>;
        }
        interface prediction_object {
          predictions: Array<prediction>;
          status: number;
        }

        const result = <prediction_object>auto_complete_choices
        console.log(result)
        if (result.status != 200) {
          console.log(result.status)
        }

        result.predictions.forEach(element => {
          let new_pair = new city_state_pair(element.terms[element.terms.length - 3].value, element.terms[element.terms.length - 2].value)
          pairs.push(new_pair)
        })
      });}
      catch (e: unknown) {
        console.log(e)
      }
      return pairs;
    }));
  }
}
export class city_state_pair {
  city: string;
  state: string;

  constructor(city: string, state: string) {
    this.city = city;
    this.state = state;
  }
}
export class Meteogram {
  symbols: Array<string>;
  
  precipitations: Array<tuples>;
  precipitationsError: Array<number>;
  winds: Array<long_tups>;
  temperatures: Array<tuples>;
  pressures: Array<tuples>;
  chart_data: Array<day_weather2>;
  container: string;
  
  /*
  precipitations: Array<number>;
  precipitationsError: Array<number>;
  winds: Array<long_tups>;
  temperatures: Array<number>;
  pressures: Array<number>;
  chart_data: Array<day_weather2>;
  container: string;
  */
  constructor(chart_data: Array<day_weather2>, container: string) {
    this.symbols = [];
    this.precipitations = [];
    this.precipitationsError = []; // Only for some data sets
    this.winds = [];
    this.temperatures = [];
    this.pressures = [];
    this.chart_data = chart_data;
    this.container = container;
    console.log('start creating chart');
  }

  parseInputData() {
    var data_to_parse = this.chart_data
  for (let i = 0; i < data_to_parse.length; i++) {
    const x = Date.parse(data_to_parse[i].startTime)
    
    this.temperatures.push({x, y: Math.round(data_to_parse[i].values.temperature)})
    this.precipitations.push({x, y: Math.round(data_to_parse[i].values.humidity)})
    if (i % 2 === 0) {
      let wind_speed = data_to_parse[i].values.windSpeed
      wind_speed.toFixed(2)
      this.winds.push({x, value: wind_speed, direction: data_to_parse[i].values.windDirection});
      //this.winds.push({value: wind_speed, direction: Number(data_to_parse[i].values.windDirection)})
    }
    this.pressures.push({x, y: data_to_parse[i].values.pressureSeaLevel})
    
    /*
    this.temperatures.push(Math.round(data_to_parse[i].values.temperature))
    this.precipitations.push(Math.round(data_to_parse[i].values.humidity))
    if (i % 2 === 0) {
      let wind_speed = data_to_parse[i].values.windSpeed
      wind_speed.toFixed(2)
      this.winds.push({x, value: wind_speed, direction: data_to_parse[i].values.windDirection});
      //this.winds.push({value: wind_speed, direction: Number(data_to_parse[i].values.windDirection)})
    }
    this.pressures.push(data_to_parse[i].values.pressureSeaLevel)
    */
  }
  console.log(this.winds)
  // Create the chart when the data is loaded
  this.createChart();
  }

  createChart() {
    return this.getChartOptions();
  };

  getChartOptions() {
    return {
      chart: {
          width: typeof window === "undefined" ? 1000 : window.innerWidth * 0.7,
          marginBottom: 70,
          marginRight: 40,
          marginTop: 50,
          plotBorderWidth: 1,
          //height: 310,
          alignTicks: false,
          scrollablePlotArea: {
              minWidth: 800
          }
      },

      title: {
          text: 'Hourly Weather(For Next 5 Days)',
          style: {
              whiteSpace: 'nowrap',
              textOverflow: 'ellipsis'
          }
      },

      credits: {
          text: 'Forecast',
          position: {
              x: -40
          }
      },
      xAxis: [{ // Bottom X axis
          type: 'datetime',
          tickInterval: 1 * 36e5, // one hour
          minorTickInterval: 36e5, // one hour
          tickLength: 0,
          gridLineWidth: 1,
          gridLineColor: 'rgba(128, 128, 128, 0.1)',
          startOnTick: false,
          endOnTick: false,
          minPadding: 0,
          maxPadding: 0,
          offset: 30,
          showLastLabel: true,
          labels: {
              format: '{value:%H}'
          },
          crosshair: true
      }, { // Top X axis
          linkedTo: 0,
          type: 'datetime',
          tickInterval: 24 * 3600 * 1000,
          labels: {
              format: '{value:<span style="font-size: 12px; font-weight: ' +
                  'bold">%a</span> %b %e}',
              align: 'left',
              x: 3,
              y: 8
          },
          opposite: true,
          tickLength: 20,
          gridLineWidth: 1
      }],

      yAxis: [{ // temperature axis
          title: {
              text: null
          },
          labels: {
              format: '{value}°',
              style: {
                  fontSize: '10px'
              },
              x: -3
          },
          plotLines: [{ // zero plane
              value: 0,
              color: '#BBBBBB',
              width: 1,
              zIndex: 2
          }],
          maxPadding: 0.3,
          minRange: 8,
          tickInterval: 1,
          gridLineColor: 'rgba(128, 128, 128, 0.1)'

      }, { // precipitation axis
          title: {
              text: null
          },
          labels: {
              enabled: false
          },
          gridLineWidth: 0,
          tickLength: 0,
          minRange: 10,
          min: 0

      }, { // Air pressure
          allowDecimals: false,
          title: { // Title on top of axis
              text: 'hPa',
              offset: 0,
              align: 'high',
              rotation: 0,
              style: {
                  fontSize: '10px',
                  color: '#FFA500'
              },
              textAlign: 'left',
              x: 3
          },
          labels: {
              style: {
                  fontSize: '8px',
                  color: '#FFA500'
              },
              y: 2,
              x: 3
          },
          gridLineWidth: 0,
          opposite: true,
          showLastLabel: false
      }],

      legend: {
          enabled: false
      },
      /*
      plotOptions: {
          series: {
              pointPlacement: 'between',
          },
          windbarb: {
              dataGrouping: {
                forced: true,
                units: 
                  [[
                    'hour',
                    [2]
                ]]
              }
          }
      },
      */
      series: [{
          name: 'Temperature',
          data: this.temperatures,
          type: 'spline',
          marker: {
              enabled: false,
              states: {
                  hover: {
                      enabled: true
                  }
              }
          },
          tooltip: {
              pointFormat: '<span style="color:{point.color}">\u25CF</span>' +
                  ' ' +
                  '{series.name}: <b>{point.y}°F</b><br/>'
          },
          zIndex: 1,
          color: '#FF3333',
          negativeColor: '#48AFE8'
      }, {
          name: 'Humidity',
          data: this.precipitations,
          type: 'column',
          color: '#87CEEB',
          yAxis: 1,
          groupPadding: 0,
          pointPadding: 0,
          grouping: false,
          dataLabels: {
              enabled: false,
              filter: {
                  operator: '>',
                  property: 'y',
                  value: 0
              },
              style: {
                  fontSize: '8px',
                  color: 'grey', 
                  textShadow: '-1px 1px 0 #000, 1px 1px 0 #000, 1px -1px 0 #000, -1px -1px 0 #000'
              }
          },
          tooltip: {
              valueSuffix: '%'
          }
      }, {
          name: 'Air pressure',
          color: '#FFA500',
          data: this.pressures,
          marker: {
              enabled: false
          },
          shadow: false,
          tooltip: {
              valueSuffix: ' inHg'
          },
          dashStyle: 'shortdot',
          yAxis: 2
      }, {
          name: 'Wind',
          type: 'windbarb',
          id: 'windbarbs',
          color: 'blue',
          lineWidth: 1,
          data: this.winds,
          vectorLength: 10,
          yOffset: -15,
          tooltip: {
              valueSuffix: ' mph'
          },
          pointRange: 36e5,
      }]
  };
  }
}
export interface tuples{
  x: number;
  y: number;
}
export interface long_tups{
  x: number;
  value: number;
  direction: number;
}
export interface day_weather{
  startTime: string;
  values: values;
}
export interface values{
  humidity: number;
  precipitationProbability: number;
  sunriseTime: string;
  sunsetTime: string;
  temperatureMax: number;
  temperatureMin: number;
  temperatureApparent: number;
  visibility: number;
  weatherCode: string;
  windSpeed: number;
  cloudCover: number;
}
export interface day_weather2{
  startTime: string;
  values: values2;
}
export interface values2{
  humidity: number;
  pressureSeaLevel: number;
  temperature: number;
  windDirection: number;
  windSpeed: number;
}
export interface fav{
  city: string;
  state: string;
  lat: string;
  lng: string;
}
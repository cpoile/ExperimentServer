/*globals jQuery,$*/
Function.prototype.method = function (name, fn) {
   "use strict";
   if (this.prototype[name] !== 'function') {
      this.prototype[name] = fn;
      return this;
   }
};

var raceView = raceView || {};

raceView.teams = {
   0: {
      name: "ESB-Mercedes",
      driver: "Jean Hamilton",
      teamLogo: "esb.png",
      engineLogo: "mercedes.jpg",
      driverFlag: "hamilton.gif",
      teamShortName: "ESB"
   },
   1: {
      name: "Red Bull Racing-Renault",
      driver: "Sebastian Vettel",
      teamLogo: "red_bull_Renault.jpg",
      engineLogo: "renault.jpg",
      driverFlag: "vettel.gif",
      teamShortName: "Red Bull"
   },
   2: {
      name: "Scuderia Ferrari",
      driver: "Fernando Alonso",
      teamLogo: "Scuderia_Ferrari.png",
      engineLogo: "ferrari.jpg",
      driverFlag: "alonso.gif",
      teamShortName: "Ferrari"
   },
   3: {
      name: "McLaren-Mercedes",
      driver: "Jenson Button",
      teamLogo: "McLaren_Mercedes.jpg",
      engineLogo: "mercedes.jpg",
      driverFlag: "button.gif",
      teamShortName: "McLaren"
   },
   4: {
      name: "Lotus-Renault",
      driver: "Kimi Räikkönen",
      teamLogo: "Lotus-Renault.jpg",
      engineLogo: "renault.jpg",
      driverFlag: "raikkonen.gif",
      teamShortName: "Lotus"
   },
   5: {
      name: "Sauber-Ferrari",
      driver: "Kamui Kobayashi",
      teamLogo: "sauber_Ferrari.gif",
      engineLogo: "ferrari.jpg",
      driverFlag: "kobayashi.gif",
      teamShortName: "Sauber"
   },
   6: {
      name: "Force India-Mercedes",
      driver: "Paul di Resta",
      teamLogo: "Force_India_Mercedes.jpg",
      engineLogo: "mercedes.jpg",
      driverFlag: "resta.gif",
      teamShortName: "Force India"
   },
   7: {
      name: "Williams-Renault",
      driver: "Pastor Maldonado",
      teamLogo: "Williams_Renault.jpg",
      engineLogo: "renault.jpg",
      driverFlag: "maldonado.gif",
      teamShortName: "Williams"
   },
   8: {
      name: "STR-Ferrari",
      driver: "Daniel Ricciardo",
      teamLogo: "STR-Ferrari.png",
      engineLogo: "ferrari.jpg",
      driverFlag: "ricciardo.gif",
      teamShortName: "Toro Rosso"
   },
   9: {
      name: "Marussia-Cosworth",
      driver: "Timo Glock",
      teamLogo: "Marussia_Cosworth.jpg",
      engineLogo: "cosworth.jpg",
      driverFlag: "Glock.gif",
      teamShortName: "Marussia"
   },
   10: {
      name: "Caterham-Renault",
      driver: "Heikki Kovalainen",
      teamLogo: "Caterham_Renault.jpg",
      engineLogo: "renault.jpg",
      driverFlag: "kovalainen.gif",
      teamShortName: "Caterham"
   },
   11: {
      name: "HRT-Cosworth",
      driver: "Pedro de la Rosa",
      teamLogo: "HRT_Cosworth.jpg",
      engineLogo: "cosworth.jpg",
      driverFlag: "rosa.gif",
      teamShortName: "HRT"
   }
};

raceView.tracks = {
   0: {
      name: "Australian Grand Prix",
      country: "Australia",
      track: "Melbourne Grand Prix Circuit",
      laps: 58,
      circuitLength: "5.303 km (3.295 mi)",
      raceLength: "307.574 km (191.071 mi)",
      flagImg: "australia.png",
      trackImg: "melbourne_australia_orig.png",
      conditionsImg: "sunny.gif",
      conditionsText: "Sunny / track is hot"
   },
   1: {
      name: "Malaysian Grand Prix",
      country: "Malaysia",
      track: "Sepang International Circuit",
      laps: 56,
      circuitLength: "5.543 km (3.444 mi)",
      raceLength: "310.408 km (192.878 mi)",
      flagImg: "malaysia.png",
      trackImg: "sepang_malaysia_orig.png",
      conditionsImg: "scatteredShowers.gif",
      conditionsText: "Light showers / track is loose"
   },
   2: {
      name: "Chinese Grand Prix",
      country: "China",
      track: "Shanghai International Circuit",
      laps: 56,
      circuitLength: "5.451 km (3.387 mi)",
      raceLength: "305.066 km (189.559 mi)",
      flagImg: "china.png",
      trackImg: "shanghai_china_orig.png",
      conditionsImg: "mainlyCloudy.gif",
      conditionsText: "Cloudy / track is cold"
   },
   3: {
      name: "Bahrain Grand Prix",
      country: "Bahrain",
      track: "Bahrain International Circuit",
      laps: 57,
      circuitLength: "5.412 km (3.363 mi)",
      raceLength: "308.405 km (191.634 mi)",
      flagImg: "bahrain.png",
      trackImg: "sakhir_bahrain_orig.png",
      conditionsImg: "sunny.gif",
      conditionsText: "Sunny / track is hot"
   },
   4: {
      name: "Spanish Grand Prix",
      country: "Spain",
      track: "Circuit de Catalunya",
      laps: 66,
      circuitLength: "4.655 km (2.892 mi)",
      raceLength: "307.104 km (190.825 mi)",
      flagImg: "spain.png",
      trackImg: "montmelo_spain_orig.png",
      conditionsImg: "rain.gif",
      conditionsText: "Heavy rain / track is dangerous"
   },
   5: {
      name: "Monaco Grand Prix",
      country: "Monaco",
      track: "Circuit de Monaco",
      laps: 78,
      circuitLength: "3.340 km (2.075 mi)",
      raceLength: "260.520 km (161.887 mi)",
      flagImg: "monaco.png",
      trackImg: "monte-carlo_monaco_orig.png",
      conditionsImg: "mainlySunny.gif",
      conditionsText: "Sunny / track is clear"
   },
   6: {
      name: "Canadian Grand Prix",
      country: "Canada",
      track: "Circuit Gilles Villeneuve",
      laps: 70,
      circuitLength: "4.361 km (2.709 mi)",
      raceLength: "305.270 km (189.694 mi)",
      flagImg: "canada.png",
      trackImg: "montreal_canada_orig.png",
      conditionsImg: "scatteredFlurries.gif",
      conditionsText: "Flurries / track is in Canada"
   },
   7: {
      name: "Singapore Grand Prix",
      country: "Singapore",
      track: "Marina Bay Street Circuit",
      laps: 61,
      circuitLength: "5.067 km (3.148 mi)",
      raceLength: "309.087 km (192.066 mi)",
      flagImg: "singapore.png",
      trackImg: "singapore_orig.png",
      conditionsImg: "sunny.gif",
      conditionsText: "Sunny / track is hot"
   }
};

raceView.xyPos = {
   0: {
      x: 0,
      y: 0
   },
   1: {
      x: 166,
      y: 0
   },
   2: {
      x: 334,
      y: 0
   },
   3: {
      x: 0,
      y: 166
   },
   4: {
      x: 166,
      y: 166
   },
   5: {
      x: 334,
      y: 166
   },
   6: {
      x: 0,
      y: 332
   },
   7: {
      x: 166,
      y: 332
   },
   8: {
      x: 334,
      y: 332
   },
   9: {
      x: 0,
      y: 498
   },
   10: {
      x: 166,
      y: 498
   },
   11: {
      x: 334,
      y: 498
   }
};

var ca_usask_chdp_ExpServerCore_View_jsExtensions_RaceView = function () {
   "use strict";

   var base, newCoords, elemTeam = [
      ], element, raceHistory, historyOfPosChange, i, teamNum, posText, track;

   element = this.getElement();
   element.id = 'base';
   base = $('#base');

   // first add the teams in their starting positions.
   raceHistory = this.getState().raceHistory;
   historyOfPosChange = this.getState().historyOfPosChange;
   for (i = 0; i < 12; i += 1) {
      teamNum = raceHistory[0][i];
      newCoords = 'top: ' + raceView.xyPos[i].y + 'px; left: ' + raceView.xyPos[i].x + 'px;';
      $('#base').append($('#team' + teamNum));
      $('#team' + teamNum).attr('style', newCoords);
      if (i < 9) {
         posText = '0' + (i + 1);
      } else {
         posText = (i + 1).toString();
      }
      $('#team' + teamNum + ' .position').text(posText);
      // Build array of references to each team's element so we don't have to find them each loop.
      // elemTeam[teamNum] = $('#team' + teamNum);

      // set up track information and other misc.
      track = raceView.tracks[this.getState().trackNum];
      $('#raceName').text(track.name);
      $('#flagImg').attr('src', qualifyURL('/VAADIN/themes/expserver/images/flags/' + track.flagImg));
      $('#trackName').text(track.track);
      $('#trackImg').attr('src', qualifyURL('/VAADIN/themes/expserver/images/tracks/' + track.trackImg));
      $('#circuitLength').text(track.circuitLength);
      $('#raceLength').text(track.raceLength);
      $('#laps').text('Out of ' + track.laps);
      $('#curLap').text('Lap #0');
      $('#conditionsImg').attr('src', qualifyURL('/VAADIN/themes/expserver/images/weather/' + track.conditionsImg));
      $('#conditionsText').text(track.conditionsText);
      $('#startingPositionsText').html('After qualifying rounds, pole position goes to <i>' + raceView.teams[raceHistory[0][0]].driver + '</i>');

   }

   this.onStateChange = function () {
   };

   var lapDelay = 1000;
   this.startRace = function () {
      $('#startingPositionsText').hide();
      var curLapElem = $('#curLap'), curLap = 0, numLaps = this.getState().raceHistory.length, interval, histOfChange = this.getState().historyOfPosChange, posText;

      var incrementRace1Lap = function () {
         var i, newPos, newCoords, curTeam;
         curLap += 1;
         curLapElem.text('Lap #' + (curLap+1));
         for (i = 0; i < histOfChange[curLap - 1].length; i += 1) {
            curTeam = histOfChange[curLap - 1][i].team;
            newPos = histOfChange[curLap - 1][i].to;
            if (i < 9) {
               posText = '0' + (newPos + 1).toString();
            } else {
               posText = (newPos + 1).toString();
            }

            newCoords = {
               left: raceView.xyPos[newPos].x + 'px',
               top: raceView.xyPos[newPos].y + 'px'
            };
            $('#team' + curTeam + ' .position').text(posText);
            $('#team' + curTeam).animate(newCoords, lapDelay);
         }
         if (curLap >= numLaps-1) {
            chdp.WatchingRaceView.raceIsFinished(true);
         } else {
            setTimeout(incrementRace1Lap, lapDelay);
         }
      };
      // and, start the race:
      setTimeout(incrementRace1Lap, lapDelay);
   };

   this.changeSpeed = function (speedSetting) {
      // will be between 1 (1000ms delay) and 10 (100 ms delay).
      lapDelay = 1100 - speedSetting * 100;
      console.log("Setting race speed to " + speedSetting + "; lapDelay: " + lapDelay);
   };
};

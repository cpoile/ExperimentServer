/*globals chdp*/

var chdp_graph = chdp_graph || {};

chdp_graph.initialized = false;

chdp_graph.unInitialize = function () {
   "use strict";

   chdp_graph.initialized = false;
};

/**
 * Called from the viewControl.js.
 */
chdp_graph.moveOnScreen = function () {
   "use strict";

   // move it's whole container to the persProj page.
   $('#graph').append($('#proj2GraphContainer'));
   $('#proj2GraphContainer').attr('style', 'width: 575px; height: 340px;');
};

chdp_graph.tracks = {
   0: {
      name: "Australian Grand Prix",
      country: "Australia",
      track: "Melbourne Grand Prix Circuit",
      laps: 58,
      circuitLength: "5.303 km (3.295 mi)",
      raceLength: "307.574 km (191.071 mi)",
      flagImg: "australia.png",
      trackImg: "australia.png"
   },
   1: {
      name: "Malaysian Grand Prix",
      country: "Malaysia",
      track: "Sepang International Circuit",
      laps: 56,
      circuitLength: "5.543 km (3.444 mi)",
      raceLength: "310.408 km (192.878 mi)",
      flagImg: "malaysia.png",
      trackImg: "malaysia.png"
   },
   2: {
      name: "Chinese Grand Prix",
      country: "China",
      track: "Shanghai International Circuit",
      laps: 56,
      circuitLength: "5.451 km (3.387 mi)",
      raceLength: "305.066 km (189.559 mi)",
      flagImg: "china.png",
      trackImg: "china.png"
   },
   3: {
      name: "Bahrain Grand Prix",
      country: "Bahrain",
      track: "Bahrain International Circuit",
      laps: 57,
      circuitLength: "5.412 km (3.363 mi)",
      raceLength: "308.405 km (191.634 mi)",
      flagImg: "china.png",
      trackImg: "china.png"
   },
   4: {
      name: "Spanish Grand Prix",
      country: "Spain",
      track: "Circuit de Catalunya",
      laps: 66,
      circuitLength: "4.655 km (2.892 mi)",
      raceLength: "307.104 km (190.825 mi)",
      flagImg: "spain.png",
      trackImg: "spain.png"
   },
   5: {
      name: "Monaco Grand Prix",
      country: "Monaco",
      track: "Circuit de Monaco",
      laps: 78,
      circuitLength: "3.340 km (2.075 mi)",
      raceLength: "260.520 km (161.887 mi",
      flagImg: "monaco.png",
      trackImg: "monaco.png"
   },
   6: {
      name: "Canadian Grand Prix",
      country: "Canada",
      track: "Circuit Gilles Villeneuve",
      laps: 70,
      circuitLength: "4.361 km (2.709 mi)",
      raceLength: "305.270 km (189.694 mi))",
      flagImg: "canada.png",
      trackImg: "canada.png"
   },
   7: {
      name: "Singapore Grand Prix",
      country: "Singapore",
      track: "Marina Bay Street Circuit",
      laps: 61,
      circuitLength: "5.067 km (3.148 mi)",
      raceLength: "309.087 km (192.066 mi)",
      flagImg: "singapore.png",
      trackImg: "singapore.png"
   }
};

// add for IE8 compatibility
(function () {
   "use strict";
   $('script').append('<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="jqueryFlot/excanvas.min.js"></script><![endif]-->');
}());

var ca_usask_chdp_ExpServerCore_View_jsExtensions_FlotGraph = function () {
   "use strict";

   var element = this.getElement(),
      container,
      tempGraphElem,
      updateGraph = function (that) {
         chdp_graph.proj2Work.data = that.getState().proj2Work;
         chdp_graph.proj2TotalWork.data = that.getState().proj2TotalWork;
         chdp_graph.myPlot.setData([chdp_graph.proj2TotalWork, chdp_graph.proj2Work ]);
         chdp_graph.myPlot.setupGrid();
         chdp_graph.myPlot.draw();
      };

   element.id = 'proj2Graph';
   container = $('#proj2Graph');
   container.attr('style', 'width: 575px; height: 340px;');

   this.onStateChange = function () {
      console.log("flotGraph stateChangeCalled. initialized? -- " + this.getState().isInitialized);
      if (!chdp_graph.initialized) {
         var labels = [], i;
         for (i = 0; i < 8; i += 1) {
            labels.push([i, chdp_graph.tracks[i].country]);
         }
         chdp_graph.proj2Work = {
            label: 'Work Completed on Concept Car per Round',
            color: 'green',
            data: this.getState().proj2Work,
            bars: { show: true },
            hoverable: true
         };
         chdp_graph.proj2TotalWork = {
            label: 'Work Completed in Total',
            color: 'rgb(51,0,26)',
            data: this.getState().proj2TotalWork,
            lines: {
               show: true,
               fill: true,
               fillColor: "rgba(128, 0, 64, 0.3)"
            },
            points: { show: true }
         };
         chdp_graph.options = {
            yaxis: { min: 0 },
            xaxis: { ticks: labels },
            legend: { margin: [5, -20] }
         };

         chdp_graph.myPlot = $.plot($('#proj2Graph'), [ chdp_graph.proj2TotalWork, chdp_graph.proj2Work ], chdp_graph.options);
         //moveOnScreen();
         chdp_graph.initialized = true;
      } else {
         updateGraph(this);
      }
   };
};
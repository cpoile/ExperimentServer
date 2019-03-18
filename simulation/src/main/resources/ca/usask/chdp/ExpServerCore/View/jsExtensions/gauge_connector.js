/*globals Gauge,Donut*/

var ca_usask_chdp_ExpServerCore_View_jsExtensions_Gauge = function () {
   "use strict";
   var element = this.getElement(),
      options = null,
      gauge = null,
      canv = document.createElement('canvas');

   canv.setAttribute('id', this.getState().elementId);
   canv.setAttribute('class', 'gaugeCanvas');
   element.appendChild(canv);

   var generateOpts = function (getState) {
      var myAngle = 0.15;
      var myLineWidth = 0.44;
      if (getState.isReversed) {
         myAngle = 0.35;
         myLineWidth = 0.1;
      }

      return {
         lines: 12, // The number of lines to draw
         angle: myAngle, // The length of each line
         lineWidth: myLineWidth, // The line thickness
         pointer: {
            length: 0.91, // The radius of the inner circle
            strokeWidth: 0.044, // The rotation offset
            color: '#000000' // Fill color
         },
         colorStart: getState.rgbColor, // Colors
         colorStop: getState.rgbColor, // just experiment with them
         strokeColor: '#E0E0E0', // to see which ones work best for you
         generateGradient: true
      };
   };

   this.onStateChange = function () {
      var opts, curValue, origMaxValue = 0, maxValue = 0;

      function updateCurValue(getState) {
         if (getState.isReversed) {
            curValue = getState.maxValue - getState.curValue + 1;
         } else {
            curValue = getState.curValue + 1;
         }
      }
      function updateMaxValue(getState) {
         if (getState.isReversed) {
            origMaxValue = getState.maxValue;
            maxValue = getState.maxValue + 2;
         } else {
            origMaxValue = getState.maxValue;
            maxValue = getState.maxValue + 1;
         }
      }

      if (gauge === null) {
         opts = generateOpts(this.getState());
         if (this.getState().isReversed) {
            gauge = new Donut(element.children[0]).setOptions(opts);
         } else {
            gauge = new Gauge(element.children[0]).setOptions(opts); // create sexy gauge!
         }
         updateMaxValue(this.getState());
         gauge.maxValue = maxValue;
         gauge.animationSpeed = 32; // set animation speed (32 is default value)
         updateCurValue(this.getState());
         gauge.set(curValue); // set actual value
      } else {
         if ((this.getState().maxValue) !== origMaxValue) {
            updateMaxValue(this.getState());
            gauge.maxValue = maxValue;
         }
         opts = generateOpts(this.getState());
         gauge.setOptions(opts);
         updateCurValue(this.getState());
         gauge.set(curValue);
      }
   };
};

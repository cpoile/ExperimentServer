/*globals setupSVOSlider,createMsg*/
window.ca_usask_chdp_survey_View_jsExtensions_SvoSlider = function () {
   "use strict";

   console.log("svoSlider.js started.");
   console.log("setting up div elements for slider.");

      var elem = this.getElement();

//      this.onStateChange = function () {
//      };

   this.initGame = function () {
      svoSlider.initialize(elem);
   };
};

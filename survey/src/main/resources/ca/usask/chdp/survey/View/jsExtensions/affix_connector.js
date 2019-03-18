
var ca_usask_chdp_survey_View_jsExtensions_Affix = function () {
   "use strict";
   var initialized = false;

   this.onStateChange = function () {
      if (!initialized) {
         initialized = true;
         $('#svsInfoBox').affix({
            offset: {
               top: 790, bottom: 270
            }
         });
      } else {
         console.log('Affix onStateChange was called. Weird.');
      }
   };
};


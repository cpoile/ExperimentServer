window.ca_usask_chdp_signup_jsExtensions_JSPersistentCmds = function () {
   "use strict";

   var arrayOfCmds = this.getState().cmds,
      arrayOfState = this.getState().boolSetOnOrOff,
      i;

   /**
    * functions to be called from server-side code.
    */
   var refreshScrollspy = function () {
         $(window).each(function () {
            var $spy = $(this).scrollspy('refresh');
         });
      };
   var removeId = function (x) {
      $('#' + x).slideUp(2000, function () {
         $('a[href=#' + x + ']').slideUp(1000,
            function () {
               $('#' + x).remove();
               $('a[href=#' + x + ']').remove();
               refreshScrollspy();
            });
      });
   };
   var hideDisablingDiv = function (x) {
      $(x).fadeOut(2000, function () {
         $(this).hide();
      });
   };

   /**
    * initialization of connector
    */
   for (i = 0; i < arrayOfState.length; i += 1) {
      if (arrayOfState[i] === true) {
         $.globalEval(arrayOfCmds[i]);
      }
   }

   this.onStateChange = function () {
      var newArrayOfState = this.getState().boolSetOnOrOff;
      for (i = 0; i < arrayOfState.length; i += 1) {
         if (newArrayOfState[i] !== arrayOfState[i]) {
            arrayOfState[i] = newArrayOfState[i];
            eval(arrayOfCmds[i]);
         }
      }
   };
};

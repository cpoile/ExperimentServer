/*globals loadScrollspy, scrollspyOnLoad*/

var ca_usask_chdp_signup_jsExtensions_NavList = function () {
   "use strict";
   var initialized = false;

   this.onStateChange = function () {

      if (!initialized) {
         initialized = true;
         //loadScrollspy(window.jQuery);
         //scrollspyOnLoad();

//         $('body').data('spy', 'scroll');
//         $('body').data('target', '.intro-sidebar');
//         setupScrollspy(window.jQuery);

         // side bar
         $('.sidenav').affix({
            offset: {
               top: 170, bottom: 270
            }
         });

         // https://github.com/twitter/bootstrap/pull/3829
         //$('.intro-sidebar').scrollspy({offset: 70, targetContainer: $('#' + this.getState().contentContainerId)});

         //$('#scrollcontent').scrollspy();
         $(window).scrollspy({offset: 70});
      } else {
         // reset scrollspy, maybe for new content.
         console.log('navList onStateChange was called');
         //$(window).scrollspy.refresh();
      }


   };
};


/*globals chdp_graph*/

//TABS
// my namespace
var chdpWatchingA = chdpWatchingA || {};

chdpWatchingA.ViewControl = function () {
   "use strict";

   this.initializeGlobalNav = function (ulNavElements) {

      ulNavElements.each(function () {
         var $navActive,
            $navContent,
            $navLinks = $(this).find('a');

         $navActive = $($navLinks.filter('[href="' + location.hash + '"]')[0] || $navLinks[0]);
         $navActive.addClass('active');
         $navActive.parent('li').addClass('current');
         $navContent = $($navActive.attr('href'));
         $navContent.find('.sidebarLoc').append($('#rightsidebar').detach());

         $navLinks.not($navActive).each(function () {
            $($(this).attr('href')).hide();
         });

         $(this).on('click', 'a', function (e) {
            $navActive.removeClass('active');
            $navActive.parent('li').removeClass('current');

            $navContent.hide();

            $navActive = $(this);
            $navContent = $($(this).attr('href'));

            $navActive.addClass('active');
            $navActive.parent('li').addClass('current');
            $navContent.show();

            // add the message window.
            $navContent.find('.sidebarLoc').append($('#rightsidebar').detach());

            e.preventDefault();
         });
      });
   };

};

/*
 * The connection to the server:
 * */
window.ca_usask_chdp_ExpServerCore_View_jsExtensions_JSViewControlWatchingA = function () {
   "use strict";

   var viewControl = new chdpWatchingA.ViewControl();

   viewControl.initializeGlobalNav($('ul.globalNav'));

   this.onStateChange = function () {
   };
};
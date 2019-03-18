/*globals chdp_graph*/

//TABS
// my namespace
var chdp = chdp || {};

chdp.ViewControl = function () {
   "use strict";
   var isPersProjEnabled = false;

   this.initializeTabs = function (ulTabsElements) {
      ulTabsElements.each(function () {
         var $active, $content, $links = $(this).find('a');

         $active = $($links.filter('[href="' + location.hash + '"]')[0] || $links[0]);
         $active.addClass('active');
         $content = $($active.attr('href'));

         $links.not($active).each(function () {
            $($(this).attr('href')).hide();
         });

         $(this).on('click', 'a', function (e) {
            $active.removeClass('active');
            $content.hide();

            $active = $(this);
            $content = $($(this).attr('href'));

            $active.addClass('active');
            $content.show();

            e.preventDefault();
         });


      });

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
               if (isPersProjEnabled) {
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
               } else {
                  return false;
               }
            });
         });
      };

      this.changeToWaitingForBState = function () {
         //$('#carpartdetails').hide('slow');

         $('.work.v-widget').hide('slow');
         $('.daysleft').each(function () {
            $(this).hide('slow');
         });
         $('#teamProjCar').animate({left: '1400px'}, 3000, function () {
            $('#teamProjCar').hide();
         });
         $('#ppwork').hide('slow');
         $('a').each(function () {
            $(this).addClass('watchingPartner');
            // add the .viewControl namespace so that we don't remove all click handlers when removing it later.
            $(this).filter('href').on('click.viewControl', function () {
               return false;
            });
            // reenable with .off
         });
      };

      this.changeToStartingWorkState = function () {
         $('#carpartdetails').show();
         $('.daysleft').each(function () {
            $(this).show();
         });
         $('#teamProjCar').show();
         $('#ppwork').show();
         $('a').each(function () {
            $(this).addClass('watchingPartner');
            $(this).filter('href').off('click.viewControl');
         });
         this.setPersProjEnabled(false);
      };

      this.setGoalReachedForPart = function (partNum, isGoalReached) {
         var partSelector = $('ul.tabs').find('a').filter('[href="#tab' + partNum + '"]'),
         // get all tags that are affected within the tab of this part.
            affectedText = $('.textAffectedByGoalComplete', $('#tab' + partNum));

         if (isGoalReached) {
            partSelector.addClass('goalReached');
            affectedText.each(function () {
               $(this).removeClass('incompleteTextColor');
               $(this).addClass('completedTextColor');
            });
         } else {
            partSelector.removeClass('goalReached');
            affectedText.each(function () {
               $(this).removeClass('completedTextColor');
               $(this).addClass('incompleteTextColor');
            });
         }
      };

      this.isPersProjEnabled = function () {
         return isPersProjEnabled;
      };

      this.setPersProjEnabled = function (enabled) {
         isPersProjEnabled = enabled;
         if (enabled) {
            $('a[href=#persProject]').removeClass('disabled');
            chdp_graph.moveOnScreen();
            // Set up the bootstrap tooltip.

         } else {
            $('a[href=#persProject]').addClass('disabled');
         }
      };
   };
};

/*
 * The connection to the server:
 * */
window.ca_usask_chdp_ExpServerCore_View_jsExtensions_JSViewControl = function () {
   "use strict";

   var isAWaitingForB = false,
      viewControl = new chdp.ViewControl(),
      isGoalReached = [false, false, false],
      i;

   viewControl.initializeTabs($('ul.tabs'));
   viewControl.initializeGlobalNav($('ul.globalNav'));

   this.onStateChange = function () {
      if (this.getState().isPersProjectEnabled && !viewControl.isPersProjEnabled()) {
         viewControl.setPersProjEnabled(true);

      }
      if (this.getState().isAWaitingForB && !isAWaitingForB) {
         isAWaitingForB = true;
         viewControl.changeToWaitingForBState();
      } else if (!this.getState().isAWaitingForB && isAWaitingForB) {
         isAWaitingForB = false;
         viewControl.changeToStartingWorkState();
      }
      for (i = 0; i < isGoalReached.length; i += 1) {
         viewControl.setGoalReachedForPart(i + 1, this.getState().isGoalReached[i]);
      }
   };
};
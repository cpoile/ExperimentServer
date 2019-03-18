/*global guider, getState, myGuiders, ca_usask_chdp_registerCallbackOnChange, ca_usask_chdp_workOnProj2ForXDays, qualifyURL, ca_usask_chdp_setInputStates, ca_usask_TutorialJSViewControl_RunAForXDaysOn, ca_usask_TutorialJSViewControl_DoneWatchingA, createGuidersForTeamProjLastDayOfWorkA*/

var ca_usask_chdp_ExpServerCore_View_jsExtensions_TooltipGuider = function () {
   "use strict";

   var ttgName = this.getState().ttgName;

   this.onStateChange = function () {
      var myState = this.getState();

      // TODO: replace with real viewcontrol:
//      console.log(myState.viewSettings);
//      console.log(myState.viewSettings.tab4);
//      console.log(myState.viewSettings.tab4);
   };

   // Call from the host page's attach handler so that we're sure that all page components have been added.
   this.initGuiders = function () {
      var ttgInit = 'build_' + ttgName;
      // use the ttgName to initialize its guiders.
      var initFn = window.myGuiders[ttgInit];
      if (typeof initFn === 'function') {
         initFn();
      } else {
         console.error("Big problem. " + ttgName + " was not found in the myGuiders object.");
      }
   };

   this.gotoStep = function (step) {
      console.log("GOTO step: " + step);
      var id = ttgName + "_" + step;
      if (typeof guider._guiders[id] === "undefined") {
         console.error("Cannot find guider with id " + id);
      } else {
         guider.show(id);
      }
   };
   this.callmyGuidersFn = function (fn) {
      console.log("callmyGuidersFn called. Fn: " + fn);
      myGuiders[fn]();
   };

   var testing_workOnButtonXForYTimes = function (button, numTimes, callback) {
      // work and set callbacks until all done the work.
      var workCount = 0;
      // enable the button, in case it isn't.
      myGuiders.setInputStatesForTutorialA({workButton1: true, workButton2: true, workButton3: true});
      // make an accessable callback and have it keep track. If the workCount has been reached, run callback.
      window.testing_recursiveFn = {};
      window.testing_recursiveFn.funct = function () {
         workCount += 1;
         if (workCount >= numTimes) {
            callback();
         } else {
            ca_usask_chdp_registerCallbackOnChange("part" + button + "Chance", 1, "testing_recursiveFn.funct");
            $('#tut_WorkButton' + button).click();
         }
      };
      ca_usask_chdp_registerCallbackOnChange("part" + button + "Chance", 1, "testing_recursiveFn.funct");
      // delay the click, because it wasn't working right for some reason.
      var clickit = _.bind($('#tut_WorkButton' + button).click, $('#tut_WorkButton' + button));
      _.delay(clickit, 1000);
   };
   var testing_workOnPersProjForYDays = function (numDays, callback) {
      // enable button
      myGuiders.setInputStatesForTutorialA({workButtonProj2: true});
      // make an accessable callback and have it call the callback when the numdays has run.
      window.testing_recursiveFn = {};
      window.testing_recursiveFn.funct = function () {
         callback();
      };
      ca_usask_chdp_registerCallbackOnChange("daysLeft", 1, "testing_recursiveFn.funct");
      ca_usask_chdp_workOnProj2ForXDays(numDays);
   };

   this.testing_doAutoWork_tutorialA = function () {
      // this is just for skipping work while testing the guider system.
      // move to step will be called here.

//      guider.show(ttgName + "_" + 15);
      testing_workOnButtonXForYTimes(1, 4, function () {
         myGuiders.setInputStatesForTutorialA({tab3: true});
//         //var tabs = $('a[href^=#tab]');
         $("#tut_PartSelector3").click();
//         myGuiders.createGuidersForTab3();
//         guider.show(ttgName + "_" + 23);

         testing_workOnButtonXForYTimes(3, 8, function () {
            myGuiders.setInputStatesForTutorialA({tab2: true});
            $("#tut_PartSelector2").click();
//            myGuiders.createGuidersForTab2A();
//            guider.show(ttgName + "_" + 29);

            ///
            testing_workOnButtonXForYTimes(2, 2, function () {
               myGuiders.setInputStatesForTutorialA({persProj: true});
               $("#tut_PersProj").click();
//               myGuiders.createGuidersForPersProj();
//               guider.show(ttgName + "_" + "40");
               testing_workOnPersProjForYDays(11, function () {
                  myGuiders.setInputStatesForTutorialA({teamProj: true});
                  $("#tut_TeamProj").click();
                  // refocus on engine.
                  myGuiders.setInputStatesForTutorialA({tab1: true, tab2: true, tab3: true});
                  $('#tut_PartSelector1').click();
                  myGuiders.createGuidersForTeamProjLastDayOfWorkA();
                  guider.show(ttgName + "_" + "48");

//                                    ca_usask_chdp_registerCallbackOnChange("daysLeft", 1, "myGuiders.finishedAllDays");
//                                    guider.next();
//                                    testing_workOnButtonXForYTimes(1, 1, function () {
//                                       guider.show();
//                                    })

               })
            });
            ///

         });
      });
   };
   this.testing_doAutoWork_tutorialB_Watching = function () {
      console.log("testing_doAutoWorkB_Watching called.");
      //this.gotoStep("8_6");
      ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part1", 4, 100, "myGuiders.testingB_return1");
      myGuiders.testingB_return1 = function () {
         ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part3", 8, 100, "myGuiders.testingB_return2");
      };
      myGuiders.testingB_return2 = function () {
         ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part2", 2, 100, "myGuiders.testingB_return3");
      };
      myGuiders.testingB_return3 = function () {
         ca_usask_TutorialJSViewControl_RunAForXDaysOn("PersProj", 10, 100, "myGuiders.testingB_return4");
      };
      myGuiders.testingB_return4 = function () {
         ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part1", 2, 100, "myGuiders.testingB_return5");
      };
      myGuiders.testingB_return5 = function () {
         ca_usask_TutorialJSViewControl_DoneWatchingA();
         console.log("finished autowork for B Watching A.");
      };
   };
   this.testing_doAutoWork_tutorialB_Working = function () {
      console.log("testing_doAutoWorkB_Working called.");
      //this.gotoStep("1");
      testing_workOnButtonXForYTimes(1, 5, function () {
         testing_workOnButtonXForYTimes(3, 11, function () {
            testing_workOnButtonXForYTimes(2, 10, function () {
            });
         });
      });
   };
};

// we can't initialize guiders until the page has been drawn.
// But we want an object to put all of our guider initialization code:
var myGuiders = {};

(function () {
   "use strict";

   /**
    * Callbacks from the server during the tutorial.
    */
   myGuiders.part1DataChanged1 = function () {
      console.log("part1DataChanged1 called.");
      myGuiders.setInputStatesForTutorialA();
      guider.next();
   };
   myGuiders.part1DataChanged2 = function () {
      console.log("part1DataChanged2 called.");
      guider.next();
   };
   myGuiders.part3DataChanged1 = function () {
      console.log("part3DataChanged1 called.");
      guider.next();
   };
   myGuiders.part2DataChanged1 = function () {
      console.log("part2DataChanged1 called.");
      guider.next();
   };
   myGuiders.finishedPersGoals = function () {
      console.log("finishedPersGoals called.");
      // disable the work buttons for now.  And this will also disable the link back to the F1 proj so it doesn't trigger the guider system.
      myGuiders.setInputStatesForTutorialA();
      guider.next();
   };
   myGuiders.workedOnProj2_1 = function () {
      console.log("workedOnProj2_1 called.");
      myGuiders.setInputStatesForTutorialA();
      guider.next();
   };
   myGuiders.workedOnProj2_2 = function () {
      console.log("workedOnProj2_2 called.");
      // setup the next guider by allowing the f1 team project tab, and advancing the guider after clicking it.
      // disable the link back to the pers so it doesn't trigger the guider system.
      myGuiders.setInputStatesForTutorialA({teamProj: true});
      // set the click on tut_TeamProj as the trigger for the next guider.
      $('ul.globalNav').on('click.tutorial', 'a', function () {
         $('ul.globalNav').off(".tutorial");
         // disable the link back to the F1 proj for now, but allow all part tabs
         console.log("ul globalnav click was captured");
         myGuiders.setInputStatesForTutorialA({tab1: true, tab2: true, tab3: true});
         // refocus on engine.
         $('#tut_PartSelector1').click();
         myGuiders.createGuidersForTeamProjLastDayOfWorkA();
         if (_(guider._guiders).size() !== 48) {
            console.error("createGuidersForTeamProjLastDayOfWork had only " + _(guider._guiders).size() + " guiders created.");
         }
         guider.next();
      });
      guider.next();
   };
   myGuiders.finishedAllDays = function () {
      console.log("finishedAllDays called.");
      guider.hideAll();
   };

   /**
    *  helpers:
    */
   myGuiders.getUrlFor = function (str) {
      return qualifyURL('/VAADIN/themes/expserver/images/tutorial/' + str);
   };
   var imgFilenames = [
      "car.png", "driveA.png", "engineA.png", "arrowDrive.png", "tireA.png", "arrowEngine.png",
      "arrowTire.png", "arrowCar.png", "suspensionB.png", "intakeB.png", "wing1B.png", "wing2B.png",
      "arrowSuspension.png", "arrowIntake.png", "arrowWing1.png", "arrowWing2.png",
      "../car/base_colourimage.png"
   ];

   // preload images
   var preloadedImgs = {};
   _(imgFilenames).forEach(function (file) {
      var img = $("<img/>");
      img.src = myGuiders.getUrlFor(file);
      preloadedImgs[file] = img;
   });

   myGuiders.makeImg = function (id, src, top, left, display) {
      if (!_.has(preloadedImgs, src)) {
         console.error("Have not loaded: " + src);
         return 0;
      } else {
         return $("<img/>").attr({
            id: id,
            src: preloadedImgs[src].src,
            width: preloadedImgs[src].width,
            height: preloadedImgs[src].height,
            'class': "tutImg"
         }).css({ top: top || 0, left: left || 0 });
      }
   };

   /**
    * Set input states of the interface. Defaults are false.
    * the stateOptions are named: workButton1, 2, 3, workButtonProj2, tab1, 2, 3, teamProj, persProj
    *
    */
   myGuiders.setInputStatesForTutorialA = function (stateOptions) {
      // disable buttons
      var states = (!_.isObject(stateOptions)) ? {} : stateOptions;
      ca_usask_chdp_setInputStates((states.workButton1 === true), (states.workButton2 === true), (states.workButton3 === true), (states.workButtonProj2 === true));
      // first clear everything that may have been put on the tab switchers
      // then disable tab switchers
      var tabs = $('a[href^=#tab]');
      $(tabs).off('.tutorial');
      if (states.tab1 !== true) {
         $(tabs[0]).on('click.tutorial', false);
      }
      if (states.tab2 !== true) {
         $(tabs[1]).on('click.tutorial', false);
      }
      if (states.tab3 !== true) {
         $(tabs[2]).on('click.tutorial', false);
      }
      // Now change input state of the global navs.
      $('#tut_TeamProj').off('.tutorial');
      $('#tut_PersProj').off('.tutorial');
      if (states.teamProj !== true) {
         $('#tut_TeamProj').on('click.tutorial', false);
      }
      if (states.persProj !== true) {
         $('#tut_PersProj').on('click.tutorial', false);
      }
   };
   /**
    * Set input states of the interface. Defaults are false.
    * the stateOptions are named: teammate, standings
    *
    */
   myGuiders.setInputStatesForTutorialB_Watching = function (stateOptions) {
      // disable buttons
      var states = (!_.isObject(stateOptions)) ? {} : stateOptions;
      $('#tut_Teammate').off('.tutorial');
      $('#tut_Standings').off('.tutorial');
      if (states.teammate !== true) {
         $('#tut_Teammate').on('click.tutorial', false);
      }
      if (states.standings !== true) {
         $('#tut_Standings').on('click.tutorial', false);
      }
   };
   /**
    * Set input states of the interface. Defaults are false.
    * the stateOptions are named: workButton1, 2, 3, workButtonProj2, tab1, 2, 3
    *
    */
   myGuiders.setInputStatesForTutorialB_Working = function (stateOptions) {
      // disable buttons
      var states = (!_.isObject(stateOptions)) ? {} : stateOptions;
      ca_usask_chdp_setInputStates((states.workButton1 === true), (states.workButton2 === true), (states.workButton3 === true));
      // first clear everything that may have been put on the tab switchers
      // then disable tab switchers
      var tabs = $('a[href^=#tab]');
      $(tabs).off('.tutorial');
      if (states.tab1 !== true) {
         $(tabs[0]).on('click.tutorial', false);
      }
      if (states.tab2 !== true) {
         $(tabs[1]).on('click.tutorial', false);
      }
      if (states.tab3 !== true) {
         $(tabs[2]).on('click.tutorial', false);
      }
//      // Now change input state of the global navs.
//      $('#tut_TeamProj').off('.tutorial');
//      $('#tut_PersProj').off('.tutorial');
//      if (states.teamProj !== true) {
//         $('#tut_TeamProj').on('click.tutorial', false);
//      }
//      if (states.persProj !== true) {
//         $('#tut_PersProj').on('click.tutorial', false);
//      }
   };
}());


/**
 * A code snippet to keep for later.
 * This will call an update callback (logchanges in the example) when the object changes with real
 * differences.
 */
//   var test = {name: 'Joe', age: 21};
//   var watchForChange = function (fnOnChange, prop, oldv, newv) {
//      if (oldv === newv) {
//         return oldv;
//      } else {
//         fnOnChange(prop, oldv, newv);
//         return newv;
//      }
//   };
//   var logChanges = watchForChange.fill(function (prop, oldv, newv) {
//      console.log(prop + " changed from: " + oldv + " to " + newv);
//   });
//   Object.watch(test, 'age', logChanges);

/**
 * Snippet not used above, but may be useful later:
 * Count clicks on a target. After numClicks remove click listener and run callback.
 */
//   var setNumClicksOnElem = function (divId, numClicks, callback) {
//      var $div = (divId[0] !== '#') ? $('#' + divId) : $(divId);
//      var clickCount = 0;
//
//      $div.on('click.tutorial', function () {
//         clickCount += 1;
//         if (clickCount >= numClicks) {
//            $(this).off('click.tutorial');
//            callback();
//         }
//      })
//   };

/**
 * Not used above:
 * for dimming the screen and helping them focus on one section of the game at a time.
 */
//var showOverlay = function () {
//   $("#guider_overlay").fadeIn("fast");
//};
//
//var hideOverlay = function () {
//   $("#guider_overlay").fadeOut("fast");
//};
//
//var divCurrentlyFocusedOn = {
//   div: null,
//   prevZIndex: 0
//};
//var removeFocus = function () {
//   divCurrentlyFocusedOn.div.css('z-index', divCurrentlyFocusedOn.prevZIndex);
//   divCurrentlyFocusedOn.divId = null;
//};
//var focusOnDiv = function (divId) {
//   var $div = (divId[0] !== '#') ? $('#' + divId) : $(divId);
//   // remove focus if have any:
//   if (divCurrentlyFocusedOn.divId) {
//      removeFocus();
//   }
//   showOverlay();
//   divCurrentlyFocusedOn.div = $div;
//   divCurrentlyFocusedOn.prevZIndex = $div.css('z-index');
//   $div.css('z-index', 1000000);
//};

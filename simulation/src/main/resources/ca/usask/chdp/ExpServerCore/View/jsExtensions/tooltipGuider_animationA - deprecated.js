/*global myGuiders, guider*/

(function () {
   "use strict";
   /**
    * A simple animation queue to let us sequentially animate steps.
    */
   var animStepQueue = [];
   var animStepisRunning = false;

   function animStepRun() {
      if (_.isEmpty(animStepQueue)) {
         animStepisRunning = false;
      } else {
         var nextStep = animStepQueue.shift();
         nextStep(animStepRun);
      }
   }

   /**
    * Add some animations to the queue.
    * Animation must accept a callback function as a parameter, and it must be called after the sequential aspect of the animation is finished.
    * @param step any function that accepts a callback parameter when it is called.
    */
   function animStepAdd(step) {
      if (!_.isFunction(step)) {
         console.error("step must be a function.");
      } else {
         animStepQueue.push(step);
         if (animStepisRunning === false) {
            animStepisRunning = true;
            animStepRun();
         }
      }
   }

   /**
    * Tutorial step functions
    */
   var animDelay = 1000;

   myGuiders.aAnim_Part1 = function () {
      // disable next button, animation, then reenable next button with new function.
      var $button = $(this);
      $button.off("click");
      $button.addClass("disabled");

      // Start animation queue here.
      // (removed step1)
      // STEP 2:
      var step2 = function (callback) {
         $("<div id='tut_txtDrive' class='tutText' style='top: 160px; left: 80px;'>Powertrain</div>").appendTo("#tutA5_container").fadeIn(animDelay);
         myGuiders.makeImg("tut_drive", "driveA.png", "190px", "60px").css({'z-index': 3}).appendTo("#tutA5_container").fadeIn(animDelay);
         myGuiders.makeImg("tut_car", "car.png", "350px", "0px").appendTo("#tutA5_container").fadeIn(animDelay, callback);
      };
      animStepAdd(step2);

      var step3 = function (callback) {
         $("<div id='tut_txtEngine' class='tutText' style='top: 160px; left: 220px;'>Engine</div>").css({'z-index': 3}).appendTo("#tutA5_container").fadeIn(animDelay);
         myGuiders.makeImg("tut_engine", "engineA.png", "220px", "220px").appendTo("#tutA5_container").fadeIn(animDelay, callback);
      };
      animStepAdd(step3);

      var step4 = function (callback) {
         $("<div id='tut_txtTire' class='tutText' style='top: 160px; left: 320px'>Wheel Assembly</div>").css({'z-index': 3}).appendTo("#tutA5_container").fadeIn(animDelay);
         myGuiders.makeImg("tut_tire", "tireA.png", "220px", "340px").appendTo("#tutA5_container").fadeIn(animDelay, callback);
      };
      animStepAdd(step4);

      var step5 = function (callback) {
         $("<div id='tut_txtInst2' style='top: 550px; left: 50px' class='tutText'>Your job is to work on the parts and send the car to your teammate.</div>").appendTo("#tutA5_container").fadeIn(animDelay, callback);
      };
      animStepAdd(step5);

      var step999 = function (callback) {
         $button.removeClass("disabled");
         $button.on("click", myGuiders.aAnim_Part2);
         callback();
      };
      animStepAdd(step999);
   };

   myGuiders.aAnim_Part2 = function () {
      // disable next button, animation, then reenable next button with new function.
      var $button = $(this);
      $button.off("click");
      $button.addClass("disabled");

      var step6 = function (callback) {
         myGuiders.makeImg("tut_arrowDrive", "arrowDrive.png", "280px", "88px").css({'z-index': 2}).appendTo("#tutA5_container").fadeIn(animDelay / 2, function () {
            $('#tut_drive').animate({top: '+=190', left: '-=38'}, animDelay, function () {
               $("#tut_arrowDrive").fadeOut(animDelay);
               $(this).fadeOut(animDelay, callback);
            });
         });
      };
      animStepAdd(step6);

      var step8 = function (callback) {
         myGuiders.makeImg("tut_arrowEngine", "arrowEngine.png", "266px", "150px").css({'z-index': 2}).appendTo("#tutA5_container").fadeIn(animDelay / 2, function () {
            $('#tut_engine').css({'z-index': 3}).animate({top: '+=170', left: '-=80'}, animDelay, function () {
               $("#tut_arrowEngine").fadeOut(animDelay);
               $(this).fadeOut(animDelay, callback);
            });
         });
      };
      animStepAdd(step8);

      var step9 = function (callback) {
         myGuiders.makeImg("tut_arrowTire", "arrowTire.png", "270px", "320px").css({'z-index': 2}).appendTo("#tutA5_container").fadeIn(animDelay / 2, function () {
            $('#tut_tire').animate({top: '+=140', left: '-=20'}, animDelay, function () {
               $("#tut_arrowTire").fadeOut(animDelay);
               $(this).fadeOut(animDelay, callback);

            });
         });
      };
      animStepAdd(step9);

      var step10 = function (callback) {
         $([$("#tut_txtDrive"), $("#tut_txtEngine"), $("#tut_txtTire"), $("#tut_txtInst2")]).fadeOut(animDelay);
         myGuiders.makeImg("tut_arrowCar", "arrowCar.png", "500px", "420px").css({'z-index': 1}).appendTo("#tutA5_container").fadeIn(animDelay / 2, function () {
            $('#tut_car').animate({left: '+=540'}, animDelay, function () {
               $("#tut_arrowCar").fadeOut(animDelay, callback);
            });
         });
      };
      animStepAdd(step10);

      var step11 = function (callback) {
         $("<div id='tut_txtInst3' style='top: 550px; left: 438px' class='tutText'>Your partner takes your work and adds their parts.<br/>Your partner is in charge of: Suspension, Air Intake System, and Wing Aerodynamics</div>").appendTo("#tutA5_container").fadeIn(animDelay, function () {
            callback();
         });
      };
      animStepAdd(step11);

      var step999 = function (callback) {
         $button.removeClass("disabled");
         $button.on("click", myGuiders.aAnim_Part3);
         callback();
      };
      animStepAdd(step999);
   };

   myGuiders.aAnim_Part3 = function () {
      var $button = $(this);
      $button.off("click");
      $button.addClass("disabled");

      var step12 = function (callback) {
         $("<div id='tut_txtSuspension' class='tutText' style='top: 130px; left: 640px;'>Suspension</div>").appendTo("#tutA5_container").fadeIn(animDelay);
         myGuiders.makeImg("tut_suspension", "suspensionB.png", "161px", "644px").appendTo("#tutA5_container").fadeIn(animDelay, callback);
      };
      animStepAdd(step12);

      var step13 = function (callback) {
         $("<div id='tut_txtIntake' class='tutText' style='top: 130px; left: 750px;'>Air Intake</div>").appendTo("#tutA5_container").fadeIn(animDelay);
         myGuiders.makeImg("tut_intake", "intakeB.png", "166px", "738px").appendTo("#tutA5_container").fadeIn(animDelay, callback);
      };
      animStepAdd(step13);

      var step14 = function (callback) {
         $("<div id='tut_txtWing' class='tutText' style='top: 130px; left: 870px;'>Wing Aerodynamics</div>").appendTo("#tutA5_container").fadeIn(animDelay);
         myGuiders.makeImg("tut_wing1", "wing2B.png", "157px", "860px").appendTo("#tutA5_container").fadeIn(animDelay);
         myGuiders.makeImg("tut_wing2", "wing1B.png", "156px", "950px").appendTo("#tutA5_container").fadeIn(animDelay, callback);
      };
      animStepAdd(step14);

      var step15 = function (callback) {
         myGuiders.makeImg("tut_arrowSuspension", "arrowSuspension.png", "280px", "700px").css({'z-index': 2}).appendTo("#tutA5_container").fadeIn(animDelay / 2, function () {
            $('#tut_suspension').animate({top: '+=212', left: '+=210'}, animDelay, function () {  // 361  731
               $("#tut_arrowSuspension").fadeOut(animDelay);
               $(this).fadeOut(animDelay, callback);
            });
         });
      };
      animStepAdd(step15);

      var step16 = function (callback) {
         myGuiders.makeImg("tut_arrowIntake", "arrowIntake.png", "250px", "680px").css({'z-index': 2}).appendTo("#tutA5_container").fadeIn(animDelay / 2, function () {
            $('#tut_intake').animate({top: '+=226', left: '-=107'}, animDelay, function () {        // 356 688
               $("#tut_arrowIntake").fadeOut(animDelay);
               $(this).fadeOut(animDelay, callback);
            });
         });
      };
      animStepAdd(step16);

      var step17 = function (callback) {
         myGuiders.makeImg("tut_arrowWing1", "arrowWing1.png", "230px", "610px").css({'z-index': 2}).appendTo("#tutA5_container").fadeIn(animDelay / 2);
         myGuiders.makeImg("tut_arrowWing2", "arrowWing2.png", "280px", "930px").css({'z-index': 2}).appendTo("#tutA5_container").fadeIn(animDelay / 2, function () {
            $('#tut_wing1').animate({top: '+=199', left: '-=308'}, animDelay);                  // 347 710
            $('#tut_wing2').animate({top: '+=226', left: '-=28'}, animDelay, function () {      // 346  920
               $("#tut_arrowWing1").fadeOut(animDelay);
               $("#tut_arrowWing2").fadeOut(animDelay);
               $("#tut_wing1").fadeOut(animDelay);
               $(this).fadeOut(animDelay, callback);
            });
         });

      };
      animStepAdd(step17);

      var step18 = function (callback) {
         $([$("#tut_txtSuspension"), $("#tut_txtIntake"), $("#tut_txtWing")]).fadeOut(animDelay);
         $("#tut_txtInst3").fadeOut(animDelay, callback);
      };
      animStepAdd(step18);

      var step19 = function (callback) {
         $("<div id='tut_txtInst4' style='top: 550px; left: 438px' class='tutText'>After finishing their work, your partner sends the completed F1 car off to the race.<br>You will be able to watch the race to see how well your team performed.</div>").appendTo("#tutA5_container").fadeIn(animDelay, callback);
      };
      animStepAdd(step19);

      var step999 = function (callback) {
         $button.removeClass("disabled");
         $button.on("click", myGuiders.aAnim_Part4);
         callback();
      };
      animStepAdd(step999);
   };

   myGuiders.aAnim_Part4 = function () {
      var $button = $(this);
      $button.off("click");
      $button.addClass("disabled");

      var step20 = function (callback) {
         myGuiders.makeImg("tut_arrowCar2", "arrowCar.png", "500px", "920px").css({'z-index': 1}).appendTo("#tutA5_container").fadeIn(animDelay / 2, function () {
            $('#tut_car').animate({left: '+=540'}, animDelay, function () {
               $('#tut_car').hide();
               $("#tut_arrowCar2").fadeOut(animDelay, callback);
            });
         });
      };
      animStepAdd(step20);

      var step999 = function (callback) {
         $button.removeClass("disabled");
         $button.on("click", guider.next);
         callback();
      };
      animStepAdd(step999);
   };
}());
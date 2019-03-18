/*global guider,myGuiders, ca_usask_chdp_setWorkProj2ButtonToXDays, ca_usask_chdp_registerCallbackOnChange, ca_usask_chdp_fastForwardThroughBsWork, chdp_WatchingRaceTutorialView_enableStartButton, chdp_RaceResultsTutorialView_enableContinueButton, chdp_DamageReportTutorialView_enableContinueButton, ca_usask_TutorialJSViewControl_RunAForXDaysOn, ca_usask_TutorialJSViewControl_DoneWatchingA*/

/**
 * This will be loaded after the connector script, and after the myGuiders object has been created,
 * but the connector will not call these building functions until it has been called at the end of the page's
 * onAttach method. By then these functions will be available.
 */

(function () {
   "use strict";

   var workDelay = 1500;
   // add methods to the myGuiders object.
   /**
    * PUBLIC FUNCTIONS:
    *
    * these are the guiders. There will be alot of them.
    * Self invoked function to create them when object is created.
    * Need to wait until the DOM is available before we can create them.
    */
      //-------------------------------------------------------------------------------
      // Intro and watching A screen.
      //-------------------------------------------------------------------------------
   myGuiders.build_tutorialB_Watching = function () {
      /**
       * Here we will create each guider.
       */
      guider.createGuider({
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialB_Watching();
               guider.next();
            }}
         ],
         description: "Thank you for taking the time to participate in this experiment. It is designed to be interesting, so we hope you enjoy it.<br/><br/> This tutorial will guide you through the game and help you understand what you are doing. If at any time you are not sure what to do, just raise your hand and the experimenter will help you. <br/><br/> After finishing the tutorial you will be ready to play the game and interact with your Teammate. If you play the game well enough, you may earn $20." + "<br/><br/>Let's get started.",
         id: "tutorialB_Watching_1",
         next: "tutorialB_2",
         overlay: true,
         title: "Welcome to the experiment!"
      });

      guider.createGuider({
         buttons: [
            {name: "Next"}
         ],
         description: "In this experiment you will be playing the the role of an engineer working for Mercedes-Benz.",
         id: "tutorialB_2",
         next: "tutorialB_3",
         overlay: true,
         title: "You are an engineer"
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         description: "This is the Formula 1 car you will be building with your teammate. It is modelled on the 2013 Petronas-Mercedes-AMG F1 team's car. You will help build it before each race.  <img src='" + myGuiders.getUrlFor("../car/base_colourimage.png") + "'/>",
         width: 1150,
         id: "tutorialB_3",
         next: "tutorialB_4",
         title: "Your team's F1 Project"
      });

      guider.createGuider({
         attachTo: "#nextRaceLabel",
         buttons: [
            {name: "Next"}
         ],
         description: "There are 8 races in total. This section shows the next race your team will compete in: <em>Race #1, the Australian Grand Prix.</em>",
         id: "tutorialB_4",
         next: "tutorialB_5",
         position: 9,
         title: "How many races?"
      });

      guider.createGuider({
         buttons: [
            {name: "Next"}
         ],
         width: 1300,
         description: "This is the 2013 Mercedes Engineering team. The team strips and rebuilds the entire car in between races. Every part of the machine is damaged by the race, and it is the Engineer's job to repair the parts for the next race. <img src='" + myGuiders.getUrlFor("petronas_f1_team.jpg") + "'/>",
         id: "tutorialB_5",
         next: "tutorialB_6",
         overlay: true,
         title: "Working with your teammate to build the F1 car"
      });

      guider.createGuider({
         buttons: [
            {name: "Continue", onclick: _.partial(myGuiders.anim_Part1, "B")}
         ],
         width: 1150,
         description: "<div id='tutA6_textCont'><div id='tutA6_text1'>In real life the team has dozens of engineers, but in this game the team is smaller. The team is you, and your partner.<div>Your partner will take the role of \"First Engineer\" in charge of three parts of the car: the Powertrain, the Engine, and the Wheel Assembly.</div></div></div> <div id='tutA5_container'><img class='tutImg' width='80' height='80' style='top: 20px; left: 147px; display: block;' src='" + myGuiders.getUrlFor("engineer.gif") + "' /><img class='tutImg'  width='80' height='80' style='top: 20px; left: 710px; display: block;' src='" + myGuiders.getUrlFor("engineer.gif") + "' /><div class='tutText' style='top: -17px; left:135px; display: block; text-align: center;'>Your Partner<br/>First Engineer</div><div class='tutText' style='top: -17px; left:690px;  display: block; text-align: center;'>You<br/>Second Engineer</div></div>",
         id: "tutorialB_6",
         next: "tutorialB_7",
         overlay: true,
         title: "Working with your teammate to build the F1 car"
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "How to Play the Game",
         description: "Now we will take you through a round of the game and learn how to play while we go.",
         id: "tutorialB_7",
         next: "tutorialB_8_1"
      });

      guider.createGuider({
         attachTo: "#tut_carInfo",
         buttons: [
            {name: "Next"}
         ],
         title: "Details of the Car",
         description: "Here we show you information about the three ways we measure a car's performance. <br/><br/>You don't need to understand the details of this information in order to play the game. But they do explain why an Engineer in the real world would be interested in this statistic.",
         id: "tutorialB_8_1",
         next: "tutorialB_8_2",
         width: 230,
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_TopSpeed",
         buttons: [
            {name: "Next"}
         ],
         title: "Top Speed",
         description: "This round your team starts with a current top speed of 270 kph.",
         id: "tutorialB_8_2",
         next: "tutorialB_8_3",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_TopSpeedGoal",
         buttons: [
            {name: "Next"}
         ],
         title: "Top Speed Goal",
         description: "In order to place first in the next race, you estimate you will need to reach a top speed of 280 kph.",
         id: "tutorialB_8_3",
         next: "tutorialB_8_4",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_LinAccel",
         buttons: [
            {name: "Next"}
         ],
         title: "Current Value",
         description: "Each of the three aspects of the car has a current value...",
         id: "tutorialB_8_4",
         next: "tutorialB_8_5",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_LinAccelGoal",
         buttons: [
            {name: "Next"}
         ],
         title: "Goal",
         description: "And a goal. <br/><br/>If you reach your goal, you will win the next race.",
         id: "tutorialB_8_5",
         next: "tutorialB_8_6",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#daysLeft",
         buttons: [
            {name: "Next", onclick: function () {
               // this function will be called from the connector JS object,
               // so we have added it to tooltipGuider_tutorialB.
               // Then, disable the "next" button.
               $(this).addClass("guider_button_hidden");
               ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part1", 2, workDelay, "myGuiders.returnFromTutB_ADidWork_GuiderNext");
            }}
         ],
         title: "Teammate's Job",
         description: "Your teammate works on the car first. He or she has 26 days to work on the F1 car. Press next and your teammate will do 2 days of work.",
         id: "tutorialB_8_6",
         next: "tutorialB_8_7",
         position: 3
      });

      myGuiders.returnFromTutB_ADidWork_GuiderNext = function () {
         guider.next();
      };

      guider.createGuider({
         attachTo: "#daysLeft",
         buttons: [
            {name: "Next"}
         ],
         title: "Teammate's Job",
         description: "Notice that you are kept up to date as your teammate works on their parts of the car.",
         id: "tutorialB_8_7",
         next: "tutorialB_8_8",
         position: 3
      });

      guider.createGuider({
         attachTo: "#tut_TopSpeed",
         buttons: [
            {name: "Next"}
         ],
         title: "Engine Upgrade",
         description: "You might have noticed that Top Speed of the car has increased. The more time your partner spends working on the Engine, the better the Top Speed becomes.",
         id: "tutorialB_8_8",
         next: "tutorialB_8_9",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_TopSpeed",
         buttons: [
            {name: "Next", onclick: function () {
               // this function will be called from the connector JS object,
               // so we have added it to tooltipGuider_tutorialB.
               // Then, disable the "next" button.
               $(this).addClass("guider_button_hidden");
               ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part1", 2, workDelay, "myGuiders.returnFromTutB_ADidWork_GuiderNext");
            }}
         ],
         title: "Engine Upgrade",
         description: "Watch the top speed this time. Press next and we'll wait for your teammate to upgrade the Engine again.",
         id: "tutorialB_8_9",
         next: "tutorialB_8_10",
         width: 230,
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_TopSpeedGoal",
         buttons: [
            {name: "Next"}
         ],
         title: "You depend on your teammate",
         description: "When your partner upgrades a part, she helps you get the car closer to the goal. You depend on your teammate to upgrade her parts so that you can reach your goal.",
         id: "tutorialB_8_10",
         next: "tutorialB_8_11",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_LatTopSpeed",
         buttons: [
            {name: "Next", onclick: function () {
               // this function will be called from the connector JS object,
               // so we have added it to tooltipGuider_tutorialB.
               // Then, disable the "next" button.
               $(this).addClass("guider_button_hidden");
               ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part3", 4, workDelay, "myGuiders.returnFromTutB_ADidWork_GuiderNext");
            }}
         ],
         title: "Upgrade Lateral Top Speed",
         description: "Press the next button and we will wait for your partner to upgrade the Wheel Assembly.",
         id: "tutorialB_8_11",
         next: "tutorialB_8_12",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#A_p3p",
         buttons: [
            {name: "Next", onclick: function () {
               // this function will be called from the connector JS object,
               // so we have added it to tooltipGuider_tutorialB.
               // Then, disable the "next" button.
               $(this).addClass("guider_button_hidden");
               ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part3", 4, workDelay, "myGuiders.returnFromTutB_ADidWork_GuiderNext");
            }}
         ],
         title: "You are a team",
         description: "Both you and your teammate are working to upgrade the car's Lateral Top Speed. Your teammate upgrades the car's Wheel Assembly, and you upgrade the Suspension System. <br/><br/>Let's wait for your partner to upgrade the Wheel Assembly again.",
         id: "tutorialB_8_12",
         next: "tutorialB_8_13",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#A_p2n",
         buttons: [
            {name: "Next"}
         ],
         title: "Linear Acceleration",
         description: "And these final two parts affect the time it takes to accelerate from 0 - 100 kph.",
         id: "tutorialB_8_13",
         next: "tutorialB_8_14",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_LinAccel",
         buttons: [
            {name: "Next", onclick: function () {
               $(this).addClass("guider_button_hidden");
               ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part2", 2, workDelay, "myGuiders.returnFromTutB_ADidWork_GuiderNext");
            }}
         ],
         title: "Linear Acceleration",
         description: "The quicker the car can reach 100 kph, the better chance it has of winning the next race. That's why lower is better.<br/><br/>Press next and we'll wait for your partner to work on the Powertrain.",
         id: "tutorialB_8_14",
         next: "tutorialB_8_15",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#daysLeft",
         buttons: [
            {name: "Next", onclick: function () {
               function removeHandlers() {
                  $("#chatinput").off(".tutorial");
                  $("#tut_ChatButton").off(".tutorial");
               }

               var advanceFn = function () {
                  if ($("#chatinput").val().length > 0) {
                     removeHandlers();
                     guider.next();
                  }
               };
               $("#chatinput").on("keydown.tutorial", function (e) {
                  if (e.keyCode === 13) {
                     advanceFn();
                  }
               });
               $("#tut_ChatButton").on("click.tutorial", function () {
                  console.log("clicked on chat button.");
                  advanceFn();
               });
               guider.next();
            }}
         ],
         title: "Your Teammate's Days Left",
         description: "At this point your teammate has 12 days left.",
         id: "tutorialB_8_15",
         next: "tutorialB_8_16",
         position: 3
      });

      guider.createGuider({
         attachTo: "#chatinput",
         buttons: [
            // guider.next is handled when they finish the goal, in myGuiders.typedIntoChatbox
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Your Teammate",
         description: "This is a good time to mention the chat function of the program. When you play the game you will be able to communicate with your teammate. You may chat about anything with your teammate, but we ask that you not reveal your identity. If you reveal your identity, you will no longer be anonymous, and the experiment assumes that no-one knows exactly who they are playing with. Thank you for your cooperation!<br/><br/> Type \"Hello\" into the chat box to test it out.",
         id: "tutorialB_8_16",
         next: "tutorialB_8_17",
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_MessageList",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialB_Watching({standings: true});
               $('ul.globalNav').on('click.tutorial', 'a', function () {
                  $('ul.globalNav').off('.tutorial');
                  // disable the link back to Teammate for now.
                  myGuiders.setInputStatesForTutorialB_Watching();
                  // start guider system back up.
                  myGuiders.createGuidersForB_Standings();
                  guider.next();
               });
               guider.next();
            }}
         ],
         title: "Remember your teammate",
         description: "Don't forget to pay attention to the message box during the game.",
         id: "tutorialB_8_17",
         next: "tutorialB_8_18",
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_Standings",
         buttons: [
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Current Standings",
         description: "While you are waiting for your teammate to complete her half of the car, you can click this link to view how well your team is doing in the races.",
         id: "tutorialB_8_18",
         next: "tutorialB_8_19",
         position: 6
      });

   };

   myGuiders.createGuidersForB_Standings = function () {
      guider.createGuider({
         attachTo: "#raceResults",
         buttons: [
            {name: "Next"}
         ],
         title: "Results of last race",
         description: "This table will show the results of the most recent race your team competed in. It's empty now (of course) because you haven't competed in any races yet.",
         id: "tutorialB_8_19",
         next: "tutorialB_8_20",
         position: 6
      });


      guider.createGuider({
         attachTo: "#overallResults",
         buttons: [
            {name: "Next"}
         ],
         title: "Overall standings",
         description: "And this table shows the overall standings for every team. There are 8 races in total, and the rankings are based on the team's point total. Just like the real F1 circuit, the following points are given for each place:<br/><br/>1st Place: 25 points<br/>2nd Place: 18 points<br/>3rd Place: 15 points<br/>4th Place: 12 points<br/>5th Place: 10 points<br/>6th Place: 8 points<br/>7th Place: 6 points<br/>8th Place: 4 points<br/>9th Place: 2 points<br/>10th Place: 1 point<br/><br/>Why is this important information?",
         id: "tutorialB_8_20",
         next: "tutorialB_8_21",
         position: 6
      });


      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Winning races is important",
         description: "Your managers want you to win races. The more races your team wins, the better the company looks.<br/><br/>Winning races is so important that your managers have promised a bonus to the Second Engineer (that's you) who gets the highest point total at the end of these 8 races.",
         id: "tutorialB_8_21",
         next: "tutorialB_8_22"
      });


      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialB_Watching({teammate: true});
               $('ul.globalNav').on('click.tutorial', 'a', function () {
                  $('ul.globalNav').off('.tutorial');
                  // disable the link back to Teammate for now.
                  myGuiders.setInputStatesForTutorialB_Watching();
                  // start guider system back up.
                  myGuiders.createGuidersForB_Watching2();
                  guider.next();
               });
               guider.next();
            }}
         ],
         title: "$20 Reward",
         description: "The experiment will simulate this bonus by giving $20 to the Second Engineer who's team does the best in the races. This means: at the end of the experiment, we will contact the Second Engineer with the highest point total, and we will set up a meeting time to give him or her $20 cash. <br/><br/>One Second Engineer in every session will earn the $20 reward. If there is a tie, for example person A's team gets 90 points and person B's team also gets 90 points, then we will randomly pick the winner and contact him or her by email.",
         id: "tutorialB_8_22",
         next: "tutorialB_8_23"
      });

      guider.createGuider({
         attachTo: "#tut_Teammate",
         buttons: [
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Back to watching your teammate",
         description: "Let's head back to watch the end of your teammate's work days.",
         id: "tutorialB_8_23",
         next: "tutorialB_8_24",
         position: 6
      });
   };

   myGuiders.createGuidersForB_Watching2 = function () {
      guider.createGuider({
         attachTo: "#daysLeft",
         buttons: [
            {name: "Next", onclick: function () {
               $(this).addClass("guider_button_hidden");
               ca_usask_TutorialJSViewControl_RunAForXDaysOn("PersProj", 10, workDelay, "myGuiders.returnFromTutB_ADidWork_GuiderNext");
            }}
         ],
         title: "Your Teammate's Days Left",
         description: "At this point your teammate has 12 days left. <br/><br/> Sometimes you might notice that your teammate's \"Days Left\" counter will go down, but none of the car parts will be upgraded. <br/><br/>Press next to see an example of when your partner's \"Days Left\" decreases but nothing happens to the team's F1 car. Watch the car's stats.",
         id: "tutorialB_8_24",
         next: "tutorialB_8_25",
         width: 300,
         position: 6
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Your Teammate Might Have Other Priorities",
         description: "If your teammate's \"Days Left\" decrease, but there is no progress on the F1 car, it means that your teammate is working on a different project.",
         id: "tutorialB_8_25",
         next: "tutorialB_8_26",
         width: 500
      });

      guider.createGuider({
         attachTo: "#tut_TopSpeed",
         buttons: [
            {name: "Next", onclick: function () {
               $(this).addClass("guider_button_hidden");
               ca_usask_TutorialJSViewControl_RunAForXDaysOn("Part1", 2, workDelay, "myGuiders.returnFromTutB_ADidWork_GuiderNext");
            }}
         ],
         title: "You have to do more work",
         description: "If your partner spends their time on other projects, it means you will have to do more work to reach the F1 car's goals.<br/> <br/>Press next to see what would happen if your teammate spent their last two days working on the Engine. Watch the Current Top Speed",
         id: "tutorialB_8_26",
         next: "tutorialB_8_27",
         width: 300,
         position: 6
      });


      guider.createGuider({
         attachTo: "#tut_TopSpeedLabel",
         buttons: [
            {name: "Next"}
         ],
         title: "That helped you reach your goals",
         description: "The current Top Speed rose from 274 to 276 kph. <br/><br/>If your teammate chooses to upgrade the engine, you have less work to do to get the car to the goal (280 kph).",
         id: "tutorialB_8_27",
         next: "tutorialB_8_28",
         position: 6
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next", onclick: function () {
               guider.hideAll();
               ca_usask_TutorialJSViewControl_DoneWatchingA();
            }}
         ],
         title: "Your turn",
         description: "Press next and your teammate will send you the car to work on.",
         id: "tutorialB_8_28",
         next: "tutorialB_8_29"
      });

   };

   myGuiders.build_tutorialB_Working = function () {
      // disable the work button at the start.
      myGuiders.setInputStatesForTutorialB_Working();

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Now it's your turn",
         description: "Now its your turn to work on the car. Let's look through the work screen and then work on the car for the Australian Grand Prix. Hopefully we can do well and win the race!",
         id: "tutorialB_Working_1",
         next: "tutorialB_Working_2"
      });

      guider.createGuider({
         attachTo: "#tut_PartName1",
         buttons: [
            {name: "Next"}
         ],
         title: "Part Name",
         description: "This is the name of the Air Intake System you currently have. You start the game with a \"Mark I\", the lowest level of Air Intake.<br/><br/>This part affects the car's Top Speed. Your teammate's Engine affects this part of the car.",
         id: "tutorialB_Working_2",
         next: "tutorialB_Working_3",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_PartData1",
         buttons: [
            {name: "Next"}
         ],
         title: "The Level of this part",
         description: "A Mark I Air Intake gives your car 276kph in speed.",
         id: "tutorialB_Working_3",
         next: "tutorialB_Working_4",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartGoal1",
         buttons: [
            {name: "Next"}
         ],
         title: "The Goal",
         description: "And in order to reach first place you will need to upgrade the Air Intake until the car can reach 280 kph.",
         id: "tutorialB_Working_4",
         next: "tutorialB_Working_5",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_GoalReached1",
         buttons: [
            {name: "Next"}
         ],
         title: "Not yet...",
         description: "You haven't reached that goal yet, but you might be close if your teammate helped out by upgrading the Engine.",
         id: "tutorialB_Working_5",
         next: "tutorialB_Working_6",
         position: 6
      });
      guider.createGuider({
         attachTo: "#tut_NextUpgrade1",
         buttons: [
            {name: "Next"}
         ],
         title: "Next Upgrade",
         description: "A Mark II is the next level of Air Intake, and it will give you 278 kph.",
         id: "tutorialB_Working_6",
         next: "tutorialB_Working_7",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_UpgradeChance",
         buttons: [
            {name: "Next"}
         ],
         title: "The more you work, the higher chance of success",
         description: "The longer you work on a problem, the closer you get to a solution. We simulate this with a probability. In this case, if you work one day on the Mark I Air Intake, there is a 21% chance you will finish the Mark II upgrade.",
         id: "tutorialB_Working_7",
         next: "tutorialB_Working_8",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_DaysLeft",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialB_Working({workButton1: true});
               // now ask the server to notify us when the current top speed changes, that means
               // they upgraded a part.
               ca_usask_chdp_registerCallbackOnChange("part1CurData", 1, "myGuiders.tutorialB_workedPart1_1");
               guider.next();
            }}
         ],
         title: "Limited Time",
         description: "Every round you will have 26 work days to spend.",
         id: "tutorialB_Working_8",
         next: "tutorialB_Working_9",
         position: 3
      });

      myGuiders.tutorialB_workedPart1_1 = function () {
         myGuiders.setInputStatesForTutorialB_Working();
         guider.next();
      };

      guider.createGuider({
         attachTo: "#tut_WorkButton1",
         buttons: [
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Work on the Engine",
         description: "You can work on the Air Intake by pressing this button. Go ahead and work on the Intake until you upgrade to Mark II.",
         id: "tutorialB_Working_9",
         next: "tutorialB_Working_10",
         width: 250,
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartData1",
         buttons: [
            {name: "Next"}
         ],
         title: "Good work",
         description: "You were able to upgrade to a Mark II Air Intake. Each Intake upgrade will give you 2 kph more speed.",
         id: "tutorialB_Working_10",
         next: "tutorialB_Working_11",
         width: 250,
         position: 8
      });

      guider.createGuider({
         attachTo: "#tut_NextUpgrade1",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialB_Working({workButton1: true});
               ca_usask_chdp_registerCallbackOnChange("part1CurData", 1, "myGuiders.tutorialB_workedPart1_1");
               guider.next();
            }}
         ],
         title: "One more upgrade needed",
         description: "We haven't reached your team's goal yet, but the next upgrade will.",
         id: "tutorialB_Working_11",
         next: "tutorialB_Working_12",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_WorkButton1",
         buttons: [
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Finishing this part",
         description: "Let's finish work on the Air Intake.",
         id: "tutorialB_Working_12",
         next: "tutorialB_Working_13",
         width: 250,
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_GoalReached1",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialB_Working({tab3: true});
               var tabs = $('a[href^=#tab]');
               // bind the click to the ul.tabs element, so that we get called after the
               // navigation script in viewcontrol.js
               $('ul.tabs').on('click.tutorial', 'a', function () {
                  $('ul.tabs').off('.tutorial');
                  // when they click on the part3 tab:
                  myGuiders.setInputStatesForTutorialB_Working();
                  // need to create the next batch of guiders, now that the new tab is visible:
                  myGuiders.createGuidersForTab3B_Working();
                  guider.next();
               });
               guider.next();
            }}
         ],
         title: "Congratulations",
         description: "You have reached your team's goal for Top Speed.",
         id: "tutorialB_Working_13",
         next: "tutorialB_Working_14",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartSelector3",
         buttons: [
            // guider.next is handled when they click on tab3
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Two Other Parts",
         description: "Recall that you have two other parts to work on: the Wing Aerodynics and the Suspension System. They are highlighted in red because you have not reached your goals on them yet. Click on the Suspention System to move to that part's screen.",
         id: "tutorialB_Working_14",
         next: "tutorialB_Working_15",
         position: 6
      });
   };

   //-------------------------------------------------------------------------------
   // Tab3
   //-------------------------------------------------------------------------------
   myGuiders.createGuidersForTab3B_Working = function () {
      guider.createGuider({
         attachTo: "#tut_PartName3",
         buttons: [
            {name: "Next"}
         ],
         title: "Current Suspension System",
         description: "Like the Air Intake, you start with the lowest level of Suspension System: the \"Mark I\". <br/><br/>This part affects the car's Lateral Top Speed (speed through a turn). Your teammate's Wheel Assembly affects this part of the car.",
         id: "tutorialB_Working_15",
         next: "tutorialB_Working_16",
         position: 9,
         width: 250
      });

      guider.createGuider({
         attachTo: "#tut_PartGoal3",
         buttons: [
            {name: "Next"}
         ],
         title: "Your Goal",
         description: "And like the Air Intake, you have a goal to complete in order to reach 1st place in Australia.",
         id: "tutorialB_Working_16",
         next: "tutorialB_Working_17",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_NextUpgrade3",
         buttons: [
            {name: "Next", onclick: function () {
               // For the working on Part 3 phase.
               myGuiders.setInputStatesForTutorialB_Working({workButton3: true});
               // now ask the server to notify us when the current top speed changes, that means
               // they upgraded a part.
               ca_usask_chdp_registerCallbackOnChange("part3CurData", 3, "myGuiders.tutorialB_workedPart3_1");
               guider.next();
            }}
         ],
         title: "Next Upgrade",
         description: "Every Suspension upgrade increases your Lateral Top Speed by 2 kph. So you will need to work on 3 upgrades to reach your goal of 159 kph.<br/><br/>Looks like your partner could have helped a bit more by upgrading the Wheel Assembly.",
         id: "tutorialB_Working_17",
         next: "tutorialB_Working_18",
         position: 6
      });

      myGuiders.tutorialB_workedPart3_1 = function () {
         myGuiders.setInputStatesForTutorialB_Working();
         guider.next();
      };

      guider.createGuider({
         attachTo: "#tut_WorkButton3",
         buttons: [
            // guider.next is handled when they finish the goal.
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Finish this part",
         description: "Let's work on this part and upgrade it to a \"Mark IV\". ",
         id: "tutorialB_Working_18",
         next: "tutorialB_Working_19",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_GoalReached3",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialB_Working({tab2: true});
               var tabs = $('a[href^=#tab]');
               // bind the click to the ul.tabs element, so that we get called after the
               // navigation script in viewcontrol.js
               $('ul.tabs').on('click.tutorial', 'a', function () {
                  $('ul.tabs').off('.tutorial');
                  // when they click on the tab:
                  myGuiders.setInputStatesForTutorialB_Working();
                  // need to create the next batch of guiders, now that the new tab is visible:
                  myGuiders.createGuidersForTab2B_Working();
                  guider.next();
               });
               guider.next();
            }}
         ],
         title: "Congratulations",
         description: "You're getting the hang of things, good work.",
         id: "tutorialB_Working_19",
         next: "tutorialB_Working_20",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartSelector2",
         buttons: [
            // guider.next is handled when they click on tab2
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Final part",
         description: "The red tooltip indicates there is one more part to work on this round. Let's move to the Wing Aerodynamics screen now.",
         id: "tutorialB_Working_20",
         next: "tutorialB_Working_21",
         position: 6
      });
   };

   //-------------------------------------------------------------------------------
   // Tab2
   //-------------------------------------------------------------------------------
   myGuiders.createGuidersForTab2B_Working = function () {
      guider.createGuider({
         attachTo: "#tut_PartName2",
         buttons: [
            {name: "Next"}
         ],
         title: "Current Wing Aerodynamics",
         description: "You are also starting with a \"Mark I\" Wing system.",
         id: "tutorialB_Working_21",
         next: "tutorialB_Working_22",
         position: 9,
         width: 250
      });

      guider.createGuider({
         attachTo: "#tut_PartData2",
         buttons: [
            {name: "Next"}
         ],
         title: "Acceleration",
         description: "The Wing Aerodynamics affects the time it takes to accelerate from 0 - 100 kph. Your teammate's Powertrain affects this part of the car.",
         id: "tutorialB_Working_22",
         next: "tutorialB_Working_23",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_NextUpgrade2",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialB_Working({workButton2: true});
               ca_usask_chdp_registerCallbackOnChange("goal2Reached", 1, "myGuiders.tutorialB_workedPart2_1");
               guider.next();
            }}
         ],
         title: "Lower is better",
         description: "The quicker the car can reach 100 kph, the better chance it has of winning the next race.",
         id: "tutorialB_Working_23",
         next: "tutorialB_Working_24",
         position: 6
      });

      myGuiders.tutorialB_workedPart2_1 = function () {
         myGuiders.setInputStatesForTutorialB_Working();
         guider.hideAll();
      };

      guider.createGuider({
         attachTo: "#tut_WorkButton2",
         buttons: [
            // guider.next is handled when they finish the goal, in myGuiders.tutorialB_workedPart2_1
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Finish this part",
         description: "Let's work on this part. Hopefully you will have enough days left to upgrade it to a \"Mark IV\". Only a Mark IV will meet your team's goals of 4.2 sec.",
         id: "tutorialB_Working_24",
         next: "tutorialB_Working_25",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_GoalReached2",
         buttons: [
            {name: "Next"}
         ],
         title: "Congratulations",
         description: "You have finished all of your goals, good work.",
         id: "tutorialB_Working_25",
         next: "tutorialB_Working_26",
         position: 6
      });
   };

   //-------------------------------------------------------------------------------
   // RaceView
   //-------------------------------------------------------------------------------
   myGuiders.build_tutorialBRaceView = function () {
      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Now we watch the race",
         description: "In this screen we see the result of your team's work on the F1 car. Your performance in the race is a simulation based on a number of factors (eg, weather and track conditions). But mostly it depends on how close you were to meeting your team goals.",
         id: "tutorialBRaceView_1",
         next: "tutorialBRaceView_2"
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Watch the race", onclick: guider.next}
         ],
         title: "Your $20 reward",
         description: "Your reward depends on how well you do in these races. <br/><br/>At the end of the experiment we will contact the Second Engineer with the highest point total, and we will set up a meeting time to give him or her $20 cash.",
         id: "tutorialBRaceView_2",
         next: "tutorialBRaceView_3"
      });

      guider.createGuider({
         attachTo: "#team0",
         buttons: [
            {name: "Close", onclick: function () {
               chdp_WatchingRaceTutorialView_enableStartButton();
               guider.hideAll();
            }}
         ],
         title: "Your team",
         description: "The ESB-Mercedes team is highlighted in red. Press the start button below when you are ready, and adjust the game speed with the slider if you wish.",
         id: "tutorialBRaceView_3",
         position: 9
      });
   };

   //-------------------------------------------------------------------------------
   // RaceResults
   //-------------------------------------------------------------------------------
   myGuiders.build_tutorialBRaceResults = function () {
      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Race Results Screen",
         description: "This screen shows us the results of the race.",
         id: "tutorialBRaceResults_1",
         next: "tutorialBRaceResults_2"
      });

      guider.createGuider({
         attachTo: "#overallResults",
         position: 6,
         buttons: [
            {name: "Next", onclick: function () {
               chdp_RaceResultsTutorialView_enableContinueButton();
               guider.hideAll();
            }}
         ],
         title: "Standings",
         description: "The overall standings are updated after each race. These are the same results you can see while you are waiting for your Teammate to finish their parts of the car. <br/><br/>The Second Engineer with the highest total points at the end of 8 races will receive the $20 prize. <br/><br/>You may click on the Continue button to view the damage screen.",
         id: "tutorialBRaceResults_2"
      });
   };

   //-------------------------------------------------------------------------------
   // Damage Report
   //-------------------------------------------------------------------------------
   myGuiders.build_tutorialBDamageReport = function () {
      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Damage Report Screen",
         description: "This screen shows us the damage your car received during the race. This simulates the natural wear and tear, and sometimes worse, that affects the components of a race car.",
         id: "tutorialBDamageReport_1",
         next: "tutorialBDamageReport_2"
      });

      guider.createGuider({
         attachTo: "#dPartInfo1",
         position: 6,
         buttons: [
            {name: "Next"}
         ],
         title: "Part Damage",
         description: "Green indicates only minor wear and tear. In this case the top speed has reduced from 280 to 276 as a result of the Engine damage.",
         id: "tutorialBDamageReport_2",
         next: "tutorialBDamageReport_3"
      });

      guider.createGuider({
         attachTo: "#dPartInfo2",
         position: 6,
         buttons: [
            {name: "Next", onclick: function () {
               chdp_DamageReportTutorialView_enableContinueButton();
               guider.hideAll();
            }}
         ],
         title: "The Effect on Next Round",
         description: "Damage has increased acceleration time from 4.2 to 4.4 seconds. This means next round the Powertrain will start with a stat of 4.4 seconds.<br/><br/>Let's continue on to the summary screen.",
         id: "tutorialBDamageReport_3"
      });
   };

}());
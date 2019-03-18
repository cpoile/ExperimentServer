/*global guider,myGuiders, ca_usask_chdp_setWorkProj2ButtonToXDays, ca_usask_chdp_registerCallbackOnChange, ca_usask_chdp_fastForwardThroughBsWork, chdp_WatchingRaceTutorialView_enableStartButton, chdp_RaceResultsTutorialView_enableContinueButton, chdp_DamageReportTutorialView_enableContinueButton*/

/**
 * This will be loaded after the connector script, and after the myGuiders object has been created,
 * but the connector will not call these building functions until it has been called at the end of the page's
 * onAttach method. By then these functions will be available.
 */

(function () {
   "use strict";
   // add methods to the myGuiders object.
   /**
    * PUBLIC FUNCTIONS:
    *
    * these are the guiders. There will be alot of them.
    * Self invoked function to create them when object is created.
    * Need to wait until the DOM is available before we can create them.
    */
      //-------------------------------------------------------------------------------
      // Itro and Tab1
      //-------------------------------------------------------------------------------
   myGuiders.build_tutorialA = function () {
      /**
       * Here we will create each guider.
       */
      guider.createGuider({
         buttons: [
            {name: "Next"}
         ],
         description: "Thank you for taking the time to participate in this experiment. It is designed to be interesting, so we hope you enjoy it.<br/><br/> This tutorial will guide you through the game and help you understand what you are doing. If at any time you are not sure what to do, just raise your hand and the experimenter will help you. <br/><br/> After finishing the tutorial you will be ready to play the game and interact with your Teammate. If you play the game well enough, you may earn $20." + "<br/><br/>Let's get started.",
         id: "tutorialA_1",
         next: "tutorialA_2",
         overlay: true,
         title: "Welcome to the experiment!"
      });

      guider.createGuider({
         buttons: [
            {name: "Next"}
         ],
         description: "In this experiment you will be playing the the role of an engineer working for Mercedes-Benz.",
         id: "tutorialA_2",
         next: "tutorialA_3",
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
         id: "tutorialA_3",
         next: "tutorialA_4",
         title: "Your team's F1 Project"
      });

      guider.createGuider({
         attachTo: "#nextRaceLabel",
         buttons: [
            {name: "Next"}
         ],
         description: "There are 8 races in total. This section shows the next race your team will compete in: <em>Race #1, the Australian Grand Prix.</em>",
         id: "tutorialA_4",
         next: "tutorialA_5",
         position: 9,
         title: "How many races?"
      });

      guider.createGuider({
         buttons: [
            {name: "Next"}
         ],
         width: 1300,
         description: "This is the 2013 Mercedes Engineering team. The team strips and rebuilds the entire car in between races. Every part of the machine is damaged by the race, and it is the Engineer's job to repair the parts for the next race. <img src='" + myGuiders.getUrlFor("petronas_f1_team.jpg") + "'/>",
         id: "tutorialA_5",
         next: "tutorialA_6",
         overlay: true,
         title: "Working with your teammate to build the F1 car"
      });

      guider.createGuider({
         buttons: [
            {name: "Continue", onclick: _.partial(myGuiders.anim_Part1, "A")}
         ],
         width: 1150,
         description: "<div id='tutA6_textCont'><div id='tutA6_text1'>In real life the team has dozens of engineers, but in this game the team is smaller. The team is you, and your partner.<div>You will take the role of \"First Engineer\" in charge of three parts of the car: the Powertrain, the Engine, and the Wheel Assembly.</div></div></div> <div id='tutA5_container'><img class='tutImg' width='80' height='80' style='top: 20px; left: 147px; display: block;' src='" + myGuiders.getUrlFor("engineer.gif") + "' /><img class='tutImg'  width='80' height='80' style='top: 20px; left: 710px; display: block;' src='" + myGuiders.getUrlFor("engineer.gif") + "' /><div class='tutText' style='top: -17px; left:135px; display: block; text-align: center;'>You<br/>First Engineer</div><div class='tutText' style='top: -17px; left:690px;  display: block; text-align: center;'>Your Partner<br/>Second Engineer</div></div>",
         id: "tutorialA_6",
         next: "tutorialA_7",
         overlay: true,
         title: "Working with your teammate to build the F1 car"
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA();
               guider.next();
            }}
         ],
         title: "How to Play the Game",
         description: "Now we will take you through a round of the game and learn how to play while we go.",
         id: "tutorialA_7",
         next: "tutorialA_8"
      });

      guider.createGuider({
         attachTo: "#carpartdetails",
         buttons: [
            {name: "Next"}
         ],
         title: "Part Details",
         description: "Here we show you details about the part you are working on.",
         id: "tutorialA_8",
         next: "tutorialA_9",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartName1",
         buttons: [
            {name: "Next"}
         ],
         title: "Part Name",
         description: "This is the name of the engine you currently have. You start the game with a \"Mark I\" engine, the lowest level of engine.",
         id: "tutorialA_9",
         next: "tutorialA_10",
         width: 250,
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_PartData1",
         buttons: [
            {name: "Next"}
         ],
         title: "The Level of this part",
         description: "A Mark I engine gives your car 270kph in speed.",
         id: "tutorialA_10",
         next: "tutorialA_11",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartGoal1",
         buttons: [
            {name: "Next"}
         ],
         title: "The Goal",
         description: "And your managers want you to upgrade the engine until it can reach 274 kph.",
         id: "tutorialA_11",
         next: "tutorialA_12",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_GoalReached1",
         buttons: [
            {name: "Next"}
         ],
         title: "Not yet...",
         description: "You haven't reached your manager's goal yet, of course.",
         id: "tutorialA_12",
         next: "tutorialA_13",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_NextUpgrade1",
         buttons: [
            {name: "Next"}
         ],
         title: "Next Upgrade",
         description: "A Mark II engine is the next level of engine, and it will give you 272 kph.",
         id: "tutorialA_13",
         next: "tutorialA_14",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_UpgradeChance",
         buttons: [
            {name: "Next"}
         ],
         title: "The more you work, the higher chance of success",
         description: "The longer you work on a problem, the closer you get to a solution. We simulate this with a probability. In this case, if you work one day on the Mark I engine, there is a 33% chance you will finish the Mark II upgrade.",
         id: "tutorialA_14",
         next: "tutorialA_15",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_DaysLeft",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA({workButton1: true});
               // now ask the server to notify us when the current top speed changes, that means
               // they upgraded a part.
               ca_usask_chdp_registerCallbackOnChange("part1CurData", 1, "myGuiders.part1DataChanged1");
               guider.next();
            }}
         ],
         title: "Limited Time",
         description: "Every round you will have 26 work days to spend.",
         id: "tutorialA_15",
         next: "tutorialA_16",
         position: 3
      });

      guider.createGuider({
         attachTo: "#tut_WorkButton1",
         buttons: [
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Work on the Engine",
         description: "You can work on the engine by pressing this button. Go ahead and work on the engine until you upgrade to Mark II.",
         id: "tutorialA_16",
         next: "tutorialA_17",
         width: 250,
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartData1",
         buttons: [
            {name: "Next"}
         ],
         title: "Good work",
         description: "You were able to upgrade to a Mark II Engine. Each engine upgrade will give you 2 kph more speed.",
         id: "tutorialA_17",
         next: "tutorialA_18",
         width: 250,
         position: 8
      });

      guider.createGuider({
         attachTo: "#tut_NextUpgrade1",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA({workButton1: true});
               // now ask the server to notify us when the current top speed changes, that means
               // they upgraded a part.
               ca_usask_chdp_registerCallbackOnChange("part1CurData", 1, "myGuiders.part1DataChanged2");
               guider.next();
            }}
         ],
         title: "One more upgrade needed",
         description: "We haven't reached your goal yet, but the next upgrade will.",
         id: "tutorialA_18",
         next: "tutorialA_19",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_WorkButton1",
         buttons: [
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Finishing this part",
         description: "Let's finish work on the engine.",
         id: "tutorialA_19",
         next: "tutorialA_20",
         width: 250,
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_GoalReached1",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA({tab3: true});
               var tabs = $('a[href^=#tab]');
               // bind the click to the ul.tabs element, so that we get called after the
               // navigation script in viewcontrol.js
               $('ul.tabs').on('click.tutorial', 'a', function () {
                  $('ul.tabs').off('.tutorial');
                  // when they click on the part3 tab:
                  myGuiders.setInputStatesForTutorialA();
                  // need to create the next batch of guiders, now that the new tab is visible:
                  myGuiders.createGuidersForTab3A();
                  if (_(guider._guiders).size() !== 27) {
                     console.error("createGuidersForTab3 had only " + _(guider._guiders).size() + " guiders created.");
                  }
                  guider.next();
               });
               guider.next();
            }}
         ],
         title: "Congratulations",
         description: "You have reached your personal goal for the Engine part.",
         id: "tutorialA_20",
         next: "tutorialA_21",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartSelector3",
         buttons: [
            // guider.next is handled when they click on tab3
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Two Other Parts",
         description: "Recall that you have two other parts to work on: the Powertrain and the Wheel Assembly. They are highlighted in red because you have not reached your goals on them yet. Click on the Wheel Assembly to move to that part's screen.",
         id: "tutorialA_21",
         next: "tutorialA_22",
         position: 6
      });

      myGuiders.setInputStatesForTutorialA();
   };
   //-------------------------------------------------------------------------------
   // Tab3
   //-------------------------------------------------------------------------------
   myGuiders.createGuidersForTab3A = function () {
      guider.createGuider({
         attachTo: "#tut_PartName3",
         buttons: [
            {name: "Next"}
         ],
         title: "Current Wheel Assembly",
         description: "Like the engine, you start with the lowest level of Wheel Assembly: the \"Mark I\".",
         id: "tutorialA_22",
         next: "tutorialA_23",
         position: 9,
         width: 250
      });

      guider.createGuider({
         attachTo: "#tut_PartGoal3",
         buttons: [
            {name: "Next"}
         ],
         title: "Your Goal",
         description: "And like the Engine, your managers have given you a goal to complete.",
         id: "tutorialA_23",
         next: "tutorialA_24",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_NextUpgrade3",
         buttons: [
            {name: "Next", onclick: function () {
               // For the working on Part 3 phase.
               myGuiders.setInputStatesForTutorialA({workButton3: true});
               // now ask the server to notify us when the current top speed changes, that means
               // they upgraded a part.
               ca_usask_chdp_registerCallbackOnChange("part3CurData", 2, "myGuiders.part3DataChanged1");
               guider.next();
            }}
         ],
         title: "Next Upgrade",
         description: "Every Wheel Assembly upgrade increases your Lateral Top Speed by 2 kph. So you will need to work on 2 upgrades to reach your goal of 159 kph.",
         id: "tutorialA_24",
         next: "tutorialA_25",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_WorkButton3",
         buttons: [
            // guider.next is handled when they finish the goal.
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Finish this part",
         description: "Let's work on this part and upgrade it to a \"Mark III\". ",
         id: "tutorialA_25",
         next: "tutorialA_26",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_GoalReached3",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA({tab2: true});
               var tabs = $('a[href^=#tab]');
               // bind the click to the ul.tabs element, so that we get called after the
               // navigation script in viewcontrol.js
               $('ul.tabs').on('click.tutorial', 'a', function () {
                  $('ul.tabs').off('.tutorial');
                  // when they click on the tab:
                  myGuiders.setInputStatesForTutorialA();
                  // need to create the next batch of guiders, now that the new tab is visible:
                  myGuiders.createGuidersForTab2A();
                  if (_(guider._guiders).size() !== 33) {
                     console.error("createGuidersForTab2 had only " + _(guider._guiders).size() + " guiders created.");
                  }
                  guider.next();
               });
               guider.next();
            }}
         ],
         title: "Congratulations",
         description: "You're getting the hang of things, good work.",
         id: "tutorialA_26",
         next: "tutorialA_27",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PartSelector2",
         buttons: [
            // guider.next is handled when they click on tab2
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Final part",
         description: "The red tooltip indicates there is one more part to work on this round. Let's move to the Powertrain's screen now.",
         id: "tutorialA_27",
         next: "tutorialA_28",
         position: 6
      });
   };
   //-------------------------------------------------------------------------------
   // Tab2
   //-------------------------------------------------------------------------------
   myGuiders.createGuidersForTab2A = function () {
      guider.createGuider({
         attachTo: "#tut_PartName2",
         buttons: [
            {name: "Next"}
         ],
         title: "Current Powertrain",
         description: "You're also starting with a \"Mark I\" Powertrain.",
         id: "tutorialA_28",
         next: "tutorialA_29",
         position: 9,
         width: 250
      });

      guider.createGuider({
         attachTo: "#tut_PartData2",
         buttons: [
            {name: "Next"}
         ],
         title: "Acceleration",
         description: "The powertrain affects the time it takes to accelerate from 0 - 100 kph.",
         id: "tutorialA_29",
         next: "tutorialA_30",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_NextUpgrade2",
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA({workButton2: true});
               // now ask the server to notify us when the data changes, that means
               // they upgraded a part.
               ca_usask_chdp_registerCallbackOnChange("goal2Reached", 1, "myGuiders.finishedPersGoals");
               guider.next();
            }}
         ],
         title: "Lower is better",
         description: "And the quicker the car can reach 100 kph, the better chance it has of winning the next race.",
         id: "tutorialA_30",
         next: "tutorialA_31",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_WorkButton2",
         buttons: [
            // guider.next is handled when they finish the goal, in myGuiders.finishedPersGoals
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Finish this part",
         description: "Let's work on this part and upgrade it to a \"Mark II\". ",
         id: "tutorialA_31",
         next: "tutorialA_32",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_GoalReached2",
         buttons: [
            {name: "Next", onclick: function () {
               // set the click on tut_PersProj as the trigger for the next guider.
               myGuiders.setInputStatesForTutorialA({persProj: true});
               $('ul.globalNav').on('click.tutorial', 'a', function () {
                  $('ul.globalNav').off('.tutorial');
                  // disable the link back to the F1 proj for now.
                  myGuiders.setInputStatesForTutorialA();
                  // start guider system back up.
                  myGuiders.createGuidersForPersProjA();
                  if (_(guider._guiders).size() !== 45) {
                     console.error("createGuidersForPersProj had only " + _(guider._guiders).size() + " guiders created.");
                  }
                  guider.next();
               });
               guider.next();
            }}
         ],
         title: "Congratulations",
         description: "You have finished all of your goals, good work.",
         id: "tutorialA_32",
         next: "tutorialA_33",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_PersProj",
         buttons: [
            // guider.next is handled when they click the link, in tutorialA_32
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "You have new options",
         description: "Now that you have finished your goals for the F1 car, you have a new project you can work on. Click on \"Concept Car Project\" to view the project's screen.",
         id: "tutorialA_33",
         next: "tutorialA_34",
         position: 6
      });
   };

   //-------------------------------------------------------------------------------
   // PersProj
   //-------------------------------------------------------------------------------
   myGuiders.createGuidersForPersProjA = function () {
      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Your Personal Project",
         description: "Many engineers work on Formula 1 teams AND on regular car design teams. They have to choose which project to spend their time on. Your work on the F1 team's car is finished, so now you can work on your personal project: the 2014 Concept Car.",
         id: "tutorialA_34",
         // deleted 35
         next: "tutorialA_36"
      });

      guider.createGuider({
         attachTo: "#proj2GraphContainer",
         buttons: [
            {name: "Next"}
         ],
         title: "Your Personal Project",
         description: "The days you spend working on your personal project will be recorded on this graph, broken down by each race.",
         id: "tutorialA_36",
         next: "tutorialA_37",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_DaysLeft2",
         buttons: [
            {name: "Next"}
         ],
         title: "Your Choice",
         description: "You have 12 days left in this round. This is your choice: How will you spend your 12 remaining days?",
         id: "tutorialA_37",
         next: "tutorialA_38",
         position: 3
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Your Choice is Important",
         description: "The reason this choice is important is this: Your managers value your work on the 2014 Concept Car. Your managers will give a bonus to the First Engineer who spends the most time working on the Concept Car project.",
         id: "tutorialA_38",
         // deleted step 39
         next: "tutorialA_40"
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA({workButtonProj2: true});
               ca_usask_chdp_setWorkProj2ButtonToXDays(4);
               ca_usask_chdp_registerCallbackOnChange("daysLeft", 1, "myGuiders.workedOnProj2_1");
               guider.next();
            }}
         ],
         title: "$20 Reward",
         description: "The experiment will simulate this bonus by giving $20 to the First Engineer who spends the most time working on the concept car. This means: at the end of the experiment, we will contact the First Engineer who spends the most number of days on the 2014 Concept Car project, and we will set up a meeting time to give him or her $20 cash. <br/><br/>One First Engineer in every session will earn the $20 prize. If there is a tie, for example person A works 30 days on the Concept Car and person B also work 30 days on the Concept Car, then we will randomly pick the winner and contact him or her by email.",
         id: "tutorialA_40",
         next: "tutorialA_41"
      });

      guider.createGuider({
         attachTo: "#tut_WorkButtonProj2",
         buttons: [
            // guider.next is handled when they finish the goal, in myGuiders.workedOnProj2_1
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Work on the concept car",
         description: "Let's work 4 days on the concept car and see how that changes the graph. Drag the slider over to 4 days, and press the work button.",
         id: "tutorialA_41",
         next: "tutorialA_42",
         position: 6
      });

      guider.createGuider({
         attachTo: "#proj2GraphContainer",
         buttons: [
            {name: "Next", onclick: function () {
               function removeHandlers() {
                  $("#chatinput").off(".tutorial");
                  $("#").off(".tutorial");
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
               $("#tut_ChatButton").on("click.tutorial", advanceFn);
               guider.next();
            }}
         ],
         title: "Progress",
         description: "You have now worked 4 days on the concept car. If the other First Engineers in the experiment work less than 4 days, you will win the $20. But chances are, the other First Engineers will work more than 4 days.",
         id: "tutorialA_42",
         next: "tutorialA_43",
         position: 6
      });

      guider.createGuider({
         attachTo: "#chatinput",
         buttons: [
            // guider.next is handled when they finish the goal, in myGuiders.typedIntoChatbox
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Your Teammate",
         description: "This is a good time to mention the chat function of the program. When you play the game you will be able to communicate with your teammate. You may chat about anything with your teammate, but we ask that you not reveal your identity. If you reveal your identity, you will no longer be anonymous, and the experiment assumes that no-one knows exactly who they are playing with. Thank you for your cooperation!<br/><br/> Type \"Hello\" into the chat box to test it out.",
         id: "tutorialA_43",
         next: "tutorialA_44",
         position: 9
      });

      guider.createGuider({
         attachTo: "#tut_MessageList",
         buttons: [
            {name: "Next"}
         ],
         title: "Remember your teammate",
         description: "Be sure to pay attention to the message box during the game.<br/><br/> Don't forget -- after you are finished this round, you will send the F1 car to your teammate. They are depending on you, because they will take the F1 car you build and then add their own parts.",
         id: "tutorialA_44",
         next: "tutorialA_45",
         position: 9
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA({workButtonProj2: true});
               ca_usask_chdp_setWorkProj2ButtonToXDays(7);
               ca_usask_chdp_registerCallbackOnChange("daysLeft", 1, "myGuiders.workedOnProj2_2");
               guider.next();
            }}
         ],
         title: "Your Choice",
         description: "We have finished describing the game.<br/><br/> In summary: Your managers will reward you only for the amount of work you do on the Concept Car. They do not care how much work you do on the F1 car.<br/><br/> So now it's up to you. How many days will you spend on the Concept Car? How many days will you spend upgrading the F1 car?",
         id: "tutorialA_45",
         next: "tutorialA_46"
      });

      guider.createGuider({
         attachTo: "#tut_WorkButtonProj2",
         buttons: [
            // guider.next is handled when they finish the goal, in myGuiders.workedOnProj2_2
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Work on the concept car",
         description: "Let's spend 7 more days on the concept car, and then the final day on the F1 car, as an example of what you might choose to do.",
         id: "tutorialA_46",
         next: "tutorialA_47",
         position: 6
      });

      guider.createGuider({
         attachTo: "#tut_TeamProj",
         buttons: [
            // guider.next is handled when they click on the tab button, in myGuiders.workedOnProj2_2
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Move back to the F1 Team Project",
         description: "Let's move back to the Team Project.",
         id: "tutorialA_47",
         next: "tutorialA_48",
         position: 6
      });
   };

   //-------------------------------------------------------------------------------
   // TeamProj last day of work
   //-------------------------------------------------------------------------------
   myGuiders.createGuidersForTeamProjLastDayOfWorkA = function () {
      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next", onclick: function () {
               myGuiders.setInputStatesForTutorialA({tab1: true, tab2: true, tab3: true, workButton1: true, workButton2: true, workButton3: true});

               ca_usask_chdp_registerCallbackOnChange("daysLeft", 1, "myGuiders.finishedAllDays");
               guider.next();
            }}
         ],
         title: "One more day",
         description: "You now have one more day to work. You might want to try to upgrade a part. This would help your teammate because you would be sending him/her a better car to build on.",
         id: "tutorialA_48",
         next: "tutorialA_49"
      });

      guider.createGuider({
         attachTo: "#tut_WorkButton1",
         buttons: [
            // hideAll handled in myGuiders.finishedAllDays
            {name: "Next", classString: "guider_button_hidden"}
         ],
         title: "Choose a part to work on",
         description: "You can pick any part to work on. It doesn't matter which one; any extra work on the F1 car will help your partner by reducing the amout of work he/she needs to do to reach their goal.<br/><br/>Of course, this is one day that you won't spend on your personal project Concept Car, and that will hurt your chances of earning the $20 prize.",
         id: "tutorialA_49",
         next: "tutorialA_50",
         position: 6
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Finish Teammate's Round", onclick: function () {
               ca_usask_chdp_fastForwardThroughBsWork();
               guider.hideAll();
            }}
         ],
         title: "Now you wait for your teammate",
         description: "Normally we would be waiting for your teammate to finish his/her round. You can communicate with them while you are waiting.<br/><br/> This would normally take a few minutes. For this tutorial we can fast forward through this part and go straight to the race..",
         id: "tutorialA_50"
         //next: "tutorialA_51"
      });
   };

   //-------------------------------------------------------------------------------
   // RaceView
   //-------------------------------------------------------------------------------
   myGuiders.build_tutorialARaceView = function () {
      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Now we watch the race",
         description: "In this screen we see the result of your team's work on the F1 car. Your performance in the race is a simulation based on a number of factors (eg, weather and track conditions). But mostly it depends on how much your team was able to upgrade your car.",
         id: "tutorialARaceView_1",
         next: "tutorialARaceView_2"
      });

      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Watch the race", onclick: guider.next}
         ],
         title: "The race doesn't affect your reward",
         description: "Your reward does not depend on how well your team does in these races. Instead, your managers will give the bonus to the First Engineer who does the most work on the Concept Car project. Your managers do not care how well you do in the races.<br/><br>But, that doesn't mean we can't enjoy the race. :)",
         id: "tutorialARaceView_2",
         next: "tutorialARaceView_3"
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
         id: "tutorialARaceView_3",
         position: 9
      });
   };

   //-------------------------------------------------------------------------------
   // RaceResults
   //-------------------------------------------------------------------------------
   myGuiders.build_tutorialARaceResults = function () {
      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Race Results Screen",
         description: "This screen shows us the results of the race.",
         id: "tutorialARaceResults_1",
         next: "tutorialARaceResults_2"
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
         description: "The overall standings are updated after each race. These results may matter to your teammate. But the race results do not matter to you or your managers.<br/><br/> Your only goal is to work on the Concept Car. The more you work on the Concept Car project, the more likely you are to earn the $20 prize. <br/><br/>You may click on the Continue button to view the damage screen.",
         id: "tutorialARaceResults_2"
      });
   };

   //-------------------------------------------------------------------------------
   // Damage Report
   //-------------------------------------------------------------------------------
   myGuiders.build_tutorialADamageReport = function () {
      guider.createGuider({
         overlay: true,
         buttons: [
            {name: "Next"}
         ],
         title: "Damage Report Screen",
         description: "This screen shows us the damage your car received during the race. This simulates the natural wear and tear, and sometimes worse, that affects the components of a race car.",
         id: "tutorialADamageReport_1",
         next: "tutorialADamageReport_2"
      });

      guider.createGuider({
         attachTo: "#dPartInfo1",
         position: 6,
         buttons: [
            {name: "Next"}
         ],
         title: "Part Damage",
         description: "Green indicates only minor wear and tear. In this case the top speed has reduced from 280 to 276 as a result of the Engine damage.",
         id: "tutorialADamageReport_2",
         next: "tutorialADamageReport_3"
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
         id: "tutorialADamageReport_3"
      });
   };
}());
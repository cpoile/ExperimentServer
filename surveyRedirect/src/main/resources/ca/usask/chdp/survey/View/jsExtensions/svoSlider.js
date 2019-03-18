/*global chdp_Survey1View_returnData, chdp_Survey1View_weAreFinished, chdp_Survey1View_restart*/
var svoSlider = svoSlider || {};


(function () {
   "use strict";

   var dragMe = {
      obj: null,
      isSlider: false,
      diffX: 0,
      diffY: 0
   };
   /**These are set in the initial setup of the game.
    *  @type {number} */
   var valueYouGet, valueOtherGets, sliderOffsetX, spacing, top_minimum, top_maximum, top_descending, bottom_minimum, bottom_maximum, bottom_descending;
   /** @type {Object} */
   var elem_aboveSliderVal_youGet, elem_infoyougetval, elem_youget_bar, elem_belowSliderVal_otherGets, elem_infoothergetsval, elem_othergets_bar;
   /**
    * we need to initialize these in the initialize function (after pageload)
    * @type {Object}
    */
   var objMinYouGet, objMaxYouGet, objMinOtherGets, objMaxOtherGets;

   /**
    * adjust the slider setup to show the correct values
    * @param {number} tminimum
    * @param {number} tmaximum
    * @param {number} tdescending
    * @param {number} bminimum
    * @param {number} bmaximum
    * @param {number} bdescending
    */
   function adjustSlider(tminimum, tmaximum, tdescending, bminimum, bmaximum, bdescending) {
      top_minimum = tminimum;
      top_maximum = tmaximum;
      top_descending = tdescending;

      bottom_minimum = bminimum;
      bottom_maximum = bmaximum;
      bottom_descending = bdescending;

      if (top_descending == 1) {
         document.getElementById('min_youget').innerHTML = top_maximum.toString();
         document.getElementById('max_youget').innerHTML = top_minimum.toString();
      } else {
         document.getElementById('min_youget').innerHTML = top_minimum.toString();
         document.getElementById('max_youget').innerHTML = top_maximum.toString();
      }

      if (bottom_descending == 1) {
         document.getElementById('min_othergets').innerHTML = bottom_maximum.toString();
         document.getElementById('max_othergets').innerHTML = bottom_minimum.toString();
      } else {
         document.getElementById('min_othergets').innerHTML = bottom_minimum.toString();
         document.getElementById('max_othergets').innerHTML = bottom_maximum.toString();
      }
   }

   /**
    * updates the scale and determines the values
    * @param {number} dX
    * @param {number} offsetX
    * @param {number} spacing
    */
   function updateScale(dX, offsetX, spacing) {
      if (top_descending == 0) {
         valueYouGet = top_minimum + (dX - offsetX) / (500 / (top_maximum - top_minimum));
      } else {
         valueYouGet = top_maximum - (dX - offsetX) / (500 / (top_maximum - top_minimum));
      }
      updateElem_youGet(dX + spacing, valueYouGet);

      if (bottom_descending == 0) {
         valueOtherGets = bottom_minimum + (dX - offsetX) / (500 / (bottom_maximum - bottom_minimum));
      } else {
         valueOtherGets = bottom_maximum - (dX - offsetX) / (500 / (bottom_maximum - bottom_minimum));
      }
      updateElem_otherGets(dX + spacing, valueOtherGets);

      if (dX < 420) {
         objMinYouGet.style.color = '#E0E0E0';
         objMinOtherGets.style.color = '#E0E0E0';
      } else {
         objMinYouGet.style.color = '#808080';
         objMinOtherGets.style.color = '#808080';
      }
      if (dX > 880) {
         objMaxYouGet.style.color = '#E0E0E0';
         objMaxOtherGets.style.color = '#E0E0E0';
      } else {
         objMaxYouGet.style.color = '#808080';
         objMaxOtherGets.style.color = '#808080';
      }
   }

   /**
    * updates the value and position above the slider
    * @param pos
    * @param value
    */
   function updateElem_youGet(pos, value) {
      var sp = (value < 10) ? 4 : 2;

      elem_aboveSliderVal_youGet.innerHTML = Math.round(value);
      if (pos > 0) {
         elem_aboveSliderVal_youGet.style.left = pos + sp + "px";
      }
      elem_infoyougetval.innerHTML = Math.round(value);
      elem_youget_bar.style.width = Math.round(value) + "px";
   }

   /**
    * updates the number and if wanted position underneath the slider
    * @param {number} pos
    * @param {number} value
    */
   function updateElem_otherGets(pos, value) {
      var sp = (value < 10) ? 4 : 2;

      elem_belowSliderVal_otherGets.innerHTML = Math.round(value);
      if (pos > 0) {
         elem_belowSliderVal_otherGets.style.left = pos + sp + "px";
      }
      elem_infoothergetsval.innerHTML = Math.round(value);
      elem_othergets_bar.style.width = Math.round(value) + "px";
   }

   /* if we click an item, this is what we drag */
   function clickMe(item, isSlider) {
      EventUtil.addHandler(item, "mousedown", function (event) {
         event = EventUtil.getEvent(event);
         // var target = EventUtil.getTarget(event);
         EventUtil.preventDefault(event);
         EventUtil.stopPropagation(event);

         dragMe.isSlider = isSlider;
         dragMe.obj = item;
         //dragMe.obj = target;

         // record the offset
         dragMe.diffX = event.clientX - item.offsetLeft;
         dragMe.diffY = event.clientY - item.offsetTop;
      });
   }

   function moveMouse(event) { /* where is the mouse? */
      event = EventUtil.getEvent(event);
      //var target = EventUtil.getTarget(event);

      if (dragMe.obj !== null) {
         if (dragMe.isSlider) {
            var newX = event.clientX - dragMe.diffX;
            /* right boundaries */
            if (newX > (sliderOffsetX + 500)) {
               newX = (sliderOffsetX + 500);
            }
            /* left boundaries */
            if (newX < sliderOffsetX) {
               newX = sliderOffsetX;
            }
            dragMe.obj.style.left = newX + "px";
            updateScale(newX, sliderOffsetX, spacing);

         } else { /* this is not a slider, we can move it freely */
            //dragMe.obj.style.position = 'absolute';
            dragMe.obj.style.left = (event.clientX - dragMe.diffX) + "px";
            dragMe.obj.style.top = (event.clientY - dragMe.diffY) + "px";
         }
      }
   }


   function initSlider() {
      if (slider_opt == 1) {
         svoSlider.moveSliderToPos(sliderOffsetX + 10 + 250, true);
      }
      if (slider_opt == 2) {
         svoSlider.moveSliderToPos(sliderOffsetX + 205 + Math.floor(Math.random() * 101), true);
      }
      if (slider_opt == 3) {
         document.getElementById('slider').style.display = 'none';
         document.getElementById('youget_bar').style.width = 0;
         document.getElementById('othergets_bar').style.width = 0;
         document.getElementById('slider_value').innerHTML = "&nbsp;";
         document.getElementById('slider_amount').innerHTML = "&nbsp;";
         document.getElementById('infoyougetval').innerHTML = "&nbsp;";
         document.getElementById('infoothergetsval').innerHTML = "&nbsp;";
      }
   }

   /**
    *
    * @param {number} pos
    * @param {boolean} allowed
    * @returns {boolean}
    */
   svoSlider.moveSliderToPos = function (pos, allowed) {
      if (allowed) {
         if (pos < 410) {
            pos = 410;
         }
         if (pos > 910) {
            pos = 910;
         }
         document.getElementById('slider').style.left = pos - 10 + 'px';
         updateScale(pos - 10, sliderOffsetX, 0);
         return true;
      } else if (!allowed && (dragMe.obj.style.display == 'none')) {
         /* we have option 3 of displaying: once we click, the slider appears */
         if (pos < 410) {
            pos = 410;
         }
         if (pos > 910) {
            pos = 910;
         }
         document.getElementById('slider').style.display = 'block';
         document.getElementById('slider').style.left = pos - 10 + 'px';
         updateScale(pos - 10, sliderOffsetX, 0);
         return true;
      } else {
         return false;
      }
   };


   /**
    *
    * @param {String} name
    * @param {number} xpos
    * @param {number} ypos
    * @param {Object} elem
    */
   function addSlider(name, xpos, ypos, elem) {
//      var sliderContainer = document.createElement('div');
//      sliderContainer.setAttribute('id', 'sliderContainer');
//      sliderContainer.setAttribute('width', "300px");
//      sliderContainer.setAttribute('height', "100px");
//      //sliderContainer.style.position = "absolute";
//      sliderContainer.style.top = (ypos - 100) + "px";
//      sliderContainer.style.left = (xpos - 100) + "px";
//      elem.appendChild(sliderContainer);
      var newValue = document.createElement('span');
      newValue.setAttribute('id', 'aboveSliderVal_youGet');
      newValue.style.position = "absolute";
      newValue.style.top = ypos + "px";
      newValue.style.display = 'block';
      elem.appendChild(newValue);

      var newAmount = document.createElement('span');
      newAmount.setAttribute('id', 'belowSliderVal_otherGets');
      newAmount.style.position = "absolute";
      newAmount.style.top = 43 + ypos + "px";
      newAmount.style.display = 'block';
      elem.appendChild(newAmount);

      var newBackground = document.createElement('div');
      newBackground.style.position = "absolute";
      newBackground.style.display = "inline-block";
      newBackground.style.zIndex = 0;
      newBackground.style.left = 5 + xpos + "px";
      newBackground.style.top = 25 + ypos + "px";
      newBackground.style.width = 510 + "px";
      newBackground.innerHTML = "&nbsp;";
      //newBackground.setAttribute('onClick', "svoSlider.moveSliderToPos('" + name + "', event.clientX, 0);");
      newBackground.style.backgroundImage = 'url(' + qualifyURL("/VAADIN/themes/survey/images/slider/slider_big.gif") + ')';
      elem.appendChild(newBackground);

      var newSlider = document.createElement('img');
      newSlider.setAttribute('id', name);
      newSlider.setAttribute('src', qualifyURL('/VAADIN/themes/survey/images/slider/circle_ipad.gif'));
      newSlider.setAttribute('width', "30px");
      newSlider.setAttribute('height', "30px");
      newSlider.setAttribute('alt', "Move to select value.");
      newSlider.setAttribute('title', "Move to select value.");
      elem.appendChild(newSlider);
      newSlider.style.position = "absolute";

      /* we randomize the slider starting position */
      /*newSlider.style.left = xpos+(Math.random()*300)+"px";*/

      /* we always start the slider at 0 */
      newSlider.style.left = xpos + (0) + "px";

      newSlider.style.top = ypos + 19 + "px";

      sliderOffsetX = xpos;

      /* some extra optimization */
      elem_aboveSliderVal_youGet = document.getElementById('aboveSliderVal_youGet');
      elem_infoyougetval = document.getElementById('infoyougetval');
      elem_youget_bar = document.getElementById('youget_bar');

      elem_belowSliderVal_otherGets = document.getElementById('belowSliderVal_otherGets');
      elem_infoothergetsval = document.getElementById('infoothergetsval');
      elem_othergets_bar = document.getElementById('othergets_bar');

      clickMe(newSlider, true);
   }

   /*                                                                 */
   /*                      END OF SLIDER SCRIPT                       */
   /*                                                                 */
   /* --------------------------------------------------------------- */

   /* init key variables */
   var randomizeFirstSix = '1'; // randomize first six items (core items)
   var randomizeLastNine = '0'; // randomize last nine items (secondary items)
   var randomizeLeftRightFirstSix = '1'; // flips left and right randomly, items will be mirrored (can be combined with the above)
   var randomizeLeftRightLastNine = '1'; // flips left and right randomly for the secondary items
   var slider_opt = '1'; // slider initial position (1=middle; 2=randomize around middle; 3=click before it appears)
   var storeSearchPath = '1'; // should intermediate values (search path) be stored or not (1=not; 0=not)

   var maxStages = 15;
   var slideName = "slider";
   var delayInvestClick = 500; // in ms
   var set = new Array(2);
   var sliderInstructions;

   /* script variables */
   /** @type {number} */
   var round, stage, sessionStart;
   /** @type {string} */
   var gameMsg;
   /** @type {boolean} */
   var sliderDisabled;

   /* initialize data sets */
   /** @type {Array} */
   var itemID, itemLowvalYou, itemHighvalYou, itemDescYou, itemLowvalOther, itemHighvalOther, itemDescOther, timeChoice, choiceYou, choiceOther;
   /** @type {number} */
   var firstSixAngle;
   /** @type {String} */
   var firstSixCat;
   /* messages */
   /** Messages
    * @type {String} */
   var msgSelectValue, msgGameEnded, msgStartGame;
   svoSlider.msgInstructions = null;
   //
   /**
    * Data to return
    * @type {number} */
   var perc;
   /** @type {boolean} */
   var isdag, transitHolds;


   var initializeSetsOfChoices = function () {
      /* this defines the choices as listed on the sheets, the array looks like:
       set 1 --> option 1 --> 'you get' lowest value
       --> 'you get' highest value
       --> 'you get' descending (1=yes; 0=no)
       --> 'other gets' lowest value
       --> 'other gets' highest value
       --> 'other gets' descending (1=yes; 0=no)
       --> item number according to original sheet (used to keep track when shuffling set[1] and set[2])
       option 2 ... etc.
       */
      set[1] = [];
      set[1][1] = new Array(7);
      set[1][1][1] = 85;
      set[1][1][2] = 85;
      set[1][1][3] = 0;
      set[1][1][4] = 15;
      set[1][1][5] = 85;
      set[1][1][6] = 1;
      set[1][1][7] = 1;
      set[1][2] = new Array(7);
      set[1][2][1] = 85;
      set[1][2][2] = 100;
      set[1][2][3] = 0;
      set[1][2][4] = 15;
      set[1][2][5] = 50;
      set[1][2][6] = 0;
      set[1][2][7] = 2;
      set[1][3] = new Array(7);
      set[1][3][1] = 50;
      set[1][3][2] = 85;
      set[1][3][3] = 0;
      set[1][3][4] = 85;
      set[1][3][5] = 100;
      set[1][3][6] = 1;
      set[1][3][7] = 3;
      set[1][4] = new Array(7);
      set[1][4][1] = 50;
      set[1][4][2] = 85;
      set[1][4][3] = 0;
      set[1][4][4] = 15;
      set[1][4][5] = 100;
      set[1][4][6] = 1;
      set[1][4][7] = 4;
      set[1][5] = new Array(7);
      set[1][5][1] = 50;
      set[1][5][2] = 100;
      set[1][5][3] = 1;
      set[1][5][4] = 50;
      set[1][5][5] = 100;
      set[1][5][6] = 0;
      set[1][5][7] = 5;
      set[1][6] = new Array(7);
      set[1][6][1] = 85;
      set[1][6][2] = 100;
      set[1][6][3] = 1;
      set[1][6][4] = 50;
      set[1][6][5] = 85;
      set[1][6][6] = 0;
      set[1][6][7] = 6;

      set[2] = [];
      set[2][1] = new Array(7);
      set[2][1][1] = 70;
      set[2][1][2] = 100;
      set[2][1][3] = 1;
      set[2][1][4] = 50;
      set[2][1][5] = 100;
      set[2][1][6] = 0;
      set[2][1][7] = 7;
      set[2][2] = new Array(7);
      set[2][2][1] = 90;
      set[2][2][2] = 100;
      set[2][2][3] = 0;
      set[2][2][4] = 90;
      set[2][2][5] = 100;
      set[2][2][6] = 1;
      set[2][2][7] = 8;
      set[2][3] = new Array(7);
      set[2][3][1] = 50;
      set[2][3][2] = 100;
      set[2][3][3] = 1;
      set[2][3][4] = 70;
      set[2][3][5] = 100;
      set[2][3][6] = 0;
      set[2][3][7] = 9;
      set[2][4] = new Array(7);
      set[2][4][1] = 90;
      set[2][4][2] = 100;
      set[2][4][3] = 1;
      set[2][4][4] = 70;
      set[2][4][5] = 100;
      set[2][4][6] = 0;
      set[2][4][7] = 10;
      set[2][5] = new Array(7);
      set[2][5][1] = 70;
      set[2][5][2] = 100;
      set[2][5][3] = 0;
      set[2][5][4] = 70;
      set[2][5][5] = 100;
      set[2][5][6] = 1;
      set[2][5][7] = 11;
      set[2][6] = new Array(7);
      set[2][6][1] = 50;
      set[2][6][2] = 100;
      set[2][6][3] = 0;
      set[2][6][4] = 90;
      set[2][6][5] = 100;
      set[2][6][6] = 1;
      set[2][6][7] = 12;
      set[2][7] = new Array(7);
      set[2][7][1] = 50;
      set[2][7][2] = 100;
      set[2][7][3] = 0;
      set[2][7][4] = 50;
      set[2][7][5] = 100;
      set[2][7][6] = 1;
      set[2][7][7] = 13;
      set[2][8] = new Array(7);
      set[2][8][1] = 70;
      set[2][8][2] = 100;
      set[2][8][3] = 1;
      set[2][8][4] = 90;
      set[2][8][5] = 100;
      set[2][8][6] = 0;
      set[2][8][7] = 14;
      set[2][9] = new Array(7);
      set[2][9][1] = 90;
      set[2][9][2] = 100;
      set[2][9][3] = 0;
      set[2][9][4] = 50;
      set[2][9][5] = 100;
      set[2][9][6] = 1;
      set[2][9][7] = 15;

      /* we can randomize the order of items here (use randomizeFirstSix and randomizeLastNine) */
      if (randomizeFirstSix == 1) {
         set[1].shift();
         set[1].sort(function () {
            return 0.5 - Math.random()
         });
         set[1].unshift('');
      }
      if (randomizeLastNine == 1) {
         set[2].shift();
         set[2].sort(function () {
            return 0.5 - Math.random()
         });
         set[2].unshift('');
      }

      /* we will flip left and right occasionally */
      if (randomizeLeftRightFirstSix == 1) {
         var j, memorize;
         for (j = 1; j < set[1].length; j++) {
            if (Math.random() < 0.5) {
               memorize = set[1][j][3];
               set[1][j][3] = set[1][j][6];
               set[1][j][6] = memorize;
            }
         }
      }
      if (randomizeLeftRightLastNine == 1) {
         for (j = 1; j < set[2].length; j++) {
            if (Math.random() < 0.5) {
               memorize = set[2][j][3];
               set[2][j][3] = set[2][j][6];
               set[2][j][6] = memorize;
            }
         }
      }

      /* initialize script variables */
      round = 1;
      stage = 1;
      gameMsg = "";
      sessionStart = Math.round((new Date().getTime()) / 100) / 10;

      /* initialize data sets */
      itemID = new Array(15);
      itemLowvalYou = new Array(15);
      itemHighvalYou = new Array(15);
      itemDescYou = new Array(15);
      itemLowvalOther = new Array(15);
      itemHighvalOther = new Array(15);
      itemDescOther = new Array(15);
      timeChoice = new Array(15);
      choiceYou = new Array(15);
      choiceOther = new Array(15);

      // spacing for the elem_youGet
      spacing = 0;
      // Data to return
      perc = 0;

      // By default dag is true (set to false if loop is found)
      isdag = true;
   };



   /* here, we interpret the results in the form of an angle */
   function calcAngle() {
      var avg_self = ((choiceYou[1] + choiceYou[2] + choiceYou[3] + choiceYou[4] + choiceYou[5] + choiceYou[6]) / 6) - 50;
      var avg_other = ((choiceOther[1] + choiceOther[2] + choiceOther[3] + choiceOther[4] + choiceOther[5] + choiceOther[6]) / 6) - 50;

      var angle_radians = Math.atan2(avg_self, avg_other);
      var angle_degrees = 90 - angle_radians * 180 / Math.PI;

      var category;
      if (angle_degrees > 57.15) {
         category = "altruistic";
      } else if (angle_degrees > 22.45) {
         category = "prosocial";
      } else if (angle_degrees > -12.04) {
         category = "individualistic";
      } else {
         category = "competitive";
      }

      if (angle_degrees < -16.26) {
         perc = 1;
      } else if (angle_degrees < 5.08) {
         perc = 5;
      } else if (angle_degrees < 7.82) {
         perc = 15;
      } else if (angle_degrees < 12.31) {
         perc = 20;
      } else if (angle_degrees < 16.11) {
         perc = 25;
      } else if (angle_degrees < 19.02) {
         perc = 30;
      } else if (angle_degrees < 23.95) {
         perc = 35;
      } else if (angle_degrees < 26.46) {
         perc = 40;
      } else if (angle_degrees < 28.60) {
         perc = 45;
      } else if (angle_degrees < 30.84) {
         perc = 50;
      } else if (angle_degrees < 32.91) {
         perc = 55;
      } else if (angle_degrees < 33.47) {
         perc = 60;
      } else if (angle_degrees < 34.87) {
         perc = 65;
      } else if (angle_degrees < 37.48) {
         perc = 70;
      } else if (angle_degrees < 40.26) {
         perc = 75;
      } else if (angle_degrees < 42.56) {
         perc = 80;
      } else if (angle_degrees < 45) {
         perc = 85;
      } else if (angle_degrees < 46.00) {
         perc = 90;
      } else if (angle_degrees < 50) {
         perc = 95;
      } else if (angle_degrees > 50.62) {
         perc = 99;
      }

      firstSixAngle = angle_degrees;
      firstSixCat = category;

      return {angle: angle_degrees, cat: category};
   }


   /*

    Function:       isDag(G)
    Purpose:        Returns a value 1 if a graph is acyclical and a value
    0 if it is not. This is used to determine transitivity
    in decision making problems.
    Input:          G, which is an array describing a graph as below

    G = new Array();
    G[1][1] = '0';
    G[1][2] = '1';
    G[2][1] = '1';
    G[2][2] = '0';

    Notation: G[from][to], so above, an edge (arc) runs
    from 1 -> 2 and from 2 -> 1. That makes this graph
    cyclical.

    Notes:          The function searches for backedges and is not entirely
    efficient with intent. It tracks paths from every node
    with the purpose of also being able to detect intransitive
    decisions in unconnected graphs.

    */


   function isDag(Graph) {
      for (var x = 1; x < Graph.length; x++) {
         /* now, we loop through all of the paths, while keeping track of the visited ones */

         //document.write('<br>starting at '+x+'<br>');
         followPath(x, Graph);
      }
      transitHolds = isdag;
   }

   /* a recursive function looping through the path */
   function followPath(fromId, Graph, visArrCP_passed) {
      var visArrCP = visArrCP_passed;
      if (!visArrCP) {
         visArrCP = fromId + '';
      }
      var cnt = 0;

      for (var s = 1; s < Graph[fromId].length; s++) {
         var tArr = [];
         tArr[s] = visArrCP;

         if (Graph[fromId][s] == 1) {
            cnt++; // end of path counter

            //document.write('moving from '+fromId+' to '+s+'<br>');
            if (tArr[s].indexOf(s) >= 0) {
               //document.write('loop found<br>');
               isdag = false;
               break;
            }
            tArr[s] += s;
            followPath(s, Graph, tArr[s]);
         }
      }

      if (cnt == 0) {
         //document.write('end of path<br>');
      }
   }

   // --------------------------------------------------------------------------
   // END OF GRAPH FUNCTION
   // --------------------------------------------------------------------------

   /* we test to see if the resulting graph is acyclical, otherwise results make no sense */
   function transitivityHolds() {
      var toGraph = [];

      /* we populate the variable & graph */
      for (var i = 1; i <= 4; i++) {
         toGraph[i] = [];
      }
      toGraph[1][1] = 0;
      toGraph[2][2] = 0;
      toGraph[3][3] = 0;
      toGraph[4][4] = 0;

      /* convert the input to a useful graph */
      for (var k = 1; k <= 6; k++) {
         switch (Number(itemID[k])) {
            case 1:
               if (choiceOther[k] > 50) {
                  toGraph[4][2] = 1;
                  toGraph[2][4] = 0;
               } //61 and 39 for thirds
               else if (choiceOther[k] < 50) {
                  toGraph[4][2] = 0;
                  toGraph[2][4] = 1;
               } else {
                  toGraph[4][2] = 0;
                  toGraph[2][4] = 0;
               }
               break;
            case 2:
               if (choiceOther[k] > 32.5) {
                  toGraph[4][3] = 1;
                  toGraph[3][4] = 0;
               } //38 and 27 for thirds
               else if (choiceOther[k] < 32.5) {
                  toGraph[4][3] = 0;
                  toGraph[3][4] = 1;
               } else {
                  toGraph[4][3] = 0;
                  toGraph[3][4] = 0;
               }
               break;
            case 3:
               if (choiceOther[k] > 92.5) {
                  toGraph[2][1] = 1;
                  toGraph[1][2] = 0;
               } //95 and 90 for thirds
               else if (choiceOther[k] < 92.5) {
                  toGraph[2][1] = 0;
                  toGraph[1][2] = 1;
               } else {
                  toGraph[1][2] = 0;
                  toGraph[2][1] = 0;
               }
               break;
            case 4:
               if (choiceOther[k] > 57.5) {
                  toGraph[4][1] = 1;
                  toGraph[1][4] = 0;
               }//71 and 44 for thirds
               else if (choiceOther[k] < 57.5) {
                  toGraph[4][1] = 0;
                  toGraph[1][4] = 1;
               } else {
                  toGraph[4][1] = 0;
                  toGraph[1][4] = 0;
               }
               break;
            case 5:
               if (choiceOther[k] > 75) {
                  toGraph[3][1] = 1;
                  toGraph[1][3] = 0;
               }//83 and 67 for thirds
               else if (choiceOther[k] < 75) {
                  toGraph[3][1] = 0;
                  toGraph[1][3] = 1;
               } else {
                  toGraph[1][3] = 0;
                  toGraph[3][1] = 0;
               }
               break;
            case 6:
               if (choiceOther[k] > 67.5) {
                  toGraph[3][2] = 1;
                  toGraph[2][3] = 0;
               }//73 and 62 for thirds
               else if (choiceOther[k] < 67.5) {
                  toGraph[3][2] = 0;
                  toGraph[2][3] = 1;
               } else {
                  toGraph[2][3] = 0;
                  toGraph[3][2] = 0;
               }
               break;
         }
      }

      isDag(toGraph);
   }

   /* analysis of the secondary items
    these have been hardcoded       */

   /* define variables */
   var altr_value = 0;
   var indiv_value = 0;
   var ineqav_value = 0;
   var jointgain_value = 0;
   var secondRes = "";

// (good) approximation due to rounding
   function secondaryItemResults() {
      var norm;
      if (choiceOther[15] > 0) {
         for (var k = 7; k <= 15; k++) {
            switch (Number(itemID[k])) {
               case 7:
                  norm = 2; // normalization value for lower axis
                  altr_value += Math.abs(100 - choiceOther[k]) * norm;
                  indiv_value += Math.abs(50 - choiceOther[k]) * norm;
                  ineqav_value += Math.abs(81 - choiceOther[k]) * norm;
                  jointgain_value += Math.abs(100 - choiceOther[k]) * norm;
                  break;
               case 8:
                  norm = 10;
                  altr_value += Math.abs(100 - choiceOther[k]) * norm;
                  indiv_value += Math.abs(90 - choiceOther[k]) * norm;
                  ineqav_value += Math.abs(95 - choiceOther[k]) * norm;
                  jointgain_value += 0; // there is no joint gain maximization here
                  break;
               case 9:
                  norm = 2;
                  altr_value += Math.abs(50 - choiceYou[k]) * norm;
                  indiv_value += Math.abs(100 - choiceYou[k]) * norm;
                  ineqav_value += Math.abs(81 - choiceYou[k]) * norm;
                  jointgain_value += Math.abs(100 - choiceYou[k]) * norm;
                  break;
               case 10:
                  norm = 10 / 3;
                  altr_value += Math.abs(100 - choiceOther[k]) * norm;
                  indiv_value += Math.abs(70 - choiceOther[k]) * norm;
                  ineqav_value += Math.abs(93 - choiceOther[k]) * norm;
                  jointgain_value += Math.abs(100 - choiceOther[k]) * norm;
                  break;
               case 11:
                  norm = 10 / 3;
                  altr_value += Math.abs(100 - choiceOther[k]) * norm;
                  indiv_value += Math.abs(70 - choiceOther[k]) * norm;
                  ineqav_value += Math.abs(85 - choiceOther[k]) * norm;
                  jointgain_value += 0; // no joint gain maximization
                  break;
               case 12:
                  norm = 2;
                  altr_value += Math.abs(50 - choiceYou[k]) * norm;
                  indiv_value += Math.abs(100 - choiceYou[k]) * norm;
                  ineqav_value += Math.abs(92 - choiceYou[k]) * norm;
                  jointgain_value += Math.abs(100 - choiceYou[k]) * norm;
                  break;
               case 13:
                  norm = 2;
                  altr_value += Math.abs(100 - choiceOther[k]) * norm;
                  indiv_value += Math.abs(50 - choiceOther[k]) * norm;
                  ineqav_value += Math.abs(75 - choiceOther[k]) * norm;
                  jointgain_value += 0;
                  break;
               case 14:
                  norm = 10 / 3;
                  altr_value += Math.abs(70 - choiceYou[k]) * norm;
                  indiv_value += Math.abs(100 - choiceYou[k]) * norm;
                  ineqav_value += Math.abs(93 - choiceYou[k]) * norm;
                  jointgain_value += Math.abs(100 - choiceYou[k]) * norm;
                  break;
               case 15:
                  norm = 2;
                  altr_value += Math.abs(100 - choiceOther[k]) * norm;
                  indiv_value += Math.abs(50 - choiceOther[k]) * norm;
                  ineqav_value += Math.abs(92 - choiceOther[k]) * norm;
                  jointgain_value += Math.abs(100 - choiceOther[k]) * norm;
                  break;
            }
         }

         altr_value /= 9;
         indiv_value /= 9;
         ineqav_value /= 9;
         jointgain_value /= 6;

         if ((ineqav_value <= Math.min(indiv_value, altr_value)) && (jointgain_value <= Math.min(indiv_value, altr_value))) {
            if (ineqav_value < jointgain_value) {
               secondRes = 'inequality averse';
            } else {
               secondRes = 'joint gain maximizer';
            }
         }
         return true;
      } else {
         return false;
      }

   }

   function createMsg() { /* dynamically generate messages using predefined templates, any public variable is usable */
      var movableNote = "";
      if (round > 2) {
         movableNote = "";
      } else {
         movableNote = "Note: this box is freely movable.<br><br>";
      }
      if (slider_opt == 3) {
         sliderInstructions = "<br><br>You will have to <strong>click on the grey slider bar before the slider itself appears</strong>."; // additional instructions in the welcome screen
      } else {
         sliderInstructions = "";
      }

      msgSelectValue = "<span style='position:relative; top: 4px; left:-4px;'><img src='" + qualifyURL('/VAADIN/themes/survey/images/slider/info_icon2.png') + "' width='18px' height='18px'></span><strong>Select a value</strong><br><br>" + movableNote + "Click anywhere on the bar to make the slider appear. You can then drag it and select the values according to your preferences.<br><br><div id=\"continuebutton\" onClick=\"svoSlider.hideMessagesEnableSlider();\" onMouseOver=\"this.style.background='#E5E5E5';this.style.borderColor='black';\" onMouseOut=\"this.style.background='#FFF';this.style.borderColor='grey';this.style.cursor='pointer';\">Continue</div><br>";

      var msgGameEndedText = "You have completed all stages and finished the decision task.<br><br><br>Thank you for your participation in this part of the experiment.<br><br>";

      msgGameEnded = "<span style='position:relative; top: 4px; left:-4px;'><img src='" + qualifyURL('/VAADIN/themes/survey/images/slider/info_icon2.png') + "' width='18px' height='18px'></span><strong>Task Completed</strong><br><br>" + msgGameEndedText + "<div id='continuebutton' onClick='svoSlider.surveyFinished()' onMouseOver=\"this.style.background='#E5E5E5';this.style.borderColor='black';\" onMouseOut=\"this.style.background='#FFF';this.style.borderColor='grey';this.style.cursor='pointer';\">Continue</div><br>";

      var msgStartGameText = "Welcome to the online social value orientation measure.</strong><br><br>" + movableNote + "Underneath this movable box you find the SVO Slider measure. There are 15 situations. Imagine you are in each situation and you have been given an amount of money that you can split between yourself an an anonymous person. Your task is to choose the amount of money to give to the other person and the amount of money to keep for yourself.<br><br>Carefully consider how much you would keep and how much you would give to your partner. Once you have made your choice, press the Submit button to go on to the next question. <br><br> <br>There are no right or wrong answers, this is all about your own personal preferences.<br>";

      msgStartGame = "<span style='position:relative; top: 4px; left:-4px;'><img src='" + qualifyURL('/VAADIN/themes/survey/images/slider/info_icon2.png') + "' width='18px' height='18px'></span><strong>" + msgStartGameText + sliderInstructions + "<br><br><div id='continuebutton' onClick='svoSlider.startGame();' onMouseOver=\"this.style.background='#E5E5E5';this.style.borderColor='black';\" onMouseOut=\"this.style.background='#FFF';this.style.borderColor='grey';this.style.cursor='pointer';\">Continue</div><br>";

      var msgInstructionsText = "<strong>Instructions</strong><br><br>Below, you see a slider. You can change the slider to adjust the amount of money you and the other person will receive.<br><br>The numbers at the ends of the slider show the range of possible distributions for you and the other person.<br><br>Once you have moved the slider to the distribution you most prefer, press the Submit button.<br><br>";

      svoSlider.msgInstructions = "<span style='position:relative; top: 4px; left:-4px;'><img src='" + qualifyURL('/VAADIN/themes/survey/images/slider/info_icon2.png') + "' width='18px' height='18px'></span>" + msgInstructionsText + "<div id='continuebutton' onClick=\"svoSlider.hideMessagesEnableSlider();\" onMouseOver=\"this.style.background='#E5E5E5';this.style.borderColor='black';\" onMouseOut=\"this.style.background='#FFF';this.style.borderColor='grey';this.style.cursor='pointer';\">Continue</div><br>";
   }


   /**
    *
    * @param gameMsg
    * @param {Object=} dimensions with defaults: {left : 370, width: 500, top: 330}
    */
   svoSlider.showMessage = function (gameMsg, dimensions) {
      dimensions = dimensions || {};
      // Assuming underscore.js:
      var defaults = {left: 370, width: 500, top: 330};
      var dims = _.defaults(dimensions, defaults);

      // Don't allow the slider to move while looking at a message box.
      sliderDisabled = true;
      console.log("5 sliderDisabled = " + sliderDisabled);

      var elem = document.getElementById("infomessage");
      elem.style.visibility = "visible";
      elem.style.left = parseInt(dims.left) + "px";
      elem.style.width = parseInt(dims.right) + "px";
      elem.style.top = parseInt(dims.top) + "px";
      document.getElementById("message_contents").innerHTML = gameMsg;
      document.getElementById("instructionstitle").style.visibility = "hidden";
      document.getElementById("gobutton").style.visibility = "hidden";
   };

   svoSlider.hideMessagesEnableSlider = function () {
      document.getElementById("infomessage").style.visibility = "hidden";
      document.getElementById("instructionstitle").style.visibility = "visible";
      document.getElementById("gobutton").style.visibility = "visible";
      sliderDisabled = false;
      console.log("6 sliderDisabled = " + sliderDisabled);
   };

   svoSlider.startGame = function () {
      svoSlider.hideMessagesEnableSlider();
      initGame();
   };

   svoSlider.surveyFinished = function () {
      console.log("surveyFinished called. Calling server.");
      chdp_Survey1View_weAreFinished(true);
   };

   function dispGame() { /* display the current game status */
      //document.getElementById("round").innerHTML = round+" of "+maxRounds;
      document.getElementById("stage").innerHTML = stage + " of " + maxStages;
      document.getElementById("msgbox").innerHTML = gameMsg;
   }

   /*
    The ticker script: tracks intermediate search paths.
    */
   var storeTimeInterval = null;
   var ticksTime = [];
   var ticksValYou = [];
   var ticksValOther = [];
   var first_item_timestamp = 0;

   for (var i = 1; i <= 15; i++) {
      ticksTime[i] = [];
      ticksValYou[i] = [];
      ticksValOther[i] = [];
   }

   function trackTicks() {
      storeTimeInterval = setInterval(storeInterimValues, 150);
   }

   function storeInterimValues() {
      var d = new Date();
      ticksTime[stage].push(Math.round(d.getTime() / 10) / 100);
      ticksValYou[stage].push(Math.round(valueYouGet));
      ticksValOther[stage].push(Math.round(valueOtherGets));
   }

   //    End of ticker script.

   function initGame() {
      first_item_timestamp = Math.round((new Date().getTime()) / 100) / 10;
      initializeSetsOfChoices();
      adjustSlider(set[round][stage][1], set[round][stage][2], set[round][stage][3], set[round][stage][4], set[round][stage][5], set[round][stage][6]);

      //     we store some data
      itemLowvalYou[stage] = set[round][stage][1];
      itemHighvalYou[stage] = set[round][stage][2];
      itemDescYou[stage] = set[round][stage][3];
      itemLowvalOther[stage] = set[round][stage][4];
      itemHighvalOther[stage] = set[round][stage][5];
      itemDescOther[stage] = set[round][stage][6];
      itemID[stage] = set[round][stage][7];

      initSlider();
      if (storeSearchPath == 1) {
         trackTicks();
      }
      gameMsg = '';
      dispGame();
   }

   function saveToServer() {
      itemID.splice(0, 1);
      itemLowvalYou.splice(0, 1);
      itemHighvalYou.splice(0, 1);
      itemDescYou.splice(0, 1);
      itemLowvalOther.splice(0, 1);
      itemHighvalOther.splice(0, 1);
      itemDescOther.splice(0, 1);
      choiceYou.splice(0, 1);
      choiceOther.splice(0, 1);
      ticksValYou.splice(0, 1);
      ticksValOther.splice(0, 1);
      timeChoice.splice(0, 1);
      ticksTime.splice(0, 1);

      chdp_Survey1View_returnData(itemID, itemLowvalYou, itemHighvalYou, itemDescYou, itemLowvalOther, itemHighvalOther, itemDescOther, choiceYou, choiceOther, ticksValYou, ticksValOther, timeChoice, ticksTime, perc, firstSixAngle, sessionStart, first_item_timestamp, altr_value, indiv_value, ineqav_value, jointgain_value, firstSixCat, secondRes, transitHolds);
   }

   function roundControl() { /* controls stage settings, rounds and message deliveries */
      createMsg();

      if (stage == maxStages) {
         calcAngle();
         transitivityHolds();
         secondaryItemResults();
         saveToServer(); // we save the data;
         if (transitHolds == 1) {
            svoSlider.showMessage(msgGameEnded);
         } else {
            var msgResult = "<span style='position:relative; top: 4px; left:-4px;'><img src='" + qualifyURL('/VAADIN/themes/survey/images/slider/info_icon2.png') + "' width='18px' height='18px'></span><strong>Task Incomplete</strong><br><br>Your choices were not successfully evaluated.  You provided inconstant answers which makes it impossible to accurately estimate your preferences.  <br><br>Consider starting over and answering each of the questions more carefully.<br><br><div id=\"continuebutton\" onClick=\"svoSlider.restart()\" onMouseOver=\"this.style.background='#E5E5E5';this.style.borderColor='black';\" onMouseOut=\"this.style.background='#FFF';this.style.borderColor='grey';this.style.cursor='pointer';\">Continue</div><br>";
            svoSlider.showMessage(msgResult);
         }
      } else if (stage >= 6) { // due to the structure, we have 2 save points
         round = 2;
         stage++;
         if (stage == 7) {
            calcAngle();
            transitivityHolds();
            //ajaxFunction();
         }
         var fic_stage = stage - 6;

         adjustSlider(set[round][fic_stage][1], set[round][fic_stage][2], set[round][fic_stage][3], set[round][fic_stage][4], set[round][fic_stage][5], set[round][fic_stage][6]);

         /* we store some data */
         itemLowvalYou[stage] = set[round][fic_stage][1];
         itemHighvalYou[stage] = set[round][fic_stage][2];
         itemDescYou[stage] = set[round][fic_stage][3];
         itemLowvalOther[stage] = set[round][fic_stage][4];
         itemHighvalOther[stage] = set[round][fic_stage][5];
         itemDescOther[stage] = set[round][fic_stage][6];
         itemID[stage] = set[round][fic_stage][7];

         initSlider();
         if (storeSearchPath == 1) {
            trackTicks();
         }
      } else {
         stage += 1;
         adjustSlider(set[round][stage][1], set[round][stage][2], set[round][stage][3], set[round][stage][4], set[round][stage][5], set[round][stage][6]);

         /* we store some data */
         itemLowvalYou[stage] = set[round][stage][1];
         itemHighvalYou[stage] = set[round][stage][2];
         itemDescYou[stage] = set[round][stage][3];
         itemLowvalOther[stage] = set[round][stage][4];
         itemHighvalOther[stage] = set[round][stage][5];
         itemDescOther[stage] = set[round][stage][6];
         itemID[stage] = set[round][stage][7];

         initSlider();
         if (storeSearchPath == 1) {
            trackTicks();
         }
      }
   }

//   /* a function to simulate a button click, so we do not have to redefine events on button clicks (from codingforums.com) */
//   if (typeof HTMLElement != "undefined") {
//      HTMLElement.prototype.click = function () {
//         var evt = this.ownerDocument.createEvent('MouseEvents');
//         evt.initMouseEvent('click', true, true, this.ownerDocument.defaultView, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
//         this.dispatchEvent(evt);
//      }
//   }

//   function keyPressed(key) { /* make the enter button work as well */
//      var k = key || window.event;
//      if (k.keyCode == 13) {
//         /* if we see the box, we map the enter button to it */
//         if (document.getElementById('infomessage').style.visibility == 'visible') {
//            document.getElementById('continuebutton').click();
//         } else {
//            svoSlider.submitClick();
//         }
//      }
//   }

   /* this is where we execute the game for real */
   svoSlider.submitClick = function () {
      //debugger;
      if (sliderDisabled == true) {
         return false;
      }
      /* disable for any use (return key, click...) */
      if (document.getElementById('slider').style.display == 'none') {
         svoSlider.showMessage(msgSelectValue);
         sliderDisabled = false;
         console.log("1 sliderDisabled = " + sliderDisabled);
         return false;
      }
      sliderDisabled = true;
      console.log("2 sliderDisabled = " + sliderDisabled);
      setTimeout(function () {sliderDisabled = false;console.log("3 sliderDisabled = " + sliderDisabled);
      }, delayInvestClick);

      clearInterval(storeTimeInterval); // clear the search path interval
      //alert(ticksValYou[stage]);

      /* we store the data */
      timeChoice[stage] = Math.round((new Date().getTime()) / 10) / 100;
      choiceYou[stage] = Math.round(valueYouGet);
      choiceOther[stage] = Math.round(valueOtherGets);

      setTimeout(function () {document.getElementById('gobutton').style.background = 'white'}, 5);
      setTimeout(function () {document.getElementById('gobutton').style.background = 'grey'}, 10);
      setTimeout(function () {document.getElementById('gobutton').style.background = 'white'}, 150);
      setTimeout(function () {document.getElementById('stage').style.color = 'black'}, 5);
      setTimeout(function () {document.getElementById('stage').style.color = 'red'}, 10);
      setTimeout(function () {document.getElementById('stage').style.color = 'black'}, 150);

      roundControl();
      dispGame();
      gameMsg = '';
      return true;
   };

   svoSlider.restart = function () {
      chdp_Survey1View_restart();
   };

   function flushThis(id) {
      var msie = 'Microsoft Internet Explorer';
      var tmp = 0;
      var elementOnShow = document.getElementById(id);
      window.setTimeout(function () {
         if (navigator.appName == msie) {
            tmp = elementOnShow.parentNode.offsetTop + 'px';
         } else {
            tmp = elementOnShow.offsetTop;
         }
      }, 10);
   }

   // Needed by Chrome, as of Release 21. Triggers a screen refresh, removing drag garbage.
   function cleanDisplay() {
      var c = document.createElement('div');
      c.innerHTML = 'x';
      c.style.visibility = 'hidden';
      c.style.height = '1px';
      document.body.insertBefore(c, document.body.firstChild);
      window.setTimeout(function () {document.body.removeChild(c)}, 1);
   }

   // ---------------------------------------------------------------------------------------------
   // Initialize. Treat like document.onload
   // ---------------------------------------------------------------------------------------------
   svoSlider.initialize = function (sliderElement) {
      // A cross-browser eventutility from Professional Javascript for Web Developers, p. 441
      EventUtil.addHandler(document.body, "mousemove", moveMouse);
      EventUtil.addHandler(document.body, "mouseup", function () {dragMe.obj = null;});

      objMinYouGet = document.getElementById('min_youget');
      objMaxYouGet = document.getElementById('max_youget');
      objMinOtherGets = document.getElementById('min_othergets');
      objMaxOtherGets = document.getElementById('max_othergets');

      /* build game */
      addSlider(slideName, 400, 430, sliderElement);
      dispGame();
      createMsg();

      svoSlider.showMessage(msgStartGame);
      /* a nice welcome message */
      clickMe(document.getElementById("infomessage"), false);
      //      EventUtil.addHandler(document, "keyup", keyPressed);

      flushThis('infomessage');
      cleanDisplay();
   };
}());

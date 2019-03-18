window.ca_usask_chdp_ExpServerCore_View_jsExtensions_Photohover = function () {
   "use strict";

   var element = this.getElement(),
      initialized = false,
      i,
      imgs = [],
      imgList = ['images/car/base_colourimage.png',
         'images/car/base_reducedimage.png',
         'images/car/drive_green.png',
         'images/car/drive_red.png',
         'images/car/drive_yellow.png',
         'images/car/engine_green.png',
         'images/car/engine_red.png',
         'images/car/engine_yellow.png',
         'images/car/intake_green.png',
         'images/car/intake_red.png',
         'images/car/intake_yellow.png',
         'images/car/susp_green.png',
         'images/car/susp_red.png',
         'images/car/susp_yellow.png',
         'images/car/tires_green.png',
         'images/car/tires_red.png',
         'images/car/tires_yellow.png',
         'images/car/wings_green.png',
         'images/car/wings_red.png',
         'images/car/wings_yellow.png'];

   $('#underlay').attr('src', this.getState().origSrc);

   for (i = 0; i < imgList.length; i += 1) {
      imgs[i] = new Image();
      imgs[i].src = qualifyURL('/VAADIN/themes/expserver/' +  imgList[i]);
   }

   this.onStateChange = function () {
      if (!initialized) {
         var part1Src = qualifyURL('/VAADIN/themes/expserver/' +  this.getState().part1Src),
            part2Src = qualifyURL('/VAADIN/themes/expserver/' +  this.getState().part2Src),
            part3Src = qualifyURL('/VAADIN/themes/expserver/' +  this.getState().part3Src),
            origSrc = qualifyURL('/VAADIN/themes/expserver/' +  this.getState().origSrc),
            underlaySrc = qualifyURL('/VAADIN/themes/expserver/' +  this.getState().underlaySrc);

         $('#underlay').attr('src', origSrc);
         initialized = true;

         $('#dPartInfo1').hover(function () {
               $('#underlay').attr('src', underlaySrc);
               $('#overlay').attr('src', part1Src);
            },
            function () {
               $('#underlay').attr('src', origSrc);
               $('#overlay').attr('src', "");
            });
         $('#dPartInfo2').hover(function () {
               $('#underlay').attr('src', underlaySrc);
               $('#overlay').attr('src', part2Src);
            },
            function () {
               $('#underlay').attr('src', origSrc);
               $('#overlay').attr('src', "");
            });
         $('#dPartInfo3').hover(function () {
               $('#underlay').attr('src', underlaySrc);
               $('#overlay').attr('src', part3Src);
            },
            function () {
               $('#underlay').attr('src', origSrc);
               $('#overlay').attr('src', "");
            });
      }
   };
};
//$(document).on('hover', '#dPart1Info1', function (evt) {
//    "use strict";
//    if (evt.type === "mouseenter") {
//
//        $('#damageImg').attr('src', part1Photo.src);
//    } else if (evt.type === "mouseleave") {
//        $('#damageImg').attr('src', origPhoto.src);
//    }
//});
//
//$(document).on('hover', '#dPart2Info2', function (evt) {
//    "use strict";
//    if (evt.type === "mouseenter") {
//        $('#damageImg').attr('src', part2Photo.src);
//    } else if (evt.type === "mouseleave") {
//        $('#damageImg').attr('src', origPhoto.src);
//
//    }
//});
//
//$(document).on('hover', '#dPart3Info3', function (evt) {
//    "use strict";
//    if (evt.type === "mouseenter") {
//        $('#damageImg').attr('src', part3Photo.src);
//    } else if (evt.type === "mouseleave") {
//        $('#damageImg').attr('src', origPhoto.src);
//
//    }
//});



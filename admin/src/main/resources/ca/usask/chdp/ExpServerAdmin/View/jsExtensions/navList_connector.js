var ca_usask_chdp_ExpServerAdmin_View_jsExtensions_NavList = function () {
   "use strict";
   var initialized = false;

   function setupScrollspy($) {
      /* =============================================================
       * bootstrap-scrollspy.js v2.2.1
       * http://twitter.github.com/bootstrap/javascript.html#scrollspy
       * =============================================================
       * Copyright 2012 Twitter, Inc.
       *
       * Licensed under the Apache License, Version 2.0 (the "License");
       * you may not use this file except in compliance with the License.
       * You may obtain a copy of the License at
       *
       * http://www.apache.org/licenses/LICENSE-2.0
       *
       * Unless required by applicable law or agreed to in writing, software
       * distributed under the License is distributed on an "AS IS" BASIS,
       * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       * See the License for the specific language governing permissions and
       * limitations under the License.
       * ============================================================== */


      "use strict"; // jshint ;_;


      /* SCROLLSPY CLASS DEFINITION
       * ========================== */

      function ScrollSpy(element, options) {
         var process = $.proxy(this.process, this)
            , $element = $(element).is('body') ? $(window) : $(element)
            , href;

         this.options = $.extend({}, $.fn.scrollspy.defaults, options);
         this.$scrollElement = $element.on('scroll.scroll-spy.data-api', process);
         this.$scrollContainer = options.targetContainer || null;
         console.log("set this.$scrollContainer.");
         this.selector = (this.options.target
            || ((href = $(element).attr('href')) && href.replace(/.*(?=#[^\s]+$)/, '')) //strip for ie7
            || '') + ' .nav li > a'
         this.$body = $('body')
         this.refresh()
         this.process()
      }

      ScrollSpy.prototype = {

         constructor: ScrollSpy, refresh: function () {
            var self = this
               , $targets

            this.offsets = $([])
            this.targets = $([])

            $targets = this.$body
               .find(this.selector)
               .map(function () {
                  var $el = $(this)
                     , href = $el.data('target') || $el.attr('href')
                     , $href = /^#\w/.test(href) && $(href)
                  return ( $href
                     && $href.length
                     && [
                     [ $href.position().top, href ]
                  ] ) || null
               })
               .sort(function (a, b) {
                  return a[0] - b[0]
               })
               .each(function () {
                  self.offsets.push(this[0])
                  self.targets.push(this[1])
               })
         }, process: function () {
            var scrollTop = this.$scrollElement.scrollTop() + this.options.offset
               , scrollHeight = this.$scrollContainer.height() || this.$scrollElement[0].scrollHeight || this.$body[0].scrollHeight
               , maxScroll = scrollHeight - this.$scrollElement.height()
               , offsets = this.offsets
               , targets = this.targets
               , activeTarget = this.activeTarget
               , i;

//               console.log('scrollTop: ' + scrollTop + ' scrollHeight: ' + scrollHeight + ' maxScroll: '
//                  + maxScroll + ' offsets: ' + offsets + ' targets: ' + targets[0] + targets[1] + targets[2] +
//                  targets[3] + 'activeTarget: ' + activeTarget);

            if (scrollTop >= maxScroll) {
               return activeTarget != (i = targets.last()[0])
                  && this.activate(i)
            }

            for (i = offsets.length; i--;) {
               activeTarget != targets[i]
                  && scrollTop >= offsets[i]
                  && (!offsets[i + 1] || scrollTop <= offsets[i + 1])
               && this.activate(targets[i])
            }
         }, activate: function (target) {
            var active
               , selector

            this.activeTarget = target

            $(this.selector)
               .parent('.active')
               .removeClass('active')

            selector = this.selector
               + '[data-target="' + target + '"],'
               + this.selector + '[href="' + target + '"]'

            active = $(selector)
               .parent('li')
               .addClass('active')

            if (active.parent('.dropdown-menu').length) {
               active = active.closest('li.dropdown').addClass('active')
            }

            active.trigger('activate')
         }

      }


      /* SCROLLSPY PLUGIN DEFINITION
       * =========================== */

      $.fn.scrollspy = function (option, moreOptions) {
         return this.each(function () {
            var $this = $(this)
               , data = $this.data('scrollspy')
               , options = typeof option == 'object' && option;
            if (!options) {
               options = moreOptions;
            }
            if (!data) $this.data('scrollspy', (data = new ScrollSpy(this, options)))
            if (typeof option == 'string') data[option]()
         })
      };

      $.fn.scrollspy.Constructor = ScrollSpy;

      $.fn.scrollspy.defaults = {
         offset: 10
      };


      /* SCROLLSPY DATA-API
       * ================== */
      // Normally this is called on window load, but here it is called when we call this function.

      /*       (function () {
       $('[data-spy="scroll"]').each(function () {
       var $spy = $(this);
       $spy.scrollspy($spy.data())
       })
       })();
       */
   }
   this.onStateChange = function () {

//
//      function refresh() {
//         $('[data-spy="scroll"]').each(function () {
//            var $spy = $(this).scrollspy('refresh')
//         });
//      }

      if (!initialized) {
         initialized = true;
         console.log("scrollSpy initialize has been called.");

         setupScrollspy(window.jQuery);
         // side bar
         $('.sidenav').affix({
            offset: {
               top: 170, bottom: 270
            }
         });
         $(window).scrollspy({offset: 70, targetContainer: $('#' + this.getState().contentContainerId)});
      } else {
         console.log("scrollSpy onStateChange has been called.");
         $(window).scrollspy({offset: 70, targetContainer: $('#' + this.getState().contentContainerId)});
      }



         //$('#scrollcontent').scrollspy();
//      } else {
//         // reset scrollspy, maybe for new content.
//         setupScrollspy(window.jQuery);
//
//         $(window).scrollspy.refresh();
//         $(window).scrollspy.process();
//      }


   };
};


// webkitdragdrop.js v1.0, Mon May 15 2010
//
// Copyright (c) 2010 Tommaso Buvoli (http://www.tommasobuvoli.com)
// No Extra Libraries are required, simply download this file, add it to your pages!
//
// To See this library in action, grab an ipad and head over to http://www.gotproject.com
// webkitdragdrop is freely distributable under the terms of an MIT-style license.


//Description
// Because this library was designed to run without requiring any other libraries, several basic helper functions were implemented
// 6 helper functons in this webkit_tools class have been taked directly from Prototype 1.6.1 (http://prototypejs.org/) (c) 2005-2009 Sam Stephenson


window.webkit_tools =
{
   //$ function - simply a more robust getElementById

   $: function (e) {
      if (typeof(e) == 'string') {
         return document.getElementById(e);
      }
      return e;
   },

   //extend function - copies the values of b into a (Shallow copy)

   extend: function (a, b) {
      for (var key in b) {
         a[key] = b[key];
      }
      return a;
   },

   //empty function - used as defaut for events

   empty: function () {

   },

   //remove null values from an array

   compact: function (a) {
      var b = []
      var l = a.length;
      for (var i = 0; i < l; i++) {
         if (a[i] !== null) {
            b.push(a[i]);
         }
      }
      return b;
   },

   //DESCRIPTION
   //	This function was taken from the internet (http://robertnyman.com/2006/04/24/get-the-rendered-style-of-an-element/) and returns
   //	the computed style of an element independantly from the browser
   //INPUT
   //	oELM (DOM ELEMENT) element whose style should be extracted
   //	strCssRule element

   getCalculatedStyle: function (oElm, strCssRule) {
      var strValue = "";
      if (document.defaultView && document.defaultView.getComputedStyle) {
         strValue = document.defaultView.getComputedStyle(oElm, "").getPropertyValue(strCssRule);
      }
      else if (oElm.currentStyle) {
         strCssRule = strCssRule.replace(/\-(\w)/g, function (strMatch, p1) {
            return p1.toUpperCase();
         });
         strValue = oElm.currentStyle[strCssRule];
      }
      return strValue;
   },

   //bindAsEventListener function - used to bind events

   bindAsEventListener: function (f, object) {
      var __method = f;
      return function (event) {
         __method.call(object, event || window.event);
      };
   },

   //cumulative offset - courtesy of Prototype (http://www.prototypejs.org)

   cumulativeOffset: function (element) {
      var valueT = 0, valueL = 0;
      do {
         valueT += element.offsetTop || 0;
         valueL += element.offsetLeft || 0;
         if (element.offsetParent == document.body)
            if (element.style.position == 'absolute') break;

         element = element.offsetParent;
      } while (element);

      return {left: valueL, top: valueT};
   },

   //getDimensions - courtesy of Prototype (http://www.prototypejs.org)

   getDimensions: function (element) {
      var display = element.style.display;
      if (display != 'none' && display != null) // Safari bug
         return {width: element.offsetWidth, height: element.offsetHeight};

      var els = element.style;
      var originalVisibility = els.visibility;
      var originalPosition = els.position;
      var originalDisplay = els.display;
      els.visibility = 'hidden';
      if (originalPosition != 'fixed') // Switching fixed to absolute causes issues in Safari
         els.position = 'absolute';
      els.display = 'block';
      var originalWidth = element.clientWidth;
      var originalHeight = element.clientHeight;
      els.display = originalDisplay;
      els.position = originalPosition;
      els.visibility = originalVisibility;
      return {width: originalWidth, height: originalHeight};
   },

   //hasClassName - courtesy of Prototype (http://www.prototypejs.org)

   hasClassName: function (element, className) {
      var elementClassName = element.className;
      return (elementClassName.length > 0 && (elementClassName == className ||
         new RegExp("(^|\\s)" + className + "(\\s|$)").test(elementClassName)));
   },

   //addClassName - courtesy of Prototype (http://www.prototypejs.org)

   addClassName: function (element, className) {
      if (!this.hasClassName(element, className))
         element.className += (element.className ? ' ' : '') + className;
      return element;
   },

   //removeClassName - courtesy of Prototype (http://www.prototypejs.org)

   removeClassName: function (element, className) {
      element.className =
         this.strip(element.className.replace(new RegExp("(^|\\s+)" + className + "(\\s+|$)"), ' '));
      return element;
   },

   //strip - courtesy of Prototype (http://www.prototypejs.org)

   strip: function (s) {
      return s.replace(/^\s+/, '').replace(/\s+$/, '');
   }

}

//Description
// Droppable fire events when a draggable is dropped on them

window.webkit_droppables = function () {
   this.initialize = function () {
      this.droppables = [];
      this.droppableRegions = [];
   }

   this.add = function (root, instance_props) {
      root = webkit_tools.$(root);
      var default_props = {accept: [], hoverClass: null, onDrop: webkit_tools.empty, onOver: webkit_tools.empty, onOut: webkit_tools.empty};
      default_props = webkit_tools.extend(default_props, instance_props || {});
      this.droppables.push({r: root, p: default_props});
   }

   this.remove = function (root) {
      root = webkit_tools.$(root);
      var d = this.droppables;
      var i = d.length;
      while (i--) {
         if (d[i].r == root) {
            d[i] = null;
            this.droppables = webkit_tools.compact(d);
            return true;
         }
      }
      return false;
   }

   //calculate position and size of all droppables

   this.prepare = function () {
      var d = this.droppables;
      var i = d.length;
      var dR = [];
      var r = null;

      while (i--) {
         r = d[i].r;
         if (r.style.display != 'none') {
            dR.push({i: i, size: webkit_tools.getDimensions(r), offset: webkit_tools.cumulativeOffset(r)})
         }
      }

      this.droppableRegions = dR;
   }

   this.finalize = function (x, y, r, e) {
      var indices = this.isOver(x, y);
      var index = this.maxZIndex(indices);
      var over = this.process(index, r);
      if (over) {
         this.drop(index, r, e);
      }
      this.process(-1, r);
      return over;
   }

   this.check = function (x, y, r) {
      var indices = this.isOver(x, y);
      var index = this.maxZIndex(indices);
      return this.process(index, r);
   }

   this.isOver = function (x, y) {
      var dR = this.droppableRegions;
      var i = dR.length;
      var active = [];
      var r = 0;
      var maxX = 0;
      var minX = 0;
      var maxY = 0;
      var minY = 0;

      while (i--) {
         r = dR[i];

         minY = r.offset.top;
         maxY = minY + r.size.height;

         if ((y > minY) && (y < maxY)) {
            minX = r.offset.left;
            maxX = minX + r.size.width;

            if ((x > minX) && (x < maxX)) {
               active.push(r.i);
            }
         }
      }

      return active;
   }

   this.maxZIndex = function (indices) {
      var d = this.droppables;
      var l = indices.length;
      var index = -1;

      var maxZ = -100000000;
      var curZ = 0;

      while (l--) {
         curZ = parseInt(d[indices[l]].r.style.zIndex || 0);
         if (curZ > maxZ) {
            maxZ = curZ;
            index = indices[l];
         }
      }

      return index;
   }

   this.process = function (index, draggableRoot) {
      //only perform update if a change has occured
      if (this.lastIndex != index) {
         //remove previous
         if (this.lastIndex != null) {
            var d = this.droppables[this.lastIndex]
            var p = d.p;
            var r = d.r;

            if (p.hoverClass) {
               webkit_tools.removeClassName(r, p.hoverClass);
            }
            p.onOut();
            this.lastIndex = null;
            this.lastOutput = false;
         }

         //add new
         if (index != -1) {
            var d = this.droppables[index]
            var p = d.p;
            var r = d.r;

            if (this.hasClassNames(draggableRoot, p.accept)) {
               if (p.hoverClass) {
                  webkit_tools.addClassName(r, p.hoverClass);
               }
               p.onOver();
               this.lastIndex = index;
               this.lastOutput = true;
            }
         }
      }
      return this.lastOutput;
   }

   this.drop = function (index, r, e) {
      if (index != -1) {
         this.droppables[index].p.onDrop(r, e);
      }
   }

   this.hasClassNames = function (r, names) {
      var l = names.length;
      if (l == 0) {
         return true
      }
      while (l--) {
         if (webkit_tools.hasClassName(r, names[l])) {
            return true;
         }
      }
      return false;
   }

   this.initialize();
}

window.webkit_drop = new webkit_droppables();

//Description
//webkit draggable - allows users to drag elements with their hands

window.webkit_draggable = function (r, slide, ip) {
   this.initialize = function (root, instance_props) {
      this.root = webkit_tools.$(root);
      var default_props = {scroll: false, revert: false, handle: this.root, zIndex: 1000, onStart: webkit_tools.empty, onEnd: webkit_tools.empty};

      this.p = webkit_tools.extend(default_props, instance_props || {});
      default_props.handle = webkit_tools.$(default_props.handle);
      this.prepare();
      this.bindEvents();
   }

   this.prepare = function () {
      var rs = this.root.style;

      //set position
      if (webkit_tools.getCalculatedStyle(this.root, 'position') != 'absolute') {
         rs.position = 'relative';
      }

      //set top, right, bottom, left
      rs.top = rs.top || '0px';
      rs.left = rs.left || '0px';
      rs.right = "";
      rs.bottom = "";

      //set zindex;
      rs.zIndex = rs.zIndex || '2';
      if (slide == "true") {
         rs.zIndex = '1';
      }
   }

   this.bindEvents = function () {
      var handle = this.p.handle;

      this.ts = webkit_tools.bindAsEventListener(this.touchStart, this);
      this.tm = webkit_tools.bindAsEventListener(this.touchMove, this);
      this.te = webkit_tools.bindAsEventListener(this.touchEnd, this);

      handle.addEventListener("touchstart", this.ts, false);
      handle.addEventListener("touchmove", this.tm, false);
      handle.addEventListener("touchend", this.te, false);
   }

   this.destroy = function () {
      var handle = this.p.handle;

      handle.removeEventListener("touchstart", this.ts);
      handle.removeEventListener("touchmove", this.tm);
      handle.removeEventListener("touchend", this.te);
   }

   this.set = function (key, value) {
      this.p[key] = value;
   }

   this.touchStart = function (event) {
      //prepare needed variables
      var p = this.p;
      var r = this.root;
      var rs = r.style;
      var t = event.targetTouches[0];

      //get position of touch
      touchX = t.pageX;
      touchY = t.pageY;

      //set base values for position of root
      rs.top = this.root.style.top || '0px';
      rs.left = this.root.style.left || '0px';
      rs.bottom = null;
      rs.right = null;

      var rootP = webkit_tools.cumulativeOffset(r);
      var cp = this.getPosition();

      //save event properties
      p.rx = cp.x;
      p.ry = cp.y;
      p.tx = touchX;
      p.ty = touchY;
      p.z = parseInt(this.root.style.zIndex);

      //boost zIndex
      rs.zIndex = p.zIndex;
      webkit_drop.prepare();
      p.onStart();
   }

   slideID = document.getElementById('slider');
   this.touchMove = function (event) {
      event.preventDefault();

      //prepare needed variables
      var p = this.p;
      var r = this.root;
      var rs = r.style;
      var t = event.targetTouches[0];
      if (t == null) {
         return
      }

      var curX = t.pageX;
      var curY = t.pageY;

      var delX = curX - p.tx;
      var delY = curY - p.ty;

      /* modification to update the slider value */
      if (slide == "true") {
         if ((p.rx + delX) > 900) {
            rs.left = 900 + 'px';
         } else if ((p.rx + delX) < 400) {
            rs.left = 400 + 'px';
         } else {
            rs.left = p.rx + delX + 'px';
         }
         updateScale(slideID, rs.left.replace('px', ''), 400, 0);
      } else {
         rs.top = p.ry + delY + 'px';
         rs.left = p.rx + delX + 'px';
      }

      //scroll window
      if (p.scroll) {
         s = this.getScroll(curX, curY);
         if ((s[0] != 0) || (s[1] != 0)) {
            window.scrollTo(window.scrollX + s[0], window.scrollY + s[1]);
         }
      }

      //check droppables
      webkit_drop.check(curX, curY, r);

      //save position for touchEnd
      this.lastCurX = curX;
      this.lastCurY = curY;
   }

   this.touchEnd = function (event) {
      var r = this.root;
      var p = this.p;
      var dropped = webkit_drop.finalize(this.lastCurX, this.lastCurY, r, event);

      if (((p.revert) && (!dropped)) || (p.revert === 'always')) {
         //revert root
         var rs = r.style;
         rs.top = (p.ry + 'px');
         rs.left = (p.rx + 'px');
      }

      r.style.zIndex = this.p.z;
      this.p.onEnd();
   }

   this.getPosition = function () {
      var rs = this.root.style;
      return {x: parseInt(rs.left || 0), y: parseInt(rs.top || 0)}
   }

   this.getScroll = function (pX, pY) {
      //read window variables
      var sX = window.scrollX;
      var sY = window.scrollY;

      var wX = window.innerWidth;
      var wY = window.innerHeight;

      //set contants
      var scroll_amount = 10; //how many pixels to scroll
      var scroll_sensitivity = 100; //how many pixels from border to start scrolling from.

      var delX = 0;
      var delY = 0;

      //process vertical y scroll
      if (pY - sY < scroll_sensitivity) {
         delY = -scroll_amount;
      }
      else if ((sY + wY) - pY < scroll_sensitivity) {
         delY = scroll_amount;
      }

      //process horizontal x scroll
      if (pX - sX < scroll_sensitivity) {
         delX = -scroll_amount;
      }
      else if ((sX + wX) - pX < scroll_sensitivity) {
         delX = scroll_amount;
      }

      return [delX, delY]
   }

   //contructor
   this.initialize(r, ip);
};

//Description
//webkit_click class. manages click events for draggables

window.webkit_click = function (r, ip) {
   this.initialize = function (root, instance_props) {
      var default_props = {onClick: webkit_tools.empty};

      this.root = webkit_tools.$(root);
      this.p = webkit_tools.extend(default_props, instance_props || {});
      this.bindEvents();
   }

   this.bindEvents = function () {
      var root = this.root;

      //bind events to local scope
      this.ts = webkit_tools.bindAsEventListener(this.touchStart, this);
      this.tm = webkit_tools.bindAsEventListener(this.touchMove, this);
      this.te = webkit_tools.bindAsEventListener(this.touchEnd, this);

      //add Listeners
      root.addEventListener("touchstart", this.ts, false);
      root.addEventListener("touchmove", this.tm, false);
      root.addEventListener("touchend", this.te, false);

      this.bound = true;
   }

   this.touchStart = function () {
      this.moved = false;
      if (this.bound == false) {
         this.root.addEventListener("touchmove", this.tm, false);
         this.bound = true;
      }
   }

   this.touchMove = function () {
      this.moved = true;
      this.root.removeEventListener("touchmove", this.tm);
      this.bound = false;
   }

   this.touchEnd = function () {
      if (this.moved == false) {
         this.p.onClick();
      }
   }

   this.setEvent = function (f) {
      if (typeof(f) == 'function') {
         this.p.onClick = f;
      }
   }

   this.unbind = function () {
      var root = this.root;
      root.removeEventListener("touchstart", this.ts);
      root.removeEventListener("touchmove", this.tm);
      root.removeEventListener("touchend", this.te);
   }

   //call constructor
   this.initialize(r, ip);
};

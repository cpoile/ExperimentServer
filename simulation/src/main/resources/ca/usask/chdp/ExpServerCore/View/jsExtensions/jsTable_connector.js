window.ca_usask_chdp_ExpServerCore_View_jsExtensions_JSTable = function () {
   "use strict";

   var element = this.getElement(),
      elementObj;
   element.id = this.getState().elementId;
   elementObj = $('#' + this.getState().elementId);

   elementObj.html(this.getState().rawHtml);

   this.onStateChange = function () {

   };
};

window.ca_usask_chdp_ExpServerCore_View_jsExtensions_BootstrapTooltip = function () {
   "use strict";

   var tooltipClassName = this.getState().tooltipClass,
      tooltipClass = '.' + tooltipClassName,
      targetClass = this.getState().targetClass,
      containerClass = this.getState().containerClass,
      initialized = false;

   this.onStateChange = function () {
      if ($(tooltipClass).length === 0) {
         initialized = true;
         var htmlWrap = '<a href="#" class="' + tooltipClassName + '" data-trigger="manual" data-original-title="0" data-placement="left"></a>';
         $(targetClass).wrap(htmlWrap);
         $(containerClass + " .tooltip .tooltip-inner").text(this.getState().value);
      } else {
         $(tooltipClass).tooltip('show');
         $(containerClass + " .tooltip .tooltip-inner").text(this.getState().value);
      }
   };
};


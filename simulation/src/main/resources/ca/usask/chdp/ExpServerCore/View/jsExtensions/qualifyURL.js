window.qualifyURL = function (url) {
   var trimmedUrl, newLoc;
   if (url.charAt(0) === "/") {
      trimmedUrl = url.substr(1);
   } else {
      trimmedUrl = url;
   }
   if (location.pathname.charAt(location.pathname.length-1) !== "/") {
      newLoc = location.pathname + '/';
   } else {
      newLoc = location.pathname;
   }
   var newUrl = newLoc + trimmedUrl;
   var img = document.createElement('img');
   img.src = newUrl; // set string url
   newUrl = img.src; // get qualified/absolute url
   img.src = null; // no server request
   return newUrl;
};
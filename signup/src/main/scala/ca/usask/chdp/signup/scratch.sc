def isNsidValid(nsid: String): Boolean = {
  (nsid.length == 6 &&
    nsid.substring(0,3).forall(p => p.isLetter) &&
    nsid.substring(3).forall(p => p.isDigit))
}

isNsidValid("ars123")
isNsidValid("ars1233")
isNsidValid("arss23")
isNsidValid("ar2323")

"ars123".length
"ars123".substring(0,3)
"ars123".substring(0,3).forall(_.isLetter)
"ars123".substring(3)
"ars123".substring(3).forall(_.isDigit)



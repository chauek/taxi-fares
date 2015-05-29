TAXI FARES
==========

## Assumption/decisions

* Assumed the mobile app will report position every ~5 seconds.
* Decided to count distance basing on added points only - no analysis of maps or roads
* Assumed 20p fare should be charged every time a charge distance or a charge time starts. Even when only part of it is used 
  and than tariff is changed.
* Decided: when minimal fare is reached but it comes back into minimal fare because of tariff change we do not get back 
  to minimal fare but keep counting medium fare (please check test cases FareSpec: T15 and T16)

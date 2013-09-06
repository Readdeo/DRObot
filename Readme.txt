Changelog
0.1
- külön service az adatgyûjtéshez
	- GPS adatok olvasása
	- ezek kml fájlba mentése, vonal ábrázolással

0.2
- Adatok kml-be mentése külön vonalhoz(LineLog.kml) és Placemarkokhoz(PMLog.kml)
- Adatfájlok áthelyezve sd/Robot mappába
- Upload gomb az activity-n feltölt 2 fájlt (/Robot/LineLog.kml, /Robot/PMLog.kml)

0.3
- App.log fájlba logolja az eseményeket
- GPS adatgyûjtési idõköze config fájlban megadható

0.4
- Server került bele, ami a 6000-es porton fogad változót
- akku szint kijelzése

0.5
- a Server a GPS service-t ki-be kapcsolja
- Server service elmenti fájlba az ip címet és feltölti egy tárhelyre

0.6
- bugfix: mivel a service implementálta a Locationlistenert, nem mûködött a kód a loclistener leiratkozásához, így a GPS-t nem lehetett kikapcsolni

0.7
- a GPS adatokat küldi a 0.2-es kliensnek

0.8
-bugfix: Android 4.1.2 (jelly Bean)-re áttérés miatt a PHP fájlfeltöltés nem mûködött, át kellett írni mindet ftp-re egy AsyncTask-ba
-bugfix: saját IP cím beolvasást szintén javítani kellett
- Apache commons library hozzáadva az ftp kapcsolathoz
- a kliens program ip címét mostantól ftp-n letölti majd beolvassa az IP2.txt fájlból

0.9
- A logolás át lett írva külön method-ba, hogy egy sorral meghívható legyen
Changelog
0.1
- k�l�n service az adatgy�jt�shez
	- GPS adatok olvas�sa
	- ezek kml f�jlba ment�se, vonal �br�zol�ssal

0.2
- Adatok kml-be ment�se k�l�n vonalhoz(LineLog.kml) �s Placemarkokhoz(PMLog.kml)
- Adatf�jlok �thelyezve sd/Robot mapp�ba
- Upload gomb az activity-n felt�lt 2 f�jlt (/Robot/LineLog.kml, /Robot/PMLog.kml)

0.3
- App.log f�jlba logolja az esem�nyeket
- GPS adatgy�jt�si id�k�ze config f�jlban megadhat�

0.4
- Server ker�lt bele, ami a 6000-es porton fogad v�ltoz�t
- akku szint kijelz�se

0.5
- a Server a GPS service-t ki-be kapcsolja
- Server service elmenti f�jlba az ip c�met �s felt�lti egy t�rhelyre

0.6
- bugfix: mivel a service implement�lta a Locationlistenert, nem m�k�d�tt a k�d a loclistener leiratkoz�s�hoz, �gy a GPS-t nem lehetett kikapcsolni

0.7
- a GPS adatokat k�ldi a 0.2-es kliensnek

0.8
-bugfix: Android 4.1.2 (jelly Bean)-re �tt�r�s miatt a PHP f�jlfelt�lt�s nem m�k�d�tt, �t kellett �rni mindet ftp-re egy AsyncTask-ba
-bugfix: saj�t IP c�m beolvas�st szint�n jav�tani kellett
- Apache commons library hozz�adva az ftp kapcsolathoz
- a kliens program ip c�m�t mostant�l ftp-n let�lti majd beolvassa az IP2.txt f�jlb�l

0.9
- A logol�s �t lett �rva k�l�n method-ba, hogy egy sorral megh�vhat� legyen
# Iverksetting

Iverksetting av utbetalinger mot OS/UR prøver å ivareta en del egenskaper:

* Utbetalinger til en person på en sak skal bygge på autorisering fra en autentisert beslutter
* Utbetalinger som iverksettes skal være konsistente med gjeldende vedtak i fagsystemet

Hovedmekanismene som skal ivaretas det er:

* Den komplette utbetalingskjeden sendes hver gang.
* Første vedtak (rammevedtak) for sak og person må iverksettes med token til beslutter (0000-GA-DAGPENGER-BESLUTTER).
* Iverksettingen må være syntaktisk og semantisk gyldig (validering)
* Ny iverksetting må referere til forrige iverksetting og inneholde nok informasjon til å avgjøre at oppdateringen er konsistent.

* Følgende misbruk blir foreløpig ikke fullstendig forhindret (vi krever autentisert token fra kallende tjeneste, men vi kan selvfølgelig ikke forhindre at den applikasjonen opptrer som en proxy/gateway for en potensielt stor mengde andre tjenester):

* Iverksettinger som uautorisert utvider utbetalingskjeden tilbake i tid
* Iverksettinger som uautorisert endrer beløp på utbetalingsperioder. Det er satt et tak på utbetalingsbeløpet, slik at det ikke kan være helt urimelig.

Et eksempel på flyten er illustrert under:

1. Saksbehandlingssystemet sender rammevedtak til iverksetting med token til beslutter.
2. Den autoriserte iverksettingen danner starten på iverksettingskjeden.
3. Det automatiserte beregningssystemet sender iverksetting av utbetalingsperioder (utbetalingsvedtak),
4. Den nye iverksettingen sender med referanse til iverksettingen av rammevedtaket (forrige iverksetting), som valideres for konsistens
5. Nye iverksettinger (av utbetalingsvedtak) kan endre og utvide med nye perioder (men burde forhindres fra å utvide utbetalingskjeden til før virkningstidspunktet i rammevedtaket)
6. En autorisert (beslutter) kan endre rammene for utbetalingene, dvs iverksette et rammevedtak, men må fortsatt referere til forrige iverksetting. Det er også mulig å endre utbetalingene samtidig (men det er ikke gjort i eksemplet)

![Iverksetting av vedtak](https://github.com/navikt/dp-iverksett/blob/ff323591b8884cd264c469fd49f943aa550460be/doc/Iverksetting%20av%20vedttak%20med%20maksbel%C3%B8p.png)
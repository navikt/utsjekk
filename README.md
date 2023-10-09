# dp-iverksett
App som tilbyr orkestrering av iverksetting for dagpenger. Følgende oppgaver gjøres i sekvens: 
1. Lag og send utbetalingsoppdrag til OS/UR (via dp-oppdrag)
2. Poll etter iverksatt utbetalingsoppdrag (mot dp-oppdrag)

## Swagger
http://localhost:8094/swagger-ui/index.html

## Kjøring lokalt
Bygging gjøres ved å kjøre `mvn clean install`.

### Autentisering lokalt
Dersom man vil gjøre autentiserte kall mot andre tjenester eller vil kjøre applikasjonen sammen med frontend, må man 
sette opp følgende miljø-variabler:
* `AZURE_APP_CLIENT_ID` 
* `AZURE_APP_CLIENT_SECRET` 

Begge kan hentes fra aktuelt cluster med `./fetch-secrets.sh`

I IntelliJ legges de inn under ApplicationLocal -> Edit Configurations -> Environment Variables.

### Overtyre funksjonsbrytere
Funksjonsbrytere lokalt er i utgangspunktet PÅ (enabled=true) kan overstyres med miljøvariable. Det er nødvendig for
* `dp.iverksett.stopp-iverksetting' = false`    

### Kjøring med in-memory-database
For å kjøre opp appen lokalt, kan en kjøre `ApplicationLocal`.

Appen starter da opp med en in Postgres-database via docker og er da tilgjengelig under `localhost:8094`.
For å aksesseres databasen kan du sjekke loggen og bruker/passord i DbContainerInitializer

### GCP
GCP bruker secrets i stedet for vault.
Anbefaler å bruke [modify-secrets](https://github.com/rajatjindal/kubectl-modify-secret)

#### Database
[Nais doc - Postgres](https://doc.nais.io/persistence/postgres/)

[Nais doc - Responsibilities](https://doc.nais.io/persistence/responsibilities/)
* `brew tap tclass/cloud_sql_proxy`
* `brew install cloud_sql_proxy`

1h temp token
* `gcloud projects add-iam-policy-binding dp-iverksett --member=user:<FIRSTNAME>.<LASTNAME>@nav.no --role=roles/cloudsql.instanceUser --condition="expression=request.time < timestamp('$(date -v '+1H' -u +'%Y-%m-%dT%H:%M:%SZ')'),title=temp_access"`

### Bruk av postman
Du kan bruke Postman til å kalle APIene i dp-iverksett. Det krever at du har satt opp autentisering i app'en riktig.
`dp-iverksett` er konfigurert til å kunne kalle seg selv. Dermed kan man bruke token for app'en til å kalle den.

#### Finne token
Den nødvendige informasjonen for å få token'et får du slik:

1. Endre kontekst til [dev-gcp|prod-gcp] `kubectl config use-context [dev-gcp|prod-gcp]`
2. Finne `AZURE_APP_CLIENT_ID` og `AZURE_APP_CLIENT_SECRET` ved å kjøre `./fetch-secrets.sh`

I Postman gjør du et GET-kall med følgende oppsett:

* URL: `https://login.microsoftonline.com/navq.onmicrosoft.com/oauth2/v2.0/token`
* Body: `x-www-form-urlencoded` med følgende key-values
    * `grant_type`: `client_credentials`
    * `client_id`: <`AZURE_APP_CLIENT_ID`> fra kubectl-kallet over
    * `client_secret`: <`AZURE_APP_CLIENT_SECRET`> fra kubectl-kallet over
    * `scope`: `api://[dev-gcp|prod-gcp].teamdagpenger.dp-iverksett/.default`

#### Lagre token globalt i Postman

Et triks kan være å sette opp en "test" under *Tests* i request'en:

```
pm.test("Lagre token globalt", function () {
    var jsonData = pm.response.json();
    pm.globals.set("token-dp-iverksett", jsonData.access_token);
});
```

som vil plukke ut token'et og lagre det i en global variabel, her `token-dp-iverksett`

Når du lager kall mot APIet, så kan du i `Authorization`-fanen for kallet velge type `Bearer Token` og skrive inn 
`{{token-dp-iverksett}}` i `Token`-feltet for å bruke verdien fra den globale variabelen.

## Produksjonssetting

Applikasjonen vil deployes til produksjon ved ny commit på master-branchen. Det er dermed tilstrekkelig å merge PR for 
å trigge produksjonsbygget.

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles ved å opprette et issue her på Github.

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-dagpenger.

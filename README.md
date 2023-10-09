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
Du kan bruke Postman til å kalle APIene i `dp-iverksett`. Det krever at du har satt opp autentisering i app'en riktig.
`dp-iverksett` er konfigurert til å kunne kalle seg selv. Dermed kan man bruke token for app'en til å kalle den.

Vi har en ferdig samling og miljø i Postman du kan forke eller utforske ved å klikke på knappen under.

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/8598165-2be7d928-461e-4cb6-85e1-72c6271d7ae8?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D8598165-2be7d928-461e-4cb6-85e1-72c6271d7ae8%26entityType%3Dcollection%26workspaceId%3Db41539b8-ca43-4c80-9475-d7e6e6d85180#?env%5BIverksett%5D=W3sia2V5IjoiYXp1cmVBcHBDbGllbnRJZCIsInZhbHVlIjoiM2FjZGMyYTUtOTA2ZC00MjAwLWJmMGMtNmU3NzlhMTViMDQ5IiwiZW5hYmxlZCI6dHJ1ZSwidHlwZSI6ImRlZmF1bHQiLCJzZXNzaW9uVmFsdWUiOiIzYWNkYzJhNS05MDZkLTQyMDAtYmYwYy02ZTc3OWExNWIwNDkiLCJzZXNzaW9uSW5kZXgiOjB9LHsia2V5IjoiYXp1cmVBcHBDbGllbnRTZWNyZXQiLCJ2YWx1ZSI6IiIsImVuYWJsZWQiOnRydWUsInR5cGUiOiJkZWZhdWx0Iiwic2Vzc2lvblZhbHVlIjoieU4xOFF+RWVoLk5nVGtvUm9PaEJ5Ni5kMWFQLnhwNUdxRGNNUWNXTyIsInNlc3Npb25JbmRleCI6MX0seyJrZXkiOiJiZWFyZXJUb2tlbiIsInZhbHVlIjoiIiwiZW5hYmxlZCI6dHJ1ZSwidHlwZSI6ImRlZmF1bHQiLCJzZXNzaW9uVmFsdWUiOiJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpTVXpJMU5pSXNJbXRwWkNJNklpMUxTVE5ST1c1T1VqZGlVbTltZUcxbFdtOVljV0pJV2tkbGR5SjkuZXlKaGRXUWlPaUl6WVdOa1l6SmhOUzA1TURaa0xUUXlNREF0WW1Zd1l5MDJaVGMzT1dFeC4uLiIsInNlc3Npb25JbmRleCI6Mn0seyJrZXkiOiJob3N0IiwidmFsdWUiOiJodHRwOi8vbG9jYWxob3N0OjgwOTQiLCJlbmFibGVkIjp0cnVlLCJ0eXBlIjoiZGVmYXVsdCIsInNlc3Npb25WYWx1ZSI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA5NCIsInNlc3Npb25JbmRleCI6M31d)

For å foreta kall mot `dp-iverksett` må du gjøre følgende:

1. Endre kontekst til [dev-gcp|prod-gcp] `kubectl config use-context [dev-gcp|prod-gcp]`
3. Pass på at aktivt miljø for samlingen i Postman er satt til `Iverksett`
4. Oppdatere miljøvariablene `azureAppClientSecret` og `azureAppClientId` med verdiene du får ved å kjøre `./fetch-secrets.sh`
5. Kjøre requesten som heter `Fetch token`. Denne henter et token og oppdaterer verdien til miljøvariabelen `bearerToken`

Nå er du klar til å foreta kall mot appen, og kan prøve å nå appen lokalt ved å kjøre requesten som heter `/api/iverksetting`

## Produksjonssetting

Applikasjonen vil deployes til produksjon ved ny commit på main. Det er dermed tilstrekkelig å merge PR for 
å trigge produksjonsbygget.

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles ved å opprette et issue her på Github.

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-dagpenger-dev.

# language: no
# encoding: UTF-8

Egenskap: Korrigere beløp tilbake i tid

  Scenario: Beløp endres tilbake i tid. Da skal vi ikke sende opphør, kun endret periode med nytt beløp som vil skrive
  over den eksisterende oppdragslinjen

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 02.06.2023 | 15.08.2023 | 800   |
      | 2            | 02.06.2023 | 15.07.2023 | 800   |
      | 2            | 16.07.2023 | 15.08.2023 | 400   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag 2
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.06.2023 | 15.08.2023 |             | 800   | NY           | Nei        | 0          |                    |
      | 2            | 16.07.2023 | 15.08.2023 |             | 400   | ENDR         | Nei        | 1          | 0                  |

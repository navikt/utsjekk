# language: no
# encoding: UTF-8

Egenskap: Engangssats, enkelt scenario

  Scenario: Har en periode, legger til en ny

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp | Satstype |
      | 1            | 01.02.2021 | 01.02.2021 | 700   | ENGANGS  |
      | 2            | 01.02.2021 | 01.02.2021 | 700   | ENGANGS  |
      | 2            | 01.04.2021 | 01.04.2021 | 900   | ENGANGS  |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Satstype |
      | 1            | 01.02.2021 | 01.02.2021 |             | 700   | NY           | Nei        | 0          |                    | ENGANGS  |
      | 2            | 01.04.2021 | 01.04.2021 |             | 900   | ENDR         | Nei        | 1          | 0                  | ENGANGS  |

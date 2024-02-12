# language: no
# encoding: UTF-8

Egenskap: Har en periode, legger til en ny

  Scenario: Har en periode, legger til en ny

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.02.2021 | 31.03.2021 | 700   |
      | 2            | 01.02.2021 | 31.03.2021 | 700   |
      | 2            | 01.04.2021 | 31.05.2021 | 900   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Satstype |
      | 1            | 01.02.2021 | 31.03.2021 |             | 700   | NY           | Nei        | 0          |                    | DAG      |
      | 2            | 01.04.2021 | 31.05.2021 |             | 900   | ENDR         | Nei        | 1          | 0                  | DAG      |

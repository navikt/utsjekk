# language: no
# encoding: UTF-8

Egenskap: Opphører vedtak, og deretter innvilget ny stønad

  Scenario: Opphører vedtak, og deretter innvilget ny stønad

    Gitt følgende tilkjente ytelser
      | BehandlingId | Uten andeler | Fra dato   | Til dato   | Beløp |
      | 1            |              | 01.03.2021 | 01.03.2021 | 800   |
      | 2            | Ja           | 01.03.2021 | 01.03.2021 |       |
      | 3            |              | 01.04.2021 | 01.04.2021 | 100   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag 2
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 800   | NY           | Nei        | 0          |                    |
      | 2            | 01.03.2021 | 01.03.2021 | 01.03.2021  | 800   | ENDR         | Ja         | 0          |                    |
      | 3            | 01.04.2021 | 01.04.2021 |             | 100   | ENDR         | Nei        | 1          | 0                  |


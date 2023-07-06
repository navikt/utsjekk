# language: no
# encoding: UTF-8

Egenskap: Har en periode, legger til en ny periode som er lik den forrige

  Scenario: Har en periode, legger til en ny periode som er lik den forrige

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.02.2021 | 31.03.2021 | 700   |
      | 2            | 01.02.2021 | 31.03.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag 2
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.02.2021 | 31.03.2021 |             | 700   | NY           | Nei        | 0          |                    |

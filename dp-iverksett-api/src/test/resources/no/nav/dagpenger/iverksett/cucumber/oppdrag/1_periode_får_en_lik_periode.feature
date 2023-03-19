# language: no
# encoding: UTF-8

Egenskap: Har en periode, legger til en ny periode som er lik den forrige

  Scenario: Har en periode, legger til en ny periode som er lik den forrige

    Gitt følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.02.2021 | 31.03.2021 | 700   |
      | 2            | 01.02.2021 | 31.03.2021 | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.02.2021 | 31.03.2021 |             | 700   | NY           | Nei        | 1          |                    |

    Så forvent følgende utbetalingsoppdrag uten utbetalingsperiode
      | BehandlingId | Kode endring | Er endring |
      | 2            | ENDR         | Ja         |


    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 01.02.2021
      | Fra dato   | Til dato   | Beløp | Periode id | Forrige periode id |
      | 01.02.2021 | 31.03.2021 | 700   | 1          |                    |
# language: no
# encoding: UTF-8

Egenskap: Ugyldig startdato

  Scenario: Startdato etter forrige andel sin startdato er ikke gyldig

    Gitt følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.02.2021 |01.02.2021 | 700   |
      | 2            |01.03.2021 |01.03.2021 | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception


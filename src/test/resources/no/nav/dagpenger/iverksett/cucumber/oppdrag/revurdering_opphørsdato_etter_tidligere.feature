# language: no
# encoding: UTF-8

Egenskap: Startdato etter tidligere startdato er ikke gyldig

  Scenario: Startdato etter tidligere startdato er ikke gyldig


    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            |01.02.2021  |
      | 3            |01.03.2021  |

    Og følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.03.2021 |01.03.2021 | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception

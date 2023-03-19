# language: no
# encoding: UTF-8

Egenskap: Opphører vedtak med 0-periode, og deretter innvilget ny stønad

  Scenario: Opphører vedtak med 0-periode, og deretter innvilget ny stønad


    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            |01.02.2021  |
      | 3            |01.02.2021  |

    Og følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.03.2021 |01.03.2021 | 800   |
      | 2            |01.03.2021 |01.03.2021 | 0     |
      | 3            |01.04.2021 |01.04.2021 | 100   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            |01.03.2021 |01.03.2021 |             | 800   | NY           | Nei        | 1          |                    |
      | 2            |01.03.2021 |01.03.2021 |01.02.2021    | 800   | ENDR         | Ja         | 1          |                    |
      | 3            |01.04.2021 |01.04.2021 |             | 100   | ENDR         | Nei        | 2          | 1                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 01.03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.03.2021 |01.03.2021 | 800   | 1          |                    | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |          |          | 0     | 1          |                    | 2                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.04.2021 |01.04.2021 | 100   | 2          | 1                  | 3                   |


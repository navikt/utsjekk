# language: no
# encoding: UTF-8

Egenskap: Opphør en tidligere periode, når startdato allerede finnes, men skal då sende startdato till oppdrag for den andelen som opphører

  Scenario: Opphør en tidligere periode, når startdato allerede finnes, men skal då sende startdato till oppdrag for den andelen som opphører

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            | 01.02.2021   |
      | 3            | 01.02.2021   |

    Og følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.03.2021 |01.03.2021 | 700   |
      | 2            |01.03.2021 |01.03.2021 | 800   |
      | 3            |01.04.2021 |01.04.2021 | 900   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            |01.03.2021 |01.03.2021 |             | 700   | NY           | Nei        | 1          |                    |
      | 2            |01.03.2021 |01.03.2021 |01.02.2021    | 700   | ENDR         | Ja         | 1          |                    |
      | 2            |01.03.2021 |01.03.2021 |             | 800   | ENDR         | Nei        | 2          | 1                  |
      | 3            |01.03.2021 |01.03.2021 |01.03.2021    | 800   | ENDR         | Ja         | 2          | 1                  |
      | 3            |01.04.2021 |01.04.2021 |             | 900   | ENDR         | Nei        | 3          | 2                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 01.03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.03.2021 |01.03.2021 | 700   | 1          |                    | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.03.2021 |01.03.2021 | 800   | 2          | 1                  | 2                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.04.2021 |01.04.2021 | 900   | 3          | 2                  | 3                   |


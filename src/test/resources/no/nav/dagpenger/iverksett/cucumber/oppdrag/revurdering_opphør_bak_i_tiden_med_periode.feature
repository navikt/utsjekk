# language: no
# encoding: UTF-8

Egenskap: Revurdering med opphør bak i tiden, samt ny periode frem i tiden


  Scenario: Revurdering med opphør bak i tiden, samt ny periode frem i tiden

    Gitt følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.02.2021 |01.02.2021 | 700   |
      | 1            |01.03.2021 |01.03.2021 | 800   |
      | 2            |01.01.2021 |01.01.2021 | 0     |
      | 2            |01.04.2021 |01.04.2021 | 800   |


    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            |01.02.2021 |01.02.2021 |             | 700   | NY           | Nei        | 1          |                    |
      | 1            |01.03.2021 |01.03.2021 |             | 800   | NY           | Nei        | 2          | 1                  |
      | 2            |01.03.2021 |01.03.2021 |01.01.2021    | 800   | ENDR         | Ja         | 2          | 1                  |
      | 2            |01.04.2021 |01.04.2021 |             | 800   | ENDR         | Nei        | 3          | 2                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.02.2021 |01.02.2021 | 700   | 1          |                    | 1                   |
      |01.03.2021 |01.03.2021 | 800   | 2          | 1                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.01.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.04.2021 |01.04.2021 | 800   | 3          | 2                  | 2                   |


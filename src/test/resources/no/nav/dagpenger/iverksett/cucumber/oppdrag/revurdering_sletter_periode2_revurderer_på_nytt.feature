# language: no
# encoding: UTF-8

Egenskap: Revurderer og sletter periode 2, revurdererer på nytt og skal da bygge videre fra andre perioden sin periodeId


  Scenario: Revurderer og sletter periode 2, revurdererer på nytt og skal da bygge videre fra andre perioden sin periodeId

    Gitt følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.03.2021 |01.03.2021 | 700   |
      | 1            |01.04.2021 |01.04.2021 | 800   |
      | 2            |01.03.2021 |01.03.2021 | 700   |
      | 3            |01.03.2021 |01.03.2021 | 700   |
      | 3            |01.04.2021 |01.04.2021 | 800   |


    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            |01.03.2021 |01.03.2021 |             | 700   | NY           | Nei        | 1          |                    |
      | 1            |01.04.2021 |01.04.2021 |             | 800   | NY           | Nei        | 2          | 1                  |
      | 2            |01.04.2021 |01.04.2021 |01.04.2021    | 800   | ENDR         | Ja         | 2          | 1                  |
      | 3            |01.04.2021 |01.04.2021 |             | 800   | ENDR         | Nei        | 3          | 2                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 01.03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.03.2021 |01.03.2021 | 700   | 1          |                    | 1                   |
      |01.04.2021 |01.04.2021 | 800   | 2          | 1                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.03.2021 |01.03.2021 | 700   | 1          |                    | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 01.03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.03.2021 |01.03.2021 | 700   | 1          |                    | 1                   |
      |01.04.2021 |01.04.2021 | 800   | 3          | 2                  | 3                   |
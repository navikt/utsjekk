# language: no
# encoding: UTF-8

Egenskap: Har en periode og får en endring midt i perioden

  Scenario: Har en periode og får en endring midt i perioden

    Gitt følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.02.2021 | 31.05.2021 | 700   |
      | 2            | 01.02.2021 | 31.03.2021 | 700   |
      | 2            | 01.04.2021 | 31.05.2021 | 900   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Type |
      | 1            | 01.02.2021 | 31.05.2021 |             | 700   | NY           | Nei        | 1          |                    | DAG  |
      | 2            | 01.02.2021 | 31.05.2021 |01.02.2021    | 700   | ENDR         | Ja         | 1          |                    | DAG  |
      | 2            | 01.02.2021 | 31.03.2021 |             | 700   | ENDR         | Nei        | 2          | 1                  | DAG  |
      | 2            | 01.04.2021 | 31.05.2021 |             | 900   | ENDR         | Nei        | 3          | 2                  | DAG  |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 01.02.2021
      | Fra dato   | Til dato   | Beløp | Periode id | Forrige periode id |
      | 01.02.2021 | 31.05.2021 | 700   | 1          |                    |

    Så forvent følgende tilkjente ytelser for behandling 2 med startdato 01.02.2021
      | Fra dato   | Til dato   | Beløp | Periode id | Forrige periode id |
      | 01.02.2021 | 31.03.2021 | 700   | 2          | 1                  |
      | 01.04.2021 | 31.05.2021 | 900   | 3          | 2                  |
# language: no
# encoding: UTF-8

Egenskap: Sekvens

  Scenario: Sekvens

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            |01.08.2021  |
      | 3            |01.08.2021  |
      | 4            |01.08.2021  |

    Og følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.08.2021 |01.11.2021 | 700   |
      | 1            |01.12.2021 |01.03.2022 | 900   |
      | 1            |01.04.2022 |01.06.2022 | 1100  |
      | 2            |01.09.2021 |01.11.2021 | 700   |
      | 2            |01.12.2021 |01.03.2022 | 900   |
      | 2            |01.04.2022 |01.05.2022 | 1100  |
      | 3            |01.09.2021 |01.11.2021 | 700   |
      | 3            |01.12.2021 |01.01.2022 | 900   |
      | 3            |01.02.2022 |01.04.2022 | 1200  |


    Og følgende tilkjente ytelser uten andel for Dagpenger
      | BehandlingId |
      | 4            |

    Og følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 5            |01.07.2021 |01.01.2022 | 900   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            |01.08.2021 |01.11.2021 |             | 700   | NY           | Nei        | 1          |                    |
      | 1            |01.12.2021 |01.03.2022 |             | 900   | NY           | Nei        | 2          | 1                  |
      | 1            |01.04.2022 |01.06.2022 |             | 1100  | NY           | Nei        | 3          | 2                  |
      | 2            |01.04.2022 |01.06.2022 |01.08.2021    | 1100  | ENDR         | Ja         | 3          | 2                  |
      | 2            |01.09.2021 |01.11.2021 |             | 700   | ENDR         | Nei        | 4          | 3                  |
      | 2            |01.12.2021 |01.03.2022 |             | 900   | ENDR         | Nei        | 5          | 4                  |
      | 2            |01.04.2022 |01.05.2022 |             | 1100  | ENDR         | Nei        | 6          | 5                  |
      | 3            |01.04.2022 |01.05.2022 |01.12.2021    | 1100  | ENDR         | Ja         | 6          | 5                  |
      | 3            |01.12.2021 |01.01.2022 |             | 900   | ENDR         | Nei        | 7          | 6                  |
      | 3            |01.02.2022 |01.04.2022 |             | 1200  | ENDR         | Nei        | 8          | 7                  |
      | 4            |01.02.2022 |01.04.2022 |01.09.2021    | 1200  | ENDR         | Ja         | 8          | 7                  |
      | 5            |01.02.2022 |01.04.2022 |01.07.2021    | 1200  | ENDR         | Ja         | 8          | 7                  |
      | 5            |01.07.2021 |01.01.2022 |             | 900   | ENDR         | Nei        | 9          | 8                  |


    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 01.08.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.08.2021 |01.11.2021 | 700   | 1          |                    | 1                   |
      |01.12.2021 |01.03.2022 | 900   | 2          | 1                  | 1                   |
      |01.04.2022 |01.06.2022 | 1100  | 3          | 2                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.08.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.09.2021 |01.11.2021 | 700   | 4          | 3                  | 2                   |
      |01.12.2021 |01.03.2022 | 900   | 5          | 4                  | 2                   |
      |01.04.2022 |01.05.2022 | 1100  | 6          | 5                  | 2                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 01.08.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.09.2021 |01.11.2021 | 700   | 4          | 3                  | 2                   |
      |01.12.2021 |01.01.2022 | 900   | 7          | 6                  | 3                   |
      |01.02.2022 |01.04.2022 | 1200  | 8          | 7                  | 3                   |

    Og forvent følgende tilkjente ytelser for behandling 4 med startdato 01.08.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |          |          | 0     | 8          | 7                  | 4                   |

    Og forvent følgende tilkjente ytelser for behandling 5 med startdato 01.07.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.07.2021 |01.01.2022 | 900   | 9          | 8                  | 5                   |
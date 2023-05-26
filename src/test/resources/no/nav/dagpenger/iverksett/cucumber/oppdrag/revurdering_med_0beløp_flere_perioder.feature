# language: no
# encoding: UTF-8

Egenskap: Revurdering med 0 beløp beholder periodeId når man har flere perioder


  Scenario: Revurdering med 0 beløp beholder periodeId når man har flere perioder

    Gitt følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.02.2021 |01.02.2021 | 700   |
      | 1            |01.03.2021 |01.03.2021 | 800   |
      | 2            |01.02.2021 |01.03.2021 | 0     |
      | 3            |01.02.2021 |01.02.2021 | 0     |
      | 4            |01.02.2021 |01.02.2021 | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            |01.02.2021 |01.02.2021 |             | 700   | NY           | Nei        | 1          |                    |
      | 1            |01.03.2021 |01.03.2021 |             | 800   | NY           | Nei        | 2          | 1                  |
      | 2            |01.03.2021 |01.03.2021 |01.02.2021    | 800   | ENDR         | Ja         | 2          | 1                  |
      | 4            |01.02.2021 |01.02.2021 |             | 700   | ENDR         | Nei        | 3          | 2                  |


    Og forvent følgende utbetalingsoppdrag uten utbetalingsperiode
      | BehandlingId | Kode endring | Er endring |
      | 3            | ENDR         | Ja         |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.02.2021 |01.02.2021 | 700   | 1          |                    | 1                   |
      |01.03.2021 |01.03.2021 | 800   | 2          | 1                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |          |          | 0     | 2          | 1                  | 2                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |          |          | 0     | 2          | 1                  | 3                   |

    Og forvent følgende tilkjente ytelser for behandling 4 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.02.2021 |01.02.2021 | 700   | 3          | 2                  | 4                   |


  Scenario: Revurdering med 0 beløp beholder periodeId når man har flere perioder, men også endring i tidligere beløp

    Gitt følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.02.2021 |01.02.2021 | 700   |
      | 1            |01.03.2021 |01.03.2021 | 800   |
      | 2            |01.02.2021 |01.02.2021 | 0     |
      | 2            |01.03.2021 |01.03.2021 | 800   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            |01.02.2021 |01.02.2021 |             | 700   | NY           | Nei        | 1          |                    |
      | 1            |01.03.2021 |01.03.2021 |             | 800   | NY           | Nei        | 2          | 1                  |
      | 2            |01.03.2021 |01.03.2021 |01.02.2021    | 800   | ENDR         | Ja         | 2          | 1                  |
      | 2            |01.03.2021 |01.03.2021 |             | 800   | ENDR         | Nei        | 3          | 2                  |


    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.02.2021 |01.02.2021 | 700   | 1          |                    | 1                   |
      |01.03.2021 |01.03.2021 | 800   | 2          | 1                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.03.2021 |01.03.2021 | 800   | 3          | 2                  | 2                   |

  Scenario: Revurdering med 0 beløp beholder periodeId når man har flere perioder, før tidligere periode

    Gitt følgende tilkjente ytelser for Dagpenger
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            |01.02.2021 |01.02.2021 | 700   |
      | 1            |01.03.2021 |01.03.2021 | 800   |
      | 2            |01.02.2021 |01.02.2021 | 0     |
      | 2            |01.03.2021 |01.03.2021 | 800   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            |01.02.2021 |01.02.2021 |             | 700   | NY           | Nei        | 1          |                    |
      | 1            |01.03.2021 |01.03.2021 |             | 800   | NY           | Nei        | 2          | 1                  |
      | 2            |01.03.2021 |01.03.2021 |01.02.2021    | 800   | ENDR         | Ja         | 2          | 1                  |
      | 2            |01.03.2021 |01.03.2021 |             | 800   | ENDR         | Nei        | 3          | 2                  |


    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.02.2021 |01.02.2021 | 700   | 1          |                    | 1                   |
      |01.03.2021 |01.03.2021 | 800   | 2          | 1                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |01.03.2021 |01.03.2021 | 800   | 3          | 2                  | 2                   |

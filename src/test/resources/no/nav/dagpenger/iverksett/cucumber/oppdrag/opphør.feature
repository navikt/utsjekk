# language: no
# encoding: UTF-8

Egenskap: Opphør

  Scenario: Opphør en periode

    Gitt følgende tilkjente ytelser
      | BehandlingId | Uten andeler | Fra dato   | Til dato   | Beløp |
      | 1            |              | 01.03.2021 | 01.03.2021 | 700   |
      | 2            | Ja           |            |            |       |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 01.03.2021 | 01.03.2021 | 01.03.2021  | 700   | ENDR         | Ja         | 0          |                    |

  Scenario: Opphør en periode med månedssats

    Gitt følgende tilkjente ytelser
      | BehandlingId | Uten andeler | Fra dato   | Til dato   | Beløp | Satstype |
      | 1            |              | 01.03.2021 | 31.03.2021 | 700   | MÅNEDLIG |
      | 2            | Ja           |            |            |       | MÅNEDLIG |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Satstype |
      | 1            | 01.03.2021 | 31.03.2021 |             | 700   | NY           | Nei        | 0          |                    | MÅNEDLIG |
      | 2            | 01.03.2021 | 31.03.2021 | 01.03.2021  | 700   | ENDR         | Ja         | 0          |                    | MÅNEDLIG |

  Scenario: Iverksetter på nytt etter opphør

    Gitt følgende tilkjente ytelser
      | BehandlingId | Uten andeler | Fra dato   | Til dato   | Beløp |
      | 1            |              | 01.03.2021 | 01.03.2021 | 700   |
      | 2            | Ja           |            |            |       |
      | 3            |              | 01.03.2021 | 01.03.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 01.03.2021 | 01.03.2021 | 01.03.2021  | 700   | ENDR         | Ja         | 0          |                    |
      | 3            | 01.03.2021 | 01.03.2021 |             | 700   | ENDR         | Nei        | 1          | 0                  |

  Scenario: Opphør en av 2 perioder

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.03.2021 | 700   |
      | 1            | 01.04.2021 | 01.04.2021 | 800   |
      | 2            | 01.03.2021 | 01.03.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.04.2021 | 01.04.2021 |             | 800   | NY           | Nei        | 1          | 0                  |
      | 2            | 01.04.2021 | 01.04.2021 | 01.04.2021  | 800   | ENDR         | Ja         | 1          | 0                  |

  Scenario: Opphører den siste av to perioder

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 31.03.2021 | 700   |
      | 1            | 01.04.2021 | 30.04.2021 | 800   |
      | 2            | 01.03.2021 | 31.03.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 31.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.04.2021 | 30.04.2021 |             | 800   | NY           | Nei        | 1          | 0                  |
      | 2            | 01.04.2021 | 30.04.2021 | 01.04.2021  | 800   | ENDR         | Ja         | 1          | 0                  |

  Scenario: Opphører en lang periode

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.06.2021 | 700   |
      | 2            | 01.03.2021 | 01.04.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.06.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 01.03.2021 | 01.06.2021 | 02.04.2021  | 700   | ENDR         | Ja         | 0          |                    |

  Scenario: Opphør en tidligere periode da vi kun har med den andre av 2 perioder

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.03.2021 | 700   |
      | 1            | 01.04.2021 | 01.04.2021 | 700   |
      | 2            | 01.04.2021 | 01.04.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.04.2021 | 01.04.2021 |             | 700   | NY           | Nei        | 1          | 0                  |
      | 2            | 01.04.2021 | 01.04.2021 | 01.03.2021  | 700   | ENDR         | Ja         | 1          | 0                  |
      | 2            | 01.04.2021 | 01.04.2021 |             | 700   | ENDR         | Nei        | 2          | 1                  |

  Scenario: Opphører den første av to perioder

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp | Satstype |
      | 1            | 01.03.2021 | 31.03.2021 | 700   | MÅNEDLIG |
      | 1            | 01.04.2021 | 30.04.2021 | 700   | MÅNEDLIG |
      | 2            | 01.04.2021 | 30.04.2021 | 700   | MÅNEDLIG |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Satstype |
      | 1            | 01.03.2021 | 31.03.2021 |             | 700   | NY           | Nei        | 0          |                    | MÅNEDLIG |
      | 1            | 01.04.2021 | 30.04.2021 |             | 700   | NY           | Nei        | 1          | 0                  | MÅNEDLIG |
      | 2            | 01.04.2021 | 30.04.2021 | 01.03.2021  | 700   | ENDR         | Ja         | 1          | 0                  | MÅNEDLIG |
      | 2            | 01.04.2021 | 30.04.2021 |             | 700   | ENDR         | Nei        | 2          | 1                  | MÅNEDLIG |

  Scenario: Endrer en tidligere periode til 0-utbetaling

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.03.2021 | 700   |
      | 1            | 01.04.2021 | 01.04.2021 | 700   |
      | 2            | 01.04.2021 | 01.04.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.04.2021 | 01.04.2021 |             | 700   | NY           | Nei        | 1          | 0                  |
      | 2            | 01.04.2021 | 01.04.2021 | 01.03.2021  | 700   | ENDR         | Ja         | 1          | 0                  |
      | 2            | 01.04.2021 | 01.04.2021 |             | 700   | ENDR         | Nei        | 2          | 1                  |


  Scenario: 2 opphør etter hverandre på ulike perioder

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.03.2021 | 700   |
      | 1            | 01.04.2021 | 01.04.2021 | 800   |
      | 1            | 01.05.2021 | 01.05.2021 | 900   |
      | 2            | 01.03.2021 | 01.03.2021 | 700   |
      | 2            | 01.04.2021 | 01.04.2021 | 800   |
      | 3            | 01.03.2021 | 01.03.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.04.2021 | 01.04.2021 |             | 800   | NY           | Nei        | 1          | 0                  |
      | 1            | 01.05.2021 | 01.05.2021 |             | 900   | NY           | Nei        | 2          | 1                  |
      | 2            | 01.05.2021 | 01.05.2021 | 01.05.2021  | 900   | ENDR         | Ja         | 2          | 1                  |
      | 3            | 01.05.2021 | 01.05.2021 | 01.04.2021  | 900   | ENDR         | Ja         | 2          | 1                  |


  Scenario: Opphør mellom 2 andeler

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.08.2021 | 700   |
      | 2            | 01.03.2021 | 01.04.2021 | 700   |
      | 2            | 01.07.2021 | 01.08.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.08.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 01.03.2021 | 01.08.2021 | 02.04.2021  | 700   | ENDR         | Ja         | 0          |                    |
      | 2            | 01.07.2021 | 01.08.2021 |             | 700   | ENDR         | Nei        | 1          | 0                  |

  Scenario: Avkorter en periode, som man sen opphører. Her må opphøret ha peiling på siste andelen med riktig tom

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.03.2021 | 700   |
      | 1            | 01.04.2021 | 01.08.2021 | 700   |
      | 2            | 01.03.2021 | 01.03.2021 | 700   |
      | 2            | 01.04.2021 | 01.05.2021 | 700   |
      | 3            | 01.03.2021 | 01.03.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.04.2021 | 01.08.2021 |             | 700   | NY           | Nei        | 1          | 0                  |
      | 2            | 01.04.2021 | 01.08.2021 | 02.05.2021  | 700   | ENDR         | Ja         | 1          | 0                  |
      | 3            | 01.04.2021 | 01.08.2021 | 01.04.2021  | 700   | ENDR         | Ja         | 1          | 0                  |

    Så forvent følgende andeler med periodeId
      | BehandlingId | Id | Periode id | Forrige periode id |
      | 1            | 0  | 0          |                    |
      | 1            | 1  | 1          | 0                  |
      | 2            | 2  | 0          |                    |
      | 2            | 3  | 1          | 0                  |
      | 3            | 4  | 0          |                    |

  Scenario: Forkorter en enkelt periode flere ganger. Oppdragslinjen fra første behandling skal da opphøres flere ganger.

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 02.06.2023 | 15.08.2023 | 700   |
      | 2            | 02.06.2023 | 15.07.2023 | 700   |
      | 3            | 02.06.2023 | 15.06.2023 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.06.2023 | 15.08.2023 |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 02.06.2023 | 15.08.2023 | 16.07.2023  | 700   | ENDR         | Ja         | 0          |                    |
      | 3            | 02.06.2023 | 15.08.2023 | 16.06.2023  | 700   | ENDR         | Ja         | 0          |                    |

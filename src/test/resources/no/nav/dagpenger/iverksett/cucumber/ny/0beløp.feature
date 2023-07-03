# language: no
# encoding: UTF-8

Egenskap: Håndtering av 0-beløp


  Scenario: Endrer en tidligere periode til 0-utbetaling

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.03.2021 | 700   |
      | 1            | 01.04.2021 | 01.04.2021 | 700   |
      | 2            | 01.04.2021 | 01.04.2021 | 700   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag 2
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.04.2021 | 01.04.2021 |             | 700   | NY           | Nei        | 1          | 0                  |
      | 2            | 01.04.2021 | 01.04.2021 | 01.03.2021  | 700   | ENDR         | Ja         | 1          | 0                  |
      | 2            | 01.04.2021 | 01.04.2021 |             | 700   | ENDR         | Nei        | 2          | 1                  |

  Scenario: Splitter en periode til 2 perioder der en av de får 0-beløp

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.04.2021 | 700   |
      | 1            | 01.05.2021 | 01.06.2021 | 800   |
      | 2            | 01.03.2021 | 01.03.2021 | 700   |
      | 2            | 01.05.2021 | 01.06.2021 | 800   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag 2
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.04.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.05.2021 | 01.06.2021 |             | 800   | NY           | Nei        | 1          | 0                  |
      | 2            | 01.05.2021 | 01.06.2021 | 02.03.2021  | 800   | ENDR         | Ja         | 1          | 0                  |
      | 2            | 01.05.2021 | 01.06.2021 |             | 800   | ENDR         | Nei        | 2          | 1                  |

  Scenario: 0-beløp beholdes, og får en ny andel

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 01.03.2021 | 01.03.2021 | 700   |
      | 1            | 01.05.2021 | 01.05.2021 | 800   |
      | 2            | 01.03.2021 | 01.03.2021 | 700   |
      | 2            | 01.05.2021 | 01.05.2021 | 800   |
      | 2            | 01.06.2021 | 01.06.2021 | 900   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag 2
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 01.05.2021 | 01.05.2021 |             | 800   | NY           | Nei        | 1          | 0                  |
      | 2            | 01.06.2021 | 01.06.2021 |             | 900   | ENDR         | Nei        | 2          | 1                  |

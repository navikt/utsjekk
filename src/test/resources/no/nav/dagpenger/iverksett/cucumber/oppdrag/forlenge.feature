# language: no
# encoding: UTF-8

Egenskap: Forlenge periode

  Scenario: Eksisterende periode forlenges i revurdering.

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 02.06.2023 | 15.07.2023 | 800   |
      | 2            | 02.06.2023 | 15.08.2023 | 800   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.06.2023 | 15.07.2023 |             | 800   | NY           | Nei        | 0          |                    |
      | 2            | 02.06.2023 | 15.08.2023 |             | 800   | ENDR         | Nei        | 1          | 0                  |

  Scenario: Periode med månedssats forlenges i slutten.

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp | Satstype |
      | 1            | 01.06.2023 | 31.07.2023 | 800   | MÅNEDLIG |
      | 2            | 01.06.2023 | 31.08.2023 | 800   | MÅNEDLIG |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Satstype |
      | 1            | 01.06.2023 | 31.07.2023 |             | 800   | NY           | Nei        | 0          |                    | MÅNEDLIG |
      | 2            | 01.06.2023 | 31.08.2023 |             | 800   | ENDR         | Nei        | 1          | 0                  | MÅNEDLIG |

  Scenario: Periode med månedssats utvides i starten.

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp | Satstype |
      | 1            | 01.06.2023 | 31.07.2023 | 800   | MÅNEDLIG |
      | 2            | 01.05.2023 | 31.07.2023 | 800   | MÅNEDLIG |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Satstype |
      | 1            | 01.06.2023 | 31.07.2023 |             | 800   | NY           | Nei        | 0          |                    | MÅNEDLIG |
      | 2            | 01.05.2023 | 31.07.2023 |             | 800   | ENDR         | Nei        | 1          | 0                  | MÅNEDLIG |

  Scenario: Periode med månedssats utvides i begge ender.

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp | Satstype |
      | 1            | 01.06.2023 | 31.07.2023 | 800   | MÅNEDLIG |
      | 2            | 01.05.2023 | 31.08.2023 | 800   | MÅNEDLIG |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Satstype |
      | 1            | 01.06.2023 | 31.07.2023 |             | 800   | NY           | Nei        | 0          |                    | MÅNEDLIG |
      | 2            | 01.05.2023 | 31.08.2023 |             | 800   | ENDR         | Nei        | 1          | 0                  | MÅNEDLIG |
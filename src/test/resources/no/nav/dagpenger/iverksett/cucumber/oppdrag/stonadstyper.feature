# language: no
# encoding: UTF-8

Egenskap: Ulike stønadstyper på andelene


  Scenario: Søker med ordinær arbeidssøker og permittering

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp | Ytelse                          |
      | 1            | 01.03.2021 | 01.03.2021 | 700   | DAGPENGER_ARBEIDSSOKER_ORDINAER |
      | 1            | 01.03.2021 | 01.03.2021 | 800   | DAGPENGER_PERMITTERING_ORDINAER |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Ytelse                          | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 700   | DAGPENGER_ARBEIDSSOKER_ORDINAER | NY           | Nei        | 0          |                    |
      | 1            | 01.03.2021 | 01.03.2021 |             | 800   | DAGPENGER_PERMITTERING_ORDINAER | NY           | Nei        | 1          |                    |

  Scenario: Revurdering endrer beløp på permittering fra april

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp | Ytelse                          |
      | 1            | 01.03.2021 | 01.05.2021 | 700   | DAGPENGER_ARBEIDSSOKER_ORDINAER |
      | 1            | 01.03.2021 | 01.05.2021 | 800   | DAGPENGER_PERMITTERING_ORDINAER |
      | 2            | 01.03.2021 | 01.05.2021 | 700   | DAGPENGER_ARBEIDSSOKER_ORDINAER |
      | 2            | 01.03.2021 | 31.03.2021 | 800   | DAGPENGER_PERMITTERING_ORDINAER |
      | 2            | 01.04.2021 | 01.05.2021 | 900   | DAGPENGER_PERMITTERING_ORDINAER |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Ytelse                          | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.05.2021 |             | 700   | DAGPENGER_ARBEIDSSOKER_ORDINAER | NY           | Nei        | 0          |                    |
      | 1            | 01.03.2021 | 01.05.2021 |             | 800   | DAGPENGER_PERMITTERING_ORDINAER | NY           | Nei        | 1          |                    |
      | 2            | 01.04.2021 | 01.05.2021 |             | 900   | DAGPENGER_PERMITTERING_ORDINAER | ENDR         | Nei        | 2          | 1                  |

  Scenario: Søker har flere stønadstyper som alle blir egne kjeder. Øker hvert beløp med 100kr i revurderingen for å verifisere at det fortsatt blir 3 ulike kjeder

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp | Ytelse                          |
      | 1            | 01.03.2021 | 01.03.2021 | 100   | DAGPENGER_ARBEIDSSOKER_ORDINAER |
      | 1            | 01.03.2021 | 01.03.2021 | 200   | DAGPENGER_PERMITTERING_ORDINAER |
      | 1            | 01.03.2021 | 01.03.2021 | 300   | DAGPENGER_EOS                   |
      | 2            | 01.03.2021 | 01.03.2021 | 200   | DAGPENGER_ARBEIDSSOKER_ORDINAER |
      | 2            | 01.03.2021 | 01.03.2021 | 300   | DAGPENGER_PERMITTERING_ORDINAER |
      | 2            | 01.03.2021 | 01.03.2021 | 400   | DAGPENGER_EOS                   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Ytelse                          | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 01.03.2021 | 01.03.2021 |             | 100   | DAGPENGER_ARBEIDSSOKER_ORDINAER | NY           | Nei        | 0          |                    |
      | 1            | 01.03.2021 | 01.03.2021 |             | 200   | DAGPENGER_PERMITTERING_ORDINAER | NY           | Nei        | 1          |                    |
      | 1            | 01.03.2021 | 01.03.2021 |             | 300   | DAGPENGER_EOS                   | NY           | Nei        | 2          |                    |
      | 2            | 01.03.2021 | 01.03.2021 |             | 200   | DAGPENGER_ARBEIDSSOKER_ORDINAER | ENDR         | Nei        | 3          | 0                  |
      | 2            | 01.03.2021 | 01.03.2021 |             | 300   | DAGPENGER_PERMITTERING_ORDINAER | ENDR         | Nei        | 4          | 1                  |
      | 2            | 01.03.2021 | 01.03.2021 |             | 400   | DAGPENGER_EOS                   | ENDR         | Nei        | 5          | 2                  |

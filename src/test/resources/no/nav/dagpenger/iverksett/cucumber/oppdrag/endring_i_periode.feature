# language: no
# encoding: UTF-8

Egenskap: Endre og forlenge eksisterende periode

  Scenario: Endrer beløp og forlenger periode i revurdering

    Gitt følgende tilkjente ytelser
      | BehandlingId | Fra dato   | Til dato   | Beløp |
      | 1            | 02.06.2023 | 15.08.2023 | 800   |
      | 2            | 02.06.2023 | 02.07.2023 | 800   |
      | 2            | 03.07.2023 | 04.09.2023 | 400   |

    Når beregner utbetalingsoppdrag

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato   | Til dato   | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.06.2023 | 15.08.2023 |             | 800   | NY           | Nei        | 0          |                    |
      | 2            | 03.07.2023 | 04.09.2023 |             | 400   | ENDR         | Nei        | 1          | 0                  |

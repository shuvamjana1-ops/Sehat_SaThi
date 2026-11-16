# SehatSaathi Drug Knowledge Dataset & Verification Sources

This directory contains the bundled sample drug knowledge dataset (`sample_drugs_dataset.json`) used by SehatSaathi for deterministic interaction checks, generic alternatives, price bands, and refill tracking.

## Public Sources for Verification

All sample entries in the dataset are marked with `"verified": false` and are intended for demonstration and offline support only. Clinical and regulatory data should be independently verified against official public health authorities:

1. **CDSCO (Central Drugs Standard Control Organization, India)**: Official regulatory body for drug approvals and standards [https://cdsco.gov.in](https://cdsco.gov.in)
2. **NLEM (National List of Essential Medicines, India)**: Essential medicines pricing and categorization guidelines [https://nppa.gov.in](https://nppa.gov.in)
3. **Pradhan Mantri Bhartiya Janaushadhi Pariyojana (PMBJP)**: Generic drug directory and price bands [https://janaushadhi.gov.in](https://janaushadhi.gov.in)
4. **US FDA Drugs@FDA & DailyMed**: Approved drug labeling, interactions, and clinical pharmacology [https://dailymed.nlm.nih.gov](https://dailymed.nlm.nih.gov)
5. **WHO Model List of Essential Medicines**: International benchmark for essential therapeutics [https://www.who.int](https://www.who.int)

## Dataset Validation & Integrity

To ensure dataset integrity, automated unit and Robolectric tests (`DrugKnowledgeTest`) validate:
- Unique medication IDs (no duplicate IDs).
- Required schema fields (`id`, `ingredient`, `strength`, `form`, `category`, `brands`, `price_band_brand`, `generic_name`, `price_band_generic`, `source`, `verified`).
- Interaction pair structure (`ingredient_a`, `ingredient_b`, `severity`, `reason`).
- All entries maintain `verified: false` and include official regulatory source attributions.

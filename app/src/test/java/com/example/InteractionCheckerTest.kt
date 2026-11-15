package com.example

import com.example.data.DrugKnowledgeDataset
import com.example.data.DrugKnowledgeRepository
import com.example.data.InteractionSeverity
import com.example.domain.drugs.InteractionChecker
import com.example.domain.drugs.MedicineCheckInput
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InteractionCheckerTest {

  private lateinit var dataset: DrugKnowledgeDataset

  @Before
  fun loadDataset() {
    val file =
      listOf(
        File("app/src/main/assets/sample_drugs_dataset.json"),
        File("src/main/assets/sample_drugs_dataset.json"),
      ).first { it.exists() }
    dataset = DrugKnowledgeRepository.parseJson(file.readText())
  }

  @Test
  fun datasetContainsRoughly60MedicinesAndNotice() {
    assertTrue(dataset.medicines.size >= 55)
    assertTrue(dataset.datasetNotice.contains("Sample data"))
  }

  @Test
  fun brandNormalizationAndExactStrengthGenericMatching() {
    val reportSameStrength =
      InteractionChecker.runSafetyCheck(
        dataset,
        listOf(MedicineCheckInput(displayName = "Glycomet", strength = "500 mg")),
      )
    val match = reportSameStrength.resolvedMedicines.first()
    assertEquals("Metformin", match.resolvedIngredient)
    assertNotNull(match.genericEquivalent)

    // Strength differs (250 mg vs 500 mg) -> generic must NOT be suggested for wrong strength
    val reportDiffStrength =
      InteractionChecker.runSafetyCheck(
        dataset,
        listOf(MedicineCheckInput(displayName = "Glycomet", strength = "250 mg")),
      )
    assertNull(reportDiffStrength.resolvedMedicines.first().genericEquivalent)
  }

  @Test
  fun duplicateIngredientDetectedAcrossDifferentBrands() {
    val report =
      InteractionChecker.runSafetyCheck(
        dataset,
        listOf(
          MedicineCheckInput("Dolo 650", strength = "650 mg"),
          MedicineCheckInput("Crocin 650", strength = "650 mg"),
        ),
      )
    assertEquals(1, report.duplicates.size)
    assertEquals("Paracetamol", report.duplicates.first().ingredient)
  }

  @Test
  fun knownDangerousPairsTriggerExpectedSeverity() {
    val report =
      InteractionChecker.runSafetyCheck(
        dataset,
        listOf(
          MedicineCheckInput("Amaryl", strength = "1 mg"),
          MedicineCheckInput("Forcan 150", strength = "150 mg"),
          MedicineCheckInput("Telma", strength = "40 mg"),
          MedicineCheckInput("Brufen 400", strength = "400 mg"),
        ),
      )
    assertTrue(report.interactions.any { it.severity == InteractionSeverity.AVOID })
    assertTrue(report.interactions.any { it.severity == InteractionSeverity.CAUTION })
  }

  @Test
  fun unknownMedicineNeverShownAsSafe() {
    val report =
      InteractionChecker.runSafetyCheck(
        dataset,
        listOf(
          MedicineCheckInput("MysteryHerbX"),
          MedicineCheckInput("Glycomet", strength = "500 mg"),
        ),
      )
    val unknown = report.resolvedMedicines.first()
    assertTrue(unknown.isUnknown)
    assertEquals(InteractionChecker.UNKNOWN_MESSAGE, unknown.unknownMessage)
    assertTrue(report.unknownPairNotices.any { it.contains("Not in our database. Please ask your pharmacist.") })
  }
}

package com.example.domain.drugs

import com.example.data.DrugKnowledgeDataset
import com.example.data.DrugReference
import com.example.data.HealthStatusLevel
import com.example.data.InteractionSeverity

data class MedicineCheckInput(
  val displayName: String,
  val normalizedHint: String? = null,
  val strength: String? = null,
)

data class ResolvedMedicineMatch(
  val inputName: String,
  val inputStrength: String?,
  val matchedDrug: DrugReference?,
  val resolvedIngredient: String?,
  val genericEquivalent: GenericMatch?,
  val isUnknown: Boolean,
  val unknownMessage: String?,
)

data class GenericMatch(
  val genericName: String,
  val ingredient: String,
  val strength: String,
  val approxBrandPrice: String,
  val approxGenericPrice: String,
  val priceDisclaimer: String,
)

data class DuplicateIngredientWarning(
  val ingredient: String,
  val medicineNames: List<String>,
  val message: String,
)

data class InteractionWarning(
  val medicineA: String,
  val medicineB: String,
  val ingredientA: String,
  val ingredientB: String,
  val severity: InteractionSeverity,
  val statusLevel: HealthStatusLevel,
  val reason: String,
)

data class MedicineSafetyCheckReport(
  val datasetNotice: String,
  val resolvedMedicines: List<ResolvedMedicineMatch>,
  val duplicates: List<DuplicateIngredientWarning>,
  val interactions: List<InteractionWarning>,
  val unknownPairNotices: List<String>,
)

object InteractionChecker {

  const val UNKNOWN_MESSAGE = "Not in our database. Please ask your pharmacist."
  private const val SAFETY_SUFFIX = "Do not stop any medicine on your own. Ask your doctor or pharmacist."

  fun normalizeToken(raw: String?): String =
    raw.orEmpty()
      .lowercase()
      .replace(Regex("[^a-z0-9.+ ]"), " ")
      .replace(Regex("\\s+"), " ")
      .trim()

  fun normalizeStrength(raw: String?): String =
    raw.orEmpty()
      .lowercase()
      .replace("milligram", "mg")
      .replace("microgram", "mcg")
      .replace(" ", "")
      .trim()

  fun findDrugMatch(
    dataset: DrugKnowledgeDataset,
    name: String,
    normalizedHint: String? = null,
    strength: String? = null,
  ): DrugReference? {
    val candidates =
      listOfNotNull(normalizedHint, name)
        .map { normalizeToken(it) }
        .filter { it.isNotEmpty() }
    val normStrength = normalizeStrength(strength)

    val matchingEntries =
      dataset.medicines.filter { drug ->
        val ing = normalizeToken(drug.ingredient)
        val brandTokens = drug.brands.map { normalizeToken(it) }
        candidates.any { q ->
          q == ing ||
            q.startsWith("$ing ") ||
            ing.startsWith("$q ") ||
            brandTokens.any { b -> q == b || q.contains(b) || b.contains(q) }
        }
      }

    if (matchingEntries.isEmpty()) return null
    if (normStrength.isEmpty()) return matchingEntries.first()

    // Exact strength match preferred; never return a generic match when strength differs
    return matchingEntries.firstOrNull { normalizeStrength(it.strength) == normStrength }
      ?: matchingEntries.first()
  }

  /**
   * Matches generic equivalent ONLY when BOTH ingredient AND strength match.
   */
  fun findExactGenericEquivalent(
    dataset: DrugKnowledgeDataset,
    drug: DrugReference,
    enteredStrength: String?,
  ): GenericMatch? {
    val normEntered = normalizeStrength(enteredStrength)
    val normRef = normalizeStrength(drug.strength)
    if (normEntered.isNotEmpty() && normEntered != normRef) {
      // Strength does not match -> do not suggest wrong-strength generic
      return null
    }
    return GenericMatch(
      genericName = drug.genericName,
      ingredient = drug.ingredient,
      strength = drug.strength,
      approxBrandPrice = drug.priceBandBrand,
      approxGenericPrice = drug.priceBandGeneric,
      priceDisclaimer = dataset.priceNotice,
    )
  }

  fun runSafetyCheck(
    dataset: DrugKnowledgeDataset,
    medicines: List<MedicineCheckInput>,
  ): MedicineSafetyCheckReport {
    val resolved =
      medicines.map { input ->
        val matched =
          findDrugMatch(
            dataset = dataset,
            name = input.displayName,
            normalizedHint = input.normalizedHint,
            strength = input.strength,
          )
        if (matched == null) {
          ResolvedMedicineMatch(
            inputName = input.displayName,
            inputStrength = input.strength,
            matchedDrug = null,
            resolvedIngredient = null,
            genericEquivalent = null,
            isUnknown = true,
            unknownMessage = UNKNOWN_MESSAGE,
          )
        } else {
          val generic = findExactGenericEquivalent(dataset, matched, input.strength)
          ResolvedMedicineMatch(
            inputName = input.displayName,
            inputStrength = input.strength ?: matched.strength,
            matchedDrug = matched,
            resolvedIngredient = matched.ingredient,
            genericEquivalent = generic,
            isUnknown = false,
            unknownMessage = null,
          )
        }
      }

    // Detect duplicate ingredients across brands
    val duplicates =
      resolved
        .filter { it.resolvedIngredient != null }
        .groupBy { normalizeToken(it.resolvedIngredient) }
        .values
        .filter { group -> group.size >= 2 }
        .map { group ->
          val ingredient = group.first().resolvedIngredient!!
          val names = group.map { it.inputName }
          DuplicateIngredientWarning(
            ingredient = ingredient,
            medicineNames = names,
            message =
              "Duplicate ingredient found: ${names.joinToString(" and ")} both contain $ingredient. $SAFETY_SUFFIX",
          )
        }

    val warnings = mutableListOf<InteractionWarning>()
    val unknownPairs = mutableListOf<String>()

    for (i in resolved.indices) {
      for (j in i + 1 until resolved.size) {
        val a = resolved[i]
        val b = resolved[j]
        if (a.isUnknown || b.isUnknown) {
          unknownPairs += "${a.inputName} + ${b.inputName}: $UNKNOWN_MESSAGE"
          continue
        }
        val ingA = normalizeToken(a.resolvedIngredient)
        val ingB = normalizeToken(b.resolvedIngredient)
        if (ingA == ingB) continue

        val match =
          dataset.interactions.firstOrNull { ref ->
            val refA = normalizeToken(ref.ingredientA)
            val refB = normalizeToken(ref.ingredientB)
            (refA == ingA && refB == ingB) || (refA == ingB && refB == ingA)
          }
        if (match != null) {
          val level =
            when (match.severity) {
              InteractionSeverity.AVOID -> HealthStatusLevel.URGENT
              InteractionSeverity.CAUTION -> HealthStatusLevel.CAUTION
              InteractionSeverity.MONITOR -> HealthStatusLevel.INFO
            }
          warnings +=
            InteractionWarning(
              medicineA = a.inputName,
              medicineB = b.inputName,
              ingredientA = a.resolvedIngredient!!,
              ingredientB = b.resolvedIngredient!!,
              severity = match.severity,
              statusLevel = level,
              reason = "${match.reason} $SAFETY_SUFFIX",
            )
        }
      }
    }

    return MedicineSafetyCheckReport(
      datasetNotice = dataset.datasetNotice,
      resolvedMedicines = resolved,
      duplicates = duplicates,
      interactions = warnings,
      unknownPairNotices = unknownPairs,
    )
  }
}

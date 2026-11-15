package com.example.data

import android.content.Context
import org.json.JSONObject

data class DrugReference(
  val id: String,
  val ingredient: String,
  val strength: String,
  val form: String,
  val category: String,
  val brands: List<String>,
  val priceBandBrand: String,
  val genericName: String,
  val priceBandGeneric: String,
)

enum class InteractionSeverity(val label: String) {
  AVOID("Avoid"),
  CAUTION("Caution"),
  MONITOR("Monitor"),
}

data class InteractionReference(
  val ingredientA: String,
  val ingredientB: String,
  val severity: InteractionSeverity,
  val reason: String,
)

data class DrugKnowledgeDataset(
  val datasetNotice: String,
  val priceNotice: String,
  val medicines: List<DrugReference>,
  val interactions: List<InteractionReference>,
)

object DrugKnowledgeRepository {

  fun loadFromAssets(context: Context): DrugKnowledgeDataset {
    val jsonStr =
      context.assets.open("sample_drugs_dataset.json").bufferedReader().use { it.readText() }
    return parseJson(jsonStr)
  }

  fun parseJson(jsonStr: String): DrugKnowledgeDataset {
    val root = JSONObject(jsonStr)
    val datasetNotice =
      root.optString(
        "dataset_notice",
        "Sample data: verify with an official source before real use",
      )
    val priceNotice =
      root.optString("price_notice", "Prices are approximate; verify at the store.")

    val medsArr = root.getJSONArray("medicines")
    val meds =
      (0 until medsArr.length()).map { i ->
        val obj = medsArr.getJSONObject(i)
        val brandsArr = obj.getJSONArray("brands")
        val brands = (0 until brandsArr.length()).map { b -> brandsArr.getString(b) }
        DrugReference(
          id = obj.getString("id"),
          ingredient = obj.getString("ingredient"),
          strength = obj.getString("strength"),
          form = obj.getString("form"),
          category = obj.getString("category"),
          brands = brands,
          priceBandBrand = obj.getString("price_band_brand"),
          genericName = obj.getString("generic_name"),
          priceBandGeneric = obj.getString("price_band_generic"),
        )
      }

    val intArr = root.getJSONArray("interactions")
    val interactions =
      (0 until intArr.length()).map { i ->
        val obj = intArr.getJSONObject(i)
        val sevStr = obj.getString("severity").lowercase()
        val severity =
          when (sevStr) {
            "avoid" -> InteractionSeverity.AVOID
            "caution" -> InteractionSeverity.CAUTION
            else -> InteractionSeverity.MONITOR
          }
        InteractionReference(
          ingredientA = obj.getString("ingredient_a"),
          ingredientB = obj.getString("ingredient_b"),
          severity = severity,
          reason = obj.getString("reason"),
        )
      }

    return DrugKnowledgeDataset(
      datasetNotice = datasetNotice,
      priceNotice = priceNotice,
      medicines = meds,
      interactions = interactions,
    )
  }
}

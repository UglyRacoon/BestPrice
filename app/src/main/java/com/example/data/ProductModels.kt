package com.example.data

enum class UnitCategory {
    WEIGHT, VOLUME, COUNT, TIME
}

enum class PriceUnit(
    val shortName: String,
    val longNameResId: Int, // references string in strings.xml
    val multiplier: Double, // conversion multiplier to the base unit in its category
    val category: UnitCategory
) {
    G("г", com.example.R.string.unit_g_long, 1.0, UnitCategory.WEIGHT),
    KG("кг", com.example.R.string.unit_kg_long, 1000.0, UnitCategory.WEIGHT),
    ML("мл", com.example.R.string.unit_ml_long, 1.0, UnitCategory.VOLUME),
    L("л", com.example.R.string.unit_l_long, 1000.0, UnitCategory.VOLUME),
    PCS("шт", com.example.R.string.unit_pcs_long, 1.0, UnitCategory.COUNT),
    DAYS("дн", com.example.R.string.unit_days_long, 1.0, UnitCategory.TIME);
    
    companion object {
        fun fromNameOrDefault(name: String): PriceUnit {
            return try {
                valueOf(name)
            } catch (e: Exception) {
                G
            }
        }
    }
}

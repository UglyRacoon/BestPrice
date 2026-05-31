package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val amount: Double,
    val unitName: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toProduct(): Product {
        return Product(
            id = id,
            name = name,
            price = price,
            amount = amount,
            unit = PriceUnit.fromNameOrDefault(unitName)
        )
    }
    
    companion object {
        fun fromProduct(product: Product): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.name,
                price = product.price,
                amount = product.amount,
                unitName = product.unit.name
            )
        }
    }
}

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val amount: Double,
    val unit: PriceUnit
)

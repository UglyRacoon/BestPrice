package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PriceUnit
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.data.UnitCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ComparisonResultItem(
    val product: Product,
    val originalPrice: Double,
    val originalAmount: Double,
    val originalUnit: PriceUnit,
    val calculatedPrice: Double?, // Normalized price for the specified comparison amount
    val isBestDeal: Boolean = false,
    val priceDifferencePercent: Double = 0.0,
    val rank: Int = 1
)

class PriceCalculatorViewModel(private val repository: ProductRepository) : ViewModel() {

    // Form states
    var priceInput by mutableStateOf("")
    var amountInput by mutableStateOf("")
    var selectedUnit by mutableStateOf(PriceUnit.G)
    var customNameInput by mutableStateOf("") // optional name override

    // Target comparison states ("Сравнить за:")
    var compareAmountInput by mutableStateOf("100")
    var compareUnit by mutableStateOf(PriceUnit.G)

    // Highlight state - controls whether to scroll to or actively show comparisons
    var isComparisonActive by mutableStateOf(false)

    // Form errors
    var formError by mutableStateOf<String?>(null)

    // Observe saved products from database
    val productsFlow: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Auto-update comparison target species when the first item is added
        viewModelScope.launch {
            productsFlow.collect { products ->
                if (products.size == 1 && !isComparisonActive) {
                    val firstItem = products.first()
                    compareUnit = firstItem.unit
                    compareAmountInput = when (firstItem.unit.category) {
                        UnitCategory.WEIGHT -> if (firstItem.unit == PriceUnit.KG) "1" else "100"
                        UnitCategory.VOLUME -> if (firstItem.unit == PriceUnit.L) "1" else "100"
                        UnitCategory.COUNT -> "1"
                        UnitCategory.TIME -> "1"
                    }
                }
            }
        }
    }

    fun addProduct() {
        val price = priceInput.trim().replace(',', '.').toDoubleOrNull()
        val amount = amountInput.trim().replace(',', '.').toDoubleOrNull()

        if (price == null || amount == null) {
            formError = "Пожалуйста, заполните поля числовыми значениями"
            return
        }

        if (price <= 0 || amount <= 0) {
            formError = "Значения цены и количества должны быть больше нуля"
            return
        }

        formError = null

        viewModelScope.launch {
            val listSize = productsFlow.value.size
            val defaultName = "Товар #${listSize + 1}"
            val name = if (customNameInput.trim().isNotEmpty()) customNameInput.trim() else defaultName

            val newProduct = Product(
                name = name,
                price = price,
                amount = amount,
                unit = selectedUnit
            )
            repository.insertProduct(newProduct)

            // Clear inputs
            priceInput = ""
            amountInput = ""
            customNameInput = ""
        }
    }

    fun updateProductName(product: Product, newName: String) {
        viewModelScope.launch {
            val updated = product.copy(name = newName)
            repository.insertProduct(updated)
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProductById(id)
            if (productsFlow.value.isEmpty()) {
                isComparisonActive = false
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllProducts()
            isComparisonActive = false
            formError = null
        }
    }

    fun triggerComparison() {
        isComparisonActive = true
    }

    fun calculateComparison(
        products: List<Product>,
        targetAmountInputStr: String,
        targetUnit: PriceUnit
    ): List<ComparisonResultItem> {
        val targetAmount = targetAmountInputStr.toDoubleOrNull() ?: 100.0
        if (products.isEmpty()) return emptyList()

        val targetCategory = targetUnit.category
        val targetBaseValue = targetAmount * targetUnit.multiplier

        val itemsWithCalculatedPrice = products.map { product ->
            val calculatedPrice: Double? = if (product.unit.category == targetCategory) {
                val productBaseValue = product.amount * product.unit.multiplier
                if (productBaseValue > 0) {
                    val pricePerBase = product.price / productBaseValue
                    pricePerBase * targetBaseValue
                } else {
                    null
                }
            } else {
                null
            }
            product to calculatedPrice
        }

        // Sort items by calculated price. Non-null first, sorted ascending; null at the end.
        val sortedComparable = itemsWithCalculatedPrice
            .filter { it.second != null }
            .map { it.first to it.second!! }
            .sortedBy { it.second }

        val baseBestPrice = sortedComparable.firstOrNull()?.second

        return itemsWithCalculatedPrice.map { (product, calcPrice) ->
            val isBest = calcPrice != null && baseBestPrice != null && Math.abs(calcPrice - baseBestPrice) < 0.001
            
            val diffPercent = if (calcPrice != null && baseBestPrice != null && baseBestPrice > 0) {
                val ratio = (calcPrice - baseBestPrice) / baseBestPrice
                ratio * 100.0
            } else {
                0.0
            }

            val rank = if (calcPrice != null) {
                sortedComparable.indexOfFirst { it.first.id == product.id } + 1
            } else {
                999
            }

            ComparisonResultItem(
                product = product,
                originalPrice = product.price,
                originalAmount = product.amount,
                originalUnit = product.unit,
                calculatedPrice = calcPrice,
                isBestDeal = isBest && sortedComparable.size > 1,
                priceDifferencePercent = diffPercent,
                rank = rank
            )
        }
    }

    class Factory(private val repository: ProductRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PriceCalculatorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PriceCalculatorViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

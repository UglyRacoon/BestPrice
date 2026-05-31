package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PriceUnit
import com.example.data.Product
import com.example.ui.theme.BronzeColor
import com.example.ui.theme.GoldColor
import com.example.ui.theme.SilverColor
import java.text.DecimalFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PriceCalculatorScreen(
    viewModel: PriceCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.productsFlow.collectAsState()
    val comparisonResults = remember(products, viewModel.compareAmountInput, viewModel.compareUnit) {
        viewModel.calculateComparison(products, viewModel.compareAmountInput, viewModel.compareUnit)
    }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Internal state for toggling item name editing mode
    var editingProductId by remember { mutableStateOf<Long?>(null) }
    var tempEditingName by remember { mutableStateOf("") }

    val comparableItems = remember(comparisonResults) {
        comparisonResults.filter { it.calculatedPrice != null }.sortedBy { it.rank }
    }
    val nonComparableItems = remember(comparisonResults) {
        comparisonResults.filter { it.calculatedPrice == null }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top App Bar integrated design style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚖️",
                            fontSize = 20.sp
                        )
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (products.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearAll() },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Очистить все",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // DYNAMIC CHAMPION SUMMARY CARD (Geometric Balance highlight design element)
            AnimatedVisibility(
                visible = comparableItems.size >= 2,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val best = comparableItems.firstOrNull()
                val second = comparableItems.getOrNull(1)
                
                if (best != null && second != null && best.calculatedPrice != null && second.calculatedPrice != null && second.calculatedPrice > 0) {
                    val diffPercent = ((second.calculatedPrice - best.calculatedPrice) / second.calculatedPrice) * 100.0
                    val dfDiff = DecimalFormat("#.#")
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ТЕКУЩИЙ ЛИДЕР",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${best.product.name} выгоднее на ${dfDiff.format(diffPercent)}%!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🎉",
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            // Main Product Input Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Новый товар",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.0.dp),
                        textAlign = TextAlign.Start
                    )

                    // Price input field
                    OutlinedTextField(
                        value = viewModel.priceInput,
                        onValueChange = { viewModel.priceInput = it },
                        label = { Text(stringResource(R.string.price_label)) },
                        placeholder = { Text(stringResource(R.string.price_placeholder)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("price_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Amount input field
                    OutlinedTextField(
                        value = viewModel.amountInput,
                        onValueChange = { viewModel.amountInput = it },
                        label = { Text(stringResource(R.string.amount_label)) },
                        placeholder = { Text(stringResource(R.string.amount_placeholder)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("amount_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Optional product descriptor field
                    OutlinedTextField(
                        value = viewModel.customNameInput,
                        onValueChange = { viewModel.customNameInput = it },
                        label = { Text("Название (необязательно)") },
                        placeholder = { Text("Пример: Бренд А, пачка 500г") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Unit Selection title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.unit_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Unit buttons arranged as 3 columns x 2 rows
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val row1 = listOf(PriceUnit.G, PriceUnit.KG, PriceUnit.ML)
                        val row2 = listOf(PriceUnit.L, PriceUnit.PCS, PriceUnit.DAYS)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row1.forEach { unit ->
                                UnitButton(
                                    unit = unit,
                                    isSelected = viewModel.selectedUnit == unit,
                                    onClick = { 
                                        viewModel.selectedUnit = unit
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row2.forEach { unit ->
                                UnitButton(
                                    unit = unit,
                                    isSelected = viewModel.selectedUnit == unit,
                                    onClick = { 
                                        viewModel.selectedUnit = unit
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Form validation errors
                    viewModel.formError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Add product button styled to match Geometric Balance primary CTAs
                    Button(
                        onClick = {
                            viewModel.addProduct()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("add_product_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.add_product),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Products list section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.products_title)} (${products.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Rounded Geometric list of products
            if (products.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛒", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.empty_items_tip),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    products.forEach { product ->
                        ProductItemCard(
                            product = product,
                            isEditing = editingProductId == product.id,
                            editingNameValue = tempEditingName,
                            onEditClick = {
                                editingProductId = product.id
                                tempEditingName = product.name
                            },
                            onNameChange = { tempEditingName = it },
                            onSaveName = {
                                viewModel.updateProductName(product, tempEditingName)
                                editingProductId = null
                            },
                            onCancelEdit = { editingProductId = null },
                            onDelete = { viewModel.deleteProduct(product.id) }
                        )
                    }

                    // Dash placeholder block reminiscent of add list item from CSS mockup
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                            .clickable {
                                focusManager.clearFocus()
                                // Simply scrolls or focuses to allow addition
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "Добавить еще один товар выше",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Compare control setup card ("Сравнить за:")
            if (products.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.compare_for),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Compare Quantity Field
                        OutlinedTextField(
                            value = viewModel.compareAmountInput,
                            onValueChange = { viewModel.compareAmountInput = it },
                            label = { Text(stringResource(R.string.amount_label)) },
                            placeholder = { Text("100") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Compare Unit Field (with custom secure DropdownMenu)
                        var dropdownExpanded by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = stringResource(viewModel.compareUnit.longNameResId),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.compare_unit)) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledContainerColor = Color.Transparent
                                ),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (dropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Dropdown Indicator"
                                    )
                                }
                            )

                            // Click interceptor box
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { dropdownExpanded = true }
                            )
                            
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                PriceUnit.entries.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(unit.longNameResId)) },
                                        onClick = {
                                            viewModel.compareUnit = unit
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Compare CTA Button
                        Button(
                            onClick = {
                                viewModel.triggerComparison()
                                focusManager.clearFocus()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("compare_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.compare_button),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Animated Comparison results segment
            AnimatedVisibility(
                visible = viewModel.isComparisonActive && products.size >= 1,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Результаты сравнения",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                    )

                    val formatAmount = viewModel.compareAmountInput.toDoubleOrNull() ?: 100.0

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Generous negative spacing
                    ) {
                        comparableItems.forEach { result ->
                            ComparisonResultCard(
                                result = result,
                                compareAmount = formatAmount,
                                compareUnit = viewModel.compareUnit
                            )
                        }

                        if (nonComparableItems.isNotEmpty()) {
                            Text(
                                text = "Несовместимые товары (другая категория):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                            )

                            nonComparableItems.forEach { result ->
                                ComparisonResultCard(
                                    result = result,
                                    compareAmount = formatAmount,
                                    compareUnit = viewModel.compareUnit
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun UnitButton(
    unit: PriceUnit,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = unit.shortName,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    isEditing: Boolean,
    editingNameValue: String,
    onEditClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onSaveName: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dfPrice = DecimalFormat("#.##")
    val dfAmount = DecimalFormat("#.###")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Information / In-place name Editor Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                if (isEditing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = editingNameValue,
                            onValueChange = onNameChange,
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        IconButton(onClick = onSaveName) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save name",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCancelEdit) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Cancel edit",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onEditClick() }
                    ) {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit name",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price & Quantity metadata row
                Text(
                    text = "${dfPrice.format(product.price)} руб. — ${dfAmount.format(product.amount)} ${product.unit.shortName}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Delete icon triggers reactive update
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.delete_content_description),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ComparisonResultCard(
    result: ComparisonResultItem,
    compareAmount: Double,
    compareUnit: PriceUnit
) {
    val dfPrice = DecimalFormat("#.##")
    val dfDiff = DecimalFormat("#.#")
    val dfAmount = DecimalFormat("#.###")

    val calculatedPrice = result.calculatedPrice

    // Determine visual style according to rank and compatibility from the theme
    val cardColor = when {
        calculatedPrice == null -> MaterialTheme.colorScheme.surface
        result.isBestDeal -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        calculatedPrice == null -> MaterialTheme.colorScheme.outline
        result.isBestDeal -> MaterialTheme.colorScheme.primary // Bold #6750A4 outline for winner/leader
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp), // More geometric, rounded corners
            colors = CardDefaults.cardColors(containerColor = cardColor),
            border = BorderStroke(if (result.isBestDeal) 2.dp else 1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank and Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (calculatedPrice != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when (result.rank) {
                                    1 -> GoldColor.copy(alpha = 0.25f)
                                    2 -> SilverColor.copy(alpha = 0.4f)
                                    3 -> BronzeColor.copy(alpha = 0.35f)
                                    else -> MaterialTheme.colorScheme.secondary
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (result.rank == 1) "🥇" else if (result.rank == 2) "🥈" else if (result.rank == 3) "🥉" else result.rank.toString(),
                                        fontSize = if (result.rank <= 3) 16.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Text(
                            text = result.product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Leave space for the floating best deal badge on top of the card
                    if (result.isBestDeal) {
                        Spacer(modifier = Modifier.width(80.dp))
                    } else if (calculatedPrice != null && result.priceDifferencePercent > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.diff_label, dfDiff.format(result.priceDifferencePercent)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Normalized Price Calculation
                if (calculatedPrice != null) {
                    Text(
                        text = stringResource(
                            R.string.item_calculated_for,
                            dfPrice.format(calculatedPrice),
                            dfAmount.format(compareAmount),
                            compareUnit.shortName
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.isBestDeal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "Несовместимо с ${compareUnit.shortName} (категория ${result.originalUnit.category})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Original specifications summary
                Text(
                    text = "Оригинал: ${dfPrice.format(result.originalPrice)} руб. за ${dfAmount.format(result.originalAmount)} ${result.originalUnit.shortName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Beautiful overlapping Best Deal ribbon at upper-right matching Geometric Balance layout
        if (calculatedPrice != null && result.isBestDeal) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-10).dp, x = (-12).dp),
                color = MaterialTheme.colorScheme.primary, // Vibrant Purple #6750A4
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "ВЫГОДНО",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

package com.uilover.project247.DashboardActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.DictionaryActivity.Model.DictionaryViewModel
import com.uilover.project247.data.models.Definition
import com.uilover.project247.data.models.DictionaryEntry
import com.uilover.project247.data.models.Meaning
import com.uilover.project247.data.models.SearchHistoryItem

@Composable
fun DictionaryScreenContent(
    viewModel: DictionaryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.errorMessage != null -> {
                    ErrorSection(
                        message = uiState.errorMessage ?: "",
                        onDismiss = { viewModel.clearError() }
                    )
                }
                
                uiState.entries.isNotEmpty() -> {
                    DictionaryResultsSection(entries = uiState.entries)
                }
                
                else -> {
                    EmptyStateSection()
                }
            }
        }
        
        // Search Bar với Dropdown - nằm trên cùng
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SearchBarSection(
                searchQuery = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { viewModel.searchWord(uiState.searchQuery) },
                onFocusChange = { viewModel.updateInputFocus(it) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Dropdown lịch sử
            if (uiState.isInputFocused && uiState.filteredRecentSearches.isNotEmpty()) {
                SearchHistoryDropdown(
                    items = uiState.filteredRecentSearches,
                    onItemClick = { word -> 
                        viewModel.selectRecentSearch(word)
                        viewModel.updateInputFocus(false)
                    }
                )
            }
        }
    }
}

@Composable
fun SearchBarSection(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFocusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    onFocusChange(focusState.isFocused)
                },
            placeholder = { 
                Text(
                    "Nhập từ cần tra...",
                    color = Color(0xFF9E9E9E)
                ) 
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search, 
                    contentDescription = "Tìm kiếm",
                    tint = Color(0xFF2196F3)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Xóa",
                            tint = Color(0xFF757575)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { onSearch() }
            )
        )
        
        // Search Button
        Button(
            onClick = onSearch,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Tìm kiếm",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ErrorSection(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFC62828),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
fun EmptyStateSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color(0xFFEDE7F6),
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF2196F3)
                )
            }
            Text(
                "Nhập từ để bắt đầu tra cứu",
                color = Color(0xFF757575),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Tìm kiếm từ vựng tiếng Anh nhanh chóng",
                color = Color(0xFF9E9E9E),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SearchHistoryDropdown(
    items: List<SearchHistoryItem>,
    onItemClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item.word) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.word,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            if (item.partOfSpeech.isNotEmpty()) {
                                Text(
                                    text = item.partOfSpeech,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                        
                        if (item.phonetic.isNotEmpty()) {
                            Text(
                                text = item.phonetic,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575),
                                fontStyle = FontStyle.Italic
                            )
                        }
                        
                        if (item.meaning.isNotEmpty()) {
                            Text(
                                text = item.meaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF616161),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentSearchesSection(
    recentSearches: List<SearchHistoryItem>,
    onSearchClick: (String) -> Unit
) {
    if (recentSearches.isEmpty()) {
        EmptyStateSection()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tìm kiếm gần đây",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }
            }
            
            items(recentSearches) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onSearchClick(item.word) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFFEDE7F6),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    item.word,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                                if (item.partOfSpeech.isNotEmpty()) {
                                    Text(
                                        item.partOfSpeech,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF757575),
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                            if (item.phonetic.isNotEmpty()) {
                                Text(
                                    item.phonetic,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            if (item.meaning.isNotEmpty()) {
                                Text(
                                    item.meaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF424242),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DictionaryResultsSection(entries: List<DictionaryEntry>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(entries) { entry ->
            DictionaryEntryCard(entry = entry)
        }
    }
}

@Composable
fun DictionaryEntryCard(entry: DictionaryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Word and Phonetic Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFEDE7F6),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.word,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    
                    val phoneticText = entry.phonetic 
                        ?: entry.phonetics.firstOrNull()?.text
                    
                    if (phoneticText != null) {
                        Text(
                            text = phoneticText,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2196F3),
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Audio button
                val audioUrl = entry.phonetics.firstOrNull { it.audio.isNullOrEmpty().not() }?.audio
                if (audioUrl != null) {
                    FilledIconButton(
                        onClick = { 
                            // TODO: Play audio
                        },
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF2196F3),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Phát âm",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            HorizontalDivider(color = Color(0xFFE0E0E0))
            
            // Meanings
            entry.meanings.forEachIndexed { index, meaning ->
                MeaningSection(meaning = meaning, showDivider = index < entry.meanings.size - 1)
            }
            
            // Origin
            if (entry.origin != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFFFF6F00),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nguồn gốc",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6F00)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = entry.origin,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF424242),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeaningSection(meaning: Meaning, showDivider: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Part of Speech Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1976D2),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = meaning.partOfSpeech,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        
        // Definitions
        meaning.definitions.forEachIndexed { index, definition ->
            DefinitionItem(
                definition = definition,
                number = index + 1
            )
        }
        
        // Synonyms
        if (meaning.synonyms.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔄 Từ đồng nghĩa: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0277BD)
                    )
                    Text(
                        text = meaning.synonyms.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF01579B)
                    )
                }
            }
        }
        
        // Antonyms
        if (meaning.antonyms.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "↔️ Từ trái nghĩa: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Text(
                        text = meaning.antonyms.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB71C1C)
                    )
                }
            }
        }
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun DefinitionItem(definition: Definition, number: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = Color(0xFF2196F3),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$number",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = definition.definition,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF212121),
                    lineHeight = 24.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (definition.example != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF9C4)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "💡",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "\"${definition.example}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5D4037),
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

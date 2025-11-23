package com.uilover.project247.DictionaryActivity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.uilover.project247.data.models.Phonetic
import com.uilover.project247.data.models.SearchHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tra từ điển") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Content - hiển thị trước SearchBar để dropdown nằm trên
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
}

@Composable
fun SearchBarSection(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        modifier = modifier.onFocusChanged { focusState ->
            onFocusChange(focusState.isFocused)
        },
        placeholder = { Text("Nhập từ cần tra...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Xóa")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            imeAction = androidx.compose.ui.text.input.ImeAction.Search
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onSearch = { onSearch() }
        )
    )
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
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color(0xFFC62828),
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Text(
                "Nhập từ để bắt đầu tra cứu",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
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
                .heightIn(max = 300.dp) // Giới hạn chiều cao
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
                        tint = Color(0xFF6200EA),
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Lịch sử tra cứu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        recentSearches.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSearchClick(item.word) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Từ và phát âm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6200EA)
                        )
                        if (item.partOfSpeech.isNotEmpty()) {
                            Text(
                                text = item.partOfSpeech,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                    
                    // Phát âm
                    if (item.phonetic.isNotEmpty()) {
                        Text(
                            text = item.phonetic,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666),
                            fontStyle = FontStyle.Italic
                        )
                    }
                    
                    // Nghĩa
                    if (item.meaning.isNotEmpty()) {
                        Text(
                            text = item.meaning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF333333),
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
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
        contentPadding = PaddingValues(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Word and Phonetic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = entry.word,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val phoneticText = entry.phonetic 
                        ?: entry.phonetics.firstOrNull()?.text
                    
                    if (phoneticText != null) {
                        Text(
                            text = phoneticText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
                
                // Audio button
                val audioUrl = entry.phonetics.firstOrNull { it.audio.isNullOrEmpty().not() }?.audio
                if (audioUrl != null) {
                    FilledTonalIconButton(
                        onClick = { 
                            // TODO: Play audio
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Phát âm",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Divider()
            
            // Meanings
            entry.meanings.forEachIndexed { index, meaning ->
                MeaningSection(meaning = meaning, showDivider = index < entry.meanings.size - 1)
            }
            
            // Origin
            if (entry.origin != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Nguồn gốc",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.origin,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}

@Composable
fun MeaningSection(meaning: Meaning, showDivider: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Part of Speech
        Text(
            text = meaning.partOfSpeech,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        
        // Definitions
        meaning.definitions.forEachIndexed { index, definition ->
            DefinitionItem(
                definition = definition,
                number = index + 1
            )
        }
        
        // Synonyms
        if (meaning.synonyms.isNotEmpty()) {
            Row {
                Text(
                    text = "Từ đồng nghĩa: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666)
                )
                Text(
                    text = meaning.synonyms.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0277BD)
                )
            }
        }
        
        // Antonyms
        if (meaning.antonyms.isNotEmpty()) {
            Row {
                Text(
                    text = "Từ trái nghĩa: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666)
                )
                Text(
                    text = meaning.antonyms.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC62828)
                )
            }
        }
        
        if (showDivider) {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun DefinitionItem(definition: Definition, number: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$number. ",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = definition.definition,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (definition.example != null) {
            Text(
                text = "\"${definition.example}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

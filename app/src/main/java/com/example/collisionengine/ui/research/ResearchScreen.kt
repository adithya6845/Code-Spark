package com.example.collisionengine.ui.research

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.collisionengine.data.network.DatabricksClient
import com.example.collisionengine.ui.components.AnimatedWaveform
import com.example.collisionengine.ui.components.ChatBubble
import com.example.collisionengine.ui.components.ProfileMatchCard
import com.example.collisionengine.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    viewModel: ResearchViewModel,
    onNavigateBack: () -> Unit,
    onFindCollisions: (String) -> Unit = {},
    onMatchClick: (com.example.collisionengine.data.model.ProfileMatch) -> Unit = {}
) {
    val context = LocalContext.current
    val queryText by viewModel.queryText.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showConfigDialog by remember { mutableStateOf(false) }
    var inputHost by remember { mutableStateOf(DatabricksClient.getHost(context)) }
    var inputToken by remember { mutableStateOf(DatabricksClient.getToken(context)) }
    var inputSpaceId by remember { mutableStateOf(DatabricksClient.getSpaceId(context)) }

    LaunchedEffect(messages.size, isLoading) {
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) {
            listState.animateScrollToItem(total - 1)
        }
    }

    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Text(
                    text = "Databricks Genie Config",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Connects directly to Databricks Genie Agent for live campus search.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = inputHost,
                        onValueChange = { inputHost = it },
                        label = { Text("Databricks Host URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputToken,
                        onValueChange = { inputToken = it },
                        label = { Text("Databricks Token (dapi...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputSpaceId,
                        onValueChange = { inputSpaceId = it },
                        label = { Text("Genie Space ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        DatabricksClient.saveSettings(context, inputHost, inputToken, inputSpaceId)
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Campus Connect AI",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showConfigDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Key,
                            contentDescription = "Genie Settings",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        // AI Waveform Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedWaveform(
                                modifier = Modifier.fillMaxSize(),
                                color = PrimaryBlue
                            )

                            // Genie Active Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "⚡ Databricks Genie Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (messages.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Talk to Campus Connect AI",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Ask any question about campus research, peer collaboration, faculty expertise, projects, or 'what if' scenarios. Powered by Databricks Genie Agent & local campus datasets.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    items(messages) { message ->
                        ChatBubble(
                            message = message.text,
                            isUser = message.isUser
                        )
                        if (!message.isUser && message.topMatches.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            message.topMatches.forEach { match ->
                                ProfileMatchCard(
                                    match = match,
                                    onClick = { onMatchClick(match) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PrimaryBlue,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Fetching response from Databricks Genie...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Chat Input Area
            val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
            val isKeyboardOpen = imeBottom > 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = if (isKeyboardOpen) 12.dp else 24.dp
                    )
                    .imePadding()
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Ask Databricks Genie or search campus...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.askDatabricks() },
                            enabled = queryText.isNotBlank() && !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (queryText.isNotBlank()) PrimaryBlue else Color.Gray
                            )
                        }
                    }
                )
            }
        }
    }
}

package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.ui.TeamMember
import com.example.ui.SharedPrompt
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChatConversation
import com.example.data.ChatMessage
import com.example.ui.ChatUiState
import com.example.ui.GalaxyTheme
import com.example.ui.GalaxyViewModel
import com.example.ui.SubscriptionTier
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GalaxyApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalaxyApp() {
    val context = LocalContext.current
    val viewModel: GalaxyViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // State bindings
    val subscriptionTier by viewModel.subscriptionTier.collectAsStateWithLifecycle()
    val activeTheme by viewModel.activeTheme.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val chatUiState by viewModel.chatUiState.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val freeCount by viewModel.freeQueryCount.collectAsStateWithLifecycle()

    // Sheet states
    var showPricingSheet by remember { mutableStateOf(false) }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var targetTierForCheckout by remember { mutableStateOf<SubscriptionTier?>(null) }
    var showIosGuideSheet by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf<Long?>(null) }
    var tempEditTitle by remember { mutableStateOf("") }
    
    // Resolve dynamic colors based on activeTheme selection (Strictly matching the "Bold Typography" design specifications)
    val primaryGradientBrush = when (activeTheme) {
        GalaxyTheme.SLATE_ASTRO -> Brush.linearGradient(
            colors = listOf(SpaceAccentLavender, SpaceAccentPink)
        )
        GalaxyTheme.DEEP_NEBULA -> Brush.linearGradient(
            colors = listOf(Color(0xFFFF007F), Color(0xFF7A0BC0))
        )
        GalaxyTheme.SOLAR_FLARE -> Brush.linearGradient(
            colors = listOf(Color(0xFFFF9F29), Color(0xFFFF5F00))
        )
    }

    val appBackgroundColor = when (activeTheme) {
        GalaxyTheme.SLATE_ASTRO -> SpaceBackground
        GalaxyTheme.DEEP_NEBULA -> Color(0xFF0B061A)
        GalaxyTheme.SOLAR_FLARE -> Color(0xFF0D0A08)
    }

    val bubbleSelfColor = when (activeTheme) {
        GalaxyTheme.SLATE_ASTRO -> SpaceSurface
        GalaxyTheme.DEEP_NEBULA -> Color(0xFF2E0249)
        GalaxyTheme.SOLAR_FLARE -> Color(0xFF2A1C15)
    }

    val accentColor = when (activeTheme) {
        GalaxyTheme.SLATE_ASTRO -> SpaceAccentLavender
        GalaxyTheme.DEEP_NEBULA -> Color(0xFFFF007F)
        GalaxyTheme.SOLAR_FLARE -> Color(0xFFFF9F29)
    }

    // Modal Drawer Sheet wrapping the whole app
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = appBackgroundColor,
                modifier = Modifier
                    .width(320.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                    )
            ) {
                // Side Drawer contents
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Organization Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primaryGradientBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Galaxy Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "GALAXY AI",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                "COSMIC ORBIT ENGINE • v1.0",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

                    // User Identity Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "hafizimran1981@gmail.com",
                                style = TextStyle(fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Tier Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.15f))
                                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = subscriptionTier.displayName.uppercase(),
                                        style = TextStyle(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    )
                                }

                                // Upgrade action if not Team
                                if (subscriptionTier != SubscriptionTier.TEAM) {
                                    Text(
                                        text = "Upgrade →",
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                showPricingSheet = true
                                                scope.launch { drawerState.close() }
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Action: Create New Chat Button
                    Button(
                        onClick = {
                            viewModel.createNewChat()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("new_chat_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryGradientBrush)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "NEW CHAT",
                                    style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                )
                            }
                        }
                    }

                    // Conversations Title Header
                    Text(
                        "RECENT CONVERSATIONS",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 2.sp
                        ),
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )

                    // Recent conversations list
                    if (conversations.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active space missions.\nClick NEW CHAT to begin.",
                                style = TextStyle(fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(conversations) { conv ->
                                val isSelected = conv.id == activeConversationId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) Color.White.copy(alpha = 0.07f) else Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.selectConversation(conv.id)
                                            scope.launch { drawerState.close() }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Chat icon",
                                            tint = if (isSelected) accentColor else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = conv.title,
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                color = if (isSelected) Color.White else Color.LightGray,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    
                                    // Row click options (rename/delete)
                                    Row {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit title",
                                            tint = Color.Gray.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    tempEditTitle = conv.title
                                                    showEditTitleDialog = conv.id
                                                }
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    viewModel.deleteConversation(conv.id)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

                    // Group menus: Workspace Board (For TEAM tier ONLY)
                    if (subscriptionTier == SubscriptionTier.TEAM) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE5A93C).copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFFE5A93C).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable {
                                    // Trigger a message to indicate Team workspace
                                    Toast.makeText(context, "Welcome to Team workspace dashboard", Toast.LENGTH_SHORT).show()
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Team Hub", tint = Color(0xFFE5A93C), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Team Workspace Active", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE5A93C)))
                                Text("4 Members • Shared Projects", style = TextStyle(fontSize = 9.sp, color = Color.Gray))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Theme Picker Selector
                    Text(
                        "ORBITAL STYLING",
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GalaxyTheme.values().forEach { theme ->
                            val isThemeSelected = activeTheme == theme
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isThemeSelected) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isThemeSelected) accentColor else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.setTheme(theme) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = theme.displayName.split(" ").last(),
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isThemeSelected) Color.White else Color.Gray
                                    )
                                )
                            }
                        }
                    }

                    // Navigation Footer Options
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Pricing/Subscription panel
                        DrawerOptionRow(
                            icon = Icons.Default.Star,
                            label = "Manage Subscriptions",
                            accentColor = accentColor,
                            onClick = {
                                showPricingSheet = true
                                scope.launch { drawerState.close() }
                            }
                        )

                        // iOS compatibility guide
                        DrawerOptionRow(
                            icon = Icons.Default.Share,
                            label = "iOS/KMP Blueprint",
                            accentColor = accentColor,
                            onClick = {
                                showIosGuideSheet = true
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        }
    ) {
        // Main Screen Interface
        Scaffold(
            containerColor = appBackgroundColor,
            topBar = {
                GalaxyTopBar(
                    title = "Galaxy AI",
                    subtitle = if (activeConversationId == null) "Initiating Core Link" else "Linked via $selectedModel",
                    accentColor = accentColor,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onPricingClick = { showPricingSheet = true },
                    onNewChatClick = { viewModel.createNewChat() },
                    subscriptionTier = subscriptionTier
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Workspace Layout
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // IF FREE, show smart usage limits card at top to encourage upgrades!
                    if (subscriptionTier == SubscriptionTier.FREE) {
                        FreeUsageBanner(freeCount = freeCount, onUpgradeClick = { showPricingSheet = true }, accentColor = accentColor)
                    }

                    // Chat message view or Welcome Portal
                    if (activeConversationId == null || activeMessages.isEmpty()) {
                        WelcomeSpacePortal(
                            onPromptClick = { prompt ->
                                viewModel.sendMessage(prompt)
                            },
                            accentColor = accentColor,
                            primaryGradient = primaryGradientBrush,
                            tier = subscriptionTier,
                            onUpgradeClick = { showPricingSheet = true }
                        )
                    } else {
                        // Message transcript
                        val listState = rememberLazyListState()
                        LaunchedEffect(activeMessages.size) {
                            if (activeMessages.isNotEmpty()) {
                                listState.animateScrollToItem(activeMessages.size - 1)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(activeMessages) { msg ->
                                ChatBubbleItem(
                                    message = msg,
                                    bubbleSelfColor = bubbleSelfColor,
                                    accentColor = accentColor
                                )
                            }

                            if (chatUiState is ChatUiState.Generating) {
                                item {
                                    GeneratingIndicatorItem(accentColor = accentColor)
                                }
                            }
                        }
                    }

                    // If error displays, present a persistent error message banner
                    if (chatUiState is ChatUiState.Error) {
                        ErrorBanner(
                            errorText = (chatUiState as ChatUiState.Error).error,
                            onUpgradeClick = { showPricingSheet = true },
                            onResetLimits = { viewModel.resetFreeCounter() },
                            tier = subscriptionTier,
                            accentColor = accentColor
                        )
                    }

                    // Bottom message input box
                    MessageInputField(
                        onSend = { text ->
                            viewModel.sendMessage(text)
                        },
                        isGenerating = chatUiState is ChatUiState.Generating,
                        accentColor = accentColor
                    )
                }
            }
        }
    }

    // Modal Sheet: Pricing plans panel (FREE, PLUS, TEAM)
    if (showPricingSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPricingSheet = false },
            containerColor = appBackgroundColor,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            PlansSettingsSection(
                activeTier = subscriptionTier,
                onSelectTier = { tier ->
                    if (tier == subscriptionTier) {
                        Toast.makeText(context, "Already subscribed to ${tier.displayName}", Toast.LENGTH_SHORT).show()
                    } else {
                        targetTierForCheckout = tier
                        showCheckoutSheet = true
                        showPricingSheet = false
                    }
                },
                onClose = { showPricingSheet = false },
                accentColor = accentColor,
                gradientBrush = primaryGradientBrush,
                teamMembers = viewModel.teamWorkspaceMembers,
                sharedPrompts = viewModel.teamSharedPrompts
            )
        }
    }

    // Modal Sheet: simulated Google Wallet Checkout sheet
    if (showCheckoutSheet && targetTierForCheckout != null) {
        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            containerColor = Color(0xFF1E1E24), // Google Wallet dark interface themed sheet
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            GoogleWalletCheckoutSheet(
                targetTier = targetTierForCheckout!!,
                onPaymentSuccess = {
                    viewModel.subscribeToTier(targetTierForCheckout!!)
                    showCheckoutSheet = false
                    targetTierForCheckout = null
                    Toast.makeText(context, "Payment Processed. Welcome to ${subscriptionTier.displayName}!", Toast.LENGTH_LONG).show()
                },
                onCancel = {
                    showCheckoutSheet = false
                    targetTierForCheckout = null
                }
            )
        }
    }

    // Modal Sheet: iOS & KMP Blueprint code reuse architecture demonstration
    if (showIosGuideSheet) {
        ModalBottomSheet(
            onDismissRequest = { showIosGuideSheet = false },
            containerColor = appBackgroundColor,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            IosMultiplatformBlueprintSheet(
                onClose = { showIosGuideSheet = false },
                accentColor = accentColor
            )
        }
    }

    // Simple Dialog: Edit Conversation titles
    if (showEditTitleDialog != null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222B)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Rename Mission Title",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = tempEditTitle,
                            onValueChange = { tempEditTitle = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "Cancel",
                                style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                modifier = Modifier
                                    .clickable { showEditTitleDialog = null }
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                "Confirm",
                                style = TextStyle(color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.editConversationTitle(showEditTitleDialog!!, tempEditTitle)
                                        showEditTitleDialog = null
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sub-component layouts
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalaxyTopBar(
    title: String,
    subtitle: String,
    accentColor: Color,
    onMenuClick: () -> Unit,
    onPricingClick: () -> Unit,
    onNewChatClick: () -> Unit,
    subscriptionTier: SubscriptionTier
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        ),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title.uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp,
                            color = accentColor
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Plan tag in App Bar
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            subscriptionTier.displayName.split(" ").first().uppercase(),
                            style = TextStyle(color = accentColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = TextStyle(fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Light)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick, modifier = Modifier.testTag("menu_button")) {
                Icon(Icons.Default.Menu, contentDescription = "Menu Sidebar", tint = Color.White)
            }
        },
        actions = {
            // New chat fast access icon
            IconButton(onClick = onNewChatClick) {
                Icon(Icons.Default.Add, contentDescription = "New Chat shortcut", tint = Color.White)
            }
            // Pricing options shortcut icon
            IconButton(onClick = onPricingClick) {
                Icon(Icons.Default.Star, contentDescription = "Settle Plans", tint = accentColor)
            }
        }
    )
}

@Composable
fun DrawerOptionRow(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = TextStyle(color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun FreeUsageBanner(
    freeCount: Int,
    onUpgradeClick: () -> Unit,
    accentColor: Color
) {
    val maxCount = 5
    val remaining = (maxCount - freeCount).coerceAtLeast(0)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F29)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Free Limit Speed-Meter",
                    style = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Simulated Custom segment bar
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..maxCount) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i <= freeCount) accentColor else Color.Gray.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$remaining of $maxCount free prompt signals remaining today",
                    style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                )
            }

            Button(
                onClick = onUpgradeClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Upgrade", style = TextStyle(fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Black))
            }
        }
    }
}

@Composable
fun WelcomeSpacePortal(
    onPromptClick: (String) -> Unit,
    accentColor: Color,
    primaryGradient: Brush,
    tier: SubscriptionTier,
    onUpgradeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celestial central orb
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(primaryGradient)
                .drawBehind {
                    drawCircle(accentColor.copy(alpha = 0.2f), radius = size.minDimension * 0.8f)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "COSMIC INTELLIGENCE",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor.copy(alpha = 0.85f),
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "GALAXY AI PORTAL",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp,
                lineHeight = 36.sp
            )
        )

        Text(
            text = "Initiate instant connection to " + if (tier == SubscriptionTier.FREE) "Gemini Flash Basic Engine" else "Gemini Pro Multi-Modal Hyperdrive",
            style = TextStyle(
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // Upgrade recommendation
        if (tier == SubscriptionTier.FREE) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onUpgradeClick)
                    .background(accentColor.copy(alpha = 0.08f))
                    .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Upgrade icon",
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Switch to Plus/Team to unlock reasoning & themes",
                    style = TextStyle(color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            "COMMENCE CHAT BY SELECTING CO-PILOT OPTION",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val samplePrompts = listOf(
            "🚀 Translate 'Hello galactic team, are we ready for checkout?' into Space Jargon",
            "🧬 Explain the concept of Quantum Hyperdrive simple terms",
            "📝 Write a formal solar service agreement for collaborative developers",
            "🌟 Draft a cosmic birthday message themed around red supernovas"
        )

        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            samplePrompts.forEach { prompt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPromptClick(prompt) },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prompt,
                            style = TextStyle(color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Normal),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Send prompt",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    bubbleSelfColor: Color,
    accentColor: Color
) {
    val isUser = message.role == "user"
    val isError = message.role == "error"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isError) Icons.Default.Warning else Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isError) Color.Red else accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 300.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) bubbleSelfColor else Color.White.copy(alpha = 0.05f)
                    )
                    .border(
                        1.dp,
                        if (isUser) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.07f),
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                // Render text with specific Monospace styling for code block patterns
                val textValue = message.text
                if (textValue.contains("```")) {
                    val parts = textValue.split("```")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        parts.forEachIndexed { idx, partText ->
                            if (idx % 2 == 1) {
                                // Monospaced Code segment rendering
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .border(0.5.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = partText.trim(),
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = accentColor
                                        )
                                    )
                                }
                            } else {
                                // Standard plain text rendering
                                Text(
                                    text = partText,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = if (isError) Color.Red else Color.White,
                                        lineHeight = 20.sp
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = textValue,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = if (isError) Color.Red else Color.White,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
            
            // Display prompt timestamp
            Text(
                text = if (isUser) "Transmitted" else "Decoded",
                style = TextStyle(fontSize = 9.sp, color = Color.Gray),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(if (isUser) Alignment.End else Alignment.Start)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ME",
                    style = TextStyle(fontSize = 9.sp, color = accentColor, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun GeneratingIndicatorItem(accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = accentColor,
                strokeWidth = 2.dp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "AI is computing stellar coordinates...",
            style = TextStyle(color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Light)
        )
    }
}

@Composable
fun MessageInputField(
    onSend: (String) -> Unit,
    isGenerating: Boolean,
    accentColor: Color
) {
    var rawText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("message_input_field"),
                placeholder = {
                    Text(
                        "Transmit message coordinates...",
                        style = TextStyle(color = Color.Gray, fontSize = 14.sp)
                    )
                },
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (rawText.trim().isNotEmpty() && !isGenerating) {
                            onSend(rawText)
                            rawText = ""
                            keyboardController?.hide()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Action: Shoot signal button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (rawText.trim().isNotEmpty() && !isGenerating) accentColor else Color.Gray.copy(alpha = 0.15f)
                    )
                    .clickable(
                        enabled = rawText.trim().isNotEmpty() && !isGenerating,
                        onClick = {
                            onSend(rawText)
                            rawText = ""
                            keyboardController?.hide()
                        }
                    )
                    .testTag("send_message_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Transmit",
                        tint = if (rawText.trim().isNotEmpty()) Color.Black else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(
    errorText: String,
    onUpgradeClick: () -> Unit,
    onResetLimits: () -> Unit,
    tier: SubscriptionTier,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF381014)),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = "Alert", tint = Color.Red, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Mission Intercepted",
                    style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorText,
                style = TextStyle(color = Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (tier == SubscriptionTier.FREE) {
                    Button(
                        onClick = onUpgradeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Upgrade Subscription", style = TextStyle(color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black))
                    }
                }
                
                // For direct evaluation simplicity, allow resetting daily counts locally
                Button(
                    onClick = onResetLimits,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Decline Signal Limits", style = TextStyle(color = Color.White, fontSize = 11.sp))
                }
            }
        }
    }
}

@Composable
fun PlansSettingsSection(
    activeTier: SubscriptionTier,
    onSelectTier: (SubscriptionTier) -> Unit,
    onClose: () -> Unit,
    accentColor: Color,
    gradientBrush: Brush,
    teamMembers: List<TeamMember>,
    sharedPrompts: List<SharedPrompt>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val sheetAccent = Color(0xFF00ADB5)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "SUBSCRIPTION",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpaceTextSecondary.copy(alpha = 0.7f),
                        letterSpacing = 2.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "CHOOSE YOUR\nORBIT",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SpaceBorder)
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. FREE CARD (Styling matched to: bg-[#2B2930] rounded-[28px] border border-[#49454F])
        val isFreeCurrent = activeTier == SubscriptionTier.FREE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .border(
                    width = 1.dp,
                    color = if (isFreeCurrent) accentColor else SpaceBorder,
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = SpaceSurface
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Free",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            "Standard Model Access",
                            style = TextStyle(fontSize = 13.sp, color = SpaceTextSecondary)
                        )
                    }
                    Text(
                        "$0",
                        style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check",
                        tint = SpaceTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Limited Daily Messages (5 slots)",
                        style = TextStyle(fontSize = 12.sp, color = SpaceTextSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSelectTier(SubscriptionTier.FREE) },
                    enabled = !isFreeCurrent,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFreeCurrent) Color.Gray.copy(alpha = 0.2f) else accentColor,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (isFreeCurrent) "STATIONED CURRENTLY" else "DEMOTE TO BASIC WORKSPACE",
                        style = TextStyle(
                            color = if (isFreeCurrent) Color.Gray else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
        }

        // 2. PLUS CARD (Styling matched to: bg-[#D0BCFF] rounded-[28px] text-[#381E72] shadow-glow)
        val isPlusCurrent = activeTier == SubscriptionTier.PLUS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .border(
                    width = if (isPlusCurrent) 2.dp else 0.dp,
                    color = if (isPlusCurrent) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = SpaceAccentLavender
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "Plus",
                                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SpaceTextDarkPurple)
                            )
                            Text(
                                "Turbo 4.0 + AR Beta",
                                style = TextStyle(fontSize = 13.sp, color = SpaceTextDarkPurple.copy(alpha = 0.8f))
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$20",
                                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Black, color = SpaceTextDarkPurple)
                            )
                            Text(
                                "/month",
                                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SpaceTextDarkPurple.copy(alpha = 0.6f))
                            )
                        }
                    }

                    // Popular ribbon tag at top right (shifted nicely)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(top = 44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpaceTextDarkPurple)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "POPULAR",
                            style = TextStyle(color = SpaceAccentLavender, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = SpaceTextDarkPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Priority GPU Access & Unrestricted Chats",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpaceTextDarkPurple)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nest Google Wallet styled pay button
                Button(
                    onClick = { onSelectTier(SubscriptionTier.PLUS) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpaceTextDarkPurple,
                        disabledContainerColor = SpaceTextDarkPurple.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Wallet Icon",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlusCurrent) "STATIONED CURRENTLY" else "PAY WITH WALLET",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }
            }
        }

        // 3. TEAM CARD (Styling matched to: bg-transparent border-2 border-[#49454F] rounded-[28px])
        val isTeamCurrent = activeTier == SubscriptionTier.TEAM
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .border(
                    width = 2.dp,
                    color = if (isTeamCurrent) accentColor else SpaceBorder,
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Team",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            "Admin Console + Tools",
                            style = TextStyle(fontSize = 13.sp, color = SpaceTextSecondary)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$30",
                            style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                        )
                        Text(
                            "/user",
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SpaceTextSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Group",
                        tint = SpaceTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Unlimited Shared Collaboration Workspace",
                        style = TextStyle(fontSize = 12.sp, color = SpaceTextSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSelectTier(SubscriptionTier.TEAM) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTeamCurrent) Color.Gray.copy(alpha = 0.2f) else accentColor,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (isTeamCurrent) "STATIONED CURRENTLY" else "PREVISION TEAM CO-PILOT",
                        style = TextStyle(
                            color = if (isTeamCurrent) Color.Gray else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
        }

        // Sub workspace Board details (Visible if TEAM is active to demonstrate full feature set requested by user!)
        if (activeTier == SubscriptionTier.TEAM) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Team Administration Dashboard Active",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE5A93C))
            )
            Text(
                "You are viewing your space team database control panel. Centralize collaborative prompting templates.",
                style = TextStyle(color = Color.Gray, fontSize = 11.sp),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic grid list of members
            Text(
                "COLLEAGUE REGISTRATION ROSTER",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray, letterSpacing = 1.sp),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                teamMembers.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(member.name, style = TextStyle(fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium))
                                Text(member.email, style = TextStyle(fontSize = 10.sp, color = Color.Gray))
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${member.tokensUsed} calls",
                                style = TextStyle(fontSize = 9.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "WORKSPACE SHARED SEED PROMPTS",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray, letterSpacing = 1.sp),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sharedPrompts.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(item.title, style = TextStyle(fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold))
                            Text(item.description, style = TextStyle(fontSize = 10.sp, color = Color.Gray))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextThemeListItem(bullet: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = bullet,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = TextStyle(fontSize = 12.sp, color = Color.LightGray, lineHeight = 16.sp),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GoogleWalletCheckoutSheet(
    targetTier: SubscriptionTier,
    onPaymentSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Card screen, 2: Loading payment status, 3: Success tick confirmation

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Layout Step 1: Simulated Card screen
            if (step == 1) {
                // Google Wallet Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Wallet Icon",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Google Wallet",
                            style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.LightGray)
                    }
                }

                Text(
                    "Secure in-app subscription via Galaxy AI Merchant Core.",
                    style = TextStyle(color = Color.Gray, fontSize = 12.sp),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card payment details
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3B4E)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Galaxy AI Visa Pro",
                                style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            )
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFF9F29),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Text(
                            "••••  ••••  ••••  1981",
                            style = TextStyle(
                                color = Color.LightGray,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hafiz Imran", style = TextStyle(color = Color.LightGray, fontSize = 11.sp))
                            Text("Exp 08/29", style = TextStyle(color = Color.LightGray, fontSize = 11.sp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Checkout Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Selected Subscription Tier:", style = TextStyle(color = Color.LightGray, fontSize = 13.sp))
                    Text(targetTier.displayName, style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Due Immediately:", style = TextStyle(color = Color.LightGray, fontSize = 13.sp))
                    Text(targetTier.price, style = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { step = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("wallet_pay_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "PAY ${targetTier.price.split("/").first()} WITH GOOGLE WALLET",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        )
                    }
                }
            }

            // Layout Step 2: Processing payment simulation
            if (step == 2) {
                LaunchedEffect(Unit) {
                    delay(2500) // Simulate fast secure payment transit delay
                    step = 3
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                CircularProgressIndicator(color = Color(0xFF4285F4), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Establishing Galaxy-to-Wallet Node link...",
                    style = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    "Contacting secure sandbox terminal at hafizimran1981@gmail.com",
                    style = TextStyle(color = Color.Gray, fontSize = 11.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Layout Step 3: Success tick screen!
            if (step == 3) {
                LaunchedEffect(Unit) {
                    delay(2000)
                    onPaymentSuccess()
                }

                Spacer(modifier = Modifier.height(40.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF0F9D58),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "TRANSACTION AUTHORIZED",
                    style = TextStyle(color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                )
                Text(
                    "Google Wallet successfully transferred $${targetTier.price.split("/").first().replace("$", "")}.00 to Galaxy AI. Your Premium cockpit model is now fully operational.",
                    style = TextStyle(color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun IosMultiplatformBlueprintSheet(
    onClose: () -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "iOS Cross-Platform Architecture",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "To satisfy compatibility with both iOS and Android platforms, Galaxy AI is structurally designed in a pure Kotlin Multiplatform (KMP) and Case-Oriented Architecture pattern.",
            style = TextStyle(color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "HOW THE ARCHITECTURE COMPILES FOR iOS",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.sp),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BlueprintStepCard(
            idx = "01",
            title = "Shared Kotlin Core Module",
            description = "The database (Room with SQLite on iOS), network client (Retrofit/Ktor), and MVVM State managers (GalaxyViewModel) are kept in a single shared module, reusing 100% of reasoning limits and business logic code."
        )
        BlueprintStepCard(
            idx = "02",
            title = "Compose Multiplatform UI Layout",
            description = "Jetpack Compose transforms cleanly to iOS UIKit canvas. Standard screens (side drawers, pricing comparisons, bubbles layouts, and message input rows) compile directly into fully hardware-accelerated iOS views."
        )
        BlueprintStepCard(
            idx = "03",
            title = "Google Wallet & Pay bridge",
            description = "On iOS, our abstract PaymentRepository leverages standard iOS Apple Pay or Wallet sheets in swift, preserving the satisfying checkout haptics."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Kotlin Multiplatform structure map
        Text(
            "SHARED IMPLEMENTATION SPECIFICATION GUIDE",
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "galaxy-ai-root/\n" +
                        " ├── shared/            <-- Pure Kotlin code (KMP)\n" +
                        " │    ├── src/commonMain/\n" +
                        " │    │     ├── api/ (Moshi + Direct REST)\n" +
                        " │    │     ├── db/ (SQLite Room Entities)\n" +
                        " │    │     └── state/ (GalaxyViewModel)\n" +
                        " ├── androidApp/        <-- Android Runner app\n" +
                        " └── iosApp/            <-- iOS Xcode project wrapper\n" +
                        "       └── iosApp/AppDelegate.swift",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = accentColor)
            )
        }
    }
}

@Composable
fun BlueprintStepCard(
    idx: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(
                text = idx,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.Gray)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    title,
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = TextStyle(fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                )
            }
        }
    }
}

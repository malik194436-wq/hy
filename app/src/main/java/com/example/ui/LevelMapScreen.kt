package com.example.ui

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.audio.SoundManager
import com.example.data.GameStorage
import com.example.model.LevelManager
import com.example.model.LevelNodeData
import com.example.model.LevelStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private val RussoOneFontFamily = FontFamily(
    Font(R.font.russo_one, FontWeight.Normal)
)

/**
 * Returns horizontal fraction (0.18f .. 0.82f) for level L along a graceful serpentine winding path.
 */
private fun getPathXFraction(level: Int): Float {
    val wave = sin((level - 1) * 0.92f) * 0.28f + cos((level - 1) * 0.35f) * 0.06f
    return (0.50f + wave).coerceIn(0.18f, 0.82f)
}

@Composable
fun LevelMapScreen(
    storage: GameStorage,
    soundManager: SoundManager,
    onStartStage: (Int) -> Unit,
    onBack: () -> Unit = {},
    onStartClassic: () -> Unit = {},
    onStartRush: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onOpenBallSkins: () -> Unit = {}
) {
    BackHandler {
        onBack()
    }

    val levelManager = remember { LevelManager(storage, totalLevels = 5000, levelsPerChapter = 6) }
    val totalLevels = levelManager.totalLevels
    val highestUnlocked = levelManager.highestUnlockedLevel

    var selectedStage by remember { mutableStateOf<Int?>(null) }
    var lockedPromptMessage by remember { mutableStateOf<String?>(null) }
    var showAllLevelsModal by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // LazyColumn with reverseLayout = true: index 0 (Level 1) is at the bottom,
    // and higher levels (2, 3... 5000) wind upwards toward the cosmos!
    val listState = rememberLazyListState()

    // Scroll to current active stage when screen first opens
    LaunchedEffect(highestUnlocked) {
        val targetIndex = (highestUnlocked - 1).coerceIn(0, totalLevels - 1)
        listState.scrollToItem(targetIndex, scrollOffset = -220)
    }

    // Infinite ambient animations for living cosmos
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_ambient")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val energyFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "energyFlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070014))
            .testTag("level_selection_screen")
    ) {
        // 1. User Video Background (Aurora Lake with Mountains & Crescent Moon)
        LevelMapVideoBackground()

        // 2. The Unified Continuous Vertical Winding Map
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val rowHeight = 135.dp

            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("continuous_level_list"),
                contentPadding = PaddingValues(top = 90.dp, bottom = 100.dp)
            ) {
                items(
                    count = totalLevels,
                    key = { index -> index + 1 }
                ) { index ->
                    val levelNumber = index + 1
                    val nodeData = remember(levelNumber, highestUnlocked) {
                        levelManager.getLevelData(levelNumber)
                    }

                    CosmicLevelRow(
                        nodeData = nodeData,
                        highestUnlocked = highestUnlocked,
                        totalLevels = totalLevels,
                        screenWidth = screenWidth,
                        rowHeight = rowHeight,
                        pulseAlpha = pulseAlpha,
                        energyFlow = energyFlow,
                        onNodeClick = {
                            if (nodeData.status != LevelStatus.LOCKED) {
                                soundManager.playClickSound()
                                selectedStage = levelNumber
                            } else {
                                soundManager.playClickSound()
                                lockedPromptMessage = "Level $levelNumber is locked! Clear Level ${levelNumber - 1} first."
                                scope.launch {
                                    delay(2200)
                                    if (lockedPromptMessage != null) {
                                        lockedPromptMessage = null
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // 3. Floating Bottom-Right "Current Stage" Quick-Snap Button
            val isAwayFromCurrent by remember {
                derivedStateOf {
                    val currentIndex = highestUnlocked - 1
                    val firstVisible = listState.firstVisibleItemIndex
                    val lastVisible = firstVisible + listState.layoutInfo.visibleItemsInfo.size
                    currentIndex !in (firstVisible - 1)..(lastVisible + 1)
                }
            }

            AnimatedVisibility(
                visible = isAwayFromCurrent,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 26.dp)
            ) {
                Surface(
                    onClick = {
                        soundManager.playClickSound()
                        scope.launch {
                            val targetIndex = (highestUnlocked - 1).coerceIn(0, totalLevels - 1)
                            listState.animateScrollToItem(targetIndex, scrollOffset = -220)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xEE16032E),
                    border = BorderStroke(2.dp, Color(0xFFFFD700).copy(alpha = pulseAlpha)),
                    shadowElevation = 16.dp,
                    modifier = Modifier.testTag("snap_to_current_level_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Current Level",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "CURRENT",
                                color = Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "STAGE $highestUnlocked",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = RussoOneFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 4. Minimal Floating Back Button (Top Bar Header removed per user request)
            IconButton(
                onClick = {
                    soundManager.playClickSound()
                    onBack()
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 10.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x660E021C))
                    .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), CircleShape)
                    .testTag("level_select_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 5. Toast Prompt for Locked Levels
            AnimatedVisibility(
                visible = lockedPromptMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFA1E0533),
                    border = BorderStroke(2.dp, Color(0xFFE040FB)),
                    shadowElevation = 24.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFF26E2),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = lockedPromptMessage ?: "",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 6. Stage Details Modal
            selectedStage?.let { stage ->
                val stageData = remember(stage) { levelManager.getLevelData(stage) }
                StageDetailsModal(
                    stageData = stageData,
                    pulseAlpha = pulseAlpha,
                    onDismiss = { selectedStage = null },
                    onGo = {
                        soundManager.playClickSound()
                        selectedStage = null
                        onStartStage(stageData.levelNumber)
                    }
                )
            }

            // 7. Full 5000 Stages Jump Browser
            if (showAllLevelsModal) {
                AllStagesModal(
                    levelManager = levelManager,
                    onSelectStage = { stage ->
                        soundManager.playClickSound()
                        showAllLevelsModal = false
                        selectedStage = stage
                        scope.launch {
                            val targetIndex = (stage - 1).coerceIn(0, totalLevels - 1)
                            listState.animateScrollToItem(targetIndex, scrollOffset = -220)
                        }
                    },
                    onDismiss = {
                        showAllLevelsModal = false
                    }
                )
            }
        }
    }
}

/**
 * Single Row in the Winding Cosmic Road:
 * Renders the glowing connector lightning curves to adjacent levels and the luxury 3D crystal island node.
 */
@Composable
private fun CosmicLevelRow(
    nodeData: LevelNodeData,
    highestUnlocked: Int,
    totalLevels: Int,
    screenWidth: Dp,
    rowHeight: Dp,
    pulseAlpha: Float,
    energyFlow: Float,
    onNodeClick: () -> Unit
) {
    val level = nodeData.levelNumber
    val isCurrent = level == highestUnlocked
    val isCompleted = nodeData.status == LevelStatus.COMPLETED
    val isUnlocked = nodeData.status != LevelStatus.LOCKED

    val curFraction = remember(level) { getPathXFraction(level) }
    val prevFraction = remember(level) { if (level > 1) getPathXFraction(level - 1) else curFraction }
    val nextFraction = remember(level) { if (level < totalLevels) getPathXFraction(level + 1) else curFraction }

    val isMilestone = level % 10 == 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isMilestone) rowHeight + 46.dp else rowHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Milestone Arch / Banner (Every 10 levels)
        if (isMilestone) {
            MilestoneChapterBanner(level = level, isUnlocked = isUnlocked)
            Spacer(modifier = Modifier.height(6.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
        ) {
            // Cosmic Energy River / Lightning Path Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val curX = w * curFraction
                val curY = h * 0.5f

                val prevX = w * prevFraction
                val nextX = w * nextFraction

                // 1. Connection coming from previous level (below, y = h) to this node (center)
                if (level > 1) {
                    val pathIn = Path().apply {
                        moveTo(prevX, h)
                        cubicTo(
                            prevX, h * 0.80f,
                            curX, h * 0.70f,
                            curX, curY
                        )
                    }

                    val inUnlocked = isUnlocked

                    // Outer ambient glow
                    drawPath(
                        path = pathIn,
                        color = if (inUnlocked) Color(0xFF00E5FF).copy(alpha = 0.22f) else Color(0xFF7C4DFF).copy(alpha = 0.12f),
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Core neon laser line
                    drawPath(
                        path = pathIn,
                        brush = Brush.verticalGradient(
                            colors = if (inUnlocked) {
                                listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFF00E5FF))
                            } else {
                                listOf(Color(0xFF4A148C).copy(alpha = 0.6f), Color(0xFF311B92).copy(alpha = 0.4f))
                            },
                            startY = curY,
                            endY = h
                        ),
                        style = Stroke(width = 3.6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Floating energy pulse dot
                    if (inUnlocked) {
                        val t = energyFlow
                        val dotX = prevX + (curX - prevX) * t
                        val dotY = h + (curY - h) * t
                        drawCircle(
                            color = Color.White,
                            radius = 3.2.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                            radius = 6.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                // 2. Connection going to next level (above, y = 0) from this node (center)
                if (level < totalLevels) {
                    val pathOut = Path().apply {
                        moveTo(curX, curY)
                        cubicTo(
                            curX, h * 0.30f,
                            nextX, h * 0.20f,
                            nextX, 0f
                        )
                    }

                    val nextUnlocked = (level + 1) <= highestUnlocked

                    // Outer ambient glow
                    drawPath(
                        path = pathOut,
                        color = if (nextUnlocked) Color(0xFF00E5FF).copy(alpha = 0.22f) else Color(0xFF7C4DFF).copy(alpha = 0.12f),
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Core neon laser line
                    drawPath(
                        path = pathOut,
                        brush = Brush.verticalGradient(
                            colors = if (nextUnlocked) {
                                listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                            } else {
                                listOf(Color(0xFF311B92).copy(alpha = 0.4f), Color(0xFF1A0836).copy(alpha = 0.2f))
                            },
                            startY = 0f,
                            endY = curY
                        ),
                        style = Stroke(width = 3.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // The Luxury 3D Crystal Island & Gem Orb
            val nodeSize = 78.dp
            val nodeCenterX = screenWidth * curFraction

            Box(
                modifier = Modifier
                    .offset(x = nodeCenterX - (nodeSize / 2), y = (rowHeight - nodeSize) / 2)
                    .size(nodeSize),
                contentAlignment = Alignment.Center
            ) {
                CosmicLevelOrbNode(
                    nodeData = nodeData,
                    isCurrent = isCurrent,
                    isCompleted = isCompleted,
                    isUnlocked = isUnlocked,
                    pulseAlpha = pulseAlpha,
                    nodeSize = nodeSize,
                    onClick = onNodeClick
                )
            }
        }
    }
}

/**
 * Luxury 3D Crystal Gem Orb with Holographic Cyber Platform Base and Stylish Emblems.
 * Fully dynamic for all 5000 levels.
 */
@Composable
private fun CosmicLevelOrbNode(
    nodeData: LevelNodeData,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    pulseAlpha: Float,
    nodeSize: Dp,
    onClick: () -> Unit
) {
    val level = nodeData.levelNumber
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "orbPress"
    )

    Box(
        modifier = Modifier
            .size(nodeSize)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Futuristic Holographic Cyber Platform Base (Replaces the rough dark rock blob)
        Canvas(
            modifier = Modifier
                .size(nodeSize * 1.45f)
                .offset(y = 16.dp)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Soft atmospheric ground glow under the platform
            val glowColor = when {
                isCurrent -> Color(0xFFFFD700).copy(alpha = 0.35f * pulseAlpha)
                isCompleted -> Color(0xFF00E5FF).copy(alpha = 0.30f)
                isUnlocked -> Color(0xFF7C4DFF).copy(alpha = 0.25f)
                else -> Color(0xFFE040FB).copy(alpha = 0.15f)
            }
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = Offset(cx, cy + h * 0.22f),
                    radius = w * 0.45f
                ),
                topLeft = Offset(cx - w * 0.45f, cy + h * 0.05f),
                size = Size(w * 0.90f, h * 0.34f)
            )

            // Sleek Metallic Cyber Disk Platform
            drawOval(
                brush = Brush.verticalGradient(
                    colors = when {
                        isCurrent -> listOf(Color(0xFF4A148C), Color(0xFF280B45), Color(0xFF0F001C))
                        isCompleted -> listOf(Color(0xFF004D40), Color(0xFF002244), Color(0xFF050014))
                        isUnlocked -> listOf(Color(0xFF1E0B38), Color(0xFF100220), Color(0xFF070010))
                        else -> listOf(Color(0xFF1A0A2E), Color(0xFF0D0218), Color(0xFF05000C))
                    }
                ),
                topLeft = Offset(cx - w * 0.36f, cy + h * 0.14f),
                size = Size(w * 0.72f, h * 0.22f)
            )

            // Outer Neon Rim of the platform
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = when {
                        isCurrent -> listOf(Color(0xFFFFD700), Color(0xFFFF6D00), Color(0xFFFFD700))
                        isCompleted -> listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFF00E5FF))
                        isUnlocked -> listOf(Color(0xFF00E5FF).copy(alpha = 0.8f), Color(0xFFE040FB).copy(alpha = 0.8f))
                        else -> listOf(Color(0xFF7C4DFF).copy(alpha = 0.6f), Color(0xFFE040FB).copy(alpha = 0.4f), Color(0xFF7C4DFF).copy(alpha = 0.6f))
                    }
                ),
                topLeft = Offset(cx - w * 0.36f, cy + h * 0.14f),
                size = Size(w * 0.72f, h * 0.22f),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // Cyber Energy Emitter Dots (Left and Right)
            val dotRadius = 2.5.dp.toPx()
            val dotColor = when {
                isCurrent -> Color(0xFFFFD700)
                isCompleted -> Color(0xFF00E5FF)
                isUnlocked -> Color(0xFFE040FB)
                else -> Color(0xFF7C4DFF).copy(alpha = 0.7f)
            }
            drawCircle(dotColor, dotRadius, Offset(cx - w * 0.30f, cy + h * 0.24f))
            drawCircle(dotColor, dotRadius, Offset(cx + w * 0.30f, cy + h * 0.24f))
        }

        // Active Level: Multi-layered Golden Energy Halo
        if (isCurrent) {
            Canvas(modifier = Modifier.size(nodeSize * 1.55f)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.60f * pulseAlpha),
                            Color(0xFFE040FB).copy(alpha = 0.30f * pulseAlpha),
                            Color.Transparent
                        )
                    ),
                    radius = size.width / 2f
                )
            }
        }

        // 3D Glass Crystal Orb Body
        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .size(nodeSize * 0.90f)
                .clip(CircleShape)
                .border(
                    width = if (isCurrent) 3.dp else 2.4.dp,
                    brush = when {
                        isCurrent -> Brush.linearGradient(
                            listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFFF6D00), Color(0xFFFF26E2))
                        )
                        isCompleted -> Brush.linearGradient(
                            listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFE040FB))
                        )
                        else -> Brush.linearGradient(
                            listOf(Color(0xFFE040FB).copy(alpha = 0.75f), Color(0xFF7C4DFF), Color(0xFF00E5FF).copy(alpha = 0.5f))
                        )
                    },
                    shape = CircleShape
                )
                .testTag("level_orb_$level"),
            color = Color.Transparent,
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = when {
                                isCurrent -> listOf(
                                    Color(0xFFFFB300),
                                    Color(0xFFFF6D00),
                                    Color(0xFF880E4F),
                                    Color(0xFF280B45)
                                )
                                isCompleted -> listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF0277BD),
                                    Color(0xFF311B92),
                                    Color(0xFF0A001A)
                                )
                                else -> listOf(
                                    Color(0xFF3D1063),
                                    Color(0xFF23083B),
                                    Color(0xFF0E0118)
                                )
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Top Specular Curved Glass Highlight (gives authentic 3D sphere depth)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(nodeSize * 0.28f)
                        .align(Alignment.TopCenter)
                        .offset(y = 3.dp)
                ) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.60f),
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        ),
                        cornerRadius = CornerRadius(20f, 20f)
                    )
                }

                // Inner content: Stylish Number + Stylish Logo/Emblem
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // STYLISH NUMBERING
                    Text(
                        text = "$level",
                        fontFamily = RussoOneFontFamily,
                        fontSize = when {
                            level >= 1000 -> 15.sp
                            level >= 100 -> 18.5.sp
                            else -> 23.sp
                        },
                        fontWeight = FontWeight.Normal,
                        color = when {
                            isCurrent -> Color(0xFFFFFFFF)
                            isCompleted -> Color(0xFFFFFFFF)
                            else -> Color(0xFFEDE7F6)
                        },
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(
                                color = when {
                                    isCurrent -> Color(0xFFD50000).copy(alpha = 0.9f)
                                    isCompleted -> Color(0xFF002244).copy(alpha = 0.9f)
                                    else -> Color.Black.copy(alpha = 0.95f)
                                },
                                offset = Offset(0f, 2.5f),
                                blurRadius = 5f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // STYLISH LOGO / EMBLEM FOR ALL LEVELS
                    LevelStatusEmblem(
                        isCurrent = isCurrent,
                        isCompleted = isCompleted,
                        isUnlocked = isUnlocked,
                        pulseAlpha = pulseAlpha
                    )
                }
            }
        }

        // Active Level: Royal Jeweled Golden Crown perched atop the orb!
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-14).dp)
            ) {
                Text(
                    text = "👑",
                    fontSize = 24.sp,
                    modifier = Modifier.graphicsLayer {
                        shadowElevation = 8f
                    }
                )
            }

            // "PLAY" pill tag below current orb
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFD700),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 13.dp)
                    .shadow(10.dp, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFFFF9C4), RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = "PLAY",
                    color = Color(0xFF210038),
                    fontSize = 9.sp,
                    fontFamily = RussoOneFontFamily,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Completed Level: Stars (1 to 3) displayed under the orb in a glossy badge
        if (isCompleted && nodeData.stars > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 11.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE0D021D))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (s in 1..3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (s <= nodeData.stars) Color(0xFFFFD700) else Color(0x33FFFFFF),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Stylish custom Emblem / Logo displayed inside each level orb according to its status:
 * - Locked: Cybernetic security badge with glowing neon padlock and dual-tone gradient rim.
 * - Completed: Golden star insignia with diamond glow.
 * - Current: Radiant energy star badge with golden aura.
 */
@Composable
private fun LevelStatusEmblem(
    isCurrent: Boolean,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    pulseAlpha: Float
) {
    when {
        isCurrent -> {
            // Radiant Energy Spark Emblem for Active Level
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0x66FFD700))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = pulseAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Active Stage",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
        isCompleted -> {
            // Shiny Golden Star Badge for Cleared Levels
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0x5500E5FF))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Completed Stage",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
        else -> {
            // Stylish High-Tech Cyber Lock Shield Logo for Locked Levels
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2D0747), Color(0xFF140222))
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFFE040FB))
                        ),
                        shape = RoundedCornerShape(5.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Stage",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(10.5.dp)
                )
            }
        }
    }
}

/**
 * Milestone Banner every 10 levels.
 */
@Composable
private fun MilestoneChapterBanner(
    level: Int,
    isUnlocked: Boolean
) {
    val chapter = (level / 10)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isUnlocked) Color(0xEE1E0538) else Color(0xBB110221),
        border = BorderStroke(
            1.2.dp,
            if (isUnlocked) Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFFFD700), Color(0xFFE040FB)))
            else Brush.horizontalGradient(listOf(Color(0x447C4DFF), Color(0x22311B92)))
        ),
        shadowElevation = 8.dp,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isUnlocked) "🌟" else "🔒",
                fontSize = 12.sp
            )
            Text(
                text = "GALAXY $chapter MILESTONE • STAGE $level",
                color = if (isUnlocked) Color(0xFFFFD700) else Color.Gray,
                fontSize = 10.5.sp,
                fontFamily = RussoOneFontFamily,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "🎁 +50 BALLS",
                color = Color(0xFF00E5FF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



/**
 * Looping Edge-to-Edge Video Background using the user-provided aurora lake video.
 * Uses TextureView for smooth hardware-accelerated Compose rendering and full screen crop.
 */
@Composable
private fun LevelMapVideoBackground(modifier: Modifier = Modifier) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Throwable) {
                // Ignore cleanup errors
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                var videoWidth = 0
                var videoHeight = 0

                fun updateTransform(view: TextureView) {
                    val viewWidth = view.width
                    val viewHeight = view.height
                    if (viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) return

                    val viewRatio = viewWidth.toFloat() / viewHeight.toFloat()
                    val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()

                    val scaleX: Float
                    val scaleY: Float

                    if (viewRatio > videoRatio) {
                        scaleX = 1f
                        scaleY = (viewWidth.toFloat() / videoWidth.toFloat()) / (viewHeight.toFloat() / videoHeight.toFloat())
                    } else {
                        scaleX = (viewHeight.toFloat() / videoHeight.toFloat()) / (viewWidth.toFloat() / videoWidth.toFloat())
                        scaleY = 1f
                    }

                    val matrix = Matrix()
                    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                    view.setTransform(matrix)
                }

                val textureView = TextureView(ctx)
                textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                        try {
                            mediaPlayer?.release()
                            val player = MediaPlayer().apply {
                                setSurface(Surface(surface))
                                val uri = Uri.parse("android.resource://" + ctx.packageName + "/" + R.raw.level_map_bg)
                                setDataSource(ctx, uri)
                                isLooping = true
                                setVolume(0f, 0f)
                                setOnVideoSizeChangedListener { _, w, h ->
                                    if (w > 0 && h > 0) {
                                        videoWidth = w
                                        videoHeight = h
                                        updateTransform(textureView)
                                    }
                                }
                                setOnPreparedListener { mp ->
                                    videoWidth = mp.videoWidth
                                    videoHeight = mp.videoHeight
                                    updateTransform(textureView)
                                    mp.start()
                                }
                                prepareAsync()
                            }
                            mediaPlayer = player
                        } catch (e: Throwable) {
                            android.util.Log.e("LevelMapVideoBg", "Error preparing background video", e)
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                        updateTransform(textureView)
                    }

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        try {
                            mediaPlayer?.release()
                            mediaPlayer = null
                        } catch (e: Throwable) {
                            // Ignore cleanup errors
                        }
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
                textureView
            },
            update = {
                mediaPlayer?.let { mp ->
                    if (!mp.isPlaying) {
                        try {
                            mp.start()
                        } catch (e: Throwable) {
                            // Ignore resume errors
                        }
                    }
                }
            }
        )

        // Subtle dark translucent ambient overlay to ensure level nodes and text pop with vibrant contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x35070014),
                            Color(0x18000000),
                            Color(0x40070014)
                        )
                    )
                )
        )
    }
}

/**
 * Stage Start Dialog displaying the futuristic sci-fi cup/mug artwork with interactive controls.
 */
@Composable
private fun StageDetailsModal(
    stageData: LevelNodeData,
    pulseAlpha: Float,
    onDismiss: () -> Unit,
    onGo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer Card Container with glowing cyber border
        Box(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .widthIn(max = 390.dp)
                .shadow(
                    elevation = 36.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0xFF00E5FF).copy(alpha = 0.5f),
                    spotColor = Color(0xFFE040FB)
                )
                .clip(RoundedCornerShape(32.dp))
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF7C4DFF),
                            Color(0xFFE040FB),
                            Color(0xFFFF26E2),
                            Color(0xFF00E5FF)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .background(Color(0xFF070418))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Don't dismiss when clicking card body
                )
        ) {
            // 1. New Cosmic Stage Portal Background Artwork
            Image(
                painter = painterResource(id = R.drawable.img_stage_portal_card),
                contentDescription = "Cosmic Stage Portal",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = 0.82f }
            )

            // 2. Cinematic Gradient Scrim for perfect contrast and readability
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xDD070216),
                                Color(0x77070216),
                                Color(0xCC050212),
                                Color(0xF5040110)
                            )
                        )
                    )
            )

            // 3. Dynamic Stage Card Content Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: Chapter Badge on Left, Close "X" Button on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chapter Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0E0326).copy(alpha = 0.85f),
                        border = BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "CHAPTER ${stageData.chapterNumber}",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC0A0520))
                            .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.8f), CircleShape)
                            .clickable(onClick = onDismiss)
                            .testTag("stage_dialog_close_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stage Number Title
                Text(
                    text = "STAGE ${stageData.levelNumber}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontFamily = RussoOneFontFamily,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF00E5FF),
                            offset = Offset(0f, 0f),
                            blurRadius = 18f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Difficulty Title
                Text(
                    text = stageData.difficultyTitle.uppercase(),
                    color = Color(0xFFFF26E2),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFFFF26E2).copy(alpha = 0.8f),
                            blurRadius = 12f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Stars Display (Glowing Stars Earned)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val earned = stageData.stars >= i
                        Box(
                            modifier = Modifier
                                .size(if (i == 2) 44.dp else 36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (earned) Color(0x33FFD600) else Color(0x2212002E)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (earned) Color(0xFFFFD600) else Color(0x55B388FF),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (earned) Color(0xFFFFD600) else Color(0x44B388FF),
                                modifier = Modifier.size(if (i == 2) 28.dp else 22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mission Stats Capsules (Starting Balls & Target Turns)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Balls Capsule
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xDD09031E),
                        border = BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BALLS",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${stageData.startingBalls}",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontFamily = RussoOneFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Target Turns Capsule
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xDD09031E),
                        border = BorderStroke(1.2.dp, Color(0xFFE040FB).copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TARGET",
                                color = Color(0xFFE040FB),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "≤ ${stageData.targetTurnsFor3Stars} TURNS",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // GLOSSY HIGH-OCTANE "GO" ARCADE BUTTON
                Surface(
                    onClick = onGo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF00E5FF),
                            spotColor = Color(0xFF00E5FF)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF00B0FF),
                                    Color(0xFF7C4DFF),
                                    Color(0xFF00E5FF)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag("stage_dialog_go_button"),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF00B0FF),
                                        Color(0xFF0072BC),
                                        Color(0xFF003366)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Top specular sheen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(26.dp)
                                .align(Alignment.TopCenter)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.55f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "GO",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontFamily = RussoOneFontFamily,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 3.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color(0xFF002244),
                                        offset = Offset(0f, 3f),
                                        blurRadius = 6f
                                    )
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full Stage Browser Modal (Allows player to view and jump to any of the 5000 stages across 834 chapters).
 */
@Composable
private fun AllStagesModal(
    levelManager: LevelManager,
    onSelectStage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val highestUnlocked = levelManager.highestUnlockedLevel
    val totalLevels = levelManager.totalLevels
    val currentChapterIndex = levelManager.getChapterIndexForLevel(highestUnlocked)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = currentChapterIndex
    )
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
                .widthIn(max = 500.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color(0xFF00E5FF).copy(alpha = 0.4f),
                    spotColor = Color(0xFFE040FB).copy(alpha = 0.7f)
                )
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFE040FB))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B0733), Color(0xFF0E021C), Color(0xFF060010))
                    )
                )
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "QUEST MAP (5000 LEVELS)",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = RussoOneFontFamily,
                            letterSpacing = 1.2.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Unlocked: $highestUnlocked / $totalLevels",
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "★ ${levelManager.getTotalStarsEarned()} Stars",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Jump Navigation Chips
                val jumpTargets = listOf(
                    "🎯 Current (Lv $highestUnlocked)" to currentChapterIndex,
                    "Ch.1" to 0,
                    "Ch.10" to 9,
                    "Ch.50" to 49,
                    "Ch.100" to 99,
                    "Ch.250" to 249,
                    "Ch.500" to 499,
                    "Ch.834" to levelManager.totalChapters - 1
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(jumpTargets.size) { idx ->
                        val (label, targetCh) = jumpTargets[idx]
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetCh)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (targetCh == currentChapterIndex) Color(0xFF6A1B9A) else Color(0x5517002E),
                            border = BorderStroke(
                                1.dp,
                                if (targetCh == currentChapterIndex) Color(0xFFFFD700) else Color(0xFF00E5FF).copy(alpha = 0.6f)
                            )
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable List of Chapters with Levels Each
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(levelManager.totalChapters) { chapterIndex ->
                        val chapterData = levelManager.getChapter(chapterIndex)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x66120126))
                                .border(
                                    1.dp,
                                    if (chapterData.isUnlocked) Color(0x5500E5FF) else Color(0x227C4DFF),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${chapterData.title} • LEVELS ${chapterData.startLevel} - ${chapterData.endLevel}",
                                    color = if (chapterData.isUnlocked) Color(0xFFFFD700) else Color.Gray,
                                    fontSize = 12.sp,
                                    fontFamily = RussoOneFontFamily,
                                    letterSpacing = 1.sp
                                )
                                if (!chapterData.isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked Chapter",
                                        tint = Color(0x66CE93D8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (node in chapterData.levels) {
                                    val isCur = node.levelNumber == highestUnlocked
                                    val isComp = node.status == LevelStatus.COMPLETED
                                    val isUnl = node.status != LevelStatus.LOCKED

                                    Surface(
                                        onClick = {
                                            if (isUnl) {
                                                onSelectStage(node.levelNumber)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        color = when {
                                            isCur -> Color(0xFF6A1B9A)
                                            isComp -> Color(0xFF280748)
                                            else -> Color(0xFF130225)
                                        },
                                        border = BorderStroke(
                                            width = if (isCur) 2.dp else 1.dp,
                                            color = when {
                                                isCur -> Color(0xFFFFD700)
                                                isComp -> Color(0xFF00E5FF).copy(alpha = 0.7f)
                                                else -> Color(0x337C4DFF)
                                            }
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isUnl) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "${node.levelNumber}",
                                                        color = if (isCur) Color(0xFFFFD700) else Color.White,
                                                        fontSize = if (node.levelNumber >= 1000) 10.5.sp else 12.5.sp,
                                                        fontFamily = RussoOneFontFamily
                                                    )
                                                    if (isCur) {
                                                        Text("👑", fontSize = 8.sp)
                                                    } else if (isComp && node.stars > 0) {
                                                        Text("★".repeat(node.stars), color = Color(0xFFFFD700), fontSize = 7.sp)
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color(0x66CE93D8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

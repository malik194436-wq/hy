package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.SoundManager
import com.example.data.GameStorage
import com.example.model.GameMode
import com.example.ui.theme.*
import kotlin.random.Random

private val RussoOneFontFamily = FontFamily(
    Font(R.font.russo_one, FontWeight.Normal)
)

/**
 * Main Menu Screen matching reference image:
 * - Beautiful dark purple fantasy mountain background with glowing violet peaks, atmospheric purple fog, stars, and subtle particles.
 * - Prominent "BRICKS QUEST" 3D metallic and crystal logo near top center.
 * - Four large, centered, vertically stacked pill buttons in exact order:
 *   1. PLAY
 *   2. MENU
 *   3. SETTINGS
 *   4. SOUND
 * - Dark purple interiors, glowing magenta borders, white text, and subtle 3D depth.
 */
@Composable
fun MainMenuScreen(
    storage: GameStorage,
    soundManager: SoundManager,
    onStartPlay: () -> Unit,
    onOpenGameMode: (GameMode, Int) -> Unit = { _, _ -> },
    onOpenShop: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var soundEnabled by remember { mutableStateOf(soundManager.isSoundEnabled) }
    var hapticsEnabled by remember { mutableStateOf(soundManager.isHapticsEnabled) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHowToPlayDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Ambient alive breathing and motion animation for background & effects ("hal chal")
    val infiniteTransition = rememberInfiniteTransition(label = "menu_ambient")
    val bgScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgScale"
    )
    val bgTranslationX by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgTranslationX"
    )
    val bgTranslationY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgTranslationY"
    )
    val meteorProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "meteor1"
    )
    val meteorProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, delayMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "meteor2"
    )
    val waterPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waterPhase"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_menu_screen")
    ) {
        val totalH = maxHeight
        val totalW = maxWidth

        // 1. Full sticker reference image with alive breathing 3D parallax motion
        Image(
            painter = painterResource(id = R.drawable.bg_main_menu),
            contentDescription = "Bricks Quest Main Menu",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                    translationX = bgTranslationX
                    translationY = bgTranslationY
                }
        )

        // 2. Animated cosmic action layer: shooting meteors, star glints, water ripples ("hal chal")
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // A. Shooting Meteor 1 (Top Left across cosmic sky)
            if (meteorProgress1 in 0.15f..0.85f) {
                val t = (meteorProgress1 - 0.15f) / 0.70f
                val startX = w * -0.1f + t * w * 1.2f
                val startY = h * 0.04f + t * h * 0.28f
                val tailLen = 95.dp.toPx()
                val dirX = -tailLen * 0.86f
                val dirY = -tailLen * 0.50f
                val alpha = (kotlin.math.sin(t * Math.PI.toFloat())).coerceIn(0f, 1f)

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = alpha),
                            Color(0xFF00E5FF).copy(alpha = alpha * 0.85f),
                            Color(0xFFD500F9).copy(alpha = alpha * 0.35f),
                            Color.Transparent
                        ),
                        start = Offset(startX, startY),
                        end = Offset(startX + dirX, startY + dirY)
                    ),
                    start = Offset(startX, startY),
                    end = Offset(startX + dirX, startY + dirY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 3.5.dp.toPx(),
                    center = Offset(startX, startY)
                )
            }

            // B. Shooting Meteor 2 (Top Right across space)
            if (meteorProgress2 in 0.15f..0.85f) {
                val t = (meteorProgress2 - 0.15f) / 0.70f
                val startX = w * 1.1f - t * w * 1.2f
                val startY = h * 0.08f + t * h * 0.25f
                val tailLen = 80.dp.toPx()
                val dirX = tailLen * 0.84f
                val dirY = -tailLen * 0.54f
                val alpha = (kotlin.math.sin(t * Math.PI.toFloat())).coerceIn(0f, 1f)

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = alpha),
                            Color(0xFFFF00E5).copy(alpha = alpha * 0.85f),
                            Color(0xFF7C4DFF).copy(alpha = alpha * 0.35f),
                            Color.Transparent
                        ),
                        start = Offset(startX, startY),
                        end = Offset(startX + dirX, startY + dirY)
                    ),
                    start = Offset(startX, startY),
                    end = Offset(startX + dirX, startY + dirY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 3.dp.toPx(),
                    center = Offset(startX, startY)
                )
            }

            // C. Twinkling Star Sparkle Glints around title gems & space
            val sparklePoints = listOf(
                Pair(Offset(w * 0.23f, h * 0.12f), Color(0xFF00E5FF)),
                Pair(Offset(w * 0.77f, h * 0.13f), Color(0xFFFF26E2)),
                Pair(Offset(w * 0.17f, h * 0.31f), Color(0xFFFFD600)),
                Pair(Offset(w * 0.83f, h * 0.31f), Color(0xFF00E676)),
                Pair(Offset(w * 0.50f, h * 0.22f), Color(0xFF00E5FF)),
                Pair(Offset(w * 0.89f, h * 0.06f), Color(0xFFB388FF))
            )

            sparklePoints.forEachIndexed { index, (pos, color) ->
                val phaseOffset = index * 0.95f
                val pulse = ((kotlin.math.sin(waterPhase + phaseOffset) + 1f) / 2f).coerceIn(0.2f, 1f)
                val flareLen = 14.dp.toPx() * pulse
                val flareWidth = 1.8.dp.toPx()

                // Horizontal beam
                drawLine(
                    color = Color.White.copy(alpha = 0.85f * pulse),
                    start = Offset(pos.x - flareLen, pos.y),
                    end = Offset(pos.x + flareLen, pos.y),
                    strokeWidth = flareWidth,
                    cap = StrokeCap.Round
                )
                // Vertical beam
                drawLine(
                    color = Color.White.copy(alpha = 0.85f * pulse),
                    start = Offset(pos.x, pos.y - flareLen),
                    end = Offset(pos.x, pos.y + flareLen),
                    strokeWidth = flareWidth,
                    cap = StrokeCap.Round
                )
                // Cross diagonal beam
                val diag = flareLen * 0.55f
                drawLine(
                    color = color.copy(alpha = 0.7f * pulse),
                    start = Offset(pos.x - diag, pos.y - diag),
                    end = Offset(pos.x + diag, pos.y + diag),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color.copy(alpha = 0.7f * pulse),
                    start = Offset(pos.x - diag, pos.y + diag),
                    end = Offset(pos.x + diag, pos.y - diag),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f * pulse),
                    radius = 2.5.dp.toPx(),
                    center = pos
                )
            }

            // D. Animated Shimmering Water Waves (Bottom sea reflections)
            val waterStartY = h * 0.86f
            for (i in 0 until 5) {
                val waveY = waterStartY + i * (h * 0.026f)
                val waveAlpha = (0.28f + 0.16f * kotlin.math.sin(waterPhase + i * 1.15f)).coerceIn(0.1f, 0.5f)
                val xShift = (kotlin.math.sin(waterPhase * 0.85f + i) * 22.dp.toPx()).toFloat()
                val waveBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFD500F9).copy(alpha = waveAlpha * 0.45f),
                        Color(0xFF00E5FF).copy(alpha = waveAlpha * 0.65f),
                        Color(0xFFD500F9).copy(alpha = waveAlpha * 0.45f),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = w
                )
                drawLine(
                    brush = waveBrush,
                    start = Offset(xShift, waveY),
                    end = Offset(w + xShift, waveY),
                    strokeWidth = (2.2f + i * 0.5f).dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // 3. Interactive Clickable Touch Areas (Clean touch targets without border overlay)
        val buttonWidth = totalW * 0.74f
        val buttonHeight = totalH * 0.108f
        val buttonSpacing = totalH * 0.019f
        val buttonsStartY = totalH * 0.384f

        // PLAY Touch Area
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = buttonsStartY)
                .size(width = buttonWidth, height = buttonHeight)
                .clip(RoundedCornerShape(32.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    soundManager.playClickSound()
                    onStartPlay()
                }
                .testTag("menu_play_button")
        )

        // SHOP Touch Area
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = buttonsStartY + buttonHeight + buttonSpacing)
                .size(width = buttonWidth, height = buttonHeight)
                .clip(RoundedCornerShape(32.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    soundManager.playClickSound()
                    onOpenShop("BALLS")
                }
                .testTag("menu_shop_button")
        )

        // SETTINGS Touch Area
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = buttonsStartY + (buttonHeight + buttonSpacing) * 2)
                .size(width = buttonWidth, height = buttonHeight)
                .clip(RoundedCornerShape(32.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    soundManager.playClickSound()
                    showSettingsDialog = true
                }
                .testTag("menu_settings_button")
        )

        // SHARE Touch Area
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = buttonsStartY + (buttonHeight + buttonSpacing) * 3)
                .size(width = buttonWidth, height = buttonHeight)
                .clip(RoundedCornerShape(32.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    soundManager.playClickSound()
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Bricks Quest")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Play Bricks Quest with me! Smash glowing neon bricks and challenge my level: ${storage.highestStageUnlocked}!"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Bricks Quest via"))
                    } catch (_: Exception) {}
                }
                .testTag("menu_share_button")
        )

        // ==========================================
        // FUNCTIONAL DIALOGS
        // ==========================================

        // 1. MENU DIALOG (Game Options: Stage Quest, Endless, 100-Ball Rush, Shop, How to Play)
        if (showMenuDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenuDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp)
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0836)),
                    border = BorderStroke(2.dp, Color(0xFFE040FB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GAME MENU",
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            IconButton(
                                onClick = { showMenuDialog = false },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stage Quest Option
                        MenuDialogOptionRow(
                            icon = Icons.Default.Terrain,
                            title = "STAGE QUEST",
                            subtitle = "5000 Mountain levels • Level ${storage.highestStageUnlocked}",
                            color = NeonCyan,
                            onClick = {
                                showMenuDialog = false
                                onStartPlay()
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Endless Mode Option
                        MenuDialogOptionRow(
                            icon = Icons.Default.AllInclusive,
                            title = "ENDLESS MODE",
                            subtitle = "Infinite waves • High score: ${storage.endlessHighScore}",
                            color = NeonLime,
                            onClick = {
                                showMenuDialog = false
                                onOpenGameMode(GameMode.ENDLESS, 1)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 100-Ball Rush Option
                        MenuDialogOptionRow(
                            icon = Icons.Default.Bolt,
                            title = "100-BALL RUSH",
                            subtitle = "Fast action • High score: ${storage.rushHighScore}",
                            color = NeonGold,
                            onClick = {
                                showMenuDialog = false
                                onOpenGameMode(GameMode.RUSH_100, 1)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ball Skins & Shop Option
                        MenuDialogOptionRow(
                            icon = Icons.Default.Palette,
                            title = "BALL SKINS & SHOP",
                            subtitle = "Unlock glowing balls and neon styles",
                            color = Color(0xFFFF4081),
                            onClick = {
                                showMenuDialog = false
                                onOpenShop("BALLS")
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // How to Play Option
                        MenuDialogOptionRow(
                            icon = Icons.Default.HelpOutline,
                            title = "HOW TO PLAY",
                            subtitle = "Aiming, ball bounce, bombs & lasers",
                            color = Color(0xFFB388FF),
                            onClick = {
                                showMenuDialog = false
                                showHowToPlayDialog = true
                            }
                        )
                    }
                }
            }
        }

        // 2. SETTINGS DIALOG
        // 2. SETTINGS DIALOG (Exact futuristic neon space theme matching user's reference image)
        if (showSettingsDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showSettingsDialog = false },
                contentAlignment = Alignment.Center
            ) {
                // Main Dialog Outer Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 380.dp)
                        .padding(horizontal = 8.dp)
                        .shadow(
                            elevation = 32.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color(0xFF00E5FF).copy(alpha = 0.6f),
                            spotColor = Color(0xFFE040FB)
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .border(
                            width = 2.8.dp,
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
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0C1238),
                                    Color(0xFF070B24),
                                    Color(0xFF030514)
                                )
                            )
                        )
                        .clickable(enabled = false) {}
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. TOP HEADER BAR: [Settings Icon] SETTINGS [X Close]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    width = 1.8.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFF7C4DFF),
                                            Color(0xFFE040FB)
                                        )
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF0C1B4A),
                                            Color(0xFF091334),
                                            Color(0xFF160B3A)
                                        )
                                    )
                                )
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left Glowing Gear Icon
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(28.dp)
                                )

                                // Center SETTINGS Title
                                Text(
                                    text = "SETTINGS",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.5.sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0xFF00E5FF),
                                            offset = Offset(0f, 0f),
                                            blurRadius = 14f
                                        )
                                    )
                                )

                                // Right Cyber Hexagonal Close Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0A153A))
                                        .border(
                                            width = 1.5.dp,
                                            color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { showSettingsDialog = false },
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
                        }

                        // 2. SOUND ROW (Electric Cyan Theme with Audio Equalizer Waves & Pill Switch)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFF00B0FF),
                                            Color(0xFF00E5FF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            color = Color(0xFF051430)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Circular Blue Glowing Icon
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF09204A))
                                        .border(2.dp, Color(0xFF00E5FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Sound",
                                        tint = if (soundEnabled) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // SOUND Label
                                Text(
                                    text = "SOUND",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.5.sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0xFF00E5FF),
                                            blurRadius = 12f
                                        )
                                    )
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Multi-color Equalizer Waves
                                NeonEqualizerWave(isActive = soundEnabled)

                                Spacer(modifier = Modifier.width(10.dp))

                                // Cyber Switch
                                CyberGlowSwitch(
                                    checked = soundEnabled,
                                    onCheckedChange = {
                                        soundEnabled = it
                                        soundManager.isSoundEnabled = it
                                        storage.soundEnabled = it
                                        if (soundEnabled) soundManager.playHitSound(0)
                                    },
                                    activeColor = Color(0xFF00E5FF)
                                )
                            }
                        }

                        // 3. VIBRATION ROW (Golden Amber Theme with Vibration Pulse Waves & Pill Switch)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFB300),
                                            Color(0xFFFFD54F),
                                            Color(0xFFFFB300)
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            color = Color(0xFF1A1304)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Circular Amber Glowing Icon with Phone & Vibration waves
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2D2005))
                                        .border(2.dp, Color(0xFFFFB300), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = "Vibration",
                                        tint = if (hapticsEnabled) Color(0xFFFFB300) else Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // VIBRATION Label
                                Text(
                                    text = "VIBRATION",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.5.sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0xFFFFB300),
                                            blurRadius = 12f
                                        )
                                    )
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Golden Vibration Wave Symbol
                                NeonVibrationWave(isActive = hapticsEnabled)

                                Spacer(modifier = Modifier.width(10.dp))

                                // Cyber Switch
                                CyberGlowSwitch(
                                    checked = hapticsEnabled,
                                    onCheckedChange = {
                                        hapticsEnabled = it
                                        soundManager.isHapticsEnabled = it
                                        storage.hapticsEnabled = it
                                        if (hapticsEnabled) soundManager.triggerBreakHaptic()
                                    },
                                    activeColor = Color(0xFFFFB300)
                                )
                            }
                        }

                        // 4. DONE BUTTON (Intense Neon Magenta Chamfered Cyber Pill)
                        Surface(
                            onClick = { showSettingsDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    ambientColor = Color(0xFFD500F9),
                                    spotColor = Color(0xFFFF26E2)
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFF4081),
                                            Color(0xFFE040FB),
                                            Color(0xFFD500F9),
                                            Color(0xFF7C4DFF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFD500F9),
                                                Color(0xFF8A00E6),
                                                Color(0xFF4A0072)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Top specular sheen
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.92f)
                                        .height(24.dp)
                                        .align(Alignment.TopCenter)
                                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.45f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                Text(
                                    text = "DONE",
                                    color = Color.White,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 3.sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0xDD000000),
                                            offset = Offset(0f, 3f),
                                            blurRadius = 4f
                                        )
                                    )
                                )
                            }
                        }

                        // Subtle options row for Game Modes and Reset
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Game Modes",
                                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    showSettingsDialog = false
                                    showMenuDialog = true
                                }
                            )
                            Text(
                                text = "Reset Progress",
                                color = Color(0xFFFF5252).copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    showResetConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // RESET CONFIRMATION MODAL
        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = { Text("Reset Progress?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Are you sure you want to reset all unlocked stages and high scores to level 1?", color = TextMuted) },
                containerColor = Color(0xFF240A42),
                confirmButton = {
                    TextButton(
                        onClick = {
                            storage.highestStageUnlocked = 1
                            storage.endlessHighScore = 0
                            storage.rushHighScore = 0
                            showResetConfirmDialog = false
                            showSettingsDialog = false
                        }
                    ) {
                        Text("RESET", color = Color(0xFFFF5252), fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmDialog = false }) {
                        Text("CANCEL", color = Color.White)
                    }
                }
            )
        }

        // 3. HOW TO PLAY DIALOG
        if (showHowToPlayDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showHowToPlayDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0836)),
                    border = BorderStroke(2.dp, NeonCyan)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HOW TO PLAY",
                                color = NeonCyan,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            IconButton(onClick = { showHowToPlayDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HowToPlayRow(icon = "🎯", title = "Touch & Drag to Aim", desc = "Drag your finger across the screen to aim the neon trajectory line. Release to fire all balls!")
                        Spacer(modifier = Modifier.height(12.dp))
                        HowToPlayRow(icon = "🟢", title = "+1 Ball Pickups", desc = "Hit glowing green orbs to increase your ball reserves for the next round.")
                        Spacer(modifier = Modifier.height(12.dp))
                        HowToPlayRow(icon = "💣", title = "Bombs & Lasers", desc = "Detonate 3x3 explosive bombs or hit vertical/horizontal lasers to wipe out entire rows.")
                        Spacer(modifier = Modifier.height(12.dp))
                        HowToPlayRow(icon = "⚡", title = "Speed & Recall", desc = "Toggle 2X/3X speed, or tap 'RECALL' to instantly return balls when you want to speed up turns.")

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = { showHowToPlayDialog = false },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = SpaceNavyDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("GOT IT!", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Large pill button matching the reference image:
 * - Stadium capsule shape
 * - Dark purple interior gradient
 * - Intense glowing magenta / violet border
 * - Crisp bold white text with 3D drop shadow
 * - Top specular highlight sheen
 */
/**
 * 3D Crystal Metallic Menu Button:
 * - Clean, sleek stadium pill with top glossy glass reflection sheen
 * - NO distracting circular icon badges or side arrows
 * - Big, bold, centered 3D crystal metallic typography matching the "BRICKS QUEST" logo
 * - Layered 3D extruded bevel, neon glow aura, and specular silver-white face
 * - Tactile spring press animation
 */
@Composable
private fun CrystalTitleMenuButton(
    text: String,
    themeColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pulseAlpha: Float = 1f,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth(0.76f)
            .widthIn(min = 250.dp, max = 320.dp)
            .height(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 6.dp else 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = themeColor.copy(alpha = 0.55f * pulseAlpha),
                spotColor = themeColor.copy(alpha = 0.85f * pulseAlpha)
            )
            .clip(RoundedCornerShape(32.dp))
            .border(
                width = 2.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        themeColor.copy(alpha = pulseAlpha),
                        secondaryColor,
                        themeColor.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .testTag(testTag),
        color = Color.Transparent,
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            themeColor.copy(alpha = 0.28f),
                            secondaryColor.copy(alpha = 0.85f),
                            Color(0xFF090114)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Upper Glossy Glass Reflection Sheen
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(28.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.40f),
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner subtle border highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.5.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent,
                                themeColor.copy(alpha = 0.35f)
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
            )

            // Bottom subtle glowing rim line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .height(2.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                themeColor.copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // CENTERED 3D CHISELED METALLIC TYPOGRAPHY (Matching Exact "BRICKS QUEST" Logo Style)
            val faceGradient = remember(text, themeColor) {
                when (text) {
                    "PLAY" -> Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFE0F7FA),
                            Color(0xFF80DEEA),
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                    "SHOP" -> Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFFCE4EC),
                            Color(0xFFFF80AB),
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                    "SETTINGS" -> Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFEDE7F6),
                            Color(0xFFB388FF),
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                    "SHARE" -> Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFE8F5E9),
                            Color(0xFF69F0AE),
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                    else -> Brush.verticalGradient(
                        listOf(
                            Color.White,
                            themeColor.copy(alpha = 0.35f),
                            Color.White
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Layer 1: Deep 3D Extrusion Drop Shadow
                Text(
                    text = text,
                    color = Color.Black.copy(alpha = 0.92f),
                    fontSize = 25.sp,
                    fontFamily = RussoOneFontFamily,
                    letterSpacing = 3.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = 4.5.dp)
                )

                // Layer 2: Thick Dark Comic/Arcade 3D Contour Outline in 8 Directions
                val outlineDist = 1.4.dp
                listOf(
                    Offset(-outlineDist.value, -outlineDist.value),
                    Offset(outlineDist.value, -outlineDist.value),
                    Offset(-outlineDist.value, outlineDist.value),
                    Offset(outlineDist.value, outlineDist.value),
                    Offset(0f, 2.5.dp.value),
                    Offset(0f, 3.5.dp.value)
                ).forEach { off ->
                    Text(
                        text = text,
                        color = Color(0xFF070114),
                        fontSize = 25.sp,
                        fontFamily = RussoOneFontFamily,
                        letterSpacing = 3.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(x = off.x.dp, y = off.y.dp)
                    )
                }

                // Layer 3: Chiseled 3D Bevel in Secondary Jewel Color
                Text(
                    text = text,
                    color = secondaryColor,
                    fontSize = 25.sp,
                    fontFamily = RussoOneFontFamily,
                    letterSpacing = 3.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = 2.dp)
                )

                // Layer 4: Vibrant Neon Glow Aura (Matches the bright ambient logo backlight)
                Text(
                    text = text,
                    color = themeColor.copy(alpha = 0.95f),
                    fontSize = 25.sp,
                    fontFamily = RussoOneFontFamily,
                    letterSpacing = 3.5.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = themeColor,
                            offset = Offset(0f, 0f),
                            blurRadius = 24f
                        )
                    )
                )

                // Layer 5: Brilliant Specular Metallic Chrome / Crystal Face (Exact Match to Logo Sheen)
                Text(
                    text = text,
                    fontSize = 25.sp,
                    fontFamily = RussoOneFontFamily,
                    letterSpacing = 3.5.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        brush = faceGradient,
                        shadow = Shadow(
                            color = Color(0xCC000000),
                            offset = Offset(0f, 1.5f),
                            blurRadius = 2.5f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun MenuDialogOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF290C49),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().height(62.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(text = subtitle, color = TextMuted, fontSize = 11.sp, maxLines = 1)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HowToPlayRow(icon: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = icon, fontSize = 18.sp, modifier = Modifier.padding(top = 1.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = desc, color = TextMuted, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

/**
 * Cyber glowing pill switch matching the exact reference UI:
 * - Rounded pill container with colored neon track when active
 * - Large crisp white circular thumb with smooth spring slide animation
 */
@Composable
private fun CyberGlowSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 30.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumb_offset"
    )

    Box(
        modifier = modifier
            .width(66.dp)
            .height(36.dp)
            .clip(CircleShape)
            .background(
                if (checked) {
                    Brush.horizontalGradient(
                        colors = listOf(activeColor.copy(alpha = 0.85f), activeColor)
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF161C2E), Color(0xFF232D48))
                    )
                }
            )
            .border(
                width = 1.8.dp,
                color = if (checked) activeColor else Color.White.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        // Glowing White Circular Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(28.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = if (checked) activeColor else Color.Black,
                    spotColor = if (checked) Color.White else Color.Black
                )
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Animated multi-colored audio equalizer waveform matching the SOUND row in the reference image.
 */
@Composable
private fun NeonEqualizerWave(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_anim")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 20f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h5"
    )

    Row(
        modifier = modifier.height(26.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = listOf(
            Triple(if (isActive) h1 else 6f, Color(0xFF00E5FF), 3.dp),
            Triple(if (isActive) h2 else 10f, Color(0xFF00B0FF), 3.5.dp),
            Triple(if (isActive) h3 else 16f, Color(0xFFE040FB), 4.dp),
            Triple(if (isActive) h4 else 12f, Color(0xFFFF4081), 3.5.dp),
            Triple(if (isActive) h5 else 8f, Color(0xFF00E5FF), 3.dp)
        )
        bars.forEach { (height, color, width) ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(height.dp)
                    .clip(CircleShape)
                    .background(if (isActive) color else color.copy(alpha = 0.3f))
            )
        }
    }
}

/**
 * Animated golden vibration wave graphic matching the VIBRATION row in the reference image.
 */
@Composable
private fun NeonVibrationWave(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vib_anim")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.88f, targetValue = 1.14f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "vib_scale"
    )

    Row(
        modifier = modifier
            .height(26.dp)
            .graphicsLayer {
                scaleX = if (isActive) waveScale else 1f
                scaleY = if (isActive) waveScale else 1f
            },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "〰️",
            color = if (isActive) Color(0xFFFFB300) else Color(0xFFFFB300).copy(alpha = 0.35f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(
                    color = if (isActive) Color(0xFFFFB300) else Color.Transparent,
                    blurRadius = 10f
                )
            )
        )
    }
}

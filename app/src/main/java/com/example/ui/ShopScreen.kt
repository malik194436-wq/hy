package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.GameStorage
import com.example.model.BallSkin
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(
    storage: GameStorage,
    onBack: () -> Unit,
    initialTab: String = "BALLS",
    soundManager: SoundManager? = null
) {
    var coins by remember { mutableIntStateOf(storage.coins) }
    var ballBalance by remember { mutableIntStateOf(storage.ballBalance) }
    var lightningCharges by remember { mutableIntStateOf(storage.lightningCharges) }
    var clusterCharges by remember { mutableIntStateOf(storage.clusterCharges) }
    var activeSkinId by remember { mutableStateOf(storage.activeSkinId) }
    var selectedSkin by remember {
        mutableStateOf(BallSkin.ALL_SKINS.find { it.id == activeSkinId } ?: BallSkin.ALL_SKINS.first())
    }

    var selectedTab by remember { mutableStateOf(initialTab) }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    var showPurchaseToast by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "shop_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceNavyDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top Bar (Exact Match to Portrait Reference Screenshot)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Back Button (Gold when on Diamonds or Balls tab, cyan otherwise)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F0B18))
                    .border(
                        2.dp,
                        if (selectedTab == "DIAMONDS" || selectedTab == "BALLS") Color(0xFFC5A059) else Color(0xFF00E5FF),
                        CircleShape
                    )
                    .testTag("shop_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (selectedTab == "DIAMONDS" || selectedTab == "BALLS") Color(0xFFFFD54F) else Color(0xFF00E5FF),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Center Title
            if (selectedTab == "DIAMONDS") {
                Text(
                    text = "DIAMONDS",
                    color = Color(0xFFFFE082),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            } else if (selectedTab == "BALLS") {
                Text(
                    text = "BALL",
                    color = Color(0xFFFFE082),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            } else if (selectedTab == "POWERUPS") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(26.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color(0xFFC084FC), Color(0xFFFFDF9E))
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color(0xFFC084FC), Color(0xFFFFDF9E))
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "POWER-UPS",
                        color = Color(0xFFFFDF9E),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(26.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFDF9E), Color(0xFFC084FC), Color.Transparent)
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFDF9E), Color(0xFFC084FC), Color.Transparent)
                                    )
                                )
                        )
                    }
                }
            } else if (selectedTab == "SKINS") {
                Text(
                    text = "SKINS",
                    color = Color(0xFFEDE9FE),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = selectedTab,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Right: Diamonds Chip or Ball Inventory Pill
            if (selectedTab == "DIAMONDS" || selectedTab == "SKINS") {
                Surface(
                    color = Color(0xFF160A29),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFC5A059))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_diamond_gem),
                            contentDescription = "Diamonds",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$coins",
                            color = Color(0xFFFFE082),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                Surface(
                    color = Color(0xFF160A29),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8B5CF6)),
                    modifier = Modifier.testTag("ball_balance_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_plasma_ball),
                            contentDescription = "Ball",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "x $ballBalance",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Navigation Tabs: [💎 DIAMONDS], [🟣 BALLS], [⚡ POWER-UPS], [📦 SKINS]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShopTabButton(
                title = "DIAMONDS",
                icon = "💎",
                isSelected = selectedTab == "DIAMONDS",
                onClick = {
                    soundManager?.playClickSound()
                    selectedTab = "DIAMONDS"
                },
                modifier = Modifier.weight(1f)
            )
            ShopTabButton(
                title = "BALLS",
                icon = "🟣",
                isSelected = selectedTab == "BALLS",
                onClick = {
                    soundManager?.playClickSound()
                    selectedTab = "BALLS"
                },
                modifier = Modifier.weight(1f)
            )
            ShopTabButton(
                title = "POWER-UPS",
                icon = "⚡",
                isSelected = selectedTab == "POWERUPS",
                onClick = {
                    soundManager?.playClickSound()
                    selectedTab = "POWERUPS"
                },
                modifier = Modifier.weight(1.15f)
            )
            ShopTabButton(
                title = "SKINS",
                icon = "📦",
                isSelected = selectedTab == "SKINS",
                onClick = {
                    soundManager?.playClickSound()
                    selectedTab = "SKINS"
                },
                modifier = Modifier.weight(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Toast feedback message if purchase made
        showPurchaseToast?.let { msg ->
            Surface(
                color = Color(0xFF065F46),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(2000L)
                showPurchaseToast = null
            }
        }

        when (selectedTab) {
            "DIAMONDS" -> {
                // Diamond & Bundle Packs (Exact match to reference user image)
                val diamondScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(diamondScrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Featured Mega Bundle (Gold glowing border matching reference screenshot)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                storage.addDiamonds(5000)
                                storage.addLightningCharges(7)
                                coins = storage.coins
                                lightningCharges = storage.lightningCharges
                                showPurchaseToast = "Successfully purchased 5,000 Diamonds + Bundle! 💎⚡"
                            }
                            .testTag("bundle_mega"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF090614)),
                        border = androidx.compose.foundation.BorderStroke(1.8.dp, Color(0xFFD4AF37))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFF2E230B),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFD4AF37))
                                    ) {
                                        Text(
                                            "+30%",
                                            color = Color(0xFFFFD54F),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "5 000 Diamonds + Bundle",
                                        color = Color(0xFFFFE082),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⚡", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("7 Lightning", color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("8 Balls", color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Golden Metallic Button with dark brown bold price
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFF7CB66), Color(0xFFD9A336), Color(0xFFB8821C))
                                        )
                                    )
                                    .clickable {
                                        storage.addDiamonds(5000)
                                        storage.addLightningCharges(7)
                                        coins = storage.coins
                                        lightningCharges = storage.lightningCharges
                                        showPurchaseToast = "Successfully purchased 5,000 Diamonds + Bundle! 💎"
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Rs 4,000.00",
                                    color = Color(0xFF1E1404),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Standard Diamond Packs Grid (Exact match to reference user image)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DiamondPackCard(
                            title = "1000 Diamonds",
                            price = "Rs 1,900.00",
                            isHot = true,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                storage.addDiamonds(1000)
                                coins = storage.coins
                                showPurchaseToast = "Purchased 1,000 Diamonds! 💎"
                            }
                        )
                        DiamondPackCard(
                            title = "10000 + 3000",
                            subtitle = "Diamonds",
                            bonus = "+30%",
                            price = "Rs 4,000.00",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                storage.addDiamonds(13000)
                                coins = storage.coins
                                showPurchaseToast = "Purchased 13,000 Diamonds! 💎"
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DiamondPackCard(
                            title = "300 Diamonds",
                            price = "Rs 550.00",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                storage.addDiamonds(300)
                                coins = storage.coins
                                showPurchaseToast = "Purchased 300 Diamonds! 💎"
                            }
                        )
                        DiamondPackCard(
                            title = "500 Diamonds",
                            price = "Rs 1,250.00",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                storage.addDiamonds(500)
                                coins = storage.coins
                                showPurchaseToast = "Purchased 500 Diamonds! 💎"
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DiamondPackCard(
                            title = "3000 + 300",
                            price = "Rs 5,500.00",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                storage.addDiamonds(3300)
                                coins = storage.coins
                                showPurchaseToast = "Purchased 3,300 Diamonds! 💎"
                            }
                        )
                        DiamondPackCard(
                            title = "5000 + 1000",
                            price = "Rs 9,500.00",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                storage.addDiamonds(6000)
                                coins = storage.coins
                                showPurchaseToast = "Purchased 6,000 Diamonds! 💎"
                            }
                        )
                    }
                }
            }
            "BALLS" -> {
                // 6 Ball packages vertically stacked inside rounded neon container (Exact match to reference)
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xAA080414)),
                        border = androidx.compose.foundation.BorderStroke(1.8.dp, Color(0xFFC5A059))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BallPackageItem(
                                title = "50 Balls",
                                priceText = "Rs 150.00",
                                onPurchase = {
                                    storage.addBalls(50)
                                    ballBalance = storage.ballBalance
                                    showPurchaseToast = "Successfully purchased 50 Balls! 🟣"
                                }
                            )

                            BallPackageItem(
                                title = "10 Balls",
                                priceText = "Rs 50.00",
                                onPurchase = {
                                    storage.addBalls(10)
                                    ballBalance = storage.ballBalance
                                    showPurchaseToast = "Successfully purchased 10 Balls! 🟣"
                                }
                            )

                            BallPackageItem(
                                title = "30 Balls",
                                priceText = "Rs 100.00",
                                onPurchase = {
                                    storage.addBalls(30)
                                    ballBalance = storage.ballBalance
                                    showPurchaseToast = "Successfully purchased 30 Balls! 🟣"
                                }
                            )

                            BallPackageItem(
                                title = "100 Balls",
                                priceText = "Rs 250.00",
                                onPurchase = {
                                    storage.addBalls(100)
                                    ballBalance = storage.ballBalance
                                    showPurchaseToast = "Successfully purchased 100 Balls! 🟣"
                                }
                            )

                            BallPackageItem(
                                title = "200 Balls",
                                priceText = "Rs 450.00",
                                onPurchase = {
                                    storage.addBalls(200)
                                    ballBalance = storage.ballBalance
                                    showPurchaseToast = "Successfully purchased 200 Balls! 🟣"
                                }
                            )

                            BallPackageItem(
                                title = "500 Balls",
                                priceText = "Rs 1,000.00",
                                onPurchase = {
                                    storage.addBalls(500)
                                    ballBalance = storage.ballBalance
                                    showPurchaseToast = "Successfully purchased 500 Balls! 🟣"
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            "POWERUPS" -> {
                // Power-ups Shop Tab matching reference screenshot image_9a58dc26.png
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "BOOSTERS & POWER-UPS",
                        color = Color(0xFFFFDF9E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    PowerUpShopCard(
                        title = "Lightning Laser Charges (+5)",
                        description = "Instantly clears entire rows & columns of",
                        iconRes = R.drawable.ic_powerup_laser_box,
                        priceDiamonds = 200,
                        currentCount = lightningCharges,
                        onClick = {
                            if (coins >= 200) {
                                storage.diamonds -= 200
                                storage.addLightningCharges(5)
                                coins = storage.coins
                                lightningCharges = storage.lightningCharges
                                showPurchaseToast = "Purchased +5 Lightning Charges! ⚡"
                            } else {
                                showPurchaseToast = "Not enough diamonds! 💎"
                            }
                        }
                    )

                    PowerUpShopCard(
                        title = "Cluster Bomb Charges (+5)",
                        description = "Destroys 3x3 brick grids with massive",
                        iconRes = R.drawable.ic_powerup_bomb_box,
                        priceDiamonds = 250,
                        currentCount = clusterCharges,
                        onClick = {
                            if (coins >= 250) {
                                storage.diamonds -= 250
                                storage.clusterCharges += 5
                                coins = storage.coins
                                clusterCharges = storage.clusterCharges
                                showPurchaseToast = "Purchased +5 Cluster Bomb Charges! 💣"
                            } else {
                                showPurchaseToast = "Not enough diamonds! 💎"
                            }
                        }
                    )
                }
            }
            "SKINS" -> {
                // Ball Skins Tab matching reference screenshot image_d5d64741.png
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xDD0A0518)),
                        border = androidx.compose.foundation.BorderStroke(1.8.dp, Color(0xFFC5A059))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // Angled header tag: CHOOSE YOUR BALL
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Color(0xFF1B0E32),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFC5A059))
                                ) {
                                    Text(
                                        text = "CHOOSE YOUR BALL",
                                        color = Color(0xFFFFDF9E),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.5.sp,
                                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(BallSkin.ALL_SKINS) { skin ->
                                    val isUnlocked = storage.isSkinUnlocked(skin.id)
                                    val isEquipped = skin.id == activeSkinId
                                    val isSelected = skin.id == selectedSkin.id

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(134.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { selectedSkin = skin }
                                            .testTag("skin_card_${skin.id}"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF0C071C)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.dp else 1.2.dp,
                                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFC5A059)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp)
                                        ) {
                                            // Ribbon badge at top-right for equipped skin
                                            if (isEquipped) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.ic_badge_ribbon),
                                                    contentDescription = "Equipped Badge",
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .align(Alignment.TopEnd)
                                                )
                                            }

                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                // Real 3D Ball Skin Image from user reference
                                                Image(
                                                    painter = painterResource(id = skin.drawableRes),
                                                    contentDescription = skin.name,
                                                    modifier = Modifier.size(54.dp)
                                                )

                                                Text(
                                                    text = skin.name,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )

                                                if (isEquipped) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "EQUIPPED",
                                                            color = Color(0xFF10B981),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFF10B981))
                                                        )
                                                    }
                                                } else if (isUnlocked) {
                                                    Text(
                                                        text = "OWNED",
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.Lock,
                                                            contentDescription = null,
                                                            tint = Color(0xFFCBD5E1),
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "${skin.price}",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
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

                    Spacer(modifier = Modifier.height(12.dp))

                    val isUnlocked = storage.isSkinUnlocked(selectedSkin.id)
                    val isEquipped = selectedSkin.id == activeSkinId

                    Surface(
                        onClick = {
                            if (isEquipped) {
                                // Already equipped
                            } else if (isUnlocked) {
                                storage.activeSkinId = selectedSkin.id
                                activeSkinId = selectedSkin.id
                                showPurchaseToast = "Equipped ${selectedSkin.name} skin! ✨"
                            } else if (coins >= selectedSkin.price) {
                                storage.diamonds -= selectedSkin.price
                                coins = storage.coins
                                storage.unlockSkin(selectedSkin.id)
                                storage.activeSkinId = selectedSkin.id
                                activeSkinId = selectedSkin.id
                                showPurchaseToast = "Unlocked & Equipped ${selectedSkin.name}! ✨"
                            } else {
                                showPurchaseToast = "Need ${selectedSkin.price - coins} more diamonds! 💎"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("skin_action_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF082236),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    isEquipped -> "CURRENTLY EQUIPPED"
                                    isUnlocked -> "EQUIP SKIN"
                                    coins >= selectedSkin.price -> "UNLOCK FOR ${selectedSkin.price} 💎"
                                    else -> "NEED ${selectedSkin.price - coins} MORE 💎"
                                },
                                color = if (isEquipped || isUnlocked) Color(0xFF00E5FF) else Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopTabButton(
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF231438) else Color(0xFF0F0B1E),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.2.dp,
            color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF4A3260)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (title == "DIAMONDS") {
                Image(
                    painter = painterResource(id = R.drawable.ic_diamond_gem),
                    contentDescription = "Diamond",
                    modifier = Modifier.size(16.dp)
                )
            } else if (title == "BALLS") {
                Image(
                    painter = painterResource(id = R.drawable.ic_plasma_ball),
                    contentDescription = "Ball",
                    modifier = Modifier.size(16.dp)
                )
            } else if (title == "POWER-UPS") {
                Text(
                    text = "⚡",
                    fontSize = 15.sp,
                    color = Color(0xFFFFEB3B)
                )
            } else {
                Text(text = icon, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = if (isSelected) Color(0xFFFFDF9E) else TextMuted,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun DiamondPackCard(
    title: String,
    subtitle: String? = null,
    bonus: String? = null,
    price: String,
    isHot: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C071C)),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.8.dp,
            color = if (isHot) Color(0xFF00E5FF) else Color(0xFF8B5CF6)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            if (bonus != null) {
                Surface(
                    color = Color(0xFF260547),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFA855F7)),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = bonus,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (isHot) {
                Surface(
                    color = Color(0xFF021B27),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF)),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "HOT",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_diamond_gem),
                    contentDescription = "Diamond Gem",
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    color = Color(0xFFFFE082),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color(0xFFFFE082),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = price,
                    color = Color(0xFFFFE082),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PowerUpShopCard(
    title: String,
    description: String,
    iconRes: Int,
    priceDiamonds: Int,
    currentCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE0E081F)),
        border = androidx.compose.foundation.BorderStroke(1.8.dp, Color(0xFFC5A059))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        color = Color(0xFFFFDF9E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp
                    )
                    Text(
                        text = description,
                        color = Color(0xFFD4C7E8),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Owned: $currentCount",
                        color = Color(0xFFFFDF9E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Shiny metallic gold button with dark purple border and diamond gem
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4A184C)),
                modifier = Modifier.testTag("buy_powerup_${title.take(10).replace(" ", "_")}")
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF2B2),
                                    Color(0xFFFFD54F),
                                    Color(0xFFE5A823)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$priceDiamonds",
                            color = Color(0xFF140D04),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_diamond_gem),
                            contentDescription = "Diamond",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BallPackCard(
    title: String,
    ballsCount: Int,
    bonusText: String?,
    priceText: String,
    diamondsCost: Int,
    testTag: String,
    isPopular: Boolean = false,
    isBestValue: Boolean = false,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPurchase)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0728)),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isBestValue || isPopular) 1.8.dp else 1.2.dp,
            color = when {
                isBestValue -> Color(0xFFFFD700)
                isPopular -> Color(0xFFD500F9)
                else -> Color(0xFF4A148C)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Round glowing purple ball icon (Matching user design reference)
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Outer neon purple glow aura
                        drawCircle(
                            color = Color(0xFFD500F9).copy(alpha = 0.30f),
                            radius = size.width * 0.48f
                        )
                        // Electric magenta boundary ring
                        drawCircle(
                            color = Color(0xFFFA58FF),
                            radius = size.width * 0.36f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2.dp.toPx())
                        )
                        // Inner saturated glowing sphere
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFA8BFF), Color(0xFF8A00D4), Color(0xFF4A0072)),
                                center = Offset(size.width * 0.38f, size.height * 0.38f),
                                radius = size.width * 0.35f
                            ),
                            radius = size.width * 0.32f
                        )
                        // Bright specular reflection dot
                        drawCircle(
                            color = Color.White.copy(alpha = 0.95f),
                            radius = size.width * 0.09f,
                            center = Offset(size.width * 0.38f, size.height * 0.38f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        if (bonusText != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isBestValue) Color(0xFFB45309) else Color(0xFF7C3AED),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = bonusText,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Increases active ball stream by +$ballsCount",
                        color = Color(0xFFC084FC),
                        fontSize = 11.sp
                    )
                }
            }

            // Purchase Button with Diamond Cost
            Button(
                onClick = onPurchase,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBestValue) Color(0xFFFFB300) else Color(0xFFD500F9)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = priceText,
                    color = if (isBestValue) Color.Black else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun BallPackageItem(
    title: String,
    priceText: String,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPurchase),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE0E081F)),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = Color(0xFFC5A059)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Purple glowing plasma energy ball image from user reference
                Image(
                    painter = painterResource(id = R.drawable.ic_plasma_ball),
                    contentDescription = title,
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    color = Color(0xFFFFDF9E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // Shiny metallic gold button with purple neon border
            Surface(
                onClick = onPurchase,
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFA855F7)),
                modifier = Modifier.testTag("ball_purchase_${title.replace(" ", "_")}")
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF2B2),
                                    Color(0xFFFFD54F),
                                    Color(0xFFE5A823)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = priceText,
                        color = Color(0xFF140D04),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}


package com.kg.yazzbozz.presentation.yazboz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.kg.yazzbozz.domain.CountStep
import com.kg.yazzbozz.domain.DayTitle
import com.kg.yazzbozz.domain.PlayerRank
import com.kg.yazzbozz.domain.turkishLowercase
import com.kg.yazzbozz.platform.LightSystemBarIcons
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private val SceneBackground = Color(0xFF10131A)
private val Gold = Color(0xFFFFD54F)
private val LoserRed = Color(0xFFFF5252)
private val Dim = Color.White.copy(alpha = 0.55f)

/**
 * Full-screen end-of-game show over the sheet. All timing comes from [state] (driven by the
 * ViewModel); this only animates between the values it is given. It swallows taps so the
 * sheet underneath can't be touched while it's up.
 */
@Composable
fun ScoreRevealScene(
    state: ScoreRevealState,
    onClose: () -> Unit,
    onSkip: () -> Unit,
) {
    LightSystemBarIcons()
    // Back skips the count to the ranking, then closes the scene — it never leaves the app.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = { if (state.phase == RevealPhase.Ranking) onClose() else onSkip() },
    )
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(SceneBackground)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        val showRanking = state.phase == RevealPhase.Ranking
        val sceneWidth = maxWidth
        val sceneHeight = maxHeight
        if (sceneWidth > sceneHeight) {
            // Landscape: there's no height to stack the ranking under the teams.
            Row(Modifier.fillMaxSize()) {
                TeamsArea(state, Modifier.weight(1f).fillMaxHeight())
                AnimatedVisibility(
                    visible = showRanking,
                    enter = fadeIn(tween(600)) + expandHorizontally(tween(600)),
                ) {
                    RankingPanel(
                        ranking = state.ranking,
                        onClose = onClose,
                        modifier = Modifier.width(sceneWidth * 0.45f).fillMaxHeight(),
                    )
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                TeamsArea(state, Modifier.fillMaxWidth().weight(1f))
                AnimatedVisibility(
                    visible = showRanking,
                    enter = fadeIn(tween(600)) + expandVertically(tween(600)),
                ) {
                    RankingPanel(
                        ranking = state.ranking,
                        onClose = onClose,
                        modifier = Modifier.fillMaxWidth().heightIn(max = sceneHeight * 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamsArea(state: ScoreRevealState, modifier: Modifier = Modifier) {
    Row(modifier) {
        TeamColumn(team = 1, state = state, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(vertical = 48.dp)
                .background(Color.White.copy(alpha = 0.15f)),
        )
        TeamColumn(team = 2, state = state, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TeamColumn(team: Int, state: ScoreRevealState, modifier: Modifier = Modifier) {
    val isCounting = state.countingTeam == team
    val showResult = state.phase != RevealPhase.Counting
    val isWinner = showResult && state.winningTeam == team
    val isLoser = showResult && state.winningTeam != null && state.winningTeam != team

    val nameColor by animateColorAsState(
        targetValue = when {
            isWinner || isCounting -> Gold
            isLoser -> Dim
            else -> Color.White
        },
        animationSpec = tween(400),
    )
    val total by animateIntAsState(
        targetValue = state.shownTotal(team),
        animationSpec = tween(durationMillis = 350),
    )

    Box(modifier.fillMaxHeight()) {
        if (isWinner) Fireworks(Modifier.fillMaxSize())

        Column(
            Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                if (team == 1) state.team1Name else state.team2Name,
                color = nameColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                total.toString(),
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))

            // Fixed-height slot so the counter doesn't jump as pops and verdicts come and go.
            Box(Modifier.height(96.dp).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                when {
                    isWinner -> WinnerText()
                    isLoser -> LoserText()
                    showResult -> Text("BERABERE", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    else -> state.lastStep?.takeIf { it.team == team }?.let { step ->
                        key(state.stepNumber) { StepPop(step) }
                    }
                }
            }
        }
    }
}

/** "+87" (or "+100 · ceza · okey atma") rising into place for each counted number. */
@Composable
private fun StepPop(step: CountStep) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(280)) }

    Column(
        Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 24.dp.toPx()
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            if (step.delta >= 0) "+${step.delta}" else "${step.delta}",
            color = if (step.isPenalty) LoserRed else Gold,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
        if (step.isPenalty) {
            Text(
                listOfNotNull("ceza", step.label?.turkishLowercase()).joinToString(" · "),
                color = Dim,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun WinnerText() {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow))
    }
    Text(
        "KAZANDI!",
        color = Gold,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    )
}

@Composable
private fun LoserText() {
    val wobble = rememberInfiniteTransition()
    val rotation by wobble.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(tween(260), RepeatMode.Reverse),
    )
    val pulse by wobble.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
    )
    Text(
        "LOOOOSEEEER",
        color = LoserRed,
        // Sized to stay inside half of a 360dp-wide phone even at the pulse's peak.
        fontSize = 21.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.graphicsLayer {
            rotationZ = rotation
            scaleX = pulse
            scaleY = pulse
        },
    )
}

@Composable
private fun RankingPanel(ranking: List<PlayerRank>, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color.White.copy(alpha = 0.06f))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text("Oyuncu", color = Dim, fontSize = 13.sp, modifier = Modifier.weight(2f))
            listOf("Açış", "Bitiş", "Puan").forEach {
                Text(it, color = Dim, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(6.dp))
        ranking.forEach { rank ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(2f)) {
                    Text(rank.name, color = Color.White, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    rank.title?.let { title ->
                        Text(title.text, color = title.darkColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                listOf(rank.openings, rank.finishes, rank.score).forEachIndexed { index, number ->
                    Text(
                        number.toString(),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = if (index == 2) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Text(RankingFormula, color = Dim, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Yazboza dön", color = Gold, fontSize = 16.sp)
        }
    }
}

/** Title colour on the dark scene. */
private val DayTitle.darkColor: Color
    get() = when (this) {
        DayTitle.Carrier -> Gold
        DayTitle.Rat -> Color(0xFFBCAAA4)
    }

// ---------------------------------------------------------------- fireworks

private class Burst(
    val startMillis: Long,
    val x: Float,
    val y: Float,
    val color: Color,
    val angles: FloatArray,
    val speeds: FloatArray,
)

private val FireworkColors = listOf(
    Gold,
    Color(0xFFFF7043),
    Color(0xFF4FC3F7),
    Color(0xFFBA68C8),
    Color(0xFF81C784),
    Color.White,
)

private const val BurstCount = 7
private const val BurstSpacingMillis = 420L
private const val BurstLifeMillis = 1_500L
private const val SparksPerBurst = 40
private const val CycleMillis = BurstCount * BurstSpacingMillis + 600L

/** Looping particle bursts, drawn on a Canvas — no assets, no extra dependency. */
@Composable
private fun Fireworks(modifier: Modifier = Modifier) {
    val bursts = remember {
        val random = Random(seed = 101)
        List(BurstCount) { index ->
            Burst(
                startMillis = index * BurstSpacingMillis,
                x = random.nextFloat() * 0.7f + 0.15f,
                y = random.nextFloat() * 0.4f + 0.1f,
                color = FireworkColors[index % FireworkColors.size],
                angles = FloatArray(SparksPerBurst) { spark ->
                    (2 * PI * spark / SparksPerBurst).toFloat() + random.nextFloat() * 0.15f
                },
                speeds = FloatArray(SparksPerBurst) { random.nextFloat() * 0.45f + 0.55f },
            )
        }
    }
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) withFrameMillis { elapsed = it - start }
    }

    Canvas(modifier) {
        val reach = min(size.width, size.height) * 0.38f
        val sparkRadius = 3.dp.toPx()
        val cycleTime = elapsed % CycleMillis
        bursts.forEach { burst ->
            val age = cycleTime - burst.startMillis
            if (age < 0 || age > BurstLifeMillis) return@forEach
            val t = age.toFloat() / BurstLifeMillis
            val travel = 1f - (1f - t) * (1f - t) // ease-out
            val fall = t * t * reach * 0.35f
            val color = burst.color.copy(alpha = (1f - t).coerceIn(0f, 1f))
            val origin = Offset(burst.x * size.width, burst.y * size.height)
            for (i in 0 until SparksPerBurst) {
                val distance = reach * burst.speeds[i] * travel
                drawCircle(
                    color = color,
                    radius = sparkRadius * (1f - t * 0.6f),
                    center = Offset(
                        origin.x + cos(burst.angles[i]) * distance,
                        origin.y + sin(burst.angles[i]) * distance + fall,
                    ),
                )
            }
        }
    }
}

package com.spatel.scansign.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.spatel.scansign.core.ui.preview.ThemePreviews
import com.spatel.scansign.core.ui.theme.ScanSignTheme

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }

    if (step > 0) {
        BackHandler {
            step = 0
        }
    }

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally(initialOffsetX = { 1000 }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -1000 })
            } else {
                slideInHorizontally(initialOffsetX = { -1000 }) togetherWith
                        slideOutHorizontally(targetOffsetX = { 1000 })
            }
        },
        label = "onboarding_step_transition",
    ) { currentStep ->
        when (currentStep) {
            0 -> NameEntryStep(
                nameInput = viewModel.nameInput,
                isNameValid = viewModel.isNameValid,
                onNameChange = { viewModel.onNameChange(it) },
                onContinue = { step = 1 },
            )
            else -> TrustCardsStep(
                onGetStarted = {
                    viewModel.saveAndProceed(onComplete)
                },
            )
        }
    }
}

// ── Step 1: Name Entry ────────────────────────────────────────────────────────

@Composable
private fun NameEntryStep(
    nameInput: String,
    isNameValid: Boolean,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome to ScanSign",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your secure PDF scanner",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "What's your name?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onKeyEvent { event ->
                            if (event.key == Key.Enter && isNameValid) {
                                onContinue()
                                true
                            } else {
                                false
                            }
                        },
                    placeholder = { Text("Enter your name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isNameValid) onContinue()
                        },
                    ),
                    isError = nameInput.isNotEmpty() && !isNameValid,
                )

                if (nameInput.isNotEmpty() && !isNameValid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Name must be at least 5 characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onContinue,
                enabled = isNameValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Step 2: Trust Cards ───────────────────────────────────────────────────────

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun TrustCardsStep(
    onGetStarted: () -> Unit,
) {
    val pagerState = rememberPagerState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            HorizontalPager(
                count = 3,
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) { page ->
                TrustCard(page = page)
            }

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
            ) {
                repeat(3) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 8.dp)
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape,
                            ),
                    )
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            // Button (only show on last card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .height(56.dp)
            ) {
                if (pagerState.currentPage == 2) {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustCard(page: Int) {
    val (icon, title, body) = when (page) {
        0 -> Triple(
            Icons.Outlined.Lock,
            "Offline-First",
            "Your documents are stored securely on your device. Nothing is ever uploaded to the cloud.",
        )
        1 -> Triple(
            Icons.Outlined.CloudOff,
            "Private by Default",
            "We don't sync, backup, or transmit your files. No servers, no accounts — just your phone.",
        )
        else -> Triple(
            Icons.Outlined.Shield,
            "You're in Control",
            "No tracking. No analytics. No third-party access. Your files are 100% yours, always.",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@ThemePreviews
@Composable
private fun NameEntryStepPreview() {
    ScanSignTheme {
        NameEntryStep(
            nameInput = "Saumil",
            isNameValid = true,
            onNameChange = {},
            onContinue = {}
        )
    }
}

@ThemePreviews
@Composable
private fun NameEntryStepInvalidPreview() {
    ScanSignTheme {
        NameEntryStep(
            nameInput = "Sau",
            isNameValid = false,
            onNameChange = {},
            onContinue = {}
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@ThemePreviews
@Composable
private fun TrustCardsStepPreview() {
    ScanSignTheme {
        TrustCardsStep(onGetStarted = {})
    }
}

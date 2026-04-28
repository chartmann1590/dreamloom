package com.charles.app.dreamloom.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onDone: (String) -> Unit,
    vm: SplashViewModel = hiltViewModel(),
) {
    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DreamSpacing.xl),
            ) {
                Image(
                    painter = painterResource(R.drawable.ill_dream_hero),
                    contentDescription = stringResource(R.string.cd_home_hero),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(Modifier.height(DreamSpacing.md))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = DreamColors.Moonglow,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(DreamSpacing.sm))
                Text(
                    text = stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DreamColors.InkMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(DreamSpacing.xl))
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = DreamColors.AuroraSoft,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(900)
        onDone(vm.nextDestination())
    }
}

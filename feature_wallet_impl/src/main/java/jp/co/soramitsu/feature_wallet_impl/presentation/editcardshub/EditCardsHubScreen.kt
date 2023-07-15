package jp.co.soramitsu.feature_wallet_impl.presentation.editcardshub

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.presentation.compose.uikit.tokens.Text
import jp.co.soramitsu.common.presentation.compose.uikit.tokens.retrieveString
import jp.co.soramitsu.ui_core.component.card.ContentCard
import jp.co.soramitsu.ui_core.component.toolbar.BasicToolbarState
import jp.co.soramitsu.ui_core.component.toolbar.SoramitsuToolbar
import jp.co.soramitsu.ui_core.component.toolbar.SoramitsuToolbarState
import jp.co.soramitsu.ui_core.component.toolbar.SoramitsuToolbarType
import jp.co.soramitsu.ui_core.resources.Dimens
import jp.co.soramitsu.ui_core.theme.customColors
import jp.co.soramitsu.ui_core.theme.customTypography

data class EditCardsHubScreenState(
    val toolbarState: SoramitsuToolbarState,
    val enabledCardsHeader: Text,
    val enabledCards: List<Pair<Text, Boolean>>,
    val disabledCardsHeader: Text,
    val disabledCards: List<Pair<Text, Boolean>>
)

@Composable
fun EditCardsHubScreen(
    state: EditCardsHubScreenState,
    onCloseScreen: () -> Unit,
    onCardEnabled: (position: Int) -> Unit,
    onCardDisabled: (position: Int) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SoramitsuToolbar(
            state = state.toolbarState,
            elevation = Dp(-5f),
            onNavigate = onCloseScreen
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.x2)
                .padding(top = Dimens.x8)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.x2)
        ) {
            if (state.enabledCards.isNotEmpty()) {
                ContentCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.x3),
                        verticalArrangement = Arrangement.spacedBy(Dimens.x2)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = Dimens.x1_2),
                            text = state.enabledCardsHeader.retrieveString(),
                            style = MaterialTheme.customTypography.textS,
                            color = MaterialTheme.customColors.fgSecondary,
                        )
                        state.enabledCards.forEachIndexed { index, (name, isSelected) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCardEnabled.invoke(index) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.x1)
                            ) {
                                Image(
                                    modifier = Modifier.size(Dimens.x3),
                                    painter = painterResource(id = if (isSelected) R.drawable.ic_selected_accent_pin_24 else R.drawable.ic_selected_pin_empty_24),
                                    contentDescription = null
                                )
                                Text(
                                    text = name.retrieveString(),
                                    style = MaterialTheme.customTypography.textM,
                                    color = MaterialTheme.customColors.fgPrimary,
                                )
                            }
                        }
                    }
                }
            }

            if (state.disabledCards.isNotEmpty()) {
                ContentCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.x3),
                        verticalArrangement = Arrangement.spacedBy(Dimens.x2)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = Dimens.x1_2),
                            text = state.disabledCardsHeader.retrieveString(),
                            style = MaterialTheme.customTypography.textS,
                            color = MaterialTheme.customColors.fgSecondary,
                        )
                        state.disabledCards.forEachIndexed { index, (name, isSelected) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCardDisabled.invoke(index) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.x1)
                            ) {
                                Image(
                                    modifier = Modifier.size(Dimens.x3),
                                    painter = painterResource(id = if (isSelected) R.drawable.ic_selected_accent_pin_24 else R.drawable.ic_selected_pin_empty_24),
                                    contentDescription = null
                                )
                                Text(
                                    text = name.retrieveString(),
                                    style = MaterialTheme.customTypography.textM,
                                    color = MaterialTheme.customColors.fgPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewEditCardsHubScreen() {
    val enabledCards = remember {
        mutableStateOf<List<Pair<Text, Boolean>>>(
            listOf(
                Text.SimpleText("Sora Card") to false,
                Text.SimpleText("Sora") to true
            )
        )
    }
    val disabledCards = remember {
        mutableStateOf<List<Pair<Text, Boolean>>>(
            listOf(
                Text.SimpleText("Sora Card") to false,
                Text.SimpleText("Sora") to true
            )
        )
    }
    EditCardsHubScreen(
        state = EditCardsHubScreenState(
            toolbarState = SoramitsuToolbarState(
                basic = BasicToolbarState(
                    title = "Title",
                    navIcon = R.drawable.ic_cross_24
                ),
                type = SoramitsuToolbarType.SmallCentered()
            ),
            enabledCardsHeader = Text.SimpleText("Enabled"),
            enabledCards = enabledCards.value,
            disabledCardsHeader = Text.SimpleText("Disabled"),
            disabledCards = disabledCards.value
        ),
        onCardEnabled = { index ->
            enabledCards.value = enabledCards.value.run {
                mutableListOf<Pair<Text, Boolean>>().apply {
                    addAll(this@run)
                    set(index, get(index).first to !get(index).second)
                }
            }
        },
        onCardDisabled = { index ->
            disabledCards.value = disabledCards.value.run {
                mutableListOf<Pair<Text, Boolean>>().apply {
                    addAll(this@run)
                    set(index, get(index).first to !get(index).second)
                }
            }
        },
        onCloseScreen = {}
    )
}

package com.kg.yazzbozz.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun YazBozScreen(
    uiState: YazBozUiState,
    /*
    * */
)
{
    Box(
        modifier = Modifier.statusBarsPadding().fillMaxSize().background(Color.White)
    )
    {
        LazyColumn {  }
        Row(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            /*
            * number of penalties, same divider
            * penaltiesOfTeam1
            * vertical divider
            * penaltiesOfTeam2
            * */
        }
    }
}

@Composable
fun HandRow(
    scoreOfTeam1: Int,
    scoreOfTeam2: Int,
    playerFinished: Int = 0, // from left to right : 1 , 2 , 3 , 4. It should be matched with name.
    playersWhoOpenedTheirHand: ImmutableList<Int>
)
{
    /**
     *
     * resultOfTeam1 first Character of names if user opened their hand,if player is finished, put first name in circle
     *  vertical divider
     *  resultOfTeam2 first Character of names if user opened their hand,if player is finished, put first name in circle
     */
    Row(modifier = Modifier.fillMaxWidth()) {

    }
}
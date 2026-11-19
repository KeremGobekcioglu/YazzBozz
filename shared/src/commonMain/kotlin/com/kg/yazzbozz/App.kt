package com.kg.yazzbozz

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.kg.yazzbozz.navigation.AppNavGraph

@Composable
fun App(appGraph: AppGraph) {
    MaterialTheme {
        AppNavGraph(appGraph)
    }
}

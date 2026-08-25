package com.kg.yazzbozz

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kg.yazzbozz.navigation.AppNavGraph

@Composable
@Preview
fun App() {
    MaterialTheme {
        AppNavGraph()
    }
}
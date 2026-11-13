package com.example.fridgetoplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.fridgetoplate.ui.AppDestination
import com.example.fridgetoplate.ui.FridgeRoute
import com.example.fridgetoplate.ui.FridgeScreen
import com.example.fridgetoplate.ui.theme.FridgeToPlateTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            FridgeToPlateTheme {
                var selectedDestination by rememberSaveable { mutableStateOf(AppDestination.FRIDGE) }

                Scaffold(bottomBar = {
                    NavigationBar {
                        AppDestination.entries.forEach { destination ->
                            val labelRes = when(destination) {
                                AppDestination.FRIDGE ->  R.string.nav_fridge
                                AppDestination.SHOPPING -> R.string.nav_shopping
                                AppDestination.RECIPES -> R.string.nav_recipes
                            }
                            val iconRes = when (destination) {
                                AppDestination.FRIDGE -> R.drawable.ic_fridge
                                AppDestination.RECIPES -> R.drawable.ic_recipes
                                AppDestination.SHOPPING -> R.drawable.ic_shopping
                            }
                            NavigationBarItem(
                                selected = selectedDestination == destination,
                                onClick = {selectedDestination = destination},
                                icon = {
                                    Icon(
                                        painter = painterResource(iconRes),
                                        contentDescription = null
                                    )
                                },
                                label = {Text(stringResource(labelRes))}
                            )
                        }
                    }
                }) { innerPadding ->
                    FridgeRoute(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
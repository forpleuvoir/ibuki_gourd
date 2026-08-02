package moe.forpleuvoir.ibukigourd.mod.waht

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.mod.ui.DrawerItem
import moe.forpleuvoir.ibukigourd.mod.ui.LocalDrawerItemSelected
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.defaults.StadiaController
import moe.forpleuvoir.ibukigourd.ui.icon.filled.StadiaController

internal val EasterEggs = DrawerItem(
    label = { Text("???") },
    icon = {
        val selected = LocalDrawerItemSelected.current
        Icon(if (selected) Icons.Filled.StadiaController else Icons.StadiaController, null)
    },
    content = { EasterEggs() }
)

@Composable
private fun EasterEggs() {
    var selectedGame by remember { mutableStateOf("snake") }
    PermanentNavigationDrawer(
        modifier = Modifier,
        drawerContent = {
            PermanentDrawerSheet(Modifier.width(240.dp)) {
                NavigationDrawerItem(
                    modifier = Modifier
                        .height(42.dp)
                        .padding(NavigationDrawerItemDefaults.ItemPadding),
                    shape = MaterialTheme.shapes.large,
                    label = {
                        Text("snake")
                    },
                    selected = selectedGame == "snake",
                    onClick = {
                        selectedGame = "snake"
                    }
                )
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    modifier = Modifier
                        .height(42.dp)
                        .padding(NavigationDrawerItemDefaults.ItemPadding),
                    shape = MaterialTheme.shapes.large,
                    label = {
                        Text("game of life")
                    },
                    selected = selectedGame == "game of life",
                    onClick = {
                        selectedGame = "game of life"
                    }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (selectedGame == "snake") {
                SnakeGame()
            } else if (selectedGame == "game of life") {
                GameOfLife(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
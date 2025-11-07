package com.droidbaza.spotifycompose.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import com.droidbaza.spotifycompose.network.auth.TokenStores
import com.droidbaza.spotifycompose.screens.MainScreen
import com.droidbaza.spotifycompose.ui.login.LoginScreen
import com.droidbaza.spotifycompose.ui.login.LoginViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun RootScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val store = remember { TokenStores.get(context) }
    val access by store.accessTokenFlow.collectAsState(initial = null)

    if (!access.isNullOrBlank()) {
        MainScreen(finish = onFinish)
    } else {
        val vm: LoginViewModel = viewModel()
        LoginScreen(vm = vm, onSuccess = { /* TokenStore mis à jour -> recomposition -> MainScreen */ })
    }
}

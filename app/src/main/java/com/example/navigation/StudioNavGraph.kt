package com.example.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ai.StudioContainer
import com.example.screens.AiToolsHubScreen
import com.example.screens.CreativeToolScreen
import com.example.screens.DownloadsScreen
import com.example.screens.HistoryScreen
import com.example.screens.HomeScreen
import com.example.screens.ImageStudioScreen
import com.example.screens.SettingsScreen
import com.example.screens.VideoStudioScreen
import com.example.types.AiToolId
import com.example.types.ImageMode
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CreativeToolViewModel
import com.example.viewmodel.DownloadsViewModel
import com.example.viewmodel.HistoryViewModel
import com.example.viewmodel.ImageStudioViewModel
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.VideoStudioViewModel

private data class TopNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val topTabs = listOf(
    TopNavTab(StudioRoutes.ROUTE_HOME, "Studio", Icons.Filled.Home, Icons.Outlined.Home),
    TopNavTab(StudioRoutes.ROUTE_IMAGE_STUDIO, "Image", Icons.Filled.Image, Icons.Outlined.Image),
    TopNavTab(StudioRoutes.ROUTE_VIDEO_STUDIO, "Video", Icons.Filled.Videocam, Icons.Outlined.Videocam),
    TopNavTab(StudioRoutes.ROUTE_AI_TOOLS, "AI Tools", Icons.Filled.AutoFixHigh, Icons.Outlined.AutoFixHigh),
    TopNavTab(StudioRoutes.ROUTE_HISTORY, "History", Icons.Filled.History, Icons.Outlined.History)
)

@Composable
fun StudioNavGraph(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = StudioContainer.getInstance(context)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val imageViewModel: ImageStudioViewModel = viewModel(
        factory = ImageStudioViewModel.provideFactory(
            container.imageAiService,
            container.creationRepository,
            container.downloadService,
            container.settingsDataStore
        )
    )
    val videoViewModel: VideoStudioViewModel = viewModel(
        factory = VideoStudioViewModel.provideFactory(
            container.videoAiService,
            container.creationRepository,
            container.downloadService,
            container.settingsDataStore
        )
    )
    val toolViewModel: CreativeToolViewModel = viewModel(
        factory = CreativeToolViewModel.provideFactory(
            container.creativeTextAiService,
            container.creationRepository,
            container.downloadService
        )
    )
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.provideFactory(
            container.creationRepository,
            container.downloadService
        )
    )
    val downloadsViewModel: DownloadsViewModel = viewModel(
        factory = DownloadsViewModel.provideFactory(container.creationRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            container.settingsDataStore,
            container.creationRepository,
            container.imageAiService,
            container.videoAiService
        )
    )

    val showTopNav = currentRoute in topTabs.map { it.route }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = StudioObsidian,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showTopNav && !useRail) {
                    NavigationBar(
                        containerColor = StudioSurface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        topTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyanSpark,
                                    selectedTextColor = CyanSpark,
                                    indicatorColor = IndigoElectric.copy(alpha = 0.25f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("bottom_nav_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopNav && useRail) {
                    NavigationRail(containerColor = StudioSurface) {
                        topTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CyanSpark,
                                    selectedTextColor = CyanSpark,
                                    indicatorColor = IndigoElectric.copy(alpha = 0.25f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = StudioRoutes.ROUTE_HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(StudioRoutes.ROUTE_HOME) {
                        HomeScreen(
                            onOpenImageStudio = {
                                imageViewModel.updateMode(ImageMode.TEXT_TO_IMAGE)
                                navigateTopLevel(StudioRoutes.ROUTE_IMAGE_STUDIO)
                            },
                            onOpenImageEditing = {
                                imageViewModel.updateMode(ImageMode.IMAGE_EDIT)
                                navigateTopLevel(StudioRoutes.ROUTE_IMAGE_STUDIO)
                            },
                            onOpenVideoStudio = { navigateTopLevel(StudioRoutes.ROUTE_VIDEO_STUDIO) },
                            onOpenAiToolsHub = { navigateTopLevel(StudioRoutes.ROUTE_AI_TOOLS) },
                            onOpenTool = { toolId ->
                                navController.navigate(StudioRoutes.toolDetail(toolId.name))
                            },
                            onOpenHistory = { navigateTopLevel(StudioRoutes.ROUTE_HISTORY) },
                            onOpenDownloads = { navController.navigate(StudioRoutes.ROUTE_DOWNLOADS) },
                            onOpenSettings = { navController.navigate(StudioRoutes.ROUTE_SETTINGS) }
                        )
                    }

                    composable(StudioRoutes.ROUTE_IMAGE_STUDIO) {
                        ImageStudioScreen(viewModel = imageViewModel)
                    }

                    composable(StudioRoutes.ROUTE_VIDEO_STUDIO) {
                        VideoStudioScreen(viewModel = videoViewModel)
                    }

                    composable(StudioRoutes.ROUTE_AI_TOOLS) {
                        AiToolsHubScreen(
                            onOpenTool = { toolId ->
                                navController.navigate(StudioRoutes.toolDetail(toolId.name))
                            }
                        )
                    }

                    composable(
                        route = StudioRoutes.ROUTE_TOOL_DETAIL,
                        arguments = listOf(navArgument("toolId") { type = NavType.StringType })
                    ) { entry ->
                        val rawName = entry.arguments?.getString("toolId").orEmpty()
                        val toolId = runCatching { AiToolId.valueOf(rawName) }.getOrDefault(AiToolId.PROMPT_ENHANCER)
                        CreativeToolScreen(
                            toolId = toolId,
                            viewModel = toolViewModel,
                            onBack = { navController.popBackStack() },
                            onSendToImageStudio = { prompt ->
                                imageViewModel.applyPrefill(prompt, ImageMode.TEXT_TO_IMAGE)
                                navigateTopLevel(StudioRoutes.ROUTE_IMAGE_STUDIO)
                            },
                            onSendToVideoStudio = { prompt ->
                                videoViewModel.applyPrefill(prompt)
                                navigateTopLevel(StudioRoutes.ROUTE_VIDEO_STUDIO)
                            }
                        )
                    }

                    composable(StudioRoutes.ROUTE_HISTORY) {
                        HistoryScreen(viewModel = historyViewModel)
                    }

                    composable(StudioRoutes.ROUTE_DOWNLOADS) {
                        DownloadsScreen(
                            viewModel = downloadsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(StudioRoutes.ROUTE_SETTINGS) {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

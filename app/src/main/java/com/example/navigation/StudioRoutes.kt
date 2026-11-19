package com.example.navigation

object StudioRoutes {
    const val ROUTE_HOME = "home"
    const val ROUTE_IMAGE_STUDIO = "image_studio"
    const val ROUTE_VIDEO_STUDIO = "video_studio"
    const val ROUTE_AI_TOOLS = "ai_tools"
    const val ROUTE_TOOL_DETAIL = "tool_detail/{toolId}"
    const val ROUTE_HISTORY = "history"
    const val ROUTE_DOWNLOADS = "downloads"
    const val ROUTE_SETTINGS = "settings"

    fun toolDetail(toolIdName: String): String = "tool_detail/$toolIdName"
}

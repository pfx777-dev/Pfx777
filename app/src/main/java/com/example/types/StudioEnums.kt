package com.example.types

enum class CreationType(val displayName: String) {
    IMAGE("Image"),
    VIDEO("Video"),
    PROMPT_TOOL("AI Tool")
}

enum class ImageMode(val label: String, val requiresSourceImage: Boolean) {
    TEXT_TO_IMAGE("Text to Image", false),
    IMAGE_TO_IMAGE("Image to Image", true),
    IMAGE_EDIT("Image Editing", true),
    AI_TRANSFORM("AI Transform", true)
}

enum class ImageStyle(val label: String, val promptModifier: String) {
    REALISTIC("Realistic", "photorealistic, ultra detailed, natural lighting, 8k lens"),
    ANIME("Anime", "modern high-budget anime key visual, crisp cel shading, expressive light"),
    CARTOON("Cartoon", "stylized 2D cartoon illustration, bold clean outlines, vibrant palette"),
    CINEMATIC("Cinematic", "cinematic film still, anamorphic bokeh, moody volumetric lighting"),
    THREE_D_ART("3D Art", "stylized 3D octane render, subsurface scattering, studio rim light"),
    FANTASY_ART("Fantasy Art", "epic fantasy concept art, ethereal atmosphere, intricate detail"),
    ILLUSTRATION("Illustration", "editorial digital illustration, textured brushwork, balanced composition"),
    PORTRAIT_ART("Portrait Art", "expressive studio portrait, soft key light, shallow depth of field"),
    LANDSCAPE_ART("Landscape Art", "sweeping atmospheric landscape vista, golden hour depth"),
    ANIME_BACKGROUNDS("Anime Backgrounds", "scenic anime background painting, luminous clouds, Makoto-inspired light"),
    CUSTOM("Custom Style", "")
}

enum class VideoMode(val label: String, val requiresSourceImage: Boolean) {
    TEXT_TO_VIDEO("Text to Video", false),
    IMAGE_TO_VIDEO("Image to Video", true),
    AI_ANIMATION("AI Animation", false)
}

enum class VideoStyle(val label: String, val promptModifier: String) {
    CINEMATIC("Cinematic", "cinematic 24fps film motion, dramatic lighting, smooth depth parallax"),
    ANIME("Anime Video", "fluid anime sakuga animation, dynamic wind & light trails"),
    CARTOON("Cartoon Video", "expressive vibrant cartoon motion, squash-and-stretch energy"),
    REALISTIC("Realistic Video", "lifelike natural physics, stabilized camera movement"),
    FANTASY("Fantasy Video", "magical floating particles, glowing atmosphere, epic scale"),
    CREATIVE("Creative Motion", "experimental kinetic visuals, iridescent light transitions"),
    CUSTOM("Custom Style", "")
}

enum class CameraMotion(val label: String, val code: String) {
    STATIC("Locked Tripod", "static"),
    PAN_LEFT("Slow Pan Left", "pan_left"),
    PAN_RIGHT("Slow Pan Right", "pan_right"),
    DOLLY_IN("Dolly Zoom In", "dolly_in"),
    ORBIT("Orbit 360°", "orbit"),
    CRANE_UP("Crane Reveal Up", "crane_up"),
    DYNAMIC("Dynamic Handheld", "dynamic")
}

enum class StudioAspectRatio(val label: String, val width: Int, val height: Int) {
    RATIO_1_1("1:1", 1024, 1024),
    RATIO_16_9("16:9", 1280, 720),
    RATIO_9_16("9:16", 720, 1280),
    RATIO_4_3("4:3", 1024, 768),
    RATIO_3_4("3:4", 768, 1024)
}

enum class ImageResolution(val label: String, val scaleFactor: Float) {
    RES_1K("1K Standard", 1.0f),
    RES_2K("2K High", 1.4f),
    RES_4K("4K Ultra", 1.8f)
}

enum class VideoDuration(val label: String, val seconds: Int) {
    SEC_4("4 sec", 4),
    SEC_6("6 sec", 6),
    SEC_8("8 sec", 8)
}

enum class VideoQuality(val label: String, val widthPx: Int, val heightPx: Int) {
    HD_720("720p HD", 1280, 720),
    FHD_1080("1080p Full HD", 1920, 1080)
}

enum class AiToolCategory(val label: String) {
    IMAGE_EDITING("Image Editing & FX"),
    GENERATION("Creative Generation"),
    PROMPT_CRAFT("Prompt Engineering"),
    TEMPLATES("Prompt Templates")
}

enum class AiToolId(
    val title: String,
    val subtitle: String,
    val category: AiToolCategory,
    val requiresImageInput: Boolean,
    val outputsImage: Boolean,
    val outputsVideoPreview: Boolean = false
) {
    PROMPT_ENHANCER(
        "AI Prompt Enhancer",
        "Expand brief ideas into richly detailed, lighting-aware creative prompts",
        AiToolCategory.PROMPT_CRAFT,
        requiresImageInput = false,
        outputsImage = false
    ),
    STYLE_CHANGER(
        "Image Style Changer",
        "Restyle any uploaded photo into anime, 3D, cyberpunk, watercolor, or oil paint",
        AiToolCategory.IMAGE_EDITING,
        requiresImageInput = true,
        outputsImage = true
    ),
    AI_IMAGE_EDITOR(
        "AI Image Editor",
        "Apply targeted edits, color grading, lighting shifts, and creative retouching",
        AiToolCategory.IMAGE_EDITING,
        requiresImageInput = true,
        outputsImage = true
    ),
    BACKGROUND_CHANGE(
        "Background Change",
        "Swap scene backdrops with studio neon, sunset vistas, or minimal sets",
        AiToolCategory.IMAGE_EDITING,
        requiresImageInput = true,
        outputsImage = true
    ),
    BACKGROUND_REMOVAL(
        "Background Removal",
        "Isolate subject silhouettes with clean cutout preview (cloud segmentation ready)",
        AiToolCategory.IMAGE_EDITING,
        requiresImageInput = true,
        outputsImage = true
    ),
    IMAGE_UPSCALING(
        "Image Upscaling",
        "Enhance sharpness, clarity, and resolution up to 4x studio dimensions",
        AiToolCategory.IMAGE_EDITING,
        requiresImageInput = true,
        outputsImage = true
    ),
    AI_ART_GENERATOR(
        "AI Art Generator",
        "Craft gallery-grade digital artwork with curated artistic movements",
        AiToolCategory.GENERATION,
        requiresImageInput = false,
        outputsImage = true
    ),
    AI_CHARACTER_CREATOR(
        "AI Character Creator",
        "Design consistent heroes, anime avatars, and fantasy character sheets",
        AiToolCategory.GENERATION,
        requiresImageInput = false,
        outputsImage = true
    ),
    IMAGE_TO_ANIMATION(
        "Image-to-Animation",
        "Breathe dynamic camera motion and atmospheric particles into still images",
        AiToolCategory.GENERATION,
        requiresImageInput = true,
        outputsImage = false,
        outputsVideoPreview = true
    ),
    VIDEO_PROMPT_GENERATOR(
        "Video Prompt Generator",
        "Structure camera movement, pacing, and motion cues for video models",
        AiToolCategory.PROMPT_CRAFT,
        requiresImageInput = false,
        outputsImage = false
    ),
    CINEMATIC_PROMPT_GENERATOR(
        "Cinematic Prompt Generator",
        "Compose 35mm lens specs, anamorphic lighting, and color-grade prompts",
        AiToolCategory.PROMPT_CRAFT,
        requiresImageInput = false,
        outputsImage = false
    ),
    ANIME_PROMPT_GENERATOR(
        "Anime Prompt Generator",
        "Build authentic cel-shaded, sakuga, and scenic anime studio prompts",
        AiToolCategory.PROMPT_CRAFT,
        requiresImageInput = false,
        outputsImage = false
    ),
    CARTOON_PROMPT_GENERATOR(
        "Cartoon Prompt Generator",
        "Generate playful 2D/3D toon character and world-building prompts",
        AiToolCategory.PROMPT_CRAFT,
        requiresImageInput = false,
        outputsImage = false
    ),
    AI_SCENE_CREATOR(
        "AI Scene Creator",
        "Architect complete world environments with foreground, midground, and mood",
        AiToolCategory.GENERATION,
        requiresImageInput = false,
        outputsImage = true
    ),
    AI_THUMBNAIL_CREATOR(
        "AI Thumbnail Creator",
        "Design bold, high-contrast 16:9 creator thumbnails with focal composition",
        AiToolCategory.GENERATION,
        requiresImageInput = false,
        outputsImage = true
    ),
    TEXT_TO_IMAGE_TEMPLATES(
        "Text-to-Image Prompt Templates",
        "One-tap curated blueprints for portraits, logos, anime, and fantasy art",
        AiToolCategory.TEMPLATES,
        requiresImageInput = false,
        outputsImage = false
    ),
    TEXT_TO_VIDEO_TEMPLATES(
        "Text-to-Video Prompt Templates",
        "Ready-to-use motion scripts for drone reveals, anime loops, and reels",
        AiToolCategory.TEMPLATES,
        requiresImageInput = false,
        outputsImage = false
    )
}

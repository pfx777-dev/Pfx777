package com.example.utilities

data class PromptTemplateItem(
    val id: String,
    val title: String,
    val category: String,
    val prompt: String,
    val styleHint: String,
    val recommendedAspect: String
)

object PromptTemplates {
    val imageTemplates = listOf(
        PromptTemplateItem(
            id = "img_cyberpunk_portrait",
            title = "Neon Cyberpunk Portrait",
            category = "Portrait Art",
            prompt = "Close-up editorial portrait of a futuristic traveler wearing holographic visor glasses, rain-slicked Tokyo alley reflections, cyan and magenta rim lighting, 85mm f/1.4 lens",
            styleHint = "Cinematic",
            recommendedAspect = "3:4"
        ),
        PromptTemplateItem(
            id = "img_anime_skyline",
            title = "Golden Hour Anime Train Station",
            category = "Anime Backgrounds",
            prompt = "Lush scenic anime background of an elevated seaside train platform at sunset, towering cumulus clouds tinted coral and gold, warm lens flare, ultra detailed hand-painted sky",
            styleHint = "Anime Backgrounds",
            recommendedAspect = "16:9"
        ),
        PromptTemplateItem(
            id = "img_fantasy_citadel",
            title = "Floating Crystal Citadel",
            category = "Fantasy Art",
            prompt = "Ancient floating stone citadel suspended above a bioluminescent cloud sea, cascading waterfalls, glowing arcane runes, epic matte painting scale, volumetric god rays",
            styleHint = "Fantasy Art",
            recommendedAspect = "16:9"
        ),
        PromptTemplateItem(
            id = "img_3d_mascot",
            title = "3D Stylized Studio Character",
            category = "3D Art",
            prompt = "Charming 3D vinyl toy astronaut holding a glowing star lantern, soft clay textures, subsurface scattering, clean obsidian studio backdrop, rim light",
            styleHint = "3D Art",
            recommendedAspect = "1:1"
        ),
        PromptTemplateItem(
            id = "img_cartoon_adventure",
            title = "Vibrant Cartoon Explorer",
            category = "Cartoon",
            prompt = "Energetic 2D cartoon explorer fox with brass goggles and map backpack standing on a cliff overlooking a whimsical valley, crisp ink linework, bright cel colors",
            styleHint = "Cartoon",
            recommendedAspect = "4:3"
        ),
        PromptTemplateItem(
            id = "img_product_luxury",
            title = "Obsidian Product Hero Shot",
            category = "Realistic",
            prompt = "Minimalist luxury smartwatch resting on dark volcanic basalt rock with subtle mist, crisp specular highlights, commercial studio photography, macro sharpness",
            styleHint = "Realistic",
            recommendedAspect = "1:1"
        )
    )

    val videoTemplates = listOf(
        PromptTemplateItem(
            id = "vid_drone_alpine",
            title = "Alpine Sunrise Drone Reveal",
            category = "Cinematic",
            prompt = "Smooth aerial drone reveal soaring over mist-covered pine ridges into a blazing golden alpine sunrise, drifting clouds, 24fps film grain, anamorphic flare",
            styleHint = "Cinematic",
            recommendedAspect = "16:9"
        ),
        PromptTemplateItem(
            id = "vid_anime_sakura",
            title = "Anime Wind & Sakura Loop",
            category = "Anime Video",
            prompt = "Anime protagonist standing on a rooftop as cherry blossom petals sweep diagonally across the frame, hair and coat billowing in slow motion, vibrant sunset sky",
            styleHint = "Anime Video",
            recommendedAspect = "9:16"
        ),
        PromptTemplateItem(
            id = "vid_cyber_chase",
            title = "Neon Street Dolly Push",
            category = "Creative Motion",
            prompt = "Fast forward dolly push through a futuristic neon bazaar with holographic signs reflecting in puddles, glowing embers rising, dynamic depth of field",
            styleHint = "Creative Motion",
            recommendedAspect = "9:16"
        ),
        PromptTemplateItem(
            id = "vid_fantasy_portal",
            title = "Arcane Forest Portal",
            category = "Fantasy Video",
            prompt = "Slow orbital camera sweep around a glowing emerald portal nestled inside ancient twisted roots, floating fireflies and luminous mist swirling inward",
            styleHint = "Fantasy Video",
            recommendedAspect = "16:9"
        ),
        PromptTemplateItem(
            id = "vid_cartoon_bounce",
            title = "Playful Cartoon Rocket Launch",
            category = "Cartoon Video",
            prompt = "Vibrant retro cartoon rocket wobbling on launchpad before blasting off into starry clouds with puffy stylized smoke rings, expressive smear frames",
            styleHint = "Cartoon Video",
            recommendedAspect = "1:1"
        )
    )
}

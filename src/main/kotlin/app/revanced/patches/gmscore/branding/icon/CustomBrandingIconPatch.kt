package app.revanced.patches.gmscore.branding.icon

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyFile
import app.revanced.util.copyResources
import app.revanced.patcher.patch.ResourcePatch

import app.revanced.util.underBarOrThrow

//@Suppress("unused")
object CustomBrandingIconPatch : ResourcePatch(
    name = "Custom branding icon",
    description = "Changes the app icon to the icon specified in options.json.",
    compatiblePackages = setOf(
        CompatiblePackage("app.revanced.android.gms")
            )
) {
    private const val DEFAULT_ICON = "revancify_blue"

    private val availableIcon = mapOf(
        "MMT" to "mmt",
        "Revancify Blue" to DEFAULT_ICON,
        "Revancify Red" to "revancify_red"
    )

    private val sizeArray = arrayOf(
        "xxxhdpi",
        "xxhdpi",
        "xhdpi",
        "hdpi",
        "mdpi",
    )

    private val mipmapDirectories = sizeArray.map { "mipmap-$it" }

    private val launcherIconResourceFileNames = arrayOf(
        "ic_app",
        "ic_app_round",
        "ic_app_foreground",
        "ic_app_foreground_mono",
        "ic_app_background"
    ).map { "$it.png" }.toTypedArray()

    private fun List<String>.getResourceGroup(fileNames: Array<String>) = map { directory ->
        ResourceGroup(
            directory, *fileNames
        )
    }

    private val iconResourceGroups =
        mipmapDirectories.getResourceGroup(launcherIconResourceFileNames)

    // region patch option

    val AppIcon = stringPatchOption(
        key = "AppIcon",
        default = DEFAULT_ICON,
        values = availableIcon,
        title = "App icon",
        description = """
            The icon to apply to the app.
            
            If a path to a folder is provided, the folder must contain the following folders:

            ${mipmapDirectories.joinToString("\n") { "- $it" }}

            Each of these folders must contain the following files:

            ${launcherIconResourceFileNames.joinToString("\n") { "- $it" }}
            """.trimIndentMultiline(),
        required = true
    )

    // endregion

    override fun execute(context: ResourceContext) {

        // Check patch options first.
        val appIcon = AppIcon
            .underBarOrThrow()

        val appIconResourcePath = "gmscore/branding/icon/$appIcon"

        context.copyResources("gmscore/branding/icon", ResourceGroup("mipmap-anydpi-v26", "ic_app.xml"))
        // Check if a custom path is used in the patch options.
        if (!availableIcon.containsValue(appIcon)) {
            context.copyFile(
                iconResourceGroups,
                appIcon,
                "WARNING: Invalid app icon path: $appIcon. Does not apply patches."
            )
        } else {
            // Change launcher icon.

            iconResourceGroups.let { resourceGroups ->
                resourceGroups.forEach {
                    context.copyResources(appIconResourcePath, it)
                }
            }
        }
        val manifest = context.get("AndroidManifest.xml", false)
        manifest.writeText(
            manifest.readText()
                .replace(
                    "\"@mipmap/ic_app_settings\"",
                    "\"@mipmap/ic_app\"",
                ).replace(
                    "android:roundIcon=\"@mipmap/ic_app\"",
                    "android:roundIcon=\"@mipmap/ic_app_round\""
                ),
        )
    }
}

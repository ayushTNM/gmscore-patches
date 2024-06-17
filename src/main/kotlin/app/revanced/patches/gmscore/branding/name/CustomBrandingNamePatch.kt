package app.revanced.patches.gmscore.branding.name

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.util.StringsElementsUtils.removeStringsElements
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.util.valueOrThrow

//@Suppress("DEPRECATION", "unused")
object CustomBrandingNamePatch : ResourcePatch(
    name = "Custom branding name",
    description = "Renames GmsCore to the name specified in options.json.",
    compatiblePackages = setOf(
        CompatiblePackage("app.revanced.android.gms")
    )
) {
    private const val APP_NAME = "MicroG"

    private val AppName = stringPatchOption(
        key = "AppName",
        default = APP_NAME,
        values = mapOf(
            "MicroG" to APP_NAME,
            "GmsCore" to "GmsCore"
        ),
        title = "App name",
        description = "The name of the app.",
        required = true
    )

    override fun execute(context: ResourceContext) {

        // Check patch options first.
        val appName = AppName
            .valueOrThrow()

        context.removeStringsElements(
            arrayOf("gms_app_name", "gms_settings_name")
        )

        context.xmlEditor["res/values/strings.xml"].use { editor ->
            val document = editor.file

            mapOf(
                "gms_app_name" to appName,
                "gms_settings_name" to appName
            ).forEach { (k, v) ->
                val stringElement = document.createElement("string")

                stringElement.setAttribute("name", k)
                stringElement.textContent = v

                document.getElementsByTagName("resources").item(0).appendChild(stringElement)
            }
        }
    }
}

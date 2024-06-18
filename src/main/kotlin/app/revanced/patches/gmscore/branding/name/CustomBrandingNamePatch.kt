package app.revanced.patches.gmscore.branding.name

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.util.valueOrThrow
import org.w3c.dom.Element


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

        val stockNames = arrayOf("microG GmsCore", "microG", "GmsCore")

        val resourceDirectory = context.get("res", false)

        for (file in resourceDirectory.listFiles()!!) {
            val path = file.name
            if (!path.startsWith("values")) continue

            val targetXml = resourceDirectory.resolve(path).resolve("strings.xml")

            if (!targetXml.exists()) continue

            context.xmlEditor[targetXml.absolutePath].use { editor ->
                val resourcesNode =
                    editor.file.getElementsByTagName("resources").item(0) as Element
                var label = ""

                for (i in 0 until resourcesNode.childNodes.length) {

                    val element = resourcesNode.childNodes.item(i) as? Element ?: continue
                    when (element.getAttribute("name")) {
                        "gms_app_name" -> {
                            if (label == "")
                                label = element.textContent

                            element.textContent = appName
                        }

                        "gms_settings_name" -> element.textContent = appName

                        else -> continue
                    }
                }

                for (i in 0 until resourcesNode.childNodes.length) {
                    val element = resourcesNode.childNodes.item(i) as? Element ?: continue
                    arrayOf(label, *stockNames).forEach {
                        if (element.textContent.contains(it)) {
                            element.textContent = element.textContent.replace(it, appName)
                            return@forEach
                        }
                    }
                }
            }
        }
    }
}

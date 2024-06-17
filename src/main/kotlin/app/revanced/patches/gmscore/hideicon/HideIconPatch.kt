package app.revanced.patches.gmscore.hideicon

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch

object HideIconPatch : ResourcePatch(
    name = "Hide icon from launcher",
    description = "Hide GmsCore icon from launcher.",
    compatiblePackages = setOf(
        CompatiblePackage("app.revanced.android.gms")
    )
) {
    override fun execute(context: ResourceContext) {
        val manifest = context.get("AndroidManifest.xml", false)
        manifest.writeText(
            manifest.readText()
                .replace(
                    "android:name=\"android.intent.category.LAUNCHER",
                    "android:name=\"android.intent.category.DEFAULT",
                ),
        )
    }
}

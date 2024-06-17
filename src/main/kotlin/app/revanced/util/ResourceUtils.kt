package app.revanced.util

import app.revanced.patcher.patch.options.PatchOption
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import java.io.File
import java.nio.file.Files
import java.io.InputStream
import java.nio.file.StandardCopyOption

private val classLoader = object {}.javaClass.classLoader


fun PatchOption<String>.valueOrThrow() = value
    ?: throw PatchException("Invalid patch option: $title.")

fun PatchOption<String>.lowerCaseOrThrow() = valueOrThrow()
    .lowercase()

fun PatchOption<String>.underBarOrThrow() = lowerCaseOrThrow()
    .replace(" ", "_")

/**
 * Copy resources from the current class loader to the resource directory.
 *
 * @param sourceResourceDirectory The source resource directory name.
 * @param resources The resources to copy.
 */
fun ResourceContext.copyResources(
    sourceResourceDirectory: String,
    vararg resources: ResourceGroup,
) {
    val targetResourceDirectory = this.get("res")

    for (resourceGroup in resources) {
        resourceGroup.resources.forEach { resource ->
            val resourceFile = "${resourceGroup.resourceDirectoryName}/$resource"
            Files.copy(
                inputStreamFromBundledResource(sourceResourceDirectory, resourceFile)!!,
                targetResourceDirectory.resolve(resourceFile).toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }
}

internal fun inputStreamFromBundledResource(
    sourceResourceDirectory: String,
    resourceFile: String,
): InputStream? = classLoader.getResourceAsStream("$sourceResourceDirectory/$resourceFile")


/**
 * Resource names mapped to their corresponding resource data.
 * @param resourceDirectoryName The name of the directory of the resource.
 * @param resources A list of resource names.
 */
class ResourceGroup(val resourceDirectoryName: String, vararg val resources: String)

fun ResourceContext.copyFile(
    resourceGroup: List<ResourceGroup>,
    path: String,
    warning: String
): Boolean {
    resourceGroup.let { resourceGroups ->
        try {
            val filePath = File(path)
            val resourceDirectory = this["res"]

            resourceGroups.forEach { group ->
                val fromDirectory = filePath.resolve(group.resourceDirectoryName)
                val toDirectory = resourceDirectory.resolve(group.resourceDirectoryName)

                group.resources.forEach { iconFileName ->
                    Files.write(
                        toDirectory.resolve(iconFileName).toPath(),
                        fromDirectory.resolve(iconFileName).readBytes()
                    )
                }
            }

            return true
        } catch (_: Exception) {
            println(warning)
        }
    }
    return false
}
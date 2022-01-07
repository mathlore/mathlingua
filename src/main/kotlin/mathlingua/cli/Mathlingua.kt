/*
 * Copyright 2021 The MathLingua Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mathlingua.cli

import java.io.File
import java.io.InputStream
import java.util.jar.JarFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mathlingua.backend.BackEnd
import mathlingua.backend.SourceCollection
import mathlingua.backend.SourceFile
import mathlingua.backend.ValueSourceTracker
import mathlingua.backend.buildSourceFile
import mathlingua.backend.findMathLinguaFiles
import mathlingua.backend.newSourceCollection
import mathlingua.backend.newSourceCollectionFromCwd
import mathlingua.frontend.chalktalk.phase2.ast.common.Phase2Node
import mathlingua.frontend.chalktalk.phase2.ast.group.toplevel.TopLevelBlockComment
import mathlingua.frontend.support.ParseError
import mathlingua.frontend.support.validationFailure
import mathlingua.getRandomUuid

const val MATHLINGUA_VERSION = "0.14.0"

object Mathlingua {
    fun check(fs: VirtualFileSystem, logger: Logger, files: List<VirtualFile>, json: Boolean): Int {
        val sourceCollection =
            newSourceCollection(fs, files.ifEmpty { listOf(fs.getDirectory(listOf("content"))) })
        val errors = BackEnd.check(sourceCollection)
        logger.log(getErrorOutput(errors, sourceCollection.size(), json))
        return if (errors.isEmpty()) {
            0
        } else {
            1
        }
    }

    private fun export(
        fs: VirtualFileSystem, logger: Logger
    ): List<ValueSourceTracker<ParseError>> {
        val files = findMathLinguaFiles(listOf(getContentDirectory(fs)))

        val errors = mutableListOf<ValueSourceTracker<ParseError>>()
        for (target in files) {
            errors.addAll(
                exportFile(
                    fs = fs,
                    logger = logger,
                    target = target,
                    stdout = false,
                    noExpand = false,
                    raw = false))
        }

        return errors
    }

    fun render(fs: VirtualFileSystem, logger: Logger): Int {
        val result = renderAll(fs = fs, logger = logger)
        val files = result.first
        val errors = mutableListOf<ErrorResult>()
        errors.addAll(result.second)
        errors.addAll(
            export(fs = fs, logger = logger).map {
                ErrorResult(
                    relativePath = it.source.file.relativePath(),
                    message = it.value.message,
                    row = it.value.row,
                    column = it.value.column)
            })
        logger.log(
            getErrorOutput(
                errors.map {
                    ValueSourceTracker(
                        value = ParseError(message = it.message, row = it.row, column = it.column),
                        source = buildSourceFile(fs.getFileOrDirectory(it.relativePath)),
                        tracker = null,
                    )
                },
                files.size,
                false))
        return if (errors.isEmpty()) {
            0
        } else {
            1
        }
    }

    fun clean(fs: VirtualFileSystem, logger: Logger): Int {
        val docsDir = getDocsDirectory(fs)
        val result =
            if (!docsDir.exists()) {
                logger.log("Nothing to clean")
                0
            } else {
                val deletedDocs = docsDir.delete()
                if (deletedDocs) {
                    logger.log("Cleaned the 'docs' directory")
                    0
                } else {
                    logger.log("${bold(red("ERROR: "))} Failed to clean the 'docs' directory")
                    1
                }
            }
        docsDir.mkdirs()
        return result
    }

    fun version(logger: Logger): Int {
        logger.log("MathLingua $MATHLINGUA_VERSION")
        return 0
    }

    fun serve(fs: VirtualFileSystem, logger: Logger, port: Int, onStart: (() -> Unit)?) =
        startServer(fs, logger, port, onStart)

    fun decompose(fs: VirtualFileSystem, logger: Logger) {
        logger.log(
            Json.encodeToString(
                decompose(
                    fs = fs, sourceCollection = newSourceCollectionFromCwd(fs), mlgFiles = null)))
    }

    fun completionJson(logger: Logger) {
        logger.log(Json.encodeToString(COMPLETIONS))
    }
}

// -----------------------------------------------------------------------------

private fun String.jsonSanitize() =
    this.replace("\\", "\\\\")
        .replace("\b", "\\b")
        .replace("\n", "\\n")
        .replace("\t", "\\t")
        .replace("\r", "\\r")
        .replace("\"", "\\\"")

private fun maybePlural(text: String, count: Int) =
    if (count == 1) {
        text
    } else {
        "${text}s"
    }

private fun getDocsDirectory(fs: VirtualFileSystem) = fs.getDirectory(listOf("docs"))

private fun getContentDirectory(fs: VirtualFileSystem) = fs.getDirectory(listOf("content"))

private fun getErrorOutput(
    errors: List<ValueSourceTracker<ParseError>>, numFilesProcessed: Int, json: Boolean
): String {
    val builder = StringBuilder()
    if (json) {
        builder.append("[")
    }
    for (i in errors.indices) {
        val err = errors[i]
        if (json) {
            builder.append("{")
            builder.append(
                "  \"file\": \"${err.source.file.absolutePath().joinToString(File.separator).jsonSanitize()}\",")
            builder.append("  \"type\": \"ERROR\",")
            builder.append("  \"message\": \"${err.value.message.jsonSanitize()}\",")
            builder.append("  \"failedLine\": \"\",")
            builder.append("  \"row\": ${err.value.row},")
            builder.append("  \"column\": ${err.value.column}")
            builder.append("}")
            if (i != errors.size - 1) {
                builder.append(",")
            }
        } else {
            builder.append(bold(red("ERROR: ")))
            builder.append(
                bold(
                    "${err.source.file.relativePath()} (Line: ${err.value.row + 1}, Column: ${err.value.column + 1})\n"))
            builder.append(err.value.message.trim())
            builder.append("\n\n")
        }
    }

    if (json) {
        builder.append("]")
    } else {
        builder.append(
            if (errors.isEmpty()) {
                bold(green("SUCCESS\n"))
            } else {
                bold(red("FAILED\n"))
            })
        builder.append("Processed $numFilesProcessed ${maybePlural("file", numFilesProcessed)}\n")
        builder.append("${errors.size} ${maybePlural("error", errors.size)} detected")
    }

    return builder.toString()
}

private fun exportFile(
    fs: VirtualFileSystem,
    logger: Logger,
    target: VirtualFile,
    stdout: Boolean,
    noExpand: Boolean,
    raw: Boolean
): List<ValueSourceTracker<ParseError>> {
    if (!target.exists()) {
        val message =
            "ERROR: The file ${target.absolutePath().joinToString(File.separator)} does not exist"
        logger.log(message)
        return listOf(
            ValueSourceTracker(
                value = ParseError(message = message, row = -1, column = -1),
                source =
                    SourceFile(
                        file = target, content = "", validation = validationFailure(emptyList())),
                tracker = null))
    }

    if (target.isDirectory() || !target.absolutePath().last().endsWith(".math")) {
        val message =
            "ERROR: The path ${target.absolutePath().joinToString(File.separator)} is not a .math file"
        logger.log(message)
        return listOf(
            ValueSourceTracker(
                value = ParseError(message = message, row = -1, column = -1),
                source =
                    SourceFile(
                        file = target, content = "", validation = validationFailure(emptyList())),
                tracker = null))
    }

    val sourceCollection = newSourceCollection(fs, listOf(fs.cwd()))
    val errors = mutableListOf<ValueSourceTracker<ParseError>>()
    val elements = getUnifiedRenderedTopLevelElements(target, sourceCollection, noExpand, errors)

    val contentBuilder = StringBuilder()
    for (element in elements) {
        if (element.second != null && element.second is TopLevelBlockComment) {
            contentBuilder.append("<div class='mathlingua-block-comment-top-level'>")
            contentBuilder.append(element.first)
            contentBuilder.append("</div>")
        } else {
            contentBuilder.append(element.first)
        }
    }

    val text =
        if (raw) {
            contentBuilder.toString()
        } else {
            buildStandaloneHtml(content = contentBuilder.toString())
        }

    if (stdout) {
        logger.log(text)
    } else {
        // get the path relative to the current working directory with
        // the file extension replaced with ".html"
        val relHtmlPath = target.relativePath().split("/").toMutableList()
        if (relHtmlPath.size > 0) {
            relHtmlPath[relHtmlPath.size - 1] =
                relHtmlPath[relHtmlPath.size - 1].replace(".math", ".html")
        }
        val htmlPath = mutableListOf<String>()
        htmlPath.add("docs")
        htmlPath.addAll(relHtmlPath)
        val outFile = fs.getFile(htmlPath)
        val parentDir =
            fs.getDirectory(htmlPath.filterIndexed { index, _ -> index < htmlPath.size - 1 })
        parentDir.mkdirs()
        outFile.writeText(text)
        logger.log("Wrote ${outFile.relativePath().split("/").joinToString(File.separator)}")
    }

    return errors
}

private fun getUnifiedRenderedTopLevelElements(
    f: VirtualFile,
    sourceCollection: SourceCollection,
    noexpand: Boolean,
    errors: MutableList<ValueSourceTracker<ParseError>>
): List<Pair<String, Phase2Node?>> {
    val codeElements = mutableListOf<Pair<String, Phase2Node?>>()
    val elements = getCompleteRenderedTopLevelElements(f, sourceCollection, noexpand, errors)
    for (element in elements) {
        val expanded = element.renderedFormHtml
        val node = element.node
        if (node != null && node is TopLevelBlockComment) {
            codeElements.add(Pair(expanded, node))
        } else {
            val literal = element.rawFormHtml
            val id = getRandomUuid()
            val html =
                "<div><button class='mathlingua-flip-icon' onclick=\"flipEntity('$id')\">&#8226;</button><div id='rendered-$id' class='mathlingua-rendered-visible'>${expanded}</div>" +
                    "<div id='literal-$id' class='mathlingua-literal-hidden'>${literal}</div></div>"
            codeElements.add(Pair(html, node))
        }
    }
    return codeElements
}

private fun readAllBytes(stream: InputStream): ByteArray {
    val result = mutableListOf<Byte>()
    val tempArray = ByteArray(1024)
    while (true) {
        val numRead = stream.read(tempArray, 0, tempArray.size)
        if (numRead < 0) {
            break
        }
        for (i in 0 until numRead) {
            result.add(tempArray[i])
        }
    }
    return result.toByteArray()
}

private fun renderAll(
    fs: VirtualFileSystem, logger: Logger
): Pair<List<String>, List<ErrorResult>> {
    val uri =
        Mathlingua.javaClass.getResource("/assets")?.toURI()?.toString()?.trim()
            ?: throw Exception("Failed to load assets directory")
    val index = uri.indexOf('!')
    val uriPrefix =
        if (index < 0) {
            uri
        } else {
            uri.substring(0, index)
        }
    val jarPath = uriPrefix.replace("jar:file:", "")

    val docDir = File(getDocsDirectory(fs).absolutePath().joinToString(File.separator))
    docDir.mkdirs()

    val cnameFile = File("CNAME")
    if (cnameFile.exists()) {
        val docsCnameFile = File(docDir, "CNAME")
        cnameFile.copyTo(target = docsCnameFile, overwrite = true)
    }

    val jarTimestamp = File(jarPath).lastModified().toString()
    val timestampFile = File(docDir, "timestamp")
    if (!timestampFile.exists() || timestampFile.readText() != jarTimestamp) {
        logger.log("Initial run detected. Saving webapp files to speed up future runs.")
        timestampFile.writeText(jarTimestamp)

        val jar = JarFile(jarPath)
        for (entry in jar.entries()) {
            if (!entry.toString().startsWith("assets/") || entry.toString() == "assets/") {
                continue
            }

            val outFile = File(docDir, entry.name.replace("assets/", ""))
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.writeBytes(readAllBytes(jar.getInputStream(entry)))
            }
        }
        jar.close()

        val indexFile = File(docDir, "index.html")
        val indexText =
            indexFile.readText().replace("<head>", "<head><script src=\"./data.js\"></script>")
        indexFile.writeText(indexText)
        logger.log("Wrote docs${File.separator}index.html")
    }

    val decomp =
        decompose(fs = fs, sourceCollection = newSourceCollectionFromCwd(fs = fs), mlgFiles = null)
    val data = Json.encodeToString(decomp)

    val dataFile = File(docDir, "data.js")
    dataFile.writeText("window.MATHLINGUA_DATA = $data")
    logger.log("Wrote docs${File.separator}data.js")

    return Pair(
        decomp.collectionResult.fileResults.map { it.relativePath }, decomp.collectionResult.errors)
}

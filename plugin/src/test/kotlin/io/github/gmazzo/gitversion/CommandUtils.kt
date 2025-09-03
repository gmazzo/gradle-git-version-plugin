package io.github.gmazzo.gitversion

import java.io.File

fun File.command(vararg commandLine: String) =
    with(Runtime.getRuntime().exec(commandLine, null, this)) {
        check(waitFor() == 0) {
            "Command '${commandLine.joinToString(" ")}' failed: ${
                errorStream.reader().readText().trim()
            }"
        }
        inputStream.reader().readText().trim()
    }

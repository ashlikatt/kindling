import serializer.sendPackageRecode
import serializer.sendPackageVanilla
import transpiler.transpile
import java.io.File
import java.io.IOException

fun main(inputs: Array<String>) {
    if (inputs.isEmpty()) {
        println("No input. Run with the -help flag for help.")
        return
    }

    // Args and flags
    val input = inputs.groupBy { if (it.startsWith("-")) "flags" else "arguments" }
    val args = input["arguments"] ?: emptyList()
    val flags = input["flags"]?.filter{ it.startsWith("--") } ?: emptyList()

    val shorthandFlags = input["flags"]
        ?.filter{ it.length >= 2 && it[0] == '-' && it[1] != '-' }
        ?.map { it.replace("-", "").toCharArray().toList() }
        ?.flatten() ?: emptyList()

    // Read flags
    val help = shorthandFlags.contains('h') || flags.contains("--help")
    val recode = shorthandFlags.contains('r') || flags.contains("--recode")
    val verbose = shorthandFlags.contains('v') || flags.contains("--verbose")
    val debug = shorthandFlags.contains('d') || flags.contains("--debug")
    val small = shorthandFlags.contains('B') || flags.contains("--basic")
    val large = shorthandFlags.contains('L') || flags.contains("--large")
    val massive = shorthandFlags.contains('M') || flags.contains("--massive")
    if ((small && large) || (small && massive) || (large && massive)) {
        println("Only one of --basic, --large, and --massive may be chosen")
        return
    }
    val plotSize = if (massive) 300 else if (large) 100 else 50

    // Handle code
    if (help) {
        printHelp()
        return
    } else if (args.size == 1) {
        try {
            val code = getInput(args[0])
            if (verbose) {
                println("Input Code:")
                println(code)
                println()
            }
            val tokens = tokenize(code)
            val parsed = parse(tokens)
            val unoptimizedTranspiled = transpile(parsed)
            println("Compiling with plot size $plotSize:")
            val transpiled = unoptimizedTranspiled.optimized(plotSize)
            if (verbose) {
                println("Formatted Blockcode:")
                println(transpiled.toString())
                println()
            }
            if (recode) {
                sendPackageRecode(transpiled, verbose)
            } else {
                sendPackageVanilla(transpiled)
            }
        } catch (e: Exception) {
            if (debug) {
                e.printStackTrace()
            } else {
                println(e.message)
            }
        }
    } else {
        println("Unexpected input. Run with the -help flag for help.")
    }
}

/**
 * If input is a file location, read from it.
 * Otherwise, return input string.
 * May throw IOException.
 */
fun getInput(s: String): String {
    val file = File(s)
    return if (file.exists()) {
        if (!file.isFile) throw IOException("Specified path does not point to file.")
        if (!file.canRead()) throw IOException("Specified file is unable to be read.")
        file.readText()
    } else {
        s
    }
}

fun printHelp() {

    println("""
    |Kindling is a program that compiles Kindling script files into DiamondFire code.
    |You can view the documentation for Kindling here: https://github.com/ashlikatt/Kindling/wiki
    |
    |Usage: java -jar Kindling.jar [FLAGS] <PROGRAM>
    |PROGRAM:
    |  Either a raw Kindling script, or a file path pointing to a plaintext containing one.
    |
    |FLAGS:
    |  --help -h:
    |    Shows this menu.
    |  --recode -r:
    |    Sends output to client via recode instead of printing commands.
    |  --debug -d:
    |    Errors will print their full stacktrace.
    |  --verbose -v:
    |    Additional output.
    |  --basic -B:
    |    Compiles for a basic plot. Incompatible with --large or --massive.
    |  --large -L:
    |    Compiles for a large plot. Incompatible with --basic or --massive.
    |  --massive -M:
    |    Compiles for a massive plot. Incompatible with --basic or --large.
    """.trimMargin())
}
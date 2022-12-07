import java.io.File

/**
 * This class represents a file in this AOC puzzle.
 */
data class AOCFile(val name: String, val size: Long)

/**
 * This class represents a directory in this AOC puzzle.
 */
data class AOCDirectory(
    val name: String,
    val parentDirectory: AOCDirectory?,
    val directories: MutableList<AOCDirectory>,
    val files: MutableList<AOCFile>
) {

    /**
     * Checks whether this directory has a sub-directory with the given [name].
     */
    fun hasSubDirectory(name: String): Boolean = this.directories.any { it.name == name }

    /**
     * Gets a sub-directory with name [name]. Throws exception if none is found.
     */
    fun subDirectoryWithName(name: String) = this.directories.first { it.name == name }

    /**
     * Calculates the total size of this directory. This includes all the files and the size of its subdirectories.
     */
    fun totalSize(): Long {
        var total = this.files.sumOf { it.size }

        this.directories.forEach { directory -> total += directory.totalSize() }
        return total
    }
}

/**
 * Represents a command.
 */
enum class Command(val syntax: String) {
    CHANGE_DIRECTORY("cd"), LIST("ls");

    companion object {
        fun fromCLI(cmd: String): Command {
            if (cmd.startsWith(CHANGE_DIRECTORY.syntax)) {
                return CHANGE_DIRECTORY
            }

            // We only recognise TWO (2) commands in this puzzle, so if it's not change directory, it will be list command.
            return LIST
        }
    }
}

/**
 * Processes the cd command.
 */
fun processChangeDirectoryCommand(
    directories: MutableList<AOCDirectory>,
    currDirectory: AOCDirectory?,
    cmd: String
): AOCDirectory {
    val newDir = cmd.substring(Command.CHANGE_DIRECTORY.syntax.length + 1)

    // Create a new folder only if its not a .. command, the directories is still empty, or current directory does not
    // have a directory with the given name.
    if (newDir != "..") {
        val newDirectory = AOCDirectory(
            name = newDir,
            parentDirectory = currDirectory,
            directories = mutableListOf(),
            files = mutableListOf()
        )

        if (directories.isEmpty()) {
            directories.add(newDirectory)
        } else {
            if (currDirectory != null && !currDirectory.hasSubDirectory(name = newDir)) {
                currDirectory.directories.add(newDirectory)
            }
        }
    }

    // It asks to go up to the parent directory, just return the parent directory.
    if (newDir == "..") {
        return currDirectory?.parentDirectory ?: throw IllegalStateException("No parent directory")
    }

    // Go to the root directory.
    if (newDir == "/") {
        return directories[0]
    }

    // Go to the subdirectory with name [newDir].
    return currDirectory!!.subDirectoryWithName(name = newDir)
}

fun processListCommand(currDirectory: AOCDirectory, data: String) {
    if (data.startsWith("dir")) {
        // This is a directory.
        val dirName = data.substring(4)
        currDirectory.directories.add(
            AOCDirectory(
                name = dirName,
                parentDirectory = currDirectory,
                directories = mutableListOf(),
                files = mutableListOf()
            )
        )
    } else {
        // This is a file.
        val (size, name) = data.split(" ")
        currDirectory.files.add(AOCFile(name = name, size = size.toLong()))
    }
}

fun part1(currDirectory: AOCDirectory, answer: MutableList<AOCDirectory>) {
    if (currDirectory.totalSize() <= 100000)
        answer.add(currDirectory)

    currDirectory.directories.forEach { part1(currDirectory = it, answer = answer) }
}

fun part2(currDirectory: AOCDirectory, minSpace: Long): Long {
    fun traverse(currDirectory: AOCDirectory, sizes: MutableList<Long>) {
        sizes.add(currDirectory.totalSize())
        currDirectory.directories.forEach { traverse(currDirectory = it, sizes = sizes) }
    }

    val sizes = mutableListOf<Long>()
    traverse(currDirectory = currDirectory, sizes = sizes)

    // Sort the sizes and find the first size that meets the criteria.
    return sizes.sorted().first { it >= minSpace }
}

fun main() {
    val directories = mutableListOf<AOCDirectory>()
    var currDir: AOCDirectory? = null
    File("input.txt").forEachLine { line ->
        if (line.startsWith("$")) {
            val cli = line.substring(2)

            // Get the command. The real command always starts from the third character.
            when (Command.fromCLI(cmd = cli)) {
                Command.CHANGE_DIRECTORY -> currDir = processChangeDirectoryCommand(
                    currDirectory = currDir,
                    directories = directories,
                    cmd = cli
                )

                else -> {
                    // We do nothing here. It's definitely list command, which will be handled by the else block.
                }
            }
        } else {
            // In this puzzle, it is guaranteed that at this stage, the current directory is not null.
            processListCommand(currDirectory = currDir!!, data = line)
        }
    }

    val answer = mutableListOf<AOCDirectory>()
    part1(currDirectory = directories[0], answer = answer) // We always start from the root directory.
    println(answer.sumOf { it.totalSize() })

    val minSpace = 30000000 - (70000000 - directories[0].totalSize())
    val deletedSpace =
        part2(currDirectory = directories[0], minSpace = minSpace) // We always start from the root directory.
    println(deletedSpace)
}
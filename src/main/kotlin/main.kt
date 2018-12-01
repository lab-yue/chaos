import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.*

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") {
        String.format("%02x", it)
    }
}

fun String.hash(exclude: MutableList<String>, length: Int = 6): String {
    val newHashName = this
        .md5()
        .substring(0..length)

    return when {
        exclude.contains(newHashName) -> newHashName.hash(exclude)
        else -> {
            exclude.add(newHashName)
            println("$this --> $newHashName")
            return newHashName
        }
    }
}

fun String.prefix(prefix: String): String {
    return "$prefix$this"
}

fun String.suffix(suffix: String): String {
    return "$this.$suffix"
}

data class Config(
    val input: String,
    val output: String,
    val overwrite: Boolean = false
)


fun main() = runBlocking {

    val exclude = mutableListOf<String>()
    val config = Config(
        input = "test_files",
        output = "test_dist/"
    )

    File(config.input)
        .walkTopDown()
        .forEach {
            when {
                it.name == config.input -> {
                    println("skip root")
                    return@forEach
                }
                it.name + '/' == config.input -> {
                    println("skip root")
                    return@forEach
                }
                it.name.startsWith('.') -> {
                    println("skip hidden")
                    return@forEach
                }
                else -> launch {
                    it.copyTo(
                        File(
                            it
                                .nameWithoutExtension
                                .hash(exclude)
                                .prefix(config.output)
                                .suffix(it.extension)
                        ), overwrite = config.overwrite
                    )

                }
            }

        }
}

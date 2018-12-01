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


fun main() = runBlocking {

    val exclude = mutableListOf<String>()
    val dist = "test_dist/"

    File("test_files/")
        .walk()
        .forEach {
            launch {
                it.copyTo(
                    File(
                        it
                            .nameWithoutExtension
                            .hash(exclude)
                            .prefix(dist)
                            .suffix(it.extension)
                    ), overwrite = true
                )

            }
        }
}

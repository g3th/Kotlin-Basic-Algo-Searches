import java.io.*
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

fun fetchFile(dir: String, list: MutableList<String>): MutableList<String>{
    File(dir).forEachLine {
        list.add(it)
    }
    return list
}

fun timeConversion(time: Long): List<Long>{
    val result = mutableListOf<Double>()
    result.add(time / 60000.0 % 60.0)
    result.add(time / 1000.0 % 60.0)
    result.add(time / 1000.0 % 1 * 1000)
    return result.map { it.toLong()}
}

fun phoneBookNameSize (str: String): String {
    if (str.split(" ").drop(2).isEmpty()){
        return str.split(" ").drop(1)[0]
    }
    return str.split(" ").drop(1)[1]
}

fun queryNameSize (str: String): String {
    if (str.split(" ").size == 2) {
        return str.split(" ").drop(1)[0]
    } else {
        return str.split(" ")[0]
    }
}

fun bubbleSort(directory: MutableList<String>): MutableList<String>{
    var counter = 0
    var swap = ""
    while (counter != directory.size) {
        for (i in directory.indices) {
            if (i + 1 < directory.size && phoneBookNameSize(directory[i]) > phoneBookNameSize(directory[i + 1])) {
                swap = directory[i]
                directory[i] = directory[i + 1]
                directory[i + 1] = swap
            }
        }
        counter += 1
    }
    return directory
}

fun main() {

    var hits = 0
    val directory = mutableListOf<String>()
    val query = mutableListOf<String>()

    fetchFile("small_find.txt", query)
    fetchFile("small_directory.txt", directory)

    /* Write Sorted List To File
       File("sorted").writeText("")
       for (i in directory) {
       File("sorted").appendText(i + "\n")
       } */

    val totalLinearSearchTime = measureTimeMillis {
        println("Start searching (linear search)...")
        for (i in query) {
            for ( j in directory) {
                if (j.contains(i)) {
                    hits += 1
                }
            }
        }
    }
    val linearSearch = timeConversion(totalLinearSearchTime)
    print("Found $hits / ${query.size} entries. Time taken: ")
    print(String.format("%1d min. %1d sec. %3d ms.", linearSearch[0], linearSearch[1], linearSearch[2]) )

    println("\n\nStart searching (bubble sort + jump search)..")
    val sortingTime = measureTimeMillis {
        bubbleSort(directory)
    }

    var currentIndex = 0
    var previousIndex = 0
    val blockSize = sqrt(directory.size.toDouble())

    val totalJumpSearchTime = measureTimeMillis {
        for (i in query.indices) {
            directorySearch@ while (i != query.size) {
                if (phoneBookNameSize(directory[currentIndex]) <= queryNameSize(query[i])) {
                    previousIndex = currentIndex
                    if (currentIndex + blockSize.toInt() > directory.size - 1) {
                        currentIndex = directory.lastIndex
                    } else {
                        currentIndex += blockSize.toInt()
                    }
                } else if (phoneBookNameSize(directory[currentIndex]) >= queryNameSize(query[i])) {
                    for (elem in currentIndex downTo previousIndex) {
                        if (phoneBookNameSize(directory[elem]) == queryNameSize(query[i])) {
                            //println("Match : ${phoneBookNameSize(directory[elem])} : ${queryNameSize(query[i])}")
                            hits += 1
                            previousIndex = 0
                            currentIndex = 0
                            break@directorySearch
                        }
                    }
                }
            }
        }
    }

    val totalSortAndSearchTime = timeConversion(sortingTime + totalJumpSearchTime)
    val totSortTime = timeConversion(sortingTime)
    val totSearchingTime = timeConversion(totalJumpSearchTime)

    print("Found $hits / ${query.size} entries. Time taken: ")
    print(String.format("%1d min. %1d sec. %3d ms.", totalSortAndSearchTime[0], totalSortAndSearchTime[1], totalSortAndSearchTime[2]) )
    println(String.format("\nSorting time: %1d min. %1d sec. %3d ms.", totSortTime[0], totSortTime[1], totSortTime[2]))
    println(String.format("Searching time: %1d min. %1d sec. %3d ms.", totSearchingTime[0], totSearchingTime[1], totSearchingTime[2]))
}

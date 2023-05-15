import java.io.*
import javax.sound.sampled.Line
import kotlin.math.sqrt
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

fun fetchFile(dir: String, list: MutableList<String>): MutableList<String> {

    File(dir).forEachLine {
        list.add(it)
    }
    return list
}

fun timeConversion(time: Long): List<Long> {

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

data class SearchReturnValues(val time: Long, val hits: Int)
fun linearSearch(query: MutableList<String>, directory: MutableList<String>): SearchReturnValues {

    var hits = 0
    val totalLinearSearchTime = measureTimeMillis {

        for (i in query) {
            for ( j in directory) {
                if (j.contains(i)) {
                    hits += 1
                }
            }
        }
    }
    return SearchReturnValues(totalLinearSearchTime, hits)
}

data class BubbleSortReturnValues(val sortedDirectory: MutableList<String>, val formattedTime: Long, val error: Boolean)
fun bubbleSort(directory: MutableList<String>, linearSearchTime: Long): BubbleSortReturnValues {

    var stoppedFlag = false
    var performance: Long = 0
    var counter = 0
    var swap: String
    mainLoop@while (counter != directory.size) {
        for (i in directory.indices) {
            val currentTime = measureTimeMillis {
                if (i + 1 < directory.size && phoneBookNameSize(directory[i]) > phoneBookNameSize(directory[i + 1])) {
                    swap = directory[i]
                    directory[i] = directory[i + 1]
                    directory[i + 1] = swap
                }
            }
            performance += currentTime

            /* Comment-out the line below if you want the bubbleSort
               to complete, regardless of time taken.
               This is not recommended, as the integral directory
               (not small_directory.txt, but directory.txt) contains 1.000.000 entries
               and sorting will take well over an hour.
             */

            if (performance >= linearSearchTime * 2) {
                stoppedFlag = true
                break@mainLoop
            }
        }
        counter += 1
    }

    return BubbleSortReturnValues(directory, performance, stoppedFlag)
}

fun jumpSearch(query: MutableList<String>, directory: MutableList<String>): SearchReturnValues {

    var hits = 0
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
    return SearchReturnValues(totalJumpSearchTime, hits)
}

fun main() {

    var displayInfo = ""
    val directory = mutableListOf<String>()
    val query = mutableListOf<String>()
    fetchFile("/home/roberto/Desktop/small_find.txt", query)
    fetchFile("/home/roberto/Desktop/small_directory.txt", directory)

    println("\nStart searching (linear search)...")

    val (lSearchTime, lHits) = linearSearch(query, directory)
    print("Found $lHits / ${query.size} entries. Time taken: ")
    val linearSearchTime = timeConversion(lSearchTime)
    print(String.format("%1d min. %1d sec. %3d ms.", linearSearchTime[0], linearSearchTime[1], linearSearchTime[2]) )

    println("\n\nStart searching (bubble sort + jump search)...")

    val (sortedList, bTotalTime, stoppedFlag) = bubbleSort(directory, lSearchTime)
    val bubbleSortTime = timeConversion(bTotalTime)

    /* Write Sorted List To File
    File("sorted").writeText("")
    for (i in sortedList) {
    File("sorted").appendText(i + "\n")
    } */

    if (stoppedFlag) {
        val totalSortLSearchTime = timeConversion(lSearchTime + bTotalTime)
        print("Found $lHits / ${query.size} entries. Time taken: ")
        print(String.format("%1d min. %1d sec. %3d ms.",
            totalSortLSearchTime[0], totalSortLSearchTime[1], totalSortLSearchTime[2]))

        print(String.format("\nSorting Time: %1d min. %1d sec. %3d ms.  - STOPPED, moved to linear search\n",
            bubbleSortTime[0], bubbleSortTime[1], bubbleSortTime[2]))

        print(String.format("Searching Time: %1d min. %1d sec. %3d ms.",
            linearSearchTime[0],linearSearchTime[1],linearSearchTime[2]))
        exitProcess(0)

    } else {

        displayInfo = String.format("\nSorting Time: %1d min. %1d sec. %3d ms.",
            bubbleSortTime[0], bubbleSortTime[1], bubbleSortTime[2])
    }

    val (jSearchTime, jHits) = jumpSearch(query, directory)
    val jumpSearchTime = timeConversion(jSearchTime)
    val totalSortAndSearchTime = timeConversion(jSearchTime + bTotalTime)

    print("Found $jHits / ${query.size} entries. Time taken: ")
    print(String.format("%1d min. %1d sec. %3d ms.", totalSortAndSearchTime[0], totalSortAndSearchTime[1], totalSortAndSearchTime[2]) )
    print(displayInfo)
    println(String.format("\nSearching time: %1d min. %1d sec. %3d ms.", jumpSearchTime[0], jumpSearchTime[1], jumpSearchTime[2]))
}

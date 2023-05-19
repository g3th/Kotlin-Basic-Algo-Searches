import java.io.*
import kotlin.math.sqrt
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

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
data class QuickSortReturnValues(val directory: MutableList<String>, val time: Long)
fun quickSort(directory: MutableList<String>, left: Int, right: Int): MutableList<String> {
    val index = partition (directory, left, right)
    if (left < index - 1) {
        quickSort(directory, left, index - 1)
    }
    if (index < right) {
        quickSort(directory, index, right)
    }
    return directory
}

fun partition(directory: MutableList<String>, l: Int, r: Int): Int {
    var left = l
    var right = r
    val pivot = phoneBookNameSize(directory[(left + right)/2])
    while (left <= right) {
        while (phoneBookNameSize(directory[left]) < pivot) left++
        while (phoneBookNameSize(directory[right]) > pivot) right--
        if (left <= right) {
            swapArray(directory, left,right)
            left++
            right--
        }
    }
    return left
}

fun swapArray(a: MutableList<String>, b: Int, c: Int) {
    val temp = a[b]
    a[b] = a[c]
    a[c] = temp
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

data class BubbleSortReturnValues(val sortedDirectory: MutableList<String>, val time: Long, val error: Boolean)
fun bubbleSort(directory: MutableList<String>, linearSearchTime: Long): BubbleSortReturnValues {
    var percentage: Double
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
               and sorting will take too long.
             */

            if (performance >= linearSearchTime * 10) {
                stoppedFlag = true
                break@mainLoop
            }
        }
        counter += 1

        /* Display Completion Percentage
            percentage = counter / directory.size.toDouble() * 100
            if (percentage != 0.0 && percentage % 10 == 0.0) {
               println("${percentage}%")
         } */

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



fun binarySearch(directory: MutableList<String>, query: String): Int {
    var high = directory.size
    var mid: Int
    var hit = 0
    var low = 0
    while(true) {
        mid = (low + high) / 2
        when {
            phoneBookNameSize(directory[mid]) == queryNameSize(query) -> {
                hit++
                break
            }
            phoneBookNameSize(directory[mid]) > queryNameSize(query) -> {
                high = mid--
            }
            phoneBookNameSize(directory[mid]) < queryNameSize(query) -> {
                low = mid++
            }
        }
    }
    return hit
}

@OptIn(ExperimentalTime::class)
fun main() {
    val userDir = System.getProperty("user.home") + "/Desktop/"
    var displayInfo = ""
    val directory = mutableListOf<String>()
    val query = mutableListOf<String>()
    fetchFile("${userDir}small_find.txt", query)
    fetchFile("${userDir}small_directory.txt", directory)


    println("\nStart searching (linear search)...")

    val (lSearchTime, lHits) = linearSearch(query, directory)
    print("Found $lHits / ${query.size} entries. Time taken: ")
    val linearSearchTime = timeConversion(lSearchTime)
    print(String.format("%1d min. %1d sec. %3d ms.", linearSearchTime[0], linearSearchTime[1], linearSearchTime[2]) )

    println("\n\nStart searching (bubble sort + jump search)...")

    val (sortedList, bTotalTime, stoppedFlag) = bubbleSort(directory, lSearchTime)
    val bubbleSortTime = timeConversion(bTotalTime)

    /* Write Sorted List To File
    for (i in sortedList) {
    File("${userDir}sorted").appendText(i + "\n")
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

    } else {
        val (jSearchTime, jHits) = jumpSearch(query, directory)
        val jumpSearchTime = timeConversion(jSearchTime)
        val totalSortAndSearchTime = timeConversion(jSearchTime + bTotalTime)

        print("Found $jHits / ${query.size} entries. Time taken: ")
        print(String.format("%1d min. %1d sec. %3d ms.", totalSortAndSearchTime[0], totalSortAndSearchTime[1], totalSortAndSearchTime[2]))
        print(String.format("\nSorting Time: %1d min. %1d sec. %3d ms.", bubbleSortTime[0], bubbleSortTime[1], bubbleSortTime[2]))
        println(String.format("Searching time: %1d min. %1d sec. %3d ms.\n", jumpSearchTime[0], jumpSearchTime[1], jumpSearchTime[2]))
    }
    println("\n\nStart searching (quick sort + binary search)...")

    val (values, duration) = measureTimedValue {
        quickSort(directory, 0, directory.size - 1)
    }

    var totalBsearchtime: Duration? = null
    var totalBsearchhits = 0
    for (i in query) {
        val (bHits, bSearchTime) = measureTimedValue{
            binarySearch(values, i)
        }
        totalBsearchtime = bSearchTime
        totalBsearchhits += bHits
    }

    val totalQuickSortAndBinaryTime = listOf(totalBsearchtime?.plus(duration)?.inWholeMinutes,
        totalBsearchtime?.plus(duration)?.inWholeSeconds,
        totalBsearchtime?.plus(duration)?.inWholeMilliseconds)

    val totalSortTime = listOf(duration.inWholeMinutes,
        duration.inWholeSeconds,
        duration.inWholeMilliseconds)

    val totalSearchTime = listOf(totalBsearchtime?.inWholeMinutes,
        totalBsearchtime?.inWholeSeconds,
        totalBsearchtime?.inWholeMilliseconds)

    print("Found $totalBsearchhits / ${query.size} entries. Time taken: ")
    print(String.format("%1d min. %1d sec. %3d ms.",totalQuickSortAndBinaryTime[0],
        totalQuickSortAndBinaryTime[1],
        totalQuickSortAndBinaryTime[2]))

    print(String.format("\nSorting Time: %1d min. %1d sec. %3d ms.",totalSortTime[0],
        totalSortTime[1],
        totalSortTime[2]))

    println(String.format("\nSearching time: %1d min. %1d sec. %3d ms.",totalSearchTime[0],
        totalSearchTime[1],
        totalSearchTime[2]))

}

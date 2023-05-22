import java.io.*
import kotlin.math.sqrt
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val parse = ParseEntries()

class ParseEntries {
    private val dir = System.getProperty("user.home")+"/Desktop/"

    fun fetchFile(file: String, list: MutableList<String>): MutableList<String>{
        File(dir + file).forEachLine{
            list.add(it)
        }
        return list
    }

    fun phoneBookEntry(str: String): String {
        if (str.split(" ").drop(2).isEmpty()) {
            return str.split(" ").drop(1)[0]
        }
        return "${str.split(" ")[1]} ${str.split(" ").drop(1)[1]}"
    }

    fun phoneNumber(str: String): String {
        return str.split(" ")[0]
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

}
fun timeConversion(time: Long): List<Long> {
    val result = mutableListOf<Double>()
    result.add(time / 60000.0 % 60.0)
    result.add(time / 1000.0 % 60.0)
    result.add(time / 1000.0 % 1 * 1000)
    return result.map { it.toLong()}
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
    val pivot = parse.phoneBookNameSize(directory[(left + right)/2])
    while (left <= right) {
        while (parse.phoneBookNameSize(directory[left]) < pivot) left++
        while (parse.phoneBookNameSize(directory[right]) > pivot) right--
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
                    if (hits == query.size){
                        break
                    }
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
                if (i + 1 < directory.size && parse.phoneBookNameSize(directory[i]) >
                    parse.phoneBookNameSize(directory[i + 1])) {
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
                if (parse.phoneBookNameSize(directory[currentIndex]) <= parse.queryNameSize(query[i])) {
                    previousIndex = currentIndex
                    if (currentIndex + blockSize.toInt() > directory.size - 1) {
                        currentIndex = directory.lastIndex
                    } else {
                        currentIndex += blockSize.toInt()
                    }
                } else if (parse.phoneBookNameSize(directory[currentIndex]) >= parse.queryNameSize(query[i])) {
                    for (elem in currentIndex downTo previousIndex) {
                        if (parse.phoneBookNameSize(directory[elem]) == parse.queryNameSize(query[i])) {
                            if (hits == query.size){
                                break
                            }
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
            parse.phoneBookNameSize(directory[mid]) == parse.queryNameSize(query) -> {
                hit++
                break
            }
            parse.phoneBookNameSize(directory[mid]) > parse.queryNameSize(query) -> {
                high = mid--
            }
            parse.phoneBookNameSize(directory[mid]) < parse.queryNameSize(query) -> {
                low = mid++
            }
        }
    }
    return hit
}

class HashMap {

    private val phoneBook = mutableMapOf<String,String>()
    private var hits = 0

    fun create(pdir: MutableList<String>) {
        for (i in pdir) {
            if (i.contains(" ")) {
                phoneBook[parse.phoneBookEntry(i)] = parse.phoneNumber(i)
            } else {
                phoneBook["${parse.phoneBookEntry(i)} ${parse.phoneBookEntry(i)}"] = parse.phoneNumber(i)
            }
        }
    }

    fun search(query: String): Int {
        for (j in phoneBook.keys) {
            if (query == j) {
                hits++
            } else if (hits == 500){
                break
            }
        }
        return hits
    }
}

@OptIn(ExperimentalTime::class)
fun main() {
    val directory = mutableListOf<String>()
    val query = mutableListOf<String>()
    parse.fetchFile("find.txt", query)
    parse.fetchFile("directory.txt", directory)

    // LINEAR SEARCH
    println("\nStart searching (linear search)...")

    val (lSearchTime, lHits) = linearSearch(query, directory)
    print("Found $lHits / ${query.size} entries. Time taken: ")
    val linearSearchTime = timeConversion(lSearchTime)
    print(String.format("%1d min. %1d sec. %3d ms.", linearSearchTime[0], linearSearchTime[1], linearSearchTime[2]) )

    // BUBBLE SORT + JUMP SEARCH
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

    // QUICK SORT + BINARY SEARCH
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

    // HASH MAP
    println("\nStart searching (hash table)...")
    val hMap = HashMap()
    val (hMapSort, hSDuration) = measureTimedValue{
        hMap.create(directory)
    }
    var totalHSearchTime: Duration? = null
    var totalHSearchHits = 0
    for (i in query) {
        val (hMapFind, hFDuration) = measureTimedValue {
            hMap.search(i)
        }
        totalHSearchHits++
        totalHSearchTime = hFDuration
    }

    val totalHashDuration = listOf(totalHSearchTime?.plus(hSDuration)?.inWholeMinutes,
        totalHSearchTime?.plus(hSDuration)?.inWholeSeconds,
        totalHSearchTime?.plus(hSDuration)?.inWholeMilliseconds)

    val parsedHsDuration = listOf(hSDuration.inWholeMinutes, hSDuration.inWholeSeconds, hSDuration.inWholeMilliseconds)
    val parsedHfDuration = listOf(totalHSearchTime?.inWholeMinutes, totalHSearchTime?.inWholeSeconds,
        totalHSearchTime?.inWholeMilliseconds)

    print("Found $totalHSearchHits / ${query.size} entries. Time taken: ")
    print(String.format("%1d min. %1d sec. %3d ms.",totalHashDuration[0],
        totalHashDuration[1],
        totalHashDuration[2]))

    print(String.format("\nCreating Time: %1d min. %1d sec. %3d ms.",parsedHsDuration[0],
        parsedHsDuration[1], parsedHsDuration[2]))

    println(String.format("\nSearching time: %1d min. %1d sec. %3d ms.",parsedHfDuration[0],
        parsedHfDuration[1],
        parsedHfDuration[2]))
}

enum class State {
    SELECT, ALL, NAME, FROM, NAME_COMMA, NONE
}
class Traslator {

    var prevState = 0
    var matrix = arrayOf<Array<Int>>(arrayOf(1, -1, -1, -1, -1),
                                     arrayOf(-1, 2, 3, 4, -1),
                                     arrayOf(-1, -1, -1, -1, 5),
                                     arrayOf(-1, -1, 3, 4, -1),
                                     arrayOf(-1, -1, -1, -1, 5),
                                     arrayOf(-1, -1, -1, 6, -1))

    fun translate(sqlCommand : Array<String>) : String {

        var mongodb : String = String()
        val tmp : String = String()
        var tbName = String()
        var cols = String()

        val columns : MutableList<String> = mutableListOf()
        var curState = 0
        var command = String()
        var name : State = State.NONE
        for (word in sqlCommand) {
            name = if (word[word.length - 1] == ',') {
                State.NAME_COMMA
            } else {
                when(word) {
                    "SELECT" -> { State.SELECT }
                    "*" -> { State.ALL }
                    "FROM" -> { State.FROM }
                    else -> { State.NAME }
                }
            }

            when(name) {
                State.NAME_COMMA -> {curState = 2}
                State.ALL -> { curState = 1 }
                State.SELECT -> {curState = 0 }
                State.NAME -> {curState = 3}
                State.FROM -> {curState = 4}
                else -> {}
            }

            curState =  matrix[prevState][curState]
            prevState = curState
            var govno = String()
            when(curState) {
                -1 -> {
                    mongodb = "error"
                    return mongodb
                }
                1 -> {
                    command = "find"
                }
                3 -> {
                    columns.add(word.dropLast(2))
                    govno = word.substring(0, word.length - 1)
                    println(word)
                }
                4 -> {
                    columns.add(word)
                }
                6 -> {
                    tbName = word
                }
            }

            //columns.clear()

        }
        for (col in columns) {
            cols += "$col : 1, "
        }
        if (cols.isNotEmpty()) {
            cols = ", $cols"
            cols.dropLast(1)
        }

        cols.dropLast(cols.length - 1)
        var t = cols.subSequence(0, cols.length - 2)
        mongodb = "db.$tbName.$command({}$t)"
        return mongodb
    }
}
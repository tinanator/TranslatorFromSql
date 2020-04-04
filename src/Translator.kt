enum class State {
    SELECT, ALL, NAME, FROM, NAME_COMMA, NONE
}


class Traslator {
    private var mongodb : String = String()
    private var prevState = 0
    private var command = String()
    private var tbName = String()
    private var isError = false
    private val columns : MutableList<String> = mutableListOf()
    private var matrix = arrayOf<Array<Int>>(arrayOf(1, -1, -1, -1, -1),
                                             arrayOf(-1, 2, 3, 4, -1),
                                             arrayOf(-1, -1, -1, -1, 5),
                                             arrayOf(-1, -1, 3, 4, -1),
                                             arrayOf(-1, -1, -1, -1, 5),
                                             arrayOf(-1, -1, -1, 6, -1))

    private fun defineLexem(word : String) : State {
        val lexem = if (word[word.length - 1] == ',') {
            State.NAME_COMMA
        } else {
            when(word) {
                "SELECT" -> { State.SELECT }
                "*" -> { State.ALL }
                "FROM" -> { State.FROM }
                else -> { State.NAME }
            }
        }
        return lexem
    }

    private fun setState(lexem : State) : Int {
        var curState = -1
        when(lexem) {
            State.NAME_COMMA -> {curState = 2}
            State.ALL -> { curState = 1 }
            State.SELECT -> {curState = 0 }
            State.NAME -> {curState = 3}
            State.FROM -> {curState = 4}
            else -> {}
        }
        return curState
    }

    private fun translateToMongodb(curState:Int, lexem:String){
        when(curState) {
            -1 -> {
                isError = true
                //return mongodb
            }
            1 -> {
                command = "find"
            }
            3 -> {
                columns.add(lexem.substring(0, lexem.length - 1))
                //govno = lexem.substring(0, lexem.length - 1)
                println(lexem.substring(0, lexem.length - 1))
            }
            4 -> {
                columns.add(lexem)
            }
            6 -> {
                tbName = lexem
            }
        }
    }

    fun translate(sqlCommand : Array<String>) : String {
        val tmp : String = String()
        var cols = String()
        var curState = 0
        var lexem : State = State.NONE
        for (word in sqlCommand) {
            lexem = defineLexem(word)
            curState = setState(lexem)
            curState =  matrix[prevState][curState]
            prevState = curState
            var govno = String()
            translateToMongodb(curState, word)
            if (isError) {
                return "ERROR"
            }


        }
        for (col in columns) {
            cols += "$col : 1, "
        }
        if (cols.isNotEmpty()) {
            cols = ", $cols"
            cols.dropLast(1)
        }
        var t = ""
        if (cols.isNotEmpty()) {
            cols.dropLast(cols.length - 1)
            t = (cols.subSequence(0, cols.length - 2)).toString()
        }
        mongodb = "db.$tbName.$command({}$t)"
        return mongodb
    }
}
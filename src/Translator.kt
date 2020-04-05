enum class State {
    SELECT, ALL, NAME, FROM, NAME_COMMA, NONE, LIMIT
}

enum class State2 {
    FIND_COMMAND, FIND_COLS, FIND_TABLENAME, FIND_LIMIT, ERROR, CHECK_COMMAND
}


class Traslator {
    private var mongodb: String = String()

    private var command = String()
    private var tbName = String()
    private var isError = false
    private val columns: MutableList<String> = mutableListOf()
    private var matrix = arrayOf<Array<Int>>(
        arrayOf(1, -1, -1, -1, -1, -1, -1),
        arrayOf(-1, 2, 3, 4, -1, -1, -1),
        arrayOf(-1, -1, -1, -1, 5, -1, -1),
        arrayOf(-1, -1, 3, 4, -1, -1, -1),
        arrayOf(-1, -1, -1, -1, 5, -1, -1),
        arrayOf(-1, -1, -1, 6, -1, -1, -1),
        arrayOf(-1, -1, -1, -1, -1, 7, -1),
        arrayOf(-1, -1, -1, -1, -1, -1, 8)
    )

    private fun defineLexem(word: String): State {
        val lexem = if (word[word.length - 1] == ',') {
            State.NAME_COMMA
        } else {
            when (word) {
                "SELECT" -> {
                    State.SELECT
                }
                "*" -> {
                    State.ALL
                }
                "FROM" -> {
                    State.FROM
                }
                "LIMIT" -> {
                    State.LIMIT
                }
                else -> {
                    State.NAME
                }
            }
        }
        return lexem
    }

    private fun setState(lexem: State): Int {
        var curState = -1
        when (lexem) {
            State.NAME_COMMA -> {
                curState = 2
            }
            State.ALL -> {
                curState = 1
            }
            State.SELECT -> {
                curState = 0
            }
            State.NAME -> {
                curState = 3
            }
            State.FROM -> {
                curState = 4
            }
            State.LIMIT -> {
                curState = 5
            }
            else -> {
            }
        }
        return curState
    }

    private fun translateToMongodb(curState: Int, lexem: String) {
        when (curState) {
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

    fun translate(sqlCommand: Array<String>): String {
        var state2 = State2.FIND_COMMAND
        val tmp: String = String()
        var cols = String()
        var curState = 0
        var prevState = 0
        var wasFrom = false
        var lexem: State = State.NONE
        for (word in sqlCommand) {
            lexem = defineLexem(word)
            when (state2) {
                State2.FIND_COMMAND -> {
                    if (lexem == State.SELECT) {
                        command = "find"
                        state2 = State2.FIND_COLS
                    } else {
                        state2 = State2.ERROR
                    }
                }
                State2.FIND_COLS -> {
                    if (lexem == State.ALL) {
                        if (columns.isEmpty()) {
                            state2 = State2.FIND_TABLENAME
                        }
                    }
                    else if (lexem == State.NAME) {
                        columns.add(word)
                        state2 = State2.FIND_TABLENAME
                    }
                    else if (lexem == State.NAME_COMMA) {
                        columns.add(word.replace(",", ""))
                        state2 = State2.FIND_COLS
                    } else {
                        state2 = State2.ERROR
                    }
                }
                State2.FIND_TABLENAME -> {
                    if (lexem == State.FROM) {
                        wasFrom = true
                        state2 = State2.FIND_TABLENAME
                    }
                    if (lexem == State.NAME) {
                        if (!wasFrom) {
                            state2 = State2.ERROR
                        } else {
                            state2 = State2.CHECK_COMMAND
                            tbName = word
                        }
                    }
                }
                State2.CHECK_COMMAND -> {
                    if (tbName.isEmpty()) {
                        state2 = State2.ERROR
                    } else {
                        state2 = State2.FIND_LIMIT
                    }
                }
                else -> {
                }
            }
            if (state2 == State2.ERROR) {
                return "ERROR"
            }
        }
        if (state2 == State2.ERROR) {
            return "ERROR"
        }
        if (tbName.isEmpty()) {return "ERROR"}
        var projection = ""
        for (i in columns.indices) {
            if (i == 0){
                projection += ", "}
            projection +=  if (i == columns.size - 1) {
                "${columns[i]}: 1"
            }else {
                "${columns[i]}: 1, "
            }
        }

        mongodb = "db.$tbName.$command({}$projection)"
        return mongodb
    }
}

//            curState = setState(lexem)
//            curState =  matrix[prevState][curState]
//            prevState = curState
//            var govno = String()
//            translateToMongodb(curState, word)
//            if (isError) {
//                return "ERROR"
//            }
//        }
//        for (col in columns) {
//            cols += "$col : 1, "
//        }
//        if (cols.isNotEmpty()) {
//            cols = ", $cols"
//            cols.dropLast(1)
//        }
//        var t = ""
//        if (cols.isNotEmpty()) {
//            cols.dropLast(cols.length - 1)
//            t = (cols.subSequence(0, cols.length - 2)).toString()
//        }
//        if (tbName.isEmpty()) {
//            return "ERROR"
//        }
//        mongodb = "db.$tbName.$command({}$t)"
//        return mongodb





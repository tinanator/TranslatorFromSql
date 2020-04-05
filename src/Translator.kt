import java.text.ParseException

enum class State {
    SELECT, ALL, NAME, FROM, NAME_COMMA, NONE, LIMIT, INT, OFFSET
}

enum class State2 {
    FIND_COMMAND, FIND_COLS, FIND_TABLENAME, FIND_LIMIT, ERROR, FIND_FUNCTIONS, FIND_OFFSET
}


class Traslator {
    private var mongodb: String = String()
    private var prevState = State.NONE
    var limit = -1;
    var offset = -1;
    private var command = String()
    private var tbName = String()
    private var isError = false
    private val columns: MutableList<String> = mutableListOf()

    private fun isDigit(word : String) : Boolean {
        try {
            word.toInt()
        }
        catch (e: NumberFormatException) {
            return false
        }
        return true
    }


    private fun defineLexem(word: String): State {
        val lexem = if (word[word.length - 1] == ',') {
            State.NAME_COMMA
        } else if (isDigit(word)) {
            State.INT
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
                "OFFSET" -> {
                    State.OFFSET
                }
                else -> {
                    State.NAME
                }
            }
        }
        return lexem
    }


    private fun findCommand(lexem : State) : State2{
        return if (lexem == State.SELECT) {
            prevState = State.SELECT
            command = "find"
            State2.FIND_COLS
        } else {
            State2.ERROR
        }
    }

    private fun findCols(lexem : State, word : String) : State2 {
        if (lexem == State.ALL) {
            if (columns.isEmpty()) {
                prevState = State.ALL
                return  State2.FIND_TABLENAME
            }
        }
        else if (lexem == State.NAME) {
            columns.add(word)
            prevState = State.NAME
            return State2.FIND_TABLENAME
        }
        else if (lexem == State.NAME_COMMA) {
            prevState = State.NAME_COMMA
            columns.add(word.replace(",", ""))
            return State2.FIND_COLS
        }
        return State2.ERROR
    }

    private fun findFindTableName(lexem : State, word : String) : State2 {
        if (lexem == State.FROM && (prevState == State.NAME || prevState == State.ALL)) {
            prevState = State.FROM
            return State2.FIND_TABLENAME
        }
        else if (lexem == State.NAME && prevState == State.FROM) {
            tbName = word
            return State2.FIND_LIMIT
        }
        return State2.ERROR
    }

    private fun findLimit(lexem : State, word : String) : State2 {

        if (lexem == State.LIMIT) {
            if (limit <= 0) {
                prevState = State.LIMIT
                return State2.FIND_LIMIT
            }
            return State2.ERROR
        }
        else if (lexem == State.INT) {
            if (prevState == State.LIMIT) {
                limit = word.toInt()
                return State2.FIND_OFFSET
            }
        }
        return State2.ERROR
    }

    private fun findOffset(lexem : State, word : String) : State2{
        if (lexem == State.OFFSET) {
            if (offset <= 0) {
                prevState = State.OFFSET
                return State2.FIND_OFFSET
            }
            return State2.ERROR
        }
        else if (lexem == State.INT) {
            if (prevState == State.OFFSET) {
                offset = word.toInt()
                return State2.FIND_LIMIT
            }
        }
        return State2.ERROR
    }


    fun translate(sqlCommand: Array<String>): String {
        var state2 = State2.FIND_COMMAND

        val tmp: String = String()
        var cols = String()
        var curState = 0
       // var prevState = 0

        var wasFrom = false
        var wasLimit = false
        var lexem: State = State.NONE
        for (word in sqlCommand) {
            lexem = defineLexem(word)
            when (state2) {
                State2.FIND_COMMAND -> {
                   state2 = findCommand(lexem)
                }
                State2.FIND_COLS -> {
                    state2 = findCols(lexem, word)
                }
                State2.FIND_TABLENAME -> {
                    state2 = findFindTableName(lexem, word)
                }
                State2.FIND_LIMIT -> {
                    if (tbName.isEmpty()) {
                        state2 = State2.ERROR
                    }
                    state2 = findLimit(lexem, word)
                }
                State2.FIND_OFFSET -> {
                    if (tbName.isEmpty()) {
                        state2 = State2.ERROR
                    }
                    state2 = findOffset(lexem, word)
                }
                else -> {
                    state2 = State2.ERROR
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
        var functions = ""
        if (limit >= 0) {
            functions += ".limit($limit)"
        }
        if (offset >= 0) {
            functions += ".offset($offset)"
        }
        mongodb = "db.$tbName.$command({}$projection)$functions"
        return mongodb
    }
}





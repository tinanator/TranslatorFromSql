import org.junit.jupiter.api.Assertions.*

internal class MainKtTest {
    val t = Traslator()
    @org.junit.jupiter.api.Test
    fun simpleTest() {
        var sql : Array<String> = arrayOf("SELECT", "*", "FROM", "TABLE")
        assert(t.translate(sql) == "db.TABLE.find({})")

        sql = arrayOf("SELECT", "COL1,", "COL2,", "COL3", "FROM", "TABLE")
        assert(t.translate(sql) == "db.TABLE.find({}, COL1: 1, COL2: 1, COL3: 1)")
    }
    @org.junit.jupiter.api.Test
    fun limitTest() {
        var sql = arrayOf("SELECT", "*", "FROM", "TABLE", "LIMIT", "5")
        assert(t.translate(sql) == "db.TABLE.find({}).limit(5)")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "LIMIT", "5", "LIMIT")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "LIMIT")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "LIMIT", "-7")
        assert(t.translate(sql) == "ERROR")
    }

    @org.junit.jupiter.api.Test
    fun predicatTest() {
        var sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "col", ">", "4")
        println("db.TABLE.find({col: {\$gt: 4}})")
        assert(t.translate(sql) == "db.TABLE.find({col: {\$gt: 4}})")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "col", "=", "value")
        assert(t.translate(sql) == "db.TABLE.find({col: {\$eq: \"value\"}})")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "col", "=", "value", "AND", "col2", "<>", "value3")
        assert(t.translate(sql) == "db.TABLE.find({col: {\$eq: \"value\"}, col2: {\$ne: \"value3\"}})")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE")
        assert(t.translate(sql) == "ERROR")
        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "col")
        assert(t.translate(sql) == "ERROR")
        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "col", "=")
        assert(t.translate(sql) == "ERROR")
        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "col", "=", "<>")
        assert(t.translate(sql) == "ERROR")
        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "<>")
        assert(t.translate(sql) == "ERROR")
        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "col", "col2")
        assert(t.translate(sql) == "ERROR")
        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "WHERE", "col", "=", "value", "AND")
        assert(t.translate(sql) == "ERROR")
    }

    @org.junit.jupiter.api.Test
    fun offsetTest() {
        var sql = arrayOf("SELECT", "*", "FROM", "TABLE", "LIMIT", "5", "OFFSET", "2")
        assert(t.translate(sql) == "db.TABLE.find({}).limit(5).offset(2)")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "LIMIT", "5", "OFFSET")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "OFFSET", "2")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "LIMIT", "7", "OFFSET", "-6")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "*", "FROM", "TABLE", "LIMIT", "5", "OFFSET", "1", "OFFSET", "8")
        assert(t.translate(sql) == "ERROR")
    }
    @org.junit.jupiter.api.Test
    fun errorTest(){
        var sql : Array<String> = arrayOf("SELECT", "FROM", "TABLE")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("AAA")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "COL1,", "FROM", "TABLE")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "col1,", "*", "FROM", "TABLE")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "col1,", "col2", "FROM")
        assert(t.translate(sql) == "ERROR")

        sql = arrayOf("SELECT", "col1,", "col2", "FRM")
        assert(t.translate(sql) == "ERROR")
    }
    @org.junit.jupiter.api.Test
    fun tests() {
        var sql = arrayOf("SELECT", "col,", "loc,", "aaA", "FROM", "TABLE","WHERE", "col", ">", "3", "AND", "loc", "<>", "2", "LIMIT", "5", "OFFSET", "3")
        assert(t.translate(sql) == "db.TABLE.find({col: {\$gt: 3}, loc: {\$ne: 2}}, col: 1, loc: 1, aaA: 1).limit(5).offset(3)")
    }
}
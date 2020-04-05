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
}
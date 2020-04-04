import org.junit.jupiter.api.Assertions.*

internal class MainKtTest {

    @org.junit.jupiter.api.Test
    fun main() {
        val t = Traslator()
        var sql : Array<String> = arrayOf("SELECT", "*", "FROM", "TABLE")
        assert(t.translate(sql) == "db.TABLE.find({})")

        sql = arrayOf("SELECT", "COL1,", "COL2,", "COL3", "FROM", "TABLE")
        assert(t.translate(sql) == "db.TABLE.find({}, COL1 : 1, COL2 : 1, COL3 : 1)")
    }
}
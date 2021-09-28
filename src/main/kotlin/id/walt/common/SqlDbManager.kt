package id.walt.common

//ANDROID PORT
import mu.KotlinLogging
import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
//ANDROID PORT
import java.sql.Statement


object SqlDbManager {
    private val log = KotlinLogging.logger {}

//    val JDBC_URL = "jdbc:sqlite:data/walt.db"
//    val JDBC_URL = "jdbc:sqlite::memory:"

    //  private val config: HikariConfig = HikariConfig()
    //ANDROID PORT
    private lateinit var connection: Connection
    //private var dataSource: HikariDataSource? = WaltIdServices.loadHikariDataSource()
    //ANDROID PORT

    // TODO: Should be configurable
    val recreateDb = false
    //ANDROID PORT
    private lateinit var androidDataDir: String
    //ANDROID PORT

    //ANDROID PORT
    init {
        try {
            DriverManager.registerDriver(Class.forName("org.sqldroid.SQLDroidDriver").newInstance() as Driver)
        } catch (e: Exception) {
            throw RuntimeException("Failed to register SQLDroidDriver")
        }

        androidDataDir = id.walt.common.androidDataDir
    }
    //ANDROID PORT

    fun start() {
//        config.jdbcUrl = JDBC_URL
//        config.maximumPoolSize = 1
//        config.isAutoCommit = false
//        config.setUsername("user")
//        config.setPassword("password")
//        config.addDataSourceProperty("cachePrepStmts", "true")
//        config.addDataSourceProperty("prepStmtCacheSize", "250")
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        // Logger.getLogger("").level = ALL

        //  ds = HikariDataSource(config)

        createDatabase()
    }

    //ANDROID PORT
    fun stop() {
        connection.close()
    }
    //ANDROID PORT

    private fun createDatabase() {
        getConnection().use { con ->
            con.createStatement().use { stmt ->

                if (recreateDb) {
                    log.debug { "Recreating database" }
                    stmt.executeUpdate("drop table if exists lt_key")
                    stmt.executeUpdate("drop table if exists lt_key_alias")
                }

                // Create lt_key
                stmt.executeUpdate(
                    "create table if not exists lt_key(" +
                            "id integer primary key autoincrement, " +
                            "name string unique, " +
                            "algorithm string, " +
                            "provider string," +
                            "priv string, " +
                            "pub string)"
                )

                // Create lt_key_alias
                stmt.executeUpdate(
                    "create table if not exists lt_key_alias(" +
                            "id integer primary key autoincrement, " +
                            "key_id integer, " +
                            "alias string unique)"
                )
            }
            //ANDROID PORT
            //con.commit()
            //ANDROID PORT
        }
    }

    //ANDROID PORT
    fun getConnection(): Connection {
        val jdbcUrl = "jdbc:sqldroid:$androidDataDir/data/walt.db"
        connection = DriverManager.getConnection(jdbcUrl)
        // var connection = DriverManager.getConnection(JDBC_URL)
        return connection
    }
    //ANDROID PORT

    fun getLastRowId(statement: Statement): Int {
        val rs = statement.executeQuery("select last_insert_rowid() AS lastRowId")
        rs.next()
        return rs.getInt("lastRowId")
    }
}

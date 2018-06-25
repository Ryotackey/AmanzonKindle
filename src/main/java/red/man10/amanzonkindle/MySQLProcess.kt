package red.man10.amanzonkindle

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import red.man10.kotlin.MySQLManager
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.collections.HashMap

class MySQLCreate(private val plugin: AmanzonKindle) : Thread() {
    override fun run() {

        val mysql = MySQLManager(plugin, "amanzon")

        mysql!!.execute("CREATE TABLE `amanzonkindle_booktable` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `book_name` varchar(50) DEFAULT NULL,\n" +
                "  `book_author_name` varchar(50) DEFAULT NULL,\n" +
                "  `book_author_uuid` varchar(50) DEFAULT NULL,\n" +
                "  `book_base64` text,\n" +
                "  `book_contents` text,\n" +
                "  `book_category` varchar(50) DEFAULT '0',\n" +
                "  `book_price` double DEFAULT '0',\n" +
                "  `book_sold_amount` int(32) DEFAULT '0',\n" +
                "  `book_fav` int(11) DEFAULT '0',\n" +
                "  `publicate_date` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                "  `enable` tinyint(4) DEFAULT '1',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")

        mysql!!.execute("CREATE TABLE `amanzonkindle_bookshelf` (\n" +
                "  `id` int(32) NOT NULL AUTO_INCREMENT,\n" +
                "  `owner_name` varchar(50) DEFAULT NULL,\n" +
                "  `owner_uuid` varchar(50) DEFAULT NULL,\n" +
                "  `buy_bookid` int(11) DEFAULT NULL,\n" +
                "  `fav` tinyint(4) DEFAULT NULL,\n" +
                "  `buy_time` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n")

        mysql!!.execute("CREATE TABLE `amanzonkindle_publicatertable` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `publicater_name` varchar(50) DEFAULT NULL,\n" +
                "  `publicater_uuid` varchar(50) DEFAULT NULL,\n" +
                "  `publicater_balance` double DEFAULT '0',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n")

        Bukkit.getLogger().info("Create Table Complite")

    }

}

class GetBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql")  != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE enable=true ORDER BY id DESC;")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null)return

        var itemList = mutableListOf<ItemStack>()

        while (rs!!.next()){

            val book: ItemStack = plugin.itemFromBase64(rs.getString("book_base64"))!!

            val bookm = book.itemMeta
            val booklore = mutableListOf<String>()

            val book_price = rs.getDouble("book_price")

            booklore.add("§6値段:${book_price}円")

            val book_amount = rs.getInt("book_sold_amount")

            booklore.add("§bダウンロード数:${book_amount}DL")

            val book_fav = rs.getInt("book_fav")

            booklore.add("§eいいね！数:${book_fav}いいね！")

            val ts = rs.getTimestamp("publicate_date")

            val TIME_FORMAT = "yyyy年MM月dd日 HH時mm分ss秒"

            booklore.add("§a${plugin.formattedTimestamp(ts, TIME_FORMAT)}")

            val cate = rs.getString("book_category")

            booklore.add(cate)

            bookm.lore = booklore

            book.itemMeta = bookm

            itemList.add(book)

        }

        mysql.close()

        plugin.openinvmap[p] = itemList

        p.closeInventory()

        p.chat("/amk getbook")

    }

}

class GetOwnBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql")  != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_bookshelf WHERE owner_uuid='${p.uniqueId}' ORDER BY id DESC;")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null)return

        var id = 0

        var itemList = mutableListOf<ItemStack>()

        while (rs!!.next()){

            id = rs.getInt("buy_bookid")

            var bookget: ResultSet? = null

            try {
                bookget = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE id='${id}';")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            if (bookget == null)return

            while (bookget!!.next()){

                val book: ItemStack = plugin.itemFromBase64(bookget.getString("book_base64"))!!

                itemList.add(book)

            }

        }
        mysql.close()


        plugin.openowninvmap[p] = itemList

        p.closeInventory()

        p.chat("/amk getownbook")

    }

}

class RegisterAuthor(private val p: Player, private val regp: Player, private val plugin: AmanzonKindle): Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val uuid: UUID = regp.uniqueId

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        rs!!.first()
        if (rs.getInt("count(1)") != 0){
            p.sendMessage("${plugin.prefix}§c既に登録されています")
            return
        }

        val regpname = regp.name

        mysql.execute("INSERT INTO amanzonkindle_publicatertable (publicater_name,publicater_uuid) VALUES('$regpname','$uuid');")

        p.sendMessage("${plugin.prefix}§e${regp.displayName}§aを登録しました")

        mysql.close()

    }
}

class DeregisterAuthor(private val p: Player, private val regp: Player, private val plugin: AmanzonKindle): Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val uuid: UUID = regp.uniqueId

        var count: ResultSet? = null

        try {
            count = mysql.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        count!!.first()
        if (count.getInt("count(1)") == 0){
            p.sendMessage("${plugin.prefix}§c登録されていません")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_author_uuid='$uuid';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        mysql.execute("DELETE FROM `amanzonkindle_booktable` WHERE `book_author_uuid`='$uuid';")

        while (rs!!.next()){

            val id = rs.getInt("id")

            mysql.execute("DELETE FROM `amanzonkindle_bookshelf` WHERE buy_bookid=$id;")

        }

        mysql.execute("DELETE FROM `amanzonkindle_publicatertable` WHERE `publicater_uuid`='$uuid';")

        p.sendMessage("${plugin.prefix}§e${regp.displayName}§aを削除しました")

        mysql.close()

    }
}

class GetBookPrice(private val p: Player, private val book: ItemStack, private val plugin: AmanzonKindle): Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val bookm = book.itemMeta
        val booklore = mutableListOf<String>()

        bookm.lore = booklore

        book.itemMeta = bookm

        val base64: String = plugin.itemToBase64(book)

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null)return

        var price = 0.0

        while (rs!!.next()){

            price = rs.getDouble("book_price")

        }

        plugin.price = price

        val guiProcess = GuiProcess(plugin)
        val inv = guiProcess.buyBookGuiCreate(book, plugin.price)

        plugin.buyBookMap[p] = inv

        p.closeInventory()

        p.chat("/amk buybook")

        mysql.close()

    }
}

class BuyBook(private val book: ItemStack, private val p: Player, private val plugin: AmanzonKindle): Thread(){

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val bookm = book.itemMeta
        val booklore = mutableListOf<String>()

        bookm.lore = booklore

        book.itemMeta = bookm

        val base64: String = plugin.itemToBase64(book)

        val author_uuid = p.uniqueId

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null)return

        var bookcount = 0

        var price = 0.0
        var id = 0

        var uuid = ""

        while (rs!!.next()){

            var count: ResultSet? = null

            try {
                count = mysql.query("SELECT count(1) FROM amanzonkindle_bookshelf WHERE buy_bookid=${rs.getInt("id")} AND owner_uuid='${p.uniqueId}';")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            count!!.first()

            bookcount = count.getInt("count(1)")

            price = rs.getDouble("book_price")

            id = rs.getInt("id")

            uuid = rs.getString("book_author_uuid")


        }

        if (bookcount != 0){
            p.sendMessage("${plugin.prefix}§cすでに持っています")
            p.closeInventory()
            return
        }

        if (plugin.vm!!.getBalance(p.uniqueId) < price){
            p.sendMessage("${plugin.prefix}§cお金が足りません")
            p.closeInventory()
            return
        }

        Bukkit.getLogger().info(price.toString())
        Bukkit.getLogger().info(plugin.config!!.getConfig()!!.getDouble("authorbalance").toString())

        mysql.execute("INSERT INTO amanzonkindle_bookshelf (owner_name,owner_uuid,buy_bookid,fav) VALUES('${p.name}','${p.uniqueId}','$id',false);")
        mysql.execute("UPDATE amanzonkindle_publicatertable SET publicater_balance=publicater_balance+${price*plugin.config!!.getConfig()!!.getDouble("authorbalance")} WHERE publicater_uuid='${uuid}';")
        mysql.execute("UPDATE amanzonkindle_booktable SET book_sold_amount=book_sold_amount+1 WHERE book_base64='$base64';")

        p.sendMessage("${plugin.prefix}§a本を購入しました")

        plugin.vm!!.withdraw(p.uniqueId, price)

        p.closeInventory()

        val gob = GetOwnBook(p, plugin)
        gob.start()

        mysql.close()

    }

}

class SearchBook(private val kind: String, private val keyword: String, private val p: Player, private val plugin: AmanzonKindle): Thread(){

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = if (kind == "author_name"){
                mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_$kind='$keyword' AND enable=true;")
            }else {
                mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_$kind LIKE '%$keyword%'AND enable=true;")
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null){
            p.sendMessage("${plugin.prefix}§cその本は見つかりませんでした")
            return
        }

        var itemList = mutableListOf<ItemStack>()

        while (rs!!.next()){

            val book: ItemStack = plugin.itemFromBase64(rs.getString("book_base64"))!!

            val bookm = book.itemMeta
            val booklore = mutableListOf<String>()

            val book_price = rs.getDouble("book_price")

            booklore.add("§6値段:${book_price}円")

            val book_amount = rs.getInt("book_sold_amount")

            booklore.add("§bダウンロード数:${book_amount}DL")

            val book_fav = rs.getInt("book_fav")

            booklore.add("§eいいね！数:${book_fav}いいね！")

            val ts = rs.getTimestamp("publicate_date")

            val TIME_FORMAT = "yyyy年MM月dd日 HH時mm分ss秒"

            booklore.add("§a${plugin.formattedTimestamp(ts, TIME_FORMAT)}")

            val cate = rs.getString("book_category")

            booklore.add(cate)

            bookm.lore = booklore

            book.itemMeta = bookm

            itemList.add(book)

        }

        plugin.searchinvmap[p] = itemList

        mysql.close()

        p.closeInventory()

        p.chat("/amk getsearch")

    }

}

class PublicateBook(private val cate: String, private val book: ItemStack, private val price: Double, private val p: Player, private val plugin: AmanzonKindle) : Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql")  != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            p.closeInventory()
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            p.closeInventory()
            return
        }

        val bookmeta: BookMeta = book.itemMeta as BookMeta
        val book_name = bookmeta.title

        val bookcontents = bookmeta.pages

        var bookcontent = ""

        for (i in 0 until bookcontents.size) {
            bookcontent += bookcontents[i]
        }

        var author_uuid: UUID = p.uniqueId
        val book_author_name = p.name

        book.amount = 1

        val book_base64 = plugin.itemToBase64(book)

        var resultSet: ResultSet? = null

        try {
            resultSet = mysql.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$author_uuid';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        resultSet!!.first()
        if (resultSet!!.getInt("count(1)") == 0){
            p.sendMessage("${plugin.prefix}§cこの本の著者は出版者として登録されていません")
            p.closeInventory()
            return
        }else {

            var count: ResultSet? = null

            try {
                count = mysql.query("SELECT count(1) FROM amanzonkindle_booktable WHERE book_base64='$book_base64';")!!
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            count!!.first()
            if (count!!.getInt("count(1)") == 0) {

                mysql.execute("INSERT INTO amanzonkindle_booktable (book_name,book_author_name,book_author_uuid,book_base64,book_contents,book_category,book_price) " +
                        "VALUES('$book_name','$book_author_name','$author_uuid','$book_base64','$bookcontent','$cate',$price);")

                Bukkit.getLogger().info("insert complite")

            } else {
                p.sendMessage("${plugin.prefix}§cこの本はすでに出版されています")
                p.closeInventory()
                return
            }

            p.sendMessage("${plugin.prefix}§e${book_name}§aを§6${price}円§aで出版しました")
            Bukkit.broadcastMessage("${plugin.prefix}§e§l${p.displayName}§f§lが§e§l${book_name}§f§lを出版しました")

            p.closeInventory()

            mysql.close()

        }
    }
}

class GetBalance(private val p: Player, private val plugin: AmanzonKindle) : Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val uuid = p.uniqueId

        var resultSet: ResultSet? = null

        try {
            resultSet = mysql.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        resultSet!!.first()
        if (resultSet!!.getInt("count(1)") == 0){
            p.sendMessage("${plugin.prefix}§cあなたは出版者として登録されていません")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        var balance = 0.0

        while (rs!!.next()) {
            balance = rs!!.getDouble("publicater_balance")
        }

        if (balance == 0.0){
            p.sendMessage("${plugin.prefix}§c印税はありませんでした")
            return
        }

        mysql.execute("UPDATE amanzonkindle_publicatertable SET publicater_balance=0 WHERE publicater_uuid='$uuid';")

        p.sendMessage("${plugin.prefix}§a印税として§6${balance}円§a振り込まれました")
        plugin.vm!!.deposit(uuid, balance)

        mysql.close()

    }
}

class GetFavBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql")  != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE enable=true ORDER BY book_fav DESC;")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null)return

        var itemList = mutableListOf<ItemStack>()

        while (rs!!.next()){

            val book: ItemStack = plugin.itemFromBase64(rs.getString("book_base64"))!!

            val bookm = book.itemMeta
            val booklore = mutableListOf<String>()

            val book_price = rs.getDouble("book_price")

            booklore.add("§6値段:${book_price}円")

            val book_amount = rs.getInt("book_sold_amount")

            booklore.add("§bダウンロード数:${book_amount}DL")

            val book_fav = rs.getInt("book_fav")

            booklore.add("§eいいね！数:${book_fav}いいね！")

            val ts = rs.getTimestamp("publicate_date")

            val TIME_FORMAT = "yyyy年MM月dd日 HH時mm分ss秒"

            booklore.add("§a${plugin.formattedTimestamp(ts, TIME_FORMAT)}")

            val cate = rs.getString("book_category")

            booklore.add(cate)

            bookm.lore = booklore

            book.itemMeta = bookm

            itemList.add(book)

        }

        plugin.searchinvmap[p] = itemList

        p.chat("/amk getsearch")

        mysql.close()

    }

}

class ShowPlayerSale(private val p: Player, private val plugin: AmanzonKindle): Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val uuid = p.uniqueId

        var count: ResultSet? = null

        try {
            count = mysql.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        count!!.first()
        if (count!!.getInt("count(1)") == 0){
            p.sendMessage("${plugin.prefix}§cあなたは出版者として登録されていません")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_author_uuid='$uuid';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null) {
            p.sendMessage("${plugin.prefix}§あなたは本を出版してません")
            return
        }

        p.sendMessage("${plugin.prefix}§e§l本の売り上げ")

        while (rs!!.next()){

            p.sendMessage("§6§l${rs.getString("book_name")}§f: " + "${rs.getInt("book_sold_amount")}DL/" +
                    "${rs.getInt("book_sold_amount")*rs.getDouble("book_price")}円/${rs.getInt("book_fav")}いいね")

        }

        var resultSet: ResultSet? = null

        try {
            resultSet = mysql.query("SELECT * FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        resultSet!!.first()
        p.sendMessage("§f§l貯まっている印税合計:${resultSet.getDouble("publicater_balance")}")
        plugin.event!!.sendHoverText(p, "§f§l§n印税を受け取るにはここをクリック！！！", "印税を受け取る", "/amk getbalance")

        mysql.close()

    }
}

class FavProcess(private val book: ItemStack, private val boolean: Boolean, private val p: Player, private val plugin: AmanzonKindle): Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val base64 = plugin.itemToBase64(book)

        var resultSet: ResultSet? = null

        try {
            resultSet = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        var book_id = 0

        while (resultSet!!.next()){
            book_id = resultSet.getInt("id")
        }

        if (boolean){

            mysql.execute("UPDATE amanzonkindle_bookshelf SET fav=false WHERE buy_bookid=$book_id;")
            mysql.execute("UPDATE amanzonkindle_booktable SET book_fav=book_fav-1 WHERE id=$book_id;")

            p.sendMessage("${plugin.prefix}§aいいね！を解除しました")

        }else{

            mysql.execute("UPDATE amanzonkindle_bookshelf SET fav=true WHERE buy_bookid=${book_id};")
            mysql.execute("UPDATE amanzonkindle_booktable SET book_fav=book_fav+1 WHERE id=$book_id;")

            p.sendMessage("${plugin.prefix}§aいいね！しました")

        }

        mysql.close()

        val getFav = GetFav(book, p, plugin)
        getFav.start()

    }
}

class GetFav(private val book: ItemStack, private val p: Player, private val plugin: AmanzonKindle): Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val base64 = plugin.itemToBase64(book)

        var resultSet: ResultSet? = null

        try {
            resultSet = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        var book_id = 0

        while (resultSet!!.next()) {
            book_id = resultSet.getInt("id")
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_bookshelf WHERE buy_bookid='$book_id' AND owner_uuid='${p.uniqueId}';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        rs!!.first()

        val gp = GuiProcess(plugin)
        val inv = gp.bookGui(rs.getBoolean("fav"), book)

        plugin.getFavMap[p] = inv

        p.closeInventory()

        p.chat("/amk getfav")

        mysql.close()

    }
}

class GetDLBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql")  != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE enable=true ORDER BY book_sold_amount DESC;")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null)return

        var itemList = mutableListOf<ItemStack>()

        while (rs!!.next()){

            val book: ItemStack = plugin.itemFromBase64(rs.getString("book_base64"))!!

            val bookm = book.itemMeta
            val booklore = mutableListOf<String>()

            val book_price = rs.getDouble("book_price")

            booklore.add("§6値段:${book_price}円")

            val book_amount = rs.getInt("book_sold_amount")

            booklore.add("§bダウンロード数:${book_amount}DL")

            val book_fav = rs.getInt("book_fav")

            booklore.add("§eいいね！数:${book_fav}いいね！")

            val ts = rs.getTimestamp("publicate_date")

            val TIME_FORMAT = "yyyy年MM月dd日 HH時mm分ss秒"

            booklore.add("§a${plugin.formattedTimestamp(ts, TIME_FORMAT)}")

            val cate = rs.getString("book_category")

            booklore.add(cate)

            bookm.lore = booklore

            book.itemMeta = bookm

            itemList.add(book)

        }

        plugin.searchinvmap[p] = itemList

        p.chat("/amk getsearch")

        mysql.close()

    }

}

class GetOPBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql")  != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable ORDER BY id DESC;")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null)return

        var itemList = mutableListOf<ItemStack>()

        while (rs!!.next()){

            val book: ItemStack = plugin.itemFromBase64(rs.getString("book_base64"))!!

            itemList.add(book)

        }

        mysql.close()

        val itemhash: HashMap<Player, MutableList<ItemStack>> = HashMap()
        itemhash[p] = itemList

        val gp = GuiProcess(plugin)
        val inv = gp.allBookGuiCreate(p,1, "§6§lOP用本一覧", itemhash)

        plugin.getOPMap[p] = inv

        p.chat("/amk openbookop")

    }

}

class RemoveBook(private val book: ItemStack, private val p: Player, private val plugin: AmanzonKindle): Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val base64 = plugin.itemToBase64(book)

        mysql.execute("UPDATE amanzonkindle_booktable SET enable=false WHERE book_base64='$base64';")

        p.sendMessage("${plugin.prefix}§a削除しました")

        mysql.close()

        val gb = GetBook(p, plugin)
        gb.start()

    }
}

class DeleteBook(private val book: ItemStack, private val p: Player, private val plugin: AmanzonKindle): Thread() {

    val mysql = MySQLManager(plugin, "amanzon")

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql") != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    @Synchronized
    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (mysql.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val base64 = plugin.itemToBase64(book)

        var rs: ResultSet? = null

        try {
            rs = mysql.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null){

            p.sendMessage("${plugin.prefix}§cこの本は出版されてません")
            return

        }

        mysql.execute("DELETE FROM `amanzonkindle_booktable` WHERE `book_base64`='$base64';")

        while (rs!!.next()) {
            mysql.execute("DELETE FROM `amanzonkindle_bookshelf` WHERE buy_bookid=${rs.getInt("id")};")
        }

        mysql.close()

        val bookm = book.itemMeta as BookMeta

        p.sendMessage("${plugin.prefix}§e${bookm.title}§aを削除しました")

    }
}
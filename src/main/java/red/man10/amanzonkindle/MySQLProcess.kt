package red.man10.amanzonkindle

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.ExecutionException

class MySQLCreate(private val plugin: AmanzonKindle) : Thread() {
    override fun run() {

        plugin.mysql!!.execute("CREATE TABLE `amanzonkindle_booktable` (\n" +
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

        plugin.mysql!!.execute("CREATE TABLE `amanzonkindle_bookshelf` (\n" +
                "  `id` int(32) NOT NULL AUTO_INCREMENT,\n" +
                "  `owner_name` varchar(50) DEFAULT NULL,\n" +
                "  `owner_uuid` varchar(50) DEFAULT NULL,\n" +
                "  `buy_bookid` int(11) DEFAULT NULL,\n" +
                "  `fav` tinyint(4) DEFAULT NULL,\n" +
                "  `buy_time` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n")

        plugin.mysql!!.execute("CREATE TABLE `amanzonkindle_publicatertable` (\n" +
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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE enable=true ORDER BY id DESC;")
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

        plugin.openinvmap[p] = itemList

    }

}

class GetOwnBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_bookshelf WHERE owner_uuid='${p.uniqueId}' ORDER BY id DESC;")
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
                bookget = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE id='${id}';")
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

        plugin.openowninvmap[p] = itemList

    }

}

class RegisterAuthor(private val p: Player, private val regp: Player, private val plugin: AmanzonKindle): Thread() {
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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val uuid: UUID = regp.uniqueId

        var rs: ResultSet? = null

        try {
            rs = plugin.mysql!!.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")
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

        plugin.mysql!!.execute("INSERT INTO amanzonkindle_publicatertable (publicater_name,publicater_uuid) VALUES('$regpname','$uuid');")

        p.sendMessage("${plugin.prefix}§e${regp.displayName}§aを登録しました")

    }
}

class DeregisterAuthor(private val p: Player, private val regp: Player, private val plugin: AmanzonKindle): Thread() {
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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val uuid: UUID = regp.uniqueId

        var count: ResultSet? = null

        try {
            count = plugin.mysql!!.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")
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
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE book_author_uuid='$uuid';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        plugin.mysql!!.execute("DELETE FROM `amanzonkindle_booktable` WHERE `book_author_uuid`='$uuid';")

        while (rs!!.next()){

            val id = rs.getInt("id")

            plugin.mysql!!.execute("DELETE FROM `amanzonkindle_bookshelf` WHERE buy_bookid=$id;")

        }

        plugin.mysql!!.execute("DELETE FROM `amanzonkindle_publicatertable` WHERE `publicater_uuid`='$uuid';")

        p.sendMessage("${plugin.prefix}§e${regp.displayName}§aを削除しました")

    }
}

class GetBookPrice(private val p: Player, private val book: ItemStack, private val plugin: AmanzonKindle): Thread() {
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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
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
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")
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

    }
}

class BuyBook(private val book: ItemStack, private val p: Player, private val plugin: AmanzonKindle): Thread(){

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
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
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (rs == null)return

        var bookcount = 0

        var price = 0.0
        var id = 0

        while (rs!!.next()){

            var count: ResultSet? = null

            try {
                count = plugin.mysql!!.query("SELECT count(1) FROM amanzonkindle_bookshelf WHERE buy_bookid=${rs.getInt("id")} AND owner_uuid='${p.uniqueId}';")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            count!!.first()

            bookcount = count.getInt("count(1)")

            price = rs.getDouble("book_price")

            id = rs.getInt("id")


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

        plugin.mysql!!.execute("INSERT INTO amanzonkindle_bookshelf (owner_name,owner_uuid,buy_bookid,fav) VALUES('${p.name}','${p.uniqueId}','$id',false);")
        plugin.mysql!!.execute("UPDATE amanzonkindle_publicatertable SET publicater_balance=publicater_balance+${price*plugin.config!!.getConfig()!!.getDouble("authorbalance")} WHERE publicater_uuid='${author_uuid}';")
        plugin.mysql!!.execute("UPDATE amanzonkindle_booktable SET book_sold_amount=book_sold_amount+1 WHERE book_base64='$base64';")

        p.sendMessage("${plugin.prefix}§a本を購入しました")

        plugin.vm!!.withdraw(p.uniqueId, price)

        p.closeInventory()

        p.chat("/amk bookshelf")

    }

}

class SearchBook(private val kind: String, private val keyword: String, private val p: Player, private val plugin: AmanzonKindle): Thread(){

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = if (kind == "author_name"){
                plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE book_$kind='$keyword' AND enable=true;")
            }else {
                plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE book_$kind LIKE '%$keyword%'AND enable=true;")
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
            val booklore = bookm.lore

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

    }

}

class PublicateBook(private val cate: String, private val book: ItemStack, private val price: Double, private val p: Player, private val plugin: AmanzonKindle) : Thread() {

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            p.closeInventory()
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
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
            resultSet = plugin.mysql!!.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$author_uuid';")!!
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
                count = plugin.mysql!!.query("SELECT count(1) FROM amanzonkindle_booktable WHERE book_base64='$book_base64';")!!
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            count!!.first()
            if (count!!.getInt("count(1)") == 0) {

                plugin.mysql!!.execute("INSERT INTO amanzonkindle_booktable (book_name,book_author_name,book_author_uuid,book_base64,book_contents,book_category,book_price) " +
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

        }
    }
}

class GetBalance(private val p: Player, private val plugin: AmanzonKindle) : Thread() {

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val uuid = p.uniqueId

        var resultSet: ResultSet? = null

        try {
            resultSet = plugin.mysql!!.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")!!
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
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")!!
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

        plugin.mysql!!.execute("UPDATE amanzonkindle_publicatertable SET publicater_balance=0 WHERE publicater_uuid='$uuid';")

        p.sendMessage("${plugin.prefix}§a印税として§6${balance}円§a振り込まれました")
        plugin.vm!!.deposit(uuid, balance)

    }
}

class GetFavBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE enable=true ORDER BY book_fav DESC;")
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

    }

}

class ShowPlayerSale(private val p: Player, private val plugin: AmanzonKindle): Thread() {

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val uuid = p.uniqueId

        var count: ResultSet? = null

        try {
            count = plugin.mysql!!.query("SELECT count(1) FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")!!
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
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE book_author_uuid='$uuid';")!!
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
                    "${rs.getInt("book_sold_amount")*rs.getDouble("book_price")*plugin.config!!.getConfig()!!.getDouble("authorbalance")}円/${rs.getInt("book_fav")}いいね")

        }

        var resultSet: ResultSet? = null

        try {
            resultSet = plugin.mysql!!.query("SELECT * FROM amanzonkindle_publicatertable WHERE publicater_uuid='$uuid';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        resultSet!!.first()
        plugin.event!!.sendHoverText(p, "§f§l印税合計:${resultSet.getDouble("publicater_balance")}", "印税を受け取る", "/amk getbalance")

    }
}

class FavProcess(private val book: ItemStack, private val boolean: Boolean, private val p: Player, private val plugin: AmanzonKindle): Thread() {

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val base64 = plugin.itemToBase64(book)

        var resultSet: ResultSet? = null

        try {
            resultSet = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")!!
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

            plugin.mysql!!.execute("UPDATE amanzonkindle_bookshelf SET fav=false WHERE buy_bookid=$book_id;")
            plugin.mysql!!.execute("UPDATE amanzonkindle_booktable SET book_fav=book_fav-1 WHERE id=$book_id;")

            p.sendMessage("${plugin.prefix}§aいいね！を解除しました")

        }else{

            plugin.mysql!!.execute("UPDATE amanzonkindle_bookshelf SET fav=true WHERE buy_bookid=${book_id};")
            plugin.mysql!!.execute("UPDATE amanzonkindle_booktable SET book_fav=book_fav+1 WHERE id=$book_id;")

            p.sendMessage("${plugin.prefix}§aいいね！しました")

        }

    }
}

class GetFav(private val book: ItemStack, private val p: Player, private val plugin: AmanzonKindle): Thread() {

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val base64 = plugin.itemToBase64(book)

        var resultSet: ResultSet? = null

        try {
            resultSet = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE book_base64='$base64';")!!
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
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_bookshelf WHERE buy_bookid='$book_id';")!!
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        rs!!.first()

        plugin.favmap[p] = rs.getBoolean("fav")

    }
}

class GetDLBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable WHERE enable=true ORDER BY book_sold_amount DESC;")
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

    }

}

class GetOPBook(private val p: Player, private val plugin: AmanzonKindle): Thread(){

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        var rs: ResultSet? = null

        try {
            rs = plugin.mysql!!.query("SELECT * FROM amanzonkindle_booktable ORDER BY id DESC;")
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

        plugin.openinvmap[p] = itemList

    }

}

class RemoveBook(private val book: ItemStack, private val p: Player, private val plugin: AmanzonKindle): Thread() {

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

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql") == null) {
            Bukkit.getLogger().info("mysql is null")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {
            Bukkit.getLogger().info("Failed Conected MySQL")
            p.sendMessage("${plugin.prefix}§cMysqlデータベースに接続できないため失敗しました")
            return
        }

        val base64 = plugin.itemToBase64(book)

        plugin.mysql!!.execute("UPDATE amanzonkindle_booktable SET enable=false WHERE book_base64='$base64';")

        p.sendMessage("${plugin.prefix}§a削除しました")

    }
}
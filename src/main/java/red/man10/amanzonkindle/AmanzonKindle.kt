package red.man10.amanzonkindle

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import red.man10.kotlin.CustomConfig
import red.man10.kotlin.MySQLManager
import red.man10.kotlin.VaultManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.sql.Timestamp
import kotlin.collections.HashMap
import java.text.SimpleDateFormat



class AmanzonKindle : JavaPlugin() {
    var vm: VaultManager? = null
    var config: CustomConfig? = null
    var mysql: MySQLManager? = null
    var event: Event? = null

    var openinvmap: HashMap<Player, MutableList<ItemStack>> = HashMap()
    var openowninvmap: HashMap<Player, MutableList<ItemStack>> = HashMap()
    var searchinvmap: HashMap<Player, MutableList<ItemStack>> = HashMap()
    var buyBookMap: HashMap<Player, Inventory> = HashMap()
    var getFavMap: HashMap<Player, Inventory> = HashMap()
    var getOPMap: HashMap<Player, Inventory> = HashMap()

    var searchTitlePlayer: MutableList<Player> = mutableListOf()
    var searchContentsPlayer: MutableList<Player> = mutableListOf()
    var searchAuthorPlayer: MutableList<Player> = mutableListOf()
    var PublicatePlayer: MutableList<Player> = mutableListOf()

    var price = 0.0

    val prefix = "§l[§a§lA§d§lma§f§ln§a§lzon§e§lKindle§f§l]"

    override fun onEnable() {
        // Plugin startup logic

        vm = VaultManager(this)
        config = CustomConfig(this)
        mysql = MySQLManager(this, "amanzon")
        event = Event(this)

        config!!.saveDefaultConfig()

        server.pluginManager.registerEvents(event, this)
        getCommand("amk").executor = Command(this)

        val create = MySQLCreate(this)
        create.start()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun itemFromBase64(data: String): ItemStack? {
        try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val items = arrayOfNulls<ItemStack>(dataInput.readInt())

            // Read the serialized inventory
            for (i in items.indices) {
                items[i] = dataInput.readObject() as ItemStack
            }

            dataInput.close()
            return items[0]
        } catch (e: Exception) {
            return null
        }

    }

    @Throws(IllegalStateException::class)
    fun itemToBase64(item: ItemStack): String {
        try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)
            val items = arrayOfNulls<ItemStack>(1)
            items[0] = item
            dataOutput.writeInt(items.size)

            for (i in items.indices) {
                dataOutput.writeObject(items[i])
            }

            dataOutput.close()
            val base64: String = Base64Coder.encodeLines(outputStream.toByteArray())

            return base64

        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }


    }

    fun formattedTimestamp(timestamp: Timestamp, timeFormat: String): String {
        return SimpleDateFormat(timeFormat).format(timestamp)
    }
}



package red.man10.amanzonkindle

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*
import kotlin.collections.HashMap

class GuiProcess(private val plugin: AmanzonKindle){

    fun allBookGuiCreate(p: Player, pagenumber: Int, title: String, map: HashMap<Player, MutableList<ItemStack>>): Inventory {
        val itemlist: MutableList<ItemStack> = map[p]!!

        val blank = ItemStack(Material.STAINED_GLASS_PANE, 1, 15)
        val blankm = blank.itemMeta
        blankm.displayName = ""
        blank.itemMeta = blankm

        val next = ItemStack(Material.PAPER)
        val nextm = next.itemMeta
        nextm.displayName = "§l次のページ"
        next.itemMeta = nextm

        val prev = ItemStack(Material.PAPER)
        val prevm = prev.itemMeta
        prevm.displayName = "§l前のページ"
        prev.itemMeta = prevm

        val page = ItemStack(Material.COMPASS)
        val pagem = page.itemMeta
        pagem.displayName = pagenumber.toString()
        page.itemMeta = pagem

        val inv: Inventory = Bukkit.getServer().createInventory(null, 54, "$title")

        if (pagenumber == 1) {
            inv.setItem(45, blank)
        }else{
            inv.setItem(45, prev)
        }
        inv.setItem(53, next)
        inv.setItem(46, blank)
        inv.setItem(47, blank)
        inv.setItem(48, blank)
        inv.setItem(49, page)
        inv.setItem(50, blank)
        inv.setItem(51, blank)
        inv.setItem(52, blank)

        var index = pagenumber * 45 - 45
        Bukkit.getLogger().info(index.toString())
        var endIndex= 0
        if (index + 45 >= itemlist.size){
            endIndex = itemlist.size
            inv.setItem(53, blank)
        }else {
            endIndex = index + 45
        }

        while (index < endIndex) {
            inv.setItem(index%45, itemlist[index])
            index++
        }

            return inv
        }

    fun buyBookGuiCreate(book: ItemStack, price: Double): Inventory{

        val blank = ItemStack(Material.STAINED_GLASS_PANE, 1, 15)
        val blankm = blank.itemMeta
        blankm.displayName = ""
        blank.itemMeta = blankm

        val buy = ItemStack(Material.STAINED_GLASS_PANE, 1, 5)
        val buym = buy.itemMeta
        buym.displayName = "§a§l購入"
        val lore = Arrays.asList<String>("§6§l${price}円")
        buym.lore = lore
        buy.itemMeta = buym

        val cancel = ItemStack(Material.STAINED_GLASS_PANE, 1, 14)
        val cancelm = cancel.itemMeta
        cancelm.displayName = "§c§lキャンセル"
        cancel.itemMeta = cancelm

        val inv: Inventory = Bukkit.getServer().createInventory(null, 27, "§6§l本を購入する")

        val slot1 = arrayOf(9, 10, 11, 12)
        val slot2 = arrayOf(14, 15, 16, 17)

        for (i in 0 until inv.size){
            inv.setItem(i, blank)
        }

        for (i in 0 until slot1.size){
            inv.setItem(slot1[i], cancel)
            inv.setItem(slot2[i], buy)
        }

        inv.setItem(13, book)

        return inv

    }

    fun mainMenuCreate(): Inventory{

        val buybook = ItemStack(Material.WRITTEN_BOOK, 1)
        val buybookm = buybook.itemMeta
        buybookm.displayName = "§6§l販売されてる本を見る"
        buybook.itemMeta = buybookm

        val booklist = ItemStack(Material.BOOKSHELF, 1)
        val booklistm = booklist.itemMeta
        booklistm.displayName = "§6§l自分の本棚を見る"
        booklist.itemMeta = booklistm

        val search = ItemStack(Material.COMPASS, 1)
        val searchm = search.itemMeta
        searchm.displayName = "§6§l本を検索する"
        search.itemMeta = searchm

        val publicate = ItemStack(Material.DISPENSER, 1)
        val publicatem = publicate.itemMeta
        publicatem.displayName = "§6§l本を出版する"
        publicate.itemMeta = publicatem

        val balance = ItemStack(Material.INK_SACK, 1, 7)
        val balancem = balance.itemMeta
        balancem.displayName = "§6§l自分の売上を確認する"
        balance.itemMeta = balancem

        val inv: Inventory = Bukkit.getServer().createInventory(null, 45, "§a§lA§d§lma§f§ln§a§lzon§e§lKindle")

        inv.setItem(11, buybook)
        inv.setItem(13, search)
        inv.setItem(15, booklist)
        inv.setItem(30, publicate)
        inv.setItem(32, balance)

        return inv
    }

    fun searchGui(): Inventory{

        val title = ItemStack(Material.SIGN, 1)
        val titlem = title.itemMeta
        titlem.displayName = "§e§l題名で検索する"
        title.itemMeta = titlem

        val contents = ItemStack(Material.BOOK_AND_QUILL, 1)
        val contentsm = contents.itemMeta
        contentsm.displayName = "§e§l内容で検索する"
        contents.itemMeta = contentsm

        val author = ItemStack(Material.FEATHER, 1)
        val authorm = author.itemMeta
        authorm.displayName = "§e§l著者で検索する"
        author.itemMeta = authorm

        val fav = ItemStack(Material.DIAMOND_HOE, 1, 801)
        val favm = fav.itemMeta
        favm.displayName = "§e§lいいね！順で検索する"
        favm.isUnbreakable = true
        favm.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        fav.itemMeta = favm

        val DL = ItemStack(Material.OBSERVER, 1)
        val DLm = DL.itemMeta
        DLm.displayName = "§e§lDL順で検索する"
        DL.itemMeta = DLm

        val inv: Inventory = Bukkit.getServer().createInventory(null, 45, "§6§l検索メニュー")

        inv.setItem(11, title)
        inv.setItem(13, contents)
        inv.setItem(15, author)
        inv.setItem(30, fav)
        inv.setItem(32, DL)

        return inv

    }

    fun bookGui(favboolean: Boolean, book: ItemStack): Inventory{

        val buybook = ItemStack(Material.WRITTEN_BOOK, 1)
        val buybookm = buybook.itemMeta
        buybookm.displayName = "§6§l本を読む"
        buybook.itemMeta = buybookm

        val fav: ItemStack
        val favm: ItemMeta
        if (favboolean) {
            fav = ItemStack(org.bukkit.Material.DIAMOND_HOE, 1, 800)
            favm = fav.itemMeta
            favm.displayName = "§6§lいいね！を解除する"

        }else{
            fav = ItemStack(Material.DIAMOND_HOE, 1, 801)
            favm = fav.itemMeta
            favm.displayName = "§6§lいいね！する"
        }
        favm.isUnbreakable = true
        favm.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        fav.itemMeta = favm

        val inv: Inventory = Bukkit.getServer().createInventory(null, 27, "§6§l本メニュー")

        inv.setItem(12, buybook)
        inv.setItem(14, fav)
        inv.setItem(4, book)

        return inv

    }

    fun publicateGui(book1: ItemStack, price: Double): Inventory{

        val inv: Inventory = Bukkit.getServer().createInventory(null, 27, "§6§l本を出版する")

        val book = book1
        val bookm = book.itemMeta
        val booklore = mutableListOf<String>()

        booklore.add(price.toString())

        bookm.lore = booklore
        book.itemMeta = bookm

        for (i in 0 until plugin.config!!.getConfig()!!.getConfigurationSection("category").getKeys(false).size) {

            val category = ItemStack(Material.BOOK, 1)
            val categorym = category.itemMeta
            categorym.displayName = "${plugin.config!!.getConfig()!!.getString("category.$i")}"
            category.itemMeta = categorym

            inv.setItem(i+9, category)

        }

        inv.setItem(4, book)

        return inv

    }

    fun removeBookGui(book: ItemStack): Inventory{

        val inv: Inventory = Bukkit.getServer().createInventory(null, 45, "§c§l本を削除する")

        val bookm = book.itemMeta
        val booklore = mutableListOf<String>()
        bookm.lore = booklore
        book.itemMeta = bookm

        inv.setItem(13, book)

        val buy = ItemStack(Material.STAINED_GLASS_PANE, 1, 5)
        val buym = buy.itemMeta
        buym.displayName = "§a§l削除"
        buy.itemMeta = buym

        val cancel = ItemStack(Material.STAINED_GLASS_PANE, 1, 14)
        val cancelm = cancel.itemMeta
        cancelm.displayName = "§c§lキャンセル"
        cancel.itemMeta = cancelm

        inv.setItem(30, buy)
        inv.setItem(32, cancel)

        return inv

    }

}
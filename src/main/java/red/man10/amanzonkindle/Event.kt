package red.man10.amanzonkindle

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemStack
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.meta.BookMeta


class Event(private val plugin: AmanzonKindle) : Listener {

    @EventHandler
    fun onClickEvent(e: InventoryClickEvent) {

        val p: Player = e.whoClicked as Player
        val item = e.currentItem

        if (e.clickedInventory.name == null)return

        if (e.clickedInventory.name == "§6§l本一覧") {

            e.isCancelled = true

            if (!item.hasItemMeta()) {
                return
            }

            if (e.click == ClickType.MIDDLE) {

                if (item.type == Material.WRITTEN_BOOK) {

                    if (!p.hasPermission("amanzon.op")) return

                    val guiProcess = GuiProcess(plugin)
                    p.openInventory(guiProcess.removeBookGui(item))

                }

            } else {

                if (item.itemMeta.displayName == "§l次のページ") {

                    val pageitem = e.clickedInventory.getItem(49)
                    val page = pageitem.itemMeta.displayName.toInt() + 1

                    val gp = GuiProcess(plugin)

                    p.openInventory(gp.allBookGuiCreate(p, page, "§6§l本一覧", plugin.openinvmap))

                }

                if (item.itemMeta.displayName == "§l前のページ") {

                    val pageitem = e.clickedInventory.getItem(49)
                    val page = pageitem.itemMeta.displayName.toInt() - 1

                    val gp = GuiProcess(plugin)

                    p.openInventory(gp.allBookGuiCreate(p, page, "§6§l本一覧", plugin.openinvmap))

                }

                if (item.type == Material.WRITTEN_BOOK) {

                    val gp = GetBookPrice(p, item, plugin)
                    gp.start()
                    try {
                        gp.join(1500) // タイムアウト
                        gp.interrupt() // 中断
                    } catch (e: InterruptedException) {
                        // 例外処理
                    }

                    val guiProcess = GuiProcess(plugin)
                    p.openInventory(guiProcess.buyBookGuiCreate(item, plugin.price))

                }
            }
        }


        if (e.clickedInventory.name == "§a§lA§d§lma§f§ln§a§lzon§e§lKindle") {

            e.isCancelled = true

            if (item.type == Material.DISPENSER) {

                plugin.PublicatePlayer.add(p)

                p.sendMessage("${plugin.prefix}§a出版したい本を手に持って、値段をチャットに入力してください")

                p.closeInventory()

            }

            if (item.type == Material.COMPASS) {

                val gp = GuiProcess(plugin)
                p.openInventory(gp.searchGui())

            }

            if (item.type == Material.BOOKSHELF) {

                val gob = GetOwnBook(p, plugin)
                gob.start()
                try {
                    gob.join(1500) // タイムアウト
                    gob.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gp = GuiProcess(plugin)
                p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l自分の本棚", plugin.openowninvmap))
            }

            if (item.type == Material.WRITTEN_BOOK) {

                if (!p.hasPermission("amanzon.menu")) return

                val gb = GetBook(p, plugin)
                gb.start()
                try {
                    gb.join(1500) // タイムアウト
                    gb.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gp = GuiProcess(plugin)
                p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l本一覧", plugin.openinvmap))

            }

            if (item.type == Material.INK_SACK) {

                p.closeInventory()

                val showPlayerSale = ShowPlayerSale(p, plugin)
                showPlayerSale.start()
            }

        }

        if (e.clickedInventory.name == "§6§l本を購入する") {

            e.isCancelled = true

            val cancel = ItemStack(Material.STAINED_GLASS_PANE, 1, 14)
            val cancelm = cancel.itemMeta
            cancelm.displayName = "§c§lキャンセル"
            cancel.itemMeta = cancelm

            if (item.itemMeta.displayName == "§a§l購入") {

                val book: ItemStack = e.clickedInventory.getItem(13)

                val buyBook = BuyBook(book, p, plugin)
                buyBook.start()

            }

            if (item == cancel) {

                if (!p.hasPermission("amanzon.menu")) return

                val gb = GetBook(p, plugin)
                gb.start()
                try {
                    gb.join(1500) // タイムアウト
                    gb.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gp = GuiProcess(plugin)
                p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l本一覧", plugin.openinvmap))

            }

        }

        if (e.clickedInventory.name == "§6§l自分の本棚") {

            e.isCancelled = true

            if (!item.hasItemMeta()) {
                return
            }

            if (item.itemMeta.displayName == "§l次のページ") {

                val pageitem = e.clickedInventory.getItem(49)
                val page = pageitem.itemMeta.displayName.toInt() + 1

                val gp = GuiProcess(plugin)

                p.openInventory(gp.allBookGuiCreate(p, page, "§6§l自分の本棚", plugin.openowninvmap))

            }

            if (item.itemMeta.displayName == "§l前のページ") {

                val pageitem = e.clickedInventory.getItem(49)
                val page = pageitem.itemMeta.displayName.toInt() - 1

                val gp = GuiProcess(plugin)

                p.openInventory(gp.allBookGuiCreate(p, page, "§6§l自分の本棚", plugin.openowninvmap))

            }

            if (item.type == Material.WRITTEN_BOOK) {

                val getFav = GetFav(item, p, plugin)
                getFav.start()
                try {
                    getFav.join(1500) // タイムアウト
                    getFav.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gp = GuiProcess(plugin)
                p.openInventory(gp.bookGui(plugin.favmap[p]!!, item))

            }

        }
        if (e.clickedInventory.name == "§6§l検索メニュー") {

            if (item.type == Material.BOOK_AND_QUILL) {

                plugin.searchContentsPlayer.add(p)

                p.sendMessage("${plugin.prefix}§aチャットに検索したいワードを入力してください")

                p.closeInventory()

            }

            if (item.type == Material.SIGN) {

                plugin.searchTitlePlayer.add(p)
                p.sendMessage("${plugin.prefix}§aチャットに検索したいワードを入力してください")

                p.closeInventory()

            }

            if (item.type == Material.FEATHER) {

                plugin.searchAuthorPlayer.add(p)

                p.sendMessage("${plugin.prefix}§aチャットに検索したいワードを入力してください")

                p.closeInventory()

            }

            if (item.type == Material.DIAMOND_HOE) {

                val getFavBook = GetFavBook(p, plugin)
                getFavBook.start()
                try {
                    getFavBook.join(1500) // タイムアウト
                    getFavBook.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                    e.printStackTrace()
                }

                val gp = GuiProcess(plugin)

                p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l検索結果", plugin.searchinvmap))

            }

            if (item.type == Material.OBSERVER) {

                val getDLBook = GetDLBook(p, plugin)
                getDLBook.start()
                try {
                    getDLBook.join(1500) // タイムアウト
                    getDLBook.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                    e.printStackTrace()
                }

                val gp = GuiProcess(plugin)

                p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l検索結果", plugin.searchinvmap))

            }

        }

        if (e.clickedInventory.name == "§6§l検索結果") {

            e.isCancelled = true

            if (!item.hasItemMeta()) {
                return
            }

            if (e.click == ClickType.MIDDLE) {

                if (item.type == Material.WRITTEN_BOOK) {

                    if (!p.hasPermission("amanzon.op")) return

                    val guiProcess = GuiProcess(plugin)
                    p.openInventory(guiProcess.removeBookGui(item))

                }

            } else {

                if (item.itemMeta.displayName == "§l次のページ") {

                    val pageitem = e.clickedInventory.getItem(49)
                    val page = pageitem.itemMeta.displayName.toInt() + 1

                    val gp = GuiProcess(plugin)

                    p.openInventory(gp.allBookGuiCreate(p, page, "§6§l本一覧", plugin.searchinvmap))

                }

                if (item.itemMeta.displayName == "§l前のページ") {

                    val pageitem = e.clickedInventory.getItem(49)
                    val page = pageitem.itemMeta.displayName.toInt() - 1

                    val gp = GuiProcess(plugin)

                    p.openInventory(gp.allBookGuiCreate(p, page, "§6§l本一覧", plugin.searchinvmap))

                }

                if (item.type == Material.WRITTEN_BOOK) {

                    val gp = GetBookPrice(p, item, plugin)
                    gp.start()
                    try {
                        gp.join(1500) // タイムアウト
                        gp.interrupt() // 中断
                    } catch (e: InterruptedException) {
                        // 例外処理
                    }
                    val guiProcess = GuiProcess(plugin)
                    p.openInventory(guiProcess.buyBookGuiCreate(item, plugin.price))

                }

            }
        }

        if (e.clickedInventory.name == "§6§l本メニュー") {

            e.isCancelled = true

            if (!item.hasItemMeta()) {
                return
            }

            if (item.itemMeta.displayName == "§6§lいいね！する") {

                val favProcess = FavProcess(e.clickedInventory.getItem(4), false, p, plugin)
                favProcess.start()
                try {
                    favProcess.join(1500) // タイムアウト
                    favProcess.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }


                val getFav = GetFav(e.clickedInventory.getItem(4), p, plugin)
                getFav.start()
                try {
                    getFav.join(1500) // タイムアウト
                    getFav.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gp = GuiProcess(plugin)
                p.openInventory(gp.bookGui(plugin.favmap[p]!!, e.clickedInventory.getItem(4)))
            }

            if (item.itemMeta.displayName == "§6§l本を読む") {

                val bp = BookProcess()
                bp.openBook(e.clickedInventory.getItem(4), p)

            }

            if (item.itemMeta.displayName == "§6§lいいね！を解除する") {

                val favProcess = FavProcess(e.clickedInventory.getItem(4), true, p, plugin)
                favProcess.start()
                try {
                    favProcess.join(1500) // タイムアウト
                    favProcess.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val getFav = GetFav(e.clickedInventory.getItem(4), p, plugin)
                getFav.start()
                try {
                    getFav.join(1500) // タイムアウト
                    getFav.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gp = GuiProcess(plugin)
                p.openInventory(gp.bookGui(plugin.favmap[p]!!, e.clickedInventory.getItem(4)))

            }

        }

        if (e.clickedInventory.name == "§6§l本を出版する") {
            e.isCancelled = true

            if (item.type == Material.BOOK) {

                val cate = "${item.itemMeta.displayName}"

                val book = e.clickedInventory.getItem(4)

                val bookm = book.itemMeta

                val bookprice = bookm.lore
                val price = bookprice[0].toDouble()

                val booklore = mutableListOf<String>()

                bookm.lore = booklore

                book.itemMeta = bookm

                val publicateBook = PublicateBook(cate, book, price, p, plugin)
                publicateBook.start()

            }

        }

        if (e.clickedInventory.name == "§6§lOP用本一覧") {

            if (item.type != Material.WRITTEN_BOOK) {
                e.isCancelled = true
            }

            if (!item.hasItemMeta()) {
                return
            }

            if (item.itemMeta.displayName == "§l次のページ") {

                val pageitem = e.clickedInventory.getItem(49)
                val page = pageitem.itemMeta.displayName.toInt() + 1

                val gp = GuiProcess(plugin)

                p.openInventory(gp.allBookGuiCreate(p, page, "§6§lOP用本一覧", plugin.openowninvmap))

            }

            if (item.itemMeta.displayName == "§l前のページ") {

                val pageitem = e.clickedInventory.getItem(49)
                val page = pageitem.itemMeta.displayName.toInt() - 1

                val gp = GuiProcess(plugin)

                p.openInventory(gp.allBookGuiCreate(p, page, "§6§lOP用本一覧", plugin.openowninvmap))

            }

        }

        if (e.clickedInventory.name == "§c§l本を削除する"){

            if (!item.hasItemMeta()) {
                return
            }

            if (item.itemMeta.displayName == "§a§l削除"){

                val rb = RemoveBook(e.clickedInventory.getItem(13), p, plugin)
                rb.start()
                try {
                    rb.join(1500) // タイムアウト
                    rb.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gb = GetBook(p, plugin)
                gb.start()
                try {
                    gb.join(1500) // タイムアウト
                    gb.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gp = GuiProcess(plugin)
                p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l本一覧", plugin.openinvmap))

            }

            val cancel = ItemStack(Material.STAINED_GLASS_PANE, 1, 14)
            val cancelm = cancel.itemMeta
            cancelm.displayName = "§c§lキャンセル"
            cancel.itemMeta = cancelm

            if (item == cancel) {

                val gb = GetBook(p, plugin)
                gb.start()
                try {
                    gb.join(1500) // タイムアウト
                    gb.interrupt() // 中断
                } catch (e: InterruptedException) {
                    // 例外処理
                }

                val gp = GuiProcess(plugin)
                p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l本一覧", plugin.openinvmap))

            }

        }

    }



    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {

        val m = e.message
        val p = e.player

        if (plugin.searchTitlePlayer.contains(p)) {

            e.isCancelled = true

            val sb = SearchBook("name", m, p, plugin)
            sb.start()
            try {
                sb.join(1500) // タイムアウト
                sb.interrupt() // 中断
            } catch (e: InterruptedException) {
                // 例外処理
                e.printStackTrace()
            }

            val gp = GuiProcess(plugin)

            p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l検索結果", plugin.searchinvmap))

            plugin.searchTitlePlayer.remove(p)
        }

        if (plugin.searchContentsPlayer.contains(p)) {

            e.isCancelled = true

            val sb = SearchBook("contents", m, p, plugin)
            sb.start()
            try {
                sb.join(1500) // タイムアウト
                sb.interrupt() // 中断
            } catch (e: InterruptedException) {
                // 例外処理
                e.printStackTrace()
            }

            val gp = GuiProcess(plugin)

            p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l検索結果", plugin.searchinvmap))

            plugin.searchContentsPlayer.remove(p)
        }

        if (plugin.searchAuthorPlayer.contains(p)) {

            e.isCancelled = true

            val sb = SearchBook("author_name", m, p, plugin)
            sb.start()
            try {
                sb.join(1500) // タイムアウト
                sb.interrupt() // 中断
            } catch (e: InterruptedException) {
                // 例外処理
                e.printStackTrace()
            }

            val gp = GuiProcess(plugin)

            p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l検索結果", plugin.searchinvmap))

            plugin.searchAuthorPlayer.remove(p)

        }

        var price = 0.0

        if (plugin.PublicatePlayer.contains(p)){

            e.isCancelled = true

            val book = p.inventory.itemInMainHand

            if (book.type != Material.WRITTEN_BOOK){
                p.sendMessage(plugin.prefix + "§c本を持ってください")
                plugin.PublicatePlayer.remove(p)
                return
            }

            val bookmeta = book.itemMeta as BookMeta

            Bukkit.getLogger().info(bookmeta.generation.toString())

            if (bookmeta.generation.toString() != "ORIGINAL"){
                p.sendMessage(plugin.prefix + "§cオリジナルの本しか出版できません")
                plugin.PublicatePlayer.remove(p)
                return
            }

            try {

                price = m.toDouble()

            } catch (vrps: NumberFormatException) {
                p.sendMessage(plugin.prefix + "§c値段を指定してください")
                plugin.PublicatePlayer.remove(p)
                return
            }

            val guiProcess = GuiProcess(plugin)
            p.openInventory(guiProcess.publicateGui(book, price))

            val bookm = book.itemMeta

            val booklore = mutableListOf<String>()

            bookm.lore = booklore

            book.itemMeta = bookm

            plugin.PublicatePlayer.remove(p)

        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //  マインクラフトチャットに、ホバーテキストや、クリックコマンドを設定する関数
    // [例1] sendHoverText(player,"ここをクリック",null,"/say おはまん");
    // [例2] sendHoverText(player,"カーソルをあわせて","ヘルプメッセージとか",null);
    // [例3] sendHoverText(player,"カーソルをあわせてクリック","ヘルプメッセージとか","/say おはまん");
    fun sendHoverText(p: Player, text: String, hoverText: String?, command: String?) {
        //////////////////////////////////////////
        //      ホバーテキストとイベントを作成する
        var hoverEvent: HoverEvent? = null
        if (hoverText != null) {
            val hover = ComponentBuilder(hoverText).create()
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)
        }

        //////////////////////////////////////////
        //   クリックイベントを作成する
        var clickEvent: ClickEvent? = null
        if (command != null) {
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
        }

        val message = ComponentBuilder(text).event(hoverEvent).event(clickEvent).create()
        p.spigot().sendMessage(*message)
    }

}
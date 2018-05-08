package red.man10.amanzonkindle


import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.inventory.meta.BookMeta


class Command(private val plugin: AmanzonKindle) : CommandExecutor {

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {

        if (sender !is Player)return false
        val p: Player = sender

        when(args!!.size){

            0 ->{

                if (!p.hasPermission("amanzon.op"))return true
                p.sendMessage("§e§l-------${plugin.prefix}§e§l-------")
                sender.sendMessage("§6§l/amk register [名前]§f§r:[名前]の人を出版者に登録する")
                sender.sendMessage("§6§l/amk deregister [名前]§f§r:[名前]の人を出版者から削除する")
                sender.sendMessage("§6§l/amk getbookop§f§r:登録されてる本を回収できる")
                sender.sendMessage("§6§l/amk reload§f§r:configをリロードする")
                sender.sendMessage("§6§l本取り下げ方法§f§r:ホイールクリック")
                sender.sendMessage("§b§lcreated by Ryotackey")
                sender.sendMessage("§e§l------------------------------")


            }

            1 ->{

                if (args!![0].equals("reload", ignoreCase = true)){

                    if (!p.hasPermission("amanzon.reload"))return true

                    plugin.config!!.reloadConfig()
                    p.sendMessage("${plugin.prefix}§areload complite")

                }

                if (args!![0].equals("menu", ignoreCase = true)){

                    if (!p.hasPermission("amanzon.menu"))return true

                    val gp = GuiProcess(plugin)
                    p.openInventory(gp.mainMenuCreate())
                }

                if (args!![0].equals("getbalance", ignoreCase = true)){
                    if (!p.hasPermission("amanzon.menu"))return true

                    val gb = GetBalance(p, plugin)
                    gb.start()

                }

                if (args!![0].equals("bookshelf", ignoreCase = true)){

                    Bukkit.getLogger().info("1")

                    val gob = GetOwnBook(p, plugin)
                    gob.start()
                    try {
                        gob.join(1500) // タイムアウト
                        gob.interrupt() // 中断
                    } catch (e: InterruptedException) {
                        // 例外処理
                        e.printStackTrace()
                    }

                    val gp = GuiProcess(plugin)
                    p.openInventory(gp.allBookGuiCreate(p, 1, "§6§l自分の本棚", plugin.openowninvmap))

                }

                if (args!![0].equals("getbookop")){

                    if (!p.hasPermission("amanzon.op"))return true

                    val gb = GetOPBook(p, plugin)
                    gb.start()
                    try {
                        gb.join(1500) // タイムアウト
                        gb.interrupt() // 中断
                    } catch (e: InterruptedException) {
                        // 例外処理
                        e.printStackTrace()
                    }

                    val gp = GuiProcess(plugin)
                    p.openInventory(gp.allBookGuiCreate(p,1, "§6§lOP用本一覧", plugin.openinvmap))

                }

                return true
            }

            2->{

                if (args[0].equals("register", ignoreCase = true)){
                    if (!p.hasPermission("amanzon.register")){
                        return true
                    }

                    val regp: Player = Bukkit.getPlayer(args[1])

                    val ra = RegisterAuthor(p, regp, plugin)
                    ra.start()

                }

                if (args[0].equals("deregister", ignoreCase = true)){
                    if (!p.hasPermission("amanzon.register")){
                        return true
                    }

                    val regp: Player = Bukkit.getPlayer(args[1])

                    val dra = DeregisterAuthor(p, regp, plugin)
                    dra.start()

                }

                if (args!![0].equals("reauthor", ignoreCase = true)){
                    if (!p.hasPermission("amanzon.reauthor")){
                        return true
                    }

                    val book = p.inventory.itemInMainHand
                    val bookMeta: BookMeta = book.itemMeta as BookMeta
                    bookMeta.author = args[1]
                    book.itemMeta = bookMeta

                }

                return true
            }

            3->{

                return true
            }
        }
        return false
    }
}
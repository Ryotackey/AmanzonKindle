package red.man10.amanzonkindle

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import io.netty.buffer.Unpooled
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer


class BookProcess {

    fun openBook(book: ItemStack, p: Player) {
        val slot = p.inventory.heldItemSlot
        val old = p.inventory.getItem(slot)
        p.inventory.setItem(slot, book)

        val buf = Unpooled.buffer(256)
        buf.setByte(0, 0.toByte().toInt())
        buf.writerIndex(1)

        val packet = PacketPlayOutCustomPayload("MC|BOpen", PacketDataSerializer(buf))
        (p as CraftPlayer).handle.playerConnection.sendPacket(packet)
        p.inventory.setItem(slot, old)
    }

}
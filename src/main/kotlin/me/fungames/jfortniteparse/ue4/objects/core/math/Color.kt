package me.fungames.jfortniteparse.ue4.objects.core.math

import me.fungames.jfortniteparse.ue4.UClass
import me.fungames.jfortniteparse.ue4.reader.FArchive
import me.fungames.jfortniteparse.ue4.writer.FArchiveWriter
import java.awt.Color

/**
 * A linear, 32-bit/component floating point RGBA color.
 */
@ExperimentalUnsignedTypes
class FLinearColor : UClass {
    var r: Float
    var g: Float
    var b: Float
    var a: Float

    constructor(Ar: FArchive) {
        super.init(Ar)
        r = Ar.readFloat32()
        g = Ar.readFloat32()
        b = Ar.readFloat32()
        a = Ar.readFloat32()
        super.complete(Ar)
    }

    fun serialize(Ar: FArchiveWriter) {
        super.initWrite(Ar)
        Ar.writeFloat32(r)
        Ar.writeFloat32(g)
        Ar.writeFloat32(b)
        Ar.writeFloat32(a)
        super.completeWrite(Ar)
    }

    fun toColor() = Color(r, g, b, a)

    constructor(r: Float, g: Float, b: Float, a: Float) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }

    override fun toString() = "(R=%f,G=%f,B=%f,A=%f)".format(r, g, b, a)
}

/**
 * Stores a color with 8 bits of precision per channel.
 *
 * Note: Linear color values should always be converted to gamma space before stored in an FColor, as 8 bits of precision is not enough to store linear space colors!
 * This can be done with FLinearColor.toFColor(true)
 */
@ExperimentalUnsignedTypes
class FColor : UClass {
    var r: UByte
    var g: UByte
    var b: UByte
    var a: UByte

    constructor(Ar: FArchive) {
        super.init(Ar)
        r = Ar.readUInt8()
        g = Ar.readUInt8()
        b = Ar.readUInt8()
        a = Ar.readUInt8()
        super.complete(Ar)
    }

    constructor() : this(0u, 0u, 0u, 0u)

    fun toColor() = Color(r.toInt(), g.toInt(), b.toInt(), a.toInt())

    fun serialize(Ar: FArchiveWriter) {
        super.initWrite(Ar)
        Ar.writeUInt8(r)
        Ar.writeUInt8(g)
        Ar.writeUInt8(b)
        Ar.writeUInt8(a)
        super.completeWrite(Ar)
    }

    constructor(r: UByte, g: UByte, b: UByte, a: UByte) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }

    /**
     * Converts this color value to a hexadecimal string.
     *
     * The format of the string is RRGGBBAA.
     *
     * @return Hexadecimal string.
     * @see fromHex
     * @see toString
     */
    fun toHex() = "%02X%02X%02X%02X".format(r, g, b, a)

    /**
     * Converts this color value to a string.
     *
     * @return The string representation.
     * @see toHex
     */
    override fun toString() = "(R=%i,G=%i,B=%i,A=%i)".format(r, g, b, a)
}
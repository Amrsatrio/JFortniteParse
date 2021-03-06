package me.fungames.jfortniteparse.ue4.assets.reader

import me.fungames.jfortniteparse.exceptions.ParserException
import me.fungames.jfortniteparse.fileprovider.FileProvider
import me.fungames.jfortniteparse.ue4.UClass
import me.fungames.jfortniteparse.ue4.assets.Package
import me.fungames.jfortniteparse.ue4.assets.exports.UExport
import me.fungames.jfortniteparse.ue4.assets.util.PayloadType
import me.fungames.jfortniteparse.ue4.objects.uobject.FName
import me.fungames.jfortniteparse.ue4.objects.uobject.FObjectExport
import me.fungames.jfortniteparse.ue4.objects.uobject.FObjectImport
import me.fungames.jfortniteparse.ue4.objects.uobject.FPackageIndex
import me.fungames.jfortniteparse.ue4.reader.FByteArchive
import java.nio.ByteBuffer

/**
 * Binary reader for UE4 Assets
 */
@ExperimentalUnsignedTypes
class FAssetArchive(data: ByteBuffer, private val provider: FileProvider?, val pkgName: String) : FByteArchive(data) {
    constructor(data: ByteArray, provider: FileProvider?, pkgName: String) : this(ByteBuffer.wrap(data), provider, pkgName)

    // Asset Specific Fields
    lateinit var owner: Package
    private val importCache = mutableMapOf<String, Package>()
    private var payloads = mutableMapOf<PayloadType, FAssetArchive>()
    var uassetSize = 0
    var uexpSize = 0

    fun getPayload(type: PayloadType) = payloads[type] ?: throw ParserException("${type.name} is needed to parse the current package")
    fun addPayload(type: PayloadType, payload: FAssetArchive) {
        if (payloads.containsKey(type))
            throw ParserException("Can't add a payload that is already attached of type ${type.name}")
        payloads[type] = payload
    }

    override fun clone(): FAssetArchive {
        val c = FAssetArchive(data, provider, pkgName)
        c.littleEndian = littleEndian
        c.pos = pos
        payloads.forEach { c.payloads[it.key] = it.value }
        c.uassetSize = uassetSize
        c.uexpSize = uexpSize
        return c
    }

    fun seekRelative(pos: Int) {
        seek(pos - uassetSize - uexpSize)
    }

    fun relativePos() = uassetSize + uexpSize + pos()
    fun toNormalPos(relativePos: Int) = relativePos - uassetSize - uexpSize
    fun toRelativePos(normalPos: Int) = normalPos + uassetSize + uexpSize

    fun readFName(): FName {
        val nameIndex = this.readInt32()
        val extraIndex = this.readInt32()
        if (nameIndex in owner.nameMap.indices)
            return FName(owner.nameMap, nameIndex, extraIndex)
        else
            throw ParserException("FName could not be read, requested index $nameIndex, name map size ${owner.nameMap.size}", this)
    }

    fun loadImport(path: String): Package? {
        if (provider == null) return null
        val fixedPath = provider.fixPath(path)
        val cachedPackage = importCache[fixedPath]
        if (cachedPackage != null)
            return cachedPackage
        val pkg = provider.loadGameFile(path)
        return if (pkg != null) {
            importCache[fixedPath] = pkg
            pkg
        } else null
    }

    inline fun <reified T> loadObject(obj: FPackageIndex?): T? {
        if (obj == null) return null
        val loaded = loadObjectGeneric(obj) ?: return null
        return if (loaded is T)
            loaded
        else
            null
    }

    inline fun <reified T> loadImport(import: FObjectImport?): T? {
        if (import == null) return null
        val loaded = loadImportGeneric(import) ?: return null
        return if (loaded is T)
            loaded
        else
            null
    }

    inline fun <reified T> loadExport(export: FObjectExport?): T? {
        if (export == null) return null
        val loaded = loadExportGeneric(export) ?: return null
        return if (loaded is T)
            loaded
        else
            null
    }

    fun loadImportGeneric(import: FObjectImport): UExport? {
        //The needed export is located in another asset, try to load it
        if (provider == null || import.outerIndex.importObject == null) return null
        val fixedPath = provider.fixPath(import.outerIndex.importObject!!.objectName.text)
        val pkg = importCache[fixedPath]
            ?: provider.loadGameFile(fixedPath)?.apply { importCache[fixedPath] = this }
        if (pkg != null) {
            val export = pkg.exports.firstOrNull {
                it.export?.classIndex?.name == import.className.text
                        && it.export?.objectName?.text == import.objectName.text
            }
            if (export != null)
                return export
            else
                UClass.logger.warn { "Couldn't resolve package index in external package" }
        } else {
            UClass.logger.warn { "Failed to load referenced import" }
        }
        return null
    }

    fun loadExportGeneric(export: FObjectExport) = export.exportObject.value

    fun loadObjectGeneric(index: FPackageIndex): UExport? {
        val import = index.importObject
        if (import != null)
            return loadImportGeneric(import)
        val export = index.exportObject
        if (export != null)
            return loadExportGeneric(export)
        return null
    }

    fun clearImportCache() = importCache.clear()

    override fun printError() = "FAssetArchive Info: pos $pos, stopper $size, package $pkgName"
}
package me.fungames.jfortniteparse.ue4.assets.enums

enum class EBulkData(val bulkDataFlags : Int) {
    BULKDATA_PayloadAtEndOfFile(0x0001),           // bulk data stored at the end of this file, data offset added to global data offset in package
    BULKDATA_CompressedZlib(0x0002),               // the same value as for UE3
    BULKDATA_Unused(0x0020),                       // the same value as for UE3
    BULKDATA_ForceInlinePayload(0x0040),           // bulk data stored immediately after header
    BULKDATA_PayloadInSeperateFile(0x0100),        // data stored in .ubulk file near the asset (UE4.12+)
    BULKDATA_SerializeCompressedBitWindow(0x0200), // use platform-specific compression
    BULKDATA_OptionalPayload(0x0800);               // same as BULKDATA_PayloadInSeperateFile, but stored with .uptnl extension (UE4.20+)

    fun check(bulkDataFlags: Int) = (this.bulkDataFlags and bulkDataFlags) != 0
}
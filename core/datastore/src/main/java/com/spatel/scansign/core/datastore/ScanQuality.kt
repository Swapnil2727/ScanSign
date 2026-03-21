package com.spatel.scansign.core.datastore

enum class ScanQuality {
    /** Balanced file size — good for most documents. */
    STANDARD,
    /** Maximum fidelity — larger files, better for fine print. */
    HIGH,
}

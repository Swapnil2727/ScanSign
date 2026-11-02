package com.scansign.app.core.datastore

enum class ScanQuality {
    /** Balanced file size — good for most documents. */
    STANDARD,
    /** Maximum fidelity — larger files, better for fine print. */
    HIGH,
}

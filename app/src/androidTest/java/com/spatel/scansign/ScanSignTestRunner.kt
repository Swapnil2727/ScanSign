package com.spatel.scansign

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class ScanSignTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: Context,
    ): Application = super.newApplication(cl, TestScanSignApplication::class.java.name, context)
}

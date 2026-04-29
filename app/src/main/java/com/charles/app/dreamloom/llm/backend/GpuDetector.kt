package com.charles.app.dreamloom.llm.backend

object GpuDetector {
    /**
     * True only if `libOpenCL.so` is actually loadable in this process.
     *
     * On Android 11+ apps cannot dlopen vendor libraries (like `/vendor/lib64/libOpenCL.so`)
     * unless the manifest declares `<uses-native-library android:name="libOpenCL.so" .../>`.
     * On Pixels in particular the file exists but `dlopen` fails for unprivileged apps,
     * which is exactly the failure LiteRT-LM raises at inference time:
     *   "UNKNOWN: Can not find OpenCL library on this device"
     *
     * Probe once and cache the result.
     */
    private val probed: Boolean by lazy { probe() }

    fun hasOpenCl(): Boolean = probed

    private fun probe(): Boolean {
        // Pixel devices ship a wrapper named libOpenCL-pixel.so; everything else uses libOpenCL.
        for (name in arrayOf("OpenCL-pixel", "OpenCL")) {
            try {
                System.loadLibrary(name)
                return true
            } catch (_: UnsatisfiedLinkError) {
                // try next
            } catch (_: Throwable) {
                // try next
            }
        }
        return false
    }
}

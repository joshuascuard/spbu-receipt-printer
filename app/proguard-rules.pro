# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.spbu.receiptprinter.data.model.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep DantSu ESC/POS library
-keep class com.dantsu.escposprinter.** { *; }

# Keep Apache POI (Excel)
-keep class org.apache.poi.** { *; }

# Keep iText PDF
-keep class com.itextpdf.** { *; }

# Keep ZXing (QR/Barcode)
-keep class com.google.zxing.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Bluetooth classes
-keep class android.bluetooth.** { *; }

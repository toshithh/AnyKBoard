package com.toshith.anykboard

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage

fun generateQr(payload: String, size: Int = 400): ImageBitmap {
    val matrix = QRCodeWriter().encode(
        payload,
        BarcodeFormat.QR_CODE,
        size,
        size
    )

    val img = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until size) {
        for (y in 0 until size) {
            img.setRGB(
                x,
                y,
                if (matrix[x, y]) 0x000000 else 0xFFFFFF
            )
        }
    }
    return img.toComposeImageBitmap()
}

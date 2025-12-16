package com.toshith.anykboard
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.toshith.anykboard.generated.resources.Res
import com.toshith.anykboard.generated.resources.akb_logo
import com.toshith.anykboard.generated.resources.allDrawableResources
import org.jetbrains.compose.resources.painterResource
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.random.Random

fun main() = application {
    fun getLanIp(): String {
        return NetworkInterface.getNetworkInterfaces()
            .toList()
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { addr ->
                !addr.isLoopbackAddress &&
                        !addr.isLinkLocalAddress &&
                        addr.hostAddress.startsWith("192.") ||
                        addr.hostAddress.startsWith("10.") ||
                        addr.hostAddress.startsWith("172.")
            }
            ?.hostAddress
            ?: "127.0.0.1"
    }
    val secret = remember { Random.nextBytes(16).joinToString("") { "%02x".format(it) } }

    var ip by remember { mutableStateOf(TextFieldValue(getLanIp())) }
    var qr by remember {
        mutableStateOf(generateQr(
            """
    {
      "ip": "${ip.text}",
      "port": $PORT,
      "secret": "$secret"
    }
    """.trimIndent())
        )
    }

    LaunchedEffect(Unit) {
        Server(secret).start()
    }

    LaunchedEffect(ip){
        qr = generateQr(
            """
    {
      "ip": "${ip.text}",
      "port": $PORT,
      "secret": "$secret"
    }
    """.trimIndent()
        )
    }

    Window(onCloseRequest = ::exitApplication, title = "AnyKBoard Server", resizable = false, icon = painterResource(Res.drawable.akb_logo)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(11, 30, 45)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = 0.55f)) {
                    item{
                        Text("AnyKBoard Server", fontSize = 24.sp, color = Color.White)
                    }
                    item {
                        Image(painterResource(Res.drawable.akb_logo), "", modifier = Modifier.fillMaxWidth(fraction = 0.6f))
                    }

                    item {
                        TextField(
                            ip,
                            onValueChange = {ip = it},
                            label = {
                                Text("Server IP")
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = Color(255, 165, 0),
                                backgroundColor = Color(255, 255, 255, 50),
                                focusedIndicatorColor = Color(255, 165, 0),
                                unfocusedIndicatorColor = Color(255, 165, 0),
                                focusedLabelColor = Color(255, 255, 255),
                                unfocusedLabelColor = Color(255, 255, 255),

                            )
                        )
                    }

                    item{
                        Text("Download the AnyKBoard App from Play Store or App Store and pair it with this PC using the QR code at right!", textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp), color = Color.White)
                        Text("Be sure to be on the same network!", textAlign = TextAlign.Center, color = Color(255, 165, 0))
                    }
                }

                Column(modifier = Modifier.fillMaxWidth(fraction = 0.45f).fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Server IP: ${ip.text}", textAlign = TextAlign.Center, color = Color.White)
                    Spacer(Modifier.height(16.dp).fillMaxWidth())
                    Image(qr, contentDescription = "QR", modifier = Modifier.fillMaxWidth(fraction = 0.9f).clip(
                        RoundedCornerShape(12.dp)
                    ))
                    Spacer(Modifier.height(16.dp).fillMaxWidth())
                    Text("Scan this QR from your phone", modifier = Modifier.fillMaxWidth(fraction = 0.9f), textAlign = TextAlign.Center, color = Color.White)
                }
            }
        }
    }
}

package com.ediptan01.akilligaleri

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

data class PhotoItem(
    val id: Long,
    val name: String,
    val uri: Uri,
    val category: String
)

class MainActivity : ComponentActivity() {

    private var pendingDelete: List<Uri> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AkilliGaleriApp()
        }
    }

    @Composable
    fun AkilliGaleriApp() {

        var photos by remember { mutableStateOf(emptyList<PhotoItem>()) }
        var selected by remember { mutableStateOf(setOf<Long>()) }
        var category by remember { mutableStateOf("Tümü") }

        val permissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                photos = loadPhotos()
            }

        val deleteLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) {
                selected = emptySet()
                photos = loadPhotos()
            }

        fun scan() {
            val permissions =
                if (Build.VERSION.SDK_INT >= 33) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            val granted = permissions.all {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }

            if (granted) {
                photos = loadPhotos()
            } else {
                permissionLauncher.launch(permissions)
            }
        }

        LaunchedEffect(Unit) {
            scan()
        }

        val filtered = when (category) {
            "Ekran görüntüleri" ->
                photos.filter { it.category == "Ekran görüntüsü" }

            "WhatsApp" ->
                photos.filter { it.category == "WhatsApp" }

            "Telegram" ->
                photos.filter { it.category == "Telegram" }

            else -> photos
        }

        MaterialTheme {

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF8F6FA)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {

                    Text(
                        text = "Akıllı Galeri",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Yapay zekâ destekli galeri temizleme",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { scan() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null
                        )

                        Spacer(Modifier.size(8.dp))

                        Text("Galeriyi yeniden tara")
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE9E4EB)
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Text(
                                "Galeri analizi",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "Toplam fotoğraf: ${photos.size}",
                                fontSize = 18.sp
                            )

                            Text(
                                "Ekran görüntüsü: ${
                                    photos.count {
                                        it.category == "Ekran görüntüsü"
                                    }
                                }",
                                fontSize = 18.sp
                            )

                            Text(
                                "WhatsApp: ${
                                    photos.count {
                                        it.category == "WhatsApp"
                                    }
                                }",
                                fontSize = 18.sp
                            )

                            Text(
                                "Telegram: ${
                                    photos.count {
                                        it.category == "Telegram"
                                    }
                                }",
                                fontSize = 18.sp
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                "Fotoğraflarınız izniniz olmadan silinmez.",
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        FilterChip(
                            selected = category == "Tümü",
                            onClick = { category = "Tümü" },
                            label = { Text("Tümü") }
                        )

                        FilterChip(
                            selected = category == "Ekran görüntüleri",
                            onClick = {
                                category = "Ekran görüntüleri"
                            },
                            label = {
                                Text("Ekran görüntüleri")
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        FilterChip(
                            selected = category == "WhatsApp",
                            onClick = {
                                category = "WhatsApp"
                            },
                            label = { Text("WhatsApp") }
                        )

                        FilterChip(
                            selected = category == "Telegram",
                            onClick = {
                                category = "Telegram"
                            },
                            label = { Text("Telegram") }
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    if (selected.isNotEmpty()) {

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    "${selected.size} fotoğraf seçildi",
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {

                                        val deleteList =
                                            photos.filter {
                                                selected.contains(it.id)
                                            }

                                        requestDelete(
                                            deleteList.map { it.uri },
                                            deleteLauncher
                                        )
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null
                                    )

                                    Spacer(Modifier.size(5.dp))

                                    Text("Sil")
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        items(
                            filtered,
                            key = { it.id }
                        ) { photo ->

                            PhotoCard(
                                photo = photo,
                                selected =
                                    selected.contains(photo.id),
                                onClick = {

                                    selected =
                                        if (selected.contains(photo.id)) {
                                            selected - photo.id
                                        } else {
                                            selected + photo.id
                                        }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadPhotos(): List<PhotoItem> {

        val result = mutableListOf<PhotoItem>()

        val collection =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )

        val sort =
            "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            collection,
            projection,
            null,
            null,
            sort
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media._ID
                )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DISPLAY_NAME
                )

            val dataColumn =
                cursor.getColumnIndex(
                    MediaStore.Images.Media.DATA
                )

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)

                val name =
                    cursor.getString(nameColumn) ?: "Fotoğraf"

                val path =
                    if (dataColumn >= 0)
                        cursor.getString(dataColumn)
                    else
                        ""

                val lower =
                    (name + " " + path).lowercase()

                val category =
                    when {
                        lower.contains("screenshot") ||
                        lower.contains("screen_shot") ||
                        lower.contains("ekran görüntüsü") ||
                        lower.contains("screenshots") ->
                            "Ekran görüntüsü"

                        lower.contains("whatsapp") ->
                            "WhatsApp"

                        lower.contains("telegram") ->
                            "Telegram"

                        else ->
                            "Fotoğraf"
                    }

                result.add(
                    PhotoItem(
                        id = id,
                        name = name,
                        uri = ContentUris.withAppendedId(
                            collection,
                            id
                        ),
                        category = category
                    )
                )
            }
        }

        return result
    }

    private fun requestDelete(
        uris: List<Uri>,
        launcher:
        androidx.activity.result.ActivityResultLauncher<
            IntentSenderRequest
            >
    ) {

        if (uris.isEmpty()) return

        if (Build.VERSION.SDK_INT >= 30) {

            val request =
                MediaStore.createDeleteRequest(
                    contentResolver,
                    uris
                )

            launcher.launch(
                IntentSenderRequest.Builder(
                    request.intentSender
                ).build()
            )

        } else {

            uris.forEach { uri ->
                contentResolver.delete(
                    uri,
                    null,
                    null
                )
            }
        }
    }

    @Composable
    private fun PhotoCard(
        photo: PhotoItem,
        selected: Boolean,
        onClick: () -> Unit
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    if (selected)
                        Color(0xFFDCD0F2)
                    else
                        Color.White
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Color(0xFFE8E1EE),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            if (photo.category ==
                                "Ekran görüntüsü")
                                Icons.Default.Screenshot
                            else
                                Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = photo.name,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = photo.category,
                        color = Color.Gray
                    )
                }

                if (selected) {
                    Text(
                        "✓",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

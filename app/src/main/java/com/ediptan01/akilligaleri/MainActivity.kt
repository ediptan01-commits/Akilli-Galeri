package com.ediptan01.akilligaleri

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

data class PhotoItem(
    val id: Long,
    val name: String,
    val uri: android.net.Uri,
    val category: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AkilliGaleriApp()
            }
        }
    }

    private fun readPhotos(): List<PhotoItem> {

        val result = mutableListOf<PhotoItem>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            val pathColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)

                val name =
                    cursor.getString(nameColumn) ?: ""

                val path =
                    cursor.getString(pathColumn) ?: ""

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val searchText =
                    (name + " " + path).lowercase()

                val category = when {

                    searchText.contains("screenshot") ||
                    searchText.contains("screen_shot") ||
                    searchText.contains("ekran") ->
                        "Ekran görüntüsü"

                    searchText.contains("whatsapp") ->
                        "WhatsApp"

                    searchText.contains("telegram") ->
                        "Telegram"

                    else ->
                        "Diğer"
                }

                result.add(
                    PhotoItem(
                        id = id,
                        name = name,
                        uri = uri,
                        category = category
                    )
                )
            }
        }

        return result
    }

    @Composable
    private fun AkilliGaleriApp() {

        var photos by remember {
            mutableStateOf(emptyList<PhotoItem>())
        }

        var scanned by remember {
            mutableStateOf(false)
        }

        var selectedCategory by remember {
            mutableStateOf("Tümü")
        }

        val permission =
            if (Build.VERSION.SDK_INT >= 33) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        val permissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->

                if (granted) {
                    photos = readPhotos()
                    scanned = true
                }
            }

        Scaffold(

            topBar = {

                TopAppBar(
                    title = {
                        Text("Akıllı Galeri")
                    }
                )
            }

        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {

                Text(
                    text = "Yapay zekâ destekli galeri temizleme",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),

                    onClick = {

                        val granted =
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED

                        if (granted) {

                            photos = readPhotos()
                            scanned = true

                        } else {

                            permissionLauncher.launch(permission)
                        }
                    }

                ) {

                    Text(
                        if (scanned)
                            "Galeriyi yeniden tara"
                        else
                            "Galeriyi tara"
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                if (scanned) {

                    val screenshots =
                        photos.count {
                            it.category == "Ekran görüntüsü"
                        }

                    val whatsapp =
                        photos.count {
                            it.category == "WhatsApp"
                        }

                    val telegram =
                        photos.count {
                            it.category == "Telegram"
                        }

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                "Galeri analizi",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                "Toplam fotoğraf: ${photos.size}"
                            )

                            Text(
                                "Ekran görüntüsü: $screenshots"
                            )

                            Text(
                                "WhatsApp: $whatsapp"
                            )

                            Text(
                                "Telegram: $telegram"
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                "Fotoğraflarınız izniniz olmadan silinmez."
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        OutlinedButton(
                            onClick = {
                                selectedCategory = "Tümü"
                            }
                        ) {
                            Text("Tümü")
                        }

                        OutlinedButton(
                            onClick = {
                                selectedCategory =
                                    "Ekran görüntüsü"
                            }
                        ) {
                            Text("Ekran görüntüleri")
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    val visiblePhotos =
                        if (selectedCategory == "Tümü") {
                            photos
                        } else {
                            photos.filter {
                                it.category ==
                                    selectedCategory
                            }
                        }

                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        items(
                            visiblePhotos.take(100)
                        ) { photo ->

                            Card(
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Column(
                                    modifier =
                                        Modifier.padding(12.dp)
                                ) {

                                    Text(
                                        photo.name,
                                        maxLines = 1
                                    )

                                    Text(
                                        photo.category
                                    )
                                }
                            }
                        }
                    }

                } else {

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Text(
                        "Başlamak için \"Galeriyi tara\" " +
                        "butonuna basın."
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        "Akıllı Galeri önce fotoğrafları " +
                        "analiz edecek. Silme işlemi " +
                        "kullanıcı onayı olmadan yapılmayacak."
                    )
                }
            }
        }
    }
}

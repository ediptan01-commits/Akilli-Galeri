package com.ediptan01.akilligaleri

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class PhotoItem(
    val id: Long,
    val name: String,
    val uri: android.net.Uri,
    val category: String,
    val sizeBytes: Long
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
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.SIZE
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

            val sizeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: ""
                val path = cursor.getString(pathColumn) ?: ""
                val size = cursor.getLong(sizeColumn)

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val searchText =
                    (name + " " + path).lowercase(Locale.getDefault())

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
                        "Fotoğraflar"
                }

                result.add(
                    PhotoItem(
                        id = id,
                        name = name,
                        uri = uri,
                        category = category,
                        sizeBytes = size
                    )
                )
            }
        }

        return result
    }

    @OptIn(ExperimentalMaterial3Api::class)
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

        var isScanning by remember {
            mutableStateOf(false)
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

        fun scanGallery() {

            val granted =
                ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    permission
                ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                permissionLauncher.launch(permission)
                return
            }

            isScanning = true

            photos = readPhotos()

            scanned = true

            isScanning = false
        }

        Scaffold(

            containerColor =
                MaterialTheme.colorScheme.background,

            topBar = {

                TopAppBar(

                    title = {

                        Column {

                            Text(
                                "Akıllı Galeri",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Galerini akıllıca düzenle",
                                fontSize = 12.sp,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    },

                    actions = {

                        IconButton(
                            onClick = {
                                scanGallery()
                            }
                        ) {

                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Yenile"
                            )
                        }
                    },

                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor =
                                MaterialTheme.colorScheme.background
                        )
                )
            }

        ) { padding ->

            if (!scanned) {

                WelcomeScreen(
                    padding = padding,
                    onScan = {
                        scanGallery()
                    }
                )

            } else {

                GalleryDashboard(
                    padding = padding,
                    photos = photos,
                    selectedCategory = selectedCategory,
                    onCategorySelected = {
                        selectedCategory = it
                    },
                    onScan = {
                        scanGallery()
                    },
                    isScanning = isScanning
                )
            }
        }
    }

    @Composable
    private fun WelcomeScreen(
        padding: PaddingValues,
        onScan: () -> Unit
    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {

            Spacer(
                Modifier.height(48.dp)
            )

            Surface(

                modifier =
                    Modifier.size(92.dp),

                shape =
                    RoundedCornerShape(28.dp),

                color =
                    MaterialTheme.colorScheme
                        .primaryContainer

            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        Icons.Default.AutoAwesome,

                        contentDescription = null,

                        modifier =
                            Modifier.size(46.dp),

                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(
                Modifier.height(24.dp)
            )

            Text(

                "Galerini\nakıllıca temizle.",

                style =
                    MaterialTheme.typography.headlineLarge,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(12.dp)
            )

            Text(

                "Ekran görüntülerini, WhatsApp fotoğraflarını ve diğer görselleri tek ekranda keşfet.",

                style =
                    MaterialTheme.typography.bodyLarge,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(28.dp)
            )

            Button(

                onClick = onScan,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                shape =
                    RoundedCornerShape(18.dp)

            ) {

                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(10.dp)
                )

                Text(
                    "Galeriyi taramaya başla",
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                Modifier.height(18.dp)
            )

            Text(

                "Fotoğraflarınız izniniz olmadan silinmez.",

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,

                fontSize = 13.sp
            )
        }
    }

    @Composable
    private fun GalleryDashboard(

        padding: PaddingValues,

        photos: List<PhotoItem>,

        selectedCategory: String,

        onCategorySelected:
            (String) -> Unit,

        onScan: () -> Unit,

        isScanning: Boolean

    ) {

        val screenshots =
            photos.count {
                it.category ==
                    "Ekran görüntüsü"
            }

        val whatsapp =
            photos.count {
                it.category ==
                    "WhatsApp"
            }

        val telegram =
            photos.count {
                it.category ==
                    "Telegram"
            }

        val screenshotSize =
            photos
                .filter {
                    it.category ==
                        "Ekran görüntüsü"
                }
                .sumOf {
                    it.sizeBytes
                }

        val whatsappSize =
            photos
                .filter {
                    it.category ==
                        "WhatsApp"
                }
                .sumOf {
                    it.sizeBytes
                }

        val visiblePhotos =
            when (selectedCategory) {

                "Ekran görüntüsü" ->
                    photos.filter {
                        it.category ==
                            "Ekran görüntüsü"
                    }

                "WhatsApp" ->
                    photos.filter {
                        it.category ==
                            "WhatsApp"
                    }

                "Telegram" ->
                    photos.filter {
                        it.category ==
                            "Telegram"
                    }

                else ->
                    photos
            }

        LazyColumn(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),

            contentPadding =
                PaddingValues(
                    horizontal = 20.dp,
                    vertical = 12.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)

        ) {

            item {

                HeroCard(

                    total =
                        photos.size,

                    screenshotSize =
                        screenshotSize,

                    onScan =
                        onScan,

                    isScanning =
                        isScanning
                )
            }

            item {

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)

                ) {

                    StatCard(

                        modifier =
                            Modifier.weight(1f),

                        icon =
                            Icons.Default.Screenshot,

                        value =
                            screenshots.toString(),

                        label =
                            "Ekran görüntüsü"
                    )

                    StatCard(

                        modifier =
                            Modifier.weight(1f),

                        icon =
                            Icons.Default.Storage,

                        value =
                            formatSize(
                                screenshotSize
                            ),

                        label =
                            "Kapladığı alan"
                    )
                }
            }

            item {

                Text(

                    "Temizlik alanları",

                    style =
                        MaterialTheme.typography.titleLarge,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            item {

                CategoryCard(

                    title =
                        "Ekran görüntüleri",

                    subtitle =
                        screenshots.toString() +
                        " fotoğraf • " +
                        formatSize(
                            screenshotSize
                        ),

                    icon =
                        Icons.Default.Screenshot,

                    onClick = {
                        onCategorySelected(
                            "Ekran görüntüsü"
                        )
                    }
                )
            }

            item {

                CategoryCard(

                    title =
                        "WhatsApp",

                    subtitle =
                        whatsapp.toString() +
                        " fotoğraf • " +
                        formatSize(
                            whatsappSize
                        ),

                    icon =
                        Icons.Default.Image,

                    onClick = {
                        onCategorySelected(
                            "WhatsApp"
                        )
                    }
                )
            }

            item {

                CategoryCard(

                    title =
                        "Telegram",

                    subtitle =
                        telegram.toString() +
                        " fotoğraf",

                    icon =
                        Icons.Default.Image,

                    onClick = {
                        onCategorySelected(
                            "Telegram"
                        )
                    }
                )
            }

            item {

                Text(

                    "Fotoğraflar",

                    style =
                        MaterialTheme.typography.titleLarge,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            item {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    listOf(
                        "Tümü",
                        "Ekran görüntüsü",
                        "WhatsApp",
                        "Telegram"
                    ).forEach { category ->

                        FilterChip(

                            selected =
                                selectedCategory ==
                                    category,

                            onClick = {
                                onCategorySelected(
                                    category
                                )
                            },

                            label = {

                                Text(

                                    if (
                                        category ==
                                        "Ekran görüntüsü"
                                    ) {
                                        "Ekran"
                                    } else {
                                        category
                                    }
                                )
    
                            }
                        )
                    }
                }
            }
        }
    }
}

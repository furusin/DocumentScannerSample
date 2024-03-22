package com.example.documentscannersample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.documentscannersample.ui.theme.DocumentScannerSampleTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream

private const val TAG = "@@@"

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(4)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        val scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                if (activityResult.resultCode == RESULT_OK) {
                    val scanResult =
                        GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
                            ?: return@registerForActivityResult

                    scanResult.pages?.let { pages ->
                        val imageUris = pages.map { it.imageUri }.toList()
                        viewModel.setImageUriList(imageUris)
                        Log.d(TAG, "imageUris: ${imageUris}")
                        Log.d(TAG, "onCreate: scanResult image")
                    }

                    scanResult.pdf?.let { pdf ->
                        val pdfCount = pdf.pageCount

                        viewModel.setPdfUri(pdf.uri.toString())
                        Log.d(TAG, "onCreate: scanResult PDF ${pdf.uri}")

                        val outputStream = FileOutputStream(File(filesDir, "scan.pdf"))
                        contentResolver.openInputStream(pdf.uri)?.let { inputStream ->
                            inputStream.copyTo(outputStream)
                            inputStream.close()
                        }
                    }
                }
            }

        scanner.getStartScanIntent(this).addOnSuccessListener { intentSender ->
            scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }.addOnFailureListener {
            Toast.makeText(this, "failed launch Scanner ${it.message}", Toast.LENGTH_LONG).show()
            Log.d(TAG, "onCreate: ${it.message}")
        }
        setContent {
            DocumentScannerSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(viewModel, "Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(viewModel: MainViewModel, name: String, modifier: Modifier = Modifier) {
    val pdfUri by viewModel.pdfUri.collectAsState()
    val imageUri by viewModel.imageUriList.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$pdfUri",
            modifier = modifier
        )
        imageUri.forEach { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
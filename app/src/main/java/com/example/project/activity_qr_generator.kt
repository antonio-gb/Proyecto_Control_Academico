package com.example.project // Define el paquete al que pertenece este archivo

import android.graphics.Bitmap // Importa Bitmap para manejar imágenes de mapa de bits
import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.ImageView // Importa ImageView para mostrar imágenes en la interfaz
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.zxing.BarcodeFormat // Importa BarcodeFormat para especificar el tipo de código de barras
import com.journeyapps.barcodescanner.BarcodeEncoder // Importa BarcodeEncoder para generar códigos de barras como imágenes

class QRGeneratorActivity : AppCompatActivity() { // Declara la actividad que genera el código QR del alumno

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_qr_generator) // Establece el layout XML de esta actividad

        val uid = intent.getStringExtra("uid") ?: "" // Obtiene el UID del alumno pasado por el intent
        val ivQR = findViewById<ImageView>(R.id.ivQRCode) // Obtiene la referencia al ImageView donde se mostrará el QR

        try { // Bloque try para manejar posibles excepciones al generar el QR
            val encoder = BarcodeEncoder() // Crea una instancia del codificador de códigos de barras
            val bitmap: Bitmap = encoder.encodeBitmap("Asistencia: $uid", BarcodeFormat.QR_CODE, 600, 600) // Genera el QR con el UID del alumno, formato QR_CODE y tamaño 600x600 píxeles
            ivQR.setImageBitmap(bitmap) // Muestra el QR generado en el ImageView
        } catch (e: Exception) { // Captura cualquier excepción que pueda ocurrir durante la generación
            e.printStackTrace() // Imprime el rastro de la excepción en el log para depuración
        }
    }
}
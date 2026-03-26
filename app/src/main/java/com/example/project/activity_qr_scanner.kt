package com.example.project // Define el paquete al que pertenece este archivo

import android.content.SharedPreferences // Importa SharedPreferences para acceder a los datos de sesión
import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.ArrayAdapter // Importa ArrayAdapter para vincular listas de datos con vistas
import android.widget.Button // Importa Button para los botones de la interfaz
import android.widget.Spinner // Importa Spinner para el selector desplegable de materias
import android.widget.TextView // Importa TextView para mostrar texto en la interfaz
import android.widget.Toast // Importa Toast para mostrar mensajes breves al usuario
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore para la base de datos en la nube
import com.journeyapps.barcodescanner.ScanContract // Importa ScanContract para el contrato de escaneo de QR
import com.journeyapps.barcodescanner.ScanOptions // Importa ScanOptions para configurar el escáner QR

class QRScannerActivity : AppCompatActivity() { // Declara la actividad del escáner de códigos QR para el profesor

    private lateinit var db: FirebaseFirestore // Variable para la instancia de Firestore
    private lateinit var prefs: SharedPreferences // Variable para las preferencias compartidas de la sesión
    private val subjectList = mutableListOf<String>() // Lista mutable con los nombres de las materias del profesor
    private val subjectIds = mutableListOf<String>() // Lista mutable con los IDs de las materias del profesor

    private val scanLauncher = registerForActivityResult(ScanContract()) { result -> // Registra el launcher para recibir el resultado del escaneo QR
        if (result.contents != null) { // Verifica que el escaneo haya retornado contenido
            val scannedUid = result.contents.removePrefix("Asistencia:") // Extrae el UID del alumno del contenido escaneado quitando el prefijo
            val position = findViewById<Spinner>(R.id.spinnerSubject).selectedItemPosition // Obtiene la posición de la materia seleccionada en el spinner
            if (subjectIds.isEmpty()) { // Verifica que haya materias cargadas antes de registrar asistencia
                Toast.makeText(this, "Selecciona una materia", Toast.LENGTH_SHORT).show() // Muestra mensaje de error si no hay materias
                return@registerForActivityResult // Sale del callback sin registrar asistencia
            }
            val subjectId = subjectIds[position] // Obtiene el ID de la materia seleccionada
            registerAttendance(scannedUid, subjectId) // Registra la asistencia del alumno en la materia
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_qr_scanner) // Establece el layout XML de esta actividad

        db = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore
        prefs = getSharedPreferences("session", MODE_PRIVATE) // Obtiene las preferencias compartidas con clave "session"

        val uid = prefs.getString("uid", "") ?: "" // Obtiene el UID del profesor actualmente autenticado
        val spinner = findViewById<Spinner>(R.id.spinnerSubject) // Obtiene la referencia al spinner de selección de materia
        val btnScan = findViewById<Button>(R.id.btnScan) // Obtiene la referencia al botón para iniciar el escaneo

        // Cargar materias del profesor
        db.collection("subjects").whereEqualTo("teacherId", uid).get() // Consulta las materias asignadas al profesor actual
            .addOnSuccessListener { result -> // Callback ejecutado si la consulta es exitosa
                for (doc in result) { // Itera sobre cada materia encontrada
                    subjectList.add(doc.getString("name") ?: "") // Agrega el nombre de la materia a la lista
                    subjectIds.add(doc.id) // Agrega el ID de la materia a la lista
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, subjectList) // Crea el adaptador para el spinner con los nombres de materias
                spinner.adapter = adapter // Asigna el adaptador al spinner
            }

        btnScan.setOnClickListener { // Configura el listener del botón de escaneo
            val options = ScanOptions() // Crea las opciones de configuración del escáner
            options.setPrompt("Escanea el QR del alumno") // Establece el mensaje que se muestra durante el escaneo
            options.setBeepEnabled(true) // Habilita el sonido de confirmación al escanear
            options.setOrientationLocked(false) // Permite que el escáner rote con la orientación del dispositivo
            scanLauncher.launch(options) // Inicia la actividad de escaneo con las opciones configuradas
        }
    }

    private fun registerAttendance(studentUid: String, subjectId: String) { // Función que registra la asistencia del alumno en Firestore
        val tvResult = findViewById<TextView>(R.id.tvResult) // Obtiene la referencia al TextView para mostrar el resultado

        // Verificar que el alumno está inscrito en la materia
        db.collection("subjects").document(subjectId).get() // Consulta el documento de la materia en Firestore
            .addOnSuccessListener { doc -> // Callback ejecutado si la consulta es exitosa
                val students = doc.get("students") as? List<*> ?: emptyList<String>() // Obtiene la lista de alumnos inscritos; lista vacía si no existe
                if (!students.contains(studentUid)) { // Verifica si el alumno escaneado está en la lista de inscritos
                    tvResult.text = "Alumno no inscrito en esta materia" // Muestra mensaje indicando que el alumno no está inscrito
                    return@addOnSuccessListener // Sale del callback sin registrar asistencia
                }

                // Guardar asistencia
                val attendance = hashMapOf( // Crea un mapa con los datos de la asistencia
                    "studentId" to studentUid, // Campo UID del alumno que asistió
                    "subjectId" to subjectId, // Campo ID de la materia donde se registra la asistencia
                    "date" to com.google.firebase.Timestamp.now() // Campo fecha y hora actual del registro de asistencia
                )
                db.collection("attendance").add(attendance) // Agrega el registro de asistencia a la colección "attendance" en Firestore
                    .addOnSuccessListener { // Callback ejecutado si el guardado es exitoso
                        tvResult.text = "Asistencia registrada" // Muestra mensaje de confirmación en el TextView
                        Toast.makeText(this, "Asistencia guardada", Toast.LENGTH_SHORT).show() // Muestra mensaje de confirmación como Toast
                    }
                    .addOnFailureListener { // Callback ejecutado si ocurre un error al guardar
                        tvResult.text = "Error al registrar" // Muestra mensaje de error en el TextView
                    }
            }
    }
}
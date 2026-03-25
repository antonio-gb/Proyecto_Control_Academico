package com.example.project

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QRScannerActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences
    private val subjectList = mutableListOf<String>()
    private val subjectIds = mutableListOf<String>()

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val scannedUid = result.contents.removePrefix("Asistencia:")
            val position = findViewById<Spinner>(R.id.spinnerSubject).selectedItemPosition
            if (subjectIds.isEmpty()) {
                Toast.makeText(this, "Selecciona una materia", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val subjectId = subjectIds[position]
            registerAttendance(scannedUid, subjectId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("session", MODE_PRIVATE)

        val uid = prefs.getString("uid", "") ?: ""
        val spinner = findViewById<Spinner>(R.id.spinnerSubject)
        val btnScan = findViewById<Button>(R.id.btnScan)

        // Cargar materias del profesor
        db.collection("subjects").whereEqualTo("teacherId", uid).get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    subjectList.add(doc.getString("name") ?: "")
                    subjectIds.add(doc.id)
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, subjectList)
                spinner.adapter = adapter
            }

        btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setPrompt("Escanea el QR del alumno")
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            scanLauncher.launch(options)
        }
    }

    private fun registerAttendance(studentUid: String, subjectId: String) {
        val tvResult = findViewById<TextView>(R.id.tvResult)

        // Verificar que el alumno está inscrito en la materia
        db.collection("subjects").document(subjectId).get()
            .addOnSuccessListener { doc ->
                val students = doc.get("students") as? List<*> ?: emptyList<String>()
                if (!students.contains(studentUid)) {
                    tvResult.text = "Alumno no inscrito en esta materia"
                    return@addOnSuccessListener
                }

                // Guardar asistencia
                val attendance = hashMapOf(
                    "studentId" to studentUid,
                    "subjectId" to subjectId,
                    "date" to com.google.firebase.Timestamp.now()
                )
                db.collection("attendance").add(attendance)
                    .addOnSuccessListener {
                        tvResult.text = "Asistencia registrada"
                        Toast.makeText(this, "Asistencia guardada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        tvResult.text = "Error al registrar"
                    }
            }
    }
}
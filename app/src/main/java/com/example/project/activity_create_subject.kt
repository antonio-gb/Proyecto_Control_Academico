package com.example.project

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CreateSubjectActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val teacherNames = mutableListOf<String>()
    private val teacherIds = mutableListOf<String>()
    private val studentNames = mutableListOf<String>()
    private val studentIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_subject)

        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etSubjectName)
        val etSchedule = findViewById<EditText>(R.id.etSchedule)
        val spinnerTeacher = findViewById<Spinner>(R.id.spinnerTeacher)
        val lvStudents = findViewById<ListView>(R.id.lvStudents)
        val btnSave = findViewById<Button>(R.id.btnSaveSubject)

        db.collection("users").whereEqualTo("role", "profesor").get()
            .addOnSuccessListener { result ->
                teacherNames.clear()
                teacherIds.clear()
                for (doc in result) {
                    teacherNames.add(doc.getString("name") ?: "Sin nombre")
                    teacherIds.add(doc.id)
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, teacherNames)
                spinnerTeacher.adapter = adapter
            }

        db.collection("users").whereEqualTo("role", "alumno").get()
            .addOnSuccessListener { result ->
                studentNames.clear()
                studentIds.clear()
                for (doc in result) {
                    studentNames.add(doc.getString("name") ?: "Sin nombre")
                    studentIds.add(doc.id)
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, studentNames)
                lvStudents.adapter = adapter
                lvStudents.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val schedule = etSchedule.text.toString().trim()

            if (name.isEmpty() || schedule.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (teacherIds.isEmpty()) {
                Toast.makeText(this, "No hay profesores registrados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val teacherId = teacherIds[spinnerTeacher.selectedItemPosition]

            val selectedStudents = mutableListOf<String>()
            for (i in 0 until lvStudents.count) {
                if (lvStudents.isItemChecked(i)) {
                    selectedStudents.add(studentIds[i])
                }
            }

            val subject = hashMapOf(
                "name" to name,
                "schedule" to schedule,
                "teacherId" to teacherId,
                "students" to selectedStudents
            )

            db.collection("subjects").add(subject)
                .addOnSuccessListener {
                    Toast.makeText(this, "Materia creada", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
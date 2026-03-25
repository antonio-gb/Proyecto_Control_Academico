package com.example.project

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class GradesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val studentNames = mutableListOf<String>()
    private val studentIds = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grades_p)

        db = FirebaseFirestore.getInstance()

        val subjectId = intent.getStringExtra("subjectId") ?: ""
        val subjectName = intent.getStringExtra("subjectName") ?: ""

        findViewById<TextView>(R.id.tvSubjectName).text = subjectName

        val lvStudents = findViewById<ListView>(R.id.lvStudents)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studentNames)
        lvStudents.adapter = adapter

        loadStudents(subjectId)

        lvStudents.setOnItemClickListener { _, _, position, _ ->
            showGradeDialog(studentIds[position], studentNames[position], subjectId)
        }
    }

    private fun loadStudents(subjectId: String) {
        db.collection("subjects").document(subjectId).get()
            .addOnSuccessListener { doc ->
                val students = doc.get("students") as? List<*> ?: emptyList<String>()
                studentNames.clear()
                studentIds.clear()
                for (uid in students) {
                    db.collection("users").document(uid.toString()).get()
                        .addOnSuccessListener { userDoc ->
                            studentNames.add(userDoc.getString("name") ?: "Sin nombre")
                            studentIds.add(uid.toString())
                            adapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun showGradeDialog(studentId: String, studentName: String, subjectId: String) {
        val input = EditText(this)
        input.hint = "Calificación (0-100)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Calificar a $studentName")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val grade = input.text.toString().trim()
                if (grade.isEmpty()) return@setPositiveButton

                val gradeData = hashMapOf(
                    "studentId" to studentId,
                    "subjectId" to subjectId,
                    "grade" to grade.toInt()
                )
                db.collection("grades").add(gradeData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Calificación guardada", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
package com.example.project

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class StudentGradesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val gradeList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grades)

        db = FirebaseFirestore.getInstance()

        val subjectId = intent.getStringExtra("subjectId") ?: ""
        val subjectName = intent.getStringExtra("subjectName") ?: ""
        val uid = getSharedPreferences("session", MODE_PRIVATE).getString("uid", "") ?: ""

        findViewById<TextView>(R.id.tvSubjectName).text = subjectName

        val lvGrades = findViewById<ListView>(R.id.lvGrades)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, gradeList)
        lvGrades.adapter = adapter

        db.collection("grades")
            .whereEqualTo("studentId", uid)
            .whereEqualTo("subjectId", subjectId)
            .get()
            .addOnSuccessListener { result ->
                gradeList.clear()
                if (result.isEmpty) {
                    gradeList.add("Sin calificaciones aún")
                } else {
                    for (doc in result) {
                        val grade = doc.getLong("grade") ?: 0
                        gradeList.add("Calificación: $grade")
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando calificaciones", Toast.LENGTH_SHORT).show()
            }
    }
}
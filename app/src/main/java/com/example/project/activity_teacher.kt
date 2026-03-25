package com.example.project

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class TeacherActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences
    private val subjectList = mutableListOf<String>()
    private val subjectIds = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("session", MODE_PRIVATE)

        val tvName = findViewById<TextView>(R.id.tvTeacherName)
        val lvSubjects = findViewById<ListView>(R.id.lvSubjects)
        val btnScanQR = findViewById<Button>(R.id.btnScanQR)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val uid = prefs.getString("uid", "") ?: ""

        // Cargar nombre del profesor
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                tvName.text = "Hola, ${doc.getString("name")}"
            }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, subjectList)
        lvSubjects.adapter = adapter

        loadSubjects(uid)

        // Al tocar una materia, ir a calificaciones
        lvSubjects.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, GradesActivity::class.java)
            intent.putExtra("subjectId", subjectIds[position])
            intent.putExtra("subjectName", subjectList[position])
            startActivity(intent)
        }

        btnScanQR.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }

        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadSubjects(uid: String) {
        db.collection("subjects")
            .whereEqualTo("teacherId", uid)
            .get()
            .addOnSuccessListener { result ->
                subjectList.clear()
                subjectIds.clear()
                for (doc in result) {
                    subjectList.add(doc.getString("name") ?: "Sin nombre")
                    subjectIds.add(doc.id)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando materias", Toast.LENGTH_SHORT).show()
            }
    }
}
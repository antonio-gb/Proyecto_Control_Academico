package com.example.project

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences
    private val userList = mutableListOf<String>()      // nombres para mostrar
    private val userIds = mutableListOf<String>()       // UIDs correspondientes
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("session", MODE_PRIVATE)

        val lvUsers = findViewById<ListView>(R.id.lvUsers)
        val btnCreateSubject = findViewById<Button>(R.id.btnCreateSubject)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userList)
        lvUsers.adapter = adapter

        loadUsers()

        // Al tocar un usuario, mostrar opciones de rol
        lvUsers.setOnItemClickListener { _, _, position, _ ->
            showRoleDialog(userIds[position], userList[position])
        }

        btnCreateSubject.setOnClickListener {
            startActivity(Intent(this, CreateSubjectActivity::class.java))
        }

        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadUsers() {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                userList.clear()
                userIds.clear()
                for (doc in result) {
                    val name = doc.getString("name") ?: "Sin nombre"
                    val role = doc.getString("role") ?: "alumno"
                    userList.add("$name ($role)")
                    userIds.add(doc.id)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando usuarios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showRoleDialog(uid: String, userName: String) {
        val view = layoutInflater.inflate(R.layout.activity_change_role, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<android.widget.TextView>(R.id.tvUserName).text = userName

        view.findViewById<Button>(R.id.btnSetAlumno).setOnClickListener {
            updateRole(uid, "alumno")
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnSetProfesor).setOnClickListener {
            updateRole(uid, "profesor")
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnSetAdmin).setOnClickListener {
            updateRole(uid, "admin")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateRole(uid: String, newRole: String) {
        db.collection("users").document(uid).update("role", newRole)
            .addOnSuccessListener {
                Toast.makeText(this, "Rol actualizado", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }
}
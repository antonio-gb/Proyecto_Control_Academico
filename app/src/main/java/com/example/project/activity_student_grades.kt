package com.example.project // Define el paquete al que pertenece este archivo

import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.ArrayAdapter // Importa ArrayAdapter para vincular listas de datos con vistas
import android.widget.ListView // Importa ListView para mostrar listas de elementos
import android.widget.TextView // Importa TextView para mostrar texto en la interfaz
import android.widget.Toast // Importa Toast para mostrar mensajes breves al usuario
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore para la base de datos en la nube

class StudentGradesActivity : AppCompatActivity() { // Declara la actividad que muestra las calificaciones de un alumno

    private lateinit var db: FirebaseFirestore // Variable para la instancia de Firestore
    private val gradeList = mutableListOf<String>() // Lista mutable con los textos de calificaciones a mostrar
    private lateinit var adapter: ArrayAdapter<String> // Adaptador para conectar la lista de calificaciones con el ListView

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_student_grades) // Establece el layout XML de esta actividad

        db = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore

        val subjectId = intent.getStringExtra("subjectId") ?: "" // Obtiene el ID de la materia pasado por el intent
        val subjectName = intent.getStringExtra("subjectName") ?: "" // Obtiene el nombre de la materia pasado por el intent
        val uid = getSharedPreferences("session", MODE_PRIVATE).getString("uid", "") ?: "" // Obtiene el UID del alumno de las preferencias compartidas

        findViewById<TextView>(R.id.tvSubjectName).text = subjectName // Muestra el nombre de la materia en el TextView

        val lvGrades = findViewById<ListView>(R.id.lvGrades) // Obtiene la referencia al ListView de calificaciones
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, gradeList) // Crea el adaptador con la lista de calificaciones
        lvGrades.adapter = adapter // Asigna el adaptador al ListView

        db.collection("grades") // Accede a la colección de calificaciones en Firestore
            .whereEqualTo("studentId", uid) // Filtra las calificaciones por el UID del alumno
            .whereEqualTo("subjectId", subjectId) // Filtra además por el ID de la materia
            .get() // Ejecuta la consulta
            .addOnSuccessListener { result -> // Callback ejecutado si la consulta es exitosa
                gradeList.clear() // Limpia la lista actual de calificaciones
                if (result.isEmpty) { // Verifica si no hay calificaciones registradas
                    gradeList.add("Sin calificaciones aún") // Agrega un mensaje indicando que no hay calificaciones
                } else { // Si hay calificaciones registradas
                    for (doc in result) { // Itera sobre cada documento de calificación
                        val grade = doc.getLong("grade") ?: 0 // Obtiene el valor numérico de la calificación; 0 si no existe
                        gradeList.add("Calificación: $grade") // Agrega el texto de la calificación a la lista
                    }
                }
                adapter.notifyDataSetChanged() // Notifica al adaptador que los datos cambiaron para refrescar la vista
            }
            .addOnFailureListener { // Callback ejecutado si ocurre un error al cargar las calificaciones
                Toast.makeText(this, "Error cargando calificaciones", Toast.LENGTH_SHORT).show() // Muestra mensaje de error al usuario
            }
    }
}
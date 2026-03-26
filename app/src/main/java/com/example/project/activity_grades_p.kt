package com.example.project // Define el paquete al que pertenece este archivo

import android.app.AlertDialog // Importa AlertDialog para mostrar cuadros de diálogo
import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.ArrayAdapter // Importa ArrayAdapter para vincular listas de datos con vistas
import android.widget.EditText // Importa EditText para los campos de texto
import android.widget.ListView // Importa ListView para mostrar listas de elementos
import android.widget.TextView // Importa TextView para mostrar texto en la interfaz
import android.widget.Toast // Importa Toast para mostrar mensajes breves al usuario
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore para la base de datos en la nube

class GradesActivity : AppCompatActivity() { // Declara la actividad de calificaciones del profesor

    private lateinit var db: FirebaseFirestore // Variable para la instancia de Firestore
    private val studentNames = mutableListOf<String>() // Lista mutable con los nombres de los alumnos de la materia
    private val studentIds = mutableListOf<String>() // Lista mutable con los UIDs de los alumnos de la materia
    private lateinit var adapter: ArrayAdapter<String> // Adaptador para conectar la lista de alumnos con el ListView

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_grades_p) // Establece el layout XML de esta actividad

        db = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore

        val subjectId = intent.getStringExtra("subjectId") ?: "" // Obtiene el ID de la materia pasado por el intent
        val subjectName = intent.getStringExtra("subjectName") ?: "" // Obtiene el nombre de la materia pasado por el intent

        findViewById<TextView>(R.id.tvSubjectName).text = subjectName // Muestra el nombre de la materia en el TextView

        val lvStudents = findViewById<ListView>(R.id.lvStudents) // Obtiene la referencia al ListView de alumnos
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studentNames) // Crea el adaptador con la lista de nombres de alumnos
        lvStudents.adapter = adapter // Asigna el adaptador al ListView

        loadStudents(subjectId) // Carga la lista de alumnos inscritos en la materia

        lvStudents.setOnItemClickListener { _, _, position, _ -> // Configura el listener de clic en un elemento del ListView
            showGradeDialog(studentIds[position], studentNames[position], subjectId) // Muestra el diálogo para asignar calificación al alumno seleccionado
        }
    }

    private fun loadStudents(subjectId: String) { // Función que carga los alumnos inscritos en la materia desde Firestore
        db.collection("subjects").document(subjectId).get() // Consulta el documento de la materia en Firestore
            .addOnSuccessListener { doc -> // Callback ejecutado si la consulta es exitosa
                val students = doc.get("students") as? List<*> ?: emptyList<String>() // Obtiene la lista de UIDs de alumnos; lista vacía si no existe
                studentNames.clear() // Limpia la lista actual de nombres de alumnos
                studentIds.clear() // Limpia la lista actual de UIDs de alumnos
                for (uid in students) { // Itera sobre cada UID de alumno en la lista
                    db.collection("users").document(uid.toString()).get() // Consulta el documento del usuario en Firestore
                        .addOnSuccessListener { userDoc -> // Callback ejecutado si la consulta del usuario es exitosa
                            studentNames.add(userDoc.getString("name") ?: "Sin nombre") // Agrega el nombre del alumno a la lista
                            studentIds.add(uid.toString()) // Agrega el UID del alumno a la lista
                            adapter.notifyDataSetChanged() // Notifica al adaptador que los datos cambiaron para refrescar la vista
                        }
                }
            }
    }

    private fun showGradeDialog(studentId: String, studentName: String, subjectId: String) { // Función que muestra el diálogo para asignar una calificación
        val input = EditText(this) // Crea un campo de texto para ingresar la calificación
        input.hint = "Calificación (0-100)" // Establece el texto de ayuda del campo
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER // Configura el teclado numérico para la entrada

        AlertDialog.Builder(this) // Crea el constructor del diálogo de alerta
            .setTitle("Calificar a $studentName") // Establece el título del diálogo con el nombre del alumno
            .setView(input) // Establece el campo de texto como vista del diálogo
            .setPositiveButton("Guardar") { _, _ -> // Configura el botón positivo "Guardar"
                val grade = input.text.toString().trim() // Lee y limpia la calificación ingresada
                if (grade.isEmpty()) return@setPositiveButton // Sale si el campo está vacío sin guardar

                val gradeData = hashMapOf( // Crea un mapa con los datos de la calificación
                    "studentId" to studentId, // Campo UID del alumno calificado
                    "subjectId" to subjectId, // Campo UID de la materia
                    "grade" to grade.toInt() // Campo calificación convertida a número entero
                )
                db.collection("grades").add(gradeData) // Agrega la calificación a la colección "grades" en Firestore
                    .addOnSuccessListener { // Callback ejecutado si el guardado es exitoso
                        Toast.makeText(this, "Calificación guardada", Toast.LENGTH_SHORT).show() // Muestra mensaje de confirmación al usuario
                    }
            }
            .setNegativeButton("Cancelar", null) // Configura el botón negativo "Cancelar" que cierra el diálogo sin hacer nada
            .show() // Muestra el diálogo en pantalla
    }
}
package com.example.project // Define el paquete al que pertenece este archivo

import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.* // Importa todos los widgets de Android (Button, EditText, Spinner, ListView, Toast, etc.)
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore para la base de datos en la nube

class CreateSubjectActivity : AppCompatActivity() { // Declara la actividad para crear una nueva materia

    private lateinit var db: FirebaseFirestore // Variable para la instancia de Firestore
    private val teacherNames = mutableListOf<String>() // Lista mutable con los nombres de los profesores disponibles
    private val teacherIds = mutableListOf<String>() // Lista mutable con los UIDs de los profesores disponibles
    private val studentNames = mutableListOf<String>() // Lista mutable con los nombres de los alumnos disponibles
    private val studentIds = mutableListOf<String>() // Lista mutable con los UIDs de los alumnos disponibles

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_create_subject) // Establece el layout XML de esta actividad

        db = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore

        val etName = findViewById<EditText>(R.id.etSubjectName) // Obtiene la referencia al campo del nombre de la materia
        val etSchedule = findViewById<EditText>(R.id.etSchedule) // Obtiene la referencia al campo del horario
        val spinnerTeacher = findViewById<Spinner>(R.id.spinnerTeacher) // Obtiene la referencia al spinner de selección de profesor
        val lvStudents = findViewById<ListView>(R.id.lvStudents) // Obtiene la referencia al ListView de selección de alumnos
        val btnSave = findViewById<Button>(R.id.btnSaveSubject) // Obtiene la referencia al botón para guardar la materia

        db.collection("users").whereEqualTo("role", "profesor").get() // Consulta todos los usuarios con rol "profesor"
            .addOnSuccessListener { result -> // Callback ejecutado si la consulta es exitosa
                teacherNames.clear() // Limpia la lista actual de nombres de profesores
                teacherIds.clear() // Limpia la lista actual de UIDs de profesores
                for (doc in result) { // Itera sobre cada documento de profesor obtenido
                    teacherNames.add(doc.getString("name") ?: "Sin nombre") // Agrega el nombre del profesor a la lista
                    teacherIds.add(doc.id) // Agrega el UID del profesor a la lista
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, teacherNames) // Crea el adaptador para el spinner con los nombres de profesores
                spinnerTeacher.adapter = adapter // Asigna el adaptador al spinner de profesores
            }

        db.collection("users").whereEqualTo("role", "alumno").get() // Consulta todos los usuarios con rol "alumno"
            .addOnSuccessListener { result -> // Callback ejecutado si la consulta es exitosa
                studentNames.clear() // Limpia la lista actual de nombres de alumnos
                studentIds.clear() // Limpia la lista actual de UIDs de alumnos
                for (doc in result) { // Itera sobre cada documento de alumno obtenido
                    studentNames.add(doc.getString("name") ?: "Sin nombre") // Agrega el nombre del alumno a la lista
                    studentIds.add(doc.id) // Agrega el UID del alumno a la lista
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, studentNames) // Crea el adaptador para el ListView con selección múltiple
                lvStudents.adapter = adapter // Asigna el adaptador al ListView de alumnos
                lvStudents.choiceMode = ListView.CHOICE_MODE_MULTIPLE // Habilita la selección múltiple en el ListView
            }

        btnSave.setOnClickListener { // Configura el listener del botón de guardar
            val name = etName.text.toString().trim() // Lee y limpia el nombre de la materia ingresado
            val schedule = etSchedule.text.toString().trim() // Lee y limpia el horario ingresado

            if (name.isEmpty() || schedule.isEmpty()) { // Valida que los campos nombre y horario no estén vacíos
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show() // Muestra mensaje de error si hay campos vacíos
                return@setOnClickListener // Sale del listener sin continuar
            }

            if (teacherIds.isEmpty()) { // Verifica que exista al menos un profesor registrado
                Toast.makeText(this, "No hay profesores registrados", Toast.LENGTH_SHORT).show() // Muestra mensaje de error si no hay profesores
                return@setOnClickListener // Sale del listener sin continuar
            }

            val teacherId = teacherIds[spinnerTeacher.selectedItemPosition] // Obtiene el UID del profesor seleccionado en el spinner

            val selectedStudents = mutableListOf<String>() // Lista para almacenar los UIDs de los alumnos seleccionados
            for (i in 0 until lvStudents.count) { // Itera sobre todos los elementos del ListView de alumnos
                if (lvStudents.isItemChecked(i)) { // Verifica si el elemento en la posición i está marcado
                    selectedStudents.add(studentIds[i]) // Agrega el UID del alumno marcado a la lista de seleccionados
                }
            }

            val subject = hashMapOf( // Crea un mapa con los datos de la nueva materia
                "name" to name, // Campo nombre de la materia
                "schedule" to schedule, // Campo horario de la materia
                "teacherId" to teacherId, // Campo UID del profesor asignado
                "students" to selectedStudents // Campo lista de UIDs de alumnos inscritos
            )

            db.collection("subjects").add(subject) // Agrega la nueva materia a la colección "subjects" en Firestore
                .addOnSuccessListener { // Callback ejecutado si el guardado es exitoso
                    Toast.makeText(this, "Materia creada", Toast.LENGTH_SHORT).show() // Muestra mensaje de confirmación al usuario
                    finish() // Cierra la actividad y regresa al panel de administrador
                }
                .addOnFailureListener { // Callback ejecutado si ocurre un error al guardar
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show() // Muestra mensaje de error al usuario
                }
        }
    }
}
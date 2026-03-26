package com.example.project // Define el paquete al que pertenece este archivo

import android.content.Intent // Importa Intent para navegar entre actividades
import android.content.SharedPreferences // Importa SharedPreferences para acceder a los datos de sesión
import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.ArrayAdapter // Importa ArrayAdapter para vincular listas de datos con vistas
import android.widget.Button // Importa Button para los botones de la interfaz
import android.widget.ListView // Importa ListView para mostrar listas de elementos
import android.widget.TextView // Importa TextView para mostrar texto en la interfaz
import android.widget.Toast // Importa Toast para mostrar mensajes breves al usuario
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore para la base de datos en la nube

class TeacherActivity : AppCompatActivity() { // Declara la actividad del panel del profesor

    private lateinit var db: FirebaseFirestore // Variable para la instancia de Firestore
    private lateinit var prefs: SharedPreferences // Variable para las preferencias compartidas de la sesión
    private val subjectList = mutableListOf<String>() // Lista mutable con los nombres de las materias del profesor
    private val subjectIds = mutableListOf<String>() // Lista mutable con los IDs de las materias del profesor
    private lateinit var adapter: ArrayAdapter<String> // Adaptador para conectar la lista de materias con el ListView

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_teacher) // Establece el layout XML de esta actividad

        db = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore
        prefs = getSharedPreferences("session", MODE_PRIVATE) // Obtiene las preferencias compartidas con clave "session"

        val tvName = findViewById<TextView>(R.id.tvTeacherName) // Obtiene la referencia al TextView del nombre del profesor
        val lvSubjects = findViewById<ListView>(R.id.lvSubjects) // Obtiene la referencia al ListView de materias
        val btnScanQR = findViewById<Button>(R.id.btnScanQR) // Obtiene la referencia al botón para escanear QR
        val btnLogout = findViewById<Button>(R.id.btnLogout) // Obtiene la referencia al botón de cerrar sesión

        val uid = prefs.getString("uid", "") ?: "" // Obtiene el UID del profesor actualmente autenticado

        // Cargar nombre del profesor
        db.collection("users").document(uid).get() // Consulta el documento del profesor en Firestore
            .addOnSuccessListener { doc -> // Callback ejecutado si la consulta es exitosa
                tvName.text = "Hola, ${doc.getString("name")}" // Muestra un saludo con el nombre del profesor
            }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, subjectList) // Crea el adaptador con la lista de materias
        lvSubjects.adapter = adapter // Asigna el adaptador al ListView

        loadSubjects(uid) // Carga las materias asignadas al profesor

        // Al tocar una materia, ir a calificaciones
        lvSubjects.setOnItemClickListener { _, _, position, _ -> // Configura el listener de clic en un elemento del ListView
            val intent = Intent(this, GradesActivity::class.java) // Crea el intent para ir a la actividad de calificaciones
            intent.putExtra("subjectId", subjectIds[position]) // Pasa el ID de la materia seleccionada al siguiente intent
            intent.putExtra("subjectName", subjectList[position]) // Pasa el nombre de la materia seleccionada al siguiente intent
            startActivity(intent) // Inicia la actividad de calificaciones del profesor
        }

        btnScanQR.setOnClickListener { // Configura el listener del botón para escanear QR
            startActivity(Intent(this, QRScannerActivity::class.java)) // Inicia la actividad del escáner de códigos QR
        }

        btnLogout.setOnClickListener { // Configura el listener del botón de cerrar sesión
            prefs.edit().clear().apply() // Elimina todos los datos de sesión guardados en las preferencias
            startActivity(Intent(this, LoginActivity::class.java)) // Redirige a la actividad de inicio de sesión
            finish() // Cierra la actividad actual para que no quede en el back stack
        }
    }

    private fun loadSubjects(uid: String) { // Función que carga las materias asignadas al profesor desde Firestore
        db.collection("subjects") // Accede a la colección de materias en Firestore
            .whereEqualTo("teacherId", uid) // Filtra las materias por el UID del profesor
            .get() // Ejecuta la consulta
            .addOnSuccessListener { result -> // Callback ejecutado si la consulta es exitosa
                subjectList.clear() // Limpia la lista actual de nombres de materias
                subjectIds.clear() // Limpia la lista actual de IDs de materias
                for (doc in result) { // Itera sobre cada materia encontrada
                    subjectList.add(doc.getString("name") ?: "Sin nombre") // Agrega el nombre de la materia a la lista
                    subjectIds.add(doc.id) // Agrega el ID de la materia a la lista
                }
                adapter.notifyDataSetChanged() // Notifica al adaptador que los datos cambiaron para refrescar la vista
            }
            .addOnFailureListener { // Callback ejecutado si ocurre un error al cargar las materias
                Toast.makeText(this, "Error cargando materias", Toast.LENGTH_SHORT).show() // Muestra mensaje de error al usuario
            }
    }
}
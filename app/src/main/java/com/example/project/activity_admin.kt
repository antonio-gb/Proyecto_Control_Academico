package com.example.project // Define el paquete al que pertenece este archivo

import android.app.AlertDialog // Importa AlertDialog para mostrar cuadros de diálogo
import android.content.Intent // Importa Intent para navegar entre actividades
import android.content.SharedPreferences // Importa SharedPreferences para almacenar datos locales de sesión
import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.ArrayAdapter // Importa ArrayAdapter para vincular listas de datos con vistas
import android.widget.Button // Importa Button para los botones de la interfaz
import android.widget.ListView // Importa ListView para mostrar listas de elementos
import android.widget.Toast // Importa Toast para mostrar mensajes breves al usuario
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore para la base de datos en la nube

class AdminActivity : AppCompatActivity() { // Declara la actividad del panel de administrador

    private lateinit var db: FirebaseFirestore // Variable para la instancia de Firestore
    private lateinit var prefs: SharedPreferences // Variable para las preferencias compartidas de la sesión
    private val userList = mutableListOf<String>()      // nombres para mostrar // Lista mutable con los nombres de usuarios para mostrar en pantalla
    private val userIds = mutableListOf<String>()       // UIDs correspondientes // Lista mutable con los UIDs de los usuarios correspondientes
    private lateinit var adapter: ArrayAdapter<String> // Adaptador para conectar la lista de usuarios con el ListView

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_admin) // Establece el layout XML de esta actividad

        db = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore
        prefs = getSharedPreferences("session", MODE_PRIVATE) // Obtiene las preferencias compartidas con clave "session"

        val lvUsers = findViewById<ListView>(R.id.lvUsers) // Obtiene la referencia al ListView de usuarios
        val btnCreateSubject = findViewById<Button>(R.id.btnCreateSubject) // Obtiene la referencia al botón de crear materia
        val btnLogout = findViewById<Button>(R.id.btnLogout) // Obtiene la referencia al botón de cerrar sesión

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userList) // Crea el adaptador con la lista de usuarios
        lvUsers.adapter = adapter // Asigna el adaptador al ListView

        loadUsers() // Carga la lista de usuarios desde Firestore

        // Al tocar un usuario, mostrar opciones de rol
        lvUsers.setOnItemClickListener { _, _, position, _ -> // Configura el listener de clic en un elemento del ListView
            showRoleDialog(userIds[position], userList[position]) // Muestra el diálogo para cambiar el rol del usuario seleccionado
        }

        btnCreateSubject.setOnClickListener { // Configura el listener del botón de crear materia
            startActivity(Intent(this, CreateSubjectActivity::class.java)) // Abre la actividad para crear una nueva materia
        }

        btnLogout.setOnClickListener { // Configura el listener del botón de cerrar sesión
            prefs.edit().clear().apply() // Elimina todos los datos de sesión guardados en las preferencias
            startActivity(Intent(this, LoginActivity::class.java)) // Redirige a la actividad de inicio de sesión
            finish() // Cierra la actividad actual para que no quede en el back stack
        }
    }

    private fun loadUsers() { // Función que carga todos los usuarios desde Firestore
        db.collection("users").get() // Solicita todos los documentos de la colección "users"
            .addOnSuccessListener { result -> // Callback ejecutado si la consulta es exitosa
                userList.clear() // Limpia la lista actual de nombres de usuarios
                userIds.clear() // Limpia la lista actual de UIDs de usuarios
                for (doc in result) { // Itera sobre cada documento obtenido
                    val name = doc.getString("name") ?: "Sin nombre" // Obtiene el nombre del usuario; "Sin nombre" si no existe
                    val role = doc.getString("role") ?: "alumno" // Obtiene el rol del usuario; "alumno" por defecto
                    userList.add("$name ($role)") // Agrega el nombre con su rol a la lista de visualización
                    userIds.add(doc.id) // Agrega el UID del documento a la lista de identificadores
                }
                adapter.notifyDataSetChanged() // Notifica al adaptador que los datos cambiaron para refrescar la vista
            }
            .addOnFailureListener { // Callback ejecutado si ocurre un error al cargar los usuarios
                Toast.makeText(this, "Error cargando usuarios", Toast.LENGTH_SHORT).show() // Muestra mensaje de error al usuario
            }
    }

    private fun showRoleDialog(uid: String, userName: String) { // Función que muestra un diálogo para cambiar el rol de un usuario
        val view = layoutInflater.inflate(R.layout.activity_change_role, null) // Infla el layout del diálogo de cambio de rol
        val dialog = AlertDialog.Builder(this).setView(view).create() // Crea el diálogo con la vista inflada

        view.findViewById<android.widget.TextView>(R.id.tvUserName).text = userName // Muestra el nombre del usuario en el diálogo

        view.findViewById<Button>(R.id.btnSetAlumno).setOnClickListener { // Configura el listener del botón "Alumno"
            updateRole(uid, "alumno") // Actualiza el rol del usuario a "alumno"
            dialog.dismiss() // Cierra el diálogo
        }
        view.findViewById<Button>(R.id.btnSetProfesor).setOnClickListener { // Configura el listener del botón "Profesor"
            updateRole(uid, "profesor") // Actualiza el rol del usuario a "profesor"
            dialog.dismiss() // Cierra el diálogo
        }
        view.findViewById<Button>(R.id.btnSetAdmin).setOnClickListener { // Configura el listener del botón "Admin"
            updateRole(uid, "admin") // Actualiza el rol del usuario a "admin"
            dialog.dismiss() // Cierra el diálogo
        }

        dialog.show() // Muestra el diálogo en pantalla
    }

    private fun updateRole(uid: String, newRole: String) { // Función que actualiza el rol de un usuario en Firestore
        db.collection("users").document(uid).update("role", newRole) // Actualiza el campo "role" del documento del usuario
            .addOnSuccessListener { // Callback ejecutado si la actualización es exitosa
                Toast.makeText(this, "Rol actualizado", Toast.LENGTH_SHORT).show() // Muestra mensaje de confirmación al usuario
                loadUsers() // Recarga la lista de usuarios para reflejar el cambio
            }
            .addOnFailureListener { // Callback ejecutado si ocurre un error al actualizar el rol
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show() // Muestra mensaje de error al usuario
            }
    }
}
package com.example.project // Define el paquete al que pertenece este archivo

import android.content.Intent // Importa Intent para navegar entre actividades
import android.content.SharedPreferences // Importa SharedPreferences para almacenar datos locales de sesión
import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.Button // Importa Button para los botones de la interfaz
import android.widget.EditText // Importa EditText para los campos de texto
import android.widget.Toast // Importa Toast para mostrar mensajes breves al usuario
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.firebase.auth.FirebaseAuth // Importa FirebaseAuth para la autenticación de usuarios
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore para la base de datos en la nube

class LoginActivity : AppCompatActivity() { // Declara la actividad de inicio de sesión

    private lateinit var auth: FirebaseAuth // Variable para la instancia de autenticación de Firebase
    private lateinit var db: FirebaseFirestore // Variable para la instancia de Firestore
    private lateinit var prefs: SharedPreferences // Variable para las preferencias compartidas de la sesión

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_login) // Establece el layout XML de esta actividad

        auth = FirebaseAuth.getInstance() // Obtiene la instancia de FirebaseAuth
        db = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore
        prefs = getSharedPreferences("session", MODE_PRIVATE) // Obtiene las preferencias compartidas con clave "session"

        // Si ya hay sesión guardada, saltar login
        val savedRole = prefs.getString("role", null) // Lee el rol guardado en las preferencias; null si no existe
        if (savedRole != null) { // Si ya existe un rol guardado, la sesión está activa
            goToPanel(savedRole) // Navega directamente al panel correspondiente al rol
            return // Sale del método para no mostrar el login
        }

        val etEmail = findViewById<EditText>(R.id.etEmail) // Obtiene la referencia al campo de correo electrónico
        val etPassword = findViewById<EditText>(R.id.etPassword) // Obtiene la referencia al campo de contraseña
        val btnLogin = findViewById<Button>(R.id.btnLogin) // Obtiene la referencia al botón de inicio de sesión
        val btnGoRegister = findViewById<Button>(R.id.btnGoRegister) // Obtiene la referencia al botón para ir al registro

        btnLogin.setOnClickListener { // Configura el listener del botón de inicio de sesión
            val email = etEmail.text.toString().trim() // Lee y limpia el correo ingresado por el usuario
            val password = etPassword.text.toString().trim() // Lee y limpia la contraseña ingresada por el usuario

            if (email.isEmpty() || password.isEmpty()) { // Valida que ningún campo esté vacío
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show() // Muestra mensaje de error si hay campos vacíos
                return@setOnClickListener // Sale del listener sin continuar
            }

            auth.signInWithEmailAndPassword(email, password) // Intenta autenticar al usuario con email y contraseña
                .addOnSuccessListener { result -> // Callback ejecutado si la autenticación es exitosa
                    val uid = result.user!!.uid // Obtiene el UID del usuario autenticado
                    db.collection("users").document(uid).get() // Consulta el documento del usuario en Firestore
                        .addOnSuccessListener { doc -> // Callback ejecutado si la consulta es exitosa
                            val role = doc.getString("role") ?: "alumno" // Obtiene el rol del usuario; "alumno" por defecto
                            // Guardar sesión con SharedPreferences
                            prefs.edit().putString("role", role).putString("uid", uid).apply() // Guarda el rol y UID en las preferencias
                            goToPanel(role) // Navega al panel correspondiente al rol del usuario
                        }
                }
                .addOnFailureListener { // Callback ejecutado si la autenticación falla
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show() // Muestra mensaje de error al usuario
                }
        }

        btnGoRegister.setOnClickListener { // Configura el listener del botón para ir al registro
            startActivity(Intent(this, RegisterActivity::class.java)) // Abre la actividad de registro
        }
    }

    private fun goToPanel(role: String) { // Función que navega al panel según el rol del usuario
        val intent = when (role) { // Determina la actividad destino según el rol
            "admin" -> Intent(this, AdminActivity::class.java) // Si es admin, ir al panel de administración
            "profesor" -> Intent(this, TeacherActivity::class.java) // Si es profesor, ir al panel de profesor
            else -> Intent(this, StudentActivity::class.java) // Cualquier otro rol, ir al panel de alumno
        }
        startActivity(intent) // Inicia la actividad del panel correspondiente
        finish() // Cierra la actividad de login para que no quede en el back stack
    }
}
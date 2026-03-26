package com.example.project // Define el paquete al que pertenece este archivo

import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import android.widget.Button // Importa Button para los botones de la interfaz
import android.widget.EditText // Importa EditText para los campos de texto
import android.widget.Toast // Importa Toast para mostrar mensajes breves al usuario
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import com.google.firebase.auth.FirebaseAuth // Importa FirebaseAuth para la autenticación de usuarios
import com.google.firebase.firestore.FirebaseFirestore // Importa FirebaseFirestore para la base de datos en la nube

class RegisterActivity : AppCompatActivity() { // Declara la actividad de registro de nuevos usuarios

    private lateinit var auth: FirebaseAuth // Variable para la instancia de autenticación de Firebase
    private lateinit var db: FirebaseFirestore // Variable para la instancia de Firestore

    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        setContentView(R.layout.activity_register) // Establece el layout XML de esta actividad

        auth = FirebaseAuth.getInstance() // Obtiene la instancia de FirebaseAuth
        db = FirebaseFirestore.getInstance() // Obtiene la instancia de Firestore

        val etName = findViewById<EditText>(R.id.etName) // Obtiene la referencia al campo de nombre
        val etEmail = findViewById<EditText>(R.id.etEmail) // Obtiene la referencia al campo de correo electrónico
        val etPassword = findViewById<EditText>(R.id.etPassword) // Obtiene la referencia al campo de contraseña
        val btnRegister = findViewById<Button>(R.id.btnRegister) // Obtiene la referencia al botón de registro

        btnRegister.setOnClickListener { // Configura el listener del botón de registro
            val name = etName.text.toString().trim() // Lee y limpia el nombre ingresado por el usuario
            val email = etEmail.text.toString().trim() // Lee y limpia el correo ingresado por el usuario
            val password = etPassword.text.toString().trim() // Lee y limpia la contraseña ingresada por el usuario

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) { // Valida que ningún campo esté vacío
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show() // Muestra mensaje de error si hay campos vacíos
                return@setOnClickListener // Sale del listener sin continuar
            }

            auth.createUserWithEmailAndPassword(email, password) // Crea una nueva cuenta en Firebase Auth con email y contraseña
                .addOnSuccessListener { result -> // Callback ejecutado si la creación de cuenta es exitosa
                    val uid = result.user!!.uid // Obtiene el UID del usuario recién creado
                    val user = hashMapOf( // Crea un mapa con los datos del usuario para Firestore
                        "name" to name, // Campo nombre del usuario
                        "email" to email, // Campo correo electrónico del usuario
                        "role" to "alumno"  // rol por defecto // Asigna el rol "alumno" por defecto
                    )
                    db.collection("users").document(uid).set(user) // Guarda los datos del usuario en Firestore usando su UID
                        .addOnSuccessListener { // Callback ejecutado si el guardado en Firestore es exitoso
                            Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show() // Muestra mensaje de éxito al usuario
                            finish() // Cierra la actividad de registro y vuelve al login
                        }
                }
                .addOnFailureListener { // Callback ejecutado si ocurre un error al crear la cuenta
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show() // Muestra el mensaje de error al usuario
                }
        }
    }
}
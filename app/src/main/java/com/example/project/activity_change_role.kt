package com.example.project // Define el paquete al que pertenece este archivo

import android.os.Bundle // Importa Bundle para manejar el estado de la actividad
import androidx.activity.enableEdgeToEdge // Importa la función para habilitar el modo edge-to-edge
import androidx.appcompat.app.AppCompatActivity // Importa la clase base para actividades con soporte de compatibilidad
import androidx.core.view.ViewCompat // Importa ViewCompat para operaciones de vista compatibles
import androidx.core.view.WindowInsetsCompat // Importa WindowInsetsCompat para manejar los insets de ventana

class activity_change_role : AppCompatActivity() { // Declara la actividad de cambio de rol (usada como layout inflado en AdminActivity)
    override fun onCreate(savedInstanceState: Bundle?) { // Método llamado al crear la actividad
        super.onCreate(savedInstanceState) // Llama al método onCreate del padre
        enableEdgeToEdge() // Habilita el diseño edge-to-edge para usar toda la pantalla
        setContentView(R.layout.activity_change_role) // Establece el layout XML de esta actividad
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets -> // Registra un listener para manejar los insets del sistema
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()) // Obtiene los insets de las barras del sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom) // Aplica padding para evitar superposición con las barras del sistema
            insets // Retorna los insets para que otros listeners los puedan procesar
        }
    }
}
package com.example.tarickalia.pares

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tarickalia.R
import com.example.tarickalia.api.Models.Familium
import com.example.tarickalia.api.Models.Usuario
import com.example.tarickalia.api.TarickaliaApi
import com.example.tarickalia.databinding.ActivityTasquesCompletesParesBinding
import com.example.tarickalia.fills.TasksAdapter
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TasquesCompletesPares : AppCompatActivity() {

    private lateinit var binding: ActivityTasquesCompletesParesBinding
    private lateinit var drawerLayout: DrawerLayout
    private var familias: List<Familium>? = null
    private var usuarios: List<Usuario>? = null
    val familiaNombres = familias?.map { it.nombre }?.filterNotNull()
    val usuarioNombres = usuarios?.map { it.nombreUsuario }?.filterNotNull()
    private var selectedFamilia: Familium? = null
    private var selectedChild: Usuario? = null
    private var tareaAdapter: TareaAdapter? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasquesCompletesParesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usernamerebut = intent.getStringExtra("username")
        binding.nompares.text = usernamerebut

        drawerLayout = findViewById(R.id.drawer_layout)



        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.GestioFills -> {
                    val intent = Intent(this, GestioFamilia::class.java)
                    intent.putExtra("username", binding.nompares.text.toString())
                    startActivity(intent)
                }
                R.id.CreacioTasca -> {
                    val intent = Intent(this, CreacioTasques::class.java)
                    intent.putExtra("username", binding.nompares.text.toString())
                    startActivity(intent)
                }
                R.id.CreacioRecompenses -> {
                    val intent = Intent(this, CreacioRecompenses::class.java)
                    intent.putExtra("username", binding.nompares.text.toString())
                    startActivity(intent)
                }
                R.id.Puntuacions -> {
                    val intent = Intent(this, PuntuacionsPares::class.java)
                    intent.putExtra("username", binding.nompares.text.toString())
                    startActivity(intent)
                }
                R.id.TasquesCompletes -> {
                    val intent = Intent(this, TasquesCompletesPares::class.java)
                    intent.putExtra("username", binding.nompares.text.toString())
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        binding.menuopen.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.gohome.setOnClickListener {
            val intent = Intent(this, HomePares::class.java)
            intent.putExtra("username", usernamerebut)
            startActivity(intent)
        }

        cargarFamiliasEnSpinner()


        binding.nomfamilia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedFamiliaName = parent.getItemAtPosition(position).toString()
                selectedFamilia = familias?.find { it.nombre == selectedFamiliaName }
                selectedFamilia?.id?.let { cargarUsuariosDeFamiliaEnSpinner(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding.nomfill.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedChildName = parent.getItemAtPosition(position).toString()
                selectedChild = usuarios?.find { it.nombreUsuario == selectedChildName }
                loadTasks()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }
    // funcio per carregar les families en el spinner
    private fun cargarFamiliasEnSpinner() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiService = TarickaliaApi().getApiService()
                val responseFamilias = apiService.getFamiliums()

                if (responseFamilias.isSuccessful) {
                    familias = responseFamilias.body()
                    withContext(Dispatchers.Main) {
                        val familiaNames = familias?.map { it.nombre } ?: listOf()
                        val adapter = ArrayAdapter(this@TasquesCompletesPares, android.R.layout.simple_spinner_item, familiaNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.nomfamilia.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TasquesCompletesPares, "Error al cargar las familias", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // funcio per carregar els usuaris de la familia seleccionada en el spinner
    private fun cargarUsuariosDeFamiliaEnSpinner(idFamilia: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiService = TarickaliaApi().getApiService()
                val responseUsuarios = apiService.getUsuariosByFamilia(idFamilia)

                if (responseUsuarios.isSuccessful) {
                    usuarios = responseUsuarios.body()
                    withContext(Dispatchers.Main) {
                        val usuarioNames = usuarios?.map { it.nombreUsuario } ?: listOf()
                        val adapter = ArrayAdapter(this@TasquesCompletesPares, android.R.layout.simple_spinner_item, usuarioNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.nomfill.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TasquesCompletesPares, "Error al cargar los usuarios", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // funcio per carregar les tasques completes del fill seleccionat
    private fun loadTasks() {
        selectedChild?.let { child ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val apiService = TarickaliaApi().getApiService()
                    val responseTareas = apiService.getTareasByUsuario(child.id!!)

                    if (responseTareas.isSuccessful) {
                        val tareas = responseTareas.body()
                        val completedTasks = tareas?.filter { it.completada == true && it.aprobada == null }

                        withContext(Dispatchers.Main) {
                            if (completedTasks.isNullOrEmpty()) {
                                tareaAdapter?.updateTareas(mutableListOf())
                            } else {
                                tareaAdapter = TareaAdapter(completedTasks.toMutableList(), selectedChild!!, true)
                                binding.recyclerView.layoutManager = LinearLayoutManager(this@TasquesCompletesPares)
                                binding.recyclerView.adapter = tareaAdapter
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TasquesCompletesPares, "Error al cargar las tareas", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
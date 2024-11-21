package com.santaananortemetapan.uberclone.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager.LayoutParams.*
import android.widget.Toast
import com.santaananortemetapan.uberclone.databinding.ActivityMainBinding
import com.santaananortemetapan.uberclone.providers.AuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            FLAG_LAYOUT_NO_LIMITS,
            FLAG_LAYOUT_NO_LIMITS
        )

        binding.btnRegister.setOnClickListener { goToRegister() }
        binding.btnLogin.setOnClickListener { login() }
    }

    private fun login(){
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()

        if(email.isEmpty()){
            Toast.makeText(this, "El correo esta vacio", Toast.LENGTH_SHORT).show()
            return
        }
        if(password.isEmpty()){
            Toast.makeText(this, "El password esta vacio", Toast.LENGTH_SHORT).show()
            return
        }

        authProvider.login(email, password).addOnCompleteListener{
            if (it.isSuccessful){
                goToMap()
            }else{
                Toast.makeText(this, "error iniciando sesion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }


    private fun goToRegister(){
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()
        if(authProvider.existSession()){
            goToMap()
        }
    }

}
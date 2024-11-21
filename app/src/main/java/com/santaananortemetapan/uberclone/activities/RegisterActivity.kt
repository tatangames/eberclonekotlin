package com.santaananortemetapan.uberclone.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.santaananortemetapan.uberclone.databinding.ActivityRegisterBinding
import com.santaananortemetapan.uberclone.models.Client
import com.santaananortemetapan.uberclone.providers.AuthProvider
import com.santaananortemetapan.uberclone.providers.ClientProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        window.setFlags(
            FLAG_LAYOUT_NO_LIMITS,
            FLAG_LAYOUT_NO_LIMITS
        )

        binding.btnGoToLogin.setOnClickListener {
            goToLogin()
        }

        binding.btnRegister.setOnClickListener {
            register()
        }
    }

    private fun register(){

        val name = binding.textFieldName.text.toString()
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()

        if(name.isEmpty()){
            Toast.makeText(this, "El nombre esta vacio", Toast.LENGTH_SHORT).show()
            return
        }

        if(email.isEmpty()){
            Toast.makeText(this, "El correo esta vacio", Toast.LENGTH_SHORT).show()
            return
        }
        if(password.isEmpty()){
            Toast.makeText(this, "El password esta vacio", Toast.LENGTH_SHORT).show()
            return
        }

        if(password.length < 6){
            Toast.makeText(this, "Password 6 caracter minimo", Toast.LENGTH_SHORT).show()
            return
        }

        authProvider.register(email, password).addOnCompleteListener{
            if(it.isSuccessful){
                val client = Client(
                    id = authProvider.getId(),
                    name = name,
                    email = email
                )
                clientProvider.create(client).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registro exitosoo", Toast.LENGTH_SHORT).show()
                        goToMap()
                    }
                    else{
                        Toast.makeText(this@RegisterActivity, "Hubo un error almacenando datos", Toast.LENGTH_SHORT).show()
                    }
                }
                goToLogin()
            }else{
                Toast.makeText(this@RegisterActivity, "Registro fallido ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }


    private fun goToLogin(){
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}
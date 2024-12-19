package com.example.implementacaoes

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        val registarBTN = findViewById<AppCompatButton>(R.id.registRegistBTN)
        val emailInput = findViewById<EditText>(R.id.emailRegistEditView)
        val passInput = findViewById<EditText>(R.id.passwordRegistEditView)
        val passConfirmInput = findViewById<EditText>(R.id.confirmPasswordRegistEditView)


        registarBTN.setOnClickListener()
        {
            val email = emailInput.text.toString()
            val password = passInput.text.toString()
            val passwordConfirm = passConfirmInput.text.toString()

            if (password == passwordConfirm && password.length >= 6) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)

                            Toast.makeText(baseContext, "Conta criada com sucesso!", Toast.LENGTH_SHORT)
                                .show()

                            val intent = Intent(this@RegisterActivity, StockActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            updateUI(null)
                        }
                    }


            } else {
                if (password != passwordConfirm) {
                    Toast.makeText(baseContext, "As passwords devem coincidir!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                        Toast.makeText(baseContext, "A password deve ter no mínimo 6 caracteres!", Toast.LENGTH_SHORT)
                            .show()
                }
            }
        }

    }

    private fun updateUI(user: FirebaseUser?) {
    }

}
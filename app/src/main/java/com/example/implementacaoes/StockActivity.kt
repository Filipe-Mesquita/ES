package com.example.implementacaoes

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class StockActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var addB: AppCompatButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var container: LinearLayout
    private var buttonColor : Int = 0
    private lateinit var homeB: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stock)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://implementacaoes-default-rtdb.firebaseio.com/").reference

        container = findViewById(R.id.container)
        addB = findViewById(R.id.addB)
        homeB = findViewById(R.id.home)

        val isNightMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        buttonColor = if (isNightMode) {
            ContextCompat.getColor(this, R.color.white)
        } else {
            ContextCompat.getColor(this, R.color.black)
        }

        homeB.setOnClickListener {
            val intent = Intent(this@StockActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        addB.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)

        // Nome
        val nameLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 50, 0, 100)
        }
        val nameText = TextView(this).apply {
            text = "Nome: "
            typeface = Typeface.DEFAULT_BOLD
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
        }
        val nameEdit = EditText(this).apply {
            hint = "Clique aqui"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        nameLayout.addView(nameText)
        nameLayout.addView(nameEdit)

        // Número de caixas
        val qLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 100)
        }
        val qText = TextView(this).apply {
            text = "Quantidade: "
            typeface = Typeface.DEFAULT_BOLD
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
        }
        val qEdit = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_CLASS_NUMBER

                    gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        qLayout.addView(qText)
        qLayout.addView(qEdit)


        layout.addView(nameLayout)
        layout.addView(qLayout)


        val builder = AlertDialog.Builder(this)
        builder.setTitle("Adicionar Medicamento")
        builder.setView(layout)

        builder.setPositiveButton("Confirmar") { _, _ ->
            val name = nameEdit.text.toString()
            val amount = qEdit.text.toString()



            if (name.isBlank() || amount.isBlank() ) {
                Toast.makeText(this, "Todos os campos devem ser preenchidos!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val stockData = mapOf(
                    "Name" to name,
                    "Amout" to amount
                )

                val user = auth.currentUser
                val userId = user?.uid ?: return@setPositiveButton

                databaseReference.child("stock").child(userId).push().setValue(stockData)
                    .addOnSuccessListener {
                        Log.d("DATA NA DB", "Data written successfully!")
                        Toast.makeText(
                            this,
                            "Matrial Adicionada com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadStock()
                    }
                    .addOnFailureListener {
                        Log.e("DATA NA DB", "Erro ao adicionar material: ${it.message}")
                    }
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.show()



        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(buttonColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(buttonColor)
    }

    private fun loadStock() {
        container.removeAllViews()
        val user = auth.currentUser
        val userId = user?.uid ?: return

        databaseReference.child("stock").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (matSnapshot in snapshot.children) {
                            val name = matSnapshot.child("Name").getValue(String::class.java)
                                ?: "Desconhecido"
                            val amount = matSnapshot.child("Amount").getValue(Long::class.java)



                            // Cria os elementos da interface dinamicamente
                            val widthInPixels = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 270f, resources.displayMetrics
                            ).toInt()
                            val heightInPixels = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics
                            ).toInt()

                            val constraintLayout = ConstraintLayout(this@StockActivity).apply {
                                setBackgroundResource(R.drawable.black_border)
                                layoutParams =
                                    LinearLayout.LayoutParams(widthInPixels, heightInPixels).apply {
                                        bottomMargin = 100
                                    }
                                setPadding(20, 20, 20, 20) // Define padding opcional
                            }

                            val nameTextView = TextView(this@StockActivity).apply {
                                text = "${name}"
                                val typedValue = TypedValue()
                                theme.resolveAttribute(android.R.attr.editTextBackground, typedValue, true)
                                setBackgroundResource(typedValue.resourceId)
                                backgroundTintList = ContextCompat.getColorStateList(this@StockActivity, R.color.yellow)
                                textSize = 16f
                                setTextColor(ContextCompat.getColor(this@StockActivity, R.color.black))
                                id = ViewCompat.generateViewId()
                            }

                            val totalTextView = TextView(this@StockActivity).apply {
                                text = "$amount"
                                gravity = Gravity.CENTER_HORIZONTAL
                                setBackgroundResource(R.drawable.black_border)
                                isClickable = true
                                textSize = 16f
                                setTextColor(ContextCompat.getColor(this@StockActivity, R.color.black))
                                id = ViewCompat.generateViewId()
                                setPadding(20, 20, 20, 20)
                            }

                            val medLayout = LinearLayout(this@StockActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(50, 50, 50, 100)
                            }
                            val sobreText = TextView(this@StockActivity).apply {
                                text = "Quantidade: "
                                typeface = Typeface.DEFAULT_BOLD
                                textSize = 18f
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                gravity = Gravity.CENTER
                            }
                            val textEdit = EditText(this@StockActivity).apply {
                                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                                gravity = Gravity.CENTER
                                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            }
                            medLayout.addView(sobreText)
                            medLayout.addView(textEdit)


                            totalTextView.setOnClickListener{

                                val builder =  AlertDialog.Builder(this@StockActivity)
                                    .setTitle("Editar Quantidade")
                                    .setView(medLayout)

                                    .setPositiveButton("Continuar") { _, _ ->
                                        val nQuantidade = textEdit.text.toString()
                                        if (nQuantidade.isBlank()) {
                                            Toast.makeText(
                                                this@StockActivity,
                                                "O campo deve ser preenchido!",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        } else {
                                            val quantidade = nQuantidade.toInt()
                                            matSnapshot.child("Amount").ref.setValue(quantidade)
                                                .addOnSuccessListener {
                                                    // Mensagem de sucesso
                                                    Toast.makeText(
                                                        this@StockActivity,
                                                        "Quantidade atualizada com sucesso!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    container.removeAllViews()
                                                    loadStock()
                                                }
                                                .addOnFailureListener { e ->
                                                    // Mensagem de erro
                                                    Toast.makeText(
                                                        this@StockActivity,
                                                        "Erro ao atualizar: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    }
                                    .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }


                                val dialog = builder.show()

                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(buttonColor)
                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(buttonColor)
                            }



                            constraintLayout.addView(nameTextView)
                            constraintLayout.addView(totalTextView)

                            val constraintSet = ConstraintSet()
                            constraintSet.clone(constraintLayout)

                            // Centraliza o texto do nome verticalmente
                            constraintSet.connect(nameTextView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 125)
                            constraintSet.connect(nameTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                            constraintSet.connect(nameTextView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                            // Centraliza o texto do total de medicamentos verticalmente
                            constraintSet.connect(totalTextView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 50)
                            constraintSet.connect(totalTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                            constraintSet.connect(totalTextView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                            constraintSet.applyTo(constraintLayout)

                            // Adiciona o listener para Long Click
                            constraintLayout.setOnLongClickListener {
                                // Exibe o AlertDialog para confirmar a exclusão
                                val builder =  AlertDialog.Builder(this@StockActivity)
                                    .setTitle("Excluir Material")
                                    .setMessage("Você tem certeza que deseja excluir este Material?")
                                    .setPositiveButton("Sim") { _, _ ->
                                        // Excluir o item do Firebase
                                        matSnapshot.ref.removeValue().addOnSuccessListener {
                                            Toast.makeText(
                                                this@StockActivity,
                                                "Material excluído com sucesso!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            container.removeAllViews()
                                            loadStock()  // Recarregar a lista de medicamentos
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                this@StockActivity,
                                                "Erro ao excluir material: ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }


                                val dialog = builder.show()

                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(buttonColor)
                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(buttonColor)
                                true
                            }

                            // Adiciona ao container principal
                            container.addView(constraintLayout)
                        }
                    } else {
                        Log.d("Firebase", "Nenhum material encontrado para o usuário.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Erro ao buscar dados: ${error.message}")
                }
            })
    }
}
package oriol.paco.kammerat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import java.sql.DriverManager
import java.sql.SQLException
import android.os.StrictMode
import android.text.TextUtils
import android.view.View
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() {

    lateinit var fileToUpload: String
    lateinit var correuAct: String
    lateinit var passAct: String
    lateinit var nameAct: String

    private var mAuthTask: LoginActivity.UserLoginTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        submitButton.setOnClickListener { crearPersona() }
        takeButton.setOnClickListener { mostrarImatges() }
    }

    private fun mostrarImatges(){
        val pickPhoto = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            0 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent.data
                fileToUpload = getRealPathFromURI(selectedImage)
            }
            1 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent.data
                fileToUpload = getRealPathFromURI(selectedImage)
            }
        }
    }

    private fun getRealPathFromURI(contentURI: Uri): String {
        val result: String
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath()
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }
    fun crearPersona(){
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        emailRegister.error = null
        passwordRegister.error = null

        // Store values at the time of the login attempt.
        val emailStr = emailRegister.text.toString()
        val passwordStr = passwordRegister.text.toString()
        val nameStr = nameRegister.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for name
        if (TextUtils.isEmpty(nameStr)) {
            nameRegister.error = getString(R.string.error_invalid_name)
            cancel = true
        }
        // Check for a valid password.
        if (!isPasswordValid(passwordStr)) {
            passwordRegister.error = getString(R.string.error_invalid_password)
            focusView = passwordRegister
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            emailRegister.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            emailRegister.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            AltaPersona().altaPersona(fileToUpload, correuAct, passAct, nameAct)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val pat = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$")
        return pat.matcher(email).matches()
    }


    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    fun ferAlta(persona: String, correu: String, pass: String, nom: String){
        println("DONO D'ALTA: " + persona)
        println("INSERT INTO users VALUES ('$correu', md5('$pass'), '$nom', '$persona');")
        try {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            Class.forName("org.postgresql.Driver")
            val conn = DriverManager.getConnection(
                    "jdbc:postgresql://kammerat.cybqc7ksnnjo.eu-west-1.rds.amazonaws.com:5432/users", "root", "2018CopeRDS!")
            val stsql = "INSERT INTO users VALUES ('$correu', md5('$pass'), '$nom', '$persona');"
            val st = conn.createStatement()
            val rs = st.executeQuery(stsql)
            rs.next()
            conn.close()
        } catch (se: SQLException) {
            println("SQL ERROR: " + se.toString())
        } catch (e: ClassNotFoundException) {
            println("SQL CLASS ERROR: " + e.message)
        }

       val intent = Intent(this, GalleryActivity::class.java)
       intent.putExtra("correu", correuAct)
       startActivity(intent)
    }
}

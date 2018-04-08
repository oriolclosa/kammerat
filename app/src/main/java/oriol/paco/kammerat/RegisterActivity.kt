package oriol.paco.kammerat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun ferAlta(persona: String){
        println("DONO D'ALTA: " + persona)
    }
}

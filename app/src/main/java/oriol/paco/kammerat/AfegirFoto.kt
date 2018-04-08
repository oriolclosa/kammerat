package oriol.paco.kammerat

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.regex.Pattern

class AfegirFoto {
    fun afegirFoto(uri : String, idPersona : String) {
        val endpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/persongroups/${Constants.GROUP_ID}/persons/${idPersona}/persistedFaces"


        println("miro $endpoint")
        FuelManager.instance.baseHeaders = mapOf(
                "Content-Type" to "application/octet-stream",
                "Ocp-Apim-Subscription-Key" to Constants.KEY
        )

        FuelManager.instance.baseParams = listOf()
        val file = File(uri)
        val size = file.length()
        val bytes = ByteArray(size.toInt())
        try {
            println("puedo leer bien $uri en afegir foto")
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: Exception) {
            println("Fitxer no trobat en afegir foto")
        }

        println("Començant a enviar afegir foto")
        val llista = ArrayList<String>()


        //val uriDest = "https://www.mamaymami.com/wp-content/uploads/2016/07/parejas-de-lesbianas-catalanas-a-la-reproduccio%CC%81n-asistida-en-centros-pu%CC%81blicos.jpg"
        //endpoint.httpPost().body("{\"url\": \"$uriDest\" }").responseString { request, response, result ->
        endpoint.httpPost().body(bytes).responseString { request, response, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    println(result.getException())
                    println("ha petat afegir foto")
                }
                is Result.Success -> {
                    val data = result.get()
                    // TODO: quitar comillas
                    val p = Pattern.compile("(\"[^\"]{36}\")")
                    val m = p.matcher(data)
                    while (m.find()) {
                        llista.add(m.group().toString())
                    }
                    println("afegir foto tot be")
                    Train().train()
                }
            }
        }
    }
}

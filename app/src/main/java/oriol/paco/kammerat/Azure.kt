package oriol.paco.kammerat

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.regex.Pattern

class Azure {
    private val endpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect"

    fun send(uri: String) {
        FuelManager.instance.baseHeaders = mapOf(
                "Content-Type" to "application/octet-stream",
                "Ocp-Apim-Subscription-Key" to Constants.KEY
        )

        FuelManager.instance.baseParams = listOf(
                "returnFaceId" to "true",
                "returnFaceRectangle" to "false",
                "returnFaceLandmarks" to "true",
                "returnFaceAttributes" to "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise"
        )
        val file = File(uri)
        val size = file.length()
        val bytes = ByteArray(size.toInt())
        try {
            println("Hola $uri")
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: Exception) {
            println("Fitxer no trobat")
        }

        println("Començant a enviar imatge")
        val llista = ArrayList<String>()


        //val uriDest = "https://www.mamaymami.com/wp-content/uploads/2016/07/parejas-de-lesbianas-catalanas-a-la-reproduccio%CC%81n-asistida-en-centros-pu%CC%81blicos.jpg"
        //endpoint.httpPost().body("{\"url\": \"$uriDest\" }").responseString { request, response, result ->
          endpoint.httpPost().body(bytes).responseString { request, response, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    println(result.getException())
                }
                is Result.Success -> {
                    val data = result.get()
                    val p = Pattern.compile("(\"[^\"]{36}\")")
                    val m = p.matcher(data)
                    while (m.find()) {
                        val item = m.group().toString()
                        val x : StringBuilder = StringBuilder(item)
                        x.deleteCharAt(0)
                        x.deleteCharAt(x.length - 1)
                        llista.add(x.toString())
                    }
                    enviarListaAGallery(llista);
                }
            }
        }
    }

    fun enviarListaAGallery(list : ArrayList<String> ) {
        GalleryActivity().ferMatch(list)
    }
}
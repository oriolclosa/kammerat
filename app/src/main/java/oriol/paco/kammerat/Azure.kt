package oriol.paco.kammerat


import android.graphics.Bitmap
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.*
import com.github.kittinunf.result.Result
import org.json.JSONArray
import org.json.JSONObject



class Azure {
    private val subscriptionKey = "6df4e65191374f5596a29b8dfc759b16"
    private val uriBase = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect"

    fun send(): String {
        FuelManager.instance.baseHeaders = mapOf(
                "Content-Type" to "application/json",
                "Ocp-Apim-Subscription-Key" to subscriptionKey
        )

        FuelManager.instance.baseParams = listOf(
                "returnFaceId" to "true",
                "returnFaceLandmarks" to "true",
                "returnFaceAttributes" to "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise"
        )

        val uriDest = "https://www.mamaymami.com/wp-content/uploads/2016/07/parejas-de-lesbianas-catalanas-a-la-reproduccio%CC%81n-asistida-en-centros-pu%CC%81blicos.jpg"
        uriBase.httpPost().body("{\"url\": \"$uriDest\" }").responseString { request, response, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    // val ex = result.getException()
                    println("no puc fer res")
                    println(result.getException())
                }
                is Result.Success -> {
                    val data = result.get()
                    print ('@')
                    println(data)
                    print ('@')
                    val j = JSONObject(data)
                }
            }
        }

        return "holita"

    }
}
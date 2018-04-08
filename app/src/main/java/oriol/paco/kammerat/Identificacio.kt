package oriol.paco.kammerat

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import java.util.regex.Pattern

class Identificacio {
    private val endpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/identify"
    fun identificacio(list : ArrayList<String> ) {
        val contingut = """
        {
            "personGroupId": "${Constants.GROUP_ID}",
            "faceIds": $list,
            "maxNumOfCandidatesReturned": 1,
            "confidenceThreshold": 0.5
        }
        """
        FuelManager.instance.baseHeaders = mapOf(
                "Content-Type" to "application/json",
                "Ocp-Apim-Subscription-Key" to Constants.KEY
        )

        endpoint.httpPost().body(contingut).responseString { request, response, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    println(result.getException())
                }
                is Result.Success -> {
                    val data = result.get()
                    println("hem acabat!!!!")
                    println(data)
                }
            }
        }

    }
}
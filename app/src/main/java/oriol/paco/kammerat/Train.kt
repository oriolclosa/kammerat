package oriol.paco.kammerat

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result

class Train {
    private val endpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/persongroups/" + Constants.GROUP_ID+"/train"
        fun train() {
            FuelManager.instance.baseHeaders = mapOf(
                    "Content-Type" to "application/json",
                    "Ocp-Apim-Subscription-Key" to Constants.KEY
            )
        endpoint.httpPost().responseString {request, response, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    println(result.getException())
                }
                is Result.Success -> {
                    println("Group ${Constants.GROUP_ID} well trained ")
                }
            }
        }
    }
}
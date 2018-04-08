package oriol.paco.kammerat

import android.Manifest
import android.app.Activity
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_gallery.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.support.v4.app.ActivityCompat
import android.util.JsonReader
import android.widget.*
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.android.core.Json
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import org.postgresql.core.Utils
import java.io.File
import java.lang.Thread.sleep
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*


class GalleryActivity : AppCompatActivity() {
    lateinit var fileToUpload:File
    lateinit var filePath:Uri

    lateinit var correu:String
    lateinit var faceid:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        correu = intent.getStringExtra("correu")
        faceid = intent.getStringExtra("faceid")

        //temporitzador()

        pickImage.setOnClickListener { mostrarImatges() }
        sendImage.setOnClickListener { obtenirIDs() }
        fetchImages.setOnClickListener{ obtenirImatges() }
    }

    /*private fun temporitzador() {
        val myTimer = Timer()
        myTimer.schedule(object : TimerTask() {
            override fun run() {
                obtenirImatges()
            }

        }, 0, 5000)
    }*/

    private fun mostrarImatges(){
        val pickPhoto = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            0 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent.data
                imagePicked.setImageURI(selectedImage)
                filePath = selectedImage
                fileToUpload = File(getRealPathFromURI(selectedImage))
            }
            1 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent.data
                imagePicked.setImageURI(selectedImage)
                filePath = selectedImage
                fileToUpload = File(getRealPathFromURI(selectedImage))
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

    private fun obtenirIDs(){
        val credentialsProvider = BasicAWSCredentials("AKIAI3HUO3V33FAMWBSQ", "qvmkUQEqKkCTkVzs1tKHFzu5qezFMn4FcxfrwGe2")

        /*val credentialsProvider = CognitoCachingCredentialsProvider(
                applicationContext,
                "eu-west-1:44527e3a-2bfa-4d2b-94b1-b6654dff4ebd", // Identity Pool ID
                Regions.EU_WEST_1 // Region
        )*/

        val s3Client = AmazonS3Client(credentialsProvider)
        val transferUtility = TransferUtility.builder()
                .context(applicationContext)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(s3Client)
                .build()

        val uploadObserver = transferUtility.upload("testoriol", correu + "/" + fileToUpload.name, fileToUpload)

        uploadObserver.setTransferListener(object : TransferListener {

            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    Toast.makeText(applicationContext, "Image sent!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()

                println("ID:$id|bytesCurrent: $bytesCurrent|bytesTotal: $bytesTotal|$percentDone%")
            }

            override fun onError(id: Int, ex: Exception) {
                ex.printStackTrace()
            }

        })

        Azure().send(getRealPathFromURI(filePath), correu, fileToUpload.name)
    }

    fun ferMatch(list: ArrayList<String>, correu3: String, imatge2: String){
        Identificacio().identificacio(list, correu3, imatge2)
    }

    fun baixarImatge(path: String){
        println("PATH: " + path)


        val credentialsProvider = BasicAWSCredentials("AKIAI3HUO3V33FAMWBSQ", "qvmkUQEqKkCTkVzs1tKHFzu5qezFMn4FcxfrwGe2")

        val s3Client = AmazonS3Client(credentialsProvider)
        val transferUtility = TransferUtility.builder()
                .context(applicationContext)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(s3Client)
                .build()

        val fitxer = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+path)

        val downloadObserver = transferUtility.download("testoriol", path, fitxer)

        downloadObserver.setTransferListener(object : TransferListener {

            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    Toast.makeText(applicationContext, "Image received!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()

                println("ID:$id|bytesCurrent: $bytesCurrent|bytesTotal: $bytesTotal|$percentDone%")
            }

            override fun onError(id: Int, ex: Exception) {
                ex.printStackTrace()
            }

        })
    }

    fun obtenirImatges(){
        println("FACE: " + faceid)
        try {
            Class.forName("org.postgresql.Driver")
            val conn = DriverManager.getConnection(
                    "jdbc:postgresql://kammerat.cybqc7ksnnjo.eu-west-1.rds.amazonaws.com:5432/users", "root", "2018CopeRDS!")
            val stsql = "SELECT * FROM images WHERE (faceid = '$faceid');"
            val st = conn.createStatement()
            val rs = st.executeQuery(stsql)
            rs.next()
            val resultat = rs.getString(1)
            baixarImatge(resultat)
            try {
                Class.forName("org.postgresql.Driver")
                val conn2 = DriverManager.getConnection(
                        "jdbc:postgresql://kammerat.cybqc7ksnnjo.eu-west-1.rds.amazonaws.com:5432/users", "root", "2018CopeRDS!")
                val stsql2 = "DELETE FROM images WHERE (id = '$resultat') and (faceid = '$faceid');"
                val st2 = conn2.createStatement()
                st2.executeQuery(stsql2)
                conn2.close()
            } catch (se2: SQLException) {
                println("SQL ERROR2: " + se2.toString())
            } catch (e2: ClassNotFoundException) {
                println("SQL CLASS ERROR2: " + e2.message)
            }
            conn.close()
        } catch (se: SQLException) {
            println("SQL ERROR: " + se.toString())
        } catch (e: ClassNotFoundException) {
            println("SQL CLASS ERROR: " + e.message)
        }
    }

    fun penjarImatge(list: JSONArray, correu2: String, imatge2: String){
        val path = "$correu2/$imatge2"

        for(i in 0..(list.length()-1)){
            val act = list.getJSONObject(i)
            for(j in 0..(act.length()-2)){
                val act2 = act.getJSONArray("candidates").getJSONObject(j).get("personId")
                println(act2.toString())
                try {
                    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(policy)

                    Class.forName("org.postgresql.Driver")
                    val conn = DriverManager.getConnection(
                            "jdbc:postgresql://kammerat.cybqc7ksnnjo.eu-west-1.rds.amazonaws.com:5432/users", "root", "2018CopeRDS!")
                    val stsql = "INSERT INTO images VALUES ('$path', '$act2');"
                    val st = conn.createStatement()
                    val rs = st.executeQuery(stsql)
                    rs.next()
                    conn.close()
                } catch (se: SQLException) {
                    println("SQL ERROR: " + se.toString())
                } catch (e: ClassNotFoundException) {
                    println("SQL CLASS ERROR: " + e.message)
                }
            }
        }
    }


}

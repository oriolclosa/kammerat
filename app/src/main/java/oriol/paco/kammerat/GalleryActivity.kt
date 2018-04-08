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
import android.support.v4.app.ActivityCompat
import android.widget.*
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import org.postgresql.core.Utils
import java.io.File
import java.lang.Thread.sleep



class GalleryActivity : AppCompatActivity() {
    lateinit var fileToUpload:File
    lateinit var filePath:Uri

    lateinit var correu:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        correu = intent.getStringExtra("correu")

        pickImage.setOnClickListener { mostrarImatges() }
        sendImage.setOnClickListener { obtenirIDs() }
    }

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
        Azure().send(getRealPathFromURI(filePath))
    }

    fun ferMatch(list: ArrayList<String>){
        Identificacio().identificacio(list)
    }


    fun penjarImatge(list: ArrayList<String>){
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

        val iterator = list.iterator()
        if(iterator.hasNext()) {
            iterator.next()
        }
        iterator.forEach {
            val uploadObserver = transferUtility.upload("testoriol", it + "/" + fileToUpload.name, fileToUpload)

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
        }
    }


}

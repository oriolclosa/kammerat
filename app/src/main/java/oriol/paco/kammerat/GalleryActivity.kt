package oriol.paco.kammerat

import android.Manifest
import android.app.Activity
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.provider.MediaStore
import android.util.Log
import android.widget.GridView
import kotlinx.android.synthetic.main.activity_gallery.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.widget.SimpleAdapter
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import org.postgresql.core.Utils
import java.io.File

class GalleryActivity : AppCompatActivity() {
    lateinit var fileToUpload:File
    lateinit var s3:AmazonS3
    lateinit var transferUtility:TransferUtility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val credentialsProvider = CognitoCachingCredentialsProvider(applicationContext, "eu-west-1:aff89198-776e-4e35-86f0-d2f7228d0b4b", Regions.EU_WEST_1)
        s3 = AmazonS3Client(credentialsProvider)
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1))
        transferUtility = TransferUtility(s3, applicationContext)

        var permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            val permisos = listOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions( this, permisos.toTypedArray(), 1)
        }

        pickImage.setOnClickListener { mostrarImatges() }
        sendImage.setOnClickListener { penjarImatge() }
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
                fileToUpload = File(getRealPathFromURI(selectedImage))
            }
            1 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent.data
                imagePicked.setImageURI(selectedImage)
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

    private fun penjarImatge(){
        transferUtility.upload( "kammerat", "prova1.jpg", fileToUpload);
    }
}

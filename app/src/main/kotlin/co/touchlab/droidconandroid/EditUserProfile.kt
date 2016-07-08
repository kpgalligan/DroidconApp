package co.touchlab.droidconandroid

import android.R.id.home
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.droidconandroid.data.AppPrefs
import co.touchlab.droidconandroid.data.UserAccount
import co.touchlab.droidconandroid.superbus.QuickClearAvatarTask
import co.touchlab.droidconandroid.superbus.UploadAvatarCommand
import co.touchlab.droidconandroid.tasks.GrabUserProfile
import co.touchlab.droidconandroid.tasks.Queues
import co.touchlab.droidconandroid.tasks.UpdateUserProfileTask
import co.touchlab.droidconandroid.tasks.persisted.PersistedTaskQueueFactory
import co.touchlab.droidconandroid.utils.Toaster
import co.touchlab.profilephotoeditor.BitmapUtils
import co.touchlab.profilephotoeditor.PhotoScaleActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_user_profile.*
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "EditUserProfile"
private const val HTTPS_S3_AMAZONAWS_COM_DROIDCONIMAGES = "https://s3.amazonaws.com/droidconimages/"
private const val PHOTO_SELECT = 100
private const val PHOTO_CAPTURE = 101
private const val PHOTO_EDIT_COMPLETE = 102
private const val GALLERY_CONTENT_URI_PREFIX = "content://media/"
private const val EXTRA_DATA = "data"
private const val EXTRA_AVATAR_PATH = "avatarPath"

fun createEditUserProfile(c: Context) {
    c.startActivity(Intent(c, EditUserProfile::class.java))
}

class EditUserProfile : AppCompatActivity() {
    private var photoPath: String? = null
    private var imageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_profile)

        if (savedInstanceState == null) {
            refreshProfile()
        }

        toolbar.title = ""
        toolbar.setNavigationIcon(R.drawable.ic_action_tick)
        setSupportActionBar(toolbar)

        edit_user_backdrop.setColorFilter(ContextCompat.getColor(this, R.color.glyph_foreground_dark))

        phone.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        EventBusExt.getDefault().register(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == home) {
            validateChanges()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validateChanges() {
        val phoneString = getStringFromEditText(name)
        val emailString = getStringFromEditText(email)
        val nameString = getStringFromEditText(name)
        var twitterString = getStringFromEditText(twitter)

        while (twitterString.startsWith("@")) {
            twitterString = twitterString.substring(1)
            twitter.setText(twitterString)
        }

        when {
            TextUtils.isEmpty(nameString)
            -> Toast.makeText(this, R.string.error_name, Toast.LENGTH_SHORT).show()
            !TextUtils.isEmpty(phoneString) && PhoneNumberUtils.isGlobalPhoneNumber(phoneString)
            -> Toast.makeText(this, R.string.error_phone, Toast.LENGTH_SHORT).show()
            !TextUtils.isEmpty(emailString) && !Patterns.EMAIL_ADDRESS.matcher(getStringFromEditText(email)).matches()
            -> Toast.makeText(this, R.string.error_email, Toast.LENGTH_SHORT).show()
            else -> saveProfile()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBusExt.getDefault().unregister(this)
    }

    private fun refreshProfile() {
        val appPrefs = AppPrefs.getInstance(this)
        Queues.localQueue(this).execute(GrabUserProfile(appPrefs.userId))
    }

    private fun saveProfile() {
        Queues.localQueue(this).execute(
                UpdateUserProfileTask(getStringFromEditText(name),
                        getStringFromEditText(bio),
                        getStringFromEditText(company),
                        getStringFromEditText(twitter),
                        getStringFromEditText(linkedIn),
                        getStringFromEditText(website),
                        getStringFromEditText(facebook),
                        getStringFromEditText(phone),
                        getStringFromEditText(email),
                        getStringFromEditText(gPlus),
                        !hide_email.isChecked))
        finish()
    }

    private fun getStringFromEditText(editText: EditText): String {
        return editText.text.toString()
    }

    fun profile(ua: UserAccount) {
        //This is a little shitty.  Assuming an empty code means we haven't
        //done the initial data set.  Updates should only be coming from
        //avatar uploads
        if (StringUtils.isEmpty(email.text)) {
            name.setText(ua.name)
            email.setText(ua.email)
            phone.setText(ua.phone)
            company.setText(ua.company)
            facebook.setText(ua.facebook ?: "")
            twitter.setText(ua.twitter)
            linkedIn.setText(ua.linkedIn)
            gPlus.setText(ua.gPlus)
            website.setText(ua.website)
            bio.setText(ua.profile)
            //If email is set to null or not public in the UA then we want to check the "hide email" box
            hide_email.isChecked = !(ua.emailPublic ?: false)
        }

        if (!TextUtils.isEmpty(ua.avatarKey)) {
            Picasso.with(this).load(HTTPS_S3_AMAZONAWS_COM_DROIDCONIMAGES + ua.avatarKey).into(profile_image)
        } else {
            profile_image.setImageResource(R.drawable.profile_placeholder)
        }

        profile_image.setOnClickListener({ callPhotoGrabber() })
    }

    private fun callPhotoGrabber() {
        AlertDialog.Builder(this).setMessage(getString(R.string.photoChooser))
                .setPositiveButton(getString(R.string.camera)) { dialog, which -> startCamera() }
                .setNegativeButton(getString(R.string.gallery)) { dialog, which -> startGalleryPicker() }
                .show()
    }

    fun setImageURI(imageURI: Uri) {
        this.imageURI = imageURI
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (intent == null) {
            Log.e(TAG, "onActivityResult: called with null intent")
            return
        }

        when (requestCode) {
            PHOTO_SELECT -> {
                if (resultCode == Activity.RESULT_OK && intent.data != null) {
                    var filePath = intent.data.toString()
                    //We can only actually resolve to a file path if
                    //our URI looks like a gallery media content URI
                    if (filePath.startsWith(GALLERY_CONTENT_URI_PREFIX)) {
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                        var cursor: Cursor? = null
                        try {
                            cursor = contentResolver.query(intent.data, filePathColumn, null,
                                    null, null)
                            cursor.moveToFirst()

                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                            filePath = cursor.getString(columnIndex)
                        } finally {
                            cursor?.close()
                        }

                    }

                    if (TextUtils.isEmpty(filePath)) {
                        showErrorMessage(R.string.error_picture_select)
                    } else {
                        val photoEditIntent = PhotoScaleActivity.callMe(this, filePath)
                        startActivityForResult(photoEditIntent, PHOTO_EDIT_COMPLETE)
                    }
                }
            }
            PHOTO_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap: Bitmap?
                    val cameraPhotoPath = cameraPhotoPath()

                    if (cameraPhotoPath != null && File(cameraPhotoPath).exists()) {
                        val decoded = BitmapUtils.decodeSampledBitmapFromPath(cameraPhotoPath, 700, 700)
                        bitmap = BitmapUtils.rotateBitmap(cameraPhotoPath, decoded)
                    } else if (intent.extras.containsKey(EXTRA_DATA)) {
                        bitmap = intent.extras.get(EXTRA_DATA) as Bitmap?
                    } else {
                        bitmap = getBitmapFromUri()
                    }

                    if (bitmap != null)
                        SaveBitmapTask().execute(bitmap)
                    else {
                        Log.e(TAG, "onActivityResult: Photo capture returned ok but couldnt get bitmap")
                    }
                }
            }
            PHOTO_EDIT_COMPLETE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        photoEditComplete(intent.getStringExtra(EXTRA_AVATAR_PATH))
                    }
                    PhotoScaleActivity.RESULT_FAILED -> showErrorMessage(R.string.error_picture_select)
                }
            }
        }
    }

    fun startCamera() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val type = Environment.DIRECTORY_PICTURES
            val path = Environment.getExternalStoragePublicDirectory(type)

            val filePhoto = File(path, "avatar_" + System.currentTimeMillis() + ".jpg")
            photoPath = filePhoto.path

            val imageUri = Uri.fromFile(filePhoto)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            setImageURI(imageUri)
            startActivityForResult(takePictureIntent, PHOTO_CAPTURE)
        } else {
            Toast.makeText(this, getString(R.string.sd_profile_toast), Toast.LENGTH_LONG).show()
        }
    }

    fun cameraPhotoPath(): String? {
        val path = photoPath
        photoPath = null
        return path
    }

    fun photoEditComplete(path: String) {
        Queues.localQueue(this).execute(
                QuickClearAvatarTask(AppPrefs.getInstance(this).userId!!))
        refreshProfile()
        PersistedTaskQueueFactory.getInstance(this).execute(UploadAvatarCommand(path))
        Toaster.showMessage(this, getString(R.string.save_profile_toast))
    }

    fun startGalleryPicker() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PHOTO_SELECT)
    }

    fun getBitmapFromUri(): Bitmap? {
        contentResolver.notifyChange(imageURI!!, null)
        val cr = contentResolver
        try {
            return android.provider.MediaStore.Images.Media.getBitmap(cr, imageURI)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun showErrorMessage(messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun onEventMainThread(command: UploadAvatarCommand) {
        refreshProfile()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun onEventMainThread(grabUserProfile: GrabUserProfile) {
        profile(grabUserProfile.userAccount)
    }

    private inner class SaveBitmapTask : AsyncTask<Bitmap, Void, File>() {
        override fun doInBackground(vararg params: Bitmap): File {
            try {
                val avatarFile = File(filesDir, "avatar_" + System.currentTimeMillis() + ".jpg")
                val out = FileOutputStream(avatarFile)
                params[0].compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.close()

                return avatarFile
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        override fun onPostExecute(file: File) {
            val intent = PhotoScaleActivity.callMe(this@EditUserProfile, file.path)
            startActivityForResult(intent, PHOTO_EDIT_COMPLETE)
        }
    }
}

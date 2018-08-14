package com.jingbanyun.nougatadaptapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.jingbanyun.libforfileprovdider.FileProvider7
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST_WRITE = 1
    private val REQUEST_CODE_TAKE_PHOTO = 0x110
    private var mCurrentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_take_photo.setOnClickListener {
            //检查版本是否大于M
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //判断下有没有读写权限
                Log.e("权限","判断有没有权限")
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("权限","没有权限，判断是否弹窗提示")
                    //没权限，则申请权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Log.e("权限","没有权限，弹窗提示")
                        //需要弹窗解释 解释为什么需要权限
                        alert("我只搜索音频文件，求求你给下权限吧","友情提示"){
                            yesButton { myRequestPermission() }
                            noButton {  }
                        }.show()
                    }else{
                        Log.e("权限","没有权限，不弹窗提示")
                        //不需要弹窗解释 第一次系统提示了或用户勾选了不再提示
                        myRequestPermission()
                    }
                }else{
                    //有权限 执行操作
                    takePhotoNoCompress()
                }
            }else{
                takePhotoNoCompress()
            }
        }

        tv_install_apk.setOnClickListener {
            //apk文件预先push到手机里面 （adb push D:\sample.apk /sdcard）
            val file = File(Environment.getExternalStorageDirectory(), "sample.apk")

            val intent = Intent(Intent.ACTION_VIEW)
            //Uri.fromFile(file)，会报异常：android.os.FileUriExposedException
//            val uri = Uri.fromFile(file)
//            intent.setDataAndType(uri,
//                    "application/vnd.android.package-archive")

            FileProvider7.setIntentDataAndType(this@MainActivity,intent,"application/vnd.android.package-archive",file,true)
            startActivity(intent)
        }
    }


    //申请权限
    private fun myRequestPermission() {
        Log.e("权限","去申请权限")
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE)
    }
    //申请权限结果
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.e("权限","申请权限结果")
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限已申请
                takePhotoNoCompress()
            }else{
                toast("相机权限被拒绝")
            }
        }
    }

    private fun takePhotoNoCompress() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {

            val filename = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA)
                    .format(Date()) + ".png"
            val file = File(Environment.getExternalStorageDirectory(), filename)
            mCurrentPhotoPath = file.absolutePath


  /*          val fileUri: Uri = if (Build.VERSION.SDK_INT >= 24) {
                //android 7.0开始，file://URI可能会触发FileUriExposedException,改用FileProvider
                FileProvider.getUriForFile(this, "com.jingbanyun.nougatadaptapplication.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }
*/
            //将上面的代码，文件的映射（file_paths.xml）,清单文件的provider注册，封装成一个小库 --->FileProvider7
            val fileUri =  FileProvider7.getUriForFile(this@MainActivity,file)

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_TAKE_PHOTO) {
            val decodeFile = BitmapFactory.decodeFile(mCurrentPhotoPath)
            iv_photo.setImageBitmap(decodeFile)
        }
        // else tip?

    }

}

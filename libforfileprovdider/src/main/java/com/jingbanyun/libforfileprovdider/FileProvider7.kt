package com.jingbanyun.libforfileprovdider

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import java.io.File

/**
 * 转自: https://blog.csdn.net/lmj623565791/article/details/72859156
 * 总结下，使用content://替代file://，主要需要FileProvider的支持，而因为FileProvider是ContentProvider的子类，
 * 所以需要在AndroidManifest.xml中注册；而又因为需要对真实的filepath进行映射，所以需要编写一个xml文档，
 * 用于描述可使用的文件夹目录，以及通过name去映射该文件夹目录
*/
object FileProvider7 {

    fun getUriForFile(context: Context, file: File): Uri? {
        return if (Build.VERSION.SDK_INT >= 24) {
            getUriForFile24(context, file)
        } else {
            Uri.fromFile(file)
        }
    }

    private fun getUriForFile24(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context,
                context.packageName + ".android7.fileprovider",
                file)
    }

    fun setIntentDataAndType(context: Context,
                             intent: Intent,
                             type: String,
                             file: File,
                             writeAble: Boolean) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriForFile(context, file), type)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type)
        }
    }


    //FileProvider权限方式有2种：
    //方式一为 Intent.addFlags，该方式主要用于针对intent.setData，setDataAndType以及setClipData相关方式传递uri的
    fun setIntentData(context: Context,
                      intent: Intent,
                      file: File,
                      writeAble: Boolean) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.data = getUriForFile(context, file)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        } else {
            intent.data = Uri.fromFile(file)
        }
    }


    //方式二为 根据Intent查询出的所以符合的应用，grantUriPermission来给他们授权
    fun grantPermissions(context: Context, intent: Intent, uri: Uri, writeAble: Boolean) {

        var flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (writeAble) {
            flag = flag or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        intent.addFlags(flag)
        val resInfoList = context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, uri, flag)
        }
    }


}

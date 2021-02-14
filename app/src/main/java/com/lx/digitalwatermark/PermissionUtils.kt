package com.lx.digitalwatermark

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions

/**
 * @title：PermissionUtils
 * @projectName RouteModule
 * @description: <Description>
 * @author linxiao
 * @data Created in 2020/05/28
 */
@SuppressLint("CheckResult", "ObsoleteSdkInt")
fun requestPermissions(mActivity: Activity, permissions: Array<String>, resultCode: Int) {
    val rxPermissions = RxPermissions(mActivity)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        rxPermissions.requestEach(*permissions)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        // 用户已经同意该权限
                        Log.d("---------", permission.name + " is granted.")
                        //                            Toast.makeText(mContext,"用户已经同意该权限",Toast.LENGTH_SHORT).show();
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                        Log.d(
                            "---------",
                            permission.name + " is denied. More info should be provided."
                        )
                        Toast.makeText(mActivity, "用户拒绝了该权限，没有选中『不再询问』", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        Log.d("---------", permission.name + " is denied.")
                        Toast.makeText(mActivity, "用户拒绝了该权限，并且选中『不再询问』，", Toast.LENGTH_SHORT).show()
                        val builder = AlertDialog.Builder(mActivity)
                        builder.setTitle("帮助")
                        builder.setMessage("当前应用缺少必要权限。\n请点击\"设置\"-\"权限\"-打开所需权限。\n最后点击两次后退按钮，即可返回。")

                        // 拒绝, 退出应用
                        builder.setNegativeButton("退出",
                            DialogInterface.OnClickListener { dialog, which -> mActivity.finish() })
                        builder.setPositiveButton("设置",
                            DialogInterface.OnClickListener { dialog, which ->
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri =
                                    Uri.fromParts("package", mActivity.packageName, null)
                                intent.data = uri
                                mActivity.startActivityForResult(intent, resultCode)
                            })
                        builder.setCancelable(false)
                        builder.show()
                    }
                }
            }
    }
}
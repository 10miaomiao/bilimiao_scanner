package cn.a10miaomiao.bilimiao.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.zxing.Result
import com.king.camera.scan.AnalyzeResult
import com.king.camera.scan.CameraScan
import com.king.camera.scan.analyze.Analyzer
import com.king.camera.scan.util.PermissionUtils
import com.king.zxing.BarcodeCameraScanActivity
import com.king.zxing.DecodeConfig
import com.king.zxing.DecodeFormatManager
import com.king.zxing.analyze.QRCodeAnalyzer

private const val SCAN_RESULT = "extra_qrcode_text_result"

class QrScannerActivity : BarcodeCameraScanActivity() {

    private val rlPermissionTips by lazy {
        findViewById<View>(R.id.rlPermissionTips)
    }
    private val btnPermissionTipsSettings by lazy {
        findViewById<MaterialButton>(R.id.btnPermissionTipsSettings)
    }
    private val btnPermissionTipsCancel by lazy {
        findViewById<MaterialButton>(R.id.btnPermissionTipsCancel)
    }

    override fun initCameraScan(cameraScan: CameraScan<Result>) {
        super.initCameraScan(cameraScan)
        // 根据需要设置CameraScan相关配置
        cameraScan.setPlayBeep(true);
    }

    override fun createAnalyzer(): Analyzer<Result> {
        val decodeConfig = DecodeConfig();
        decodeConfig.setHints(DecodeFormatManager.QR_CODE_HINTS)//如果只有识别二维码的需求，这样设置效率会更高，不设置默认为DecodeFormatManager.DEFAULT_HINTS
            .setFullAreaScan(false)//设置是否全区域识别，默认false
            .setAreaRectRatio(0.8f)//设置识别区域比例，默认0.8，设置的比例最终会在预览区域裁剪基于此比例的一个矩形进行扫码识别
            .setAreaRectVerticalOffset(0)//设置识别区域垂直方向偏移量，默认为0，为0表示居中，可以为负数
            .setAreaRectHorizontalOffset(0);//设置识别区域水平方向偏移量，默认为0，为0表示居中，可以为负数
        return QRCodeAnalyzer(decodeConfig);
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qr_scanner
    }

    override fun initUI() {
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
        super.initUI()
        btnPermissionTipsCancel.setOnClickListener {
            finish()
        }
        btnPermissionTipsSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.setData(uri)
            startActivity(intent)
        }
        var themeColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.purple_200, theme)
        } else {
            resources.getColor(R.color.purple_200)
        }
        themeColor = intent.getIntExtra("extra_theme_color", themeColor)
        viewfinderView.setFrameColor(themeColor)
        viewfinderView.setFrameCornerColor(themeColor)
        viewfinderView.setLaserColor(themeColor)
        btnPermissionTipsCancel.setTextColor(themeColor)
        btnPermissionTipsCancel.setBackgroundWithRipple(Color.TRANSPARENT, 0x33000000)
        btnPermissionTipsSettings.backgroundTintList = ColorStateList.valueOf(themeColor)
        btnPermissionTipsSettings.setBackgroundWithRipple(themeColor, 0x33000000)
    }

    /**
     * 设置 MaterialButton 的背景色和涟漪色，保持圆角和水波效果
     *
     * @param bgColor 背景色
     * @param rippleColor 涟漪色
     */
    fun MaterialButton.setBackgroundWithRipple(
        @ColorInt bgColor: Int,
        @ColorInt rippleColor: Int
    ) {
        this.backgroundTintList = ColorStateList.valueOf(bgColor)
        this.rippleColor = ColorStateList.valueOf(rippleColor)
    }

    override fun onScanResultCallback(result: AnalyzeResult<Result>) {
        // 停止分析
        cameraScan.setAnalyzeImage(false)
        // 返回结果
        val intent = Intent()
        intent.putExtra(SCAN_RESULT, result.result.text)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun requestCameraPermissionResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (PermissionUtils.requestPermissionsResult(
                Manifest.permission.CAMERA,
                permissions,
                grantResults
            )
        ) {
            startCamera()
        } else {
            rlPermissionTips.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        if (rlPermissionTips.visibility == View.VISIBLE) {
            if (PermissionUtils.checkPermission(this, Manifest.permission.CAMERA)) {
                rlPermissionTips.visibility = View.GONE
                startCamera()
            }
        }
    }
}
package jp.techacademy.nasahiro.autoslideshowapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import jp.techacademy.nasahiro.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.database.Cursor

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PERMISSIONS_REQUEST_CODE = 100

    // APIレベルによって許可が必要なパーミッションを切り替える
    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

    // タイマー用の時間のための変数
    private var timer: Timer? = null
    private var seconds = 0.0
    private var handler = Handler(Looper.getMainLooper())
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // パーミッションの許可状態を確認する
        // 進む
        binding.startButton.setOnClickListener {
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(readImagesPermission),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
        // 戻る
        binding.returnButton.setOnClickListener {
            if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo2()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(readImagesPermission),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
        //再生/停止
        binding.playButton.setOnClickListener {
            var cursor: Cursor? = null
        // log
            Log.d("test","aaa")
            Log.d("test","$cursor")
            Log.d("test_t","$timer")

            //最初の画像表示　cursol を先頭を格納する
            cursor = getCursor()

           if (timer == null) {

                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {

                        // 描画の依頼
                        handler.post {
                            if (cursor != null) {
                                binding.playButton.text = String.format("停止")

                                if (!cursor!!.moveToNext()) cursor!!.moveToFirst()

        //                        showImage()

                                if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
                                    // 許可されている
                                    getContentsInfo()
                                } else {
                                    // 許可されていないので許可ダイアログを表示する
                                    requestPermissions(
                                        arrayOf(readImagesPermission),
                                        PERMISSIONS_REQUEST_CODE
                                    )
                                }

                                binding.startButton.isClickable = false
                                binding.returnButton.isClickable = false
                            }
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで200ミリ秒、ループの間隔を100ミリ秒 に設定
            } else {
               binding.playButton.text = String.format("再生")
                timer!!.cancel()
                timer = null
               binding.startButton.isClickable = true
               binding.returnButton.isClickable = true
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    // グローバル変数の宣言
    var photo_no = 1//写真番号

    // cursorを使えるようにする
    private fun getCursor(): Cursor? {
        // ContentProviderのデータを参照するcontentResolverクラスのインスタンス生成
        // ContentProvider：他アプリとのデータをやり取りする仕組み
        val resolver = contentResolver

        // 画像のデータをcursorに入れる
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        // 画像のデータが入ったcursorをreturnする
        return if (cursor!!.moveToFirst()) { // DB上の検索結果を格納するcursorが先頭にある場合
            cursor // 先頭のcursor
        } else {
            cursor.close()
            null
        }
    }

    // cursorに入れた画像を表示する
    private fun showImage(){
        var cursor: Cursor? = null
        if(cursor != null){
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            binding.imageView.setImageURI(imageUri)
        }
    }

    private fun getContentsInfo() { //進む
        // 画像の情報を取得する
        val photo_max = 5//最大写真番号 同じ写真もある
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        photo_no = photo_no + 1
        if (photo_no == (photo_max+1)) photo_no = 1
        if (cursor!!.moveToPosition(photo_no)) {
            //  if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            var imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            binding.imageView.setImageURI(imageUri)
        }
            cursor.close()
        }
    private fun getContentsInfo2() { //戻る
        // 画像の情報を取得する
        val photo_max = 5//最大写真番号 同じ写真もある
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        photo_no = photo_no - 1
        if (photo_no == 0) photo_no = photo_max
        if (cursor!!.moveToPosition(photo_no)) {
            //  if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            var imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            binding.imageView.setImageURI(imageUri)
        }
        cursor.close()
    }
}


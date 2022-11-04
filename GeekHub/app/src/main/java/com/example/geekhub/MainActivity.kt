package com.example.geekhub

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcF
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.geekhub.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.skt.Tmap.*
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread
import kotlin.coroutines.jvm.internal.CompletedContinuation.context


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tMapView: TMapView
    private lateinit var tMapTapi: TMapTapi
    private lateinit var tMapPointStart: TMapPoint
    private lateinit var tMapPointEnd: TMapPoint
    private lateinit var markerItem: TMapMarkerItem
    private lateinit var bitmap: Bitmap
    var userid = "미래의유저pid"
    val bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        println("1111111111111111111")
        fusedLocationClient.lastLocation.addOnSuccessListener {
            location : Location? ->
            if (location != null) {
                tMapPointStart = TMapPoint(location.latitude, location.longitude)
                tMapView.setCenterPoint(location.latitude, location.longitude)
                markerItem.tMapPoint = tMapPointStart
                markerItem.name = "현위치"
                markerItem.visible = TMapMarkerItem.VISIBLE
                bitmap = BitmapFactory.decodeResource(Re, R.drawable.placeholder)
                markerItem.icon = bitmap
                markerItem.setPosition(0.5F, 1.0F)
                tMapView.addMarkerItem("현위치", markerItem)
            }
        }
        println("22222222222222222222")

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<NavFragment>(R.id.main_container_view)
//                add<CameraxFragment>(R.id.camera_view)
            }
        }

        val MY_PERMISSION_ACCESS_ALL = 100

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            var permissions = arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSION_ACCESS_ALL)

        } // 퍼미션


        binding.goChatting.setOnClickListener {
            moveFragment(ChattingFragment())

        }
        // 채팅버튼

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        //nfc

    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    public override fun onResume() {
        super.onResume()

        val intent: Intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        var pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")    /* Handles all MIME based dispatches.
                                     You should specify only the ones that you need. */
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }

        }

        val rootBtn = findViewById<Button>(R.id.linebutton)

        binding.navButton.setOnClickListener {
            goNav()
        }
        rootBtn.setOnClickListener {
            findPath()
        }

        var intentFiltersArray = arrayOf(ndef)
        var techListsArray = arrayOf(arrayOf<String>(NfcF::class.java.name))
        nfcAdapter!!.enableForegroundDispatch(
            this,
            pendingIntent,
            intentFiltersArray,
            techListsArray
        )
        // nfc
    }

//    public override fun onNewIntent(intent: Intent) {
//        val tagFromIntent: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
//        //do something with tagFromIntent
//    }

    private fun initialize() {
        tMapView = TMapView(this)
        tMapView.setSKTMapApiKey("l7xx9f33576ed47042d3ac571c8a70c73e31")
        val linearLayoutTmap: LinearLayout =
            findViewById<LinearLayout>(R.id.linearLayoutTmap)
        linearLayoutTmap.addView(tMapView)
    }


    fun changeFragment(index: Int) {
        when (index) {
            1 -> {
                moveFragment(DeliveryFragment())
            }

            2 -> {
                moveFragment(DeliveryDetailFragment())
            }

            3 -> {
                moveFragment(NfcFragment())
            }

            4 -> {
                moveFragment(DeliveryCameraFragment())
            }

            5 -> {
                clearBackStack()
                moveFragment(DeliveryFragment())
            }
            6 -> {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<CameraxFragment>(R.id.camera_view)
                }
            }

        }


    }

    private fun moveFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_container_view, fragment)
            .addToBackStack(null)
            .commit()

    }


    fun clearBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tagFromIntent: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (tagFromIntent != null) {
            val data = Ndef.get(tagFromIntent)
            data.connect()
            val message = data.ndefMessage
            val record = message.records
            for (records in record) {
                println(records.payload)
                val convert = String(records.payload, StandardCharsets.UTF_8)
                println("프린트값")
                var spot = convert.substring(3)
                sendUserId(spot)


            }

        }

    }

    private fun findPath() {
        tMapPointStart = TMapPoint(35.178013, 126.90464)
        tMapPointEnd = TMapPoint(35.17764128, 126.9042024)
        thread {
            try {
                var tMapPolyLine: TMapPolyLine = TMapData().findPathDataWithType(
                    TMapData.TMapPathType.CAR_PATH,
                    tMapPointStart,
                    tMapPointEnd
                )
                println(tMapPolyLine.lineColor)
                tMapPolyLine.lineColor = Color.RED
                tMapPolyLine.setLineWidth(2F)
                tMapView.addTMapPolyLine("Line1", tMapPolyLine)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun goNav() {
        tMapTapi = TMapTapi(this)
        if (tMapTapi.isTmapApplicationInstalled) {
            tMapTapi.invokeRoute("테스트 목적지", 126.90303593521712F, 35.17764936F)
        } else {
            println("설치 안됨")
            var uri = Uri.parse(tMapTapi.tMapDownUrl[0])
            var intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }


    fun sendData(fragment: Fragment, title: String) {

        bundle.putString("title", title)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun sendUserId(spot: String) {
        val fragment = CameraxFragment()
        Log.d("스트링",spot)
        bundle.putString("spot", spot)
        bundle.putString("userid",userid)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().add(R.id.camera_view,fragment).commit()


    }
}



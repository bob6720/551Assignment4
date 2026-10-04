package com.example.assignment4

import android.Manifest
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.example.assignment4.Viewmodels.ActiveScreen
import com.example.assignment4.Viewmodels.ApplicationViewModel
import com.example.assignment4.ui.theme.Assignment4Theme
import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.androidcommunications.api.ble.model.gatt.client.ChargeState
import com.polar.androidcommunications.api.ble.model.gatt.client.PowerSourcesState
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallbackProvider
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.PolarBleDisconnectInfo
import com.polar.sdk.api.model.EcgSample
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarEcgDataSample
import com.polar.sdk.api.model.PolarHealthThermometerData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.subscribe
import kotlinx.coroutines.launch
import java.util.UUID

// Issue found with screen rotation going to home page, now fixed
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var connectionStatus by mutableStateOf("Disconnected")
    private var device by mutableStateOf("")
    private var searchJob: Job? = null
    private var slowSampling: Boolean = false

    //by default all api features are enabled.
    //we can get rid of the ones we arent using.
    val api: PolarBleApi by lazy {
        PolarBleApiDefaultImpl.defaultImplementation(
            applicationContext,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,    //maybe remove
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_H10_EXERCISE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP, //maybe remove
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO  //maybe remove
            )
        )
    }

    private lateinit var db : ExerciseDataStore;
    private lateinit var exerciseDao: ExerciseDao;

    // Don't use
    // private lateinit var viewModel : ApplicationViewModel;

    // Use this
    // Rotating the screen will keep you on whichever screen you were on
    // The ViewModel and all its states will survive configuration changes
    private val viewModel: ApplicationViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ApplicationViewModel(
                    exerciseDao,
                    resources,
                    sharedPrefs,
                    ::connectToDevice,
                    ::disconnectDevice
                ) as T
            }
        }
    }


    private lateinit var sharedPrefs : SharedPreferences

    private lateinit var resources : Resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = Room.databaseBuilder<ExerciseDataStore>(applicationContext, "workout-data")
            .fallbackToDestructiveMigration()
            .setDriver(AndroidSQLiteDriver())
            .build()

        exerciseDao = db.exerciseDao()

        sharedPrefs = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE)

        resources = getResources()

        // Don't Use
        // viewModel = ApplicationViewModel(exerciseDao, resources, sharedPrefs, ::connectToDevice, ::disconnectDevice)

        setupPolarCallback()

        setContent {
            Assignment4Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val our_screen = viewModel.activeScreen.collectAsState()

                    when(our_screen.value)
                    {
                        ActiveScreen.HOME_SCREEN -> {
                            HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel,
                                connectionStatus = connectionStatus,
                                device = device
                            )
                        }
                        ActiveScreen.HISTORY_SCREEN -> HistoryScreen(modifier = Modifier.padding(innerPadding), viewModel)
                        ActiveScreen.ACTIVE_SCREEN -> ActiveScreen(modifier = Modifier.padding(innerPadding), viewModel)
                        ActiveScreen.FINISHED_WORKOUT_SCREEN -> FinishedWorkoutScreen(modifier = Modifier.padding(innerPadding), viewModel)
                        ActiveScreen.SETTINGS_SCREEN -> SettingsScreen(modifier = Modifier.padding(innerPadding), viewModel)
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        viewModel.activeScreenViewModel?.ScreenExit()
        super.onDestroy()
        searchJob?.cancel()
        api.shutDown()
    }

    private fun setupPolarCallback() {
        api.setApiCallback(object : PolarBleApiCallbackProvider {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d(TAG, "BLE power: $powered")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTED: ${polarDeviceInfo.deviceId}")
                connectionStatus = "connected"
                device = polarDeviceInfo.deviceId
                searchJob?.cancel()
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTING: ${polarDeviceInfo.deviceId}")
                connectionStatus = "connecting"
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "DISCONNECTED: ${polarDeviceInfo.deviceId}")
                connectionStatus = "Disconnected"
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo, info: PolarBleDisconnectInfo) {
                Log.d(TAG, "DISCONNECTED: ${polarDeviceInfo.deviceId}, info: $info")
                connectionStatus = "Disconnected"
            }

            override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
                Log.d(TAG, "Polar BLE SDK feature $feature is ready for $identifier")
                if (feature == PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING) {
                    lifecycleScope.launch {
                        try {
                            val streamTypes = api.getAvailableOnlineStreamDataTypes(identifier)
                            if (PolarBleApi.PolarDeviceDataType.HR in streamTypes) {
                                api.startHrStreaming(identifier)
                                    .catch { error -> Log.e(TAG, "HR stream failed", error) }
                                    .collect { hrData ->
                                        if (viewModel.GetActiveWorkout() == null) {
                                            return@collect
                                        }

                                        CoroutineScope(Dispatchers.IO).launch {

                                            var lastTime = 0L
                                            for (sample in hrData.samples) {
                                                slowSampling = !slowSampling
                                                if (slowSampling) {
                                                    continue
                                                }
//                                                Log.d(TAG, "HR sample: ${sample.hr} bpm")

                                                val currentTime = System.currentTimeMillis()
                                                val time =
                                                    if (currentTime <= lastTime) lastTime + 1 else currentTime
                                                lastTime = time

                                                Log.d(TAG, "$time")
                                                viewModel.InsertHRSample(
                                                    viewModel.GetActiveWorkout()!!.id,
                                                    time,
                                                    sample.hr.toFloat()
                                                )
                                            }
                                        }
                                    }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to query stream types", e)
                        }
                    }

                    lifecycleScope.launch {
                        val streamTypes = api.getAvailableOnlineStreamDataTypes(identifier)

                        try {
                            if (PolarBleApi.PolarDeviceDataType.ACC in streamTypes) {
                                val accel_settings: Map<PolarSensorSetting.SettingType, Int> =
                                    mapOf(PolarSensorSetting.SettingType.SAMPLE_RATE to 25);
                                api.startAccStreaming(
                                    identifier, PolarSensorSetting(accel_settings)
                                )
                                    .catch { error -> Log.e(TAG, "ACC stream failed", error) }
                                    .collect { accData ->
                                        if (viewModel.GetActiveWorkout() == null) {
                                            return@collect
                                        }

                                        for (sample in accData.samples) {
                                            //Log.d(TAG, "ACC sample: ${sample.x}, ${sample.y}, ${sample.z}")
                                            viewModel.InsertAccelSample(
                                                viewModel.GetActiveWorkout()!!.id,
                                                System.currentTimeMillis(),
                                                sample.x.toFloat(),
                                                sample.y.toFloat(),
                                                sample.z.toFloat()
                                            )
                                            CoroutineScope(Dispatchers.IO).launch {
                                                for (sample in accData.samples) {
                                                    //Log.d(TAG, "ACC sample: ${sample.x}, ${sample.y}, ${sample.z}")
                                                    viewModel.InsertAccelSample(
                                                        viewModel.GetActiveWorkout()!!.id,
                                                        System.currentTimeMillis(),
                                                        sample.x.toFloat(),
                                                        sample.y.toFloat(),
                                                        sample.z.toFloat()
                                                    )
                                                }
                                            }
                                        }
                                    }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to query stream types", e)
                        }
                    }

                    lifecycleScope.launch {
                        val streamTypes = api.getAvailableOnlineStreamDataTypes(identifier)

                        try {
                            if (PolarBleApi.PolarDeviceDataType.ECG in streamTypes) {
                                val ecg_settings = api.requestStreamSettings(
                                    identifier,
                                    PolarBleApi.PolarDeviceDataType.ECG
                                )
                                api.startEcgStreaming(identifier, ecg_settings.maxSettings())
                                    .catch { error -> Log.e(TAG, "ECG stream failed", error) }
                                    .collect { ecgData ->
                                        if (viewModel.GetActiveWorkout() == null) {
                                            return@collect
                                        }

                                        CoroutineScope(Dispatchers.IO).launch {
                                            for (sample in ecgData.samples) {
                                                if (sample is EcgSample) {
                                                    //Log.d(TAG, "ECG sample: ${sample.voltage}")
                                                    viewModel.InsertECGSample(
                                                        viewModel.GetActiveWorkout()!!.id,
                                                        sample.timeStamp,
                                                        sample.voltage.toFloat()
                                                    )
                                                }
                                            }
                                        }
                                    }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to query stream types", e)
                        }
                    }
                }
            }

            //same as the PolarBLEAPI. we can remove the calls we arent using
            override fun bleSdkFeaturesReadiness(
                identifier: String,
                ready: List<PolarBleApi.PolarBleSdkFeature>,
                unavailable: List<PolarBleApi.PolarBleSdkFeature>
            ) {
                Log.d(TAG, "BLE SDK features readiness - ready: $ready, unavailable: $unavailable")
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                Log.d(TAG, "DIS INFO uuid: $uuid value: $value")
            }

            override fun disInformationReceived(identifier: String, disInfo: DisInfo) {
                Log.d(TAG, "DIS INFO disInfo: $disInfo")
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d(TAG, "BATTERY LEVEL: $level")
            }

            override fun batteryChargingStatusReceived(identifier: String, chargingStatus: ChargeState) {
                Log.d(TAG, "BATTERY CHARGING STATUS: $chargingStatus")
            }

            override fun powerSourcesStateReceived(identifier: String, powerSourcesState: PowerSourcesState) {
                Log.d(TAG, "POWER SOURCES STATE: $powerSourcesState")
            }

            override fun hrNotificationReceived(identifier: String, data: PolarHrData.PolarHrSample) {
                Log.d(TAG, "HR NOTIFICATION: ${data.hr} bpm")
            }

            override fun htsNotificationReceived(identifier: String, data: PolarHealthThermometerData) {
                Log.d(TAG, "HTS NOTIFICATION: $data")
            }
        })
    }

    /**
     * checks bluetooth perms
     * finds the nearest polar device and connects to it.
     */
    fun connectToDevice() {
        requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), 1)

        api.foregroundEntered()

        lifecycleScope.launch {
            try {
                // Auto-connect to closest Polar sensor (RSSI >= -80 dBm)
                api.autoConnectToDevice(rssiLimit = -80, service = null, polarDeviceType = null)
            } catch (e: Exception) {
                Log.e(TAG, "Auto connect failed", e)
            }
        }

        //finds the nearest polar device and connects to it.
        //could add a section in the ui that loads all the nearby devices and you then choose which one
        //to connect to
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            api.searchForDevice()
                .catch { error -> Log.e(TAG, "Search failed", error) }
                .collect { polarDeviceInfo ->
                    Log.d(TAG, "FOUND DEVICE: ${polarDeviceInfo.deviceId} (${polarDeviceInfo.name})")
                    try {
                        api.connectToDevice(polarDeviceInfo.deviceId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Connect to ${polarDeviceInfo.deviceId} failed", e)
                    }
                }
        }
    }

    fun disconnectDevice()
    {
        api.shutDown()
    }
}

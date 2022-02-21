package com.example.userlocalauth

import android.util.Log
import androidx.annotation.NonNull
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterFragmentActivity
import androidx.fragment.app.FragmentActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.Executor
import android.content.Context;
import android.app.KeyguardManager;
import android.content.Intent;
import android.provider.Settings;
import android.hardware.fingerprint.FingerprintManager;

class MainActivity: FlutterFragmentActivity() { // flutter fragment for load auth screen....important
     private val CHANNEL = "com.flutter.epic/epic"

     var isValidUser = "false"
     var hasBiometric = ""

     var isKeyLocked = false
     var isDeviceloked = false
     var isSecure = false
     var locked = false
     var iskeyguardSecure = false

    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233
    private var status = "invalid"
    private lateinit var keyguardManager:KeyguardManager

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "biometricPrompt") {
                result.success("This is kotlin")
                biometricAuth()
            }
            else if(call.method == "keyguardManager"){
                keyguardAuth()
            }
            else if(call.method == "isAuthenticated"){
                var isValid = isAuthenticated()
                result.success(isValid)
            }
        }
    }
    fun biometricAuth(){
        val activity: FragmentActivity = this //this is important
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricManager = BiometricManager.from(activity) 

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.e("MY_APP_TAG", "The user hasn't associated " +
                        "any biometric credentials with their account.")
            }
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS){
            Log.d("MainActivity", "Device supports biometric auth")

            val callback = object: BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d("MainActivity", "$errorCode :: $errString")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d("MainActivity", "Authentication failed")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isValidUser = "true"
                    super.onAuthenticationSucceeded(result)
                    Log.d("MainActivity", "Authentication was successful")
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setDescription("User needs to be authenticated before using the app")
                .setDeviceCredentialAllowed(true)
                .build();

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        }
    }
    
    fun keyguardAuth(){
        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        var i = keyguardManager.createConfirmDeviceCredentialIntent("Title", "Description")
        try {
            var a = startActivityForResult(i, LOCK_REQUEST_CODE)
            println("info 1: "+a)
        } catch (e:Exception) {
            println("info 2: "+e)
            var intent = Intent(Settings.ACTION_SECURITY_SETTINGS);
            try {
                println("info 3: goto settings")
                startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE);
            } catch (e:Exception) {
                println("info 4: "+e)
                }
            }  
    }

    fun isAuthenticated():String{
        try{

            locked = keyguardManager.inKeyguardRestrictedInputMode()
            isSecure = keyguardManager.isDeviceSecure()
            isDeviceloked = keyguardManager.isDeviceLocked ()
            isKeyLocked = keyguardManager.isKeyguardLocked ()
            iskeyguardSecure = keyguardManager.isKeyguardSecure()//true if the user set up their Lock Screen securely.

            println("is locked : $locked")
            println("is device secure : $isSecure") 
            println("is device locked : $isDeviceloked")    
            println("is key locked : $isKeyLocked") 
            println("is key guard secuew : $iskeyguardSecure") 

        } catch (e:Exception) {
            println("info 4: "+e)
            println("is locked : $locked")
            println("is device secure : $isSecure") 
            println("is device locked : $isDeviceloked")    
            println("is key locked : $isKeyLocked") 
            println("is key guard secuew : $iskeyguardSecure") 
            }

        println(RESULT_OK)
        println(isValidUser)

        return isValidUser
    }
}

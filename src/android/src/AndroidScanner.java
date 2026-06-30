package tl.cordova.plugin.firebase.mlkit.barcode.scanner;

// ----------------------------------------------------------------------------
// |  Android Imports
// ----------------------------------------------------------------------------
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

// ----------------------------------------------------------------------------
// |  Cordova Imports
// ----------------------------------------------------------------------------
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

// ----------------------------------------------------------------------------
// |  Google Imports
// ----------------------------------------------------------------------------
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.common.api.CommonStatusCodes;

// ----------------------------------------------------------------------------
// |  Our Imports
// ----------------------------------------------------------------------------

public class AndroidScanner extends CordovaPlugin {
  // ----------------------------------------------------------------------------
  // | Public Properties
  // ----------------------------------------------------------------------------

  // ----------------------------------------------------------------------------
  // | Protected Properties
  // ----------------------------------------------------------------------------
  protected CallbackContext CallbackContext;

  // ----------------------------------------------------------------------------
  // | Private Properties
  // ----------------------------------------------------------------------------
  private static final int RC_BARCODE_CAPTURE = 9001;

  // ----------------------------------------------------------------------------
  // |  Public Functions
  // ----------------------------------------------------------------------------
  @Override
  public boolean execute(String p_Action, JSONArray p_Args, CallbackContext p_CallbackContext) throws JSONException {
    Log.d("AndroidScanner", "execute called. action=" + p_Action + ", argsCount=" + (p_Args != null ? p_Args.length() : -1));
    CallbackContext = p_CallbackContext;

    if (p_Action.equals("startScan")) {
      sendJsLog("debug", "startScan invoked from JavaScript.");
      cordova.getActivity().runOnUiThread(new Runnable() {
        public void run() {
          openNewActivity(p_Args);
        }
      });
      return true;
    }

    return false;
  }

  @Override
  public void onActivityResult(int p_RequestCode, int p_ResultCode, Intent p_Data) {
    super.onActivityResult(p_RequestCode, p_ResultCode, p_Data);
    Log.d("AndroidScanner", "onActivityResult requestCode=" + p_RequestCode + ", resultCode=" + p_ResultCode + ", hasData=" + (p_Data != null));
    sendJsLog("debug", "onActivityResult requestCode=" + p_RequestCode + ", resultCode=" + p_ResultCode + ", hasData=" + (p_Data != null));

    if (p_RequestCode == RC_BARCODE_CAPTURE) {
      if (CallbackContext == null) {
        Log.e("AndroidScanner", "CallbackContext is null in onActivityResult; cannot return result to JS.");
        return;
      }

      if (p_ResultCode == CommonStatusCodes.SUCCESS) {
        if (p_Data != null) {
          String barcode = p_Data.getStringExtra(BarcodeCaptureActivity.BarcodeValue);
          JSONArray result = new JSONArray();
          result.put(barcode);
          result.put("");
          result.put("");
          CallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));

          Log.d("AndroidScanner", "Barcode read: " + barcode);
          sendJsLog("debug", "Barcode read successfully.");
        } else {
          Log.w("AndroidScanner", "SUCCESS result received but Intent data was null.");
          sendJsLog("warn", "Scanner returned SUCCESS but no data payload was provided.");
        }
      } else {
        String err = p_Data != null ? p_Data.getStringExtra("err") : "No scan data received";
        Log.w("AndroidScanner", "Scanner returned error result. err=" + err);
        sendJsLog("warn", "Scanner returned error result: " + err);
        JSONArray result = new JSONArray();
        result.put(err);
        result.put("");
        result.put("");
        CallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, result));
      }
    }
  }

  @Override
  public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
    CallbackContext = callbackContext;
  }

  // ----------------------------------------------------------------------------
  // |  Protected Functions
  // ----------------------------------------------------------------------------

  // ----------------------------------------------------------------------------
  // |  Private Functions
  // ----------------------------------------------------------------------------
  private void openNewActivity(JSONArray args) {
    int detectionTypes = args.optInt(0, 1234);
    double viewFinderWidth = args.optDouble(1, .5);
    double viewFinderHeight = args.optDouble(2, .7);

    Log.d("AndroidScanner", "Opening SecondaryActivity with DetectionTypes=" + detectionTypes + ", ViewFinderWidth=" + viewFinderWidth + ", ViewFinderHeight=" + viewFinderHeight);
    sendJsLog("debug", "Opening scanner activity with DetectionTypes=" + detectionTypes + ", ViewFinderWidth=" + viewFinderWidth + ", ViewFinderHeight=" + viewFinderHeight);

    Intent intent = new Intent(cordova.getActivity(), SecondaryActivity.class);
    intent.putExtra("DetectionTypes", detectionTypes);
    intent.putExtra("ViewFinderWidth", viewFinderWidth);
    intent.putExtra("ViewFinderHeight", viewFinderHeight);

    this.cordova.setActivityResultCallback(this);
    this.cordova.startActivityForResult(this, intent, RC_BARCODE_CAPTURE);
  }

  private void sendJsLog(String p_Level, String p_Message) {
    if (CallbackContext == null) {
      return;
    }

    try {
      JSONObject payload = new JSONObject();
      payload.put("__type", "log");
      payload.put("source", "android");
      payload.put("level", p_Level);
      payload.put("message", p_Message);

      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, payload);
      pluginResult.setKeepCallback(true);
      CallbackContext.sendPluginResult(pluginResult);
    } catch (JSONException e) {
      Log.e("AndroidScanner", "Failed to send JS log payload.", e);
    }
  }
}

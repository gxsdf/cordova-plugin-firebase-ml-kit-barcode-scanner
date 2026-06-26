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
    CallbackContext = p_CallbackContext;

    if (p_Action.equals("startScan")) {
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

    if (p_RequestCode == RC_BARCODE_CAPTURE) {
      if (p_ResultCode == CommonStatusCodes.SUCCESS) {
        if (p_Data != null) {
          String barcode = p_Data.getStringExtra(BarcodeCaptureActivity.BarcodeValue);
          JSONArray result = new JSONArray();
          result.put(barcode);
          result.put("");
          result.put("");
          CallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));

          Log.d("AndroidScanner", "Barcode read: " + barcode);
        }
      } else {
        String err = p_Data != null ? p_Data.getStringExtra("err") : "No scan data received";
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
    Intent intent = new Intent(cordova.getActivity(), SecondaryActivity.class);
    intent.putExtra("DetectionTypes", args.optInt(0, 1234));
    intent.putExtra("ViewFinderWidth", args.optDouble(1, .5));
    intent.putExtra("ViewFinderHeight", args.optDouble(2, .7));

    this.cordova.setActivityResultCallback(this);
    this.cordova.startActivityForResult(this, intent, RC_BARCODE_CAPTURE);
  }
}

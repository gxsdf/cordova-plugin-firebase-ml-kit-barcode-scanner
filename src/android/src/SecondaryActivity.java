package tl.cordova.plugin.firebase.mlkit.barcode.scanner;

// ----------------------------------------------------------------------------
// |  Android Imports
// ----------------------------------------------------------------------------
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.google.android.gms.common.api.CommonStatusCodes;

public class SecondaryActivity extends Activity implements View.OnClickListener {
  // ----------------------------------------------------------------------------
  // | Public Properties
  // ----------------------------------------------------------------------------
  public static final String BarcodeValue = "FirebaseVisionBarcode";

  // ----------------------------------------------------------------------------
  // | Protected Properties
  // ----------------------------------------------------------------------------

  // ----------------------------------------------------------------------------
  // | Private Properties
  // ----------------------------------------------------------------------------
  private static final int    RC_BARCODE_CAPTURE = 9001         ;
  private static final String TAG                = "BarcodeMain";

  // private CompoundButton autoFocus    ;
  // private CompoundButton useFlash     ;
  // private TextView       statusMessage;
  // private TextView       barcodeValue ;

  // ----------------------------------------------------------------------------
  // |  Public Functions
  // ----------------------------------------------------------------------------
  @Override
  public void onClick(View p_View) {
    if (p_View.getId() == getResources().getIdentifier("read_barcode", "id", getPackageName())) {
      Log.d(TAG, "Read barcode button clicked. Launching BarcodeCaptureActivity.");
      // launch barcode activity.
      Intent intent = new Intent(this, BarcodeCaptureActivity.class);

      startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }
  }

  // ----------------------------------------------------------------------------
  // |  Protected Functions
  // ----------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle p_SavedInstanceState) {
    super.onCreate(p_SavedInstanceState);
    Log.d(TAG, "SecondaryActivity created. savedInstanceState=" + (p_SavedInstanceState != null));

    int detectionTypes = getIntent().getIntExtra("DetectionTypes", 1234);
    double viewFinderWidth = getIntent().getDoubleExtra("ViewFinderWidth", .5);
    double viewFinderHeight = getIntent().getDoubleExtra("ViewFinderHeight", .7);
    Log.d(TAG, "Incoming scan config: DetectionTypes=" + detectionTypes + ", ViewFinderWidth=" + viewFinderWidth + ", ViewFinderHeight=" + viewFinderHeight);

    int layoutId = getResources().getIdentifier("activity_barcode_scanner", "layout", getPackageName());
    if (layoutId == 0) {
      // Cordova Android 15 merges plugin resources from activity_main.xml in this plugin.
      layoutId = getResources().getIdentifier("activity_main", "layout", getPackageName());
    }

    if (layoutId == 0) {
      Log.e(TAG, "Missing scanner layout resource. Expected activity_barcode_scanner or activity_main.");
      setResult(CommonStatusCodes.ERROR);
      finish();
      return;
    }

    Log.d(TAG, "Using layout resource id=" + layoutId);
    setContentView(layoutId);

    View readBarcodeButton = findViewById(getResources().getIdentifier("read_barcode", "id", getPackageName()));
    if (readBarcodeButton != null) {
      readBarcodeButton.setOnClickListener(this);
    } else {
      Log.w(TAG, "read_barcode view not found in selected layout.");
    }

    Intent intent = new Intent(this, BarcodeCaptureActivity.class);

    intent.putExtra("DetectionTypes", detectionTypes);
    intent.putExtra("ViewFinderWidth", viewFinderWidth);
    intent.putExtra("ViewFinderHeight", viewFinderHeight);

    Log.d(TAG, "Starting BarcodeCaptureActivity for result with RC=" + RC_BARCODE_CAPTURE);

    startActivityForResult(intent, RC_BARCODE_CAPTURE);
  }

  @Override
  protected void onActivityResult(int p_RequestCode, int p_ResultCode, Intent p_Data) {
    Log.d(TAG, "onActivityResult requestCode=" + p_RequestCode + ", resultCode=" + p_ResultCode + ", hasData=" + (p_Data != null));
    if (p_RequestCode == RC_BARCODE_CAPTURE) {
      Intent d = new Intent();
      if (p_ResultCode == CommonStatusCodes.SUCCESS) {
        if (p_Data != null) {
          String barcode = p_Data.getStringExtra(BarcodeCaptureActivity.BarcodeValue);
          Log.d(TAG, "Barcode capture success. barcodeNull=" + (barcode == null));
          d.putExtra(BarcodeValue, barcode);
          setResult(CommonStatusCodes.SUCCESS, p_Data);
        } else {
          Log.w(TAG, "Barcode capture returned SUCCESS but Intent data was null.");
          d.putExtra("err", "USER_CANCELLED");
          setResult(CommonStatusCodes.ERROR, d);
        }
      } else {
        Log.w(TAG, "Barcode capture finished with non-success status=" + p_ResultCode);
        d.putExtra("err", "There was an error with the barcode reader.");
        setResult(CommonStatusCodes.ERROR, d);
      }
      finish();
    } else {
      super.onActivityResult(p_RequestCode, p_ResultCode, p_Data);
    }
  }

  // ----------------------------------------------------------------------------
  // |  Private Functions
  // ----------------------------------------------------------------------------
}

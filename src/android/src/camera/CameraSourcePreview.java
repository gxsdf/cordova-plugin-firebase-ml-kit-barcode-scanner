package tl.cordova.plugin.firebase.mlkit.barcode.scanner.camera;

// ----------------------------------------------------------------------------
// |  Android Imports
// ----------------------------------------------------------------------------
import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import androidx.annotation.RequiresPermission;

// ----------------------------------------------------------------------------
// |  Google Imports
// ----------------------------------------------------------------------------
import com.google.android.gms.common.images.Size;

// ----------------------------------------------------------------------------
// |  Java Imports
// ----------------------------------------------------------------------------
import java.io.IOException;

public class CameraSourcePreview extends ViewGroup {
  // ----------------------------------------------------------------------------
  // | Public Properties
  // ----------------------------------------------------------------------------
  public double ViewFinderWidth  = .5;
  public double ViewFinderHeight = .7;

  // ----------------------------------------------------------------------------
  // | Private Properties
  // ----------------------------------------------------------------------------
  private static final String TAG = "CameraSourcePreview";

  private Context        _Context                 ;
  private SurfaceView    _SurfaceView             ;
  private View           _ViewFinderView          ;
  private View           _VerticalLine            ;
  private View           _HorizontalLine          ;
  private Button         _TorchButton             ;
  private boolean        _StartRequested          ;
  private boolean        _SurfaceAvailable        ;
  private CameraSource2  _CameraSource            ;
  private boolean        _FlashState       = false;
  private GraphicOverlay _Overlay                 ;

  public CameraSourcePreview(Context p_Context, AttributeSet p_AttributeSet) {
    super(p_Context, p_AttributeSet);
    _Context = p_Context;
    _StartRequested = false;
    _SurfaceAvailable = false;

    _SurfaceView = new SurfaceView(p_Context);
    _SurfaceView.getHolder().addCallback(new SurfaceCallback());
    addView(_SurfaceView);

    _HorizontalLine = new View(_Context);
    _HorizontalLine.setBackgroundColor(Color.argb(170, 255, 0, 0));
    addView(_HorizontalLine);

    _VerticalLine = new View(_Context);
    _VerticalLine.setBackgroundColor(Color.argb(170, 255, 0, 0));
    addView(_VerticalLine);

    _TorchButton = new Button(_Context);
    _TorchButton.setBackgroundResource(getResources().getIdentifier("torch_inactive", "drawable", _Context.getPackageName()));
    _TorchButton.layout(0, 0, dpToPx(45), dpToPx(45));
    _TorchButton.setMaxWidth(50);
    _TorchButton.setRotation(90);

    _TorchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          _CameraSource
              .setFlashMode(!_FlashState ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
          _FlashState = !_FlashState;
          _TorchButton.setBackgroundResource(getResources()
              .getIdentifier(_FlashState ? "torch_active" : "torch_inactive", "drawable", _Context.getPackageName()));
        } catch (Exception e) {

        }
      }
    });
    addView(_TorchButton);
  }

  // ----------------------------------------------------------------------------
  // |  Public Functions
  // ----------------------------------------------------------------------------
  public int dpToPx(int p_Dot) {
    float density = _Context.getResources().getDisplayMetrics().density;
    return Math.round((float) p_Dot * density);
  }

  @RequiresPermission(Manifest.permission.CAMERA)
  public void start(CameraSource2 p_CameraSource) throws IOException, SecurityException {
    if (p_CameraSource == null) {
      stop();
    }

    _CameraSource = p_CameraSource;

    if (_CameraSource != null) {
      _StartRequested = true;
      startIfReady();
    }
  }

  @RequiresPermission(Manifest.permission.CAMERA)
  public void start(CameraSource2 p_CameraSource, GraphicOverlay overlay) throws IOException, SecurityException {
    _Overlay = overlay;
    start(p_CameraSource);
  }

  public void stop() {
    if (_CameraSource != null) {
      _CameraSource.stop();
    }
  }

  public void release() {
    if (_CameraSource != null) {
      _CameraSource.release();
      _CameraSource = null;
    }
  }

  // ----------------------------------------------------------------------------
  // |  Protected Functions
  // ----------------------------------------------------------------------------

  // ----------------------------------------------------------------------------
  // |  Private Functions
  // ----------------------------------------------------------------------------  
  @RequiresPermission(Manifest.permission.CAMERA)
  private void startIfReady() throws IOException, SecurityException {
    if (_StartRequested && _SurfaceAvailable) {
      _CameraSource.start(_SurfaceView.getHolder());
      if (_Overlay != null) {
        Size size = _CameraSource.getPreviewSize();
        int min = Math.min(size.getWidth(), size.getHeight());
        int max = Math.max(size.getWidth(), size.getHeight());
        if (isPortraitMode()) {
          _Overlay.setCameraInfo(min, max, _CameraSource.getCameraFacing());
        } else {
          _Overlay.setCameraInfo(max, min, _CameraSource.getCameraFacing());
        }
        _Overlay.clear();
      }
      _StartRequested = false;
    }
  }

  @Override
  protected void onLayout(boolean p_Changed, int p_Left, int p_Top, int p_Right, int p_Bottom) {
    int width = 320;
    int height = 240;
    if (_CameraSource != null) {
      Size size = _CameraSource.getPreviewSize();
      if (size != null) {
        width = size.getWidth();
        height = size.getHeight();
      }
    }

    if (isPortraitMode()) {
      int tmp = width;
      width = height;
      height = tmp;
    }

    final int layoutWidth = p_Right - p_Left;
    final int layoutHeight = p_Bottom - p_Top;

    int childWidth;
    int childHeight;
    int leftOffset;
    int topOffset;

    float previewAspectRatio = (float) width / (float) height;
    float layoutAspectRatio = (float) layoutWidth / (float) layoutHeight;

    if (previewAspectRatio > layoutAspectRatio) {
      // Preview is relatively wider than container: fit height and center-crop width.
      childHeight = layoutHeight;
      childWidth = Math.round(layoutHeight * previewAspectRatio);
      leftOffset = (layoutWidth - childWidth) / 2;
      topOffset = 0;
    } else {
      // Preview is relatively taller than container: fit width and center-crop height.
      childWidth = layoutWidth;
      childHeight = Math.round(layoutWidth / previewAspectRatio);
      leftOffset = 0;
      topOffset = (layoutHeight - childHeight) / 2;
    }

    _SurfaceView.layout(leftOffset, topOffset, leftOffset + childWidth, topOffset + childHeight);

    int lineThickness = Math.max(2, dpToPx(2));

    int horizontalTop = (layoutHeight / 2) - (lineThickness / 2);
    _HorizontalLine.layout(0, horizontalTop, layoutWidth, horizontalTop + lineThickness);

    int verticalLeft = (layoutWidth / 2) - (lineThickness / 2);
    _VerticalLine.layout(verticalLeft, 0, verticalLeft + lineThickness, layoutHeight);

    int buttonSize = dpToPx(45);
    int torchRightMargin = dpToPx(16);
    int torchBottomMargin = dpToPx(16);

    int insetRight = 0;
    int insetBottom = 0;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      WindowInsets windowInsets = getRootWindowInsets();
      if (windowInsets != null) {
        insetRight = windowInsets.getStableInsetRight();
        insetBottom = windowInsets.getStableInsetBottom();
      }
    }

    int torchLeft = layoutWidth - buttonSize - torchRightMargin - insetRight;
    int torchTop = layoutHeight - buttonSize - torchBottomMargin - insetBottom;
    torchLeft = Math.max(0, torchLeft);
    torchTop = Math.max(0, torchTop);

    _TorchButton.layout(torchLeft, torchTop, torchLeft + buttonSize, torchTop + buttonSize);
    _TorchButton.bringToFront();

    try {
      startIfReady();
    } catch (SecurityException se) {
      Log.e(TAG, "Do not have permission to start the camera", se);
    } catch (IOException e) {
      Log.e(TAG, "Could not start camera source.", e);
    }
  }

  private boolean isPortraitMode() {
    int orientation = _Context.getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
      return false;
    }
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      return true;
    }

    Log.d(TAG, "isPortraitMode returning false by default");
    return false;
  }

  // ----------------------------------------------------------------------------
  // |  Helper classes
  // ----------------------------------------------------------------------------  
  private class SurfaceCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceCreated(SurfaceHolder surface) {
      _SurfaceAvailable = true;
      try {
        startIfReady();
      } catch (SecurityException se) {
        Log.e(TAG, "Do not have permission to start the camera", se);
      } catch (IOException e) {
        Log.e(TAG, "Could not start camera source.", e);
      }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surface) {
      _SurfaceAvailable = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
  }
}

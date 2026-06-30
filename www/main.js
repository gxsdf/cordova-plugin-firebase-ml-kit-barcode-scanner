// ----------------------------------------------------------------------------
// |  Imports
// ----------------------------------------------------------------------------
var exec = require('cordova/exec');

// ----------------------------------------------------------------------------
// |  Public interface
// ----------------------------------------------------------------------------
exports.getDefaultSettings = function() {
  return getDefaultSettings();
};

exports.startScanning = function (p_OnSuccess, p_OnError, p_Settings) {
  return startScanning(p_OnSuccess, p_OnError, p_Settings);
};

// ----------------------------------------------------------------------------
// |  Functions
// ----------------------------------------------------------------------------
function getDefaultSettings() {
  var settings = {
    barcodeTypes: {
      Aztec     : true,
      CodaBar   : true,
      Code39    : true,
      Code93    : true,
      Code128   : true,
      DataMatrix: true,
      EAN8      : true,
      EAN13     : true,
      ITF       : true,
      PDF417    : true,
      QRCode    : true,
      UPCA      : true,
      UPCE      : true,
    },
    detectorSize: {
        width : .5,
        height: .7
    }
  }; 

  return settings;
}

function startScanning(p_OnSuccess, p_OnError, p_Settings) {
  if (!p_Settings) {
    p_OnError("p_Settings can't be undefined. Use getDefaultSettings() to get a new settings object");
    return;
  }

  var enabledDetectorTypes = 0; //The type of detectors which are neabled are represented by an integer;

  var detectionTypes = {
    Code128   : 1   ,
    Code39    : 2   ,
    Code93    : 4   ,
    CodaBar   : 8   ,
    DataMatrix: 16  ,
    EAN13     : 32  ,
    EAN8      : 64  ,
    ITF       : 128 ,
    QRCode    : 256 ,
    UPCA      : 512 ,
    UPCE      : 1024,
    PDF417    : 2048,
    Aztec     : 4096,
  };

  for (var key in p_Settings.barcodeTypes) {
    if (p_Settings.barcodeTypes[key] == true) {
      enabledDetectorTypes += detectionTypes[key];
    }
  }

  var settingArray = [
    enabledDetectorTypes,
    p_Settings.detectorSize.width,
    p_Settings.detectorSize.height
  ];

  var handleNativeLog = function(p_Payload) {
    if (!p_Payload || p_Payload.__type !== 'log') {
      return false;
    }

    var prefix = '[cordova-plugin-firebase-ml-kit-barcode-scanner][' + (p_Payload.source || 'native') + '] ';
    var message = prefix + (p_Payload.message || '');

    if (p_Payload.level === 'error' && console.error) {
      console.error(message);
    } else if (p_Payload.level === 'warn' && console.warn) {
      console.warn(message);
    } else {
      console.log(message);
    }

    return true;
  };

  
  exec(p_Result => {
    if (handleNativeLog(p_Result)) {
      return;
    }

    if (Array.isArray(p_Result)) {
      p_OnSuccess(p_Result[0]);
      return;
    }

    p_OnSuccess(p_Result);
  }, p_Error => {
    if (handleNativeLog(p_Error)) {
      return;
    }

    p_OnError(p_Error);
  }, 'cordova-plugin-firebase-ml-kit-barcode-scanner','startScan',settingArray);
};
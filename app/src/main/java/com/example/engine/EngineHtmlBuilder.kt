package com.example.engine

import android.content.Context
import com.example.model.RenderConfig

object EngineHtmlBuilder {

    private var cachedMuxerJs: String? = null

    fun getMuxerScript(context: Context): String {
        cachedMuxerJs?.let { return it }
        return try {
            val script = context.assets.open("mp4-muxer.min.js").bufferedReader().use { it.readText() }
            cachedMuxerJs = script
            script
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun buildHtml(
        context: Context,
        userCode: String,
        renderConfig: RenderConfig
    ): String {
        val rawMuxerScript = getMuxerScript(context)
        val muxerScript = rawMuxerScript.replace("</script>", "<\\/script>", ignoreCase = true)
        val targetW = renderConfig.resolution.width
        val targetH = renderConfig.resolution.height
        val fps = renderConfig.fps
        val duration = renderConfig.durationSeconds
        val bitrate = renderConfig.safeBitrateBps.coerceIn(100_000L, 50_000_000L)

        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>CodeMotion Engine</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    html, body {
      width: 100%;
      height: 100%;
      overflow: hidden;
      background: #000;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }
    #viewportWrap {
      position: relative;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      background: #000;
    }
    #stageScaler {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: ${targetW}px;
      height: ${targetH}px;
      transform-origin: center center;
    }
    #stageScaler iframe {
      width: 100%;
      height: 100%;
      border: none;
      display: block;
      background: #000;
    }
    #renderBanner {
      position: absolute;
      top: 8px;
      left: 8px;
      background: rgba(2, 132, 199, 0.9);
      backdrop-filter: blur(4px);
      color: #fff;
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0.5px;
      display: none;
      z-index: 100;
      border: 1px solid rgba(255, 255, 255, 0.3);
    }
  </style>
  <script>
    $muxerScript
  </script>
</head>
<body>

<div id="viewportWrap">
  <div id="renderBanner">🔴 RENDERING PREVIEW...</div>
  <div id="stageScaler">
    <iframe id="liveFrame" title="Stage" sandbox="allow-scripts allow-same-origin"></iframe>
  </div>
</div>

<script>
const TARGET_W = $targetW;
const TARGET_H = $targetH;
const FPS = $fps;
const DURATION = $duration;
const BITRATE = $bitrate;

let userCodeRaw = ${escapeJsString(userCode)};

function postProgress(current, total, progress, status) {
  if (window.AndroidBridge && window.AndroidBridge.onProgress) {
    window.AndroidBridge.onProgress(current, total, progress, status);
  }
}

function postError(err) {
  if (window.AndroidBridge && window.AndroidBridge.onError) {
    window.AndroidBridge.onError(String(err));
  } else {
    console.error(err);
  }
}

function fitViewport() {
  const wrap = document.getElementById('viewportWrap');
  const stage = document.getElementById('stageScaler');
  if (!wrap || !stage) return;
  const cw = wrap.clientWidth || window.innerWidth;
  const ch = wrap.clientHeight || window.innerHeight;
  if (cw > 0 && ch > 0) {
    const scale = Math.min(cw / TARGET_W, ch / TARGET_H);
    stage.style.transform = 'translate(-50%, -50%) scale(' + scale + ')';
  }
}

window.addEventListener('resize', fitViewport);

const HARNESS_SCRIPT_TEXT = `
(function () {
  var OriginalDate = Date;
  var origPerfNow = (window.performance && window.performance.now) ? window.performance.now.bind(window.performance) : function () { return Date.now(); };
  var EPOCH = 1700000000000;
  var virtualTime = 0;
  var nextId = 1;
  var rafCallbacks = new Map();
  var isControlled = false;

  function FakeDate() {
    var args = Array.prototype.slice.call(arguments);
    if (!(this instanceof FakeDate)) return new OriginalDate(EPOCH + virtualTime).toString();
    if (args.length === 0) return new OriginalDate(EPOCH + virtualTime);
    return new (Function.prototype.bind.apply(OriginalDate, [null].concat(args)))();
  }
  FakeDate.now = function () { return EPOCH + virtualTime; };
  FakeDate.parse = OriginalDate.parse;
  FakeDate.UTC = OriginalDate.UTC;
  FakeDate.prototype = OriginalDate.prototype;

  try {
    Object.defineProperty(performance, 'now', {
      value: function () { return isControlled ? virtualTime : origPerfNow(); },
      writable: true, configurable: true
    });
  } catch (e) {}

  var origRAF = window.requestAnimationFrame ? window.requestAnimationFrame.bind(window) : null;
  window.requestAnimationFrame = function (cb) {
    if (!isControlled && origRAF) return origRAF(cb);
    var id = nextId++;
    rafCallbacks.set(id, cb);
    return id;
  };

  window.cancelAnimationFrame = function (id) {
    if (!isControlled && window.cancelAnimationFrame) {
      try { window.cancelAnimationFrame(id); } catch(e) {}
    }
    rafCallbacks.delete(id);
  };

  try {
    Object.defineProperty(window, 'devicePixelRatio', {
      get: function () { return 1; }, configurable: true
    });
    Object.defineProperty(window, 'innerWidth', {
      get: function () { return ${targetW}; }, configurable: true
    });
    Object.defineProperty(window, 'innerHeight', {
      get: function () { return ${targetH}; }, configurable: true
    });
  } catch (e) {}

  // Auto-preserve WebGL drawing buffer for video recording capture
  try {
    var origGetContext = HTMLCanvasElement.prototype.getContext;
    HTMLCanvasElement.prototype.getContext = function (type, options) {
      if (type === 'webgl' || type === 'webgl2' || type === 'experimental-webgl') {
        options = options || {};
        options.preserveDrawingBuffer = true;
      }
      return origGetContext.call(this, type, options);
    };
  } catch(e) {}

  window.__HARNESS__ = {
    setControlled: function (on) {
      isControlled = on;
      if (on) {
        try { window.Date = FakeDate; } catch (e) {}
      } else {
        try { window.Date = OriginalDate; } catch (e) {}
      }
    },
    step: function (dt) {
      virtualTime += dt;
      var pending = Array.from(rafCallbacks.entries());
      rafCallbacks.clear();
      for (var i = 0; i < pending.length; i++) {
        try { pending[i][1](virtualTime); } catch (e) { console.error('Harness RAF err:', e); }
      }
    },
    getTime: function () { return virtualTime; },
    isControlled: function () { return isControlled; }
  };
})();
`;

function sanitizeJsForScriptTag(src) {
  return String(src == null ? '' : src).replace(/<\/(script)/gi, '<\\/$1');
}

function compileCodeToHtml(code, controlled) {
  code = (code || '').trim();
  
  // 1. Deteksi mode
  const isFullHtml = /<!DOCTYPE\s+html/i.test(code) || /<html[\s>]/i.test(code);
  const isFragment = !isFullHtml && /<(canvas|script|style|div|section|main|svg|body|head)\b/i.test(code);
  const mode = isFullHtml ? 'full-html' : (isFragment ? 'fragment' : 'js-only');

  const cssReset = '<style>' +
    'html,body{margin:0!important;padding:0!important;width:100%!important;height:100%!important;overflow:hidden!important;background:#000!important;}' +
    'canvas{display:block!important;}' +
    '#__cm_err__{display:none;position:fixed;bottom:0;left:0;right:0;background:rgba(225,29,72,0.95);color:#fff;padding:8px 12px;font-family:monospace;font-size:12px;z-index:999999;border-top:1px solid #fda4af;word-break:break-word;white-space:pre-wrap;}' +
    '</style>';

  const harnessScript = '<script>' +
    HARNESS_SCRIPT_TEXT + '\n' +
    'if (window.__HARNESS__) { window.__HARNESS__.setControlled(' + (controlled ? 'true' : 'false') + '); }\n' +
    'window.__HARNESS_READY__ = true;\n' +
    '<\/script>';

  const errHandlerScript = '<script>' +
    'function __showCmErr(msg) {' +
    '  var box = document.getElementById("__cm_err__");' +
    '  if (!box) {' +
    '    box = document.createElement("div");' +
    '    box.id = "__cm_err__";' +
    '    if (document.body) { document.body.appendChild(box); } else { document.documentElement.appendChild(box); }' +
    '  }' +
    '  box.style.display = "block";' +
    '  box.textContent = "⚠️ " + msg;' +
    '  if (window.parent && window.parent.onStageConsole) {' +
    '    window.parent.onStageConsole(String(msg));' +
    '  }' +
    '}' +
    'window.onerror = function(msg, url, line, col, err) {' +
    '  var errText = (line ? "Baris " + line + ": " : "") + msg;' +
    '  __showCmErr(errText);' +
    '};' +
    'window.addEventListener("unhandledrejection", function(e) {' +
    '  var reason = (e && e.reason) ? (e.reason.message || String(e.reason)) : "Unhandled promise rejection";' +
    '  __showCmErr(reason);' +
    '});' +
    '<\/script>';

  const canvasAliasScript = '<script>' +
    '(function() {' +
    '  var findCanvas = function() {' +
    '    return document.querySelector("canvas") || document.getElementById("canvas") || document.getElementById("c");' +
    '  };' +
    '  var origGet = document.getElementById.bind(document);' +
    '  document.getElementById = function(id) {' +
    '    var el = origGet(id);' +
    '    if (!el && (id === "c" || id === "canvas" || id === "stage" || id === "myCanvas")) return findCanvas();' +
    '    return el;' +
    '  };' +
    '  var origQuery = document.querySelector.bind(document);' +
    '  document.querySelector = function(sel) {' +
    '    var el = origQuery(sel);' +
    '    if (!el && (sel === "canvas" || sel === "#c" || sel === "#canvas")) return findCanvas();' +
    '    return el;' +
    '  };' +
    '  var setupGlobals = function() {' +
    '    var c = findCanvas();' +
    '    if (c) {' +
    '      window.canvas = c;' +
    '      window.c = c;' +
    '      window.W = ' + TARGET_W + ';' +
    '      window.H = ' + TARGET_H + ';' +
    '    }' +
    '  };' +
    '  if (document.readyState === "loading") {' +
    '    document.addEventListener("DOMContentLoaded", setupGlobals);' +
    '  } else {' +
    '    setupGlobals();' +
    '  }' +
    '})();' +
    '<\/script>';

  const errorDivHtml = '<div id="__cm_err__"></div>';

  if (mode === 'full-html') {
    let html = code;
    const headBlock = harnessScript + errHandlerScript + cssReset + canvasAliasScript;

    // Inject to <head> if exists, or after <html...>
    if (/<head\b[^>]*>/i.test(html)) {
      html = html.replace(/<head\b[^>]*>/i, function(match) { return match + headBlock; });
    } else if (/<html\b[^>]*>/i.test(html)) {
      html = html.replace(/<html\b[^>]*>/i, function(match) { return match + '<head>' + headBlock + '</head>'; });
    } else {
      html = '<head>' + headBlock + '</head>' + html;
    }

    // Ensure error div in body
    if (/<body\b[^>]*>/i.test(html)) {
      html = html.replace(/<body\b[^>]*>/i, function(match) { return match + errorDivHtml; });
    } else {
      html = html + errorDivHtml;
    }

    // Auto-create canvas if not present
    if (!/<canvas\b/i.test(html)) {
      const defaultCanvas = '<canvas id="c" width="' + TARGET_W + '" height="' + TARGET_H + '"></canvas>';
      if (/<\/body>/i.test(html)) {
        html = html.replace(/<\/body>/i, defaultCanvas + '</body>');
      } else {
        html = html + defaultCanvas;
      }
    }

    return html;
  }

  if (mode === 'fragment') {
    let bodyContent = code;
    if (!/<canvas\b/i.test(bodyContent)) {
      bodyContent = '<canvas id="c" width="' + TARGET_W + '" height="' + TARGET_H + '"></canvas>\n' + bodyContent;
    }

    return '<!DOCTYPE html>' +
      '<html><head><meta charset="utf-8">' +
      harnessScript +
      errHandlerScript +
      cssReset +
      canvasAliasScript +
      '</head><body>' +
      errorDivHtml +
      bodyContent +
      '</body></html>';
  }

  // js-only mode: sanitasi script tag karena kita yang membungkus kode user ke tag <script>
  const safeJsCode = sanitizeJsForScriptTag(code);
  return '<!DOCTYPE html>' +
    '<html><head><meta charset="utf-8">' +
    harnessScript +
    errHandlerScript +
    cssReset +
    '</head><body>' +
    '<canvas id="c" width="' + TARGET_W + '" height="' + TARGET_H + '"></canvas>' +
    errorDivHtml +
    canvasAliasScript +
    '<script>\n' +
    'try {\n' +
    safeJsCode + '\n' +
    '} catch(e) { window.onerror(e && e.message ? e.message : String(e), "", 0, 0, e); }\n' +
    '<\/script>' +
    '</body></html>';
}

function loadLivePreview(code, controlled) {
  const container = document.getElementById('stageScaler');
  if (!container) return;
  
  // Create a brand new clean iframe to completely isolate execution context
  const oldIframe = document.getElementById('liveFrame');
  const newIframe = document.createElement('iframe');
  newIframe.id = 'liveFrame';
  newIframe.title = 'Stage';
  newIframe.setAttribute('sandbox', 'allow-scripts allow-same-origin');
  
  const htmlContent = compileCodeToHtml(code, controlled || false);
  newIframe.srcdoc = htmlContent;
  
  if (oldIframe) {
    oldIframe.replaceWith(newIframe);
  } else {
    container.appendChild(newIframe);
  }
  
  newIframe.onload = function() {
    fitViewport();
    if (window.AndroidBridge && window.AndroidBridge.onPreviewLoaded) {
      window.AndroidBridge.onPreviewLoaded(TARGET_W, TARGET_H);
    }
  };
}

window.onStageConsole = function(msg) {
  if (window.AndroidBridge && window.AndroidBridge.onConsoleLog) {
    window.AndroidBridge.onConsoleLog(msg);
  }
};

loadLivePreview(userCodeRaw, false);
setTimeout(fitViewport, 100);

window.reloadPreviewWithCode = function(newCode) {
  userCodeRaw = newCode;
  loadLivePreview(userCodeRaw, false);
};

window.startVideoRender = async function() {
  const banner = document.getElementById('renderBanner');
  try {
    if (banner) banner.style.display = 'block';
    postProgress(0, DURATION * FPS, 0.0, "Menyiapkan stage render " + TARGET_W + "×" + TARGET_H + "...");

    // Render directly with controlled harness
    loadLivePreview(userCodeRaw, true);
    
    // Wait for stage iframe and harness to be ready
    await new Promise((resolve, reject) => {
      const deadline = Date.now() + 5000;
      const check = () => {
        const iframe = document.getElementById('liveFrame');
        const iwin = iframe && iframe.contentWindow;
        if (iwin && iwin.__HARNESS_READY__) {
          resolve();
        } else if (Date.now() > deadline) {
          reject(new Error("Timeout menunggu harness siap."));
        } else {
          setTimeout(check, 50);
        }
      };
      check();
    });

    const iframe = document.getElementById('liveFrame');
    const iwin = iframe.contentWindow;
    const idoc = iframe.contentDocument;

    if (!iwin.__HARNESS__) throw new Error("Harness deterministik gagal terpasang.");
    const canvas = idoc.querySelector('canvas') || idoc.getElementById('canvas') || idoc.getElementById('c');
    if (!canvas) throw new Error("Elemen <canvas> tidak ditemukan di dalam kode.");

    canvas.width = TARGET_W;
    canvas.height = TARGET_H;

    const totalFrames = Math.round(DURATION * FPS);
    const frameDurationUs = Math.round(1_000_000 / FPS);
    const keyFrameInterval = Math.max(1, Math.round(FPS * 2));

    const MuxerLib = window.Mp4Muxer || window.mp4Muxer;
    if (!MuxerLib || !MuxerLib.Muxer) {
      throw new Error("Pustaka MP4 Muxer belum siap.");
    }

    const muxer = new MuxerLib.Muxer({
      target: new MuxerLib.ArrayBufferTarget(),
      video: {
        codec: 'avc',
        width: TARGET_W,
        height: TARGET_H
      },
      fastStart: 'in-memory'
    });

    if (typeof VideoEncoder === 'undefined') {
      throw new Error("Hardware VideoEncoder tidak tersedia di perangkat ini.");
    }

    function pickCodec(w, h, f) {
      const mbW = Math.ceil(w / 16);
      const mbH = Math.ceil(h / 16);
      const mbPerSec = mbW * mbH * f;
      let level;
      if (mbPerSec <= 108000) level = '1f'; // 3.1
      else if (mbPerSec <= 245760) level = '28'; // 4.0
      else if (mbPerSec <= 522240) level = '2a'; // 4.2
      else if (mbPerSec <= 983040) level = '33'; // 5.1
      else level = '34'; // 5.2
      return 'avc1.6400' + level;
    }

    let config = {
      codec: pickCodec(TARGET_W, TARGET_H, FPS),
      width: TARGET_W,
      height: TARGET_H,
      bitrate: Math.min(Math.max(Number(BITRATE) || 8000000, 100000), 50000000),
      framerate: FPS,
      latencyMode: 'quality'
    };

    // Find supported codec config across high/main/baseline
    let isSupported = false;
    const candidates = [
      config.codec,
      'avc1.64002a',
      'avc1.4d002a',
      'avc1.42001f',
      'avc1.4d0034',
      'avc1.640034'
    ];

    for (const cand of candidates) {
      try {
        config.codec = cand;
        const support = await VideoEncoder.isConfigSupported(config);
        if (support && support.supported) {
          isSupported = true;
          break;
        }
      } catch(e) {}
    }

    if (!isSupported) {
      config.latencyMode = 'realtime';
      config.codec = 'avc1.42001f';
      const support = await VideoEncoder.isConfigSupported(config);
      if (!support || !support.supported) {
        throw new Error("Resolusi " + TARGET_W + "×" + TARGET_H + " (" + FPS + "fps) tidak didukung oleh encoder hardware perangkat ini.");
      }
    }

    let encoderError = null;
    const encoder = new VideoEncoder({
      output: (chunk, meta) => {
        try { muxer.addVideoChunk(chunk, meta); } catch (e) { encoderError = e; }
      },
      error: (e) => { encoderError = e; postError("Encoder error: " + (e.message || e)); }
    });
    encoder.configure(config);

    const harness = iwin.__HARNESS__;
    const dt = 1000 / FPS;
    harness.step(0);

    postProgress(0, totalFrames, 0.0, "Merender frame 0/" + totalFrames + "...");

    for (let i = 0; i < totalFrames; i++) {
      if (encoderError) throw encoderError;

      harness.step(dt);

      const frame = new VideoFrame(canvas, {
        timestamp: Math.round(i * 1_000_000 / FPS),
        duration: frameDurationUs
      });

      encoder.encode(frame, { keyFrame: (i % keyFrameInterval === 0) });
      frame.close();

      if (i % 3 === 0 || i === totalFrames - 1) {
        const prog = (i + 1) / totalFrames;
        const pct = Math.round(prog * 100);
        postProgress(i + 1, totalFrames, prog, "Merender frame " + (i + 1) + "/" + totalFrames + " (" + pct + "%)");
        await new Promise(r => setTimeout(r, 0));
      }

      // Keep encode queue bounded
      let waitCount = 0;
      while (encoder.encodeQueueSize > 2 && waitCount < 50) {
        await new Promise(r => setTimeout(r, 10));
        waitCount++;
        if (encoderError) throw encoderError;
      }
    }

    postProgress(totalFrames, totalFrames, 0.98, "Menyelesaikan MP4 dan menyatukan trek...");
    await encoder.flush();
    if (encoderError) throw encoderError;
    muxer.finalize();

    const buffer = muxer.target.buffer;
    if (!buffer || buffer.byteLength === 0) {
      throw new Error("Hasil encoding kosong.");
    }

    const uint8 = new Uint8Array(buffer);
    const totalBytes = uint8.byteLength;
    const filename = "codemotion_" + TARGET_W + "x" + TARGET_H + "_" + Date.now();

    if (window.AndroidBridge && window.AndroidBridge.onStartStream) {
      window.AndroidBridge.onStartStream(filename, TARGET_W, TARGET_H, DURATION, totalBytes);
    }

    const CHUNK_SIZE = 128 * 1024;
    for (let offset = 0; offset < totalBytes; offset += CHUNK_SIZE) {
      const slice = uint8.subarray(offset, Math.min(offset + CHUNK_SIZE, totalBytes));
      let binary = '';
      const subStep = 8192;
      for (let s = 0; s < slice.length; s += subStep) {
        const sub = slice.subarray(s, Math.min(s + subStep, slice.length));
        binary += String.fromCharCode.apply(null, sub);
      }
      const b64 = btoa(binary);
      if (window.AndroidBridge && window.AndroidBridge.onChunkStream) {
        window.AndroidBridge.onChunkStream(b64);
      }
      if (offset % (CHUNK_SIZE * 4) === 0) {
        await new Promise(r => setTimeout(r, 0));
      }
    }

    if (window.AndroidBridge && window.AndroidBridge.onEndStream) {
      window.AndroidBridge.onEndStream(totalBytes);
    }

    // Restore live interactive preview
    loadLivePreview(userCodeRaw, false);

  } catch (err) {
    postError("Gagal merender video: " + (err.message || err));
    loadLivePreview(userCodeRaw, false);
  } finally {
    if (banner) banner.style.display = 'none';
  }
};
</script>
</body>
</html>
        """.trimIndent()
    }

    private fun escapeJsString(str: String): String {
        val escaped = str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("</script>", "<\\/script>")
        return "\"$escaped\""
    }
}

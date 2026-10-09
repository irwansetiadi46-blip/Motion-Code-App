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
        val muxerScript = getMuxerScript(context)
        val targetW = renderConfig.resolution.width
        val targetH = renderConfig.resolution.height
        val fps = renderConfig.fps
        val duration = renderConfig.durationSeconds
        val bitrate = renderConfig.safeBitrateBps

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
    <iframe id="liveFrame" title="Stage"></iframe>
  </div>
</div>

<script>
const HARNESS_SOURCE = `
(function () {
  var OriginalDate = Date;
  var EPOCH = 1700000000000;
  var virtualTime = 0;
  var nextId = 1;
  var callbacks = new Map();
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
      value: function () { return virtualTime; },
      writable: true, configurable: true
    });
  } catch (e) {
    try { performance.now = function () { return virtualTime; }; } catch (e2) {}
  }

  var origRAF = window.requestAnimationFrame ? window.requestAnimationFrame.bind(window) : null;
  window.requestAnimationFrame = function (cb) {
    if (!isControlled && origRAF) return origRAF(cb);
    var id = nextId++;
    callbacks.set(id, cb);
    return id;
  };

  window.cancelAnimationFrame = function (id) {
    callbacks.delete(id);
  };

  try {
    Object.defineProperty(window, 'devicePixelRatio', {
      get: function () { return 1; }, configurable: true
    });
  } catch (e) {}

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
      var pending = Array.from(callbacks.entries());
      callbacks.clear();
      for (var i = 0; i < pending.length; i++) {
        try { pending[i][1](virtualTime); } catch (e) { console.error('Harness RAF err:', e); }
      }
    },
    getTime: function () { return virtualTime; },
    isControlled: function () { return isControlled; }
  };
})();
`;

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
  const scale = Math.min(cw / TARGET_W, ch / TARGET_H);
  stage.style.transform = 'translate(-50%, -50%) scale(' + scale + ')';
}

window.addEventListener('resize', fitViewport);

function compileCodeToHtml(code, controlled) {
  code = (code || '').trim();
  const isHtml = /<(html|head|body|canvas|script|style|div|svg)\b/i.test(code);

  if (isHtml) {
    // User provided full or partial HTML document
    const parser = new DOMParser();
    let doc;
    try {
      doc = parser.parseFromString(code, 'text/html');
    } catch(e) {
      doc = parser.parseFromString('<!DOCTYPE html><html><head></head><body></body></html>', 'text/html');
    }
    
    let head = doc.head;
    if (!head) {
      head = doc.createElement('head');
      doc.documentElement.insertBefore(head, doc.documentElement.firstChild);
    }
    
    // Inject deterministic harness
    const s = doc.createElement('script');
    s.textContent = HARNESS_SOURCE + '\nif(window.__HARNESS__) window.__HARNESS__.setControlled(' + (controlled ? 'true' : 'false') + ');';
    head.insertBefore(s, head.firstChild);

    // Inject base style
    const st = doc.createElement('style');
    st.textContent = 'html,body{margin:0!important;padding:0!important;overflow:hidden!important;width:100%!important;height:100%!important;background:#000!important}canvas{display:block!important;width:100%!important;height:100%!important;}';
    head.appendChild(st);

    // Ensure a canvas exists if user forgot to declare <canvas>
    if (!doc.querySelector('canvas')) {
      const c = doc.createElement('canvas');
      c.id = 'c';
      c.width = TARGET_W;
      c.height = TARGET_H;
      (doc.body || doc.documentElement).appendChild(c);
    }

    return '<!DOCTYPE html>\n' + doc.documentElement.outerHTML;
  } else {
    // User provided pure JavaScript code (direct canvas animation logic)
    return '<!DOCTYPE html>' +
      '<html><head><meta charset="utf-8">' +
      '<style>' +
      'html,body{margin:0!important;padding:0!important;overflow:hidden!important;width:100%!important;height:100%!important;background:#000!important}' +
      'canvas{display:block!important;width:100%!important;height:100%!important;}' +
      '#errBox{display:none;position:fixed;bottom:0;left:0;right:0;background:rgba(220,38,38,0.9);color:#fff;padding:8px 12px;font-family:monospace;font-size:12px;z-index:9999;border-top:1px solid #fca5a5;}' +
      '</style>' +
      '<script>' + HARNESS_SOURCE + '<\/script>' +
      '</head><body>' +
      '<canvas id="c" width="' + TARGET_W + '" height="' + TARGET_H + '"></canvas>' +
      '<div id="errBox"></div>' +
      '<script>' +
      'window.onerror = function(msg, url, line, col, err) {' +
      '  var b = document.getElementById("errBox");' +
      '  if (b) { b.style.display = "block"; b.textContent = "⚠️ Error (Baris " + line + "): " + msg; }' +
      '};' +
      'if(window.__HARNESS__) window.__HARNESS__.setControlled(' + (controlled ? 'true' : 'false') + ');\n' +
      'var c = document.getElementById("c");\n' +
      'var canvas = c;\n' +
      'var W = ' + TARGET_W + ';\n' +
      'var H = ' + TARGET_H + ';\n' +
      'try {\n' +
      code + '\n' +
      '} catch(e) { window.onerror(e.message || String(e), "", 0, 0, e); }\n' +
      '<\/script>' +
      '</body></html>';
  }
}

function loadLivePreview(code, controlled) {
  const iframe = document.getElementById('liveFrame');
  const doc = iframe.contentDocument || iframe.contentWindow.document;
  doc.open();
  doc.write(compileCodeToHtml(code, controlled || false));
  doc.close();

  iframe.onload = function() {
    fitViewport();
    if (window.AndroidBridge && window.AndroidBridge.onPreviewLoaded) {
      window.AndroidBridge.onPreviewLoaded(TARGET_W, TARGET_H);
    }
  };
}

loadLivePreview(userCodeRaw, false);
setTimeout(fitViewport, 80);

window.reloadPreviewWithCode = function(newCode) {
  userCodeRaw = newCode;
  loadLivePreview(userCodeRaw, false);
};

window.startVideoRender = async function() {
  const banner = document.getElementById('renderBanner');
  try {
    if (banner) banner.style.display = 'block';
    postProgress(0, DURATION * FPS, 0.0, "Menyiapkan stage render " + TARGET_W + "×" + TARGET_H + "...");

    // Render directly on the visible live stage so Chromium GPU compositor keeps it active and user sees preview!
    loadLivePreview(userCodeRaw, true);
    await new Promise(r => setTimeout(r, 400));

    const iframe = document.getElementById('liveFrame');
    const iwin = iframe.contentWindow;
    const idoc = iframe.contentDocument;

    if (!iwin.__HARNESS__) throw new Error("Harness gagal terpasang.");
    const canvas = idoc.querySelector('canvas') || idoc.getElementById('c');
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
      if (mbPerSec <= 108000) level = '1f';
      else if (mbPerSec <= 245760) level = '28';
      else if (mbPerSec <= 522240) level = '2a';
      else if (mbPerSec <= 983040) level = '33';
      else level = '34';
      return 'avc1.6400' + level;
    }

    let config = {
      codec: pickCodec(TARGET_W, TARGET_H, FPS),
      width: TARGET_W,
      height: TARGET_H,
      bitrate: BITRATE,
      framerate: FPS,
      latencyMode: 'quality'
    };

    let support = await VideoEncoder.isConfigSupported(config);
    if (!support.supported) {
      config.codec = 'avc1.4d0034';
      support = await VideoEncoder.isConfigSupported(config);
      if (!support.supported) {
        config.codec = 'avc1.42001f';
        config.latencyMode = 'realtime';
        support = await VideoEncoder.isConfigSupported(config);
        if (!support.supported) {
          config.codec = 'avc1.4d002a';
          support = await VideoEncoder.isConfigSupported(config);
        }
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

      if (i % 4 === 0 || i === totalFrames - 1) {
        const prog = (i + 1) / totalFrames;
        const pct = Math.round(prog * 100);
        postProgress(i + 1, totalFrames, prog, "Merender frame " + (i + 1) + "/" + totalFrames + " (" + pct + "%)");
        await new Promise(r => setTimeout(r, 0));
      }

      let waitIter = 0;
      while (encoder.encodeQueueSize > 4 && waitIter < 35) {
        await new Promise(r => setTimeout(r, 8));
        waitIter++;
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

    const CHUNK_SIZE = 64 * 1024;
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

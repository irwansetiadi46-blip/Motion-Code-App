package com.example.data

import com.example.model.VideoPreset

object PresetRepository {

    val PRESETS: List<VideoPreset> = listOf(
        VideoPreset(
            id = "particles",
            name = "Particle Plexus",
            category = "Motion Background",
            description = "Jaringan partikel kosmik bercahaya yang saling terhubung secara dinamis berdasarkan jarak.",
            defaultAspect = "16:9",
            defaultFps = 60,
            defaultDuration = 6,
            tags = listOf("Particles", "Constellation", "Glow", "Network"),
            code = """
const c = document.getElementById('c');
const ctx = c.getContext('2d');
let W, H;
function resize(){ W = c.width = innerWidth; H = c.height = innerHeight; }
resize(); addEventListener('resize', resize);
const N = 180;
const particles = [];
for (let i = 0; i < N; i++) particles.push({
  x: Math.random(), y: Math.random(),
  vx: (Math.random()-0.5)*0.0012, vy: (Math.random()-0.5)*0.0012,
  r: 1+Math.random()*2, hue: 190+Math.random()*70
});
function loop(t) {
  requestAnimationFrame(loop);
  ctx.fillStyle = 'rgba(4,8,16,0.18)';
  ctx.fillRect(0, 0, W, H);
  for (const p of particles) {
    p.x += p.vx; p.y += p.vy;
    if (p.x < 0) p.x += 1; if (p.x > 1) p.x -= 1;
    if (p.y < 0) p.y += 1; if (p.y > 1) p.y -= 1;
  }
  const linkDist = Math.min(W, H) * 0.12;
  ctx.lineWidth = Math.min(W, H) / 900;
  for (let i = 0; i < N; i++) for (let j = i+1; j < N; j++) {
    const a = particles[i], b = particles[j];
    const dx = (a.x-b.x)*W, dy = (a.y-b.y)*H;
    const d = Math.hypot(dx, dy);
    if (d < linkDist) {
      ctx.strokeStyle = 'hsla(200,100%,60%,'+((1-d/linkDist)*0.35)+')';
      ctx.beginPath(); ctx.moveTo(a.x*W,a.y*H); ctx.lineTo(b.x*W,b.y*H); ctx.stroke();
    }
  }
  const baseR = Math.min(W, H) / 540;
  for (const p of particles) {
    ctx.fillStyle = 'hsla('+p.hue+',100%,65%,0.9)';
    ctx.beginPath(); ctx.arc(p.x*W, p.y*H, p.r*baseR, 0, Math.PI*2); ctx.fill();
  }
}
requestAnimationFrame(loop);
""".trimIndent()
        ),

        VideoPreset(
            id = "waves",
            name = "Gradient Waves",
            category = "Abstract Ambient",
            description = "Gelombang harmonik bernuansa gradien dinamis dengan pergeseran spektrum warna.",
            defaultAspect = "16:9",
            defaultFps = 60,
            defaultDuration = 6,
            tags = listOf("Gradient", "Waves", "Fluid", "Color Shift"),
            code = """
const c = document.getElementById('c');
const ctx = c.getContext('2d');
function resize(){ c.width = innerWidth; c.height = innerHeight; }
resize(); addEventListener('resize', resize);
function loop(t) {
  requestAnimationFrame(loop);
  const time = t * 0.001;
  const W = c.width, H = c.height;
  const h1 = (time*30)%360, h2 = (h1+120)%360;
  const g = ctx.createLinearGradient(0,0,W,H);
  g.addColorStop(0,'hsl('+h1+',80%,22%)');
  g.addColorStop(1,'hsl('+h2+',80%,12%)');
  ctx.fillStyle = g; ctx.fillRect(0,0,W,H);
  ctx.lineWidth = Math.min(W, H) / 500;
  const amp1 = Math.min(W, H) * 0.11;
  const amp2 = Math.min(W, H) * 0.055;
  const spacing = Math.min(W, H) * 0.075;
  for (let w = 0; w < 5; w++) {
    ctx.beginPath();
    for (let x = 0; x <= W; x += Math.max(2, W/400)) {
      const y = H/2 + Math.sin(x*0.01+time*2+w)*amp1 + Math.sin(x*0.02+time*3+w*2)*amp2 + (w-2)*spacing;
      if (x === 0) ctx.moveTo(x,y); else ctx.lineTo(x,y);
    }
    ctx.strokeStyle = 'hsla('+(h1+w*20)+',100%,65%,0.7)';
    ctx.stroke();
  }
}
requestAnimationFrame(loop);
""".trimIndent()
        ),

        VideoPreset(
            id = "grid",
            name = "Grid Pulse",
            category = "Cyberpunk & Tech",
            description = "Kisi matriks futuristik dengan gelombang radial berdenyut responsif.",
            defaultAspect = "16:9",
            defaultFps = 60,
            defaultDuration = 6,
            tags = listOf("Grid", "Matrix", "Pulse", "Cyber"),
            code = """
const c = document.getElementById('c');
const ctx = c.getContext('2d');
function resize(){ c.width = innerWidth; c.height = innerHeight; }
resize(); addEventListener('resize', resize);
function loop(t) {
  requestAnimationFrame(loop);
  const W = c.width, H = c.height;
  ctx.fillStyle = '#050810'; ctx.fillRect(0,0,W,H);
  const time = t*0.001;
  const cols = 22, rows = Math.max(1, Math.round(cols*H/W));
  const cw = W/cols, ch = H/rows;
  const cx = cols/2, cy = rows/2;
  const baseR = Math.min(cw, ch) * 0.35;
  for (let y = 0; y < rows; y++) for (let x = 0; x < cols; x++) {
    const d = Math.hypot(x-cx, y-cy);
    const pulse = 0.5 + 0.5*Math.sin(d*0.6 - time*3);
    const size = Math.min(cw, ch)*0.08 + pulse*baseR;
    const hue = 190 + pulse*60;
    ctx.fillStyle = 'hsl('+hue+',100%,'+(35+pulse*35)+'%)';
    ctx.beginPath(); ctx.arc(x*cw+cw/2, y*ch+ch/2, size, 0, Math.PI*2); ctx.fill();
  }
}
requestAnimationFrame(loop);
""".trimIndent()
        ),

        VideoPreset(
            id = "matrix",
            name = "Digital Matrix Rain",
            category = "Cyberpunk & Tech",
            description = "Aliran hujan kode biner digital hijau khas sci-fi dengan jejak fosfor bercahaya.",
            defaultAspect = "9:16",
            defaultFps = 60,
            defaultDuration = 8,
            tags = listOf("Hacker", "Code Rain", "Cyberpunk", "Terminal"),
            code = """
const c = document.getElementById('c');
const ctx = c.getContext('2d');
let W, H, cols, ypos;
const chars = '01アイウエオカキクケコサシスセソタチツテト0123456789ABCDEF<>/{};=';
function resize(){
  W = c.width = innerWidth; H = c.height = innerHeight;
  cols = Math.floor(W / 24);
  ypos = Array(cols).fill(0).map(() => Math.random() * -50);
}
resize(); addEventListener('resize', resize);
function loop(t) {
  requestAnimationFrame(loop);
  ctx.fillStyle = 'rgba(2, 6, 12, 0.08)';
  ctx.fillRect(0, 0, W, H);
  ctx.font = 'bold 20px monospace';
  for (let i = 0; i < cols; i++) {
    const char = chars[Math.floor(Math.random() * chars.length)];
    const x = i * 24;
    const y = ypos[i] * 24;
    // Glowing head
    ctx.fillStyle = '#C7FFD8';
    ctx.shadowBlur = 8;
    ctx.shadowColor = '#00FF66';
    ctx.fillText(char, x, y);
    ctx.shadowBlur = 0;
    // Body trailing
    ctx.fillStyle = '#00E676';
    ctx.fillText(char, x, y - 24);
    if (y > H && Math.random() > 0.975) {
      ypos[i] = 0;
    } else {
      ypos[i] += 0.8;
    }
  }
}
requestAnimationFrame(loop);
""".trimIndent()
        ),

        VideoPreset(
            id = "tunnel",
            name = "Neon Cyber Vortex",
            category = "3D & Geometric",
            description = "Terowongan geometri poligon futuristik yang berputar dan meluncur ke dimensi virtual.",
            defaultAspect = "1:1",
            defaultFps = 60,
            defaultDuration = 6,
            tags = listOf("3D", "Tunnel", "Vortex", "Neon", "Geometry"),
            code = """
const c = document.getElementById('c');
const ctx = c.getContext('2d');
let W, H;
function resize(){ W = c.width = innerWidth; H = c.height = innerHeight; }
resize(); addEventListener('resize', resize);
function loop(t) {
  requestAnimationFrame(loop);
  const time = t * 0.001;
  ctx.fillStyle = 'rgba(5, 5, 16, 0.25)';
  ctx.fillRect(0, 0, W, H);
  const cx = W / 2, cy = H / 2;
  const count = 36;
  const sides = 6;
  for (let i = 0; i < count; i++) {
    const z = ((i / count) + (time * 0.25)) % 1;
    const r = Math.pow(z, 2.5) * Math.min(W, H) * 0.85;
    const rot = time * 0.8 + i * 0.12;
    const hue = (time * 40 + i * 8) % 360;
    ctx.beginPath();
    for (let s = 0; s < sides; s++) {
      const angle = rot + (s / sides) * Math.PI * 2;
      const x = cx + Math.cos(angle) * r;
      const y = cy + Math.sin(angle) * r;
      if (s === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    }
    ctx.closePath();
    ctx.lineWidth = Math.max(1, (1 - z) * 4);
    ctx.strokeStyle = 'hsla(' + hue + ', 100%, 65%, ' + Math.min(1, z * 1.5) + ')';
    ctx.stroke();
  }
}
requestAnimationFrame(loop);
""".trimIndent()
        ),

        VideoPreset(
            id = "audio",
            name = "Audio Spectrum Beats",
            category = "Audio & Music",
            description = "Visualizer spektrum frekuensi audio neon berdenyut mengikuti simulasi beat bass.",
            defaultAspect = "16:9",
            defaultFps = 60,
            defaultDuration = 6,
            tags = listOf("Audio", "Spectrum", "Bars", "Music Video", "Bass"),
            code = """
const c = document.getElementById('c');
const ctx = c.getContext('2d');
let W, H;
function resize(){ W = c.width = innerWidth; H = c.height = innerHeight; }
resize(); addEventListener('resize', resize);
const bars = 48;
function loop(t) {
  requestAnimationFrame(loop);
  const time = t * 0.001;
  ctx.fillStyle = '#080B14';
  ctx.fillRect(0, 0, W, H);
  const barW = (W / bars) * 0.75;
  const gap = (W / bars) * 0.25;
  const baseY = H * 0.65;
  for (let i = 0; i < bars; i++) {
    const x = i * (barW + gap) + gap / 2;
    const freq = (i / bars) * 12;
    const beat = Math.sin(time * 6 + i * 0.25) * 0.5 + 0.5;
    const wave = Math.sin(time * 3 + freq) * 0.35 + 0.65;
    const h = (beat * wave) * (H * 0.45) + (H * 0.05);
    const hue = 180 + (i / bars) * 120;
    const grad = ctx.createLinearGradient(x, baseY - h, x, baseY);
    grad.addColorStop(0, 'hsl(' + hue + ', 100%, 65%)');
    grad.addColorStop(1, 'hsl(' + (hue + 40) + ', 100%, 35%)');
    ctx.fillStyle = grad;
    ctx.fillRect(x, baseY - h, barW, h);
    // Floor reflection
    const refGrad = ctx.createLinearGradient(x, baseY, x, baseY + h * 0.4);
    refGrad.addColorStop(0, 'hsla(' + hue + ', 100%, 50%, 0.35)');
    refGrad.addColorStop(1, 'transparent');
    ctx.fillStyle = refGrad;
    ctx.fillRect(x, baseY, barW, h * 0.4);
  }
}
requestAnimationFrame(loop);
""".trimIndent()
        ),

        VideoPreset(
            id = "flow",
            name = "Quantum Flow Field",
            category = "Motion Background",
            description = "Ribuan partikel kuantum melayang mengikuti pusaran medan vektor gaya gravitasi fluida.",
            defaultAspect = "9:16",
            defaultFps = 60,
            defaultDuration = 8,
            tags = listOf("Quantum", "Flow Field", "Perlin", "Fluid", "Vectors"),
            code = """
const c = document.getElementById('c');
const ctx = c.getContext('2d');
let W, H;
function resize(){ W = c.width = innerWidth; H = c.height = innerHeight; }
resize(); addEventListener('resize', resize);
const N = 400;
const pts = [];
for (let i = 0; i < N; i++) {
  pts.push({ x: Math.random(), y: Math.random(), vx: 0, vy: 0, age: Math.random() * 100 });
}
function loop(t) {
  requestAnimationFrame(loop);
  const time = t * 0.0006;
  ctx.fillStyle = 'rgba(6, 9, 20, 0.12)';
  ctx.fillRect(0, 0, W, H);
  for (let i = 0; i < N; i++) {
    const p = pts[i];
    const angle = Math.sin(p.x * 4 + time * 3) * Math.cos(p.y * 4 + time * 2) * Math.PI * 2;
    p.vx = Math.cos(angle) * 0.0035;
    p.vy = Math.sin(angle) * 0.0035;
    p.x += p.vx; p.y += p.vy;
    p.age++;
    if (p.x < 0 || p.x > 1 || p.y < 0 || p.y > 1 || p.age > 200) {
      p.x = Math.random(); p.y = Math.random(); p.age = 0;
    }
    const hue = (210 + angle * 30 + time * 200) % 360;
    ctx.fillStyle = 'hsla(' + hue + ', 100%, 65%, 0.75)';
    ctx.beginPath();
    ctx.arc(p.x * W, p.y * H, Math.min(W, H) / 450, 0, Math.PI * 2);
    ctx.fill();
  }
}
requestAnimationFrame(loop);
""".trimIndent()
        ),

        VideoPreset(
            id = "code_terminal",
            name = "Cinematic Code Terminal",
            category = "Code & Typography",
            description = "Simulasi pengetikan kode programming real-time sinematik dengan compiler glowing log.",
            defaultAspect = "16:9",
            defaultFps = 60,
            defaultDuration = 8,
            tags = listOf("Typing", "Code", "Terminal", "Hacker", "Software"),
            code = """
const c = document.getElementById('c');
const ctx = c.getContext('2d');
let W, H;
function resize(){ W = c.width = innerWidth; H = c.height = innerHeight; }
resize(); addEventListener('resize', resize);
const lines = [
  'import { NeuralEngine, QuantumPipeline } from "@core/synth";',
  'const engine = new NeuralEngine({ latency: 0.12, precision: 64 });',
  'const pipeline = await engine.initVideoPipeline({ res: "4K" });',
  'pipeline.attachHarness({ deterministic: true, fps: 60 });',
  'pipeline.render({ codec: "H.264/AVC", bitrate: "20Mbps" });',
  '// status: 100% rendered successfully -> footage.mp4'
];
function loop(t) {
  requestAnimationFrame(loop);
  ctx.fillStyle = '#0A0E17';
  ctx.fillRect(0, 0, W, H);
  const fontSize = Math.min(W, H) * 0.038;
  ctx.font = fontSize + 'px monospace';
  const startY = H * 0.25;
  const lineH = fontSize * 1.8;
  const charSpeed = 22; // chars per second
  const totalChars = Math.floor((t * 0.001) * charSpeed);
  let consumed = 0;
  for (let i = 0; i < lines.length; i++) {
    const text = lines[i];
    const y = startY + i * lineH;
    // Line number
    ctx.fillStyle = '#334155';
    ctx.fillText((i + 1).toString().padStart(2, '0') + '  ', W * 0.08, y);
    const charsToShow = Math.max(0, Math.min(text.length, totalChars - consumed));
    const sub = text.substring(0, charsToShow);
    consumed += text.length;
    // Syntax coloring
    if (text.startsWith('//')) ctx.fillStyle = '#10B981';
    else if (text.startsWith('import') || text.startsWith('const')) ctx.fillStyle = '#38BDF8';
    else ctx.fillStyle = '#F8FAFC';
    ctx.fillText(sub, W * 0.16, y);
    if (charsToShow < text.length && totalChars >= (consumed - text.length)) {
      // Blinking cursor
      if (Math.floor(t * 0.005) % 2 === 0) {
        const textWidth = ctx.measureText(sub).width;
        ctx.fillStyle = '#00E5FF';
        ctx.fillRect(W * 0.16 + textWidth + 2, y - fontSize + 3, fontSize * 0.5, fontSize);
      }
      break;
    }
  }
}
requestAnimationFrame(loop);
""".trimIndent()
        )
    )

    fun getById(id: String): VideoPreset? = PRESETS.find { it.id == id }
}

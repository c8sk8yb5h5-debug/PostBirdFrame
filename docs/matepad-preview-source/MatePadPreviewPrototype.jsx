import React, { useMemo, useRef, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Settings, Pause, Mail, Download, RefreshCw, X, Check, ChevronUp, ChevronDown } from "lucide-react";

const thumbLabels = [
  "厨房探索", "门口站立", "餐椅挥手", "滑梯合影",
  "室内活动", "操场散步", "球场背影", "触摸红墙",
  "公园合照", "树下招牌", "树干特写", "游乐场",
  "公园小路", "草地玩耍", "山景广场", "红墙留影",
  "亲子散步", "户外笑脸", "餐桌记录", "玩具时刻"
];

const thumbs = thumbLabels.map((label, index) => ({
  id: index + 1,
  label,
  year: 2026,
  month: 1,
  day: 6 + index,
  hue: (index * 31) % 360,
}));

function ScenePhoto({ item }) {
  return (
    <div className="relative h-full w-full overflow-hidden bg-[#101010]">
      <div className="absolute inset-y-0 left-0 w-[17%] bg-black" />
      <div className="absolute inset-y-0 right-0 w-[25%] bg-black" />
      <div className="absolute inset-y-0 left-[17%] w-[58%] overflow-hidden bg-[#cbb8a6]">
        <div className="absolute inset-0 bg-[linear-gradient(90deg,#e9e4da_0%,#d2c2b2_35%,#b19078_100%)]" />
        <div className="absolute left-[8%] top-[8%] h-[84%] w-[30%] rounded-[32px] bg-[#efebe3] shadow-inner" />
        <div className="absolute left-[44%] top-[10%] h-[26%] w-[42%] rounded-[24px] bg-[#8d6f55]/80 blur-[0.2px]" />
        <div className="absolute bottom-[12%] left-[38%] h-[18%] w-[48%] rounded-[26px] bg-[#34291f]/55" />
        <div className="absolute bottom-[10%] left-[31%] h-[32%] w-[18%] rounded-t-[999px] bg-[#1f5d63] shadow-xl" />
        <div className="absolute bottom-[39%] left-[34%] h-[8%] w-[8%] rounded-full bg-[#ffd0b3]" />
        <div className="absolute bottom-[13%] left-[48%] h-[52%] w-[26%] rounded-t-[999px] bg-[#f2c36c] shadow-xl" />
        <div className="absolute bottom-[59%] left-[55%] h-[10%] w-[10%] rounded-full bg-[#ffd4b8]" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_42%_38%,rgba(255,255,255,.25),transparent_28%),radial-gradient(circle_at_65%_65%,rgba(255,215,150,.20),transparent_32%)]" />
      </div>
      <div className="absolute left-[7%] top-[8%] rounded-full bg-black/45 px-4 py-2 text-sm text-white backdrop-blur-md">{item.label}</div>
    </div>
  );
}

function MiniThumb({ item, active, onClick }) {
  return (
    <motion.button
      layout
      onClick={onClick}
      whileTap={{ scale: 0.96 }}
      className={`relative aspect-square w-full overflow-hidden rounded-2xl border transition ${active ? "border-[#78d6ff] ring-2 ring-[#78d6ff]/50" : "border-white/10"}`}
      style={{ background: `linear-gradient(135deg, hsl(${item.hue}, 65%, 70%), hsl(${(item.hue + 60) % 360}, 55%, 42%))` }}
    >
      <div className="absolute inset-0 bg-black/15" />
      <div className="absolute left-2 top-2 h-6 w-6 rounded-full bg-[#ffd0b3]" />
      <div className="absolute bottom-2 left-2 right-2 rounded-lg bg-black/30 px-2 py-1 text-left text-[10px] text-white backdrop-blur-sm">{item.label}</div>
    </motion.button>
  );
}

function CircleControls({ item, onOpenSettings }) {
  return (
    <div className="absolute bottom-5 left-5 z-30 flex items-end gap-3">
      <motion.button whileTap={{ scale: 0.92 }} className="flex h-16 w-16 flex-col items-center justify-center rounded-full border border-white/35 bg-white/12 text-white shadow-[0_12px_35px_rgba(0,0,0,.35)] backdrop-blur-xl">
        <span className="text-base font-semibold leading-none">{item.year}</span>
        <span className="mt-1 text-[10px] text-white/65">年</span>
      </motion.button>
      <motion.button whileTap={{ scale: 0.92 }} className="flex h-16 w-16 flex-col items-center justify-center rounded-full border border-white/35 bg-white/12 text-white shadow-[0_12px_35px_rgba(0,0,0,.35)] backdrop-blur-xl">
        <span className="text-lg font-semibold leading-none">{String(item.month).padStart(2, "0")}</span>
        <span className="text-[11px] text-white/70">/{String(item.day).padStart(2, "0")}</span>
      </motion.button>
      <motion.button onClick={onOpenSettings} whileTap={{ scale: 0.9 }} className="flex h-16 w-16 items-center justify-center rounded-full border border-white/35 bg-white/12 text-white shadow-[0_12px_35px_rgba(0,0,0,.35)] backdrop-blur-xl">
        <Settings size={26} />
      </motion.button>
    </div>
  );
}

function Panel({ selectedId, setSelectedId, onClose, onOpenYearPicker, yearFrom, yearTo, status, setStatus }) {
  return (
    <motion.aside
      initial={{ x: 360, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: 360, opacity: 0 }}
      transition={{ type: "spring", stiffness: 280, damping: 32 }}
      className="absolute right-5 top-5 z-40 flex h-[calc(100%-40px)] w-[350px] flex-col overflow-hidden rounded-[34px] border border-white/25 bg-[#101317]/72 text-white shadow-[0_30px_90px_rgba(0,0,0,.55)] backdrop-blur-2xl"
    >
      <div className="flex items-center justify-between border-b border-white/10 px-5 py-4">
        <div>
          <div className="text-lg font-semibold">相框设置</div>
          <div className="mt-1 text-xs text-white/55">邮箱收取 · 自动播放 · 更新</div>
        </div>
        <button onClick={onClose} className="rounded-full bg-white/10 p-2"><X size={18} /></button>
      </div>

      <div className="space-y-4 overflow-y-auto px-5 py-4">
        <div className="rounded-3xl border border-white/10 bg-white/[0.06] p-4">
          <div className="mb-3 flex items-center justify-between">
            <div className="flex items-center gap-2 text-sm font-medium"><Mail size={16} /> QQ 邮箱接收</div>
            <span className="rounded-full bg-emerald-400/18 px-2 py-1 text-[10px] text-emerald-200">已连接</span>
          </div>
          <div className="space-y-2 text-xs text-white/62">
            <div className="flex justify-between"><span>检查频率</span><span>每 15 分钟</span></div>
            <div className="flex justify-between"><span>最近接收</span><span>3 分钟前</span></div>
          </div>
          <div className="mt-3 flex gap-2">
            <button onClick={() => setStatus("正在检查邮箱...")} className="flex flex-1 items-center justify-center gap-2 rounded-2xl bg-white/12 px-3 py-2 text-xs"><RefreshCw size={14} /> 立即检查</button>
            <button onClick={() => setStatus("已准备更新包") } className="flex flex-1 items-center justify-center gap-2 rounded-2xl bg-white/12 px-3 py-2 text-xs"><Download size={14} /> 检查更新</button>
          </div>
        </div>

        <div className="rounded-3xl border border-white/10 bg-white/[0.06] p-4">
          <div className="mb-3 text-sm font-medium">播放控制</div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <button className="rounded-2xl bg-white/12 px-3 py-3">顺序播放</button>
            <button className="rounded-2xl bg-white/12 px-3 py-3">8 秒切换</button>
            <button onClick={onOpenYearPicker} className="rounded-2xl bg-white/12 px-3 py-3">年份 {yearFrom}</button>
            <button className="rounded-2xl bg-white/12 px-3 py-3">全部日期</button>
          </div>
        </div>

        <div className="rounded-3xl border border-white/10 bg-white/[0.06] p-4">
          <div className="mb-3 text-sm font-medium">照片列表</div>
          <div className="grid grid-cols-3 gap-2">
            {thumbs.slice(0, 12).map((t) => <MiniThumb key={t.id} item={t} active={selectedId === t.id} onClick={() => setSelectedId(t.id)} />)}
          </div>
        </div>
      </div>

      <div className="border-t border-white/10 px-5 py-4 text-xs text-white/60">{status}</div>
    </motion.aside>
  );
}

function YearPicker({ open, currentYear, onClose, onConfirm }) {
  const [year, setYear] = useState(currentYear);
  const touchStartY = useRef(null);

  const inc = (v) => setYear((y) => Math.max(2000, Math.min(2035, y + v)));

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="absolute inset-0 z-50 flex items-center justify-center bg-black/28 backdrop-blur-md"
          onClick={onClose}
        >
          <motion.div
            initial={{ scale: 0.92, opacity: 0, y: 18 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.94, opacity: 0, y: 10 }}
            transition={{ type: "spring", stiffness: 260, damping: 26 }}
            onClick={(e) => e.stopPropagation()}
            onWheel={(e) => inc(e.deltaY > 0 ? 1 : -1)}
            onTouchStart={(e) => { touchStartY.current = e.touches[0].clientY; }}
            onTouchEnd={(e) => { if (touchStartY.current == null) return; const dy = e.changedTouches[0].clientY - touchStartY.current; if (Math.abs(dy) > 20) inc(dy < 0 ? 1 : -1); touchStartY.current = null; }}
            className="w-[330px] rounded-[34px] border border-white/25 bg-white/16 p-5 text-white shadow-[0_35px_110px_rgba(0,0,0,.55)] backdrop-blur-2xl"
          >
            <div className="mb-4 flex items-center justify-between">
              <div>
                <div className="text-lg font-semibold">选择播放年份</div>
                <div className="text-xs text-white/55">上下滑动切换年份</div>
              </div>
              <button onClick={onClose} className="rounded-full bg-white/10 p-2"><X size={17} /></button>
            </div>
            <div className="flex items-center justify-center gap-4 py-5">
              <button onClick={() => inc(-1)} className="rounded-full bg-white/10 p-2"><ChevronUp /></button>
              <div className="flex w-28 flex-col items-center gap-3">
                <span className="text-xl text-white/40">{year - 1}</span>
                <span className="rounded-2xl bg-white/18 px-6 py-3 text-4xl font-semibold">{year}</span>
                <span className="text-xl text-white/40">{year + 1}</span>
              </div>
              <button onClick={() => inc(1)} className="rounded-full bg-white/10 p-2"><ChevronDown /></button>
            </div>
            <button onClick={() => onConfirm(year)} className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl bg-white px-4 py-3 text-sm font-semibold text-[#111]"><Check size={17} /> 应用年份</button>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

export default function MatePadFramePreview() {
  const [selectedId, setSelectedId] = useState(1);
  const [panelOpen, setPanelOpen] = useState(false);
  const [yearPickerOpen, setYearPickerOpen] = useState(false);
  const [yearFrom, setYearFrom] = useState(2026);
  const [yearTo, setYearTo] = useState(2026);
  const [status, setStatus] = useState("等待自动检查邮箱");

  const item = useMemo(() => thumbs.find((t) => t.id === selectedId) || thumbs[0], [selectedId]);

  return (
    <main className="flex min-h-screen items-center justify-center bg-[#050607] p-6 text-white">
      <div className="relative h-[720px] w-[1120px] overflow-hidden rounded-[44px] border border-white/10 bg-[#0d0f12] shadow-[0_30px_120px_rgba(0,0,0,.65)]">
        <div className="absolute left-5 top-5 z-20 flex items-center gap-3 rounded-full border border-white/12 bg-white/10 px-4 py-2 backdrop-blur-xl">
          <span className="h-2 w-2 rounded-full bg-emerald-300 shadow-[0_0_12px_rgba(110,231,183,.9)]" />
          <span className="text-sm text-white/78">自动播放中</span>
          <Pause size={14} className="text-white/45" />
        </div>

        <section className="relative h-full w-full">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_45%,rgba(255,255,255,.06),transparent_36%)]" />
          <div className="absolute left-0 top-0 h-full w-[calc(100%-390px)] p-0">
            <div className="relative h-full w-full overflow-hidden bg-black">
              <AnimatePresence mode="wait">
                <motion.div key={selectedId} className="absolute inset-0" initial={{ opacity: 0, x: 24 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -24 }} transition={{ duration: 0.28, ease: [0.2, 0, 0, 1] }}>
                  <ScenePhoto item={item} />
                </motion.div>
              </AnimatePresence>
            </div>
          </div>
          <CircleControls item={{ ...item, year: yearFrom }} onOpenSettings={() => setPanelOpen((v) => !v)} />
        </section>

        <AnimatePresence>
          {panelOpen && <Panel selectedId={selectedId} setSelectedId={setSelectedId} onClose={() => setPanelOpen(false)} onOpenYearPicker={() => setYearPickerOpen(true)} yearFrom={yearFrom} yearTo={yearTo} status={status} setStatus={setStatus} />}
        </AnimatePresence>

        <YearPicker open={yearPickerOpen} currentYear={yearFrom} onClose={() => setYearPickerOpen(false)} onConfirm={(y) => { setYearFrom(y); setYearTo(y); setYearPickerOpen(false); }} />
      </div>
    </main>
  );
}

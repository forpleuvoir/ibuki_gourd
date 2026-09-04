-- Sokitsu 纹理元数据编辑器
--
-- 数据模型（与 Aseprite 结构的对应关系）：
--
--   .ase 文件   = 一个 SokitsuTexture
--   图层        = 一个 TextureLayer
--   图集 atlas  = 多个 .ase 缝合而成
--
-- 因此本插件把 Sokitsu 参数以 extension properties 的形式挂在**图层**上
-- （一个图层即一个 TextureLayer），随 .ase 保存，供烘焙工具（tools/ase）读取。
--
-- pluginKey 必须等于 "publisher/name"，与 package.json 中的
-- publisher("forpleuvoir") 和 name("sokitsu") 保持一致。

local PLUGIN_KEY = "forpleuvoir/sokitsu"

-- Slice 命名约定：<图层名>:sokitsu
-- 用于把九宫格 Slice 绑到对应图层（每层一个），兼顾自解释与自动匹配
local SLICE_SUFFIX = ":sokitsu"

local FILL_STRETCH = "stretch"
local FILL_NINEPATCH = "ninepatch"
local FILL_TILE = "tile"
local FILL_OPTIONS = { FILL_STRETCH, FILL_NINEPATCH, FILL_TILE }

-- tint 顺序与 TextureTintMode 枚举一致：Mask / Tint / HueShift
local TINT_OPTIONS = { "Mask", "Tint", "HueShift" }

-- 与选项同序的说明（面板动态提示用）
local TINT_HINTS = {
  "纹理=剪影/形状（颜色会被完全替换，只有 Alpha 生效）",
  "纹理=明暗（画灰阶，颜色由主题上色）",
  "纹理=彩色成品（保留纹理深浅，只把色相换成主题）",
}

local function tintHintIndex(mode)
  for i, o in ipairs(TINT_OPTIONS) do
    if o == mode then return i end
  end
  return nil
end

local LEVEL_NONE = ""
-- 顺序与 ColorLevel 枚举一致：Outline / Shadow / Dark / Base / Highlight
local LEVEL_OPTIONS = { LEVEL_NONE, "Outline", "Shadow", "Dark", "Base", "Highlight" }

local BORDER_EDGES = {
  { id = "Left", label = "左" },
  { id = "Top", label = "上" },
  { id = "Right", label = "右" },
  { id = "Bottom", label = "下" },
}

-- ---------------------------------------------------------------------------
-- Sokitsu 参考调色板（灰阶）
--
-- 作画统一从这里取色，避免不同 .ase 间灰度/明暗偏差。
-- 按亮度从高到低排列，每档亮度 -25（255 → 5），以**灰度色**（gray 通道）定义，
-- 而非等值 RGB；着色模式下颜色由主题决定，纹理只贡献明暗/alpha，灰阶即唯一规范占位。
-- ---------------------------------------------------------------------------

local SOKITSU_PALETTE = {
  { v = 255 },
  { v = 230 },
  { v = 205 },
  { v = 180 },
  { v = 155 },
  { v = 130 },
  { v = 105 },
  { v = 80 },
  { v = 55 },
  { v = 30 },
  { v = 5 },
}

-- ---------------------------------------------------------------------------
-- 读写
-- ---------------------------------------------------------------------------

local function defaults()
  return {
    enabled = true,
    fill = FILL_STRETCH,
    -- 默认 Tint：灰阶占位纹理只贡献明度、颜色由主题上色，覆盖绝大多数结构层
    tint = "Tint",
    tintAlpha = false,
    level = LEVEL_NONE,
    borderLeft = 0,
    borderTop = 0,
    borderRight = 0,
    borderBottom = 0,
    disabled = {},   -- 禁用的切片编号集合（0..8）
    scale = 1.0,
  }
end

local function read(layer)
  local p = defaults()
  -- 未配置过该图层时按"导出"处理：用户打开面板就是要配置它，
  -- 若默认 enabled=false，未勾选导出就点确定会导致其它配置全部不落盘
  local ok, stored = pcall(function() return layer.properties(PLUGIN_KEY) end)
  if not ok or stored == nil then return p end

  -- 注意：layer.properties(PLUGIN_KEY) 返回的是 Properties 对象（userdata），
  -- 不是普通 table，但支持按字段名索引读取。整体包在 pcall 里以防未知字段抛错。
  pcall(function()
    p.enabled = (stored.enabled ~= false)
    if type(stored.fill) == "string" then p.fill = stored.fill end
    if type(stored.tint) == "string" then
      -- 历史命名映射：Flat/Hsv/Hsl -> Mask/Tint/HueShift
      p.tint = ({
        ["Flat"] = "Mask", ["Hsv"] = "Tint", ["Hsl"] = "HueShift",
      })[stored.tint] or stored.tint
    end
    if stored.tintAlpha == true then p.tintAlpha = true end
    if type(stored.level) == "string" then p.level = stored.level end
    if type(stored.scale) == "number" then p.scale = stored.scale end

    for _, edge in ipairs(BORDER_EDGES) do
      local v = stored["border" .. edge.id]
      if type(v) == "number" then p["border" .. edge.id] = math.floor(v) end
    end

    local ds = stored.disableSlice
    if type(ds) == "table" then
      for _, v in ipairs(ds) do
        local n = math.floor(v)
        if n >= 0 and n <= 8 then p.disabled[n] = true end
      end
    end
  end)

  return p
end

local function write(layer, p)
  if not p.enabled then
    layer.properties(PLUGIN_KEY, { enabled = false })
    return
  end

  local out = {
    enabled = true,
    fill = p.fill,
    tint = p.tint,
  }

  if p.tintAlpha then out.tintAlpha = true end
  if p.level ~= LEVEL_NONE then out.level = p.level end

  if p.fill == FILL_NINEPATCH then
    for _, edge in ipairs(BORDER_EDGES) do
      out["border" .. edge.id] = p["border" .. edge.id]
    end
    local disabled = {}
    for i = 0, 8 do
      if p.disabled[i] then table.insert(disabled, i) end
    end
    if #disabled > 0 then out.disableSlice = disabled end
  elseif p.fill == FILL_TILE then
    out.scale = p.scale
  end

  layer.properties(PLUGIN_KEY, out)
end

-- ---------------------------------------------------------------------------
-- 图层选取
-- ---------------------------------------------------------------------------

local function selectedLayers()
  local sprite = app.activeSprite
  if not sprite then return {} end

  -- 多选（≥2 个图层）才走 range；单选一律以 activeLayer 为准。
  -- 原因：右键菜单命令执行时 range 可能仍指向旧选区，而面板标题显示的是
  -- activeLayer 的名字，保证「看到谁 = 改谁」。
  local rangeLayers = {}
  local ok, layers = pcall(function() return app.range.layers end)
  if ok and layers then
    for _, layer in ipairs(layers) do
      -- 组与 tilemap 不承载 Sokitsu 纹理
      if not layer.isGroup then rangeLayers[#rangeLayers + 1] = layer end
    end
  end

  if #rangeLayers > 1 then return rangeLayers end

  if app.activeLayer and not app.activeLayer.isGroup then
    return { app.activeLayer }
  end

  if #rangeLayers == 1 then return rangeLayers end
  return {}
end

-- 从九宫格 Slice 推导 border。Slice 必须画在画布内，故推导值恒为正；
-- 需要向外渲染的边，请在面板上勾选"向外"翻转符号。
local function borderFromSlice(slice)
  local bounds, center = slice.bounds, slice.center
  if not bounds or not center then return nil end
  return {
    borderLeft = center.x,
    borderTop = center.y,
    borderRight = bounds.width - center.x - center.width,
    borderBottom = bounds.height - center.y - center.height,
  }
end

-- Slice 属于 sprite 而非图层，彼此靠名称区分，因此必须按名查找
local function findSliceByName(sprite, name)
  if not sprite or not sprite.slices then return nil end
  if not name or name == "" then return nil end
  for _, slice in ipairs(sprite.slices) do
    if slice.name == name then return slice end
  end
  return nil
end

-- 是否启用九宫格（9-slices）。center 为 nil 表示用户在 Slice 属性里未启用
local function sliceHasCenter(slice)
  local ok, center = pcall(function() return slice.center end)
  return ok and center ~= nil
end

-- ---------------------------------------------------------------------------
-- 面板
-- ---------------------------------------------------------------------------

local function openDialog(layers)
  local first = layers[1]
  local p = read(first)
  local sprite = app.activeSprite
  local multiple = #layers > 1

  local title
  if multiple then
    title = string.format("Sokitsu 纹理（%d 个图层）", #layers)
  else
    title = "Sokitsu 纹理 — " .. first.name
  end

  local dlg = Dialog(title)

  dlg:check{
    id = "enabled",
    text = "作为 Sokitsu 图层导出",
    selected = p.enabled,
  }

  dlg:separator{ text = "着色" }
  dlg:combobox{
    id = "tint",
    label = "模式",
    option = p.tint,
    options = TINT_OPTIONS,
    onchange = function()
      local idx = 0
      for i, o in ipairs(TINT_OPTIONS) do
        if o == dlg.data.tint then idx = i break end
      end
      dlg:modify{ id = "tintHint", text = TINT_HINTS[idx] or "" }
    end,
  }
  dlg:newrow()
  dlg:label{
    id = "tintHint",
    text = TINT_HINTS[tintHintIndex(p.tint)] or "",
  }
  dlg:newrow()
  dlg:check{
    id = "tintAlpha",
    text = "同时替换 Alpha（α 取自主题色档）",
    selected = p.tintAlpha,
  }
  dlg:newrow()
  dlg:combobox{
    id = "level",
    label = "色彩层级",
    option = p.level,
    options = LEVEL_OPTIONS,
  }
  dlg:newrow()

  -- 九宫格区块内全部控件的 id，供按 fill 模式整体显隐
  local nineGroup = {}
  local function addNine(id) nineGroup[#nineGroup + 1] = id end
  local function isNine() return p.fill == FILL_NINEPATCH end

  -- 填充模式联动：tile 显示平铺缩放，ninepatch 显示九宫格区块
  local function applyFillVisibility()
    local f = dlg.data.fill
    dlg:modify{ id = "scale", visible = (f == FILL_TILE) }
    for _, id in ipairs(nineGroup) do
      dlg:modify{ id = id, visible = (f == FILL_NINEPATCH) }
    end
    -- 强制一次重排：部分 Aseprite 版本隐藏控件后不自动收缩布局，
    -- 会残留旧控件像素（拖动窗口可修复），这里模拟同等效果的 relayout
    local ok, b = pcall(function() return dlg.bounds end)
    if ok and b then
      pcall(function() dlg.bounds = Rectangle(b.x, b.y, b.width + 1, b.height) end)
      pcall(function() dlg.bounds = Rectangle(b.x, b.y, b.width, b.height) end)
    end
  end

  dlg:separator{ text = "填充" }
  dlg:combobox{
    id = "fill",
    label = "模式",
    option = p.fill,
    options = FILL_OPTIONS,
    onchange = applyFillVisibility,
  }
  dlg:newrow()
  dlg:number{
    id = "scale",
    label = "平铺缩放（tile）",
    text = string.format("%g", p.scale),
    decimals = 2,
    visible = p.fill == FILL_TILE,
  }
  dlg:newrow()

  -- 九宫格区块（仅 ninepatch 显示）
  dlg:separator{
    id = "sepNine",
    text = "九宫格（ninepatch）",
    visible = isNine(),
  }
  addNine("sepNine")

  -- Slice 下拉：列出当前文件全部 Slice 供导入选择
  local sliceNames = {}
  if sprite and sprite.slices then
    for _, s in ipairs(sprite.slices) do
      sliceNames[#sliceNames + 1] = s.name
    end
  end
  -- 默认选中：命名约定 <图层名>:sokitsu；不存在则取第一个
  local defaultName = first.name .. SLICE_SUFFIX
  local found = false
  for _, n in ipairs(sliceNames) do
    if n == defaultName then found = true break end
  end
  local sliceOptions = sliceNames
  local sliceOption
  if #sliceNames == 0 then
    sliceOptions = { "(无)" }
    sliceOption = "(无)"
  elseif found then
    sliceOption = defaultName
  else
    sliceOption = sliceNames[1]
  end
  dlg:combobox{
    id = "sliceName",
    label = "Slice",
    option = sliceOption,
    options = sliceOptions,
    visible = isNine(),
  }
  addNine("sliceName")
  dlg:newrow()

  dlg:button{
    id = "fromSlice",
    text = "导入所选 Slice",
    visible = isNine(),
    onclick = function()
      if multiple then
        app.alert{
          title = "Sokitsu",
          text = "各图层的 Slice 彼此独立，请单选一个图层后再导入。",
        }
        return
      end
      local name = dlg.data.sliceName
      if not name or name == "" or name == "(无)" then
        app.alert{
          title = "Sokitsu",
          text = "当前文件没有 Slice。\n请用切片工具（Shift+C）框选九宫格区域后重试。",
        }
        return
      end
      local slice = findSliceByName(sprite, name)
      if not slice then
        app.alert{
          title = "Sokitsu",
          text = "未找到名为「" .. name .. "」的 Slice。",
        }
        return
      end
      if not sliceHasCenter(slice) then
        app.alert{
          title = "Sokitsu",
          text = "该 Slice 未启用 9-slices。\n请双击 Slice，在其属性中勾选 9-slices 并调整中心区域。",
        }
        return
      end
      local b = borderFromSlice(slice)
      if not b then return end
      for _, edge in ipairs(BORDER_EDGES) do
        dlg:modify{ id = "border" .. edge.id, text = tostring(b["border" .. edge.id]) }
        dlg:modify{ id = "neg" .. edge.id, selected = false }
      end
      dlg:modify{ id = "fill", option = FILL_NINEPATCH }
    end,
  }
  addNine("fromSlice")
  dlg:newrow()

  for _, edge in ipairs(BORDER_EDGES) do
    local value = p["border" .. edge.id]
    dlg:number{
      id = "border" .. edge.id,
      label = edge.label,
      text = tostring(math.abs(value)),
      decimals = 0,
      visible = isNine(),
    }
    addNine("border" .. edge.id)
    dlg:check{
      id = "neg" .. edge.id,
      text = "向外",
      selected = value < 0,
      visible = isNine(),
    }
    addNine("neg" .. edge.id)
    dlg:newrow()
  end

  -- 禁用切片：9 个 checkbox 按 3×3 排布，编号对应九宫格空间位置
  --   0 1 2
  --   3 4 5
  --   6 7 8
  dlg:label{
    id = "lblDisable",
    text = "禁用切片",
    visible = isNine(),
  }
  addNine("lblDisable")
  dlg:newrow()
  for i = 0, 8 do
    dlg:check{
      id = "slice" .. i,
      text = tostring(i),
      selected = p.disabled[i] == true,
      visible = isNine(),
    }
    addNine("slice" .. i)
    if i % 3 == 2 then dlg:newrow() end
  end

  dlg:button{ id = "ok", text = "确定", focus = true }
  dlg:button{ id = "cancel", text = "取消" }

  dlg:show()

  local data = dlg.data
  if not data.ok then return end

  -- properties 按 Lua 数值子类型推断 .ase 存储类型：
  -- 整数会存成 int32，这里强制转成 float 以免烘焙器按 float 读取时类型不符
  local scale = tonumber(data.scale) or 1.0
  if math.type and math.type(scale) == "integer" then scale = scale + 0.0 end

  local result = {
    enabled = data.enabled == true,
    fill = data.fill or FILL_STRETCH,
    tint = data.tint or TINT_OPTIONS[1],
    tintAlpha = data.tintAlpha == true,
    level = data.level or LEVEL_NONE,
    scale = scale,
    disabled = {},
  }
  for i = 0, 8 do
    if data["slice" .. i] == true then result.disabled[i] = true end
  end
  for _, edge in ipairs(BORDER_EDGES) do
    local v = math.floor(tonumber(data["border" .. edge.id]) or 0)
    if data["neg" .. edge.id] then v = -math.abs(v) end
    result["border" .. edge.id] = v
  end

  app.transaction("Sokitsu 纹理设置", function()
    for _, layer in ipairs(layers) do
      write(layer, result)
    end
  end)
  app.refresh()

  -- 保存自检：立即回读。注意 properties() 返回 Properties 对象（userdata），
  -- 只要 enabled 字段读得到即视为写入成功
  local okR, verify = pcall(function() return first.properties(PLUGIN_KEY) end)
  local readBack = okR and verify ~= nil
  if readBack then
    local okE, enabledV = pcall(function() return verify.enabled end)
    readBack = okE and enabledV ~= nil
  end
  if not readBack then
    local d = Dialog("Sokitsu")
    d:label{ text = "保存后回读失败，设置可能未保存。" }
    d:newrow()
    d:label{ text = "请右键该图层，运行「Sokitsu 保存诊断」，" }
    d:newrow()
    d:label{ text = "把弹窗内容发我。" }
    d:newrow()
    d:button{ id = "ok", text = "知道了", focus = true }
    d:show()
  end
end

-- ---------------------------------------------------------------------------
-- 注册
-- ---------------------------------------------------------------------------

function init(plugin)
  plugin:newCommand{
    id = "SokitsuLayerSettings",
    title = "Sokitsu 纹理设置...",
    group = "layer_popup_properties",
    onclick = function()
      local layers = selectedLayers()
      if #layers == 0 then
        app.alert{ title = "Sokitsu", text = "请先选中一个图层。" }
        return
      end
      openDialog(layers)
    end,
    onenabled = function()
      return app.activeSprite ~= nil and #selectedLayers() > 0
    end,
  }

  plugin:newCommand{
    id = "SokitsuLayerClear",
    title = "清除 Sokitsu 设置",
    group = "layer_popup_properties",
    onclick = function()
      local layers = selectedLayers()
      if #layers == 0 then return end
      app.transaction("清除 Sokitsu 设置", function()
        for _, layer in ipairs(layers) do
          layer.properties(PLUGIN_KEY, {})
        end
      end)
      app.refresh()
    end,
    onenabled = function()
      return app.activeSprite ~= nil and #selectedLayers() > 0
    end,
  }

  plugin:newCommand{
    id = "SokitsuSaveDiag",
    title = "Sokitsu 保存诊断",
    group = "layer_popup_properties",
    onclick = function()
      local layer = app.activeLayer
      if not layer then
        app.alert{ title = "Sokitsu", text = "无活动图层。" }
        return
      end

      local lines = { "图层: " .. tostring(layer.name) }

      local function readProps()
        local ok, v = pcall(function() return layer.properties(PLUGIN_KEY) end)
        return ok, v
      end

      local function dump(label, v)
        lines[#lines + 1] = "[" .. label .. "] type=" .. type(v)
        if type(v) == "table" then
          local okj, js = pcall(function() return json.encode(v) end)
          lines[#lines + 1] = "  " .. (okj and js or "<json 编码失败>")
        elseif type(v) == "userdata" then
          -- Properties 对象：枚举已知字段
          local parts = {}
          local okE = pcall(function()
            for _, k in ipairs({
              "enabled", "fill", "tint", "level", "scale",
              "borderLeft", "borderTop", "borderRight", "borderBottom",
              "disableSlice",
            }) do
              local val = v[k]
              if val ~= nil then
                if type(val) == "table" then
                  parts[#parts + 1] = k .. "={" .. table.concat(val, ",") .. "}"
                else
                  parts[#parts + 1] = k .. "=" .. tostring(val)
                end
              end
            end
          end)
          lines[#lines + 1] = "  " .. (okE and (#parts > 0 and table.concat(parts, "  ") or "(空/无属性)") or "<枚举失败>")
        elseif v ~= nil then
          lines[#lines + 1] = "  " .. tostring(v)
        else
          lines[#lines + 1] = "  (nil)"
        end
      end

      local ok1, before = readProps()
      dump("读前", before)

      -- 复刻设置面板的真实保存路径：write() 全量 ninepatch（负 border + disableSlice vector）
      local okW, errW = pcall(function()
        app.transaction("Sokitsu 诊断写入", function()
          write(layer, {
            enabled = true,
            fill = "ninepatch",
            tint = "Flat",
            level = "Base",
            scale = 2.5,
            disabled = { [1] = true, [5] = true },
            borderLeft = 4,
            borderTop = -2,
            borderRight = 4,
            borderBottom = -2,
          })
        end)
      end)
      lines[#lines + 1] = "[write 全量 ninepatch] ok=" .. tostring(okW) .. (okW and "" or (" err=" .. tostring(errW)))
      local ok2, after = readProps()
      dump("读回", after)

      -- 对照：只写标量（不带负值 / 不带数组）
      local ok3, err3 = pcall(function()
        layer.properties(PLUGIN_KEY, { enabled = true, fill = "tile", tint = "Hsl", scale = 2.5 })
      end)
      lines[#lines + 1] = "[只写标量] ok=" .. tostring(ok3) .. (ok3 and "" or (" err=" .. tostring(err3)))
      local ok4, out = readProps()
      dump("最终读", out)

      -- 复刻设置面板时序：先经过一次 Dialog（点确定关闭），再写入并回读
      local cont = Dialog("Sokitsu 诊断 · 第 2 步")
      cont:label{ text = "点「确定」关闭本对话框后，" }
      cont:newrow()
      cont:label{ text = "将执行一次写入并回读，验证对话框时序。" }
      cont:newrow()
      cont:button{ id = "ok", text = "确定", focus = true }
      cont:show()
      if cont.data.ok then
        local okD, errD = pcall(function()
          app.transaction("Sokitsu 诊断写入2", function()
            write(layer, {
              enabled = true,
              fill = "tile",
              tint = "Flat",
              level = "Highlight",
              scale = 3.0,
              disabled = {},
              borderLeft = 1,
              borderTop = 1,
              borderRight = 1,
              borderBottom = 1,
            })
          end)
        end)
        lines[#lines + 1] = "[对话框关闭后 write] ok=" .. tostring(okD) .. (okD and "" or (" err=" .. tostring(errD)))
        local ok5, out5 = readProps()
        dump("对话框后读回", out5)
      else
        lines[#lines + 1] = "[对话框关闭后 write] 已跳过"
      end

      -- 结果写入剪贴板，便于直接粘贴发送
      local copied = pcall(function() app.clipboard.text = table.concat(lines, "\n") end)

      local d = Dialog("Sokitsu 保存诊断")
      d:label{
        text = copied and "结果已复制到剪贴板，直接粘贴即可发送。" or "无法复制，请手动抄录。",
      }
      d:newrow()
      d:separator{ text = "结果" }
      for _, line in ipairs(lines) do
        d:label{ text = line }
        d:newrow()
      end
      d:button{ id = "ok", text = "知道了", focus = true }
      d:show{ autoscrollbars = true }
    end,
    onenabled = function()
      return app.activeLayer ~= nil
    end,
  }

  plugin:newCommand{
    id = "SokitsuPalette",
    title = "导入 Sokitsu 调色板",
    group = "layer_popup_properties",
    onclick = function()
      local sprite = app.activeSprite
      if not sprite then
        app.alert{ title = "Sokitsu", text = "请先打开一个精灵。" }
        return
      end
      local applied = 0
      local ok, err = pcall(function()
        app.transaction("Sokitsu 调色板", function()
          -- 正确的调色板入口是 sprite.palettes[1]（Palette 对象数组），而非 sprite.palette
          local pal = (sprite.palettes and sprite.palettes[1]) or app.palette
          if not pal then error("无可用调色板") end
          local need = #SOKITSU_PALETTE
          if #pal < need then pal:resize(need) end
          for i, entry in ipairs(SOKITSU_PALETTE) do
            -- 以灰度色写入（Color{gray=...}），而非等值 RGB
            pal:setColor(i - 1, Color{ gray = entry.v, alpha = 255 })
            applied = applied + 1
          end
        end)
      end)
      if ok then
        app.refresh()
        app.alert{
          title = "Sokitsu",
          text = "已写入前 " .. applied .. " 个调色板槽。\n灰阶（亮度 255→5，每档 -25）：\n作画只用这套取色，跨文件明暗即可一致。",
        }
      else
        app.alert{
          title = "Sokitsu",
          text = "自动写入调色板失败：" .. tostring(err) ..
            "\n可手动导入：在调色板面板前 11 槽填入上方定义的颜色。",
        }
      end
    end,
    onenabled = function()
      return app.activeSprite ~= nil
    end,
  }
end

function exit(plugin)
end

#!/usr/bin/env python3
"""Regenerates ui/expressive/components/SolsticeIcons.kt from tools/symbols.txt.

Downloads each Material Symbol (Rounded, weight WEIGHT, grade 0) at optical sizes 24 and 20 from
google/material-design-icons (Apache-2.0) and writes the Kotlin catalog. Run from the repo root:
    python3 tools/make_symbols.py
"""
import pathlib
import re
import urllib.request

R = pathlib.Path(__file__).resolve().parent.parent
F = R / "app/src/main/kotlin/org/breezyweather/ui/expressive/components/SolsticeIcons.kt"
BASE = "https://raw.githubusercontent.com/google/material-design-icons/master/symbols/web"

# Every icon's stroke weight, in one place (Material Symbols' axis: 100–700; 400 is the default). All
# icons, and the live clock that matches "schedule", follow it (2026-11-22, the user: a little heavier).
WEIGHT = 500
_cache = {}


def path(size, n):
    """The SVG path of symbol n (n may end in _fill) at optical size 24 ('svg') or 20 ('svg20')."""
    name = n[:-5] if n.endswith("_fill") else n
    filled = n.endswith("_fill")
    # File names: "<name>_24px" at 400, "<name>_wght500_24px" otherwise; filled adds "fill1".
    weight = f"_wght{WEIGHT}" if WEIGHT != 400 else ""
    fill = ("fill1" if weight else "_fill1") if filled else ""
    px = "20" if size == "svg20" else "24"
    url = f"{BASE}/{name}/materialsymbolsrounded/{name}{weight}{fill}_{px}px.svg"
    if url not in _cache:
        with urllib.request.urlopen(url) as r:
            svg = r.read().decode()
        ps = re.findall(r'<path d="([^"]+)"', svg)
        assert len(ps) == 1, url
        # Most files draw on a 960 grid (viewBox "0 -960 960 960"); a few (push_pin) still use the
        # old one, in the file's own pixel size with no viewBox.
        own = None if "viewBox" in svg else re.search(r'width="(\d+)"', svg).group(1)
        _cache[url] = (ps[0], own)
    return _cache[url]


def pascal(n):
    return "".join(p.capitalize() for p in n.split("_"))


entries_txt = [
    l.split() for l in (R / "tools/symbols.txt").read_text().splitlines() if l.strip() and not l.startswith("#")
]
syms = [e[0] for e in entries_txt]
fills = [e[0] for e in entries_txt if "fill" in e[1:]]
mirror = {e[0] for e in entries_txt if "mirror" in e[1:]}
header = F.read_text().split('package ')[0]
out = [header.rstrip()+'\n', 'package org.breezyweather.ui.expressive.components\n',
'import androidx.compose.ui.graphics.Color',
'import androidx.compose.ui.graphics.SolidColor',
'import androidx.compose.ui.graphics.vector.ImageVector',
'import androidx.compose.ui.graphics.vector.PathParser',
'import androidx.compose.ui.graphics.vector.group',
'import androidx.compose.ui.unit.Dp',
'import androidx.compose.ui.unit.dp',
'import org.breezyweather.ui.expressive.theme.Sizes',
'',
'/**',
f' * Every icon Solstice uses, from Material Symbols **Rounded** (weight {WEIGHT}, grade 0), so call sites',
' * never pick a source or a style. Rules (docs/PLAN.md, "Icons"):',
' * - Unfilled by default; the `…Fill` variants only for on/selected (starred, pinned, location in use).',
' * - Optical size follows the drawn size, as Material recommends: `SolsticeIcons.X` is drawn for',
' *   24 dp, `SolsticeIcons.Small.X` for 20 dp and smaller ([forSize] picks one).',
' * - New icon: add both optical sizes here from google/material-design-icons',
' *   (`symbols/web/<name>/materialsymbolsrounded/<name>_24px.svg` and `_20px.svg`). Don\'t use',
' *   `material-icons-extended` (the old Material Icons, frozen since 2022, removed).',
' *',
' * Material Symbols: Apache License 2.0, Google.',
' */',
'object SolsticeIcons {',
'    /** The stroke weight every icon is drawn at (Material Symbols\' axis; set in tools/make_symbols.py). */',
f'    const val WEIGHT = {WEIGHT}',
'',
]
def one(indent, name, pv, mir):
    p, own = pv
    chunks=[p[i:i+88] for i in range(0,len(p),88)]
    r=[f'{indent}val {name}: ImageVector by lazy {{', f'{indent}    symbol(', f'{indent}        "{name}",']
    for i,c in enumerate(chunks):
        pre = f'{indent}        ' if i==0 else f'{indent}            '
        post = ' +' if i < len(chunks)-1 else (',' if (mir or own) else '')
        r.append(f'{pre}"{c}"{post}')
    if mir: r.append(f'{indent}        autoMirror = true' + (',' if own else ''))
    if own: r.append(f'{indent}        grid = {own}f')
    r += [f'{indent}    )', f'{indent}}}']
    return r
def entries(d, indent):
    res=[]
    for n in syms:
        res += one(indent, pascal(n), path(d,n), n in mirror)
    for n in fills:
        res += one(indent, pascal(n)+'Fill', path(d,n+'_fill'), False)
    return res
allnames=[pascal(n) for n in syms]+[pascal(n)+'Fill' for n in fills]
out += entries('svg','    ')
out += ['', '    /** The same icons at optical size 20, for icons drawn at 20 dp or smaller. */', '    object Small {']
out += entries('svg20','        ')
out += ['    }', '',
'    /**',
'     * The variant of [icon] drawn best at [size]: the optical size 20 version ([Small]) up to 20 dp,',
'     * as Material recommends. Icons from outside the catalog are returned as they are.',
'     */',
'    fun forSize(icon: ImageVector, size: Dp): ImageVector =',
'        if (size <= Sizes.Icon) small[icon.name]?.invoke() ?: icon else icon',
'',
'    private val small: Map<String, () -> ImageVector> = mapOf(',
] + [f'        "{n}" to {{ Small.{n} }}' + (',' if i < len(allnames)-1 else '') for i,n in enumerate(allnames)] + [
'    )',
'}', '',
'/**',
' * One Material Symbol: drawn on a 960 grid with y from -960 to 0, as in most of the SVGs, or on the',
' * file\'s own [grid] (its pixel size, y from 0) for the few still in the old format.',
' */',
'private fun symbol(name: String, path: String, autoMirror: Boolean = false, grid: Float = 0f): ImageVector =',
'    ImageVector.Builder(',
'        name = name,',
'        defaultWidth = 24.dp,',
'        defaultHeight = 24.dp,',
'        viewportWidth = if (grid > 0f) grid else 960f,',
'        viewportHeight = if (grid > 0f) grid else 960f,',
'        autoMirror = autoMirror',
'    ).apply {',
'        group(translationY = if (grid > 0f) 0f else 960f) {',
'            addPath(PathParser().parsePathString(path).toNodes(), fill = SolidColor(Color.Black))',
'        }',
'    }.build()', '']

def points(d):
    # Every on-curve point of an SVG path (enough for its extremes; control points are skipped).
    toks = re.findall(r'[MmLlHhVvQqTtCcSsZz]|-?\d*\.?\d+(?:e-?\d+)?', d)
    i = 0; x = y = sx = sy = 0.0; cmd = None; subs = [[]]
    def num():
        nonlocal i
        v = float(toks[i]); i += 1; return v
    arity = {'M': 2, 'L': 2, 'H': 1, 'V': 1, 'Q': 4, 'T': 2, 'C': 6, 'S': 4}
    while i < len(toks):
        t = toks[i]
        if t.isalpha():
            cmd = t; i += 1
            if cmd in 'Zz':
                x, y = sx, sy; subs.append([]); continue
        up = cmd.upper(); rel = cmd.islower()
        vals = [num() for _ in range(arity[up])]
        if up == 'H': nx, ny = (x + vals[0] if rel else vals[0]), y
        elif up == 'V': nx, ny = x, (y + vals[0] if rel else vals[0])
        else:
            nx, ny = vals[-2], vals[-1]
            if rel: nx += x; ny += y
        x, y = nx, ny
        if up == 'M':
            sx, sy = x, y
            if subs[-1]: subs.append([])
            cmd = 'l' if rel else 'L'
        subs[-1].append((x, y))
    return [s for s in subs if s]

def clock_geometry():
    # The live clock's proportions, measured from "schedule" at the optical size the clock is drawn at
    # (20), on its 960 grid centred at (480, -480): the ring's outer and inner radius, and how far each
    # hand reaches (to the middle of its rounded end).
    d, _ = path('svg20', 'schedule')
    subs = points(d)
    dist = lambda p: ((p[0] - 480) ** 2 + (p[1] + 480) ** 2) ** 0.5
    rings = sorted(subs, key=lambda s: -max(dist(p) for p in s))
    outer = max(dist(p) for p in rings[0])
    inner = min(dist(p) for s in subs[1:] for p in s if dist(p) > 200)
    stroke = outer - inner
    hands = subs[0]
    up = max(-p[1] - 480 for p in hands if abs(p[0] - 480) < stroke)  # the hand pointing at 12 (y is up = -)
    diag = max(dist(p) for p in hands if p[0] > 480 + stroke and p[1] > -480)  # the one towards 4:30
    return outer - stroke / 2, stroke, up - stroke / 2, diag - stroke / 2

ring, stroke, minute, hour = clock_geometry()
out += [
'/**',
f' * "schedule" (Rounded, weight {WEIGHT}, optical size 20) measured on its 960 grid by tools/make_symbols.py,',
' * so the live clock ([ClockIcon]) matches the symbols at any weight.',
' */',
'internal object ScheduleGeometry {',
'    const val GRID = 960f',
'',
'    /** The ring\'s centre line. */',
f'    const val RING = {ring:.1f}f',
'',
'    /** The ring\'s and the hands\' thickness. */',
f'    const val STROKE = {stroke:.1f}f',
'',
'    /** The long hand, centre to the middle of its rounded end. */',
f'    const val MINUTE_HAND = {minute:.1f}f',
'',
'    /** The short hand, centre to the middle of its rounded end. */',
f'    const val HOUR_HAND = {hour:.1f}f',
'}', '']
F.write_text('\n'.join(out))
print('clock', round(ring,1), round(stroke,1), round(minute,1), round(hour,1))
print(len(syms), 'symbols')

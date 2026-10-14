#!/usr/bin/env python3
"""Пересобирает ВСЕ ресурсы мода одной командой.

Запуск:  python3 tools/gen_all.py

Порядок важен: сначала текстуры/модели/lang, потом data-JSON'ы и шаблоны структур,
в конце — проверка того, что всё сходится.

Если правили код (добавили предмет/блок/сущность) — допишите id в списки
tools/gen_lang_models.py, затем запустите этот скрипт и tools/validate_assets.py.
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

import gen_data          # noqa: E402
import gen_lang_models   # noqa: E402
import gen_structures    # noqa: E402
import patch_worldgen    # noqa: E402
import validate_assets   # noqa: E402

STEPS = [
    ("lang + модели + blockstates", gen_lang_models.main),
    ("текстуры (нужен Pillow)", None),  # обрабатывается отдельно
    ("структуры (.nbt)", gen_structures.main),
    ("мир: оверрайды генерации", patch_worldgen.main),
    ("data-JSON'ы (лут, урон, структуры, теги)", gen_data.main),
]


def run_textures() -> None:
    try:
        import gen_textures
    except ImportError:
        print("  Pillow не установлен — текстуры пропущены "
              "(pip install Pillow, затем python3 tools/gen_textures.py)")
        return
    gen_textures.main()


def main() -> int:
    for title, func in STEPS:
        print(f"\n=== {title} ===")
        (run_textures() if func is None else func())
    print("\n=== проверка ресурсов ===")
    return validate_assets.main()


if __name__ == "__main__":
    raise SystemExit(main())

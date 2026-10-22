#!/usr/bin/env python3
"""1.21.4+ client item definitions (assets/spartanshields/items/<id>.json) from the 1.21.1 item models and their
overrides: minecraft:blocking -> the minecraft:using_item condition, minecraft:disabled (an empty powered shield) ->
the spartanshields:powered_off condition; tower shields -> the spartanshields:tower_shield special model (the 1.21.1
BEWLR); the light layer of the Mekanist / Dark Steel basic shields gets its colour as a constant tint (1.21.1:
ClientHelper#registerItemColours). Run from the repository root; writes the items folder and drops the overrides."""
import json, os, re

ASSETS = 'common/src/main/resources/assets/spartanshields'
MODELS = os.path.join(ASSETS, 'models/item')
ITEMS = os.path.join(ASSETS, 'items')
# ARGB of the light layer (layer 1)
TINTS = {
    'basic_mekanists_basic_shield': 0xFF78F083,
    'advanced_mekanists_basic_shield': 0xFFF07883,
    'elite_mekanists_basic_shield': 0xFF7883F0,
    'ultimate_mekanists_basic_shield': 0xFFF083F0,
    'dark_steel_riot_basic_shield': 0xFF80FFA0,
}

def ids():
    src = open('common/src/main/java/com/oblivioussp/spartanshields/init/ModItems.java').read()
    return sorted(set(re.findall(r'REGISTER\.register\("([a-z0-9_]+)"', src)))

def signed(v):
    return v - (1 << 32) if v >= 1 << 31 else v

def leaf(item, model):
    if item.endswith('_tower_shield'):
        return {'type': 'minecraft:special', 'base': model, 'model': {'type': 'spartanshields:tower_shield', 'shield': 'spartanshields:' + item}}
    out = {'type': 'minecraft:model', 'model': model}
    if item in TINTS:
        out['tints'] = [{'type': 'minecraft:constant', 'value': -1}, {'type': 'minecraft:constant', 'value': signed(TINTS[item])}]
    return out

def condition(prop, on_true, on_false):
    return {'type': 'minecraft:condition', 'property': prop, 'on_true': on_true, 'on_false': on_false}

def definition(item):
    path = os.path.join(MODELS, item + '.json')
    data = json.load(open(path))
    base = 'spartanshields:item/' + item
    models = {(0, 0): base}
    for o in data.get('overrides', []):
        p = o['predicate']
        models[(int(p.get('minecraft:blocking', 0)), int(p.get('minecraft:disabled', 0)))] = o['model']
    def by_blocking(disabled):
        off = models.get((0, disabled), base)
        on = models.get((1, disabled), off)
        return leaf(item, off) if on == off else condition('minecraft:using_item', leaf(item, on), leaf(item, off))
    tree = by_blocking(0)
    if any(d for _, d in models):
        tree = condition('spartanshields:powered_off', by_blocking(1), tree)
    if 'overrides' in data:
        del data['overrides']
        json.dump(data, open(path, 'w'), indent=2)
        open(path, 'a').write('\n')
    return {'model': tree}

os.makedirs(ITEMS, exist_ok=True)
for item in ids():
    json.dump(definition(item), open(os.path.join(ITEMS, item + '.json'), 'w'), indent=2)
    open(os.path.join(ITEMS, item + '.json'), 'a').write('\n')
print(len(ids()), 'item definitions')

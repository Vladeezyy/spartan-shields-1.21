#!/usr/bin/env python3
"""Rebuilds src/main/resources/{assets,data} for 1.21.1 from the 1.20.1 Spartan Shields resources.

    python3 -I tools/convert_resources.py <SpartanShields 1.20.1 checkout> src/main/resources

Inputs: <checkout>/src/main/resources (hand-written assets) and <checkout>/src/generated/resources (datagen output:
recipes, advancements, tags, item models, sounds). Hand-written 1.21 data in tools/overlay/ is copied on top
(lang files there are merged key by key). Running it again gives the same result.
"""
import json
import os
import re
import shutil
import sys

MOD = 'spartanshields'

# 1.20.1 forge tags -> 1.21.1 common (c) tags
FORGE_TAGS = {
    'forge:cobblestone': 'c:cobblestones',
    'forge:obsidian': 'c:obsidians',
}
CONDITIONS = {
    'forge:mod_loaded': 'neoforge:mod_loaded',
    'forge:not': 'neoforge:not',
    'forge:tag_empty': 'neoforge:tag_empty',
}


def tag(name):
    if name in FORGE_TAGS:
        return FORGE_TAGS[name]
    if name.startswith('forge:'):
        return 'c:' + name[len('forge:'):]
    return name


def condition(c):
    c = dict(c)
    c['type'] = CONDITIONS.get(c['type'], c['type'])
    if 'tag' in c:
        c['tag'] = tag(c['tag'])
    if 'value' in c:
        c['value'] = condition(c['value'])
    return c


def ingredient(i):
    if isinstance(i, list):
        return [ingredient(x) for x in i]
    i = dict(i)
    if 'tag' in i:
        i['tag'] = tag(i['tag'])
    return i


def recipe(d):
    out = {}
    for k, v in d.items():
        if k == 'conditions':
            out['neoforge:conditions'] = [condition(c) for c in v]
        elif k == 'key':
            out[k] = {s: ingredient(i) for s, i in v.items()}
        elif k in ('base', 'addition', 'template', 'ingredient'):
            out[k] = ingredient(v)
        elif k == 'ingredients':
            out[k] = [ingredient(i) for i in v]
        elif k == 'result':
            r = dict(v)
            if 'item' in r:
                r['id'] = r.pop('item')
            out[k] = r
        else:
            out[k] = v
    return out


def item_predicate(p):
    if 'tag' in p:
        return {'items': '#' + tag(p['tag'])}
    if 'items' in p:
        items = p['items']
        return {'items': items[0] if len(items) == 1 else items}
    return p


def advancement(d, recipe_conditions):
    out = {}
    if recipe_conditions:
        out['neoforge:conditions'] = recipe_conditions
    for k, v in d.items():
        if k == 'criteria':
            crit = {}
            for name, c in v.items():
                c = json.loads(json.dumps(c))
                cond = c.get('conditions', {})
                if 'items' in cond:
                    cond['items'] = [item_predicate(p) for p in cond['items']]
                crit[name] = c
            out[k] = crit
        else:
            out[k] = v
    return out


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write('\n')


def load(path):
    with open(path, encoding='utf-8') as f:
        text = f.read()
    # the 1.20.1 lang file has a // comment line (accepted by the lenient Gson reader, not by json)
    text = re.sub(r'^\s*//.*$', '', text, flags=re.M)
    return json.loads(text)


def copytree(src, dst):
    for root, _, files in os.walk(src):
        for name in files:
            s = os.path.join(root, name)
            d = os.path.join(dst, os.path.relpath(s, src))
            os.makedirs(os.path.dirname(d), exist_ok=True)
            shutil.copyfile(s, d)


def main(upstream, out):
    main_res = os.path.join(upstream, 'src/main/resources')
    gen_res = os.path.join(upstream, 'src/generated/resources')
    for sub in ('assets', 'data'):
        shutil.rmtree(os.path.join(out, sub), ignore_errors=True)

    # --- assets
    src_assets = os.path.join(main_res, 'assets', MOD)
    dst_assets = os.path.join(out, 'assets', MOD)
    copytree(os.path.join(src_assets, 'textures'), os.path.join(dst_assets, 'textures'))
    copytree(os.path.join(src_assets, 'models'), os.path.join(dst_assets, 'models'))
    copytree(os.path.join(gen_res, 'assets', MOD, 'models'), os.path.join(dst_assets, 'models'))
    shutil.copyfile(os.path.join(gen_res, 'assets', MOD, 'sounds.json'), os.path.join(dst_assets, 'sounds.json'))
    for lang in os.listdir(os.path.join(src_assets, 'lang')):
        dump(os.path.join(dst_assets, 'lang', lang), load(os.path.join(src_assets, 'lang', lang)))

    # The 1.20.1 atlas file added the whole textures/entity directory of every namespace (all vanilla and modded mob
    # textures) to the shield atlas. Only the tower shield textures belong there.
    sprites = []
    entity_dir = os.path.join(dst_assets, 'textures', 'entity')
    for root, _, files in os.walk(entity_dir):
        for name in sorted(files):
            if name.endswith('_pattern.png') or name.endswith('_nopattern.png'):
                rel = os.path.relpath(os.path.join(root, name), os.path.join(dst_assets, 'textures'))
                sprites.append(MOD + ':' + rel[:-len('.png')].replace(os.sep, '/'))
    dump(os.path.join(out, 'assets/minecraft/atlases/shield_patterns.json'),
         {'sources': [{'type': 'single', 'resource': s} for s in sorted(sprites)]})

    # --- data
    gen_data = os.path.join(gen_res, 'data')
    recipe_conditions = {}
    rdir = os.path.join(gen_data, MOD, 'recipes')
    for name in sorted(os.listdir(rdir)):
        d = recipe(load(os.path.join(rdir, name)))
        recipe_conditions[MOD + ':' + name[:-5]] = d.get('neoforge:conditions')
        dump(os.path.join(out, 'data', MOD, 'recipe', name), d)

    adir = os.path.join(gen_data, MOD, 'advancements')
    for root, _, files in os.walk(adir):
        for name in sorted(files):
            src = os.path.join(root, name)
            d = load(src)
            unlocked = d.get('rewards', {}).get('recipes', [])
            conds = recipe_conditions.get(unlocked[0]) if unlocked else None
            dump(os.path.join(out, 'data', MOD, 'advancement', os.path.relpath(src, adir)), advancement(d, conds))

    for ns in os.listdir(gen_data):
        tdir = os.path.join(gen_data, ns, 'tags', 'items')
        if not os.path.isdir(tdir):
            continue
        for root, _, files in os.walk(tdir):
            for name in sorted(files):
                src = os.path.join(root, name)
                rel = os.path.relpath(src, tdir)
                name_in = ns + ':' + rel[:-5].replace(os.sep, '/')
                ns_out, path_out = tag(name_in).split(':', 1)
                d = load(src)
                d['values'] = [('#' + tag(v[1:])) if isinstance(v, str) and v.startswith('#') else v for v in d['values']]
                dump(os.path.join(out, 'data', ns_out, 'tags', 'item', path_out + '.json'), d)

    # 1.20.1 shield enchantment category: item instanceof ShieldBaseItem = every shield of this mod (the basic and tower
    # shield tags also list optional Mekanism Tools shields, which could not take these enchantments)
    shields = []
    for t in ('basic_shields', 'tower_shields'):
        shields += [v for v in load(os.path.join(out, 'data', MOD, 'tags', 'item', t + '.json'))['values']
                    if isinstance(v, str) and v.startswith(MOD + ':')]
    dump(os.path.join(out, 'data', MOD, 'tags', 'item', 'enchantable', 'shield.json'), {'values': shields})

    # --- hand-written 1.21 data on top
    overlay = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'overlay')
    for root, _, files in os.walk(overlay):
        for name in files:
            src = os.path.join(root, name)
            dst = os.path.join(out, os.path.relpath(src, overlay))
            if '/lang/' in src.replace(os.sep, '/') and os.path.exists(dst):
                merged = load(dst)
                merged.update(load(src))
                dump(dst, merged)
            else:
                os.makedirs(os.path.dirname(dst), exist_ok=True)
                shutil.copyfile(src, dst)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])

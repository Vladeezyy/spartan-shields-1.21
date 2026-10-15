#!/usr/bin/env python3
"""Writes tools/overlay/assets/spartanshields/lang/{ru_ru,uk_ua,de_de,es_es,pt_br}.json (the required languages).

    python3 -I tools/gen_translations.py src/main/resources/assets/spartanshields/lang/en_us.json

Shield names are built from the material and the style word of the English name; the rest is a table. Vanilla terms
follow the game's own translations (Shield, Thorns, Knockback, Banner Pattern, Attack/Destroy, Glowstone ...).
Machine translations (no native speaker checked them). Run tools/convert_resources.py afterwards.
"""
import json
import os
import re
import sys

LANGS = ['ru_ru', 'uk_ua', 'de_de', 'es_es', 'pt_br']

# material id: ru genitive, uk genitive, de noun (after "aus"), es noun, pt noun, ru instrumental, uk instrumental
MAT = {
    'wooden': ('дерева', 'дерева', 'Holz', 'madera', 'madeira', 'деревом', 'деревом'),
    'stone': ('камня', 'каменю', 'Stein', 'piedra', 'pedra', 'камнем', 'каменем'),
    'copper': ('меди', 'міді', 'Kupfer', 'cobre', 'cobre', 'медью', 'міддю'),
    'iron': ('железа', 'заліза', 'Eisen', 'hierro', 'ferro', 'железом', 'залізом'),
    'golden': ('золота', 'золота', 'Gold', 'oro', 'ouro', 'золотом', 'золотом'),
    'diamond': ('алмаза', 'діаманта', 'Diamant', 'diamante', 'diamante', 'алмазом', 'діамантом'),
    'netherite': ('незерита', 'незериту', 'Netherit', 'netherita', 'netherita', 'незеритом', 'незеритом'),
    'obsidian': ('обсидиана', 'обсидіану', 'Obsidian', 'obsidiana', 'obsidiana', 'обсидианом', 'обсидіаном'),
    'tin': ('олова', 'олова', 'Zinn', 'estaño', 'estanho', 'оловом', 'оловом'),
    'bronze': ('бронзы', 'бронзи', 'Bronze', 'bronce', 'bronze', 'бронзой', 'бронзою'),
    'steel': ('стали', 'сталі', 'Stahl', 'acero', 'aço', 'сталью', 'сталлю'),
    'silver': ('серебра', 'срібла', 'Silber', 'plata', 'prata', 'серебром', 'сріблом'),
    'electrum': ('электрума', 'електруму', 'Elektrum', 'electro', 'electro', 'электрумом', 'електрумом'),
    'lead': ('свинца', 'свинцю', 'Blei', 'plomo', 'chumbo', 'свинцом', 'свинцем'),
    'nickel': ('никеля', 'нікелю', 'Nickel', 'níquel', 'níquel', 'никелем', 'нікелем'),
    'invar': ('инвара', 'інвару', 'Invar', 'invar', 'invar', 'инваром', 'інваром'),
    'constantan': ('константана', 'константану', 'Konstantan', 'constantán', 'constantan', 'константаном', 'константаном'),
    'platinum': ('платины', 'платини', 'Platin', 'platino', 'platina', 'платиной', 'платиною'),
    'aluminum': ('алюминия', 'алюмінію', 'Aluminium', 'aluminio', 'alumínio', 'алюминием', 'алюмінієм'),
    'manasteel': ('манастали', 'манасталі', 'Manastahl', 'acero de maná', 'aço de mana', 'манасталью', 'манасталлю'),
    'terrasteel': ('террастали', 'терасталі', 'Terrastahl', 'terracero', 'terraço', 'террасталью', 'терасталлю'),
    'elementium': ('элементиума', 'елементію', 'Elementium', 'elementium', 'elementium', 'элементиумом', 'елементієм'),
    'soulforged_steel': ('душекованой стали', 'душекованої сталі', 'Seelenstahl', 'acero forjado con almas', 'aço forjado com almas', 'душекованой сталью', 'душекованою сталлю'),
    'darkstone': ('тёмного камня', 'темного каменю', 'Dunkelstein', 'piedra oscura', 'pedra escura', 'тёмным камнем', 'темним каменем'),
    'abyssalnite': ('абиссалнита', 'абісалніту', 'Abyssalnit', 'abisalnita', 'abissalnita', 'абиссалнитом', 'абісалнітом'),
    'coralium': ('коралиума', 'коралію', 'Coralium', 'coralium', 'coralium', 'коралиумом', 'коралієм'),
    'dreadium': ('дредиума', 'дредію', 'Dreadium', 'dreadium', 'dreadium', 'дредиумом', 'дредієм'),
    'ethaxium': ('этаксиума', 'етаксію', 'Ethaxium', 'ethaxium', 'ethaxium', 'этаксиумом', 'етаксієм'),
    'osmium': ('осмия', 'осмію', 'Osmium', 'osmio', 'ósmio', 'осмием', 'осмієм'),
    'lapis_lazuli': ('лазурита', 'лазуриту', 'Lapislazuli', 'lapislázuli', 'lápis-lazúli', 'лазуритом', 'лазуритом'),
    'refined_glowstone': ('очищенного светокамня', 'очищеного світлокаменю', 'raffiniertem Leuchtstein', 'piedra luminosa refinada', 'pedra luminosa refinada', 'очищенным светокамнем', 'очищеним світлокаменем'),
    'refined_obsidian': ('очищенного обсидиана', 'очищеного обсидіану', 'raffiniertem Obsidian', 'obsidiana refinada', 'obsidiana refinada', 'очищенным обсидианом', 'очищеним обсидіаном'),
    'signalum': ('сигналума', 'сигналуму', 'Signalum', 'signalum', 'signalum', 'сигналумом', 'сигналумом'),
    'lumium': ('люмиума', 'люміуму', 'Lumium', 'lumium', 'lumium', 'люмиумом', 'люміумом'),
    'enderium': ('эндериума', 'ендеріуму', 'Enderium', 'enderium', 'enderium', 'эндериумом', 'ендеріумом'),
}

# style word of the English basic shield name -> per language template ({g} genitive, {i} instrumental, {n} noun)
STYLE = {
    'Braced': ('Щит с оковкой из {g}', 'Щит з окуттям із {g}', 'Beschlagener Schild aus {n}', 'Escudo ribeteado de {n}', 'Escudo com reforço de {n}'),
    'Plated': ('Щит с обшивкой из {g}', 'Щит з обшивкою із {g}', 'Gepanzerter Schild aus {n}', 'Escudo chapado en {n}', 'Escudo chapeado de {n}'),
    'Gilded': ('Щит с отделкой из {g}', 'Щит з оздобленням із {g}', 'Verzierter Schild aus {n}', 'Escudo bañado en {n}', 'Escudo banhado a {n}'),
    'Reinforced': ('Щит, укреплённый {i}', 'Щит, укріплений {i}', 'Verstärkter Schild aus {n}', 'Escudo reforzado con {n}', 'Escudo reforçado com {n}'),
    'Strengthened': ('Щит, усиленный {i}', 'Щит, посилений {i}', 'Gehärteter Schild aus {n}', 'Escudo fortalecido con {n}', 'Escudo fortalecido com {n}'),
}
TOWER = ('Башенный щит из {g}', 'Баштовий щит із {g}', 'Turmschild aus {n}', 'Escudo torre de {n}', 'Escudo torre de {n}')
INGOT = ('Слиток {g}', 'Злиток {g}', '{n}barren', 'Lingote de {n}', 'Lingote de {n}')

FIXED = {
    'item.spartanshields.wooden_basic_shield': ('Грубый деревянный щит', 'Грубий дерев\'яний щит', 'Grober Holzschild', 'Escudo tosco de madera', 'Escudo rústico de madeira'),
    'item.spartanshields.golden_basic_shield': ('Позолоченный щит', 'Позолочений щит', 'Vergoldeter Schild', 'Escudo bañado en oro', 'Escudo banhado a ouro'),
    'item.spartanshields.silver_basic_shield': ('Посеребрённый щит', 'Посріблений щит', 'Versilberter Schild', 'Escudo bañado en plata', 'Escudo banhado a prata'),
    'item.spartanshields.obsidian_basic_shield': ('Массивный обсидиановый щит', 'Масивний обсидіановий щит', 'Massiver Obsidianschild', 'Escudo voluminoso de obsidiana', 'Escudo robusto de obsidiana'),
    'item.spartanshields.lumium_basic_shield': ('Сияющий щит из люмиума', 'Сяйливий щит із люміуму', 'Strahlender Schild aus Lumium', 'Escudo radiante de lumium', 'Escudo radiante de lumium'),
    'item.spartanshields.dark_steel_riot_basic_shield': ('Щит спецназа из тёмной стали', 'Щит спецпризначенця з темної сталі', 'Dunkelstahl-Einsatzschild', 'Escudo antidisturbios de acero oscuro', 'Escudo antimotim de aço escuro'),
    'item.spartanshields.dark_steel_riot_tower_shield': ('Башенный щит спецназа из тёмной стали', 'Баштовий щит спецпризначенця з темної сталі', 'Dunkelstahl-Einsatzturmschild', 'Escudo torre antidisturbios de acero oscuro', 'Escudo torre antimotim de aço escuro'),
    'item.spartanshields.flux_infused_shield': ('Щит, насыщенный флюксом', 'Щит, насичений флюксом', 'Fluxdurchtränkter Schild', 'Escudo infundido de flujo', 'Escudo infundido com fluxo'),
    'item.spartanshields.infused_riot_shield': ('Насыщенный щит спецназа', 'Насичений щит спецпризначенця', 'Durchtränkter Einsatzschild', 'Escudo antidisturbios infundido', 'Escudo antimotim infundido'),
    'item.spartanshields.infused_tower_shield': ('Насыщенный башенный щит', 'Насичений баштовий щит', 'Durchtränkter Turmschild', 'Escudo torre infundido', 'Escudo torre infundido'),
    'mod.spartanshields.botania': ('Botania',) * 5,
    'mod.spartanshields.mekanism': ('Mekanism',) * 5,
    'itemGroup.spartanshields': ('Spartan Shields',) * 5,
    'gui.spartanshields.category.shield_banner': ('Нанесение флага на щит', 'Нанесення стяга на щит', 'Schild mit Banner verzieren', 'Decorar escudo con estandarte', 'Decorar escudo com estandarte'),
    'gui.spartanshields.tooltip.shield_banner.any_pattern': ('Результат зависит от узора флага', 'Результат залежить від візерунка стяга', 'Das Ergebnis hängt vom Bannermuster ab', 'El resultado depende del diseño del estandarte', 'O resultado depende do padrão do estandarte'),
    'tooltip.spartanshields.dev.wip': ('В разработке (WIP)', 'У розробці (WIP)', 'In Arbeit (WIP)', 'En desarrollo (WIP)', 'Em desenvolvimento (WIP)'),
    'tooltip.spartanshields.dev.unimplemented': ('Ещё не реализовано!', 'Ще не реалізовано!', 'Noch nicht umgesetzt!', '¡Aún no implementado!', 'Ainda não implementado!'),
    'tooltip.spartanshields.protection': ('Макс. защита: %d', 'Макс. захист: %d', 'Max. Schutz: %d', 'Protección máx.: %d', 'Proteção máx.: %d'),
    'tooltip.spartanshields.shield_bash': ('Удар щитом: %s', 'Удар щитом: %s', 'Schildstoß: %s', 'Golpe de escudo: %s', 'Golpe de escudo: %s'),
    'tooltip.spartanshields.shield_bash.value': ('Блок + %s [%s]', 'Блок + %s [%s]', 'Blocken + %s [%s]', 'Bloquear + %s [%s]', 'Bloquear + %s [%s]'),
    'tooltip.spartanshields.shield_bash_target_single': ('Цель: одна', 'Ціль: одна', 'Ziel: einzeln', 'Objetivo: único', 'Alvo: único'),
    'tooltip.spartanshields.shield_bash_target_multiple': ('Цель: все вокруг', 'Ціль: усі навколо', 'Ziel: Rundumschlag', 'Objetivo: barrido', 'Alvo: varredura'),
    'tooltip.spartanshields.payback_bonus': ('Бонус урона от Расплаты: %f', 'Бонус шкоди від Відплати: %f', 'Vergeltungs-Schadensbonus: %f', 'Bonificación de daño de Represalia: %f', 'Bônus de dano de Revide: %f'),
    'tooltip.spartanshields.on_block': ('При блоке: %s', 'Під час блоку: %s', 'Beim Blocken: %s', 'Al bloquear: %s', 'Ao bloquear: %s'),
    'tooltip.spartanshields.shield_silver.desc': ('Ранит нежить', 'Ранить нежить', 'Verletzt Untote', 'Daña a los no muertos', 'Fere mortos-vivos'),
    'tooltip.spartanshields.inflict_mob_effect.desc': ('Накладывает %s на %d с', 'Накладає %s на %d с', 'Verursacht %s für %d s', 'Inflige %s durante %d s', 'Aplica %s por %d s'),
    'tooltip.spartanshields.shield_signalum.desc': ('Накладывает на врагов Слабость (%d с)', 'Накладає на ворогів Слабкість (%d с)', 'Verursacht Schwäche (%d s) bei Gegnern', 'Inflige Debilidad (%d s) a los enemigos', 'Aplica Fraqueza (%d s) aos inimigos'),
    'tooltip.spartanshields.shield_sumium.desc': ('Накладывает на врагов Свечение (%d с)', 'Накладає на ворогів Світіння (%d с)', 'Verursacht Leuchten (%d s) bei Gegnern', 'Inflige Luminiscencia (%d s) a los enemigos', 'Aplica Brilho (%d s) aos inimigos'),
    'tooltip.spartanshields.shield_snderium.desc': ('Накладывает на врагов Замедление (%d с)', 'Накладає на ворогів Повільність (%d с)', 'Verursacht Langsamkeit (%d s) bei Gegnern', 'Inflige Lentitud (%d s) a los enemigos', 'Aplica Lentidão (%d s) aos inimigos'),
    'tooltip.spartanshields.shield_elementium.desc': ('Призывает пикси (шанс %d%%)', 'Викликає піксі (шанс %d%%)', 'Beschwört einen Pixie (%d%% Chance)', 'Invoca un duendecillo (%d%% de probabilidad)', 'Invoca uma fada (%d%% de chance)'),
    'tooltip.spartanshields.rf_capacity': ('%s/%s RF',) * 5,
    'tooltip.spartanshields.rf_charge_rate': ('Зарядка: %s RF/т', 'Заряджання: %s RF/т', 'Laden: %s RF/t', 'Carga: %s RF/t', 'Carga: %s RF/t'),
    'tooltip.spartanshields.rf_per_damage': ('Тратит %s RF на сердце поглощённого урона', 'Витрачає %s RF на серце поглиненої шкоди', 'Verbraucht %s RF pro absorbiertem Herz Schaden', 'Usa %s RF por cada corazón de daño absorbido', 'Usa %s RF por coração de dano absorvido'),
    'tooltip.spartanshields.rf_shield.desc': ('Работает на Redstone Flux', 'Працює на Redstone Flux', 'Wird mit Redstone Flux betrieben', 'Funciona con Redstone Flux', 'Funciona com Redstone Flux'),
    'tooltip.spartanshields.ui_capacity': ('%s/%s µI',) * 5,
    'tooltip.spartanshields.ui_charge_rate': ('Зарядка: %s µI/т', 'Заряджання: %s µI/т', 'Laden: %s µI/t', 'Carga: %s µI/t', 'Carga: %s µI/t'),
    'tooltip.spartanshields.ui_per_damage': ('Тратит %s µI на сердце поглощённого урона', 'Витрачає %s µI на серце поглиненої шкоди', 'Verbraucht %s µI pro absorbiertem Herz Schaden', 'Usa %s µI por cada corazón de daño absorbido', 'Usa %s µI por coração de dano absorvido'),
    'tooltip.spartanshields.fe_capacity': ('%s/%s FE',) * 5,
    'tooltip.spartanshields.fe_charge_rate': ('Зарядка: %s FE/т', 'Заряджання: %s FE/т', 'Laden: %s FE/t', 'Carga: %s FE/t', 'Carga: %s FE/t'),
    'tooltip.spartanshields.fe_per_damage': ('Тратит %s FE на сердце поглощённого урона', 'Витрачає %s FE на серце поглиненої шкоди', 'Verbraucht %s FE pro absorbiertem Herz Schaden', 'Usa %s FE por cada corazón de daño absorbido', 'Usa %s FE por coração de dano absorvido'),
    'tooltip.spartanshields.fe_shield.desc': ('Принимает Forge Energy', 'Приймає Forge Energy', 'Nimmt Forge Energy auf', 'Acepta Forge Energy', 'Aceita Forge Energy'),
    'tooltip.spartanshields.has_patterns': ('Узоры флага на щите:', 'Візерунки стяга на щиті:', 'Hat folgende Bannermuster:', 'Tiene estos diseños de estandarte:', 'Tem estes padrões de estandarte:'),
    'tooltip.spartanshields.uncraftable_missing_mods': ('Нельзя скрафтить: мод «%s» не загружен!', 'Не можна скрафтити: мод «%s» не завантажено!', 'Nicht herstellbar: Die Mod „%s“ ist nicht geladen!', 'No se puede fabricar: ¡el mod «%s» no está cargado!', 'Não pode ser fabricado: o mod "%s" não está carregado!'),
    'tooltip.spartanshields.uncraftable_missing_material': ('Нельзя скрафтить: нет предметов в теге «%s»', 'Не можна скрафтити: немає предметів у тегу «%s»', 'Nicht herstellbar: Kein Gegenstand im Tag „%s“', 'No se puede fabricar: no hay objetos en la etiqueta «%s»', 'Não pode ser fabricado: nenhum item na tag "%s"'),
    'enchantment.spartanshields.spikes': ('Колючки', 'Колючки', 'Stacheln', 'Púas', 'Cravos'),
    'enchantment.spartanshields.spikes.desc': ('Ранит врагов, которые бьют по поднятому щиту', 'Ранить ворогів, які б\'ють по піднятому щиту', 'Verletzt Gegner, die den erhobenen Schild treffen', 'Daña a los enemigos que golpean el escudo mientras bloqueas', 'Fere os inimigos que atingem o escudo enquanto você bloqueia'),
    'enchantment.spartanshields.firebrand': ('Огненное клеймо', 'Вогняне тавро', 'Brandmal', 'Marca ígnea', 'Marca de fogo'),
    'enchantment.spartanshields.firebrand.desc': ('Удар щитом поджигает врагов. Чем выше уровень, тем дольше горение', 'Удар щитом підпалює ворогів. Чим вищий рівень, тим довше горіння', 'Ein Schildstoß setzt Gegner in Brand. Höhere Stufen verlängern das Brennen', 'El golpe de escudo prende fuego a los enemigos. Los niveles altos alargan el fuego', 'O golpe de escudo incendeia os inimigos. Níveis maiores aumentam o tempo em chamas'),
    'enchantment.spartanshields.payback': ('Расплата', 'Відплата', 'Vergeltung', 'Represalia', 'Revide'),
    'enchantment.spartanshields.payback.desc': ('Заблокированные атаки накапливают в щите 50% урона, который выпускается более сильным ударом щитом. Чем выше уровень, тем больше запас', 'Заблоковані атаки накопичують у щиті 50% шкоди, яку випускає сильніший удар щитом. Чим вищий рівень, тим більший запас', 'Geblockte Angriffe speichern 50 % des Schadens im Schild, der als stärkerer Schildstoß entfesselt wird. Höhere Stufen erhöhen die Speichergrenze', 'Los ataques bloqueados acumulan el 50 % del daño en el escudo, que se libera en un golpe de escudo más fuerte. Los niveles altos aumentan el límite', 'Ataques bloqueados acumulam 50% do dano no escudo, liberado num golpe de escudo mais forte. Níveis maiores aumentam o limite'),
    'enchantment.spartanshields.deflect': ('Отражение', 'Відбиття', 'Ablenkung', 'Desvío', 'Desvio'),
    'stat.spartanshields.shield_bash_hits': ('[Spartan Shields] Попаданий ударом щитом', '[Spartan Shields] Влучань ударом щитом', '[Spartan Shields] Schildstoß-Treffer', '[Spartan Shields] Golpes de escudo acertados', '[Spartan Shields] Golpes de escudo acertados'),
    'subtitle.spartanshields.shield.bash.hit': ('Удар щитом по мобу', 'Удар щитом по мобу', 'Schildstoß trifft', 'Golpe de escudo acierta', 'Golpe de escudo acerta'),
    'subtitle.spartanshields.shield.bash.miss': ('Удар щитом мимо', 'Удар щитом повз', 'Schildstoß verfehlt', 'Golpe de escudo falla', 'Golpe de escudo erra'),
    'subtitle.spartanshields.shield.payback.charge': ('Расплата щита заряжена', 'Відплату щита заряджено', 'Schild-Vergeltung aufgeladen', 'Represalia del escudo cargada', 'Revide do escudo carregado'),
    'message.spartanshields.powered_shield_block_fail': ('Нельзя поглотить урон: у %s кончилась энергия.', 'Не можна поглинути шкоду: у %s скінчилася енергія.', 'Kein Schaden absorbierbar: %s hat keine Energie mehr.', 'No se puede absorber el daño: %s no tiene energía.', 'Não é possível absorver dano: %s está sem energia.'),
    'message.spartanshields.event_bus_shutdown_error': ('[Критическая ошибка] Другой мод остановил шину событий до или во время регистрации, и Spartan Shields не смог зарегистрировать свои предметы! Найдите этот мод по логу!', '[Критична помилка] Інший мод зупинив шину подій до або під час реєстрації, і Spartan Shields не зміг зареєструвати свої предмети! Знайдіть цей мод у лозі!', '[Kritischer Fehler] Eine Mod hat den Event-Bus vor oder während der Registrierung gestoppt, und Spartan Shields konnte seine Gegenstände nicht registrieren! Sieh im Log nach, welche Mod schuld ist!', '[Error crítico] ¡Un mod detuvo el bus de eventos antes o durante el registro y Spartan Shields no pudo registrar sus objetos! ¡Revisa el registro para saber qué mod lo causó!', '[Erro crítico] Um mod parou o barramento de eventos antes ou durante o registro e o Spartan Shields não conseguiu registrar seus itens! Veja no log qual mod causou isso!'),
    'key.spartanshields.alt_shield_bash': ('Дополнительная клавиша удара щитом', 'Додаткова клавіша удару щитом', 'Alternativer Schildstoß', 'Golpe de escudo alternativo', 'Golpe de escudo alternativo'),
    'key.spartanshields.category': ('Spartan Shields',) * 5,
}

MEK_TIER = {'basic': ('Базовый', 'Базовий', 'Einfacher', 'básico', 'básico'),
            'advanced': ('Продвинутый', 'Просунутий', 'Fortgeschrittener', 'avanzado', 'avançado'),
            'elite': ('Элитный', 'Елітний', 'Elite-', 'de élite', 'de elite'),
            'ultimate': ('Абсолютный', 'Абсолютний', 'Ultimativer', 'definitivo', 'supremo')}


def mekanist(tier, tower):
    t = MEK_TIER[tier]
    if tower:
        de = t[2] + 'Mechaniker-Turmschild' if t[2].endswith('-') else t[2] + ' Mechaniker-Turmschild'
        return (t[0] + ' башенный щит механика', t[1] + ' баштовий щит механіка', de,
                'Escudo torre de mecanista ' + t[3], 'Escudo torre de mecanista ' + t[4])
    de = t[2] + 'Mechanikerschild' if t[2].endswith('-') else t[2] + ' Mechanikerschild'
    return (t[0] + ' щит механика', t[1] + ' щит механіка', de, 'Escudo de mecanista ' + t[3], 'Escudo de mecanista ' + t[4])


def fill(template, mat):
    g_ru, g_uk, n_de, n_es, n_pt, i_ru, i_uk = MAT[mat]
    return (template[0].format(g=g_ru, i=i_ru), template[1].format(g=g_uk, i=i_uk), template[2].format(n=n_de),
            template[3].format(n=n_es), template[4].format(n=n_pt))


def translate(key, en):
    if key in FIXED:
        return FIXED[key]
    m = re.fullmatch(r'item\.spartanshields\.(basic|advanced|elite|ultimate)_mekanists_(basic|tower)_shield', key)
    if m:
        return mekanist(m.group(1), m.group(2) == 'tower')
    m = re.fullmatch(r'item\.spartanshields\.(.+)_(basic|tower)_shield', key)
    if m and m.group(1) in MAT:
        if m.group(2) == 'tower':
            return fill(TOWER, m.group(1))
        style = next((w for w in STYLE if w in en), None)
        if style:
            return fill(STYLE[style], m.group(1))
    m = re.fullmatch(r'item\.spartanshields\.(.+)_ingot', key)
    if m and m.group(1) in MAT:
        t = fill(INGOT, m.group(1))
        return t[:2] + (t[2],) + t[3:]
    if key.startswith('book.'):
        return None  # Patchouli pages for Botania: kept in English below
    raise KeyError('no translation for ' + key + ' = ' + en)


BOOK = {
    'book.spartanshields.entry.manasteel_shield': ('Щит из манастали', 'Щит із манасталі', 'Manastahlschild', 'Escudo de acero de maná', 'Escudo de aço de mana'),
    'book.spartanshields.entry.terrasteel_shield': ('Щит из террастали', 'Щит із терасталі', 'Terrastahlschild', 'Escudo de terracero', 'Escudo de terraço'),
    'book.spartanshields.entry.elementium_shield': ('Щит из элементиума', 'Щит із елементію', 'Elementiumschild', 'Escudo de elementium', 'Escudo de elementium'),
}


def main(en_path):
    with open(en_path, encoding='utf-8') as f:
        en = json.load(f)
    out_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'overlay', 'assets', 'spartanshields', 'lang')
    # names the pages refer to
    for lang_i, lang in enumerate(LANGS):
        data = {}
        for key, value in en.items():
            t = BOOK.get(key) or translate(key, value)
            if t is None:
                # the Patchouli pages of the Botania lexicon: the item names inside are translated, the text stays English
                page = re.fullmatch(r'book\.spartanshields\.page\.(manasteel|terrasteel|elementium)_shield1', key)
                if page:
                    t = translate('item.spartanshields.%s_basic_shield' % page.group(1), en['item.spartanshields.%s_basic_shield' % page.group(1)])
                    data[key] = t[lang_i]
                else:
                    data[key] = value
                continue
            data[key] = t[lang_i]
        with open(os.path.join(out_dir, lang + '.json'), 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write('\n')


if __name__ == '__main__':
    main(sys.argv[1])

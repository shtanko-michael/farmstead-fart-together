"""Fart Together translations: one table, twelve lang files, and the completeness check.

    python -I tools/lang.py          write src/main/resources/assets/farmstead_fart_together/lang/*.json, then check
    python -I tools/lang.py --check  only check (CI): every language has every key, placeholders match

Each entry: key -> 12 strings in LANGS order. English and Russian are written by hand, the rest translate the meaning,
not the words. Family rating: neutral, playful words only ("toot", ru "залп"); no crude terms in any language.
"Fart Together" stays as is. %s / %d placeholders must match English.
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/farmstead_fart_together/lang"
LANGS = ["en_us", "ru_ru", "zh_cn", "pt_br", "es_es", "de_de", "fr_fr", "pl_pl", "uk_ua", "ja_jp", "ko_kr", "tr_tr"]

T = {
    "config.farmstead_fart_together.about.current": [
        "You have the latest version.",
        "У вас последняя версия.",
        "你用的是最新版本。",
        "Você tem a versão mais recente.",
        "Tienes la última versión.",
        "Du hast die neueste Version.",
        "Vous avez la dernière version.",
        "Masz najnowszą wersję.",
        "У вас остання версія.",
        "最新バージョンです。",
        "최신 버전입니다.",
        "En son sürüme sahipsiniz.",
    ],
    "config.farmstead_fart_together.about.curseforge": [
        "CurseForge page",
        "Страница на CurseForge",
        "CurseForge 页面",
        "Página no CurseForge",
        "Página en CurseForge",
        "CurseForge-Seite",
        "Page CurseForge",
        "Strona na CurseForge",
        "Сторінка на CurseForge",
        "CurseForge のページ",
        "CurseForge 페이지",
        "CurseForge sayfası",
    ],
    "config.farmstead_fart_together.about.issues": [
        "Report a bug or an idea",
        "Сообщить об ошибке или идее",
        "报告问题或建议",
        "Relatar um bug ou ideia",
        "Informar de un fallo o una idea",
        "Fehler oder Idee melden",
        "Signaler un bug ou une idée",
        "Zgłoś błąd lub pomysł",
        "Повідомити про помилку чи ідею",
        "不具合やアイデアを報告",
        "버그나 아이디어 제보",
        "Hata ya da fikir bildir",
    ],
    "config.farmstead_fart_together.about.modrinth": [
        "Modrinth page",
        "Страница на Modrinth",
        "Modrinth 页面",
        "Página no Modrinth",
        "Página en Modrinth",
        "Modrinth-Seite",
        "Page Modrinth",
        "Strona na Modrinth",
        "Сторінка на Modrinth",
        "Modrinth のページ",
        "Modrinth 페이지",
        "Modrinth sayfası",
    ],
    "config.farmstead_fart_together.about.newer": [
        "A newer version is out: %s",
        "Вышла новая версия: %s",
        "有新版本：%s",
        "Saiu uma versão nova: %s",
        "Hay una versión nueva: %s",
        "Eine neuere Version ist erschienen: %s",
        "Une nouvelle version est sortie : %s",
        "Wyszła nowa wersja: %s",
        "Вийшла нова версія: %s",
        "新しいバージョンがあります：%s",
        "새 버전이 나왔습니다: %s",
        "Yeni bir sürüm çıktı: %s",
    ],
    "config.farmstead_fart_together.about.unknown": [
        "Update check: not done (switched off, or no answer from Modrinth)",
        "Проверка обновлений: не выполнена (выключена или Modrinth не ответил)",
        "更新检查：未进行（已关闭，或 Modrinth 没有响应）",
        "Verificação de atualização: não feita (desligada ou sem resposta do Modrinth)",
        "Búsqueda de actualizaciones: no hecha (desactivada o Modrinth no respondió)",
        "Update-Prüfung: nicht erfolgt (abgeschaltet oder keine Antwort von Modrinth)",
        "Vérification des mises à jour : non faite (désactivée ou pas de réponse de Modrinth)",
        "Sprawdzanie aktualizacji: nie wykonano (wyłączone albo Modrinth nie odpowiedział)",
        "Перевірка оновлень: не виконана (вимкнена або Modrinth не відповів)",
        "更新の確認：未実施（オフ、または Modrinth から応答なし）",
        "업데이트 확인: 안 함(꺼져 있거나 Modrinth 응답 없음)",
        "Güncelleme denetimi: yapılmadı (kapalı ya da Modrinth yanıt vermedi)",
    ],
    "config.farmstead_fart_together.general.checkUpdates": [
        "Check for updates",
        "Проверять обновления",
        "检查更新",
        "Verificar atualizações",
        "Buscar actualizaciones",
        "Nach Updates suchen",
        "Rechercher les mises à jour",
        "Sprawdzaj aktualizacje",
        "Перевіряти оновлення",
        "更新を確認",
        "업데이트 확인",
        "Güncellemeleri denetle",
    ],
    "config.farmstead_fart_together.general.checkUpdates.tooltip": [
        "Once at start-up, ask Modrinth whether a newer Fart Together is out and say so in the log. Sends nothing about you.",
        "Один раз при запуске спросить Modrinth, не вышла ли новая версия Fart Together, и написать об этом в лог. О вас ничего не отправляется.",
        "启动时向 Modrinth 查询一次是否有新版 Fart Together，并写入日志。不会发送任何关于你的信息。",
        "Ao iniciar, pergunta uma vez ao Modrinth se há uma versão nova do Fart Together e avisa no log. Nada sobre você é enviado.",
        "Al arrancar, pregunta una vez a Modrinth si hay una versión nueva de Fart Together y lo indica en el registro. No envía nada sobre ti.",
        "Fragt beim Start einmal bei Modrinth nach einer neueren Fart-Together-Version und meldet sie im Log. Über dich wird nichts gesendet.",
        "Au démarrage, demande une fois à Modrinth si une nouvelle version de Fart Together est sortie et l'indique dans le journal. Rien sur vous n'est envoyé.",
        "Przy starcie raz pyta Modrinth, czy jest nowsza wersja Fart Together, i zapisuje to w logu. Nic o tobie nie jest wysyłane.",
        "Один раз під час запуску запитати Modrinth, чи вийшла нова версія Fart Together, і написати про це в лог. Про вас нічого не надсилається.",
        "起動時に一度だけ Modrinth に Fart Together の新しいバージョンがあるか確認し、ログに書き出します。あなたに関する情報は送信しません。",
        "시작할 때 한 번 Modrinth에 Fart Together 새 버전이 있는지 확인하고 로그에 남깁니다. 사용자에 대한 정보는 보내지 않습니다.",
        "Açılışta bir kez Modrinth'e yeni bir Fart Together sürümü olup olmadığını sorar ve günlüğe yazar. Hakkınızda hiçbir şey gönderilmez.",
    ],
    "config.farmstead_fart_together.general.streamerSafe": [
        "Streamer-safe sounds",
        "Безопасно для стримов",
        "直播安全音效",
        "Sons seguros para lives",
        "Sonidos aptos para directos",
        "Streamer-sichere Töne",
        "Sons adaptés au stream",
        "Dźwięki bezpieczne dla streamów",
        "Безпечно для стримів",
        "配信向けの音",
        "방송용 안전 사운드",
        "Yayın dostu sesler",
    ],
    "config.farmstead_fart_together.general.streamerSafe.tooltip": [
        "Every sound becomes a soft puff: for streams and videos with strict platform rules.",
        "Все звуки заменяются мягким «пуф» — для стримов и роликов на площадках со строгими правилами.",
        "所有音效都换成轻柔的“噗”声，适合规则严格的平台上的直播和视频。",
        "Todo som vira um \"puf\" suave: para lives e vídeos em plataformas com regras rígidas.",
        "Todos los sonidos se vuelven un \"puf\" suave: para directos y vídeos en plataformas con reglas estrictas.",
        "Jeder Ton wird zu einem sanften „Puff“: für Streams und Videos auf Plattformen mit strengen Regeln.",
        "Chaque son devient un « pouf » discret : pour les streams et vidéos sur des plateformes aux règles strictes.",
        "Każdy dźwięk zmienia się w delikatne „puf” — na streamy i filmy na platformach o surowych zasadach.",
        "Усі звуки замінюються м'яким «пуф» — для стримів і роликів на майданчиках із суворими правилами.",
        "すべての音がやわらかい「ぷっ」に変わります。規約の厳しい配信や動画向けです。",
        "모든 소리가 부드러운 '푸' 소리로 바뀝니다. 규정이 엄격한 플랫폼의 방송과 영상용입니다.",
        "Tüm sesler yumuşak bir \"puf\"a dönüşür: kuralları sıkı platformlardaki yayınlar ve videolar için.",
    ],
    "config.farmstead_fart_together.particles.amount": [
        "Cloud particles",
        "Частицы облака",
        "云雾粒子",
        "Partículas da nuvem",
        "Partículas de la nube",
        "Wolkenpartikel",
        "Particules du nuage",
        "Cząsteczki chmury",
        "Частинки хмари",
        "雲のパーティクル",
        "구름 입자",
        "Bulut parçacıkları",
    ],
    "config.farmstead_fart_together.particles.amount.tooltip": [
        "How many cloud particles to draw. 0 hides the cloud for you only.",
        "Сколько частиц рисовать в облаке. 0 скрывает облако только у вас.",
        "云雾绘制多少粒子。0 只对你自己隐藏云雾。",
        "Quantas partículas desenhar na nuvem. 0 esconde a nuvem só para você.",
        "Cuántas partículas dibujar en la nube. 0 oculta la nube solo para ti.",
        "Wie viele Partikel die Wolke hat. 0 blendet die Wolke nur für dich aus.",
        "Nombre de particules dans le nuage. 0 cache le nuage pour vous seul.",
        "Ile cząsteczek rysować w chmurze. 0 ukrywa chmurę tylko u ciebie.",
        "Скільки частинок малювати в хмарі. 0 приховує хмару лише у вас.",
        "雲に描くパーティクルの量です。0 にするとあなたの画面でだけ雲が消えます。",
        "구름에 그릴 입자 수입니다. 0이면 나에게만 구름이 보이지 않습니다.",
        "Bulutta kaç parçacık çizileceği. 0 bulutu yalnızca sizin için gizler.",
    ],
    "config.farmstead_fart_together.picker.add": [
        "Add as typed",
        "Добавить как есть",
        "按输入添加",
        "Adicionar como está",
        "Añadir tal cual",
        "So hinzufügen",
        "Ajouter tel quel",
        "Dodaj jak wpisano",
        "Додати як є",
        "入力どおり追加",
        "입력대로 추가",
        "Yazıldığı gibi ekle",
    ],
    "config.farmstead_fart_together.picker.add.tooltip": [
        "Adds exactly what is typed, for a #tag or an id from a mod that is not loaded.",
        "Добавляет ровно то, что введено: #тег или id из мода, который сейчас не загружен.",
        "原样添加输入的内容，用于 #标签 或未加载模组的 ID。",
        "Adiciona exatamente o que foi digitado: uma #tag ou um id de um mod não carregado.",
        "Añade exactamente lo escrito: una #etiqueta o un id de un mod no cargado.",
        "Fügt genau das Eingegebene hinzu, für ein #Tag oder eine ID aus einer nicht geladenen Mod.",
        "Ajoute exactement ce qui est tapé : un #tag ou un id d'un mod non chargé.",
        "Dodaje dokładnie to, co wpisano: #tag albo id z moda, który nie jest wczytany.",
        "Додає рівно те, що введено: #тег або id з мода, який зараз не завантажено.",
        "入力したとおりに追加します。#タグや読み込まれていない MOD の ID 用です。",
        "입력한 그대로 추가합니다. #태그나 로드되지 않은 모드의 ID에 씁니다.",
        "Yazılanı aynen ekler: bir #etiket ya da yüklü olmayan bir modun kimliği.",
    ],
    "config.farmstead_fart_together.preset": [
        "Preset",
        "Пресет",
        "预设",
        "Predefinição",
        "Preajuste",
        "Voreinstellung",
        "Préréglage",
        "Ustawienie wstępne",
        "Пресет",
        "プリセット",
        "프리셋",
        "Ön ayar",
    ],
    "config.farmstead_fart_together.preset.default": [
        "Default",
        "По умолчанию",
        "默认",
        "Padrão",
        "Predeterminado",
        "Standard",
        "Par défaut",
        "Domyślne",
        "За замовчуванням",
        "標準",
        "기본",
        "Varsayılan",
    ],
    "config.farmstead_fart_together.preset.family": [
        "Family",
        "Семейный",
        "家庭",
        "Família",
        "Familiar",
        "Familie",
        "Famille",
        "Rodzinne",
        "Сімейний",
        "ファミリー",
        "가족",
        "Aile",
    ],
    "config.farmstead_fart_together.preset.performance": [
        "Performance",
        "Производительность",
        "性能优先",
        "Desempenho",
        "Rendimiento",
        "Leistung",
        "Performances",
        "Wydajność",
        "Продуктивність",
        "パフォーマンス",
        "성능",
        "Performans",
    ],
    "config.farmstead_fart_together.preset.tooltip": [
        "Sets every value below at once. Values you change afterwards are kept.",
        "Задаёт все значения ниже разом. То, что вы поменяете потом, сохранится.",
        "一次性设置下方所有数值。之后手动修改的数值会被保留。",
        "Define todos os valores abaixo de uma vez. Valores alterados depois são mantidos.",
        "Establece todos los valores de abajo a la vez. Los que cambies después se conservan.",
        "Setzt alle Werte unten auf einmal. Danach geänderte Werte bleiben erhalten.",
        "Règle toutes les valeurs ci-dessous d'un coup. Celles modifiées ensuite sont conservées.",
        "Ustawia naraz wszystkie wartości poniżej. Zmienione później wartości zostają.",
        "Задає всі значення нижче одразу. Те, що ви зміните потім, збережеться.",
        "下の値をまとめて設定します。後から変更した値はそのまま残ります。",
        "아래 값을 한 번에 설정합니다. 이후에 바꾼 값은 유지됩니다.",
        "Aşağıdaki tüm değerleri tek seferde ayarlar. Sonradan değiştirdiğiniz değerler korunur.",
    ],
    "config.farmstead_fart_together.section.about": [
        "About",
        "О моде",
        "关于",
        "Sobre",
        "Acerca de",
        "Über",
        "À propos",
        "O modzie",
        "Про мод",
        "情報",
        "정보",
        "Hakkında",
    ],
    "config.farmstead_fart_together.section.general": [
        "General",
        "Общие",
        "常规",
        "Geral",
        "General",
        "Allgemein",
        "Général",
        "Ogólne",
        "Загальні",
        "全般",
        "일반",
        "Genel",
    ],
    "config.farmstead_fart_together.section.particles": [
        "Cloud",
        "Облако",
        "云雾",
        "Nuvem",
        "Nube",
        "Wolke",
        "Nuage",
        "Chmura",
        "Хмара",
        "雲",
        "구름",
        "Bulut",
    ],
    "config.farmstead_fart_together.section.sounds": [
        "Sounds",
        "Звуки",
        "声音",
        "Sons",
        "Sonidos",
        "Töne",
        "Sons",
        "Dźwięki",
        "Звуки",
        "サウンド",
        "소리",
        "Sesler",
    ],
    "config.farmstead_fart_together.sounds.mutedForOthers": [
        "Mute other players",
        "Не слышать других",
        "静音其他玩家",
        "Silenciar outros jogadores",
        "Silenciar a otros jugadores",
        "Andere Spieler stumm",
        "Couper les autres joueurs",
        "Wycisz innych graczy",
        "Не чути інших",
        "他のプレイヤーをミュート",
        "다른 플레이어 음소거",
        "Diğer oyuncuları sustur",
    ],
    "config.farmstead_fart_together.sounds.mutedForOthers.tooltip": [
        "Do not hear other players' toots (you still see the cloud).",
        "Не слышать чужие залпы (облако всё равно видно).",
        "听不到其他玩家的声音（仍能看到云雾）。",
        "Não ouvir os puns dos outros jogadores (a nuvem continua visível).",
        "No oír las pedorretas de otros jugadores (la nube se sigue viendo).",
        "Pupse anderer Spieler nicht hören (die Wolke bleibt sichtbar).",
        "Ne pas entendre les prouts des autres joueurs (le nuage reste visible).",
        "Nie słyszysz bąków innych graczy (chmurę nadal widać).",
        "Не чути чужих залпів (хмару все одно видно).",
        "他のプレイヤーの「ぷっ」を聞こえなくします（雲は見えます）。",
        "다른 플레이어의 '뿡' 소리를 듣지 않습니다(구름은 보입니다).",
        "Diğer oyuncuların \"pırt\" seslerini duymazsınız (bulut yine görünür).",
    ],
    "config.farmstead_fart_together.sounds.volume": [
        "Volume",
        "Громкость",
        "音量",
        "Volume",
        "Volumen",
        "Lautstärke",
        "Volume",
        "Głośność",
        "Гучність",
        "音量",
        "음량",
        "Ses düzeyi",
    ],
    "config.farmstead_fart_together.sounds.volume.tooltip": [
        "Loudness of every toot, yours and others'.",
        "Громкость каждого залпа — вашего и чужого.",
        "每一声的音量，包括你自己和别人的。",
        "Volume de cada pum, seu e dos outros.",
        "Volumen de cada pedorreta, tuya y de los demás.",
        "Lautstärke jedes Pupses, deiner und der anderen.",
        "Volume de chaque prout, le vôtre et celui des autres.",
        "Głośność każdego bąka, twojego i cudzego.",
        "Гучність кожного залпу — вашого й чужого.",
        "自分と他の人、すべての「ぷっ」の音量です。",
        "나와 다른 사람의 모든 '뿡' 소리 크기입니다.",
        "Sizin ve başkalarının her \"pırt\" sesinin yüksekliği.",
    ],
    "config.farmstead_fart_together.title": [
        "Fart Together settings",
        "Настройки Fart Together",
        "Fart Together 设置",
        "Configurações do Fart Together",
        "Ajustes de Fart Together",
        "Fart-Together-Einstellungen",
        "Paramètres de Fart Together",
        "Ustawienia Fart Together",
        "Налаштування Fart Together",
        "Fart Together の設定",
        "Fart Together 설정",
        "Fart Together ayarları",
    ],
    "config.farmstead_fart_together.try": [
        "Try it",
        "Показать",
        "试试看",
        "Experimentar",
        "Probar",
        "Ausprobieren",
        "Essayer",
        "Wypróbuj",
        "Показати",
        "試す",
        "미리 보기",
        "Dene",
    ],
    "config.farmstead_fart_together.try.tooltip": [
        "Closes this screen and makes a puff behind you with the current settings. Only you see it.",
        "Закрывает экран и выпускает облачко у вас за спиной с текущими настройками. Видите его только вы.",
        "关闭此界面，并按当前设置在你身后放出一团小云。只有你自己能看到。",
        "Fecha esta tela e solta uma nuvenzinha atrás de você com as configurações atuais. Só você vê.",
        "Cierra esta pantalla y suelta una nubecilla detrás de ti con los ajustes actuales. Solo la ves tú.",
        "Schließt diesen Bildschirm und lässt mit den aktuellen Einstellungen ein Wölkchen hinter dir entstehen. Nur du siehst es.",
        "Ferme cet écran et lâche un petit nuage derrière vous avec les réglages actuels. Vous seul le voyez.",
        "Zamyka ten ekran i wypuszcza za tobą chmurkę z bieżącymi ustawieniami. Widzisz ją tylko ty.",
        "Закриває екран і випускає хмаринку у вас за спиною з поточними налаштуваннями. Бачите її лише ви.",
        "この画面を閉じ、今の設定であなたの後ろに小さな雲を出します。見えるのはあなただけです。",
        "이 화면을 닫고 현재 설정으로 내 뒤에 작은 구름을 만듭니다. 나만 볼 수 있습니다.",
        "Bu ekranı kapatır ve mevcut ayarlarla arkanızda küçük bir bulut çıkarır. Yalnızca siz görürsünüz.",
    ],
    "farmstead_fart_together.command.problems": [
        "Fart Together settings: %s problem(s), see below. Fix the file and run /farttogether reload again.",
        "Настройки Fart Together: проблем — %s, подробности ниже. Исправьте файл и снова выполните /farttogether reload.",
        "Fart Together 设置：%s 个问题，详见下方。修正文件后再次执行 /farttogether reload。",
        "Configurações do Fart Together: %s problema(s), veja abaixo. Corrija o arquivo e rode /farttogether reload de novo.",
        "Ajustes de Fart Together: %s problema(s), mira abajo. Corrige el archivo y ejecuta /farttogether reload otra vez.",
        "Fart-Together-Einstellungen: %s Problem(e), siehe unten. Datei korrigieren und /farttogether reload erneut ausführen.",
        "Paramètres de Fart Together : %s problème(s), voir ci-dessous. Corrigez le fichier puis relancez /farttogether reload.",
        "Ustawienia Fart Together: problemów: %s, szczegóły poniżej. Popraw plik i ponownie wpisz /farttogether reload.",
        "Налаштування Fart Together: проблем — %s, подробиці нижче. Виправте файл і знову виконайте /farttogether reload.",
        "Fart Together の設定: 問題が %s 件あります（下記参照）。ファイルを直して再度 /farttogether reload を実行してください。",
        "Fart Together 설정: 문제 %s건, 아래를 확인하세요. 파일을 고친 뒤 /farttogether reload를 다시 실행하세요.",
        "Fart Together ayarları: %s sorun, aşağıya bakın. Dosyayı düzeltip /farttogether reload komutunu yeniden çalıştırın.",
    ],
    "farmstead_fart_together.command.reloaded": [
        "Fart Together settings reloaded.",
        "Настройки Fart Together перезагружены.",
        "Fart Together 设置已重新加载。",
        "Configurações do Fart Together recarregadas.",
        "Ajustes de Fart Together recargados.",
        "Fart-Together-Einstellungen neu geladen.",
        "Paramètres de Fart Together rechargés.",
        "Ustawienia Fart Together wczytane ponownie.",
        "Налаштування Fart Together перезавантажено.",
        "Fart Together の設定を再読み込みしました。",
        "Fart Together 설정을 다시 불러왔습니다.",
        "Fart Together ayarları yeniden yüklendi.",
    ],
}

sys.path.insert(0, str(Path(__file__).resolve().parent))
from lang_more import TABLE as _MORE  # noqa: E402
from lang_m2 import TABLE as _M2  # noqa: E402

T.update(_MORE)
T.update(_M2)

PH = re.compile(r"%(?:\d+\$)?[sd]")


def write():
    OUT.mkdir(parents=True, exist_ok=True)
    for i, lang in enumerate(LANGS):
        data = {k: v[i] for k, v in sorted(T.items())}
        (OUT / f"{lang}.json").write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"{len(LANGS)} languages x {len(T)} keys -> {OUT}")


JAVA = ROOT / "src/main/java"
NS = "farmstead_fart_together"


def used_keys() -> set[str]:
    """Keys the code asks for: literal keys, K/C + "..." in the command and tab code, client settings, items."""
    keys = set()
    literal = re.compile(r'"((?:item|block|message|command|config|key|subtitles)\.' + NS + r'\.[A-Za-z0-9_.]*[A-Za-z0-9_])"')
    prefixed = re.compile(r'\b([KC])\s*\+\s*"([A-Za-z0-9_.]*[A-Za-z0-9_])"')
    tr = re.compile(r'\btr\("([A-Za-z0-9_.]+)"')
    for f in JAVA.rglob("*.java"):
        text = f.read_text(encoding="utf-8")
        keys.update(literal.findall(text))
        if f.name in ("FartCommands.java", "MyStyleTab.java"):
            for kind, rest in prefixed.findall(text):
                keys.add(("command." if kind == "K" else "config.") + NS + "." + rest)
            if f.name == "FartCommands.java":
                keys.update(f"command.{NS}.{k}" for k in tr.findall(text))
    client = (JAVA / "online/farmsteadminecraft/farttogether/client/ClientConfig.java").read_text(encoding="utf-8")
    for section, key in re.findall(r'FILE\.\w+\("(\w+)", "(\w+)"', client):
        keys.add(f"config.{NS}.{section}.{key}")
        keys.add(f"config.{NS}.{section}.{key}.tooltip")
    content = (JAVA / "online/farmsteadminecraft/farttogether/content/Content.java").read_text(encoding="utf-8")
    blocks, items = content.split("public static void registerItems", 1)
    keys.update(f"block.{NS}.{n}" for n in re.findall(r'sink\.accept\("(\w+)"', blocks))
    keys.update(f"item.{NS}.{n}" for n in re.findall(r'sink\.accept\("(\w+)"', items))
    return keys


def check() -> int:
    errors = [f"table: {k} has {len(v)} strings, not {len(LANGS)}" for k, v in T.items() if len(v) != len(LANGS)]
    errors += [f"code uses {k}, the table has no such key" for k in sorted(used_keys()) if k not in T]
    ref = json.loads((OUT / "en_us.json").read_text(encoding="utf-8"))
    for lang in LANGS:
        path = OUT / f"{lang}.json"
        if not path.exists():
            errors.append(f"{lang}: file missing")
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        for key, text in ref.items():
            if key not in data or not str(data[key]).strip():
                errors.append(f"{lang}: missing {key}")
            elif sorted(PH.findall(text)) != sorted(PH.findall(data[key])):
                errors.append(f"{lang}: placeholders differ in {key}")
        for key in data:
            if key not in ref:
                errors.append(f"{lang}: extra key {key}")
    for e in errors:
        print("  " + e)
    print("lang check: " + ("OK" if not errors else f"{len(errors)} problem(s)"))
    return 1 if errors else 0


if __name__ == "__main__":
    if "--check" in sys.argv:
        sys.exit(check())
    write()
    sys.exit(check())

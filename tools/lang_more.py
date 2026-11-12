"""M1 strings (key, sound subtitles, style settings), merged into lang.py's table. Same order of languages:
en_us, ru_ru, zh_cn, pt_br, es_es, de_de, fr_fr, pl_pl, uk_ua, ja_jp, ko_kr, tr_tr. Family rating: "toot" and
playful words only."""

C = "config.farmstead_fart_together."

TABLE = {
    "key.farmstead_fart_together.toot": [
        "Toot", "Залп", "放个屁", "Pum", "Pedorreta", "Pups", "Prout", "Bąk", "Залп", "おなら", "뿡", "Osur",
    ],
    "subtitles.farmstead_fart_together.toot.short": [
        "Quick toot", "Короткий залп", "短促的噗", "Pum rápido", "Pedorreta corta", "Kurzer Pups", "Petit prout",
        "Krótki bąk", "Короткий залп", "短いおなら", "짧은 뿡", "Kısa osuruk",
    ],
    "subtitles.farmstead_fart_together.toot.squeaky": [
        "Squeaky toot", "Писклявый залп", "尖细的噗", "Pum agudinho", "Pedorreta chillona", "Quietschender Pups",
        "Prout couinant", "Piskliwy bąk", "Пискливий залп", "キュッとしたおなら", "삑 소리 뿡", "Cırlak osuruk",
    ],
    "subtitles.farmstead_fart_together.toot.bubbly": [
        "Bubbly toot", "Булькающий залп", "咕嘟的噗", "Pum borbulhante", "Pedorreta burbujeante", "Blubbernder Pups",
        "Prout pétillant", "Bulgoczący bąk", "Булькаючий залп", "ブクブクおなら", "보글보글 뿡", "Fokurdayan osuruk",
    ],
    "subtitles.farmstead_fart_together.toot.long": [
        "Long toot", "Долгий залп", "长长的噗", "Pum demorado", "Pedorreta larga", "Langer Pups", "Long prout",
        "Długi bąk", "Довгий залп", "長いおなら", "기다란 뿡", "Uzun osuruk",
    ],
    "subtitles.farmstead_fart_together.toot.trumpet": [
        "Trumpet toot", "Залп-труба", "喇叭般的噗", "Pum de trombone", "Pedorreta de trompeta", "Trompeten-Pups",
        "Prout trompette", "Bąk jak trąbka", "Залп-труба", "ラッパみたいなおなら", "나팔 뿡", "Trompet gibi osuruk",
    ],
    "subtitles.farmstead_fart_together.toot.tiny": [
        "Tiny toot", "Крошечный залп", "小小的噗", "Punzinho", "Pedorretita", "Mini-Pups", "Mini prout",
        "Malutki bąk", "Крихітний залп", "小さなおなら", "아주 작은 뿡", "Minik osuruk",
    ],
    "subtitles.farmstead_fart_together.toot.rumble": [
        "Rumbling toot", "Раскатистый залп", "隆隆的噗", "Pum trovejante", "Pedorreta retumbante", "Grollender Pups",
        "Prout grondant", "Grzmiący bąk", "Гуркітливий залп", "ゴロゴロおなら", "우르릉 뿡", "Gürüldeyen osuruk",
    ],
    "subtitles.farmstead_fart_together.toot.puff": [
        "Soft puff", "Мягкий пуф", "轻轻一噗", "Sopro suave", "Soplido suave", "Sanftes Puff", "Petit pouf",
        "Cichy puf", "М'який пуф", "ふわっと", "살짝 퍽", "Hafif püf",
    ],
    C + "general.poses": [
        "Poses", "Позы", "姿势动画", "Poses", "Poses", "Posen", "Poses", "Pozy", "Пози", "ポーズ", "포즈", "Pozlar",
    ],
    C + "general.poses.tooltip": [
        "Play the toot pose, yours and others'. Off: only the cloud and the sound.",
        "Показывать позу при залпе — свою и чужую. Выкл.: только облачко и звук.",
        "播放放屁姿势（自己和他人）。关闭后只有烟雾和声音。",
        "Mostra a pose do pum, sua e dos outros. Desligado: só a nuvem e o som.",
        "Muestra la pose de la pedorreta, tuya y de otros. Desactivado: solo la nube y el sonido.",
        "Zeigt die Pups-Pose, deine und die der anderen. Aus: nur Wolke und Geräusch.",
        "Joue la pose du prout, la vôtre et celle des autres. Désactivé : seulement le nuage et le son.",
        "Pokazuje pozę przy bąku, twoją i innych. Wył.: tylko chmurka i dźwięk.",
        "Показувати позу під час залпу — свою й чужу. Вимк.: лише хмаринка та звук.",
        "おならのポーズを表示します（自分と他の人）。オフ：雲と音だけ。",
        "뿡 포즈를 보여 줍니다(나와 다른 사람). 끄면 구름과 소리만 나옵니다.",
        "Osurma pozunu oynatır, seninkini ve başkalarınınkini. Kapalı: yalnızca bulut ve ses.",
    ],
    C + "particles.maxPerToot": [
        "Particle cap per toot", "Предел частиц на залп", "每次粒子上限", "Limite de partículas por pum",
        "Límite de partículas por pedorreta", "Partikel-Limit pro Pups", "Limite de particules par prout",
        "Limit cząsteczek na bąka", "Межа частинок на залп", "1回あたりの粒子上限", "뿡 한 번당 입자 상한",
        "Osuruk başına parçacık sınırı",
    ],
    C + "particles.maxPerToot.tooltip": [
        "Most particles one toot of another player may draw on your screen. Lower it on a weak PC.",
        "Сколько частиц максимум рисует чужой залп на вашем экране. Уменьшите на слабом ПК.",
        "其他玩家一次放屁最多在你屏幕上绘制的粒子数。电脑较弱时调低。",
        "Máximo de partículas que o pum de outro jogador desenha na sua tela. Diminua em PCs fracos.",
        "Máximo de partículas que dibuja en tu pantalla la pedorreta de otro jugador. Bájalo en un PC modesto.",
        "Höchstzahl an Partikeln, die ein Pups eines anderen Spielers bei dir zeichnet. Auf schwachen PCs senken.",
        "Nombre maximal de particules qu'un prout d'un autre joueur affiche chez vous. À baisser sur un PC modeste.",
        "Najwięcej cząsteczek, ile bąk innego gracza narysuje na twoim ekranie. Zmniejsz na słabym PC.",
        "Скільки частинок щонайбільше малює чужий залп на вашому екрані. Зменште на слабкому ПК.",
        "他のプレイヤーの1回のおならがあなたの画面に描く粒子の最大数。低スペックPCでは下げてください。",
        "다른 플레이어의 뿡 한 번이 내 화면에 그릴 수 있는 최대 입자 수. 사양이 낮으면 줄이세요.",
        "Başka bir oyuncunun tek osuruğunun ekranında çizebileceği en fazla parçacık. Zayıf bilgisayarda düşürün.",
    ],
    C + "section.style": [
        "My style", "Мой стиль", "我的风格", "Meu estilo", "Mi estilo", "Mein Stil", "Mon style", "Mój styl",
        "Мій стиль", "マイスタイル", "내 스타일", "Benim tarzım",
    ],
    C + "style.others": [
        "Others' toots", "Чужие залпы", "他人的屁", "Puns dos outros", "Pedorretas ajenas", "Pupse der anderen",
        "Prouts des autres", "Bąki innych", "Чужі залпи", "他人のおなら", "다른 사람의 뿡", "Başkalarının osurukları",
    ],
    C + "style.others.tooltip": [
        "Theirs: each player's own style. Simple: everyone gets the plain green puff. Mine: everyone looks like you.",
        "Их: у каждого свой стиль. Простой: у всех обычное зелёное облачко. Мой: все выглядят как вы.",
        "他们的：每人用自己的风格。简单：所有人都是普通的绿色烟雾。我的：所有人都和你一样。",
        "Deles: o estilo de cada jogador. Simples: todos com a nuvem verde comum. Meu: todos iguais a você.",
        "Suyo: el estilo de cada jugador. Simple: todos con la nube verde normal. Mío: todos se ven como tú.",
        "Ihrer: jeder mit eigenem Stil. Einfach: alle mit der normalen grünen Wolke. Meiner: alle wie du.",
        "Le leur : le style de chacun. Simple : tout le monde a le nuage vert ordinaire. Le mien : tous comme vous.",
        "Ich: każdy ma swój styl. Prosty: wszyscy mają zwykłą zieloną chmurkę. Mój: wszyscy wyglądają jak ty.",
        "Їхній: у кожного свій стиль. Простий: у всіх звичайна зелена хмаринка. Мій: усі виглядають як ви.",
        "相手の：各自のスタイル。シンプル：全員ふつうの緑の雲。自分の：全員あなたと同じ。",
        "그들의 것: 각자의 스타일. 단순: 모두 평범한 초록 구름. 내 것: 모두 나처럼 보입니다.",
        "Onların: herkesin kendi tarzı. Sade: herkese düz yeşil bulut. Benim: herkes senin gibi görünür.",
    ],
    C + "style.others.theirs": [
        "Theirs", "Их", "他们的", "Deles", "Suyo", "Ihrer", "Le leur", "Ich", "Їхній", "相手の", "그들의 것", "Onların",
    ],
    C + "style.others.simple": [
        "Simple", "Простой", "简单", "Simples", "Simple", "Einfach", "Simple", "Prosty", "Простий", "シンプル", "단순",
        "Sade",
    ],
    C + "style.others.mine": [
        "Mine", "Мой", "我的", "Meu", "Mío", "Meiner", "Le mien", "Mój", "Мій", "自分の", "내 것", "Benim",
    ],
    C + "style.color": [
        "Cloud colour", "Цвет облачка", "烟雾颜色", "Cor da nuvem", "Color de la nube", "Wolkenfarbe",
        "Couleur du nuage", "Kolor chmurki", "Колір хмаринки", "雲の色", "구름 색", "Bulut rengi",
    ],
    C + "style.color.tooltip": [
        "#RRGGBB, a preset (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random or rainbow. Gradients and rainbow need the server's permission.",
        "#RRGGBB, пресет (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random или rainbow. Для градиента и радуги нужно право на сервере.",
        "#RRGGBB、预设（swamp, bean, lilac, gold, pumpkin, pine, sky, rose）、gradient:#A,#B、random 或 rainbow。渐变和彩虹需要服务器权限。",
        "#RRGGBB, um preset (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random ou rainbow. Degradê e arco-íris precisam de permissão do servidor.",
        "#RRGGBB, un preset (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random o rainbow. El degradado y el arcoíris necesitan permiso del servidor.",
        "#RRGGBB, eine Vorlage (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random oder rainbow. Verlauf und Regenbogen brauchen die Erlaubnis des Servers.",
        "#RRGGBB, un préréglage (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random ou rainbow. Dégradé et arc-en-ciel demandent la permission du serveur.",
        "#RRGGBB, preset (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random lub rainbow. Gradient i tęcza wymagają uprawnienia na serwerze.",
        "#RRGGBB, пресет (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random або rainbow. Для градієнта й веселки потрібен дозвіл на сервері.",
        "#RRGGBB、プリセット（swamp, bean, lilac, gold, pumpkin, pine, sky, rose）、gradient:#A,#B、random、rainbow。グラデーションと虹にはサーバーの権限が必要です。",
        "#RRGGBB, 프리셋(swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random 또는 rainbow. 그라데이션과 무지개는 서버 권한이 필요합니다.",
        "#RRGGBB, bir hazır ayar (swamp, bean, lilac, gold, pumpkin, pine, sky, rose), gradient:#A,#B, random veya rainbow. Geçiş ve gökkuşağı için sunucu izni gerekir.",
    ],
    C + "style.particle": [
        "Particles", "Частицы", "粒子", "Partículas", "Partículas", "Partikel", "Particules", "Cząsteczki", "Частинки",
        "粒子", "입자", "Parçacıklar",
    ],
    C + "style.particle.tooltip": [
        "The shape of your cloud's particles.", "Форма частиц вашего облачка.", "你烟雾的粒子形状。",
        "O formato das partículas da sua nuvem.", "La forma de las partículas de tu nube.", "Die Form der Partikel deiner Wolke.",
        "La forme des particules de votre nuage.", "Kształt cząsteczek twojej chmurki.", "Форма частинок вашої хмаринки.",
        "あなたの雲の粒子の形。", "내 구름 입자의 모양.", "Bulutunun parçacıklarının şekli.",
    ],
    C + "style.particle.puff": [
        "Puff", "Пух", "烟团", "Fumacinha", "Bocanada", "Wölkchen", "Bouffée", "Puszek", "Пух", "もくもく", "뭉게",
        "Duman",
    ],
    C + "style.particle.bubbles": [
        "Bubbles", "Пузырьки", "泡泡", "Bolhas", "Burbujas", "Blasen", "Bulles", "Bąbelki", "Бульбашки", "泡", "거품",
        "Baloncuklar",
    ],
    C + "style.particle.sparks": [
        "Sparks", "Искры", "火花", "Faíscas", "Chispas", "Funken", "Étincelles", "Iskry", "Іскри", "火花", "불꽃",
        "Kıvılcımlar",
    ],
    C + "style.particle.leaves": [
        "Leaves", "Листья", "落叶", "Folhas", "Hojas", "Blätter", "Feuilles", "Liście", "Листя", "葉っぱ", "잎사귀",
        "Yapraklar",
    ],
    C + "style.particle.notes": [
        "Notes", "Ноты", "音符", "Notas", "Notas", "Noten", "Notes", "Nuty", "Ноти", "音符", "음표", "Notalar",
    ],
    C + "style.particle.hearts": [
        "Hearts", "Сердечки", "爱心", "Corações", "Corazones", "Herzen", "Cœurs", "Serduszka", "Сердечка", "ハート", "하트",
        "Kalpler",
    ],
    C + "style.particle.snow": [
        "Snow", "Снежинки", "雪花", "Neve", "Nieve", "Schnee", "Neige", "Śnieg", "Сніжинки", "雪", "눈송이", "Kar",
    ],
    C + "style.size": [
        "Particle size", "Размер частиц", "粒子大小", "Tamanho das partículas", "Tamaño de partículas", "Partikelgröße",
        "Taille des particules", "Rozmiar cząsteczek", "Розмір частинок", "粒子の大きさ", "입자 크기", "Parçacık boyutu",
    ],
    C + "style.size.tooltip": [
        "How big your particles are. The server may cap it.", "Насколько крупные частицы. Сервер может ограничить.",
        "粒子有多大。服务器可能会限制。", "Quão grandes são as partículas. O servidor pode limitar.",
        "Qué tan grandes son las partículas. El servidor puede limitarlo.", "Wie groß deine Partikel sind. Der Server kann es begrenzen.",
        "La taille de vos particules. Le serveur peut la limiter.", "Jak duże są cząsteczki. Serwer może to ograniczyć.",
        "Наскільки великі частинки. Сервер може обмежити.", "粒子の大きさ。サーバーが上限を設けることがあります。",
        "입자의 크기. 서버가 제한할 수 있습니다.", "Parçacıkların ne kadar büyük olduğu. Sunucu sınırlayabilir.",
    ],
    C + "style.amount": [
        "Particle amount", "Количество частиц", "粒子数量", "Quantidade de partículas", "Cantidad de partículas",
        "Partikelmenge", "Quantité de particules", "Liczba cząsteczek", "Кількість частинок", "粒子の量", "입자 양",
        "Parçacık miktarı",
    ],
    C + "style.amount.tooltip": [
        "How many particles your cloud has. The server may cap it.", "Сколько частиц в вашем облачке. Сервер может ограничить.",
        "你的烟雾有多少粒子。服务器可能会限制。", "Quantas partículas sua nuvem tem. O servidor pode limitar.",
        "Cuántas partículas tiene tu nube. El servidor puede limitarlo.", "Wie viele Partikel deine Wolke hat. Der Server kann es begrenzen.",
        "Le nombre de particules de votre nuage. Le serveur peut le limiter.", "Ile cząsteczek ma twoja chmurka. Serwer może to ograniczyć.",
        "Скільки частинок у вашій хмаринці. Сервер може обмежити.", "雲の粒子の数。サーバーが上限を設けることがあります。",
        "내 구름의 입자 수. 서버가 제한할 수 있습니다.", "Bulutunda kaç parçacık olduğu. Sunucu sınırlayabilir.",
    ],
    C + "style.spread": [
        "Spread", "Разлёт", "扩散", "Espalhamento", "Dispersión", "Streuung", "Dispersion", "Rozrzut", "Розліт", "広がり",
        "퍼짐", "Yayılma",
    ],
    C + "style.spread.tooltip": [
        "How far the cloud spreads.", "Насколько широко разлетается облачко.", "烟雾扩散多远。",
        "Até onde a nuvem se espalha.", "Hasta dónde se dispersa la nube.", "Wie weit sich die Wolke ausbreitet.",
        "Jusqu'où le nuage s'étale.", "Jak daleko rozchodzi się chmurka.", "Наскільки широко розлітається хмаринка.",
        "雲がどこまで広がるか。", "구름이 얼마나 퍼지는지.", "Bulutun ne kadar yayıldığı.",
    ],
    C + "style.duration": [
        "Duration", "Длительность", "持续时间", "Duração", "Duración", "Dauer", "Durée", "Czas trwania", "Тривалість",
        "長さ", "지속 시간", "Süre",
    ],
    C + "style.duration.tooltip": [
        "How long your cloud keeps puffing out. The server may cap it.", "Как долго выходит облачко. Сервер может ограничить.",
        "烟雾持续冒出的时间。服务器可能会限制。", "Por quanto tempo a nuvem continua saindo. O servidor pode limitar.",
        "Cuánto tiempo sigue saliendo la nube. El servidor puede limitarlo.", "Wie lange die Wolke herauspufft. Der Server kann es begrenzen.",
        "Combien de temps le nuage continue de sortir. Le serveur peut le limiter.", "Jak długo wydobywa się chmurka. Serwer może to ograniczyć.",
        "Як довго виходить хмаринка. Сервер може обмежити.", "雲が出続ける長さ。サーバーが上限を設けることがあります。",
        "구름이 뿜어져 나오는 시간. 서버가 제한할 수 있습니다.", "Bulutun ne kadar süre çıktığı. Sunucu sınırlayabilir.",
    ],
    C + "style.sound": [
        "Sound", "Звук", "声音", "Som", "Sonido", "Geräusch", "Son", "Dźwięk", "Звук", "音", "소리", "Ses",
    ],
    C + "style.sound.tooltip": [
        "random, or one of: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random или один из: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random，或以下之一：short, squeaky, bubbly, long, trumpet, tiny, rumble, puff。",
        "random, ou um destes: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random, o uno de estos: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random oder eines davon: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random, ou l'un de : short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random albo jeden z: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random або один із: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random、または次のいずれか：short, squeaky, bubbly, long, trumpet, tiny, rumble, puff。",
        "random 또는 다음 중 하나: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
        "random ya da şunlardan biri: short, squeaky, bubbly, long, trumpet, tiny, rumble, puff.",
    ],
    C + "style.pitch": [
        "Pitch", "Высота тона", "音调", "Tom", "Tono", "Tonhöhe", "Hauteur", "Wysokość dźwięku", "Висота тону", "音の高さ",
        "음높이", "Perde",
    ],
    C + "style.pitch.tooltip": [
        "Pitch of your sound: below 1 lower, above 1 higher.", "Высота вашего звука: меньше 1 — ниже, больше 1 — выше.",
        "声音的音调：小于 1 更低，大于 1 更高。", "Tom do seu som: abaixo de 1 mais grave, acima de 1 mais agudo.",
        "Tono de tu sonido: por debajo de 1 más grave, por encima de 1 más agudo.", "Tonhöhe deines Geräuschs: unter 1 tiefer, über 1 höher.",
        "Hauteur de votre son : sous 1 plus grave, au-dessus de 1 plus aigu.", "Wysokość twojego dźwięku: poniżej 1 niżej, powyżej 1 wyżej.",
        "Висота вашого звуку: менше 1 — нижче, більше 1 — вище.", "音の高さ：1 より小さいと低く、大きいと高く。",
        "내 소리의 음높이: 1보다 작으면 낮게, 크면 높게.", "Sesinin perdesi: 1'in altı daha kalın, üstü daha ince.",
    ],
    C + "style.volume": [
        "Sound volume", "Громкость звука", "声音音量", "Volume do som", "Volumen del sonido", "Lautstärke",
        "Volume du son", "Głośność dźwięku", "Гучність звуку", "音量", "소리 크기", "Ses düzeyi",
    ],
    C + "style.volume.tooltip": [
        "How loud your toot is for everyone who hears it.", "Насколько громко ваш залп слышат все вокруг.",
        "其他人听到你放屁的音量。", "Quão alto todos ouvem o seu pum.", "Qué tan fuerte oyen todos tu pedorreta.",
        "Wie laut alle deinen Pups hören.", "À quel volume tout le monde entend votre prout.", "Jak głośno wszyscy słyszą twojego bąka.",
        "Наскільки гучно ваш залп чують усі навколо.", "まわりの人に聞こえるあなたのおならの音量。",
        "다른 사람들에게 들리는 내 뿡의 크기.", "Osuruğunun herkese ne kadar yüksek duyulduğu.",
    ],
    C + "style.pose": [
        "Pose", "Поза", "姿势", "Pose", "Pose", "Pose", "Pose", "Poza", "Поза", "ポーズ", "포즈", "Poz",
    ],
    C + "style.pose.tooltip": [
        "Classic: lean forward, hands on knees, a glance back. Hero: chest out, hands on hips. None: no pose.",
        "Классика: наклон вперёд, руки на коленях, взгляд назад. Герой: грудь колесом, руки на поясе. Нет: без позы.",
        "经典：前倾，双手扶膝，回头一瞥。英雄：挺胸叉腰。无：不摆姿势。",
        "Clássica: inclinar, mãos nos joelhos, olhada para trás. Herói: peito estufado, mãos na cintura. Nenhuma: sem pose.",
        "Clásica: inclinarse, manos en las rodillas, mirada atrás. Héroe: pecho fuera, manos en la cintura. Ninguna: sin pose.",
        "Klassisch: vorbeugen, Hände auf die Knie, Blick zurück. Held: Brust raus, Hände in die Hüften. Keine: ohne Pose.",
        "Classique : penché, mains sur les genoux, regard en arrière. Héros : torse bombé, poings sur les hanches. Aucune : pas de pose.",
        "Klasyczna: pochylenie, ręce na kolanach, spojrzenie za siebie. Bohater: pierś do przodu, ręce na biodrach. Brak: bez pozy.",
        "Класика: нахил уперед, руки на колінах, погляд назад. Герой: груди колесом, руки на поясі. Немає: без пози.",
        "クラシック：前かがみで膝に手、ちらっと振り返る。ヒーロー：胸を張って腰に手。なし：ポーズなし。",
        "클래식: 앞으로 숙이고 무릎에 손, 뒤를 힐끗. 영웅: 가슴을 펴고 허리에 손. 없음: 포즈 없음.",
        "Klasik: öne eğil, eller dizlerde, arkaya bir bakış. Kahraman: göğüs ileri, eller belde. Yok: poz yok.",
    ],
    C + "style.pose.classic": [
        "Classic", "Классика", "经典", "Clássica", "Clásica", "Klassisch", "Classique", "Klasyczna", "Класика",
        "クラシック", "클래식", "Klasik",
    ],
    C + "style.pose.hero": [
        "Hero", "Герой", "英雄", "Herói", "Héroe", "Held", "Héros", "Bohater", "Герой", "ヒーロー", "영웅", "Kahraman",
    ],
    C + "style.pose.none": [
        "None", "Нет", "无", "Nenhuma", "Ninguna", "Keine", "Aucune", "Brak", "Немає", "なし", "없음", "Yok",
    ],
}

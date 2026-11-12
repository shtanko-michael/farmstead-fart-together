"""M3 strings (hold to charge, Fart Together combos, advancements), merged into lang.py's table after M2 (a key here
replaces the earlier one). Same order of languages: en_us, ru_ru, zh_cn, pt_br, es_es, de_de, fr_fr, pl_pl, uk_ua,
ja_jp, ko_kr, tr_tr. Family rating: "toot", "gas" and playful words only."""

NS = "farmstead_fart_together"
A = f"advancements.{NS}."

TABLE = {
    # the key is held now: the controls screen says so
    f"key.{NS}.toot": [
        "Toot (hold for more)", "Залп (держите — сильнее)", "放屁（按住更响）", "Pum (segure para mais)",
        "Pedorreta (mantén para más)", "Pups (halten für mehr)", "Prout (maintenir pour plus)",
        "Bąk (przytrzymaj, by mocniej)", "Залп (тримайте — сильніше)", "おなら（長押しで強く）", "뿡 (길게 누르면 세게)",
        "Osur (daha güçlü için basılı tut)",
    ],
    f"message.{NS}.together": [
        "Fart Together x%s!", "Fart Together ×%s!", "一起放屁 ×%s！", "Pum em conjunto x%s!", "¡Pedorreta en grupo x%s!",
        "Gemeinsam pupsen x%s!", "Prout ensemble x%s !", "Bąk razem x%s!", "Fart Together ×%s!", "みんなでおなら ×%s！",
        "다 함께 뿡 ×%s!", "Birlikte osuruk x%s!",
    ],
    A + "first_toot.title": [
        "Excuse Me!", "Пардон!", "不好意思！", "Com licença!", "¡Perdón!", "Verzeihung!", "Pardon !", "Przepraszam!",
        "Перепрошую!", "失礼！", "실례!", "Pardon!",
    ],
    A + "first_toot.description": [
        "Let one go for the first time", "Впервые выпустить газы", "第一次放屁", "Solte o primeiro pum",
        "Suelta tu primera pedorreta", "Zum ersten Mal pupsen", "Lâcher son premier prout", "Pierwszy bąk w życiu",
        "Уперше випустити гази", "はじめてのおなら", "처음으로 뿡", "İlk kez osur",
    ],
    A + "legendary_toot.title": [
        "Legendary", "Легендарный", "传奇之屁", "Lendário", "Legendaria", "Legendär", "Légendaire", "Legendarny",
        "Легендарний", "伝説の一発", "전설의 뿡", "Efsanevi",
    ],
    A + "legendary_toot.description": [
        "Hold the key until a full gas meter comes out at once", "Держать клавишу, пока не выйдет вся полная шкала газа",
        "按住按键，把满满一槽气一次放完", "Segure a tecla até soltar o medidor cheio de uma vez",
        "Mantén la tecla hasta soltar el medidor lleno de una vez", "Halte die Taste, bis ein volles Gasmeter auf einmal rauskommt",
        "Maintenir la touche jusqu'à lâcher une jauge pleine d'un coup", "Przytrzymaj klawisz, aż pełny pasek gazu wyjdzie naraz",
        "Тримати клавішу, доки не вийде вся повна шкала газу", "キーを長押しして満タンのガスを一気に出す",
        "키를 꾹 눌러 가득 찬 가스를 한 번에 내보내기", "Tuşu basılı tut, dolu gaz göstergesi bir kerede çıksın",
    ],
    A + "jet_jump.title": [
        "Jet Propulsion", "Реактивная тяга", "喷气推进", "Propulsão a jato", "Propulsión a chorro", "Düsenantrieb",
        "Propulsion à réaction", "Napęd odrzutowy", "Реактивна тяга", "ジェット推進", "제트 추진", "Jet itkisi",
    ],
    A + "jet_jump.description": [
        "Toot in mid-air and fly a little higher", "Сделать залп в прыжке и взлететь чуть выше", "在空中放屁，飞得更高一点",
        "Solte um pum no ar e voe um pouco mais alto", "Suelta una pedorreta en el aire y vuela un poco más alto",
        "Pupse in der Luft und flieg ein Stück höher", "Prouter en plein saut et monter un peu plus haut",
        "Puść bąka w powietrzu i wzleć trochę wyżej", "Зробити залп у стрибку й злетіти трохи вище",
        "空中でおならをして少し高く飛ぶ", "공중에서 뿡 하고 조금 더 높이 날기", "Havada osur ve biraz daha yükseğe uç",
    ],
    A + "golden_toot.title": [
        "Pure Gold", "Чистое золото", "纯金之屁", "Ouro puro", "Oro puro", "Pures Gold", "Or pur", "Czyste złoto",
        "Чисте золото", "純金", "순금", "Saf altın",
    ],
    A + "golden_toot.description": [
        "Toot after eating an enchanted golden apple", "Сделать залп после зачарованного золотого яблока",
        "吃下附魔金苹果后放屁", "Solte um pum depois de comer uma maçã dourada encantada",
        "Suelta una pedorreta tras comer una manzana dorada encantada", "Pupse nach einem verzauberten goldenen Apfel",
        "Prouter après avoir mangé une pomme dorée enchantée", "Puść bąka po zaklętym złotym jabłku",
        "Зробити залп після зачарованого золотого яблука", "エンチャントされた金のリンゴを食べておなら",
        "마법이 부여된 황금 사과를 먹고 뿡", "Büyülü altın elmadan sonra osur",
    ],
    A + "bubbles.title": [
        "Hot Tub", "Джакузи", "按摩浴缸", "Hidromassagem", "Jacuzzi", "Whirlpool", "Jacuzzi", "Jacuzzi", "Джакузі",
        "ジャグジー", "자쿠지", "Jakuzi",
    ],
    A + "bubbles.description": [
        "Toot under water", "Сделать залп под водой", "在水下放屁", "Solte um pum debaixo d'água",
        "Suelta una pedorreta bajo el agua", "Pupse unter Wasser", "Prouter sous l'eau", "Puść bąka pod wodą",
        "Зробити залп під водою", "水中でおなら", "물속에서 뿡", "Su altında osur",
    ],
    A + "wither_toot.title": [
        "Biological Weapon", "Биологическое оружие", "生化武器", "Arma biológica", "Arma biológica", "Biowaffe",
        "Arme biologique", "Broń biologiczna", "Біологічна зброя", "生物兵器", "생물학 무기", "Biyolojik silah",
    ],
    A + "wither_toot.description": [
        "Toot near the Wither", "Сделать залп рядом с Иссушителем", "在凋灵附近放屁", "Solte um pum perto do Wither",
        "Suelta una pedorreta cerca del Wither", "Pupse in der Nähe des Withers", "Prouter près du Wither",
        "Puść bąka obok Withera", "Зробити залп поряд з Візером", "ウィザーの近くでおなら", "위더 근처에서 뿡",
        "Wither'ın yanında osur",
    ],
    A + "together_2.title": [
        "Fart Together!", "Fart Together!", "一起放屁！", "Pum em conjunto!", "¡Pedorreta en grupo!", "Gemeinsam pupsen!",
        "Prout ensemble !", "Bąk razem!", "Fart Together!", "みんなでおなら！", "다 함께 뿡!", "Birlikte osuruk!",
    ],
    A + "together_2.description": [
        "Toot at the same moment as a friend next to you", "Сделать залп одновременно с другом рядом",
        "和身边的朋友同时放屁", "Solte um pum junto com um amigo ao seu lado",
        "Suelta una pedorreta a la vez que un amigo a tu lado", "Pupse im selben Moment wie ein Freund neben dir",
        "Prouter en même temps qu'un ami à côté", "Puść bąka w tej samej chwili co przyjaciel obok",
        "Зробити залп одночасно з другом поруч", "隣の友達と同時におなら", "옆에 있는 친구와 동시에 뿡",
        "Yanındaki arkadaşınla aynı anda osur",
    ],
    A + "together_4.title": [
        "Gas Quartet", "Газовый квартет", "气体四重奏", "Quarteto de gás", "Cuarteto de gas", "Gasquartett",
        "Quatuor à gaz", "Kwartet gazowy", "Газовий квартет", "ガス四重奏", "가스 4중주", "Gaz dörtlüsü",
    ],
    A + "together_4.description": [
        "Four players toot together", "Четыре игрока делают залп вместе", "四名玩家一起放屁", "Quatro jogadores soltam um pum juntos",
        "Cuatro jugadores sueltan una pedorreta juntos", "Vier Spieler pupsen zusammen", "Quatre joueurs proutent ensemble",
        "Czterech graczy puszcza bąka razem", "Четверо гравців роблять залп разом", "4人で一緒におなら",
        "네 명이 함께 뿡", "Dört oyuncu birlikte osurur",
    ],
    A + "together_8.title": [
        "Wind Orchestra", "Духовой оркестр", "管乐团", "Orquestra de sopro", "Orquesta de viento", "Blasorchester",
        "Orchestre à vent", "Orkiestra dęta", "Духовий оркестр", "吹奏楽団", "관악 오케스트라", "Nefesli orkestra",
    ],
    A + "together_8.description": [
        "Eight players toot together", "Восемь игроков делают залп вместе", "八名玩家一起放屁", "Oito jogadores soltam um pum juntos",
        "Ocho jugadores sueltan una pedorreta juntos", "Acht Spieler pupsen zusammen", "Huit joueurs proutent ensemble",
        "Ośmiu graczy puszcza bąka razem", "Восьмеро гравців роблять залп разом", "8人で一緒におなら",
        "여덟 명이 함께 뿡", "Sekiz oyuncu birlikte osurur",
    ],
}

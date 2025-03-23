Obsah

Tabulka revizí
| Jméno | Datum | Důvod | změny | Verze |
| ----- | ----- | ----- | ----- | ----- |
| <Autor> | <Datum> | <Stručný popis změny> | <Verze> |



1. Základní informace  
1.1 Popis a zaměření softwarového díla  
<Popište krátce specifikovaný software. Krátce zdůvodněte, proč jste se rozhodli ho
implementovat, uveďte, co přinese nového, na jakou cílovou skupinu je zaměřen. Pokud existují
alternativy, uveďte je a zdůvodněte, v čem se vaše řešení bude odlišovat>
1.2 Použité technologie
<Uveďte seznam nejdůležitějších technologií (např. knihovny, hardware atd.), které budete
v rámci projektu využívat>
1.3 Odkazy (Reference)
<Uveďte odkazy na všechny ostatní informace, se kterými by měl být seznámen čtenář této
specifikace. To mohou být například webové stránky, knihy, manuály, tutoriály nebo odborné
články. Uvádějte je všechny ve stejném formátu obsahujícím autora/instituci, verzi, datum a tam,
kde je to nutné, také krátký popis, popřípadě zdroj.>

2. Stručný popis softwarového díla
2.1 Důvod vzniku softwarového díla a jeho základní části a cíle řešení
<Popište, proč má softwarové dílo vzniknout. Uveďte, zda se má jednat o samostatný produkt,
rozšíření funkcionality, náhradu existujícího produktu nebo něco jiného. Pokud se jedná o součást
většího systému, popište požadavky na tento systém, popřípadě požadavky na jeho začlenění do
tohoto systému, popište rozhraní, které bude pro začlenění využito. Pokud bude systém složen
z několika částí či vrstev, nakreslete jednoduchý diagram a popište funkce jednotlivých částí.>
2.2 Hlavní funkce
<Popište hlavní funkce díla. Nezacházejte do detailů, jednotlivé detaily funkcionality budou
popsány v následujících sekcích, zde postačí pouze stručný „high-level“ přehled.>
2.3 Motivační příklad užití
	Např. načtení a vyhledávání v dokumentaci
2.4 Prostředí aplikace
<Popište prostředí, ve kterém bude aplikace běžet. To může být například požadovaný hardware,
operační systém, vyžadované knihovny a balíčky, jejich verze apod.>
2.5 Omezení díla
<Popište veškerá omezení, která mohou ovlivnit specifikaci a implementaci. To mohou být
například licenční podmínky využívaných technologií, omezení daná rozhraním hardwarové
platformy nebo software, se kterým má dílo spolupracovat, vyžadovaná bezpečnost, konvence již
existujících částí v případě rozšiřování funkcionality atd. Další kategorií je závislost na třetích
stranách, například nutnost poskytnutí práv na specifický stroj, přistup do sítě, poskytnutí dat
apod.>

3. Vnější rozhraní
3.1 Uživatelské rozhraní, vstupy a výstupy
<Popište základní principy interakce mezi uživatelem a programem. Dále v této části popište
vstupy a výstupy programu.>
3.2 Rozhraní s hardware
<Popište interface díla s hardware, pokud nějaký specificky využívá. Tato část může obsahovat
výčet podporovaného hardware, ovladačů, komunikačních protokolů atd.>
3.3 Rozhraní se software
<Popište interakci díla s ostatními částmi logiky aplikace včetně jejich verze. Tato část může
obsahovat popis interakce s databází, operačním systémem, knihovnami nebo jinými částmi
software. Popište, jaká data se budou předávat a jejich význam. Uveďte, která data budou sdílena
jednotlivými částmi, popřípadě jakým způsobem bude sdílení implementováno.>
3.4 Komunikační rozhraní
<Popište komunikační rozhraní produktu. To může být například zasílání emailů nebo
komunikace se síťovými servery apod. Uveďte použité standardy/technologie/protokoly jako je např.
FTP, HTTP. Specifikujte zabezpečení nebo synchronizaci komunikace, pokud budete nějaké
využívat.>

4. Detailní popis funkcionality
<Tato část má za účel popsat detailně jednotlivé části funkcionality. Následující část bude
v dokumentu tolikrát, kolikrát je to potřeba. Součástí popisu funkcionality je i popis reakce na
chybové stavy.>
4.1 Funkce 1
<Nadpis „Funkce 1“ přepište na název funkcionality, kterou chcete popisovat a popište tuto
funkci programu.>
4.2 Funkce 2
<Nadpis „Funkce 2“ přepište na název funkcionality, kterou chcete popisovat a popište tuto
funkci programu.>
4.3 Funkce 3-n
<Popište stejným způsobem další funkce programu.>

5. Obrazovky
<Načrtněte návrhy nejdůležitějších obrazovek a stručně je popište. Obrazovky mohou být pouze
schématické. U aplikací s více pohledy nebo složitějšími obrazovkami by součástí této sekce měl být
i jednoduchý automat, který bude zobrazovat přechody mezi jednotlivými obrazovkami.
Jednoduchou cestou, jak je rychle vytvořit, je například WinForm designer ve Microsoft Visual
Studiu apod. - ale stačí třeba i naskenovaný přehledný náčrtek na papíře.>
5.1 Obrazovka 1
<Obrázek obrazovky.>
<Popis této obrazovky.>
5.2 Obrazovka 2
<Obrázek obrazovky.>
<Popis této obrazovky.>
5.3 Obrazovka 3-n
<Popište stejným způsobem další obrazovky programu.>

6. Ostatní (mimofunkční) požadavky
6.1 Požadavky na výkon
<Pokud jsou na produkt kladeny nějaké specifické výkonové požadavky, specifikujte a
zdůvodněte je zde. Může jít například o deadlines nebo latenci jednotlivých tasků u real-time
systémů.>
6.2 Požadavky na bezpečnost využívání aplikace
<Specifikujte potenciální nebezpečí spojená s využíváním díla. Tato část může obsahovat
například potenciální ztrátu dat, fyzické škody v případě řídících systémů nebo zdravotní rizika
uživatelů.>
6.3 Požadavky na zabezpečení dat
<Specifikujte požadavky spojené s bezpečností a zabezpečením dat využívaných nebo
vytvořených programem. Popište způsob autentizace uživatelů, pokud je v aplikace využívána.
Uveďte všechny externí požadavky na bezpečnost, které musí být splněny.>
6.4 Požadavky na rozšiřitelnost a začlenitelnost
<Uveďte požadavky na budoucí rozšiřitelnost díla, popřípadě jeho začleňování do existujících
projektů.>

7. Ostatní požadavky
<Specifikujte ostatní požadavky na dílo.>

8. Negativní vymezení
<Uveďte, co není součástí tohoto díla a mohlo by to být implicitně předpokládáno.>

9. Time-line & Milestones
<Specifikujte fáze definovatelné implementace – takzvané milestones, milníky. Uveďte datum,
kdy bude dosaženo milníku a způsob, kterým bude prezentován vašemu vedoucímu, například
prezentací, předvedením, commitem do repository apod. Typický rozestup mezi milníky by měl být
přibližně jeden měsíc. Nezapomeňte na dostatečné rezervy. Není snadné přesně odhadnout, kolik
budete potřebovat na vypracování jednotlivých částí času – programátoři často podceňují časové
nároky třikrát i více!>
Datum Milník Způsob prezentace


Dodatek A: Vymezení pojmů
<Definujte ne zcela obvyklé pojmy nutné k pochopení a správné interpretaci této specifikace.>

Dodatek B: To Be Determined List
<Uveďte seznam částí specifikace, které nebylo možno rozhodnout a popsat do doby dokončení
tohoto dokumentu a budou dospecifikovány později. Při standardním průběhu projektu by tato část
měla být nepotřebná.>
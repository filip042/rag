Anotace
	

Obsah
<todo>

Tabulka revizí

| Jméno   | Datum   | Důvod změny           | Verze   |
|---------|---------|-----------------------|---------|
| <Autor> | <Datum> | <Stručný popis změny> | <Verze> |



# Základní informace  

## 1.1 Popis a zaměření softwarového díla 

Software si načte do databáze ?knihovnu? dokumentů. Uživatel pak může do chatbota zadat požadavek v přirozeném jazyce, na který mu chatbot odpoví spolu se zdroji, odkud informace získal.
## 1.2 Použité technologie
<Uveďte seznam nejdůležitějších technologií (např. knihovny, hardware atd.), které budete
v rámci projektu využívat>
## 1.3 Odkazy (Reference)
<Uveďte odkazy na všechny ostatní informace, se kterými by měl být seznámen čtenář této
specifikace. To mohou být například webové stránky, knihy, manuály, tutoriály nebo odborné
články. Uvádějte je všechny ve stejném formátu obsahujícím autora/instituci, verzi, datum a tam,
kde je to nutné, také krátký popis, popřípadě zdroj.>
účel
- 
- [Rag](https://aws.amazon.com/what-is/retrieval-augmented-generation/)
- elastic search
- SPring AI

# 2. Stručný popis softwarového díla

## 2.1 Důvod vzniku softwarového díla a jeho základní části a cíle řešení

<Popište, proč má softwarové dílo vzniknout. Uveďte, zda se má jednat o samostatný produkt,
rozšíření funkcionality, náhradu existujícího produktu nebo něco jiného. Pokud se jedná o součást
většího systému, popište požadavky na tento systém, popřípadě požadavky na jeho začlenění do
tohoto systému, popište rozhraní, které bude pro začlenění využito. Pokud bude systém složen
z několika částí či vrstev, nakreslete jednoduchý diagram a popište funkce jednotlivých částí.>


## 2.2 Hlavní funkce

<Popište hlavní funkce díla. Nezacházejte do detailů, jednotlivé detaily funkcionality budou
popsány v následujících sekcích, zde postačí pouze stručný „high-level“ přehled.>
	1. vyhledávat v uživatelem indexovaných textech
	vyhledávací algoritmus bude kombinovat klasické vyhledávání pomocí klíčových slov (BM25) se 
	sémantickým vyhledáváním pomocí dokumentových embeddingů (vektorů zachycující obsah textu).
	
	Semanticke vyhledavani umožnuje hledat i bez znalosti konkretnich slov a terminu. 
	Napriklad, pri hledani "programové rozhraní" by mělo najít i dokumenty zminujici jen API.
	
## 2.3 Motivační příklad užití
1. Např. načtení a vyhledávání v softwarové dokumentaci
	
## 2.4 Prostředí aplikace
<Popište prostředí, ve kterém bude aplikace běžet. To může být například požadovaný hardware,
operační systém, vyžadované knihovny a balíčky, jejich verze apod.>
	???
	- 
	
	
## 2.5 Omezení díla
<Popište veškerá omezení, která mohou ovlivnit specifikaci a implementaci. To mohou být
například licenční podmínky využívaných technologií, omezení daná rozhraním hardwarové
platformy nebo software, se kterým má dílo spolupracovat, vyžadovaná bezpečnost, konvence již
existujících částí v případě rozšiřování funkcionality atd. Další kategorií je závislost na třetích
stranách, například nutnost poskytnutí práv na specifický stroj, přistup do sítě, poskytnutí dat
apod.>
	licence elasitc, spring
	System bude vyuzivat Large Language Model (LLM).
	Z praktickych duvodu bude vyuzito 3rd-party LLM pres API (napr. OpenAI)

# 3. Vnější rozhraní

## 3.1 Uživatelské rozhraní, vstupy a výstupy
<Popište základní principy interakce mezi uživatelem a programem. Dále v této části popište
vstupy a výstupy programu.>

## Indexovac cast - 

Bude mit formu jednoducheho CLI (command line interface), 
kde uživatel zadá soubor nebo adresář se soubory, které se mají zaindexovat.
Soubory mohouy byt v jednom z nasledujicich formatu: txt, pdf, md.

## Dotazovaci cast

Bude mít velmi jednoduché GUI
uzivatel zada dotaz v plaintextu, program vrati plaintextovou odpoved spolu s referencemi na puvodni dokumenty

## 3.2 Rozhraní se software
<Popište interakci díla s ostatními částmi logiky aplikace včetně jejich verze. Tato část může
obsahovat popis interakce s databází, operačním systémem, knihovnami nebo jinými částmi
software. Popište, jaká data se budou předávat a jejich význam. Uveďte, která data budou sdílena
jednotlivými částmi, popřípadě jakým způsobem bude sdílení implementováno.>
    - viz obrazek

## 3.4 Komunikační rozhraní
<Popište komunikační rozhraní produktu. To může být například zasílání emailů nebo
komunikace se síťovými servery apod. Uveďte použité standardy/technologie/protokoly jako je např.
FTP, HTTP. Specifikujte zabezpečení nebo synchronizaci komunikace, pokud budete nějaké
využívat.>
	Asi jak se komunikuje s databází?
    - komunikace s Elastic a LLM pres HTTP

Aplikace bude implementována jako aplikační server, který bude vystavovat své funkce pomocí REST API:


Dotazovací čá

# 4. Detailní popis funkcionality

Systém bude mít dva základní moduly:
- Indexační - system nacte relevantni dokumenty, predzpracuje je a ulozi je do indexu
- dotazovaci - uzivatel polozi dotaz v beznem jazyce a dostane odpoved 

Index bude zalozen na ElasticSeach.




<Tato část má za účel popsat detailně jednotlivé části funkcionality. Následující část bude
v dokumentu tolikrát, kolikrát je to potřeba. Součástí popisu funkcionality je i popis reakce na
chybové stavy.>
## 4.1 Indexace 
<Nadpis „Funkce 1“ přepište na název funkcionality, kterou chcete popisovat a popište tuto
funkci programu.>
## 4.2 Dotazování

Uzivatel polozi ...
Systém naje relevantni casti dokumentu pomoci vektorového hledání a BM25
Na zaklade nich vytvori odpoved 

# 5. Obrazovky
<Načrtněte návrhy nejdůležitějších obrazovek a stručně je popište. Obrazovky mohou být pouze
schématické. U aplikací s více pohledy nebo složitějšími obrazovkami by součástí této sekce měl být
i jednoduchý automat, který bude zobrazovat přechody mezi jednotlivými obrazovkami.
Jednoduchou cestou, jak je rychle vytvořit, je například WinForm designer ve Microsoft Visual
Studiu apod. - ale stačí třeba i naskenovaný přehledný náčrtek na papíře.>
## 5.1 Obrazovka 1
<Obrázek obrazovky.>
<Popis této obrazovky.>

# 6. Ostatní (mimofunkční) požadavky  

## 6.1 Požadavky na výkon
Rychlost neni cilem.
Z praktickych duvodu nebude embeddovani pocitano na GPU ale na CPU, coz znamena ze bude podstatne pomalejsi
Zpracovani dotazu by melo trvat maximalne 60 vterin, idealne do 15 vterin.
Zaindexovani desetistrankoveho dokumentu by melo byt take do 60 vterin.

## 6.3 Požadavky na zabezpečení dat
<Specifikujte požadavky spojené s bezpečností a zabezpečením dat využívaných nebo
vytvořených programem. Popište způsob autentizace uživatelů, pokud je v aplikace využívána.
Uveďte všechny externí požadavky na bezpečnost, které musí být splněny.>
Aplikace nebude obsahovat nejakou formu zabezpeceni

## 6.4 Požadavky na rozšiřitelnost a začlenitelnost
<Uveďte požadavky na budoucí rozšiřitelnost díla, popřípadě jeho začleňování do existujících
projektů.>
Tim ze je aplikace ve forme aplikacniho serveru, bude mozne vymenit jak indexacni CLI, tak vyhledavaci GUI

# 7. Ostatní požadavky  
<Specifikujte ostatní požadavky na dílo.>

# 8. Negativní vymezení  
<Uveďte, co není součástí tohoto díla a mohlo by to být implicitně předpokládáno.>
Asi ne konverzace

# 9. Time-line & Milestones  

| Datum | Milník | Způsob | prezentace |
| ----- | ------ | ------ | ---------- |


Dodatek A: Vymezení pojmů
<Definujte ne zcela obvyklé pojmy nutné k pochopení a správné interpretaci této specifikace.>

Dodatek B: To Be Determined List
<Uveďte seznam částí specifikace, které nebylo možno rozhodnout a popsat do doby dokončení
tohoto dokumentu a budou dospecifikovány později. Při standardním průběhu projektu by tato část
měla být nepotřebná.>
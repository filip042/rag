Anotace
	

Obsah
<tbd>

Tabulka revizí

| Jméno      | Datum       | Důvod změny           | Verze   |
|------------|-------------|-----------------------|---------|
| Filip Hana | 24. 3. 2025 | Vytvořeno             | 0.1     |



# Základní informace  

## 1.1 Popis a zaměření softwarového díla 

Software si načte do databáze dokumenty ze souboru.
Uživatel pak může do chatbota zadat dotaz v přirozeném jazyce, na který mu chatbot odpoví spolu se zdroji, odkud informace získal.

## 1.2 Použité technologie

- Java
  - Možné alternativy
    - Python, Javascript:
      - Javu umím lépe
- Elasticsearch
  - Možné alternativy:
    - Opensearch
- Spring AI (+ možná Spring Boot)
  - Možné alternativy:
    - Langchain4j
      - Spring AI by mělo být jednodušší propojit se zbytkem Spring Frameworku
- LLM

## 1.3 Odkazy (Reference)

- Základní informace o RAG
  - [https://aws.amazon.com/what-is/retrieval-augmented-generation/](https://aws.amazon.com/what-is/retrieval-augmented-generation/), Amazon, 2025
- Elasticsearch
  - [https://www.elastic.co/](https://www.elastic.co/), Elastic NV, 2025
- Spring AI
  - [https://spring.io/projects/spring-ai](https://spring.io/projects/spring-ai), Spring AI, 2025

# 2. Stručný popis softwarového díla

## 2.1 Důvod vzniku softwarového díla a jeho základní části a cíle řešení

Důvodem vzniku je poskytnout uživateli jednoduchý způsob jak vyhledávat v dokumentech.
Dílo bude navrženo jako samostatný produkt, který je tvořen ze dvou hlavních částí:

- Indexační část
  - Uživatel vybere adresář s dokumenty, které software načte do databáze.
    - Bude si moci vybrat jestli se při změně v adresáři indexované dokumenty budou aktualizovat, nebo jestli bude o aktualizace uživatel žádat manuálně 
- Dotazovací část
  - Uživatel přes GUI zadá dotaz, který software zpracuje a využije na vyhledání relevantních informací v databázi, které pak čitelným způsobem vypíše.
- obr. 


## 2.2 Hlavní funkce

Dílo umožňuje uživateli vyhledávat v uživatelem indexovaných textech.
Vyhledávací algoritmus bude kombinovat klasické vyhledávání pomocí klíčových slov (Elasticsearch používá BM25) se
sémantickým vyhledáváním pomocí dokumentových embeddingů (vektorů zachycující obsah textu).
	
Sémantické vyhledávání umožňuje hledat i bez znalostí konkrétních slov a termínů. 
Například, při hledání "programové rozhraní" by mělo najít i dokumenty zmiňujici jen API.

### Usecases:
- Přihlášení
  - Při spuštění aplikace je uživatel požádán, aby si vybral účet 
    - Defaultní je guest user, u který má přístup k, a může vytvářet jen, veřejné workspacy
- Otevření workspacu
  - Pokud nějaký existuje, může si uživatel vybrat z existujících workspaců
  - Pokud ne, musí si nějaký vytvořit
- Vytvoření workspacu
  - Uživatel vybere adresář, ze kterého se budou indexovat dokumenty
    - Možná dát možnost vybrat několik adresářů
  - Při vytváření workspacu má uživatel možnost si vybrat jestli:
    - Bude workspace soukromý/sdílený jen některým uživatelům/veřejný
      - Guest uživateli je přístupná jen poslední možnost
      - Možná možnost že workspace bude existovat jen na danou session
    - Bude adresář workspacu automaticky kontrolován pro změny/jestli bude kontrolován jen po požádání uživatelem
      - Uživatel je notifikován, pokud byl obsah adresáře změněn (možná)
- Smazání workspacu
  - Workspacy které uživatel vytvořil, nebo ke kterým má admin přístup, může mazat
  - Workspacy, ke kterým nemá uživatel admin přístup, si může odebrat z knihovny nabízených workspaců
- Dotazování
  - Po výběru zdrojového adresáře se může uživatel ptát na libovolné otázky o daných dokumentech. Chatbot si pamatuje historii dotazů z dané session
    - Mohl by si pamatovat často kladené dotazy spojené s daným workspacem a ty uživateli nabídnout na začátku session
  - Uživatel může dokumenty, ve kterých vyhledává, filtrovat (podle autora, tématu, datumu etc.), případně prohledávané dokumenty vybrat ručně
	
## 2.3 Motivační příklad užití

Uživatel si do aplikace načte například sbírku zákonů, smlouvy, nebo dokumentaci.
Software mu umožní získat z těchto dokumentů potřebné informace, aniž by musel znát specifickou terminologii, nebo vědět,
v jakých dokumentech se o dané věci píše.

## 2.4 Prostředí aplikace

Program bude odladěn na Windows, měl by ale fungovat i v jiných prostředích.
S největší pravděpodobností bude testován i na Linuxu.
	
## 2.5 Omezení díla

- licence:
  - Elasticsearch - Elastic License 2.0
    - OpenSearch má Apache License 2.0
  - Spring - Apache License 2.0

Z praktických důvodů bude využito 3rd-party LLM přes API (napr. OpenAI). Licence záleží na konkrétním modelu.

# 3. Vnější rozhraní

## 3.1 Uživatelské rozhraní, vstupy a výstupy

Aplikace bude využívat grafické uživatelské rozhraní, které je podrobněji popsáno v následujících sekcích.  
Uživatel bude mít možnost si vybrat účet, což ovlivní, ke kterým workspacům bude mít přístup.

### 3.1.1 Indexovací část

Uživateli zadá informace o workspacu do formuláře, který obsahuje následující vstupní pole:
- Adresář ze kterého se dokumenty zaindexují (uživateli se asi zobrazí nějaký souborový prohlížeč)
- Kdo z uživatelů bude mít k workspacu přístup
- Jestli bude databáze updatována automaticky po změně adresáře

Soubory mohou být v jednom z následujicich formátů: txt, pdf, md, Tex/Latex, Html

### 3.1.2 Dotazovaci cast

Uživatel zadá dotaz v plaintextu, program vrátí plaintextovou odpověď spolu s referencemi na původní dokumenty.

## 3.2 Rozhraní se software

- viz nákres

Indexační část aplikace bude přijímat jako vstup cestu k souboru v plaintextu, předanou formulářem. Načte kompatibilní soubory a rozdělí je na menší bloky.
Tyto bloky pošle do LLM přes Http, které je poupraví tak, aby každý blok dával sám o sobě smysl. Tyto poupravené bloky pak pošle po Http zpátky.
Aplikace pak jednotlivé bloky embedduje, a původní blok spolu s embeddingem zaindexuje do databáze. 

Dotazovací část aplikace bude přijímat dotaz v plaintextu. Ten se převede na vektor,
a aplikace pak použije původní text aby v Elastic databázi hledala s pomocí klíčových slov, a vektor aby hledala vektorově.
ElasticSearch vrátí nějaký počet bloků pravděpodobně obsahující relevantní informace k dotazu, ze kterých pak LLM vytvoří smysluplnou odpověď.  


## 3.4 Komunikační rozhraní

Aplikace bude komunikovat s Elasticsearch a LLM přes HTTP.

# 4. Detailní popis funkcionality

Systém bude mít dva základní moduly:
- Indexační - systém načte relevantní dokumenty, předzpracuje je a uloží je do indexu
- Dotazovací - uživatel položí dotaz v běžném jazyce a dostane odpověď 

Index bude založen na ElasticSeach.

## 4.1 Indexace

Uživatel vybere adresář, ze kterého se budou indexovat dokumenty v podporovaných formátech (txt, pdf, md, Tex/Latex, HTML).
Tyto dokumenty budou rozděleny na menší části, které pak budou přepsány LLM tak, aby samostatně dávaly smysl (např. správná zájmena)
Tyto bloky pak budou zaindexovány do databáze.

Aplikační server vrátí http status kód podle zvyklostí RestApi a chyby zaznamená do logu.
Aplikace všechny chyby, ať už na své straně nebo na straně serveru, zobrazí v čitelné podobě uživateli.

Možné chyby:
- Na straně serveru:
  - Špatný formát dokumentu
  - Nedostupné LLM

## 4.2 Dotazování

Uživatel položí chatbotu dotaz přes textbox v GUI.
Systém najde relevantní části dokumentu pomocí vektorového hledání a BM25.
Na jejich základě vytvoří odpověď, kterou spolu s referencemi na původní dokumenty vypíše uživateli.

Podobně jako v indexační části, aplikační server vrátí http status kód.
Případné chyby zaznamenává server do logu a GUI vypíše uživateli.

Možné chyby:
- Na straně serveru:
  - Nedostupné LLM
  - Chybějící dokumenty (asi není chyba, spíše speciální případ)

# 5. Obrazovky

## 5.1 Chatbot

(Něco ve stylu UIMockup.svg)
GUI bude vypadat podobně jako to u dalších chatbotů, například těch od OpenAI, případně jako GUI v aplikacích pro zasílání textových zpráv.

## 5.2 Výběr uživatele/workspacu

//

## 5.3 Vytvoření uživatele workspacu

//

# 6. Ostatní (mimofunkční) požadavky  

## 6.1 Požadavky na výkon
Rychlost není cílem.
Z praktických důvodů nebude embeddování počítáno na GPU ale na CPU, což znamená že bude podstatně pomalejší.
Zpracování dotazů by mělo trvat maximálně 60 vteřin, ideálně by mělo být hotové do 15 vteřin.
Zaindexování desetistránkového dokumentu by mělo být také do 60 vteřin.

## 6.3 Požadavky na zabezpečení dat

Aplikace nebude obsahovat žádnou formu zabezpečení.

## 6.4 Požadavky na rozšiřitelnost a začlenitelnost

Tím, že je aplikace ve formě aplikačního serveru, bude možné vyměnit jak indexační, tak vyhledávací uživatelské rozhraní.  
Zároveň bude možnost přidat podporu načítání více druhů dokumentů 

# 9. Time-line & Milestones  

| Datum | Milník | Způsob | prezentace |
| ----- | ------ | ------ | ---------- |


Dodatek A: Vymezení pojmů
- RAG - Retrieval Augmented Generation je způsob 

Dodatek B: To Be Determined List
<Uveďte seznam částí specifikace, které nebylo možno rozhodnout a popsat do doby dokončení
tohoto dokumentu a budou dospecifikovány později. Při standardním průběhu projektu by tato část
měla být nepotřebná.>
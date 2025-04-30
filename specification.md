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
Uživatel pak může do chatbotu zadat dotaz v přirozeném jazyce, na který mu chatbot odpoví spolu se zdroji, odkud informace získal.

## 1.2 Použité technologie

- Java
  - Možné alternativy
    - Python, Javascript:
      - Javu umím z těchto jazyků nejlépe
- Elasticsearch
  - Možné alternativy:
    - OpenSearch
      - Základní funkcionalitu má stejnou jako Elasticsearch, ale je novější, takže je k němu značně méně online zdrojů (například dotazů na stackoverflow)
      - Na druhou stranu, kdyby se ukázalo, že neplacená verze ElasticSearch nepodporuje nějakou potřebnou funkcionalitu, tak kvůli tomu, jak funguje Spring AI, by nemělo být příliš náročné daný kód přepsat
- Spring AI
  - Možné alternativy:
    - Langchain4j
      - Po vyzkoušení obou frameworků jsem usoudil, že je jednodušší pracovat se Spring AI
- Spring Boot
- Spring MVC + ThymeLeaf
  - Možné alternativy:
    - Vaadin
      - Na vytvoření několika málo potřebných stránek je příliš složitý
    - Javascript, Htmx
      - Spring MVC je jednodušší integrovat se zbytkem aplikace
- Apache Tika
  - Vybráno kvůli kompatibilitě se Spring AI
- LLM
  - Ollama, přinejmenším na počáteční testování
  - Způsob, kterým je navržen Spring AI, umožňuje poměrně jednoduše měnit použitý model - nemělo by být příliš nároční kód přepsat tak, aby využíval např. OpenAI model

## 1.3 Odkazy (Reference)

- Základní informace o RAG
  - [https://aws.amazon.com/what-is/retrieval-augmented-generation/](https://aws.amazon.com/what-is/retrieval-augmented-generation/), Amazon, 2025
- Elasticsearch
  - [https://www.elastic.co/](https://www.elastic.co/), Elastic NV, 2025
- Spring AI
  - [https://spring.io/projects/spring-ai](https://spring.io/projects/spring-ai), Spring AI, 2025
- Apache Tika
  - [https://tika.apache.org/](https://tika.apache.org/), The Apache Software Foundation, 2025
- Ollama
  - [https://ollama.com/](https://ollama.com/), Ollama, 2025
- ThymeLeaf
  - [https://www.thymeleaf.org/](https://www.thymeleaf.org/), Thymeleaf, 2025

# 2. Stručný popis softwarového díla

## 2.1 Důvod vzniku softwarového díla a jeho základní části a cíle řešení

Důvodem vzniku je poskytnout uživateli jednoduchý způsob jak vyhledávat v dokumentech.
Dílo bude navrženo jako samostatný produkt, který je tvořen ze dvou hlavních částí:

- Indexační část
  - Uživatel vybere adresář s dokumenty, které software načte do databáze.
- Dotazovací část
  - Uživatel přes GUI zadá dotaz, který software zpracuje a využije na vyhledání relevantních informací v databázi, které pak čitelným způsobem vypíše. 


## 2.2 Hlavní funkce

Dílo umožňuje uživateli vyhledávat v uživatelem indexovaných textech.
Vyhledávací algoritmus bude kombinovat klasické vyhledávání pomocí klíčových slov (Elasticsearch používá BM25) se
sémantickým vyhledáváním pomocí dokumentových embeddingů (vektorů zachycující obsah textu).
	
Sémantické vyhledávání umožňuje hledat i bez znalostí konkrétních slov a termínů. 
Například, při hledání "programové rozhraní" by mělo najít i dokumenty zmiňujici jen API.

### Usecases:
- Vytvoření účtu
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Spuštěna aplikace
  - Kroky:
    - Při spuštění aplikace má uživatel možnost si vytvořit účet zmáčníknutím příslušného tlačítka
    - Uživateli je zobrazen formulář, s políčky na relevantní informace
    - Po vyplnění formuláře může uživatel stisknout tlačítko na odeslání
    - Uživateli se vytvoří nový účet, do kterého je automaticky přihlášen
    - Alternativně může uživatel stisknutím tlačítka tvorbu účtu zrušit
  - Důsledky:
    - Uživatel přihlášen
- Přihlášení
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Spuštěna aplikace
    - Existuje alespoň jeden účet
  - Kroky:
    - Uživateli je nabídnutý seznam registrovaných účtů
    - Uživatel si vybere účet a zadá heslo. Pokud je správné, program uživatele přihlásí
    - Přístupný bez hesla je defaultní guest user, který má přístup k, a může vytvářet jen, veřejné workspacy
  - Důsledky:
    - Uživatel je přihlášen
- Otevření workspacu
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Uživatel je přihlášen
    - Existuje alespoň jeden uživateli přístupný workspace
  - Kroky:
    - Pokud nějaký existuje, může si uživatel vybrat z existujících workspaců
    - Pokud ne, musí si nějaký vytvořit
  - Důsledky:
    - Načtení workspacu
- Vytvoření workspacu
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Uživatel je přihlášen
  - Kroky:
    - Uživatel vybere adresář, ze kterého se budou indexovat dokumenty
      - Možná dát možnost vybrat několik adresářů
    - Při vytváření workspacu má uživatel možnost si vybrat jestli:
      - Bude workspace soukromý/sdílený jen některým uživatelům/veřejný
        - Guest uživateli je přístupná jen poslední možnost
      - Bude adresář vymazán po dané session
      - Bude adresář workspacu automaticky kontrolován pro změny/jestli bude kontrolován jen po požádání uživatelem
        - Uživatel je notifikován (nějakou malou ikonou), pokud byl obsah adresáře změněn
      - Jazyk databáze workspacu (defaultně podle locale, pokud je daný jazyk podporovaný)
  - Důsledky:
    - Workspace je vytvořen
    - Uživatel je přihlášen
- Smazání workspacu
  - Účastník:
    - Admin, případně registrovaný uživatel
  - Předpoklady:
    - Uživatel je přihlášen
    - Uživatel není přihlášen do guest účtu
  - Kroky:
    - Workspacy, ke kterým má uživatel admin přístup (ať už protože je vytvořil, nebo mu byl přístup přidělen), může smazat stisknutím tlačítka v seznamu workspaců
    - Uživateli se pak zobrazí popup, kde musí akci potvrdit
    - Pokud nemá uživatel k danému workspacu admin přístup, tato posloupnost kroků workspace jen odebere ze seznamu přístupných workspaců
      - Protože je Guest User sdílený účet, tak tuto možnost nemá
  - Důsledky:
    - Workspace je vymazán z databáze
    - Alternativně, uživatel k němu ztratí přístup
- Kontrola změny v workspacu
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Uživatel je přihlášen a má vybraný workspace
  - Kroky:
    - Uživatel může po vybrání workspacu zmáčknout tlačítko, které zkontroluje, jestli se od poslední kontroly v adresáři stala nějaká změna
    - Pokud nějaká změna nastala, updatuje workspace
  - Důsledky:
    - Workspace je aktualizován
- Dotazování
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Uživatel je přihlášen a má vybraný workspace
  - Kroky:
    - Uživatel může do textového pole zadávat libovolné otázky o dokumentech v workspacu
    - Chatbot si pamatuje historii dotazů z dané session
      - Může nabídnout uživateli na začátku session často kladené dotazy
    - Uživatel může prohledávané dokumenty filtrovat (podle autora, tématu, datumu etc.), případně ručně vybrat prohledávané dokumenty
    - Když má uživatel zadaný dotaz a vybrané parametry, zmáčkne tlačítko, které spustí vyhledávání
  - Důsledky:
    - Chatbot vypíše odpověď na základě dostupných dokumentů
- Změna workspacu
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Uživatel je přihlášen a má vybraný workspace
  - Kroky:
    - Uživatel stiskne tlačítko "Change workspace"
  - Důsledky:
    - Program uživatele vrátí na obrazovku na výběr workspaců
- Změna uživatele
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Uživatel je přihlášen
  - Kroky:
    - Uživatel stiskne tlačítko "Sign out"
  - Důsledky:
    - Program uživatele odhlásí a vrátí na obrazovku na výběr uživatelů
- Změna jazyka GUI (Defaultně podle locale)
  - Účastník:
    - Uživatel
  - Předpoklady:
    - Uživatel je přihlášen
  - Kroky:
    - Uživatel stiskne tlačítko "Change language" (s nějakou ikonou na změnu jazyka)
  - Důsledky:
    - Program změní jazyk GUI na ten vybraný uživatelem
  - Poznámky:
    - Program uživatele upozorní, že jazyk GUI a jazyk vyhledávání/komunikace s LLM jsou nezávislé
    - Program bude hlavně v angličtině, a alespoň nějaká jeho část bude v češtině
	
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
  - Apache Tika - Apache License 2.0
  - Ollama - MIT License 
    - Licence také záleží na konkrétním modelu.
  - Thymeleaf - Apache License 2.0

# 3. Vnější rozhraní

## 3.1 Uživatelské rozhraní, vstupy a výstupy

Aplikace bude využívat grafické uživatelské rozhraní, které je podrobněji popsáno v sekci 5.  
Uživatel bude mít možnost si vybrat účet, což ovlivní, ke kterým workspacům bude mít přístup.

### 3.1.1 Indexovací část

Uživateli zadá informace o workspacu do formuláře, který obsahuje následující vstupní pole:
- Adresář ze kterého se dokumenty zaindexují (uživateli se asi zobrazí nějaký souborový prohlížeč)
- Kdo z uživatelů bude mít k workspacu přístup
- Jestli bude databáze updatována automaticky po změně adresáře

Soubory mohou být v jednom z následujicich formátů: txt, pdf, md, Tex/Latex, Html, případně další formáty podporované Apache Tika

### 3.1.2 Dotazovaci část

Uživatel zadá dotaz v plaintextu, program vrátí plaintextovou odpověď spolu s referencemi na původní dokumenty.

## 3.2 Rozhraní se software

- viz nákres

Indexační část aplikace bude přijímat jako vstup cestu k souboru, předanou formulářem. Načte kompatibilní soubory a rozdělí je na menší bloky.
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

Aplikační server vrátí http status kód podle zvyklostí Rest API a chyby zaznamená do logu.
Aplikace všechny chyby, ať už na své straně nebo na straně serveru, zobrazí v čitelné podobě uživateli.

Možné chyby:
- Špatný formát dokumentu
- Nedostupné LLM

## 4.2 Dotazování

Uživatel položí chatbotu dotaz přes textbox v GUI.
Systém najde relevantní části dokumentu pomocí vektorového hledání a BM25.
Na jejich základě vytvoří odpověď, kterou spolu s referencemi na původní dokumenty vypíše uživateli.

Podobně jako v indexační části, aplikační server vrátí http status kód.
Případné chyby zaznamenává server do logu a GUI vypíše uživateli.

Možné chyby:
- Nedostupné LLM

# 5. Obrazovky

## 5.1 Chatbot

(Něco ve stylu UIMockup.svg)
GUI bude vypadat podobně jako to u dalších chatbotů, například těch od OpenAI, případně jako GUI v aplikacích pro zasílání textových zpráv.

## 5.2 Výběr účtu/workspacu

Uživatel si buď vybere účet/workspace z dropdown menu, nebo stiskne tlačítko "vytvořit"

## 5.3 Vytvoření účtu/workspacu

formulář - viz. sekce 3.1.1

# 6. Ostatní (mimofunkční) požadavky  

## 6.1 Požadavky na výkon
Rychlost není cílem.
Z praktických důvodů nebude embeddování počítáno na GPU ale na CPU, což znamená že bude podstatně pomalejší.
Zpracování dotazů by mělo trvat maximálně 60 vteřin, ideálně by mělo být hotové do 15 vteřin.
Zaindexování desetistránkového dokumentu by mělo být také do 60 vteřin.

## 6.3 Požadavky na zabezpečení dat

Aplikace nebude obsahovat žádnou formu zabezpečení, kromě hesla účtů. (todovrozmyslet)

## 6.4 Požadavky na rozšiřitelnost a začlenitelnost

Tím, že je aplikace ve formě aplikačního serveru, bude možné vyměnit jak indexační, tak vyhledávací uživatelské rozhraní.  
Zároveň bude možnost přidat podporu načítání více druhů dokumentů ve formě pluginů  
Zároveň bude možné přidat více jazyků GUI

# 9. Time-line & Milestones  

| Datum | Milník | Způsob | prezentace |
| ----- | ------ | ------ | ---------- |


Dodatek A: Vymezení pojmů
- RAG - Retrieval Augmented Generation je způsob, jak zajistit, aby LLM dokázal odpovídat na dotazy z dat, na kterých nebyl natrénován, aniž by bylo nutné ho přetrénovat

Dodatek B: To Be Determined List
<Uveďte seznam částí specifikace, které nebylo možno rozhodnout a popsat do doby dokončení
tohoto dokumentu a budou dospecifikovány později. Při standardním průběhu projektu by tato část
měla být nepotřebná.>
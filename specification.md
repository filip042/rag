Anotace
	

Obsah
<todo>

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
      - Langchain4j 
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
  - Uživatel do příkazové řádky zadá cestu k souboru s dokumenty, které software načte do databáze.
- Dotazovací část
  - Uživatel přes GUI zadá dotaz, který software zpracuje a využije na vyhledání relevantních informací v databázi, které pak čitelným způsobem vypíše.
- obr. 


## 2.2 Hlavní funkce

Dílo umožňuje uživateli vyhledávat v uživatelem indexovaných textech.
Vyhledávací algoritmus bude kombinovat klasické vyhledávání pomocí klíčových slov (Elasticsearch používá BM25) se
sémantickým vyhledáváním pomocí dokumentových embeddingů (vektorů zachycující obsah textu).
	
Sémantické vyhledávání umožňuje hledat i bez znalostí konkrétních slov a termínů. 
Například, při hledání "programové rozhraní" by mělo najít i dokumenty zminujici jen API.
	
## 2.3 Motivační příklad užití

Uživatel si do aplikace načte například sbírku zákonů, smlouvy, nebo dokumentaci.
Software mu umožní získat z těchto dokumentů potřebné informace, aniž by musel znát specifickou terminologii, nebo vědět, v jakých dokumentech se o dané věci píše.

## 2.4 Prostředí aplikace

Program bude odladěn na Windows, měl by ale fungovat i v jiných prostředích.
(možná ještě trochu na Linuxu)
	
## 2.5 Omezení díla

- licence:
  - Elasticsearch - Elastic License 2.0
    - OpenSearch má Apache License 2.0
  - Spring - Apache License 2.0

Z praktických důvodů bude využito 3rd-party LLM přes API (napr. OpenAI). Licence záleží na konkrétním modelu.

# 3. Vnější rozhraní

## 3.1 Uživatelské rozhraní, vstupy a výstupy

### 3.1.1 Indexovací část

Bude mít formu jednoducheho CLI (command line interface), 
kde uživatel zadá soubor nebo adresář se soubory, které se mají zaindexovat.

Soubory mohou být v jednom z následujicich formátů: txt, pdf, md.

### 3.1.2 Dotazovaci cast

Aplikace bude mít velmi jednoduché GUI.  
Uživatel zadá dotaz v plaintextu, program vrátí plaintextovou odpověď spolu s referencemi na původní dokumenty.

## 3.2 Rozhraní se software
<Popište interakci díla s ostatními částmi logiky aplikace včetně jejich verze. Tato část může
obsahovat popis interakce s databází, operačním systémem, knihovnami nebo jinými částmi
software. Popište, jaká data se budou předávat a jejich význam. Uveďte, která data budou sdílena
jednotlivými částmi, popřípadě jakým způsobem bude sdílení implementováno.>
    - viz obrazek
Indexační část aplikace bude přijímat jako vstup cestu v plaintextu


## 3.4 Komunikační rozhraní

Aplikace bude komunikovat s Elasticsearch a LLM přes HTTP.

# 4. Detailní popis funkcionality

Systém bude mít dva základní moduly:
- Indexační - systém načte relevantní dokumenty, předzpracuje je a uloží je do indexu
- Dotazovací - uživatel položí dotaz v běžném jazyce a dostane odpověď 

Index bude založen na ElasticSeach.

## 4.1 Indexace

Uživatel do příkazové řádky zadá cestu k adresáři obsahující dokumenty v podporovaných formátech (txt, pdf, md).
Tyto dokumenty budou rozděleny na menší části, které pak budou přepsány LLM tak, aby samostatně dávaly smysl (např. správná zájmena)
Tyto bloky pak budou zaindexovány do databáze.

Aplikační server vrátí http status kód podle zvyklostí RestApi a chyby zaznamená do logu.
CLI všechny chyby, ať už na své straně nebo na straně serveru, zobrazí v čitelné podobě uživateli.

Možné chyby:
- Na straně serveru:
  - Špatný formát dokumentu
  - Nedostupné LLM
- Na straně příkazové řádky:
  - Neplatná cesta

## 4.2 Dotazování

Uživatel položí chatbotu dotaz přes GUI.
Systém najde relevantní části dokumentu pomocí vektorového hledání a BM25.
Na jejich základě vytvoří odpověď.

Podobně jako v indexační části, aplikační server vrátí http status kód.
Případné chyby zaznamenává server do logu a GUI vypíše uživateli.

Možné chyby:
- Na straně serveru:
  - Nedostupné LLM
  - Chybějící dokumenty

# 5. Obrazovky

## 5.1 Obrazovka 1
()
GUI bude vypadat podobně jako to u dalších chatbotů, například těch od OpenAI, případně jako GUI v aplikacích pro zasílání textových zpráv.

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

# 8. Negativní vymezení

Software nebude uchovávat historii dotazů. Nebude na ni proto možno odkazovat.

# 9. Time-line & Milestones  

| Datum | Milník | Způsob | prezentace |
| ----- | ------ | ------ | ---------- |


Dodatek A: Vymezení pojmů
- RAG - Retrieval Augmented Generation je způsob 

Dodatek B: To Be Determined List
<Uveďte seznam částí specifikace, které nebylo možno rozhodnout a popsat do doby dokončení
tohoto dokumentu a budou dospecifikovány později. Při standardním průběhu projektu by tato část
měla být nepotřebná.>
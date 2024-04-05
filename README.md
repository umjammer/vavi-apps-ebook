[![Release](https://jitpack.io/v/umjammer/vavi-apps-ebook.svg)](https://jitpack.io/#umjammer/vavi-apps-ebook)
[![Java CI](https://github.com/umjammer/vavi-apps-ebook/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-apps-ebook/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-apps-ebook/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-apps-ebook/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-apps-ebook

aozora to epub3 converter.<br/>
this project is based on https://github.com/kyukyunyorituryo/AozoraEpub3

<img src="https://github.com/umjammer/vavi-apps-ebook/assets/493908/3e7b4494-7996-496a-8d2a-bd41df293eff" width="120" alt="aozora bunko logo"/><sub><a href="https://www.aozora.gr.jp/"> CC BY-ND 4.0</a></sub>
<span>👉</span>
<img src="https://github.com/umjammer/vavi-apps-ebook/assets/493908/5bb47511-d463-43ab-b53b-ddc4489fc699" height="120" alt="epub logo" /><sub><a href="https://www.w3.org/publishing/groups/epub-wg/">Public Domain</a></sub>

## Goal

 * command line filter like

```
 $ cat aozora.txt | aozora2epub3 | epub-viewer
```

## Install

* https://jitpack.io/#umjammer/vavi-apps-ebook

## Usage

```
 $ mvn -o -P a2e_2 antrun:run -Dinfile="in.txt"
```

## Others

 - [PDF right to left](src/test/java/PdfRtoL.java) ... make a pdf direction right to left (for Japanese Manga)

## Term

* IVS ... Ideographic Variation Sequence (異体字シーケンス)

## TODO

 * ~~epub manipulator~~
 * PDF tool


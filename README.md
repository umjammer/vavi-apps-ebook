[![Release](https://jitpack.io/v/umjammer/vavi-apps-ebook.svg)](https://jitpack.io/#umjammer/vavi-apps-ebook)
[![Java CI](https://github.com/umjammer/vavi-apps-ebook/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-apps-ebook/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-apps-ebook/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-apps-ebook/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-apps-ebook

aozora to epub3 converter.<br/>
this project is based on https://github.com/kyukyunyorituryo/AozoraEpub3

## Goal

 * command line filter like

```
 $ cat aozora.txt | aozora2epub3 | epub-viewer
```

## Usage

```
 $ mvn -o -P a2e_2 antrun:run -Dinfile="in.epub"
```

## Term

 * IVS ... Ideographic Variation Sequence (異体字シーケンス)

## Others

 - [PDF right to left](src/test/java/PdfRtoL.java) ... make a pdf direction right to left (for Japanese Manga)

## TODO


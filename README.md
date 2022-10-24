[![Release](https://jitpack.io/v/umjammer/vavi-apps-aozora2epub3.svg)](https://jitpack.io/#umjammer/vavi-apps-aozora2epub3)
[![Java CI](https://github.com/umjammer/vavi-apps-aozora2epub3/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-apps-aozora2epub3/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-apps-aozora2epub3/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-apps-aozora2epub3/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-apps-aozora2epub3

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
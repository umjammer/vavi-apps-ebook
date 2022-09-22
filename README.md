[![Release](https://jitpack.io/v/umjammer/vavi-apps-aozora2epub.svg)](https://jitpack.io/#umjammer/vavi-apps-aozora2epub)
[![Java CI](https://github.com/umjammer/vavi-apps-aozora2epub/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-apps-aozora2epub/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-apps-aozora2epub/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-apps-aozora2epub/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-apps-aozora2epub

aozora to epub converter.

## Goal

 * command line filter like

```
 $ cat aozora.txt | aozora2epub | epub-viewer
```

## Usage

```
 $ mvn -o -P a2e_2 antrun:run -Dinfile="in.epub"
```

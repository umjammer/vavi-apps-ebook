AozoraEpub3
============

説明
------------
青空文庫テキスト→ePub3変換  
青空文庫の注記入りテキストファイルをePub3ファイル(zip圧縮)に変換するツールです  


利用上の注意
------------
利用は自己責任でお願いします。  
* 現状、いくつか対応していない注記があります。  
* 青空文庫の注記仕様外の注記等で変換したxhtmlエラーで章ごと表示されない場合があります。  
* 4バイト文字を出力すると対応していない端末では外字以降表示されない場合があります。  
  （変換しないオプション選択時は注記を小書きで表示）  
* 背景が透明なPNGは元画像の背景を白にしてから変換してください  
  （Readerで黒または灰色になる）  

バグや変換できない注記があった配布サイトで報告お願いします。  


変換時の注意
------------
コメントの異常、対応していない注記、変換できなかった外字はログに表示されるので適宜元テキスト修正してください。  
- 仕様外や一部の揺らぎのある注記は対応しません。  
- 外字注記内で外字注記が使われている場合はエラーになります（対応予定無し）  
  →※［＃「姉」の正字、「女＋※［＃第3水準1-85-57］のつくり」 とログに出たら  
    該当部分の元テキストを ※［＃「姉」の正字、U+59CA］に修正  
- 注記内に注記がある底本コメント注記は削除してください  


動作環境
------------
Java 6 / Java 7 の動作環境 ( http://www.java.com/ja/ )


使い方
------------
#### インストール
　AozoraEpub3-*.zip を任意のフォルダに解凍します。  

#### 起動
　AozoraEpub3.jar をダブルクリックして実行します。  
　またはコンソールから "java -jar AozoraEpub3.jar" でも実行可。  
　※javaが見えなければフルパスで指定  
　　例: "C:\Program Files\Java\jre\bin\java.exe" -jar AozoraEpub3.jar  

#### 変換
　表示されたアプレットに、変換したい青空文庫テキストファイル（拡張子txtまたはzip)  
　（複数可）をドラッグ＆ドロップします。(「ファイル選択」から開くでも同じ)  
　テキストファイルと同じ場所に「元ファイル名.epub」または「[著作者名] 表題.epub」のファイルが生成されます。  
　※テキストのない画像のみのzipを変換した場合は、画像のみのePubファイルを生成します。  


使い方 CUI
------------
####コマンドラインからの実行 (仮)
　Usage: java -cp AozoraEpub3.jar AozoraEpub3 [-options] input_files(txt,zip)

**オプション**  
- -h,--help  
　　show usage
- -i,--ini <arg>  
　　指定したiniファイルから設定を読み込みます (コマンドラインオプション以外)  
　　(指定がない場合はAozoraEpub3.ini ファイルがなければデフォルト値)  

- -enc <arg>  
　　入力ファイルエンコード  [MS932](default) [UTF-8]
- -t <arg>  
　　本文内の表題種別  [0:表題→著者名](default)[1:著者名→表題][2:表題→著者名(副題優先)][3:表題のみ][4:なし]
-  -c,--cover <arg>  
　　表紙画像  [0:先頭の挿絵][1:ファイル名と同じ画像][ファイル名 or URL]
- -tf  
　　入力ファイル名を表題に利用

- -d,--dst <arg>  
　　出力先パス  
- -ext <arg>  
　　出力ファイル拡張子  [.epub](default) [.kepub.epub]  
- -of <arg>  
　　出力ファイル名を入力ファイル名に合せる  

画面設定
------------
#### 表題
* 本文内  
  本文内のタイトルと著者名の有無を設定します。  
  3行連続の場合はタイトルの次は副題してタイトルと連結します。  
  本文中のタイトルは大きい文字で、著者名地付きに設定されます。  
  画像や空行は無視されます。  
* ファイル名優先  
  "[著作者名] 表題.epub" のファイル名からタイトルと著者名を取得します。  
  本文中のタイトル行と著者名の行のスタイル設定は本文内の選択に従います。  

#### 表紙
* 表紙  
  表紙の画像を[先頭の挿絵][入力ファイル名と同じ画像(png,jpg)][表紙無し]またはファイル、URLを指定します。  
  [入力ファイル名と同じ画像(png,jpg)]は、入力ファイル名と拡張子以外が同じ画像を表紙に利用します。  
  (拡張子は以下の順でチェックpng,jpg,jpeg,gif)  

#### ページ出力
* 表紙  
  ePubの先頭に表紙ページ（画像は幅100％）を追加します。  
  Reader等で表紙を出したい場合に指定してください。  
  （先頭の画像を表紙に指定している場合は先頭に移動されませす）  
* 表題左右中央  
  表題、著者等のページを左右中央の単一ページで出力します。  
* 目次  
  目次ページを出力する場合に選択します。  
  縦書きと横書きが選べます  

* 拡張子
  出力ファイルの拡張子を指定します。  
  (Koboでの利用はkepub.epubを選択推奨)  
* 出力ファイル名に表題利用  
  "[著作者名] 表題.epub" のファイル名で出力します。  
  どちらも設定されていない場合は「元ファイル名.epub」で出力します。  
* ePubファイル上書き
  同名のファイル(元ファイル名.epub)がすでにある場合でも上書きして出力します。  
  チェックを外すとエラーを表示して変換しません。  

#### 出力先
* 出力先  
  出力先を指定する場合にフルパスを設定します。  

#### 画像縮小
* 画像サイズ
  大きすぎる画像がある場合は指定ピクセルのサイズ以下になるように縮小します。  

#### 変換設定
* 栞用ID出力  
  Koboのkepubでの栞用のidを行のpタグに設定します。  
  Koboのkepub以外の環境では不要です。  
* 4バイト文字変換  
  チェックを外すと4バイト文字を〓に変換し、後ろに注記を小書きで表示します。  
  （Koboでは行内の4バイトの文字以降が表示されない問題があります）  
  Readerでは問題なく4バイトのJIS漢字は表示されます。  
  （ただし表示できない漢字は□で表示され小書きの注記は表示されない）  
* 縦書き 横書き  
  本文の縦書きと横書きを指定します。  

#### 変換
* 入力文字コード  
  入力する青空文庫ファイルの文字コードを指定します。通常はMS932(SJIS)です。  
* ファイル選択  
  ファイルを選択するとテキストエリアにドラッグアンドドロップするのと同様に変換されます  
* 変換前確認  
  変換前に、タイトルと著者名と表紙の確認と編集が可能なダイアログを表示します。  
  修正したタイトルと著者名でメタデータが作成されます。  
  本文側のタイトルやスタイルは変更されません。  
  表紙はトリミングした画像を出力し、元画像を残す指定も可能です。  

#### 画像設定
* 画面サイズ  
  画面の縦横比と、小さい画像を拡大しない場合の判別に利用します  
* 画像単ページ化  
  文中の画像の前後に改ページを入れて単ページ化する対象の画像サイズを設定します  
  単ページ化することで画像周辺の余白が小さくなります  
* 小さい画像を拡大表示  
  画面サイズより小さい画像は画面の縦か幅に合わせて拡大表示されます  
* 画像縮小  
  縦x横の画素数または横・横のピクセル数以下になるように画像を縮小します (縮小アルゴリズムはBicubic)  
  端末のサイズ制限がある場合に設定してください  
* 表紙画像  
  表紙画像はこのサイズ以下になるように縮小します  
* 縮小時のJpeg画質  
  縮小処理を行うときのJepgの圧縮パラメータです 100が最高画質です  

#### 詳細設定
* 文中全角スペースの処理  
  ？の後などに全角スペースがある場合に2行目以降で行頭に来て段落のように見えてしまうためスペースを非表示にします  
* 自動縦中横 
  2文字の半角の数字と２～３文字の!と?を縦に並べて表示します。  
　数字1桁3桁で縦にする半角数字の文字の長さを追加できます。  
　前後に全角の文字が無い場合や、横組み注記の中では無効になります  
* コメント出力  
  50文字以上の - の行で挟まれたコメントブロックの表示方法を指定します  
* 強制改ページ  
  有効にすると、指定バイト数で強制改ページを行います。  
  ePub内の各xhtmlファイルのサイズや行数が増えることで、Reader等で処理が重くなるのを防ぎます。  
  字下げ等のブロック注記等の中にある場合は改ページされません。  
  各行: 指定バイト数を超えた行で強制改ページします。  
  空行: 空行が指定行数続いたとき指定バイト数を超えていたら強制改ページします。  
  見出し前: 目次の見出しに該当する行の前で指定バイト数を超えていたら強制改ページします。  

#### 目次設定
* 目次出力  
  最大文字数: 目次の名称の最大文字数を設定します。長い文字が省略された場合は ... がつきます。  
  表紙: 表紙ページへの目次を出力します。 表紙画像が無い場合は出力されません。  
  次の行を繋げる: 章タイトルが次の行にある場合等で、見出しの次の行の文字を目次の名称に繋げます。 
  連続する見出しを除外: 目次ページ等で自動抽出された見出しを目次に入れません。  
* 目次抽出
　改ページ後: 改ページ後に最初の文字の行を目次に追加します。  
  注記: 選択した見出し注記内の文字を目次に追加します。ブロック注記の場合は次の行(繋げた場合の2行)のみ  
  章見出し: 章の名前(数字含む)を自動で抽出して目次に追加します。  
    （第～話/第～章/第～篇/第～部/第～節/第～幕/その～/～章/プロローグ/エピローグ/序/序章/終章/間章/幕間）  
  数字のみ: 数字のみの行を目次に追加します。  
  数字+見出し: 数字+空白等+見出し文字 の行を目次に追加します。  
  数字(括弧内): 括弧内の数字のみの行を目次に追加します。（）〔〕【】  
  数字(括弧内)+見出し: （数字）+空白等+見出し文字 の行を目次に追加します。  
  その他パターン: 目次抽出パターンを正規表現で指定します。前後の空白と注記タグを除いた文字列と比較します。  

ファイルの説明
------------
#### プログラムファイル  
* AozoraEpub3.jar
    ePub3変換ツール  
    ダブルクリックまたは"java -jar AozoraEpub3.jar"で実行  
* AozoraEpub3.ico
    ショートカットを作成時にこのアイコンを指定してください（jarなので設定できない）  
* lib/以下の *.jar ファイル
    利用ライブラリ (commons-cli, commons-compress, Velocity, JAI)  

#### ePub3テンプレート  
* template/*  
    ePub3テンプレート  
* template/OPS/css/*.css  
    ePub3スタイル  

#### 変換用設定ファイル  
* chuki_tag_suf.txt  
    前方参照型注記を開始終了型注記に変換  
* chuki_tag.txt  
    注記をePubタグに変換  
* chuki_alt.txt  
    外字注記を代替文字に変換  
* chuki_utf.txt  
    外字注記(コード無し)をUTF-8文字に変換  
* chuki_latin.txt  
    ラテン文字注記をUTF-8に変換  
* replace.txt  
    文字置換設定ファイル  


対応している注記
------------
#### 基本的な注記に設定ファイルで対応
 chuki_tag.txt 参照

*機種別の対応状況  
  横組みはKoboのみ対応  
  横書きはKoboとKindleのみ対応 (横書き内の字下げ等の注記はうまく出ないかも)  

#### 例外的にプログラム処理しているもの
- ページの左右中央  
-［＃注記付き］○［＃「△」の注記付き終わり］と［＃「○」に「△」のルビ］ → ｜○《△》に変換  
- ［＃「○」に×傍点］ → 文字と同数の×ルビに変換  
- 字下げ連続時の［＃ここで字下げ終わり］の省略  
- 字下げ折り返しと字下げ字詰めは数値化してインデント計算  
  ［＃ここから○字下げ、折り返して●字下げ］［＃ここから○字下げ、●字詰め］  
- 字下げ複合はclassを合成 (罫囲み、中央揃え)  
- 画像 (キャプション、サイズ指定は未対応)  
    ［＃説明（ファイル名.拡張子）］  
    &lt;img src="ファイル名"/&gt;  
- 底本： で改ページ (直前に改ページがない場合)  


対応している外字と特殊文字
------------
* 外字注記をコード変換してUTF-8文字で出力 （UTF8コード、JISコードあり）  
 ※［＃「さんずい＋垂」、unicode6DB6］  
 ※［＃「さんずい＋垂」、U+6DB6、235-7］  
 ※［＃「さんずい＋垂」、UCS6DB6、235-7］  
 ※［＃「てへん＋劣」、第3水準1-84-77］  
* UTF-8にない外字注記は代替文字を出力 (chuk_alt.txt)  
* コード記述がない外字注記は名称からUTF-8に変換 (chuk_utf.txt)  
* 4バイト文字 0x9000以上 は変換しない設定 (ただしJIS漢字も変換されない)  

* 青空文庫特殊文字（《》［］〔〕｜＃※）  
 ※［＃始め二重山括弧、1-1-52］ →《  
 ※［＃終わり二重山括弧、1-1-53］ →》  
 ※［＃始め角括弧、1-1-46］ →［  
 ※［＃終わり角括弧、1-1-47］ →］  
 ※［＃始めきっこう（亀甲）括弧、1-1-44］ →〔  
 ※［＃終わりきっこう（亀甲）括弧、1-1-45］ →〕  
 ※［＃縦線、1-1-35］ →｜  
 ※［＃井げた、1-1-84］ →＃  
 ※［＃米印、1-2-8］ →※  
 
* くの字点「／＼」「／″＼」をUTF-8で出力  


未対応注記
------------
- 窓見出し
- 割り注
- 訂正と「ママ」
- 左ルビ
- 画像キャプション
- 行内の地付き
- ２段組
- 重複不可に定義された注記の間に含まれるルビ・圏点・縦横中
- ルビ指定文字内の注記(圏点・縦横中・太字等)

更新予定と更新履歴
------------
README_Changes.txt 参照
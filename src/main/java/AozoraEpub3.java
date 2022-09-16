import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.image.ImageInfoReader;
import com.github.hmdev.info.BookInfo;
import com.github.hmdev.info.SectionInfo;
import com.github.hmdev.util.Detector;
import com.github.hmdev.writer.Epub3ImageWriter;
import com.github.hmdev.writer.Epub3Writer;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

/** コマンドライン実行用mainとePub3変換関数 */
public class AozoraEpub3
{
    public static final String VERSION = "1.1.1b12Q";

    static Logger logger = Logger.getLogger("com.github.hmdev");

    /** コマンドライン実行用 */
    public static void main(String[] args) {
        String jarPath = System.getProperty("java.class.path");
        int idx = jarPath.indexOf(";");
        if (idx > 0) jarPath = jarPath.substring(0, idx);
        if (!jarPath.endsWith(".jar")) jarPath = "";
        else jarPath = jarPath.substring(0, jarPath.lastIndexOf(File.separator)+1);
        //this.cachePath = new File(jarPath+".cache");
        //this.webConfigPath = new File(jarPath+"web");

        // ePub3出力クラス
        Epub3Writer epub3Writer;
        // ePub3画像出力クラス
        Epub3ImageWriter epub3ImageWriter;

        // 設定ファイル
        Preferences props;
        // 設定ファイル名
        String propFileName = "AozoraEpub3.ini";
        // 出力先パス
        File dstPath = null;

        String helpMsg = "AozoraEpub3 [-options] input_files(txt,zip,cbz)\nversion : "+VERSION;

        try {
            // コマンドライン オプション設定
            Options options = new Options();
            options.addOption("h", "help", false, "show usage");
            options.addOption("i", "ini", true, "指定したiniファイルから設定を読み込みます (コマンドラインオプション以外の設定)");
            options.addOption("t", true, "本文内の表題種別\n[0:表題→著者名] (default)\n[1:著者名→表題]\n[2:表題→著者名(副題優先)]\n[3:表題のみ]\n[4:なし]");
            options.addOption("tf", false, "入力ファイル名を表題に利用");
            options.addOption("c", "cover", true, "表紙画像\n[0:先頭の挿絵]\n[1:ファイル名と同じ画像]\n[ファイル名 or URL]");
            options.addOption("ext", true, "出力ファイル拡張子\n[.epub] (default)\n[.kepub.epub]");
            options.addOption("of", false, "出力ファイル名を入力ファイル名に合せる");
            options.addOption("d", "dst", true, "出力先パス");
            options.addOption("enc", true, "入力ファイルエンコード標準は自動認識\n[MS932]\n[UTF-8]");
            //options.addOption("id", false, "栞用ID出力 (for Kobo)");
            //options.addOption("tcy", false, "自動縦中横有効");
            //options.addOption("g4", false, "4バイト文字変換");
            //options.addOption("tm", false, "表題を左右中央");
            //options.addOption("cp", false, "表紙画像ページ追加");
            options.addOption("hor", false, "横書き (指定がなければ縦書き)");
            options.addOption("device", true, "端末種別(指定した端末向けの例外処理を行う)\n[kindle]");

            CommandLine commandLine;
            try {
                commandLine = new DefaultParser().parse(options, args, true);
            } catch (ParseException e) {
                new HelpFormatter().printHelp(helpMsg, options);
                return;
            }
            // オプションの後ろをファイル名に設定
            String[] fileNames = commandLine.getArgs();
            if (fileNames.length == 0) {
                new HelpFormatter().printHelp(helpMsg, options);
                return;
            }

            // ヘルプ出力
            if (commandLine.hasOption('h') ) {
                new HelpFormatter().printHelp(helpMsg, options);
                return;
            }
            // iniファイル確認
            if (commandLine.hasOption("i")) {
                propFileName = commandLine.getOptionValue("i");
                if (!Preferences.userNodeForPackage(AozoraEpub3.class).nodeExists(propFileName)) {
                    logger.severe("-i : preferences not exists. "+propFileName);
                    return;
                }
            }
            // 出力パス確認
            if (commandLine.hasOption("d")) {
                dstPath = new File(commandLine.getOptionValue("d"));
                if (!dstPath.isDirectory()) {
                    logger.severe("-d : dst path not exist. "+dstPath.getAbsolutePath());
                    return;
                }
            }
            // ePub出力クラス初期化
            epub3Writer = new Epub3Writer("/template/");
            epub3ImageWriter = new Epub3ImageWriter("/template/");

            // propsから読み込み
            props = Preferences.userNodeForPackage(AozoraEpub3.class).node(propFileName);

            int titleIndex = 0; // try { titleIndex = Integer.parseInt(props.getProperty("TitleType")); } catch (Exception e) {}//表題

            // コマンドラインオプション以外
            boolean coverPage = props.getBoolean("CoverPage", true); // 表紙追加
            int titlePage = BookInfo.TITLE_NONE;
            if (props.getBoolean("TitlePageWrite", true)) {
                titlePage =props.getInt("TitlePage", 0);
            }
            boolean withMarkId = props.getBoolean("MarkId", false);
            //boolean gaiji32 = props.getBoolean("Gaiji32"));
            boolean commentPrint = props.getBoolean("CommentPrint", false);
            boolean commentConvert = props.getBoolean("CommentConvert", false);
            boolean autoYoko = props.getBoolean("AutoYoko", false);
            boolean autoYokoNum1 = props.getBoolean("AutoYokoNum1", false);
            boolean autoYokoNum3 = props.getBoolean("AutoYokoNum3", false);
            boolean autoYokoEQ1 = props.getBoolean("AutoYokoEQ1", false);
            int spaceHyp = props.getInt("SpaceHyphenation", 0);
            boolean tocPage = props.getBoolean("TocPage", false); // 目次追加
            boolean tocVertical = props.getBoolean("TocVertical", false); // 目次縦書き
            boolean coverPageToc = props.getBoolean("CoverPageToc", false);
            int removeEmptyLine = props.getInt("RemoveEmptyLine", 0);
            int maxEmptyLine = props.getInt("MaxEmptyLine", 0);

            // 画面サイズと画像リサイズ
            int dispW = props.getInt("DispW", 600);
            int dispH = props.getInt("DispH", 800);
            int coverW = props.getInt("CoverW", 600);
            int coverH = props.getInt("CoverH", 800);
            int resizeW = 0; if (props.getBoolean("ResizeW", false)) resizeW = props.getInt("ResizeNumW", 0);
            int resizeH = 0; if (props.getBoolean("ResizeH", false)) resizeH = props.getInt("ResizeNumH", 0);
            int singlePageSizeW = props.getInt("SinglePageSizeW", 480);
            int singlePageSizeH = props.getInt("SinglePageSizeH", 640);
            int singlePageWidth = props.getInt("SinglePageWidth", 600);
            float imageScale = props.getFloat("ImageScale", 1);
            int imageFloatType = props.getInt("ImageFloatType", 0);
            int imageFloatW = props.getInt("ImageFloatW", 0);
            int imageFloatH = props.getInt("ImageFloatH", 0);
            int imageSizeType = props.getInt("ImageSizeType", SectionInfo.IMAGE_SIZE_TYPE_HEIGHT);
            boolean fitImage = props.getBoolean("FitImage", false);
            boolean svgImage = props.getBoolean("SvgImage", false);
            int rotateImage = 0; if (1 == props.getInt("RotateImage", 0)) rotateImage = 90; else if (2 == props.getInt("RotateImage", 0)) rotateImage = -90;
            float jpegQualty = props.getFloat("JpegQuality", 80)/100f;
            float gamma = 1.0f; if (props.getBoolean("Gamma", false)) gamma = props.getFloat("GammaValue", 0);
            int autoMarginLimitH = 0;
            int autoMarginLimitV = 0;
            int autoMarginWhiteLevel = 80;
            float autoMarginPadding = 0;
            int autoMarginNombre = 0;
            float nobreSize = 0.03f;
            if (props.getBoolean("AutoMargin", false)) {
                autoMarginLimitH = props.getInt("AutoMarginLimitH", 0);
                autoMarginLimitV = props.getInt("AutoMarginLimitV", 0);
                autoMarginWhiteLevel = props.getInt("AutoMarginWhiteLevel", 0);
                autoMarginPadding = props.getFloat("AutoMarginPadding", 0);
                autoMarginNombre = props.getInt("AutoMarginNombre", 0);
                autoMarginPadding = props.getFloat("AutoMarginNombreSize", 0);
             }
            epub3Writer.setImageParam(dispW, dispH, coverW, coverH, resizeW, resizeH, singlePageSizeW, singlePageSizeH, singlePageWidth, imageSizeType, fitImage, svgImage, rotateImage,
                    imageScale, imageFloatType, imageFloatW, imageFloatH, jpegQualty, gamma, autoMarginLimitH, autoMarginLimitV, autoMarginWhiteLevel, autoMarginPadding, autoMarginNombre, nobreSize);
            epub3ImageWriter.setImageParam(dispW, dispH, coverW, coverH, resizeW, resizeH, singlePageSizeW, singlePageSizeH, singlePageWidth, imageSizeType, fitImage, svgImage, rotateImage,
                    imageScale, imageFloatType, imageFloatW, imageFloatH, jpegQualty, gamma, autoMarginLimitH, autoMarginLimitV, autoMarginWhiteLevel, autoMarginPadding, autoMarginNombre, nobreSize);
            // 目次階層化設定
            epub3Writer.setTocParam(props.getBoolean("NavNest", false), props.getBoolean("NcxNest", false));

            // スタイル設定
            String[] pageMargin = {};
            try { pageMargin = props.get("PageMargin", "").split(","); } catch (Exception ignored) {}
            if (pageMargin.length != 4) pageMargin = new String[]{"0", "0", "0", "0"};
            else {
                String pageMarginUnit = props.get("PageMarginUnit", "");
                for (int i=0; i<4; i++) { pageMargin[i] += pageMarginUnit; }
            }
            String[] bodyMargin = {};
            try { bodyMargin = props.get("BodyMargin", "").split(","); } catch (Exception ignored) {}
            if (bodyMargin.length != 4) bodyMargin = new String[]{"0", "0", "0", "0"};
            else {
                String bodyMarginUnit = props.get("BodyMarginUnit", "");
                for (int i=0; i<4; i++) { bodyMargin[i] += bodyMarginUnit; }
            }
            float lineHeight = props.getFloat("LineHeight", 1.8f);
            int fontSize = props.getInt("FontSize", 100);
            boolean boldUseGothic = props.getBoolean("BoldUseGothic", false);
            boolean gothicUseBold = props.getBoolean("gothicUseBold", false);
            epub3Writer.setStyles(pageMargin, bodyMargin, lineHeight, fontSize, boldUseGothic, gothicUseBold);

            // 自動改ページ
            int forcePageBreakSize = 0;
            int forcePageBreakEmpty = 0;
            int forcePageBreakEmptySize = 0;
            int forcePageBreakChapter = 0;
            int forcePageBreakChapterSize = 0;
            if (props.getBoolean("PageBreak", false)) {
                try {
                    forcePageBreakSize = props.getInt("PageBreakSize", 0) * 1024;
                    if (props.getBoolean("PageBreakEmpty", false)) {
                        forcePageBreakEmpty = props.getInt("PageBreakEmptyLine", 0);
                        forcePageBreakEmptySize = props.getInt("PageBreakEmptySize", 0) * 1024;
                    } if (props.getBoolean("PageBreakChapter", false)) {
                        forcePageBreakChapter = 1;
                        forcePageBreakChapterSize = props.getInt("PageBreakChapterSize", 0) * 1024;
                    }
                } catch (Exception e) { logger.info(e.toString()); }
            }
            int maxLength = props.getInt("ChapterNameLength", 64);
            boolean insertTitleToc = props.getBoolean("TitleToc", false);
            boolean chapterExclude = props.getBoolean("ChapterExclude", false);
            boolean chapterUseNextLine = props.getBoolean("ChapterUseNextLine", false);
            boolean chapterSection = props.getBoolean("ChapterSection", false);
            boolean chapterH = props.getBoolean("ChapterH", false);
            boolean chapterH1 = props.getBoolean("ChapterH1", false);
            boolean chapterH2 = props.getBoolean("ChapterH2", false);
            boolean chapterH3 = props.getBoolean("ChapterH3", false);
            boolean sameLineChapter = props.getBoolean("SameLineChapter", false);
            boolean chapterName = props.getBoolean("ChapterName", false);
            boolean chapterNumOnly = props.getBoolean("ChapterNumOnly", false);
            boolean chapterNumTitle = props.getBoolean("ChapterNumTitle", false);
            boolean chapterNumParen = props.getBoolean("ChapterNumParen", false);
            boolean chapterNumParenTitle = props.getBoolean("hapterNumParenTitle", false);
            String chapterPattern = ""; if (props.getBoolean("ChapterPattern", false)) chapterPattern = props.get("ChapterPatternText", "");

            // オプション指定を反映
            boolean useFileName = false; // 表題に入力ファイル名利用
            String coverFileName = null;
            String encType = "AUTO"; // 文字コードの初期設定を空に
            String outExt = ".epub";
            boolean autoFileName = true; // ファイル名を表題に利用
            boolean vertical = true;
            String targetDevice = null;
            if(commandLine.hasOption("t")) try { titleIndex = Integer.parseInt(commandLine.getOptionValue("t")); } catch (Exception e) {}//表題
            if(commandLine.hasOption("tf")) useFileName = true;
            if(commandLine.hasOption("c")) coverFileName = commandLine.getOptionValue("c");
            if(commandLine.hasOption("enc")) encType = commandLine.getOptionValue("enc");
            if(commandLine.hasOption("ext")) outExt = commandLine.getOptionValue("ext");
            if(commandLine.hasOption("of")) autoFileName = false;
            //if(commandLine.hasOption("id")) withMarkId = true;
            //if(commandLine.hasOption("tcy")) autoYoko = true;
            //if(commandLine.hasOption("g4")) gaiji32 = true;
            //if(commandLine.hasOption("tm")) middleTitle = true;
            //if(commandLine.hasOption("cb")) commentPrint = true;
            //if(commandLine.hasOption("cc")) commentConvert = true;
            //if(commandLine.hasOption("cp")) coverPage = true;
            if(commandLine.hasOption("hor")) vertical = false;
            if(commandLine.hasOption("device")) {
                targetDevice = commandLine.getOptionValue("device");
                if (targetDevice.equalsIgnoreCase("kindle")) {
                    epub3Writer.setIsKindle(true);
                }
            }

            // 変換クラス生成とパラメータ設定
            AozoraEpub3Converter  aozoraConverter = new AozoraEpub3Converter(epub3Writer, jarPath);
            // 挿絵なし
            aozoraConverter.setNoIllust(props.getBoolean("NoIllust", false));
            // 栞用span出力
            aozoraConverter.setWithMarkId(withMarkId);
            // 変換オプション設定
            aozoraConverter.setAutoYoko(autoYoko, autoYokoNum1, autoYokoNum3, autoYokoEQ1);
            // 文字出力設定
            int dakutenType = props.getInt("DakutenType", 0);
            boolean printIvsBMP = props.getBoolean("IvsBMP", false);
            boolean printIvsSSP = props.getBoolean("IvsSSP", false);

            aozoraConverter.setCharOutput(dakutenType, printIvsBMP, printIvsSSP);
            // 全角スペースの禁則
            aozoraConverter.setSpaceHyphenation(spaceHyp);
            // コメント
            aozoraConverter.setCommentPrint(commentPrint, commentConvert);

            aozoraConverter.setRemoveEmptyLine(removeEmptyLine, maxEmptyLine);

            // 強制改ページ
            aozoraConverter.setForcePageBreak(forcePageBreakSize, forcePageBreakEmpty, forcePageBreakEmptySize, forcePageBreakChapter, forcePageBreakChapterSize);
            // 目次設定
            aozoraConverter.setChapterLevel(maxLength, chapterExclude, chapterUseNextLine, chapterSection,
                    chapterH, chapterH1, chapterH2, chapterH3, sameLineChapter,
                    chapterName,
                    chapterNumOnly, chapterNumTitle, chapterNumParen, chapterNumParenTitle,
                    chapterPattern);

            ////////////////////////////////
            // 各ファイルを変換処理
            ////////////////////////////////
            for (String fileName : fileNames) {
                logger.info("-------- " + fileName);
                File srcFile = new File(fileName);
                if (!srcFile.isFile()) {
                    logger.severe("file not exist. "+srcFile.getAbsolutePath());
                    continue;
                }
                String ext = srcFile.getName();
                ext = ext.substring(ext.lastIndexOf('.')+1).toLowerCase();

                int coverImageIndex = -1;
                if (coverFileName != null) {
                    if ("0".equals(coverFileName)) {
                        coverImageIndex = 0;
                        coverFileName = "";
                    } else if ("1".equals(coverFileName)) {
                        coverFileName = AozoraEpub3.getSameCoverFileName(srcFile); // 入力ファイルと同じ名前+.jpg/.png
                    }
                }

                // zipならzip内のテキストを検索
                int txtCount = 1;
                boolean imageOnly = false;
                boolean isFile = "txt".equals(ext);
                if ("zip".equals(ext) || "txtz".equals(ext)) {
                    try {
                        txtCount = AozoraEpub3.countZipText(srcFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (txtCount == 0) { txtCount = 1; imageOnly = true; }
                } else if("rar".equals(ext)) {
                    try {
                        txtCount = AozoraEpub3.countRarText(srcFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (txtCount == 0) { txtCount = 1; imageOnly = true; }
                } else if ("cbz".equals(ext)) {
                    imageOnly = true;
                }
                for (int txtIdx=0; txtIdx<txtCount; txtIdx++) {
                    ImageInfoReader imageInfoReader = new ImageInfoReader(isFile, srcFile);

                    BookInfo bookInfo = null;
                    // 文字コード判別
                    String encauto;

                    encauto=AozoraEpub3.getTextCharset(srcFile, ext, imageInfoReader, txtIdx);
                    if ("SHIFT_JIS".equals(encauto))encauto="MS932";
                    if ("AUTO".equals(encType)) encType =encauto;
                    if (!imageOnly) {
                        bookInfo = AozoraEpub3.getBookInfo(srcFile, ext, txtIdx, imageInfoReader, aozoraConverter, encType, BookInfo.TitleType.indexOf(titleIndex), false);
                        bookInfo.vertical = vertical;
                        bookInfo.insertTocPage = tocPage;
                        bookInfo.setTocVertical(tocVertical);
                        bookInfo.insertTitleToc = insertTitleToc;
                        aozoraConverter.vertical = vertical;
                        // 表題ページ
                        bookInfo.titlePageType = titlePage;
                    }
                    // 表題の見出しが非表示で行が追加されていたら削除
                    if (!bookInfo.insertTitleToc && bookInfo.titleLine >= 0) {
                        bookInfo.removeChapterLineInfo(bookInfo.titleLine);
                    }

                    Epub3Writer writer = epub3Writer;
                    if (!isFile) {
                        if ("rar".equals(ext)) {
                            imageInfoReader.loadRarImageInfos(srcFile, imageOnly);
                        } else {
                            imageInfoReader.loadZipImageInfos(srcFile, imageOnly);
                        }
                        if (imageOnly) {
                            logger.info("画像のみのePubファイルを生成します");
                            // 画像出力用のBookInfo生成
                            bookInfo = new BookInfo(srcFile);
                            bookInfo.imageOnly = true;
                            // Writerを画像出力用派生クラスに入れ替え
                            writer = epub3ImageWriter;

                            if (imageInfoReader.countImageFileInfos() == 0) {
                                logger.severe("画像がありませんでした");
                                return;
                            }
                            // 名前順で並び替え
                            imageInfoReader.sortImageFileNames();
                        }
                    }
                    // 先頭からの場合で指定行数以降なら表紙無し
                    if (coverFileName == null || coverFileName.isEmpty()) {
                        try {
                            int maxCoverLine = props.getInt("MaxCoverLine", 0);
                            if (maxCoverLine > 0 && bookInfo.firstImageLineNum >= maxCoverLine) {
                                coverImageIndex = -1;
                                coverFileName = null;
                            }
                        } catch (Exception ignored) {}
                    }

                    // 表紙設定
                    bookInfo.insertCoverPageToc = coverPageToc;
                    bookInfo.insertCoverPage = coverPage;
                    bookInfo.coverImageIndex = coverImageIndex;
                    if (coverFileName != null && !coverFileName.startsWith("http")) {
                        File coverFile = new File(coverFileName);
                        if (!coverFile.exists()) {
                            coverFileName = srcFile.getParent()+"/"+coverFileName;
                            if (!new File(coverFileName).exists()) {
                                coverFileName = null;
                                logger.info("[WARN] 表紙画像ファイルが見つかりません : "+coverFile.getAbsolutePath());
                            }
                        }
                    }
                    bookInfo.coverFileName = coverFileName;

                    String[] titleCreator = BookInfo.getFileTitleCreator(srcFile.getName());
                    if (useFileName) {
                        if (titleCreator[0] != null && titleCreator[0].trim().length() >0) bookInfo.title = titleCreator[0];
                        if (titleCreator[1] != null && titleCreator[1].trim().length() >0) bookInfo.creator = titleCreator[1];
                    } else {
                        // テキストから取得できていない場合
                        if (bookInfo.title == null || bookInfo.title.length() == 0) bookInfo.title = titleCreator[0]==null?"":titleCreator[0];
                        if (bookInfo.creator == null || bookInfo.creator.length() == 0) bookInfo.creator = titleCreator[1]==null?"":titleCreator[1];
                    }

                    File outFile = getOutFile(srcFile, dstPath, bookInfo, autoFileName, outExt);
                    AozoraEpub3.convertFile(
                            srcFile, ext, outFile,
                            aozoraConverter, writer,
                            encType, bookInfo, imageInfoReader, txtIdx);
                }
            }
Thread.getAllStackTraces().keySet().forEach(System.err::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 出力ファイルを生成 */
    static File getOutFile(File srcFile, File dstPath, BookInfo bookInfo, boolean autoFileName, String outExt)
    {
        // 出力ファイル
        if (dstPath == null) dstPath = srcFile.getAbsoluteFile().getParentFile();
        String outFileName = "";
        if (autoFileName && (bookInfo.creator != null || bookInfo.title != null)) {
            outFileName = dstPath.getAbsolutePath()+"/";
            if (bookInfo.creator != null && bookInfo.creator.length() > 0) {
                String str = bookInfo.creator.replaceAll("[\\\\|\\/|\\:|\\*|\\?|\\<|\\>|\\||\\\"|\t]", "");
                if (str.length() > 64) str = str.substring(0, 64);
                outFileName += "["+str+"] ";
            }
            if (bookInfo.title != null) {
                outFileName += bookInfo.title.replaceAll("[\\\\|\\/|\\:|\\*|\\!|\\?|\\<|\\>|\\||\\\"|\t]", "");
            }
            if (outFileName.length() > 250) outFileName = outFileName.substring(0, 250);
        } else {
            outFileName = dstPath.getAbsolutePath()+"/"+srcFile.getName().replaceFirst("\\.[^\\.]+$", "");
        }
        if (outExt.length() == 0) outExt = ".epub";
        File outFile = new File(outFileName + outExt);
        // 書き込み許可設定
        outFile.setWritable(true);

        return outFile;
    }

    /** 前処理で一度読み込んでタイトル等の情報を取得 */
    static public BookInfo getBookInfo(File srcFile, String ext, int txtIdx, ImageInfoReader imageInfoReader, AozoraEpub3Converter aozoraConverter,
            String encType, BookInfo.TitleType titleType, boolean pubFirst)
    {
        try {
            String[] textEntryName = new String[1];
            InputStream is = AozoraEpub3.getTextInputStream(srcFile, ext, imageInfoReader, textEntryName, txtIdx);
            if (is == null) return null;

            // タイトル、画像注記、左右中央注記、目次取得
            BufferedReader src = new BufferedReader(new InputStreamReader(is, (String)encType));
            BookInfo bookInfo = aozoraConverter.getBookInfo(srcFile, src, imageInfoReader, titleType, pubFirst);
            is.close();
            bookInfo.textEntryName = textEntryName[0];
            return bookInfo;

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("エラーが発生しました : " + e.getMessage());
        }
        return null;
    }

    /** ファイルを変換
     * @param srcFile 変換するファイル
     * @param outFile 出力先パス */
    static public void convertFile(File srcFile, String ext, File outFile, AozoraEpub3Converter aozoraConverter, Epub3Writer epubWriter,
            String encType, BookInfo bookInfo, ImageInfoReader imageInfoReader, int txtIdx)
    {
        try {
            long time = System.currentTimeMillis();
            logger.info("変換開始 : " + srcFile.getPath());

            // 入力Stream再オープン
            BufferedReader src = null;
            if (!bookInfo.imageOnly) {
                src = new BufferedReader(new InputStreamReader(getTextInputStream(srcFile, ext, null, null, txtIdx), encType));
            }

            // ePub書き出し srcは中でクローズされる
            epubWriter.write(aozoraConverter, src, srcFile, ext, outFile, bookInfo, imageInfoReader);

            logger.info("変換完了["+(((System.currentTimeMillis()-time)/100)/10f)+"s] : " + outFile.getPath());

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("エラーが発生しました : " + e.getMessage());
            // logger.printStaclTrace(e);
        }
    }

    /** 入力ファイルからStreamオープン
     *
     * @param srcFile
     * @param ext
     * @param imageInfoReader
     * @param txtIdx テキストファイルのZip内の位置
     * @return テキストファイルのストリーム (close()は呼び出し側ですること)
     * @throws RarException
     */
    static public InputStream getTextInputStream(File srcFile, String ext, ImageInfoReader imageInfoReader, String[] textEntryName, int txtIdx) throws IOException, RarException
    {
        if ("txt".equals(ext)) {
            return Files.newInputStream(srcFile.toPath());
        } else if ("zip".equals(ext) || "txtz".equals(ext)) {
            // Zipなら最初のtxt
            ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(Files.newInputStream(srcFile.toPath()), 65536), "MS932", false);
            ArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.substring(entryName.lastIndexOf('.')+1).equalsIgnoreCase("txt") && txtIdx-- == 0) {
                    if (imageInfoReader != null) imageInfoReader.setArchiveTextEntry(entryName);
                    if (textEntryName != null) textEntryName[0] = entryName;
                    return zis;
                }
            }
            logger.info("zip内にtxtファイルがありません: " + srcFile.getName());
            return null;
        } else if ("rar".equals(ext)) {
            // tempのtxtファイル作成
            try (Archive archive = new Archive(srcFile)) {
                FileHeader fileHeader = archive.nextFileHeader();
                while (fileHeader != null) {
                    if (!fileHeader.isDirectory()) {
                        String entryName = fileHeader.getFileName();
                        entryName = entryName.replace('\\', '/');
                        if (entryName.substring(entryName.lastIndexOf('.') + 1).equalsIgnoreCase("txt") && txtIdx-- == 0) {
                            if (imageInfoReader != null) imageInfoReader.setArchiveTextEntry(entryName);
                            if (textEntryName != null) textEntryName[0] = entryName;
                            // tmpファイルにコピーして終了時に削除
                            File tmpFile = File.createTempFile("rarTmp", "txt");
                            tmpFile.deleteOnExit();
                            try (FileOutputStream fos = new FileOutputStream(tmpFile); InputStream is = archive.getInputStream(fileHeader)) {
                                IOUtils.copy(is, fos);
                            }
                            return new BufferedInputStream(Files.newInputStream(tmpFile.toPath()), 65536);
                        }
                    }
                    fileHeader = archive.nextFileHeader();
                }
            }
            logger.info("rar内にtxtファイルがありません: " + srcFile.getName());
            return null;
        } else {
            logger.info("txt, zip, rar, txtz, cbz のみ変換可能です: " + srcFile.getPath());
        }
        return null;
    }

    /** 入力ファイルから文字コードを判別
     *
     * @param srcFile
     * @param ext
     * @param imageInfoReader
     * @param txtIdx テキストファイルのZip内の位置
     * @return テキストファイルのストリーム (close()は呼び出し側ですること)
     * @throws RarException
     */
    static public String getTextCharset(File srcFile, String ext, ImageInfoReader imageInfoReader, int txtIdx) throws IOException, RarException
    {    String cs ="";
        if ("txt".equals(ext)) {
            InputStream is = Files.newInputStream(srcFile.toPath());
            cs = Detector.getCharset(is);
            return cs;
        } else if ("zip".equals(ext) || "txtz".equals(ext)) {
            //Zipなら最初のtxt
            ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(Files.newInputStream(srcFile.toPath()), 65536), "MS932", false);
            ArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.substring(entryName.lastIndexOf('.')+1).equalsIgnoreCase("txt") && txtIdx-- == 0) {
                    if (imageInfoReader != null) imageInfoReader.setArchiveTextEntry(entryName);
                //    if (textEntryName != null) textEntryName[0] = entryName;
                    cs = Detector.getCharset(zis);
                    return cs;
                }
            }
            logger.info("zip内にtxtファイルがありません: " + srcFile.getName());
            return null;
        } else if ("rar".equals(ext)) {
            //tempのtxtファイル作成
            try (Archive archive = new Archive(srcFile)) {
                FileHeader fileHeader = archive.nextFileHeader();
                while (fileHeader != null) {
                    if (!fileHeader.isDirectory()) {
                        String entryName = fileHeader.getFileName();
                        entryName = entryName.replace('\\', '/');
                        if (entryName.substring(entryName.lastIndexOf('.') + 1).equalsIgnoreCase("txt") && txtIdx-- == 0) {
                            if (imageInfoReader != null) imageInfoReader.setArchiveTextEntry(entryName);
                            //        if (textEntryName != null) textEntryName[0] = entryName;
                            //tmpファイルにコピーして終了時に削除
                            File tmpFile = File.createTempFile("rarTmp", "txt");
                            tmpFile.deleteOnExit();
                            try (FileOutputStream fos = new FileOutputStream(tmpFile); InputStream is = archive.getInputStream(fileHeader)) {
                                IOUtils.copy(is, fos);
                            }
                            InputStream bis = new BufferedInputStream(new FileInputStream(tmpFile), 65536);
                            cs = Detector.getCharset(bis);
                            return cs;
                        }
                    }
                    fileHeader = archive.nextFileHeader();
                }
            }
            logger.info("rar内にtxtファイルがありません: " + srcFile.getName());
            return null;
        } else {
            logger.info("txt, zip, rar, txtz, cbz のみ変換可能です: " + srcFile.getPath());
        }
        return null;
    }
    /** Zipファイル内のテキストファイルの数を取得 */
    static public int countZipText(File zipFile) throws IOException
    {
        int txtCount = 0;
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(Files.newInputStream(zipFile.toPath()), 65536), "MS932", false)) {
            ArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.substring(entryName.lastIndexOf('.') + 1).equalsIgnoreCase("txt")) txtCount++;
            }
        }
        return txtCount;
    }

    /** Ripファイル内のテキストファイルの数を取得 */
    static public int countRarText(File rarFile) throws IOException, RarException
    {
        int txtCount = 0;
        try (Archive archive = new Archive(rarFile)) {
            for (FileHeader fileHeader : archive.getFileHeaders()) {
                if (!fileHeader.isDirectory()) {
                    String entryName = fileHeader.getFileName();
                    entryName = entryName.replace('\\', '/');
                    if (entryName.substring(entryName.lastIndexOf('.') + 1).equalsIgnoreCase("txt")) txtCount++;
                }
            }
        }
        return txtCount;
    }

    static final String[] extOrder = {"png","jpg","jpeg","PNG","JPG","JPEG","Png","Jpg","Jpeg"};

    /** 入力ファイルと同じ名前の画像を取得
     * png, jpg, jpegの順で探す  */
    static public String getSameCoverFileName(File srcFile)
    {
        String baseFileName = srcFile.getPath();
        baseFileName = baseFileName.substring(0, baseFileName.lastIndexOf('.')+1);
        for (String ext : extOrder) {
            String coverFileName = baseFileName+ext;
            if (new File(coverFileName).exists()) return coverFileName;
        }
        return null;
    }
}

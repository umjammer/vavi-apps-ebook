/*
 *  Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 *  Programmed by Naohide Sano
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.condition.EnabledIf;
import vavi.text.epub.Epub3;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * EpubManipulator.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/28 umjammer initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
public class EpubManipulator {

    @Property(name = "epub.file")
    String epub;
    @Property(name = "epub.dir")
    String epubDir;

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    Epub3 epub3;

    /**
     * @param args 0: epub
     */
    public static void main(String[] args) throws Exception {
//        String epub = args[0];

        EpubManipulator app = new EpubManipulator();
        PropsEntity.Util.bind(app);

//        Path p = Path.of(app.epub);
        Files.list(Path.of(app.epubDir))
                .filter(p -> p.getFileName().toString().contains("[Unknown]"))
                .filter(p -> p.getFileName().toString().endsWith(".epub"))
                .forEach(p -> {
            try {
                app.epub3 = new Epub3(p);
                app.epub3.manipulate(app::modify2);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * @see "https://www.ipentec.com/document/internet-get-amazon-product-image"
     */
    static String amazon(String asin) {
        int countryCode = 9;
        String imageType = "LZZZZZZZ";
        return String.format("http://images-jp.amazon.com/images/P/%s.%02d.%s.jpg", asin, countryCode, imageType);
    }

    /** get a cover image from amazon and set */
    void modifyImage(Epub3.Content content) throws XPathExpressionException, IOException {
        String imageUtl = amazon(content.asin);
Debug.println("imageUtl: " + imageUtl);
        Path imageDir = epub3.getPath("vavi");
        Path imagePath = imageDir.resolve(content.asin + ".jpg");
        content.addCoverImage(imageUtl, imagePath);
        epub3.setDirty(true);
    }

    /** regex for author and title: "[author] title" */
    static final Pattern pattern1 = Pattern.compile("\\[(.+)\\]\s*(.+)");

    /** modification sample 1 */
    void modify1(Epub3.Content content) {
        try {
            if (content.author.equals("Yuri Yuru")) {
Debug.println("HIT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Matcher matcher = pattern1.matcher(content.title);
                if (matcher.matches()) {
Debug.println("corrected author: " + matcher.group(1));
                    content.author = matcher.group(1);
Debug.println("corrected title: " + matcher.group(2));
                    content.title = matcher.group(2);
                    content.updateMetadata();
                    epub3.setDirty(true);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** modification sample 2 */
    void modify2(Epub3.Content content) {
        try {
            if (content.author.equals("Unknown")) {
Debug.println("HIT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                content.author = "大島里美×宮村優子";
                content.updateMetadata();
                epub3.setDirty(true);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

/* */

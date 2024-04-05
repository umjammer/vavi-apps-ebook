/*
 *  Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 *  Programmed by Naohide Sano
 */

package vavi.text.epub;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import vavi.util.Debug;


/**
 * Epub3.
 * <p>
 * for read/write
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-11-25 nsano initial version <br>
 */
public class Epub3 implements Closeable {

    /** */
    private final DocumentBuilder db;
    /** */
    private final XPath xpath;
    /** */
    private final Transformer transformer;

    {
        try {
            System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI,
                    "net.sf.saxon.xpath.XPathFactoryImpl");
            xpath = XPathFactory.newInstance().newXPath();

//            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "net.sf.saxon.dom.DocumentBuilderFactoryImpl");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            db = dbf.newDocumentBuilder();

            TransformerFactory tf = TransformerFactory.newInstance();
            transformer = tf.newTransformer();
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** epub zip filesystem */
    private final FileSystem epubFs;

    /** */
    public Path getPath(String path) {
        return epubFs.getPath(path);
    }

    /** entry point */
    public Epub3(Path epub) throws IOException {
        URI uri = URI.create("jar:" + epub.toUri());
Debug.println(Level.FINER, "uri: " + uri);

        epubFs = FileSystems.newFileSystem(uri, Collections.emptyMap());
Debug.println(Level.FINER, "fs: " + epubFs.getClass().getName());
    }

    /** modified or not */
    private boolean dirty;

    /** if set true, modification will be stored. */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /** */
    public Content getContent() throws IOException {
        return new Container().getContent();
    }

    /** content.xml will be updated when {@link #dirty} is true */
    public void manipulate(Consumer<Content> task) throws IOException {
        Content content = getContent();

        task.accept(content);

        if (dirty) {
            content.write();
Debug.println(Level.FINE, "\ncontent written: " + Files.size(content.content));
        }
    }

    @Override
    public void close() throws IOException {
        epubFs.close();
    }

    /** container.xml */
    private class Container {
        Content content;
        public Container() throws IOException {
            try {
                Path container = epubFs.getPath("META-INF/container.xml");
//Debug.println("---- " + container + ": " + Files.exists(container) + " ----");
//try (Scanner s = new Scanner(Files.newInputStream(container))) {
// while (s.hasNextLine()) {
//  System.out.println(s.nextLine());
// }
//}
                InputSource is = new InputSource(Files.newInputStream(container));
//XPathDebugger.getEntryList(is).forEach(System.err::println);

                @SuppressWarnings({ "unchecked", "rawtypes" })
                List<NodeInfo> elements = (List) xpath.evaluate("//*[local-name() = 'rootfile']", is, XPathConstants.NODESET);
                Element element = (Element) NodeOverNodeInfo.wrap(elements.get(0));
Debug.println(Level.FINER, "element: " + element.getTagName());
                String fullPath = element.getAttribute("full-path");
Debug.println(Level.FINER, "full-path: " + fullPath);
                content = new Content(fullPath);
            } catch (XPathExpressionException e) {
                throw new IOException(e);
            }
        }
        public Content getContent() {
            return content;
        }
    }

    /** represents content.xml */
    public class Content {
        Path content;
        Document document;
        public String author;
        public String title;
        public String asin;
        /** path specified in the container */
        public Content(String fullPath) throws IOException {
            try {
                content = epubFs.getPath(fullPath);
if (Debug.isLoggable(Level.FINER)) {
 Debug.println(Level.FINER, "---- " + fullPath + " ----");
 try (Scanner s = new Scanner(Files.newInputStream(content))) {
  while (s.hasNextLine()) {
   System.out.println(s.nextLine());
  }
 }
}
                document = db.parse(new InputSource(Files.newInputStream(content)));

                //
                DOMNodeList elements = (DOMNodeList) xpath.evaluate("//*[local-name() = 'identifier'][@*[local-name() = 'scheme']]", document, XPathConstants.NODESET);
                Element element = (Element) elements.item(0);
                if (element == null) {
Debug.println(Level.WARNING, "no image");
                } else {
Debug.println(Level.FINER, "element: " + element.getTagName());
                    asin = element.getTextContent();
Debug.println(Level.FINER, "asin: " + asin);
                }

                author = (String) xpath.evaluate("//*[local-name() = 'creator']/text()", document, XPathConstants.STRING);
Debug.println(Level.FINER, "author: " + author);
                title = (String) xpath.evaluate("//*[local-name() = 'title']/text()", document, XPathConstants.STRING);
Debug.println(Level.FINER, "title: " + title);
            } catch (XPathExpressionException | SAXException e) {
                throw new IOException(e);
            }
        }

        /** write content.xml to epub */
        public void write() throws IOException {
            try {
                DOMSource source = new DOMSource(document);
                OutputStream os = Files.newOutputStream(content, StandardOpenOption.TRUNCATE_EXISTING);
                StreamResult result = new StreamResult(os);
                transformer.transform(source, result);
                os.flush(); // needed
                os.close(); // needed
if (Debug.isLoggable(Level.FINER)) {
 result = new StreamResult(System.out);
 transformer.transform(source, result);
 System.out.println();
}
            } catch (TransformerException e) {
                throw new IOException(e);
            }
        }

        /** add image physically */
        public void addCoverImage(String imageUrl, Path imagePath) throws IOException {
            if (!Files.exists(imagePath.getParent())) {
                Files.createDirectory(imagePath.getParent());
            }
            Files.copy(new URL(imageUrl).openStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
Debug.println(Level.FINER, "imagePath: " + Files.size(imagePath));
            updateImageMetadata(imagePath);
        }

        /** image */
        private void updateImageMetadata(Path out) throws IOException {
            try {
                DOMNodeList elements = (DOMNodeList) xpath.evaluate("//*[local-name() = 'manifest']/*[local-name() = 'item'][@*[local-name() = 'id']='cover']", document, XPathConstants.NODESET);
                Element element = (Element) elements.item(0);
Debug.println(Level.FINER, "href:B: " + element.getAttribute("href"));
                element.setAttribute("href", out.toString());
Debug.println(Level.FINER, "href:A: " + element.getAttribute("href"));
            } catch (XPathExpressionException e) {
                throw new IOException(e);
            }
        }

        /** @return nullable */
        public BufferedImage getImage() throws IOException {
            try {
                DOMNodeList elements = (DOMNodeList) xpath.evaluate("//*[local-name() = 'manifest']/*[local-name() = 'item'][@*[local-name() = 'id']='cover']", document, XPathConstants.NODESET);
                Element element = (Element) elements.item(0);
                if (element == null) {
Debug.println(Level.FINE, "no cover element");
                    return null;
                }
                String url = element.getAttribute("href");
Debug.println(Level.FINE, "href:cover: " + url);
                return url.isEmpty() ? null : ImageIO.read(Files.newInputStream(epubFs.getPath(url)));
            } catch (XPathExpressionException e) {
                throw new IOException(e);
            }
        }

        /** currently author and title */
        public void updateMetadata() throws IOException {
            try {
                Element element = (Element) xpath.evaluate("//*[local-name() = 'creator']", document, XPathConstants.NODE);
                element.setTextContent(author);
                element.removeAttributeNS("opf", "file-as");
                element = (Element) xpath.evaluate("//*[local-name() = 'title']", document, XPathConstants.NODE);
                element.setTextContent(title);
            } catch (XPathExpressionException e) {
                throw new IOException(e);
            }
        }
    }
}

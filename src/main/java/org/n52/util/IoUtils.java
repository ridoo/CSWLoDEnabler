package org.n52.util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Some little helper methods for IO-handling.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class IoUtils {

    public static String readText(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuffer sb = new StringBuffer();
        for (int i=0; (line = br.readLine()) != null; i++) {
            
            // if not first line --> append "\n"
            if (i > 0) {
                sb.append("\n");
            }
            
            sb.append(line);
        }
        br.close();

        return sb.toString();
    }
    
    public static String readText(URL url) throws IOException {
        return readText(url.openStream());
    }
    
    public static String readText(File file) throws IOException {
        return readText(file.toURL());
    }

    public static String supplyProperty(String key, URL url) throws IOException {
        InputStream in = url.openConnection().getInputStream();
        Properties prop = new Properties();
        prop.load(in);

        String erg = prop.getProperty(key);

        return erg;
    }

    public static void saveFile(OutputStream out, InputStream in) throws IOException {
    	BufferedOutputStream bufout = new BufferedOutputStream(out);
        int b;
        while ( (b = in.read()) != -1) {
        	bufout.write(b);
        }
        bufout.flush();
        bufout.close();
        out.flush();
        out.close();
    }
    
    public static void saveFile(String filename, URL url) throws IOException {
        InputStream in = url.openConnection().getInputStream();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
        saveFile(out, in);
    }
    
    public static void saveFile(File file, InputStream in) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        saveFile(out, in);
    }

    public static void saveFile(File filename, File fileIn) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(fileIn));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
        saveFile(out, in);
    }
    
    public static void saveFile(File filename, String stringToStoreInFile) throws IOException {
        OutputStream out = new FileOutputStream(filename);
        out.write(stringToStoreInFile.getBytes());
        out.flush();
        out.close();
    }

    /**
     * 
     * @param filename
     * @param msg
     * @param append
     *        if <code>true</code>, then bytes will be written to the end of the file rather than the
     *        beginning
     * @throws IOException
     */
    public static void saveFile(String filename, String msg, boolean append) throws IOException {
        FileWriter writer = new FileWriter(new File(filename), append);

        writer.write(msg);
        writer.flush();
        writer.close();
    }

    public static void decompressAll(File zipFile, File targetDirectory) throws IOException,
            ZipException {

        if ( !targetDirectory.isDirectory())
            throw new IOException("2nd Parameter targetDirectory is not a valid diectory!");

        byte[] buf = new byte[4096];
        ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
        while (true) {
            // N�chsten Eintrag lesen
            ZipEntry entry = in.getNextEntry();
            if (entry == null) {
                break;
            }

            // Ausgabedatei erzeugen
            FileOutputStream out = new FileOutputStream(targetDirectory.getAbsolutePath() + "/"
                    + new File(entry.getName()).getName());
            // Lese-Vorgang
            int len;
            while ( (len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();

            // Eintrag schlie�en
            in.closeEntry();
        }
        in.close();
    }

    public static void compressFilesToZip(File[] files, File zipFile) throws IOException,
            ZipException {
        File[] clearedFiles = removeDoubleFiles(files);

        byte[] buf = new byte[4096];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        for (int i = 0; i < clearedFiles.length; i++) {
            String fname = clearedFiles[i].getAbsolutePath();

            FileInputStream in = new FileInputStream(fname);
            ZipEntry entry = new ZipEntry(fname);
            out.putNextEntry(entry);
            int len;
            while ( (len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
        }
        out.close();
    }

    /**
     * helper-method for compressFilesToZip
     */
    private static File[] removeDoubleFiles(File[] files) {
        ArrayList tmpList = new ArrayList();

        for (int i = 0; i < files.length; i++) {
            if ( !tmpList.contains(files[i])) {
                tmpList.add(files[i]);
            }
        }

        File[] res = new File[tmpList.size()];
        for (int i = 0; i < tmpList.size(); i++) {
            res[i] = (File) tmpList.get(i);
        }

        return res;
    }

}
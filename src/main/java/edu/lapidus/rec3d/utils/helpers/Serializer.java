package edu.lapidus.rec3d.utils.helpers;

import edu.lapidus.rec3d.utils.SerializedDataType;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by Егор on 02.12.2015.
 */
public class Serializer {

    private final static Logger logger = Logger.getLogger(Serializer.class);
    public static final String SER_DIRECTORY = "/Data/";
    public static String saveObject(Serializable obj, SerializedDataType type) {
        File f = buildFileName(obj, type);
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
            os.writeObject(obj);
            logger.info("Saved " + type.name());
        } catch (IOException e) {
            //TODO we need some logger thing everywhere
            //TODO we also need our own exceptions to rethrow
            logger.error("Failed to save " + type.name(), e);
            return null;
        }
        return f.getPath();
    }

    public static Object loadObject(String name) {
        File f;
        if (name.contains(SER_DIRECTORY)) {
            f = new File(name);
        } else {
            f = new File(SER_DIRECTORY + name);
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            Object res = ois.readObject();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File buildFileName(Serializable obj, SerializedDataType type) {
        String namePref = obj.getClass().getSimpleName();
        String suff;
        switch (type) {
            case POINTS:
                suff = "points";
                break;
            case SOMETHING_ELSE:
                suff = "smth";
                break;
            default:
                suff = "unknown";
                break;
        }
        String nameSuff = suff.concat("_" + getFileIndex(namePref, suff));

        return new File(SER_DIRECTORY + namePref + "_" + nameSuff + ".ser");
    }

    private static String getFileIndex(String prefix, String suffix) {
        final String fileName = prefix.concat("_".concat(suffix));
        File data = new File(SER_DIRECTORY);
        File[] files = data.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.contains(fileName))
                    return true;
                return false;
            }
        });
        if (files.length == 0)
            return "0";
        int maxIndex = 0;
        for (File f : files) {
            String [] parts = f.getName().split("[_\\.]");
            int read = Integer.parseInt(parts[2]);
            if (maxIndex < read) {
                maxIndex = read;
            }
        }
        return ++maxIndex + "";
    }

}

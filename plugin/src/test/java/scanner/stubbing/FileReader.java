package scanner.stubbing;

import java.io.*;
import java.util.Objects;

abstract class FileReader {

    static String readFileInResources(String fileName) throws IOException {

        ClassLoader classLoader = FileReader.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());


        InputStream is = new FileInputStream(file);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while(line != null){
            sb.append(line).append("\n");
            line = buf.readLine();
        }

        return sb.toString();
    }
}

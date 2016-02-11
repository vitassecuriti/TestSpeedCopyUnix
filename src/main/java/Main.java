import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by VSKryukov on 27.01.2016.
 */
public class Main {
    public static FileSystemManager vfsManager;
    public static final String EXECUTION_DIR = new File("").getAbsolutePath();
    public static ArrayList<String> listReport = new ArrayList<>();

    public static void main (String [] args) throws Exception {

        vfsManager = VFS.getManager();
        TestHostParams params;
        File file = new File(EXECUTION_DIR + "/HostList.txt");


        if (!file.exists() || file.isDirectory()){
            throw new Exception(String.format("File \"'%s'\" does not exisss!",file.getName()));
        }

        Scanner sc = new Scanner(file);
        int countLine=0;
        //Ignor header from file
        sc.nextLine();
        while(sc.hasNext()) {
            String[] line = sc.nextLine().split(";");
            countLine++;
            if (line.length != 7) {
                throw new Exception(String.format("Wrong number params in line '%s'!",countLine ));

            }
            params = new TestHostParams(line);
            // params.print();
            params.setSourceDir(EXECUTION_DIR.replace("\\", "/"));
            // params.print();


            CopyFile copyFile = new CopyFile(params);
            copyFile.setFsManager(vfsManager);


            System.out.println("Test for host: " + params.getHost());
            copyFile.copyFile();





            Long duration = copyFile.getEnd_time() - copyFile.getStart_time();
            double InterfaceCapacity = getInterfaceCapacity(copyFile.getSize(),duration);
            System.out.println("Speed - "+ InterfaceCapacity + "Mb/sec");
            listReport.add("HOST: " + params.getHost() + ",  InterfaceCapacity: " + InterfaceCapacity + "MB/sec");

            System.out.println("Removal of the copied file :");
            copyFile.cleanFolder();

        }


        WriteReport(EXECUTION_DIR, listReport);
        System.out.println("Process of check of speed of the interface is successfully ended.");
        System.out.println("Results are written down in the" + EXECUTION_DIR + "/Report.txt file");
    }

    public static double getInterfaceCapacity(long fileSize, long duration){


        double fileSizeMb = fileSize/1048576;
        double durationSec = duration/1000;

        System.out.println("fileSizeMb - " + fileSizeMb + " MB" );
        System.out.println("durationSec - " + durationSec + " SEC" );

        return roundDouble(((fileSizeMb / durationSec)* 1.7), 2, RoundingMode.HALF_UP); /*2 так как никакое сжатие не действует*/
    }

    private static double roundDouble(double roundedNumber, int roundingPrecision, RoundingMode roundingMethod){
        return new BigDecimal(roundedNumber).setScale(roundingPrecision, roundingMethod).doubleValue();
    }

    private static void WriteReport (String path, ArrayList<String> listReport){
        if (listReport.size()!=0) {
            String fileReport = path + "/Report.txt";

            try (FileWriter writer = new FileWriter(fileReport, false)) {
                for (String record : listReport)
                    writer.write(record + "\n");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

}

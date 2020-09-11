package IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class JavaCMD {
    public void cmd(){
        File currentFile = new File("");
        Scanner scanner = new Scanner(System.in);
        while (true){
            String currentFilePath = currentFile.getAbsolutePath();
            System.out.print(currentFilePath + ": ");
            switch (scanner.next()){
                case "cd":{
                    File temp = cmdCD(currentFilePath, scanner.next());
                    if (temp == null)
                        System.out.println("error path");
                    else {
                        currentFile = temp;
                    }
                }break;
                case "dir":
                    cmdDir(currentFilePath);break;
                case "md":
                    cmdMd(currentFilePath, scanner);
                break;
                case "copy":
                    cmdCopy(currentFilePath, scanner.next(), scanner.next(), false);break;
                case "move":
                    cmdCopy(currentFilePath, scanner.next(), scanner.next(), true);break;
                case "exit":
                    System.exit(0);break;
                case "help":
                    System.out.println("Command List: cd, dir, md, move, exit");break;
                case "#":break;
                default:
                    System.out.println("Input help to get command list");break;
            }
        }
    }

    private File cmdCD(String currentFilePath, String path){
            String[] targetPathCut = path.split("/");
            for (String tp: targetPathCut){
                switch (tp){
                    case ".":break;
                    case "..":{
                        int lastIndex = currentFilePath.lastIndexOf("\\");
                        if (lastIndex != -1)
                            currentFilePath = currentFilePath.substring(0, lastIndex);
                    }break;
                    default:
                        currentFilePath = currentFilePath + "\\" + tp;break;
                }
            }
            if (currentFilePath.lastIndexOf("\\") == -1)
                currentFilePath += "\\";
            File targetFile = new File(currentFilePath);
            if (!targetFile.exists())
                return null;
            else
                return targetFile;
    }

    private void cmdDir(String currentFilePath){
        Path currentPath = Paths.get(currentFilePath);
        System.out.println(currentFilePath + " File List:");
        try {
            Files.list(currentPath).forEach(path -> {
                try {
                    BasicFileAttributes basicFileAttributes = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
                    Date date = Date.from(basicFileAttributes.lastModifiedTime().toInstant());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd  hh:mm");
                    String result = sdf.format(date) + "    ";
                    if (basicFileAttributes.isDirectory()){
                        result += "<DIR>          ";
                        System.out.println(result + path.getFileName());
                    }
                    else{
                        result += "    ";
                        System.out.printf("%s%10s %s\n", result, basicFileAttributes.size(), path.getFileName());
                    }
                } catch (IOException e) {
                    System.out.println("File attributes get failed");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cmdMd(String currentFilePath, Scanner scanner){
        //scanner阻塞式，hasNext不会返回false, 必须设置终止符跳出循环
        System.out.println("Input # to finish this task");
        while (!scanner.hasNext("#")){
            String[] mdPathCut = scanner.next().split("/");
            String rootPath = currentFilePath;
            for (String dirName: mdPathCut){
                rootPath += "/" + dirName;
                if (!Files.exists(Paths.get(rootPath))) {
                    try {
                        Files.createDirectory(Paths.get(rootPath));
                        System.out.println("Directory " + dirName + " create success");
                    } catch (IOException e) {
                        System.out.println("Create Directory failed");
                    }
                }
            }
        }
    }

    private boolean cmdCopy(String currentFilePath, String source, String target, boolean type){
        File s = null;
        File t = null;

        if (Files.exists(Paths.get(source)))
            s = new File(source);
        else if (Files.exists(Paths.get(currentFilePath, source))){
            source = currentFilePath + "/" + source;
            s = new File(source);
        }
        else {
            System.out.println("Source file or directory is not exist, please check again");
            return false;
        }

        if (Files.exists(Paths.get(target)))
            t = new File(target);
        else if (Files.exists(Paths.get(currentFilePath, target))){
            target = currentFilePath + "/" + target;
            t = new File(target);
        }
        else {
            System.out.println("Target file or directory is not exist, please check again");
            return false;
        }

        try {
            if (s.isDirectory() && !t.isDirectory()) {
                System.out.println("Can not copy a directory to a file, please check again");
                return false;
            }
            else if (!s.isDirectory() && t.isDirectory()) {
                String fileName = source.substring(source.lastIndexOf("/") + 1);
                FileInputStream fis = new FileInputStream(s);
                File tagetFile = new File(target + "/" + fileName);
                FileOutputStream fos = new FileOutputStream(tagetFile);
                byte[] bbuf = new byte[1024];
                //不定义，最后一个buf输出两次
                int hasRead = 0;
                while ((hasRead = fis.read(bbuf)) > 0) {
                    fos.write(bbuf, 0, hasRead);
                }
                fis.close();
                fos.close();
                if (type)
                    s.delete();
                //System.out.println("File copy to directory success");
                return true;
            }
            else if (s.isDirectory() && t.isDirectory()) {
                String newTarget = target + source.substring(source.lastIndexOf("/"));
                File targetDir = new File(newTarget);
                if (!targetDir.exists())
                    targetDir.mkdir();
                File[] files = s.listFiles();
                for (File f : files) {
                    cmdCopy(currentFilePath, source + "/" + f.getName(), newTarget, type);
                }
                if (type)
                    s.delete();
                //System.out.println("Directory copy to directory success");
                return true;
            }
            else {
                FileInputStream fis = new FileInputStream(s);
                FileOutputStream fos = new FileOutputStream(t);
                byte[] bbuf = new byte[1024];
                int hasRead = 0;
                while ((hasRead = fis.read(bbuf)) > 0)
                    fos.write(bbuf, 0, hasRead);
                fis.close();
                fos.close();
                if (type)
                    s.delete();
                //System.out.println("File copy to file success");
                return true;
            }
        }catch (IOException e){
            System.out.println("file copy filed");
            return false;
        }
    }

    public static void main(String[] args) {
        JavaCMD javaCMD = new JavaCMD();
        javaCMD.cmd();
    }
}

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/*
 * 
 *   
 */
public class HelloJava extends JPanel {
	
    public static void main(String[] args) throws Exception{
    	String dStr = "D:/scorpionking";
    	String javaCodeStr = "";
    	
    	List<String> list = new ArrayList<String>();
    	File file = new File(dStr);
    	ergodic(file,list);
    	for (String item : list){
    		System.out.println(item);
    		javaCodeStr += CommonUtil.readFile(item, "utf-8");
    	}
    	CommonUtil.writeFile("D:/", "hello.txt", 123 ,javaCodeStr, "utf-8");
    	System.out.println(list.size());
    }
    private static List<String> ergodic(File file,List<String> resultFileName){
        File[] files = file.listFiles();
        if(files==null)return resultFileName;// 判断目录下是不是空的
        for (File f : files) {
        	if(f.isFile() && f.getName().indexOf("java") > -1 && f.getName().indexOf("svn-base") < 0){
        		resultFileName.add(f.getPath());
        	}else if(f.isDirectory()){
        		ergodic(f,resultFileName);
        	}
        }
        return resultFileName;
    }
}

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonUtil {

	private static final Log log = LogFactory.getLog(CommonUtil.class);

	public static String getStackMessage(Exception exception){
		if (exception == null){
			return "";
		}
		StringWriter writer = null;
		String ret ="";
		try{
			writer = new StringWriter();
			exception.printStackTrace(new PrintWriter(writer));
			ret = writer.getBuffer().toString();
		}finally {
			if(writer != null){
				try {
					writer.close();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	/**
	 * 执行命令
	 * @param command window:{"cmd.exe /c dir"}
	 * @return
	 */
	public static String callSystem(String command) {
		InputStream is = null;
		try {
			Process process = Runtime.getRuntime().exec(command);
			StringBuffer sb = new StringBuffer();
			byte[] buffer = new byte[256];
			int cnt = 0;
			is = process.getInputStream();
			while ((cnt = is.read(buffer)) >= 0) {
				sb.append(new String(buffer, 0, cnt));
			}
			is.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		} finally{
			if (is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 利用第三方开源包cpdetector获取文件编码格式
	 * 
	 * @param path
	 *            要判断文件编码格式的源文件的路径
	 * @author huanglei
	 * @version 2012-7-12 14:05
	 */
	

	/**
	 * 将远程资源保存到本地
	 * 
	 * @param httpUrl
	 *            要下载的资源的地址
	 * @return 远程对象的url
	 * @throws Exception
	 */
	public static String saveHttpResourceToFile(String httpUrl,
			String filePath, String fileName, String cookie) {
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		HttpURLConnection conn = null;
		String returnUrl = null;
		try {
			URL url = new URL(httpUrl);// 获取到路径,要下载的资源的地址，
			// http协议连接对象

			conn = (HttpURLConnection) url.openConnection();
			if (cookie != null && !cookie.equals("")) {
				conn.setRequestProperty("Cookie", cookie);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
			}

			// conn.setRequestMethod("GET");// 这里是不能乱写的，详看API方法
			conn.setConnectTimeout(6 * 1000); // 别超过10秒。

			if (conn.getResponseCode() == 200) {
				returnUrl = conn.getURL().toString();
				inputStream = conn.getInputStream();
				File fileD = new File(filePath);// 给资源起名子
				fileD.mkdirs();

				File file = new File(filePath + fileName);// 给资源起名子
				outputStream = new FileOutputStream(file);// 写出对象

				byte[] buffer = new byte[1024]; // 用数据装
				int len = -1;
				while ((len = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, len);
				}
				outputStream.close();
				inputStream.close();
			}
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			returnUrl = null;
		}
		return returnUrl;
	}

	/**
	 * 获取文件名扩展名（小写,不带点的）
	 * 
	 * @param fileName
	 *            :文件名
	 * @return
	 */
	public static String getFileExtName(String fileName) {
		if (fileName == null) {
			return "";
		} else {
			int lastIndex = fileName.lastIndexOf(".");
			return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1)
					.toLowerCase();
		}
	}

	public static long getDateDaysDiff(Date date1, Date date2) {
		long ret = 0;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date d2 = df.parse(df.format(date2));
			Date d1 = df.parse(df.format(date1));
			ret = (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24);
		} catch (Exception e) {
			ret = 0;
		}
		return ret;
	}

	/**
	 * 文件复制
	 * 
	 * @param srcFile
	 *            源的文件
	 * @param toPath
	 *            保存路径(包括文件名)
	 * @return 1:成功，-1：源文件不存在，-2：其他异常
	 * @throws Exception
	 */
	public static String copyFile(File srcFile, String destPath,
			String destFileName) {
		if (!srcFile.exists()) {
			return "-1";
		}
		new File(destPath).mkdirs();
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			fos = new FileOutputStream(destPath + "/" + destFileName);
			fis = new FileInputStream(srcFile);
			byte[] buffer = new byte[1024 * 16];
			int len = 0;
			while ((len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		} catch (Exception e) {
			log.error("::", e);
			return "-2";
		} finally {
			try {
				if (null != fis) {
					fis.close();
				}
				if (null != fos) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "1";
	}

	public static void deleteDir(String filepath) {
		File f = new File(filepath);// 定义文件路径
		try {
			if (f.exists() && f.isDirectory()) {// 判断是文件还是目录
				if (f.listFiles().length == 0) {// 若目录下没有文件则直接删除
					f.delete();
				} else {// 若有则把文件放进数组，并判断是否有下级目录
					File delFile[] = f.listFiles();
					int i = f.listFiles().length;
					for (int j = 0; j < i; j++) {
						if (delFile[j].isDirectory()) {
							deleteDir(delFile[j].getAbsolutePath());// 递归调用del方法并取得子目录路径
						}
						delFile[j].delete();// 删除文件
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <b>功能:</b>文件（目录）拷贝
	 * 
	 * @param srcDirPath ：源文件（或目录）
	 * @param destDirPath ：目标文件（或目录）
	 * @return {-1:源文件不存在,1:成功}
	 */
	public static String copyFile(String srcDirPath, String destDirPath) {
		File srcDir = new File(srcDirPath);
		File destDir = new File(destDirPath);
		return copyFile(srcDir, destDir);
	}
	
	/**
	 * <b>功能:</b>检查文件是否存在
	 * 
	 * @param filePath ：文件（或目录）
	 * @return {-1:源文件不存在,1:存在}
	 */
	public static String checkExist(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return "-1";
		}else{
			return "1";
		}
	}

	public static String getIp() {
		StringBuffer sb = new StringBuffer();
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();

				sb.append("DisplayName:" + ni.getDisplayName() + ",");
				sb.append("Name:" + ni.getName() + ",");
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					sb.append("IP:" + ips.nextElement().getHostAddress());
				}
				sb.append("\r\n");
			}
		} catch (Exception e) {
			log.error("::", e);
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param url
	 * @param connectionTimeout
	 * @param soTimeout
	 * @return 出错将返回+"error:"
	 */

	/**
	 * 将字符串按照编码写入文件
	 * 
	 * @param filePath
	 *            ：文件路径
	 * @param content
	 *            :写入的内容
	 * @param charset
	 *            ：文件编码
	 * @return
	 */
	public static boolean writeFile(String filePath, String fileName,
			String content, String charset) {
		try {
			File file = new File(filePath);
			file.mkdirs();
			OutputStreamWriter os = new OutputStreamWriter(
					new FileOutputStream(new File(filePath + fileName)),
					charset);
			os.write(content);
			os.close();
			return true;
		} catch (Exception e) {
			log.error("::", e);
			return false;
		}
	}

	/**
	 * 将字符串按照编码写入文件
	 * 
	 * @param filePath
	 *            ：文件路径
	 * @param content
	 *            :写入的内容
	 * @param charset
	 *            ：文件编码
	 * @return
	 */
	public static boolean writeFile(String filePath, String fileName,
			long lastModified, String content, String charset) {
		try {
			File path = new File(filePath);
			path.mkdirs();

			File _file = new File(filePath + fileName);
			OutputStreamWriter os = new OutputStreamWriter(
					new FileOutputStream(_file), charset);
			os.write(content);
			os.close();
			_file.setLastModified(lastModified);
			return true;
		} catch (Exception e) {
			log.error("::", e);
			return false;
		}
	}

	/**
	 * 读入字符文件内容
	 * 
	 * @param filePath
	 *            ：文件路径
	 * @return
	 */
	public static String readFile(String filePath, String charset) {
		try {
			StringBuilder sb = new StringBuilder();
			InputStreamReader in = new InputStreamReader(new FileInputStream(
					filePath), charset);
			char[] buffer = new char[4 * 1024];
			int len = 0;
			while ((len = in.read(buffer)) != -1) {
				sb.append(buffer, 0, len);
			}
			in.close();
			return sb.toString();
		} catch (Exception e) {
			log.error("::", e);
			return "";
		}
	}

	/**
	 * 有效邮件正则表达式
	 */
	private static Pattern emailPattern = Pattern
			.compile("[_a-zA-Z0-9.\\-]+@([_a-zA-Z0-9]+\\.)+[a-zA-Z0-9]{2,3}");

	/**
	 * 手机号正则表达式
	 */
	private static Pattern mobilePattern = Pattern
			.compile("(13[0-9]|15[0|1|3|5|6|8|9])\\d{8}");

	/**
	 * 数字政策表达式
	 */
	private static Pattern numberPattern = Pattern
			.compile("[0-9]{1,}[.][0-9]{1,}|[0-9]{1,}");

	private static Pattern positiveNumPattern = Pattern
			.compile("[1-9][0-9]{0,}");

	public static String[] domains = { ".com.cn", ".net.cn", ".cn", ".com",
			".net" /** ,".org",".edu",".mil",".gov" */
	};

	/**
	 * 判断是否有效邮件
	 * 
	 * @param email
	 */
	public static boolean isValidEmail(String email) {
		if (email == null) {
			return false;
		}
		Matcher m = emailPattern.matcher(email);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断是否有效手机号
	 * 
	 * @param email
	 */
	public static boolean isValidMobile(String mobile) {

		Matcher m = mobilePattern.matcher(mobile);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 给定字符传是否是数字
	 * 
	 * @return
	 */
	public static boolean isNumber(String targetStr) {
		Matcher m = numberPattern.matcher(targetStr);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 给定字符串是否正整数
	 * 
	 * @param targetStr
	 * @return
	 */
	public static boolean isPositiveNumber(String targetStr) {
		Matcher m = positiveNumPattern.matcher(targetStr);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 是否空字符串
	 * 
	 * @param targetStr
	 */
	public static boolean isEmpty(String targetStr) {
		if (targetStr == null || "".equals(targetStr.trim())) {
			return true;
		} else {
			return false;
		}
	}

	public static Properties loadPropertiesFile(String fileName) {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				is = CommonUtil.class.getClassLoader().getResourceAsStream(fileName);
			} else {
				is = new FileInputStream(file);
			}
			if (is != null) {
				prop.load(is);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return prop;
	}

	/**
	 * 去掉字符串前后的空字符，包括全角的
	 * 
	 * @return
	 */
	public static String trim(String str, String Flag) {

		if (str == null || str.equals("")) {
			return str;
		} else {
			str = "" + str;
			if (Flag == "l" || Flag == "L") {// 去掉首空格
				String RegularExp = "^[\u00a0|\u0020]+";
				return str.replaceAll(RegularExp, "");
			} else if (Flag == "r" || Flag == "R") { // 去掉尾空格
				String RegularExp = "[\u00a0|\u0020]+$";
				return str.replaceAll(RegularExp, "");
			} else { // 去掉首和尾空格
				String RegularExp = "^[\u00a0|\u0020]+|[\u00a0|\u0020]+$";
				return str.replaceAll(RegularExp, "");
			}
		}
	}

	/**
	 * <b>功能:</b>文件（目录）拷贝
	 * 
	 * @param sourceFile：源文件（或目录）
	 * @param destFile：目标文件（或目录）
	 * @return {-1:源文件不存在,1:成功}
	 */
	public static String copyFile(File sourceFile, File destFile) {
		if (!sourceFile.exists()) {
			log.debug( "源文件不存在:" + sourceFile.getPath());
			return "-1";
		}
		
		if (sourceFile.isDirectory()) {
			destFile.mkdirs();
			log.debug( "建文件夹:" + destFile.getPath() );
			File[] files = sourceFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				File ifile = files[i];
				String name = destFile.getPath() + "/" + ifile.getName();
				File jfile = new File(name);
				copyFile(ifile, jfile);
			}
		} else {
			FileInputStream fi = null;
			FileOutputStream fo = null;
			try {
				fi = new FileInputStream(sourceFile);
				fo = new FileOutputStream(destFile);
				byte[] buf = new byte[1024];
				int size = 0;
				while ((size = fi.read(buf)) > 0) {
					fo.write(buf, 0, size);
				}
				fo.flush();

				log.debug( "拷贝文件:" + sourceFile.getName() + "\t>>  " + destFile.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fi != null)
					try {
						fi.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if (fo != null)
					try {
						fo.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return "1";
	}

	/**
	 * <b>功能:</b>文件（目录）拷贝
	 * 
	 * @param file1
	 *            ：源文件（或目录）
	 * @param file2
	 *            ：目标文件（或目录）
	 */
	public static String copyFile(File file1, File file2, boolean content) {
		String log = "";
		if (!file1.exists()) {
			log += "源文件不存在:" + file1.getPath();
			return log;
		}
		if (file1.isDirectory()) {
			file2.mkdirs();
			log += "建文件夹:" + file2.getPath() + "\n";
			File[] files = file1.listFiles();
			for (int i = 0; i < files.length; i++) {
				File ifile = files[i];
				String name = file2.getPath() + "/" + ifile.getName();
				File jfile = new File(name);
				log += copyFile(ifile, jfile);
			}
		} else {
			FileInputStream fi = null;
			FileOutputStream fo = null;
			try {
				fi = new FileInputStream(file1);
				fo = new FileOutputStream(file2);
				byte[] buf = new byte[1024];
				int size = 0;
				while ((size = fi.read(buf)) > 0) {
					fo.write(buf, 0, size);
				}
				fo.flush();

				log += "拷贝文件:" + file1.getName() + "\t>>  " + file2.getPath()
						+ "\n";
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fi != null)
					try {
						fi.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if (fo != null)
					try {
						fo.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return log;
	}
//	public static String x163(String orgin){
//		var ret=orgin;
//		if (ret.indexOf("分钟前")!=-1){
//			ret = ScorpionUtil.formatDate(new Date(Scorpion.currentTime.getTime()-ret.replace("分钟前","")*1000*60),true);
//		}
//		ret = ret.replace("昨天",ScorpionUtil.formatDate(new Date(Scorpion.currentTime.getTime()-1000*60*60*24))+" ");
//		ret = ret.replace("今天",ScorpionUtil.formatDate(Scorpion.currentTime)+" ");
//		return ret;
//	}
//
//	public static String sohu((orgin){
//		var ret=orgin;
//		if (ret.indexOf("分钟前")!=-1){
//			ret = ScorpionUtil.formatDate(new Date(Scorpion.currentTime.getTime()-ret.replace("分钟前","")*1000*60),true);
//		}
//		ret = ret.replace("昨天",ScorpionUtil.formatDate(new Date(Scorpion.currentTime.getTime()-1000*60*60*24))+" ");
//		ret = ret.replace("今天",ScorpionUtil.formatDate(Scorpion.currentTime)+" ");
//		return ret;
//	}
	
	public static Date formatTime(String timeString) {
		if (timeString==null){
			return null;
		}
		timeString = timeString.trim().replaceAll(",", " ");
		timeString = timeString.trim().replaceAll("\\s{2,}", " ");
		
		//12年10月23日, 11:36 下午 
		
		//时间格式，带年月日
		String[] timeFormatYear=new String[]{
				"yy年MM月dd日 KK:mm:ss a",//12年10月23日, 11:36 下午 
				"yy年MM月dd日 KK:mm a",//12年10月23日, 11:36 下午 
				"yyyy-M-d H:m:s",//2012-10-10 11:39:11
				"yyyy-M-d H:m",//2012-10-10 11:39
				"yyyy年M月d日 H:m:s",//2012年10月10日 11:39:11
				"yyyy年M月d日 H:m"//2012年10月10日 11:39
				};
		//时间格式，带月日
		String[] timeFormatMonthDate=new String[]{
				"M-d H:m:s",//10-10 11:39:11
				"M-d H:m",//10-10 11:39
				"M月d日 H:m:s",//10月10日 11:39:11
				"M月d日 H:m"//10月10日 11:39
				};
		//时间格式，不带年月日
		String[] timeFormat=new String[]{
				"H:m:s",//11:39:11
				"H:m",//11:39
				};
		Date dateParse = null;
		for (String string : timeFormatYear) {
			try{
				dateParse = new SimpleDateFormat(string).parse(timeString);
				return dateParse;
			}catch (Exception e) {
				//e.printStackTrace();
			}
		}
		if ( dateParse == null){
			for (String string : timeFormatMonthDate) {
				try{
					dateParse = new SimpleDateFormat(string).parse(timeString);
					Calendar ca=Calendar.getInstance();
					ca.setTime(dateParse);
					ca.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
					return ca.getTime();
				}catch (Exception e) {
					//e.printStackTrace();
				}
			}
		}
		if ( dateParse == null){
			for (String string : timeFormat) {
				try{
					dateParse = new SimpleDateFormat(string).parse(timeString);
					Calendar ca=Calendar.getInstance();
					ca.setTime(dateParse);
					ca.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
					ca.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
					ca.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE));
					return ca.getTime();
				}catch (Exception e) {
					//e.printStackTrace();
				}
			}
		}
		return dateParse;
	}
	public static void outFormatTime(String timeString) {
		System.out.println(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(formatTime(timeString)));
	}
	public static void main(String[] args) throws Exception {
		
//		SimpleDateFormat formatter = new  SimpleDateFormat ( "yyyy年MM月dd日 KK:mm:ss a" );
//		String x=formatter.format(new Date());
//		System.out.println(x);
//		System.out.println(formatter.parse(x));
		
		outFormatTime(" 12年10月25日 05:40 下午");
		outFormatTime(" 12年10月25日 05:40:14 下午");
//		outFormatTime(" 12年10月22日 05:07 下午 ");
//		outFormatTime(" 12年10月23日, 11:36 下午 ");
//		outFormatTime(" 2012-10-10  11:39:11 ");
//		outFormatTime(" 2012-10-10  11:39 ");
//		outFormatTime(" 2012年10月10日   11:39:11 ");
//		outFormatTime(" 2012年10月10日   11:39 ");
//		
//		outFormatTime(" 10-10   11:39:11 ");
//		outFormatTime(" 10-10   11:39 ");
//		outFormatTime(" 10月10日   11:39:11 ");
//		outFormatTime(" 10月10日   11:39 ");
//		outFormatTime(" 9月9日   9:9 ");
//		
//		outFormatTime(" 11:39:11 ");
//		outFormatTime(" 11:39 ");
		
	}

}

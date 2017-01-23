package dms_re;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.dms.IOUtil;
import com.dms.bo.LogData;



public class DMSClient {
	
	//第一步
	private File logFile;//系统日志文件
	
	private File textLogFile;//解析后的日志文件
	
	private File lastPositionFile;//每次解析后最后的地址 
	
	private int batch;//每次解析的条目数
	
	//第二步
	private File logRecFile;//保存配对的日志文件	
	private File loginLogFile;//保存未配对的日志文件

	//第三部
	private String serverHost;//服务端地址
	private int serverPort; //服务端端口
	
	
	public DMSClient(){
		Map<String,String> config = new HashMap<String,String>();
		config = loadConfig();
		init(config);
	}
	
	public Map<String,String> loadConfig(){
		SAXReader reader = new SAXReader();
		Map<String,String> config = null;
		try {
			Document doc = reader.read(new File("config.xml"));
			config = new HashMap<String,String>();
			
			Element root = doc.getRootElement();
						
			List<Element> list = new ArrayList<Element>();
			list = root.elements();
			
			for(Element e : list){
				String key = e.getName();
				String value = e.getTextTrim();
				config.put(key, value);
			}
			//return config;
			System.out.println(config);
		} catch (DocumentException e) {
			System.out.println("解析配置文件异常!");
			e.printStackTrace();
		}
		return config;	
		
	}
	
	public void init(Map<String,String> config){
		try{
			logFile = new File(config.get("logfile"));
			textLogFile = new File(config.get("textlogfile"));
			lastPositionFile = new File(config.get("lastpositionfile"));
			batch = Integer.parseInt(config.get("batch"));
			
			logRecFile = new File(config.get("logrecfile"));
			loginLogFile = new File(config.get("loginlogfile"));
			
			serverHost = config.get("serverhost");
			serverPort = Integer.parseInt(config.get("serverport"));
			System.out.println("初始化成功");
		}catch(Exception e){
			System.out.println("初始化失败");
			e.printStackTrace();
		}		
	}

	public long hasLogs(){
		try{
			if(!lastPositionFile.exists()){
				return 0;
			}
			long lastPosition = IOUtil.readLong(lastPositionFile);
			
			if(logFile.length()-lastPosition>=LogData.LOG_LENGTH){
				return lastPosition;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return -1;		
	}
	
	public boolean parseLogs(){
		
		if(textLogFile.exists()){
			System.out.println(textLogFile+"存在");
			return true;
		}
		
		if(!logFile.exists()){
			System.out.println(logFile+"不存在");
			return false;
		}
		
		long lastPosition = hasLogs();
		
		if(lastPosition<0){
			System.out.println("已经没有日志可以解析了");
			return false;
		}
		
		try {
			RandomAccessFile raf = new RandomAccessFile(logFile,"r");
			raf.seek(lastPosition);
			
			List<LogData> list = new ArrayList<LogData>();
			for(int i=0; i<batch; i++){
				if(logFile.length()-lastPosition<LogData.LOG_LENGTH){
					break;
				}
				raf.seek(lastPosition+LogData.USER_OFFSET);
				String user = IOUtil.readString(raf,LogData.USER_LENGTH).trim();
				
				raf.seek(lastPosition+LogData.PID_OFFSET);
				int pid = raf.readInt();
				
				raf.seek(lastPosition+LogData.TYPE_OFFSET);
				short type = raf.readShort();
				
				raf.seek(lastPosition+LogData.TIME_OFFSET);
				int time = raf.readInt();
				
				raf.seek(lastPosition+LogData.HOST_OFFSET);
				String host = IOUtil.readString(raf, LogData.HOST_LENGTH).trim();
				
				LogData log = new LogData(user,pid,type,time,host);
				
				list.add(log);
				
				
			}
			
			IOUtil.saveCollection(list, textLogFile);
			
			IOUtil.saveLong(lastPosition, lastPositionFile);
			
			System.out.println("解析日志成功!");
			
			return true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("解析日志失败!");
			e.printStackTrace();
		}
		
		return false;		
	}
	
	public void start(){
		parseLogs();
	}
	
	public static void main(String[] args) {
		DMSClient client = new DMSClient();
		client.start();
	}

}

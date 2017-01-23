package dms_re;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class DMSServer {
	private ServerSocket server;
	private File serverLogFile;
	private ExecutorService threadPool;
	private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
	
	public DMSServer(){
		System.out.println("服务端正在初始化...");
		
		Map<String,String>  config = new HashMap<String,String>();
		try {
			config = loadConfig();
			
			init(config);
			System.out.println("服务端初始化完毕...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("初始化服务端失败!");
			e.printStackTrace();
		}
	}
	
	public void init(Map<String,String> config){
		try {
			this.server = new ServerSocket(Integer.parseInt(config.get("serverport")));
			this.serverLogFile = new File(config.get("logrecfile"));
			this.threadPool = Executors.newFixedThreadPool(
					Integer.parseInt(config.get("threadsum")));
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<String,String> loadConfig() throws Exception{
		Map<String,String> config = new  HashMap<String,String>();
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new File("server-config.xml"));
			Element root = doc.getRootElement();
			List<Element> list = root.elements();
			for(Element e : list){
				String key = e.getName();
				String value = e.getText().trim();
				config.put(key, value);
			}
			return config;
			
		} catch (Exception e) {
			System.out.println("解析配置文件异常!");
			e.printStackTrace();
			throw e;
		}		
		
	}
	
	public void start(){
		System.out.println("服务端开始工作...");
		SaveLogHandler handler = new SaveLogHandler();
		new Thread(handler).start();
		
		while(true){
			try {
				Socket socket = server.accept();
				ClientHandler clienthandler = new ClientHandler(socket);
				threadPool.execute(clienthandler);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	public class SaveLogHandler implements Runnable{
		PrintWriter pw = null;
		public void run() {
			try {
				pw  = new PrintWriter(new FileOutputStream(serverLogFile,true));
				String log;
				Iterator<String> it = messageQueue.iterator();
			while(it.hasNext()){
					log = it.next();
					pw.println(log);
				}
			pw.flush();
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				pw.close();
			}
			
		}
		
	}
	
	public class ClientHandler implements Runnable{
		Socket socket;
		public ClientHandler(Socket socket){
			this.socket = socket;
		}

		public void run() {
			PrintWriter pw=null;
			BufferedReader br=null;
			try {
				pw = new PrintWriter(socket.getOutputStream());
				br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				
				String line;
				while((line=br.readLine())!=null){
					if(!"OVER".equals(line)){
						messageQueue.offer(line);
					}else{
						break;
					}
				}
				System.out.println("消息读取完毕");
				
				pw.println("OK");
				
				pw.flush();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				pw.println("Error");
				e.printStackTrace();
			}finally{
				if(socket!=null){
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
		}
		
	}

	public static void main(String[] args) {
		try{
			DMSServer server = new DMSServer();
			server.start();
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("启动服务端失败!");
		}
		
		
	}

}

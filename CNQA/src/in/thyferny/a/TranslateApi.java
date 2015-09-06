package in.thyferny.a;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class TranslateApi {

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String dst = translate("百度翻译API测试");

	}

	public static String translate(String source) {
		String api_url;
		try {

			FileReader fr = new FileReader(new File("data/questions-train.txt"));
			BufferedReader reader = new BufferedReader(fr);
			String ll = "";
			while ((ll = reader.readLine()) != null) {
				String result = "";
				String str = ll.substring(2);
				api_url = new StringBuilder(
						"http://openapi.baidu.com/public/2.0/bmt/translate?client_id=OG5Bkftx5iVgTLSEvqu4TeOv&from=auto&to=auto&")
								.append("&q=" + str).toString();
				String urlNameString = api_url;
				URL realUrl = new URL(urlNameString);
				// 打开和URL之间的连接
				URLConnection connection = realUrl.openConnection();
				// 设置通用的请求属性
				connection.setRequestProperty("accept", "*/*");
				connection.setRequestProperty("connection", "Keep-Alive");
				connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
				// 建立实际的连接
				connection.connect();
				// 获取所有响应头字段
				Map<String, List<String>> map = connection.getHeaderFields();
				// 遍历所有的响应头字段
//				for (String key : map.keySet()) {
//					System.out.println(key + "--->" + map.get(key));
//				}
				// 定义 BufferedReader输入流来读取URL的响应
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = in.readLine()) != null) {
					result += line;
				}
//				System.out.println(result);
				Gson gson=new Gson();
				TranslateMode translateMode=gson.fromJson(result, TranslateMode.class);
				
				if(translateMode!=null&&translateMode.getTrans_result()!=null&&translateMode.getTrans_result().size()==1)
				{
					System.out.println(new String(translateMode.getTrans_result().get(0).getDst().getBytes(),"UTF8"));
				} 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
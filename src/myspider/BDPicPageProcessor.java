package myspider;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import utils.Utils;

/**
 * 百度图片爬虫
 * 
 */
public class BDPicPageProcessor implements PageProcessor {

	private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

	private TreeMap<String, String> treeMap = new TreeMap<String, String>();
	private File filePath;
	private String key;

	public BDPicPageProcessor(String key) {

		this.key = key;
		init();
	}

	private void init() {

		filePath = new File("pic/" + key);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}

		File[] files = filePath.listFiles();
		for (File file : files) {
			String md5 = Utils.getMd5ByFile(file);
			if (treeMap.get(md5) != null) {
				System.gc();
				System.out.println("删除重复文件	" + file.getName() + " " + file.delete());
			} else {
				treeMap.put(md5, "");
			}
		}
	}

	// 转码百度的objURL和fromURL
	private String transCoding(String string) {

		String result = "";

		TreeMap<String, String> treeMap = new TreeMap<String, String>();
		treeMap.put("w", "a");
		treeMap.put("k", "b");
		treeMap.put("v", "c");
		treeMap.put("1", "d");
		treeMap.put("j", "e");
		treeMap.put("u", "f");
		treeMap.put("2", "g");
		treeMap.put("i", "h");
		treeMap.put("t", "i");
		treeMap.put("3", "j");
		treeMap.put("h", "k");
		treeMap.put("s", "l");
		treeMap.put("4", "m");
		treeMap.put("g", "n");
		treeMap.put("5", "o");
		treeMap.put("r", "p");
		treeMap.put("q", "q");
		treeMap.put("6", "r");
		treeMap.put("f", "s");
		treeMap.put("p", "t");
		treeMap.put("7", "u");
		treeMap.put("e", "v");
		treeMap.put("o", "w");
		treeMap.put("8", "1");
		treeMap.put("d", "2");
		treeMap.put("n", "3");
		treeMap.put("9", "4");
		treeMap.put("c", "5");
		treeMap.put("m", "6");
		treeMap.put("0", "7");
		treeMap.put("b", "8");
		treeMap.put("l", "9");
		treeMap.put("a", "0");

		treeMap.put("_z2C$q", ":");
		treeMap.put("_z&e3B", ".");
		treeMap.put("AzdH3F", "/");

		string = string.replace("_z2C$q", ":").replaceAll("_z&e3B", ".").replaceAll("AzdH3F", "/");

		char[] cs = string.toCharArray();

		StringBuffer buffer = new StringBuffer();

		for (char c : cs) {
			String ts = treeMap.get(String.valueOf(c));
			if (ts != null) {
				buffer.append(ts);
			} else {
				buffer.append(String.valueOf(c));
			}

		}

		result = buffer.toString();

		System.out.println(result);
		return result;
	}

	private void downloadPicture(String surl) {
		URL url = null;

		try {
			url = new URL(surl);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setConnectTimeout(1000);
			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
			connection.setRequestProperty("Referer", "https://www.baidu.com");

			connection.connect();

			if (connection.getResponseCode() == 302) {
				String location = connection.getHeaderField("location");
				String cookie = connection.getHeaderField("Set-Cookie");

				System.out.println("跳转地址为: " + location);

				url = new URL(location);
				connection = (HttpURLConnection) url.openConnection();

				connection.setConnectTimeout(1000);
				connection.setRequestMethod("GET");
				connection.setInstanceFollowRedirects(false);
				connection.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
				connection.setRequestProperty("Referer", "https://www.baidu.com");
				connection.setRequestProperty("Cookie", cookie);

				connection.connect();

			}

			DataInputStream dataInputStream = new DataInputStream(url.openStream());
			String imageName = new SimpleDateFormat("HHmmssSS").format(new Date()) + ".jpg";

			File file = new File("pic/" + key + "/" + imageName.trim());

			FileOutputStream fileOutputStream = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = dataInputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, length);
			}
			dataInputStream.close();
			fileOutputStream.close();

			if (file.length() < 102400) {
				System.gc();
				file.delete();
				System.out.println("删掉过小文件");
			} else if (treeMap.get(Utils.getMd5ByFile(file)) != null) {
				System.gc();
				file.delete();
				System.out.println("删掉重复图片");
			} else {
				treeMap.put(Utils.getMd5ByFile(file), "");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Site getSite() {
		// TODO Auto-generated method stub
		return site;
	}

	@Override
	public void process(Page page) {

		JSONObject jsonObject = (JSONObject) JSONObject.parse(page.getRawText());
		JSONArray data = (JSONArray) jsonObject.get("data");
		for (int i = 0; i < data.size(); i++) {

			String url = transCoding((String) data.getJSONObject(i).get("objURL"));

			if (url != null) {
				downloadPicture(url);
			}
		}
	}

	public static void main(String[] args) throws Exception {

		String key = "动漫壁纸";
		String encodekey = URLEncoder.encode(key, "utf-8"); // 百度图片 关键词

		for (int i = 0; i < 999999; i++) {

			String url = "https://image.baidu.com/search/acjson?tn=resultjson_com&ipn=rj&ct=201326592&is=&fp=result&queryWord="
					+ encodekey + "&cl=2&lm=-1&ie=utf-8&oe=utf-8&adpicid=&st=-1&z=&ic=0&word=" + encodekey
					+ "&s=&se=&tab=&width=1920&height=1080&face=0&istype=2&qc=&nc=1&fr=&pn=" + i * 30
					+ "&rn=30&gsm=1e&1512303435036=";

			Spider.create(new BDPicPageProcessor(key)).addUrl(url).run();
			//Spider.create(new BDPicPageProcessor(key));
		}
	}
}

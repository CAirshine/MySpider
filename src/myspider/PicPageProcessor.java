package myspider;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
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

public class PicPageProcessor implements PageProcessor {

	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

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
		result = result.replace(":", "%3A").replaceAll("/", "%2F").replaceAll("=", "%3D");

		result = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1512322899066&di=ed65fb2a16ed5d89bf7464480085799c&imgtype=0&src=" + result;

		System.out.println(result);
		return result;
	}

	private void downloadPicture(String surl) {
		URL url = null;

		try {
			url = new URL(surl);
			DataInputStream dataInputStream = new DataInputStream(url.openStream());
			String imageName = new SimpleDateFormat("HHmmssSS").format(new Date()) + ".jpg";
			File file = new File("pic\\Test");
			if (!file.isDirectory()) {
				file.mkdirs();
			}
			FileOutputStream fileOutputStream = new FileOutputStream(new File("pic\\Test\\" + imageName.trim()));
			byte[] buffer = new byte[1024];
			int length;
			while ((length = dataInputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, length);
			}
			dataInputStream.close();
			fileOutputStream.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

		String key = URLEncoder.encode("樱花", "utf-8"); // 百度图片 关键词

		String url = "https://image.baidu.com/search/acjson?tn=resultjson_com&ipn=rj&ct=201326592&is=&fp=result&queryWord="
				+ key + "&cl=2&lm=-1&ie=utf-8&oe=utf-8&adpicid=&st=-1&z=&ic=0&word=" + key
				+ "&s=&se=&tab=&width=1920&height=1080&face=0&istype=2&qc=&nc=1&fr=&pn=30&rn=30&gsm=1e&1512303435036=";

		System.out.println(url);

		Spider.create(new PicPageProcessor()).addUrl(url).run();
	}
}

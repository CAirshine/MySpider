package myspider;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import utils.Utils;

/**
 * 
 * 需要借助Cookie，没调好
 * 
 * 爬娟娟壁纸网壁纸 http://www.jj20.com/ http://www.jj20.com/bz/nxxz/
 * 
 */
@SuppressWarnings("unused")
public class JJBZWPicPageProcesser implements PageProcessor {

	Site site = Site.me().setRetryTimes(3).setSleepTime(100);

	private String key;
	private TreeMap<String, String> map = new TreeMap<String, String>();
	private File dir;
	private String priex = "http://cj.jj20.com/d/down0.php?p=";

	public JJBZWPicPageProcesser(String key) {

		this.key = key;
		init();
	}

	private void init() {

		dir = new File("pic/" + key);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	@Override
	public Site getSite() {
		// TODO Auto-generated method stub
		return site;
	}

	@Override
	public void process(Page page) {

		Document document = page.getHtml().getDocument();

		Pattern pattern = Pattern.compile("(<script>var id='(\\S+)';</script>)");
		Matcher matcher = pattern.matcher(document.toString());
		if (matcher.find()) {
			String surl = matcher.group(2);
			System.out.println(priex + surl);
			downloadPicture(priex + surl);
		}

		Elements as = document.getElementsByAttribute("href");
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < as.size(); i++) {
			Element element = as.get(i);
			String surl = element.absUrl("href");
			list.add(surl);
		}

		// page.addTargetRequests(list);
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
			// connection.setRequestProperty("Referer", "");

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
				// connection.setRequestProperty("Referer", "");
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
			} else if (map.get(Utils.getMd5ByFile(file)) != null) {
				System.gc();
				file.delete();
				System.out.println("删掉重复图片");
			} else {
				map.put(Utils.getMd5ByFile(file), "");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		Spider.create(new JJBZWPicPageProcesser("娟娟壁纸")).addUrl("http://www.jj20.com/bz/nxxz/shxz").thread(1).run();
	}
}

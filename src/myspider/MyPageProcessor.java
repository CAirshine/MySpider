package myspider;

import java.io.BufferedWriter;
import java.util.TreeMap;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class MyPageProcessor implements PageProcessor {

	private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

	private TreeMap<String, String> map = new TreeMap<String, String>();
	private String url = "";
	private long num = 0L;
	
	private BufferedWriter Writer;
	
	@Override
	public void process(Page page) {

		url = page.getUrl().toString();
		if (map.get(url) == null) {
			
			map.put(url, "");
			
			page.putField("title", page.getHtml().xpath("//title/text()").toString());
			page.putField("content", page.getHtml().xpath("//div[@class=\"content-text\"]/text()").toString());

			if (page.getResultItems().get("content") == null) {
				System.out.println("[SKIP]	" + page.getUrl());
				page.setSkip(true);
			}
			
			System.out.println("------------------------------------" + num++ + "------------------------------------");
		} else {
			System.out.println("[INFO]	Ìø¹ýÖØ¸´");
		}

		page.addTargetRequests(page.getHtml().links().regex("(https://www.qiushibaike.com/article/\\w+)").all());
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {

		// while (true) {
		Spider.create(new MyPageProcessor()).addUrl("https://www.qiushibaike.com/article/118981526").thread(1).run();
		// }
	}
}
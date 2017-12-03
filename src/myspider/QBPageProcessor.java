package myspider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class QBPageProcessor implements PageProcessor {

	private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

	private TreeMap<String, String> map = new TreeMap<String, String>();
	private String url = "";
	private long num = 0L;

	private BufferedReader reader;
	private PrintWriter Writer;

	public QBPageProcessor() {

		try {
			reader = new BufferedReader(new FileReader(new File("QB.log")));
			Writer = new PrintWriter(new FileOutputStream(new File("QB.log"), true), true);

			String line = "";
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("url:")) {
					map.put(line.replace("url:", "").trim(), "");
				}
				if (line.startsWith("------")) {
					line = line.replaceAll("-", "");
					num = Long.parseLong(line);
				}
			}
			reader.close();
			System.out.println("[INFO]num从" + num + "开始");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(Page page) {

		url = page.getUrl().toString().trim();
		if (map.get(url) == null) {

			map.put(url, "");

			page.putField("title", page.getHtml().xpath("//title/text()").toString());
			page.putField("content", page.getHtml().xpath("//div[@class=\"content-text\"]/text()").toString());

			if (page.getResultItems().get("content") == null) {
				System.out.println("[SKIP]	" + page.getUrl());
				page.setSkip(true);
			} else {
				num++;
				System.out
						.println("------------------------------------" + num + "------------------------------------");

				Writer.println("------------------------------------" + num + "------------------------------------");
				Writer.println("url:	" + page.getUrl());
				Writer.println("title:	" + page.getHtml().xpath("//title/text()").toString());
				Writer.println(
						"content:	" + page.getHtml().xpath("//div[@class=\"content-text\"]/text()").toString());
			}

		} else {
			System.out.println("[INFO]	跳过重复内容");
		}

		page.addTargetRequests(page.getHtml().links().regex("(https://www.qiushibaike.com/article/\\w+)").all());
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {

		Spider.create(new QBPageProcessor()).addUrl("https://www.qiushibaike.com/text/").thread(1).run();
	}
}
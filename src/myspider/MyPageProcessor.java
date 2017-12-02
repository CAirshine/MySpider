package myspider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class MyPageProcessor implements PageProcessor {

	private Site site = Site.me().setRetryTimes(30).setSleepTime(100);

	@Override
	public void process(Page page) {
		
		page.addTargetRequests(page.getHtml().links().regex("(https://www.qiushibaike.com/\\w+)").all());
		
		page.putField("content", page.getHtml().xpath("//div[@class=\"content\"/span/]"));
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		Spider.create(new MyPageProcessor()).addUrl("https://www.qiushibaike.com/hot/").thread(5).run();
	}
}
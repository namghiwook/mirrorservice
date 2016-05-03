package com.namghiwook.mirror.dust;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import com.namghiwook.mirror.GeocodeService;

@Configurable
@Service
public class YellowDustService {

	Logger logger = Logger.getLogger(getClass());

	// ddd, airs
	public static HashMap<String, String> mDDDLabels;
	public static ArrayList<String> DDDs;
	protected static HashMap<Integer, Integer> steps;

	private static int loading_ddd_index = -1;
	
	static {
		mDDDLabels = new HashMap<String, String>();
		mDDDLabels.put("02", "서울");
		mDDDLabels.put("051", "부산");
		mDDDLabels.put("053", "대구");
		mDDDLabels.put("032", "인천");
		mDDDLabels.put("062", "광주");
		mDDDLabels.put("042", "대전");
		mDDDLabels.put("052", "울산");
		mDDDLabels.put("031", "경기");
		mDDDLabels.put("033", "강원");
		mDDDLabels.put("043", "충북");
		mDDDLabels.put("041", "충남");
		mDDDLabels.put("063", "전북");
		mDDDLabels.put("061", "전남");
		mDDDLabels.put("054", "경북");
		mDDDLabels.put("055", "경남");
		mDDDLabels.put("064", "제주");

		DDDs = new ArrayList<String>();
		DDDs.add("02");
		DDDs.add("051");
		DDDs.add("053");
		DDDs.add("032");
		DDDs.add("062");
		DDDs.add("042");
		DDDs.add("052");
		DDDs.add("031");
		DDDs.add("033");
		DDDs.add("043");
		DDDs.add("041");
		DDDs.add("063");
		DDDs.add("061");
		DDDs.add("054");
		DDDs.add("055");
		DDDs.add("064");

		steps = new HashMap<Integer, Integer>();
		steps.put(0, 0);
		steps.put(1, 50);
		steps.put(2, 100);
		steps.put(3, 150);
		steps.put(4, 250);
		steps.put(5, 350);
		steps.put(6, 500);
	}

	@Autowired
	private GeocodeService geocodeService;
	@Autowired
	private YellowDustRepository yellowDustRepository;
	
	private YellowDust findYellowDustByLabel(ArrayList<YellowDust> dusts, String label) {
		if (dusts == null) return null;
		for (YellowDust dust : dusts) {
			if (dust.label.equals(label)) return dust;
		}
		return null;
	}

	public void loadData() {
		
		ArrayList<YellowDust> dusts = (ArrayList<YellowDust>)yellowDustRepository.findAll();

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if (cal.get(Calendar.HOUR) == 0) {
			cal.add(Calendar.HOUR, -1);
		}
		Date now = cal.getTime();

		// http://www.airkorea.or.kr/pmRelaySub?strDateDiv=1&searchDate=2016-04-26&district=02&itemCode=10007&searchDate_f=201604
		
		String searchDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now);
		String searchDate_f = new SimpleDateFormat("yyyyMM", Locale.US).format(now);
		
		
		++loading_ddd_index;
		if (loading_ddd_index >= DDDs.size()) {
			loading_ddd_index = 0;
		}
		String district = DDDs.get(loading_ddd_index);
		
		Document doc = null;
		try {
			String url = "http://www.airkorea.or.kr/pmRelaySub?strDateDiv=1&searchDate=" + searchDate + "&district="
					+ district + "&itemCode=10007&searchDate_f=" + searchDate_f;
			logger.info("url " + url);
			doc = Jsoup.parse(executeRequest(url));
			// doc =
			// Jsoup.connect("http://www.airkorea.or.kr/pmRelaySub?strDateDiv=1&searchDate="
			// + searchDate + "&district=" + district +
			// "&itemCode=10007&searchDate_f=" + searchDate_f).get();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (doc == null) {
			logger.error("airkorea page not loaded");
			return;
		}
		
		Elements elements = doc.select("tbody > tr");
		if (elements == null || elements.size() == 0) {
			logger.error("airkorea page loaded but scheme changed");
			return;
		}
		logger.info("DDD " + district + " elements length " + elements.size());
		
		for (Element el : elements) {
			Elements children = el.children();
			if (children == null || children.size() < 26) {
				continue;
			}
			String group = children.get(1).text();
			int data = 0;
			Element child = null;
			for (int i = children.size() - 1; i > 1; i--) {
				child = children.get(i);
				if (!child.equals("-")) {
					try {
						data = Integer.parseInt(child.text());
					} catch (Exception e) {
					}
					if (data != 0)
						break;
				}
			}

			YellowDust yodust = findYellowDustByLabel(dusts, group);
			if (yodust != null) {
				yodust.density = data;
				yodust.lastUpdated = now;
			}
		}
		
		
		
		saveDusts(dusts);
		
		
//		ArrayList<String> groups = new ArrayList<String>();
		
//		String district = "02";
//		groups.add("[서울]강남구");
//		groups.add("[서울]강동구");
//		groups.add("[서울]강북구");
//		groups.add("[서울]강서구");
//		groups.add("[서울]관악구");
//		groups.add("[서울]광진구");
//		groups.add("[서울]구로구");
//		groups.add("[서울]금천구");
//		groups.add("[서울]노원구");
//		groups.add("[서울]도봉구");
//		groups.add("[서울]동대문구");
//		groups.add("[서울]동작구");
//		groups.add("[서울]마포구");
//		groups.add("[서울]서대문구");
//		groups.add("[서울]서초구");
//		groups.add("[서울]성동구");
//		groups.add("[서울]성북구");
//		groups.add("[서울]송파구");
//		groups.add("[서울]양천구");
//		groups.add("[서울]영등포구");
//		groups.add("[서울]용산구");
//		groups.add("[서울]은평구");
//		groups.add("[서울]종로구");
//		groups.add("[서울]중구");
//		groups.add("[서울]중랑구");
//		groups.add("[서울]강남대로");
//		groups.add("[서울]강변북로");
//		groups.add("[서울]공항대로");
//		groups.add("[서울]길동");
//		groups.add("[서울]내부순환로");
//		groups.add("[서울]도산대로");
//		groups.add("[서울]동작대로 중앙차로");
//		groups.add("[서울]신촌로");
//		groups.add("[서울]영등포로");
//		groups.add("[서울]종로");
//		groups.add("[서울]청계천");
//		groups.add("[서울]청량리");
//		groups.add("[서울]한강대로");
//		groups.add("[서울]화랑로");
				
		
//		String district = "051";
//		groups.add("[부산 강서구]녹산동");
//		groups.add("[부산 강서구]대저동");
//		groups.add("[부산 금정구]부곡동");
//		groups.add("[부산 금정구]청룡동");
//		groups.add("[부산 기장군]기장읍");
//		groups.add("[부산 기장군]용수리");
//		groups.add("[부산 남구]대연동");
//		groups.add("[부산 동구]수정동");
//		groups.add("[부산 동래구]명장동");
//		groups.add("[부산 부산진구]전포동");
//		groups.add("[부산 북구]덕천동");
//		groups.add("[부산 사상구]학장동");
//		groups.add("[부산 사하구]장림동");
//		groups.add("[부산 서구]대신동");
//		groups.add("[부산 수영구]광안동");
//		groups.add("[부산 연제구]연산동");
//		groups.add("[부산 영도구]태종대");
//		groups.add("[부산 중구]광복동");
//		groups.add("[부산 해운대구]좌동");
//		groups.add("[부산 동구]초량동");
//		groups.add("[부산 동래구]온천동");
			
//		String district = "053";
//		groups.add("[대구 남구]대명동");
//		groups.add("[대구 달서구]호림동");
//		groups.add("[대구 달성군]현풍면");
//		groups.add("[대구 동구]서호동");
//		groups.add("[대구 동구]신암동");
//		groups.add("[대구 북구]노원동");
//		groups.add("[대구 북구]태전동");
//		groups.add("[대구 서구]이현동");
//		groups.add("[대구 수성구]만촌동");
//		groups.add("[대구 수성구]지산동");
//		groups.add("[대구 중구]수창동");
//		groups.add("[대구 서구]평리동");
//		groups.add("[대구 중구]남산동");
		
		
//		String district = "032";
//		groups.add("[인천 옹진군]백령도");
//		groups.add("[인천 강화군]석모리");
//		groups.add("[인천 옹진군]덕적도");
//		groups.add("[인천 강화군]송해");
//		groups.add("[인천 계양구]계산");
//		groups.add("[인천 남구]숭의");
//		groups.add("[인천 남구]숭의");
//		groups.add("[인천 남동구]고잔");
//		groups.add("[인천 남동구]구월동");
//		groups.add("[인천 남동구]논현");
//		groups.add("[인천 동구]송림");
//		groups.add("[인천 부평구]부평");
//		groups.add("[인천 서구]검단");
//		groups.add("[인천 서구]석남");
//		groups.add("[인천 서구]연희");
//		groups.add("[인천 서구]원당");
//		groups.add("[인천 연수구]동춘");
//		groups.add("[인천 중구]신흥");
//		groups.add("[인천 중구]운서");
//		groups.add("[인천 남구]석바위");
//		groups.add("[인천 동구]송현");
//		groups.add("[인천 부평구]부평역");
	
//		String district = "062";
//		groups.add("[광주 광산구]송정1동");
//		groups.add("[광주 광산구]오선동");
//		groups.add("[광주 남구]주월동");
//		groups.add("[광주 동구]서석동");
//		groups.add("[광주 북구]건국동");
//		groups.add("[광주 북구]두암동");
//		groups.add("[광주 서구]농성동");
//		groups.add("[광주 북구]운암동");
//		groups.add("[광주 서구]치평동");
		
//		String district = "042";
//		groups.add("[대전 대덕구]문평동");
//		groups.add("[대전 대덕구]읍내동");
//		groups.add("[대전 동구]성남동1");
//		groups.add("[대전 서구]둔산동");
//		groups.add("[대전 서구]정림동");
//		groups.add("[대전 유성구]구성동");
//		groups.add("[대전 유성구]노은동");
//		groups.add("[대전 중구]문창동");
//		groups.add("[대전 서구]월평동");
//		groups.add("[대전 중구]대흥동1");
			
		
//		String district = "052";
//		groups.add("[울산 남구]무거동");
//		groups.add("[울산 남구]부곡동(울산)");
//		groups.add("[울산 남구]삼산동");
//		groups.add("[울산 남구]신정동");
//		groups.add("[울산 남구]야음동");
//		groups.add("[울산 남구]여천동(울산)");
//		groups.add("[울산 동구]대송동");
//		groups.add("[울산 북구]농소동");
//		groups.add("[울산 북구]효문동");
//		groups.add("[울산 울주군]덕신리");
//		groups.add("[울산 울주군]삼남면");
//		groups.add("[울산 울주군]상남리");
//		groups.add("[울산 울주군]화산리");
//		groups.add("[울산 중구]성남동");
//		groups.add("[울산 남구]신정2동");
			
//		String district = "031";
//		groups.add("[경기 이천시]설성면");
//		groups.add("[경기 포천시]관인면");
//		groups.add("[경기 가평군]가평읍");
//		groups.add("[경기 고양시]식사동");
//		groups.add("[경기 고양시]행신동");
//		groups.add("[경기 과천시]과천동");
//		groups.add("[경기 과천시]별양동");
//		groups.add("[경기 광명시]소하동");
//		groups.add("[경기 광명시]철산동");
//		groups.add("[경기 광주시]경안동");
//		groups.add("[경기 구리시]교문동");
//		groups.add("[경기 구리시]동구동");
//		groups.add("[경기 군포시]당동");
//		groups.add("[경기 군포시]산본동");
//		groups.add("[경기 김포시]고촌면");
//		groups.add("[경기 김포시]사우동");
//		groups.add("[경기 김포시]통진읍");
//		groups.add("[경기 남양주시]금곡동");
//		groups.add("[경기 남양주시]오남읍");
//		groups.add("[경기 동두천시]보산동");
//		groups.add("[경기 부천시]내동");
//		groups.add("[경기 부천시]소사본동");
//		groups.add("[경기 부천시]오정동");
//		groups.add("[경기 부천시]중2동");
//		groups.add("[경기 성남시]단대동");
//		groups.add("[경기 성남시]복정동");
//		groups.add("[경기 성남시]상대원1동");
//		groups.add("[경기 성남시]수내동");
//		groups.add("[경기 성남시]운중동");
//		groups.add("[경기 성남시]정자1동");
//		groups.add("[경기 수원시]고색동");
//		groups.add("[경기 수원시]광교동");
//		groups.add("[경기 수원시]신풍동");
//		groups.add("[경기 수원시]영통동");
//		groups.add("[경기 수원시]인계동");
//		groups.add("[경기 수원시]천천동");
//		groups.add("[경기 시흥시]대야동");
//		groups.add("[경기 시흥시]시화공단");
//		groups.add("[경기 시흥시]정왕동");
//		groups.add("[경기 안산시]고잔동");
//		groups.add("[경기 안산시]대부동");
//		groups.add("[경기 안산시]본오동");
//		groups.add("[경기 안산시]부곡동1");
//		groups.add("[경기 안산시]원곡동");
//		groups.add("[경기 안산시]원시동");
//		groups.add("[경기 안산시]호수동");
//		groups.add("[경기 안성시]봉산동");
//		groups.add("[경기 안양시]부림동");
//		groups.add("[경기 안양시]안양2동");
//		groups.add("[경기 안양시]안양6동");
//		groups.add("[경기 안양시]호계동");
//		groups.add("[경기 양주시]백석읍");
//		groups.add("[경기 양평군]양평");
//		groups.add("[경기 여주군]여주시");
//		groups.add("[경기 연천군]연천");
//		groups.add("[경기 오산시]오산동");
//		groups.add("[경기 용인시]기흥");
//		groups.add("[경기 용인시]김량장동");
//		groups.add("[경기 용인시]수지");
//		groups.add("[경기 의왕시]고천동");
//		groups.add("[경기 의왕시]부곡3동");
//		groups.add("[경기 의정부시]의정부1동");
//		groups.add("[경기 의정부시]의정부동");
//		groups.add("[경기 이천시]창전동");
//		groups.add("[경기 파주시]금촌동");
//		groups.add("[경기 파주시]운정");
//		groups.add("[경기 평택시]비전동");
//		groups.add("[경기 평택시]안중");
//		groups.add("[경기 평택시]평택항");
//		groups.add("[경기 포천시]선단동");
//		groups.add("[경기 하남시]신장동");
//		groups.add("[경기 화성시]남양동");
//		groups.add("[경기 화성시]동탄동");
//		groups.add("[경기 화성시]향남");
//		groups.add("[경기 고양시]마두역");
//		groups.add("[경기 부천시]계남공원");
//		groups.add("[경기 성남시]모란역");
//		groups.add("[경기 성남시]백현동");
//		groups.add("[경기 수원시]동수원");
//		groups.add("[경기 안산시]중앙로1");
//		groups.add("[경기 용인시]구갈동");
	
//		String district = "033";
//		groups.add("[강원 고성군]간성읍");
//		groups.add("[강원 양구군]방산면");
//		groups.add("[강원 정선군]북평면");
//		groups.add("[강원 횡성군]치악산");
//		groups.add("[강원 강릉시]옥천동");
//		groups.add("[강원 동해시]천곡동");
//		groups.add("[강원 삼척시]남양동1");
//		groups.add("[강원 원주시]명륜동");
//		groups.add("[강원 원주시]중앙동(원주)");
//		groups.add("[강원 춘천시]석사동");
//		groups.add("[강원 춘천시]중앙로");
	
		
//		String district = "043";
//		groups.add("[충북 괴산군]청천면");
//		groups.add("[충북 단양군]매포읍");
//		groups.add("[충북 제천시]장락동");
//		groups.add("[충북 청원군]오창읍");
//		groups.add("[충북 청주시]문화동");
//		groups.add("[충북 청주시]사천동");
//		groups.add("[충북 청주시]송정동(봉명동)");
//		groups.add("[충북 청주시]용암동");
//		groups.add("[충북 충주시]칠금동");
//		groups.add("[충북 충주시]호암동");
//		groups.add("[충북 청주시]복대동");
		
		
//		String district = "041";
//		groups.add("[충남 공주시]사곡면");
//		groups.add("[충남 태안군]파도리");
//		groups.add("[충남 당진군]난지도리");
//		groups.add("[충남 당진군]정곡리");
//		groups.add("[충남 서산시]독곶리");
//		groups.add("[충남 서산시]동문동");
//		groups.add("[충남 아산시]모종동");
//		groups.add("[충남 천안시]백석동");
//		groups.add("[충남 천안시]성황동");
//		groups.add("[충남 천안시]성성동");
		
		
//		String district = "063";
//		groups.add("[전북 임실군]운암면");
//		groups.add("[전북 고창군]고창읍");
//		groups.add("[전북 군산시]개정동");
//		groups.add("[전북 군산시]소룡동");
//		groups.add("[전북 군산시]신풍동2");
//		groups.add("[전북 김제시]요촌동");
//		groups.add("[전북 남원시]죽항동");
//		groups.add("[전북 부안군]부안군");
//		groups.add("[전북 익산시]남중동");
//		groups.add("[전북 익산시]모현동");
//		groups.add("[전북 익산시]팔봉동");
//		groups.add("[전북 전주시]삼천동");
//		groups.add("[전북 전주시]중앙동(전주)");
//		groups.add("[전북 전주시]팔복동");
//		groups.add("[전북 정읍시]연지동");
//		groups.add("[전북 전주시]금암동");
		
		
//		String district = "061";
//		groups.add("[전남 화순군]송단리");
//		groups.add("[전남 광양시]광양읍");
//		groups.add("[전남 광양시]중동");
//		groups.add("[전남 광양시]진상면");
//		groups.add("[전남 광양시]태인동");
//		groups.add("[전남 목포시]부흥동");
//		groups.add("[전남 목포시]용당동");
//		groups.add("[전남 순천시]순천만");
//		groups.add("[전남 순천시]연향동");
//		groups.add("[전남 순천시]장천동");
//		groups.add("[전남 순천시]호두리");
//		groups.add("[전남 여수시]광무동");
//		groups.add("[전남 여수시]덕충동");
//		groups.add("[전남 여수시]문수동");
//		groups.add("[전남 여수시]여천동");
//		groups.add("[전남 여수시]월내동");
//		groups.add("[전남 영암군]나불리");
	
		
//		String district = "054";
//		groups.add("[경북 울릉군]태하리");
//		groups.add("[경북 영덕군]지품면");
//		groups.add("[경북 영천시]화북면");
//		groups.add("[경북 의성군]안계면");
//		groups.add("[경북 경산시]중방동");
//		groups.add("[경북 경주시]성건동");
//		groups.add("[경북 구미시]4공단");
//		groups.add("[경북 구미시]공단동");
//		groups.add("[경북 구미시]원평동");
//		groups.add("[경북 구미시]형곡동");
//		groups.add("[경북 김천시]신음동");
//		groups.add("[경북 안동시]남문동");
//		groups.add("[경북 영주시]휴천동");
//		groups.add("[경북 포항시]3공단");
//		groups.add("[경북 포항시]대도동");
//		groups.add("[경북 포항시]대송면");
//		groups.add("[경북 포항시]장량동");
//		groups.add("[경북 포항시]장흥동");
		

//		String district = "055";
//		groups.add("[경남 거제시]저구리");
//		groups.add("[경남 거창군]남상면");
//		groups.add("[경남 창원시]대산면");
//		groups.add("[경남 거제시]아주동");
//		groups.add("[경남 김해시]동상동");
//		groups.add("[경남 김해시]삼방동");
//		groups.add("[경남 김해시]장유동");
//		groups.add("[경남 사천시]사천읍");
//		groups.add("[경남 양산시]북부동");
//		groups.add("[경남 양산시]웅상읍");
//		groups.add("[경남 진주시]대안동");
//		groups.add("[경남 진주시]상대동");
//		groups.add("[경남 진주시]상봉동");
//		groups.add("[경남 창원시]가음정동");
//		groups.add("[경남 창원시]경화동");
//		groups.add("[경남 창원시]명서동");
//		groups.add("[경남 창원시]봉암동");
//		groups.add("[경남 창원시]사파동");
//		groups.add("[경남 창원시]용지동");
//		groups.add("[경남 창원시]웅남동");
//		groups.add("[경남 창원시]회원동");
//		groups.add("[경남 하동군]하동읍");
//		groups.add("[경남 창원시]반송로");
	
	
//		String district = "064";
//		groups.add("[제주 제주시]고산리");
//		groups.add("[제주 서귀포시]동홍동");
//		groups.add("[제주 제주시]연동");
//		groups.add("[제주 제주시]이도동");
		
//		for (String group : groups) {
//			YellowDust air = new YellowDust();
//			air.code = group;
//			air.label = group;
//			air.density = 0;
//			air.ddd = district;
//			dusts.add(air);
//		}
		
		
//		saveDusts(setGeocode(dusts));
		
//		saveDusts(dusts);
		
//		logger.info("dusts loading complete");
		
		
		
		long count = yellowDustRepository.count();
		logger.info(String.format("yellowdust repository count ? %d", count));

		return;
	}
	
	private ArrayList<YellowDust> setGeocode(ArrayList<YellowDust> dusts) {
		for (YellowDust dust : dusts) {
			try {
				Double[] latlng = geocodeService.addr2geo(dust.label);
				dust.location = new GeoJsonPoint(latlng[0], latlng[1]);
				logger.info("dust " + dust.label + " " + ArrayUtils.toString(latlng));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dusts;
	}
	
	private ArrayList<YellowDust> saveDusts(ArrayList<YellowDust> dusts) {
		return (ArrayList<YellowDust>) yellowDustRepository.save(dusts);
	}

	protected static String executeRequest(String url) throws Exception {
		StringBuffer result = new StringBuffer();

		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
				System.out.println(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(), "utf-8"));
				String input;
				while ((input = bufferedReader.readLine()) != null) {
					result.append(input);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result.toString();
	}

}

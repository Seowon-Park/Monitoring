import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Monitor {
    private static final String PAGE_URL = "https://www.swingguitars.com/612/?&page=1&sort=recent";
    private static final int SECOND = 10;

    public static void main(String[] args) throws InterruptedException {//sleep에 대한 InterruptedException
        try {
            Map<String, Product> previousProducts = fetchProductMap();//맨처음 출력하는 상품

            System.out.println("▶ 초기 상품 목록:");
            previousProducts.values().forEach(System.out::println); // System.out::println ==value -> System.out.println(value) == System.out.println(value)

            while (true) {
                TimeUnit.SECONDS.sleep(SECOND);

                Map<String, Product> currentProducts = fetchProductMap(); //map형식으로 현재 상품 목록 받음

                Set<Product> added = new HashSet<>();
                for (String code : currentProducts.keySet()) {
                    if(!previousProducts.containsKey(code)) //code값 없으면 added셋에 추가
                        added.add(currentProducts.get(code));
                }

                Set<Product> soldOutNow = new HashSet<>();
                for (String code : currentProducts.keySet()) {
                    Product before = previousProducts.get(code);//이전, 현재
                    Product now = currentProducts.get(code);

                    if (before != null && !before.soldOut && now.soldOut) {//이전엔 soldOut아니었는데, 지금은 soldOut인 것
                        soldOutNow.add(now);
                    }
                }

                if (!added.isEmpty() || !soldOutNow.isEmpty()) {
                    notifyChanges(added, soldOutNow);
                } else {
                    System.out.println("10초 지남: 변경사항 없음");
                }
                previousProducts = currentProducts;
            }
        } catch(IOException e){
            System.err.println("-페이지 요청 실패: " + e.getMessage());
        }

    }

    private static Map<String, Product> fetchProductMap() throws IOException  {//외부에서 받아오는 거니까 IOExceptoin
        Document doc = Jsoup.connect(PAGE_URL)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get();
        Elements items = doc.select(".item-wrap");//Element의 리스트에 class="item-wrap"인 모든 요소 담는다.

        Map<String, Product> products = new HashMap<>();

        for (Element item : items) {
            Element img = item.selectFirst("img[data-prodcode]");
            if (img == null) continue; //null체크 때문에 있는 거임

            //고유번호
            String code = img.attr("data-prodcode");

            // 이름
            Element nameEl = item.selectFirst("h2");
            String name = nameEl != null ? nameEl.text().trim() : "이름없음";
            //null체크 nameEl.text().trim()=> nameEl.문자열만 빼오기.공백(줄바꿈)없애기

            // 가격
            Element priceEl = item.selectFirst("p.pay");
            String price = priceEl != null ? priceEl.text().trim() : "가격없음"; //null체크

            // 품절 유무
            boolean soldOut = !item.select(".prod_icon.sold_out").isEmpty();
            //sold_out.isEmpty()=> true면 솔드아웃태그 없다는 뜻. => not(!) 붙여야함

            products.put(code, new Product(code, name, price, soldOut));
        }

        return products;
    }

    public static void notifyChanges(Set<Product> added,Set<Product> soldOutNow)  {
        System.out.println("\n[" + new Date() + "]"+" 변경 사항 감지됨:");
        if (!added.isEmpty()) {
            System.out.println("추가된 상품:");
            added.forEach(System.out::println);
        }
        if (!soldOutNow.isEmpty()) {
            System.out.println("제거된 상품:");
            soldOutNow.forEach(System.out::println);
        }
    }
}
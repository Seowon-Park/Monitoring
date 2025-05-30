import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Monitor {
    private static final String PAGE_URL = "https://www.swingguitars.com/612/?&page=1&sort=recent";
    private static final int SECOND = 10;

    // 상품 목록 파싱
    private static List<Product> fetchProducts() {
        List<Product> products = new ArrayList<>(); //끝에 계속 추가 => ArrayList가 빠름.
        try {
            Document doc = Jsoup.connect(PAGE_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Elements items = doc.select(".shop-item");
            for (Element item : items) {
                String name = Optional.ofNullable(item.selectFirst("h2")).map(Element::text).orElse("");
                String price = Optional.ofNullable(item.selectFirst(".item-pay .pay")).map(Element::text).orElse("");
                boolean soldout = item.text().toUpperCase().contains("SOLDOUT");
                products.add(new Product(name, price, soldout));
            }
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
        }
        return products;
    }

    private static void notifyChanges(Set<Product> added, Set<Product> removed) {
        System.out.println("\n[변경 감지 시각: " + new Date() + "]"); //[변경 감지 시각: 0000]
        if (!added.isEmpty()) {//not 추가된거 없음
            System.out.println("🆕 새 상품이 추가되었습니다:");
            added.forEach(p -> System.out.println("- " + p)); //for (String p : added) 프린트 "- "+p.toString
        }
        if (!removed.isEmpty()) {//not 제거된거 없음
            System.out.println("❌ 상품이 제거되었습니다:");
            removed.forEach(p -> System.out.println("- " + p));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        //상품 받아옴
        List<Product> previousList = fetchProducts();
        //상품 목록 출력
        System.out.println("초기 상품 목록:");
        previousList.forEach(p -> System.out.println("- " + p));

        System.out.println("\n모니터링 시작...");

        while (true) {
            //10초 쉬고
            TimeUnit.SECONDS.sleep(SECOND);
            //다시 상품 받아옴
            List<Product> currentList = fetchProducts();
            if (currentList.isEmpty())  break;// 가져온 목록이 없다? => 오류. 10초 뒤에 다시 시작하자

            Set<Product> oldSet = new HashSet<>(previousList);//이전 상품 Set
            Set<Product> newSet = new HashSet<>(currentList);//최신 상품 Set

            Set<Product> added = new HashSet<>(currentList); //최신 상품 리스트를 HashSet으로 생성. 얘를 Set이라 퉁치겠다~
            added.removeAll(oldSet);//최신 상품 Set - 이전 상품 Set = 추가된 상품 Set

            Set<Product> removed = new HashSet<>(previousList); //이전 상품 리스트를 HashSet으로 생성. 얘를 Set이라 퉁치겠다~
            removed.removeAll(newSet);//이전 상품 Set - 최신 상품 Set = 품절된 상품 Set

            if (!added.isEmpty() || !removed.isEmpty()) {
                notifyChanges(added, removed);
                previousList = currentList;
            } else { //추가된 상품 Set 이랑 품절된 상품 Set 둘 다 비었다 => 추가, 품절 없다
                System.out.println("10초 지남: 변경사항 없음");
            }
        }
    }
}


// PS IdeaProjects\Monitoring> javac -encoding UTF-8 -cp jsoup-1.20.1.jar src/Monitor.java -d out
// PS IdeaProjects\Monitoring> java -cp "jsoup-1.20.1.jar;out" Monitor
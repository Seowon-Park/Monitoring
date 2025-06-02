import java.util.*;

public class Product {
    String code;
    String name;
    String price;
    boolean soldOut;

    Product(String code, String name, String price, boolean soldOut) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.soldOut = soldOut;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s", name, price, soldOut ? "품절" : "판매중<<<<<<<<<<<<<<<");
    }

    @Override
    public boolean equals(Object o) {//Object 들어오게 오버라이딩했으니까
        if (this == o) return true; //(진짜 똑같은 상품임)
        if (!(o instanceof Product)) return false; //프로덕트 자기(or자손)이 아닌 애 막아야하고
        Product product = (Product) o;// Product로 다시 돌려놔야지
        return Objects.equals(code, product.code); //한쪽이 null이어도 안전하게 계산 -> false반환
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
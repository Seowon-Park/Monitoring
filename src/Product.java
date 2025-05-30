import java.util.*;

public class Product{
    private String name;
    private String price;
    private boolean soldout;

    Product(String name, String price, boolean soldout){
        this.name = name;
        this.price = price;
        this.soldout = soldout;
    }

    @Override
    public boolean equals(Object o){ //객체 비교할때 쓸거임
        if(this==o) return true;
        if(!(o instanceof Product)) return false;
        Product p = (Product) o;
        return soldout == p.soldout &&
                name.equals(p.name) &&
                price.equals(p.price);
    }

    @Override
    public int hashCode(){ // 상태 비교를 위해서는 같은 객체인지 확실히 알아야함
        return Objects.hash(name, price, soldout);
    }
    //객체의 중요한 값(name, price, soldout)이 같으니까 같은 객체야~

    @Override
    public String toString() {
        return name + ": "+ price+"("+(soldout ? "품절" : "판매중")+")";
    }

}
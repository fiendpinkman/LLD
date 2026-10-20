import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PromotionDemo
 */
public class PromotionDemo {

    enum UserType { NORMAL, VIP }
    enum ProductCategory { ELECTRONICS, SHIRTS}

    record User(Integer id, UserType userType) {};

    record Product(Integer id, String name, ProductCategory productCategory, BigDecimal price, Boolean isClearance){};

    record CartItem(Product product, Integer quantity){};

    static class Cart {
        private final Integer id;
        private final User user;
        private final List<CartItem> cartItems = new ArrayList<>();

        Cart(Integer id, User user) {
            this.id = id;
            this.user = user;
        }

        void addItem(Product product, Integer quantity) {
            cartItems.add(new CartItem(product, quantity));
        }

        BigDecimal subTotal() {
            return cartItems.stream()
                            .map(cartItem -> cartItem.product.price.multiply(BigDecimal.valueOf(cartItem.quantity)))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    interface Promotion {
        boolean isApplicable(Cart cart);    
        BigDecimal calculateDiscount(Cart cart);
    }

    static class ElectronicDiscount implements Promotion {

        @Override
        public boolean isApplicable(PromotionDemo.Cart cart) {
            return cart.cartItems.stream().anyMatch(cartItem -> cartItem.product.productCategory == ProductCategory.ELECTRONICS);
        }

        @Override
        public BigDecimal calculateDiscount(PromotionDemo.Cart cart) {
            BigDecimal discountRate = new BigDecimal(0.10);
            return cart.cartItems.stream()
                .filter(cartItem -> cartItem.product.productCategory == ProductCategory.ELECTRONICS)
                .map(cartItem -> cartItem.product.price.multiply(BigDecimal.valueOf(cartItem.quantity).multiply(discountRate)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
    }

    static class PromotionEngine {
        private final List<Promotion> promotions;

        PromotionEngine(List<Promotion> promotions) {
            this.promotions = promotions;
        }

        void checkOut(Cart cart) {
            // cart -> caritems -> product -> check discount
            BigDecimal subTotal = cart.subTotal();
            BigDecimal totalDiscount = BigDecimal.ZERO;
            for (Promotion promotion : promotions) {
                boolean isApplicable = cart.cartItems.stream().anyMatch(cartItem -> promotion.isApplicable(cart));
                if (isApplicable){
                    totalDiscount = totalDiscount.add(promotion.calculateDiscount(cart));
                }
            }
            System.out.println("SubTotal: " + subTotal + " TotalDiscount: " + totalDiscount.setScale(2, RoundingMode.HALF_UP));
        }
    }

    public static void main(String[] args) {
        User user = new User(1, UserType.NORMAL);
        Product product = new Product(1, "Laptop", ProductCategory.ELECTRONICS, BigDecimal.valueOf(10_000), false);
        Cart cart = new Cart(1, user);
        cart.addItem(product, 1);
        PromotionEngine promotionEngine = new PromotionEngine(Arrays.asList(new ElectronicDiscount()));
        promotionEngine.checkOut(cart);
    }
    
    
}
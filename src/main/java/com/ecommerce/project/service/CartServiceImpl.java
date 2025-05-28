package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.security.jwt.AuthEntryPointJwt;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductsToCart(Long productId, Integer quantity) {



        Cart cart = createCart();


        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));


       CartItem cartItem = cartItemRepository.findCardItemByProductIdAndCartId(
                cart.getCartId(), productId);

       if(cartItem != null){
           throw new APIException("product "+ product.getProductName() + "already exists in cart");
       }
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + "is not avilable.");
        }
       if(product.getQuantity() < quantity){
           throw new APIException("please make an order of the " + product.getProductName()
           + "less than or equal to the quantity" + product.getQuantity()+".");
       }


        CartItem newCartItem = new CartItem();
       newCartItem.setProduct(product);
       newCartItem.setCart(cart);
       newCartItem.setQuantity(quantity);
       newCartItem.setDiscount(product.getDiscount());
       newCartItem.setProductPrice(product.getSpecialPrice());


       cartItemRepository.save(newCartItem);

       product.setQuantity(product.getQuantity());

       cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice()*quantity));

       cartRepository.save(cart);

        cart.getCartItems().add(newCartItem);


      CartDTO cartDto = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();
        List<ProductDTO> productDTOs = new ArrayList<>();

        for (CartItem item : cartItems) {
            ProductDTO dto = modelMapper.map(item.getProduct(), ProductDTO.class);
            dto.setQuantity(item.getQuantity());
            productDTOs.add(dto);
        }
      cartDto.setProducts(productDTOs);
        System.out.println(cartDto);
        return cartDto;

    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if(carts.size() == 0){
            throw new APIException("No cart exist");
        }
        List<CartDTO> cartDTOS = new ArrayList<>();

        for(int i=0; i<carts.size(); i++){
            Cart cart = carts.get(i);
            CartDTO dto = modelMapper.map(carts.get(i), CartDTO.class);
            List<CartItem> cartItems = cart.getCartItems();
            List<ProductDTO> productDTOS = new ArrayList<>();
            for(int j=0; j<cartItems.size(); j++){
                CartItem cartItem = cartItems.get(j);
                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity());
                productDTOS.add(productDTO);
            }
            dto.setProducts(productDTOS);
            cartDTOS.add(dto);
        }
        return cartDTOS;


    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId,cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart","cartId",cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        List<ProductDTO> productDTOS = new ArrayList<>();
        for(int j=0; j<cartItems.size(); j++){
            CartItem cartItem = cartItems.get(j);
            ProductDTO productDTO = modelMapper.map(cartItem.getProduct(),ProductDTO.class);
            productDTO.setQuantity(cartItem.getQuantity());
            productDTOS.add(productDTO);
        }
        cartDTO.setProducts(productDTOS);

        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("cart","cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + "is not avilable.");
        }
        if(product.getQuantity() < quantity){
            throw new APIException("please make an order of the " + product.getProductName()
                    + "less than or equal to the quantity" + product.getQuantity()+".");
        }
        CartItem cartItem = cartItemRepository.findCardItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null ){
            throw new APIException("Product "+ product.getProductName() +" not avialable in cart" );
        }

        int newQuantity = cartItem.getQuantity()+quantity;
        if(newQuantity < 0){
            throw new APIException("The resulting quantity cannot be negative");
        }
        if(newQuantity == 0){
            deleteProductFromCart(cartId,productId);
        }
        else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
            //CartItem updatedItem = cartItemRepository.save(cartItem);
        }
        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() == 0){
        cartItemRepository.deleteById(updatedItem.getCartItemId());
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        List<ProductDTO> productDTOS = new ArrayList<>();
        for(int j=0; j<cartItems.size(); j++){
            CartItem newcartItem = cartItems.get(j);
            ProductDTO productDTO = modelMapper.map(newcartItem.getProduct(),ProductDTO.class);
            productDTO.setQuantity(newcartItem.getQuantity());
            productDTOS.add(productDTO);
        }
        cartDTO.setProducts(productDTOS);

        return cartDTO;
    }


    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("cart","cartId",cartId));
        CartItem cartItem = cartItemRepository.findCardItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
            throw new ResourceNotFoundException("product","productId",productId);
        }
        cart.setTotalPrice(cart.getTotalPrice()-(cartItem.getProductPrice()*cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);

        return "product " + cartItem.getProduct().getProductName() + "removed from the cart!";
    }

    @Override
    public void updateProductInCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("cart","cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));
        CartItem cartItem = cartItemRepository.findCardItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
            throw new APIException("product "+ product.getProductName() + "not avilable in the cart!");
        }
        double cartPrice = cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItem= cartItemRepository.save(cartItem);
    }

    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }
        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart = cartRepository.save(cart);
        return newCart;
    }
}

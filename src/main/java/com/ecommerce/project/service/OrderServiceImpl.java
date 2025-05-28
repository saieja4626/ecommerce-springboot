package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderSerive{
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository ProductRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId,
                               String paymentMethod, String pgName, String pgPaymentId,
                               String pgStatus, String pgResponseMessage) {
        //get user cart
        Cart cart = cartRepository.findCartByEmail(emailId);
        if(cart == null){
            throw new ResourceNotFoundException("cart","email",emailId);
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address","addressId",addressId));

        //create a new order with payment info
        Order order = new Order();
        order.setOrderDate(LocalDate.now());
        order.setEmail(emailId);
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);

        payment.setOrder(order);
        Payment savedPayment = paymentRepository.save(payment);
        order.setPayment(savedPayment);

       Order savedOrder = orderRepository.save(order);

       //get items from the cart into the order items
        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems.isEmpty()){
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem cartItem : cartItems){
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }
    List<OrderItem>  savedOrderItems =  orderItemRepository.saveAll(orderItems);

        //update product stock
        List<CartItem> cartItemList = cart.getCartItems();
        for(int i=0; i<cartItemList.size(); i++){
            CartItem cartItem = cartItemList.get(i);
            int quantity = cartItem.getQuantity();
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - quantity);
            ProductRepository.save(product);
            //remove the item from cart because order is places already
            cartService.deleteProductFromCart(cart.getCartId(), cartItem.getProduct().getProductId());

        }

        //send back the order summary
        OrderDTO orderDTO = modelMapper.map(savedOrder,OrderDTO.class);
        for(int i=0; i<savedOrderItems.size(); i++){
            //we are converting order items to orderItemDto and adding to it for every item in list
            orderDTO.getOrderItems().add(modelMapper.map(savedOrderItems.get(i), OrderItemDTO.class));
        }
        orderDTO.setAddressId(addressId);

        return orderDTO;
    }
}

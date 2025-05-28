package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private CartRepository cartRepository;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProducts(ProductDTO productDTO, Long categoryId) {
       Category category = categoryRepository.findById(categoryId)
               .orElseThrow(()-> new ResourceNotFoundException("category","categoryId",categoryId));
       boolean isProductNotPresent = true;
       List<Product> products = category.getProducts();
       for(Product value : products){
           if(value.getProductName().equals(productDTO.getProductName())){
               isProductNotPresent = false;
               break;
           }
       }
       if(isProductNotPresent) {
           Product product = modelMapper.map(productDTO, Product.class);
           product.setCategory(category);
           product.setImage("default.png");
           double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
           product.setSpecialPrice(specialPrice);
           Product savedProduct = productRepository.save(product);

           return modelMapper.map(savedProduct, ProductDTO.class);
       }else{
           throw new APIException("product exist");
       }


    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);
         List<Product> product =  productPage.getContent();
         if(product.isEmpty()){
             throw new APIException("sorry,There are no products currently. Please come again.");
         }
         List<ProductDTO> productDTOS = new ArrayList<>();
         for(int i=0; i<product.size(); i++){
            ProductDTO dto = modelMapper.map(product.get(i), ProductDTO.class);
            productDTOS.add(dto);
         }
         ProductResponse productResponse = new ProductResponse();
         productResponse.setContent(productDTOS);
         productResponse.setPageNumber(productPage.getNumber());
         productResponse.setPageSize(productPage.getSize());
         productResponse.setTotalElements(productPage.getTotalElements());
         productResponse.setTotalPages(productPage.getTotalPages());
         productResponse.setLastPage(productPage.isLast());

         return productResponse;
    }
    @Override
    public ProductResponse searchByCategory(Long categoryId,Integer pageNumber, Integer pageSize,String sortBy, String sortOrder) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("category","categoryId",categoryId));
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);

         List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = new ArrayList<>();
        for(int i=0; i<products.size(); i++){
            ProductDTO dto = modelMapper.map(products.get(i), ProductDTO.class);
            productDTOS.add(dto);
        }
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;

    }

    @Override
    public ProductResponse searchBykeyword(String keyword,Integer pageNumber, Integer pageSize,String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage =  productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%',pageDetails);
        List<Product> products = productPage.getContent();

        if(products.isEmpty()){
            throw new APIException("sorry,There are no products currently. Please come again.");
        }
        List<ProductDTO> productDTOS = new ArrayList<>();
        for(int i=0; i<products.size(); i++){
            ProductDTO dto = modelMapper.map(products.get(i), ProductDTO.class);
            productDTOS.add(dto);
        }
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product updatedProduct = productRepository.findById(productId).
                orElseThrow(()-> new ResourceNotFoundException("product","productId",productId));

        Product product = modelMapper.map(productDTO, Product.class);
        updatedProduct.setProductName(product.getProductName());
        updatedProduct.setDescription(product.getDescription());
        updatedProduct.setQuantity(product.getQuantity());
        updatedProduct.setPrice(product.getPrice());
        updatedProduct.setDiscount(product.getDiscount());
        double specialPrice = product.getPrice()-((product.getDiscount()*0.01)*product.getPrice());
        updatedProduct.setSpecialPrice(specialPrice);

        Product savedProduct =  productRepository.save(updatedProduct);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        List<ProductDTO> productDTOS = new ArrayList<>();
       List<CartDTO> cartDTOS = new ArrayList<>();
        for(int i =0; i<carts.size(); i++){
            Cart cart = carts.get(i);
            CartDTO  cartDTO = modelMapper.map(cart,CartDTO.class);
            List<CartItem> carts1 = cart.getCartItems();
            for(int j=0; j<carts1.size(); j++){
                CartItem cartItem = carts1.get(i);
                ProductDTO dto = modelMapper.map(cartItem, ProductDTO.class);
                dto.setQuantity(cartItem.getQuantity());
                productDTOS.add(dto);
            }
            cartDTO.setProducts(productDTOS);
            cartDTOS.add(cartDTO);
        }
        for(int i=0; i<cartDTOS.size(); i++){
            Cart cart = carts.get(i);
           cartService.updateProductInCart(cart.getCartId(),productId);
        }



        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).
                orElseThrow(()-> new ResourceNotFoundException("product","productId",productId));
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        for(int i=0; i<carts.size(); i++){
            Cart cart = carts.get(i);
            cartService.deleteProductFromCart(cart.getCartId(),productId);
        }
        //productRepository.delete(product);
        //return modelMapper.map(product,ProductDTO.class);
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        productRepository.delete(product);
        return dto;

    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("product","productId",productId));
        //upload image to server and get the filename
        String fileName = fileService.uploadImage(path,image);

        product.setImage(fileName);

      Product updaedProduct =  productRepository.save(product);

      return modelMapper.map(updaedProduct, ProductDTO.class);
    }



}

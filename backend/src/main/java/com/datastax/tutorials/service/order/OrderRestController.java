package com.datastax.tutorials.service.order;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.tutorials.service.cartproducts.CartProductsRepository;
import com.datastax.tutorials.service.user.Address;
import com.datastax.tutorials.service.user.AddressEntity;
import com.datastax.tutorials.service.usercarts.UserCartsPrimaryKey;
import com.datastax.tutorials.service.usercarts.UserCartsRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@CrossOrigin(
  methods = {POST, GET, OPTIONS, PUT, DELETE, PATCH},
  maxAge = 3600,
  allowedHeaders = {"x-requested-with", "origin", "content-type", "accept"},
  origins = "*" 
)
@RequestMapping("/api/v1/order/")
@Tag(name = "Order Service", description="Provide crud operations for Order data")
public class OrderRestController {
	private OrderRepository orderRepo;
	private OrderByUserRepository orderUserRepo;
	private OrderStatusHistoryRepository orderStatusRepo;
	private UserCartsRepository userCartRepo;
	private CartProductsRepository cartProductsRepo;
	
	private PulsarClient client;
	private Producer<byte[]> orderProducer;
	

	private static final BigDecimal SHIPPING_HANDLING = new BigDecimal(4.00);
	

	private static final BigDecimal TAX_RATE = new BigDecimal(0.05);
	

	private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
	

	private static final OrderStatusEnum NEW_ORDER_STATUS = OrderStatusEnum.PENDING;
	
	private static final String SERVICE_URL = System.getenv("ASTRA_STREAM_URL");
	private static final String YOUR_PULSAR_TOKEN = System.getenv("ASTRA_STREAM_TOKEN");
	private static final String STREAMING_TENANT = System.getenv("ASTRA_STREAM_TENANT");
	private static final String STREAMING_PREFIX = STREAMING_TENANT + "/default/";
	private static final String PENDING_ORDER_TOPIC = "persistent://" + STREAMING_PREFIX + "pending-orders";
	
	public OrderRestController(OrderRepository oRepo,OrderByUserRepository oURepo,
			OrderStatusHistoryRepository oSHRepo,UserCartsRepository uCRepo,
			CartProductsRepository cPRepo) {

		orderRepo = oRepo;
		orderUserRepo = oURepo;
		orderStatusRepo = oSHRepo;
		userCartRepo = uCRepo;
		cartProductsRepo = cPRepo;
        
		try {
			client = PulsarClient.builder()
			        .serviceUrl(SERVICE_URL)
			        .authentication(
			            AuthenticationFactory.token(YOUR_PULSAR_TOKEN)
			        )
			        .build();
		} catch (PulsarClientException e) {
			e.printStackTrace();
		}

        try {
			orderProducer = client.newProducer()
			        .topic(PENDING_ORDER_TOPIC)
			        .create();
		} catch (PulsarClientException e) {
			e.printStackTrace();
		}
	}

    @GetMapping("/{orderid}/")
    @Operation(
     summary = "Retrieve data for a specifc order by a orderid",
     description= "Find **orderss list** for a user by its id `SELECT * FROM order WHERE orderid =?`",
     responses = {
       @ApiResponse(
         responseCode = "200",
         description = "Detail for order by orderid",
         content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = Order.class, name = "Order")
         )
       ),
       @ApiResponse(
         responseCode = "404", 
         description = "orderId not found",
         content = @Content(mediaType = "")),
       @ApiResponse(
         responseCode = "400",
         description = "Invalid parameter check orderId format."),
       @ApiResponse(
         responseCode = "500",
         description = "Internal error.") 
    })
    public ResponseEntity<OrderResponse> findOrderById(
            HttpServletRequest req, 
            @PathVariable(value = "orderid")
            @Parameter(name = "orderid", description = "order identifier (UUID)", example = "5929e846-53e8-173e-8525-80b666c46a83")
            UUID orderid) {
    	
    	List<OrderEntity> entityList = orderRepo.findByKeyOrderId(orderid);

    	if (entityList.size() > 0) {

	    	OrderResponse order = new OrderResponse();
	

	    	OrderEntity firstResult = entityList.get(0);
	    	OrderPrimaryKey key = firstResult.getKey();
	    	order.setOrderId(key.getOrderId());

	    	order.setOrderStatus(firstResult.getOrderStatus());

	    	order.setOrderTimestamp(new Date(getTimeFromUUID(key.getOrderId())));
	    	order.setOrderSubtotal(firstResult.getOrderSubtotal());
	    	order.setOrderShippingHandling(firstResult.getOrderShippingHandling());
	    	order.setOrderTax(firstResult.getOrderTax());
	    	order.setOrderTotal(firstResult.getOrderTotal());
	    	order.setPaymentMethod(firstResult.getPaymentMethod());
	    	order.setShippingAddress(mapAddress(firstResult.getShippingAddress()));    	
	

	    	List<OrderProduct> productList = new ArrayList<OrderProduct>();
	    	
	    	for (OrderEntity orderLine : entityList) {
		    	OrderProduct prod = new OrderProduct();
		    	prod.setProductName(key.getProductName());
		    	prod.setProductId(key.getProductId());
		    	prod.setProductQty(orderLine.getProductQty());
		    	prod.setProductPrice(orderLine.getProductPrice());
		    	
		    	productList.add(prod);
	    	}
	    	
	    	order.setProductList(productList);
	    	
	    	return ResponseEntity.ok(order);
    	} else {
    		return ResponseEntity.notFound().build();
    	}
    }
    
    @GetMapping("/user/{userid}/")
    @Operation(
     summary = "Retrieve all orders by a userid",
     description= "Find **orders list** for a userid `SELECT * FROM order_by_user WHERE userid =?`",
     responses = {
       @ApiResponse(
         responseCode = "200",
         description = "A list of orders for the user",
         content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = Order.class, name = "Order")
         )
       ),
       @ApiResponse(
         responseCode = "404", 
         description = "userId not found",
         content = @Content(mediaType = "")),
       @ApiResponse(
         responseCode = "400",
         description = "Invalid parameter check userId format."),
       @ApiResponse(
         responseCode = "500",
         description = "Internal error.")
    })
    public ResponseEntity<Stream<List<OrderByUser>>> findOrdersByUserId(
            HttpServletRequest req, 
            @PathVariable(value = "userid")
            @Parameter(name = "userid", description = "user identifier (UUID)", example = "5929e846-53e8-473e-8525-80b666c46a83")
            UUID userid) {
    	
    	Optional<List<OrderByUserEntity>> e = orderUserRepo.findByKeyUserId(userid);

    	return ResponseEntity.ok(e.stream().map(this::mapOrderByUser));
    }

    @PostMapping("/user/{userid}/")
    @Operation(
     summary = "Place an order",
     description= "Create an order for the user id `INSERT INTO order WHERE orderid=?`",
     responses = {
       @ApiResponse(
         responseCode = "200",
         description = "Place an order for the user",
         content = @Content(
           mediaType = "application/json")
       ),
       @ApiResponse(
         responseCode = "404", 
         description = "An error occured",
         content = @Content(mediaType = "")),
       @ApiResponse(
         responseCode = "400",
         description = "Invalid parameter."),
       @ApiResponse(
         responseCode = "500",
         description = "Internal error.")
    })
    @Transactional
    public ResponseEntity<OrderRequest> placeOrder(
            HttpServletRequest req,
            @RequestBody OrderRequest order,
            @PathVariable(value = "userid")
            @Parameter(name = "userid", description = "user identifier (UUID)", example = "5929e846-53e8-473e-8525-80b666c46a83")
            UUID userid) {
    	
    	List<OrderProduct> products = order.getProductList();
    	if (products.size() < 1) {
    		return ResponseEntity.notFound().build();
    	}
    	
    	UUID cartid = order.getCartId();
    	UUID orderid = Uuids.timeBased();
    	long timestamp = getTimeFromUUID(orderid);
    	Date orderTimeStamp = new Date(timestamp);

    	BigDecimal running_total = BigDecimal.ZERO;
    	for (OrderProduct prod : products) {
    		BigDecimal linePrice = prod.getProductPrice()
    				.multiply(new BigDecimal(prod.getProductQty()));
    		running_total = running_total.add(linePrice);
    	}
    	order.setOrderSubtotal(running_total);
    	
    	order.setOrderShippingHandling(SHIPPING_HANDLING);
    	
    	BigDecimal tax = running_total
    			.multiply(TAX_RATE)
    			.setScale(2, RoundingMode.HALF_EVEN);
    	order.setOrderTax(tax);
    	
    	BigDecimal total = running_total
    			.add(SHIPPING_HANDLING)
    			.add(tax);
    	order.setOrderTotal(total);
    	
    	OrderEntity orderE = new OrderEntity();
    	OrderPrimaryKey oKey = new OrderPrimaryKey();
    	oKey.setOrderId(orderid);

    	orderE.setOrderSubtotal(order.getOrderSubtotal());
    	orderE.setOrderShippingHandling(order.getOrderShippingHandling());
    	orderE.setOrderTax(order.getOrderTax());
    	orderE.setOrderTotal(order.getOrderTotal());
    	orderE.setPaymentMethod(order.getPaymentMethod());
    	orderE.setShippingAddress(mapAddressEntity(order.getShippingAddress()));
    	
    	for (OrderProduct product : order.getProductList()) {
    		oKey.setProductName(product.getProductName());
    		oKey.setProductId(product.getProductId());
        	orderE.setKey(oKey);

    		orderE.setProductQty(product.getProductQty());
    		orderE.setProductPrice(product.getProductPrice());
    		
        	orderRepo.save(orderE);
    	}
    	
    	OrderByUserEntity orderByUserE = new OrderByUserEntity();
    	OrderByUserPrimaryKey oUKey = new OrderByUserPrimaryKey();
    	oUKey.setOrderId(orderid);
    	oUKey.setUserId(userid);
    	orderByUserE.setKey(oUKey);
    	orderByUserE.setOrderStatus(NEW_ORDER_STATUS.name());
    	orderByUserE.setOrderTotal(order.getOrderTotal());

    	orderUserRepo.save(orderByUserE);

    	OrderStatusHistoryEntity orderStatus = new OrderStatusHistoryEntity();
    	OrderStatusHistoryPrimaryKey orderStatusKey = new OrderStatusHistoryPrimaryKey();
    	orderStatusKey.setOrderId(orderid);
    	orderStatusKey.setStatusTimestamp(orderTimeStamp);
    	orderStatus.setKey(orderStatusKey);
    	orderStatus.setOrderStatus(NEW_ORDER_STATUS.name());
    	
    	orderStatusRepo.save(orderStatus);
    	
    	UserCartsPrimaryKey cartKey = new UserCartsPrimaryKey();
    	cartKey.setUserId(userid);
    	cartKey.setCartId(cartid);
    	cartKey.setCartName(order.getCartName());
    	
    	userCartRepo.deleteById(cartKey);
    	
    	cartProductsRepo.deleteByKeyCartId(cartid);
    	
    	order.setOrderId(orderid);
    	order.setUserId(userid);
    	order.setOrderTimestamp(orderTimeStamp);
    	

    	String orderJSON = new Gson().toJson(order);
		try {
			sendToOrderStream(orderJSON);
		} catch (Exception e) {
			try {

			} catch (Exception e2) {
				return ResponseEntity.internalServerError().build();
			}
		}
    	
    	return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderid}/status/history")
    @Operation(
     summary = "Retrieve the status history for an orderid",
     description= "Find **orders list** for a user by its id `SELECT * FROM order_status_history WHERE orderid =?`",
     responses = {
       @ApiResponse(
         responseCode = "200",
         description = "A list of status history for the order",
         content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = OrderStatusHistory.class, name = "OrderStatusHistory")
         )
       ),
       @ApiResponse(
         responseCode = "404", 
         description = "orderId not found",
         content = @Content(mediaType = "")),
       @ApiResponse(
         responseCode = "400",
         description = "Invalid parameter check orderId format."),
       @ApiResponse(
         responseCode = "500",
         description = "Internal error.")
    })
    public ResponseEntity<Stream<OrderStatusHistory>> findOrderStatusHistory(
            @PathVariable(value = "orderid")
            @Parameter(name = "orderid", description = "order identifier (UUID)", example = "5929e846-53e8-473e-8525-80b666c46a83")
            UUID orderid) {
    	Optional<OrderStatusHistoryEntity> e = orderStatusRepo.findByKeyOrderId(orderid);

    	return ResponseEntity.ok(e.stream().map(this::mapOrderStatusHistory));
    }

    @PutMapping("/{orderid}/user/{userid}/cancel")
    @Operation(
     summary = "Cancel an order",
     description= "Update all statuses for the order to CANCELLED, to prevent it from progressing",
     responses = {
       @ApiResponse(
         responseCode = "200",
         description = "Cancel an order for the user",
         content = @Content(
           mediaType = "application/json")
       ),
       @ApiResponse(
         responseCode = "404", 
         description = "An error occured",
         content = @Content(mediaType = "")),
       @ApiResponse(
         responseCode = "400",
         description = "Invalid parameter."),
       @ApiResponse(
         responseCode = "500",
         description = "Internal error.")
    })
    @Transactional
    public ResponseEntity<OrderStatusHistory> cancelOrder(
            @PathVariable(value = "orderid")
            @Parameter(name = "orderid", description = "order identifier (UUID)", example = "5929e846-53e8-173e-8525-80b666c46a83")
            UUID orderid,
    		@PathVariable(value = "userid")
            @Parameter(name = "userid", description = "user identifier (UUID)", example = "5929e846-53e8-473e-8525-80b666c46a83")
            UUID userid) {
    	
    	final OrderStatusEnum CANCELLED_ORDER_STATUS = OrderStatusEnum.CANCELLED;

    	OrderByUserPrimaryKey orderBUKey = new OrderByUserPrimaryKey();
    	orderBUKey.setUserId(userid);
    	orderBUKey.setOrderId(orderid);
    	Optional<OrderByUserEntity> orderByUserO = orderUserRepo.findById(orderBUKey);
    	
    	if (!orderByUserO.isPresent()) {
    		return ResponseEntity.notFound().build();
    	} else {
    		OrderByUserEntity orderByUserE = orderByUserO.get();
	    	OrderStatusEnum currentStatus = computeOrderStatus(orderByUserE.getOrderStatus());
    		
	    	if (currentStatus.getStatusOrdinal() < OrderStatusEnum.SHIPPED.getStatusOrdinal()) {
	    		OrderEntity orderE = orderRepo.findByKeyOrderId(orderid).get(0);

	    		orderE.setOrderStatus(CANCELLED_ORDER_STATUS.name());
		    	orderByUserE.setOrderStatus(CANCELLED_ORDER_STATUS.name());
		    	OrderStatusHistoryEntity orderStatusE = new OrderStatusHistoryEntity();
		    	OrderStatusHistoryPrimaryKey orderStatusKey = new OrderStatusHistoryPrimaryKey();
		    	orderStatusKey.setOrderId(orderid);
		    	orderStatusKey.setStatusTimestamp(new Date());
		    	orderStatusE.setKey(orderStatusKey);
		    	orderStatusE.setOrderStatus(CANCELLED_ORDER_STATUS.name());
		    	
		    	orderRepo.save(orderE);
		    	orderUserRepo.save(orderByUserE);
		    	orderStatusRepo.save(orderStatusE);

		    	return ResponseEntity.ok(mapOrderStatusHistory(orderStatusE));
	    	} else {
	    		return ResponseEntity.badRequest().build();
	    	}
    	}
    }
    
    private void sendToOrderStream(String message) throws Exception {
        orderProducer.send(message.getBytes());
    }
 
    private List<OrderByUser> mapOrderByUser(List<OrderByUserEntity> entityList) {
    	List<OrderByUser> returnVal = new ArrayList<OrderByUser>();
    	
    	for (OrderByUserEntity order : entityList) {
    		
	    	OrderByUser orderByUser = new OrderByUser();
	    	
	    	OrderByUserPrimaryKey key = order.getKey();
	    	orderByUser.setOrderId(key.getOrderId());
	    	orderByUser.setUserId(key.getUserId());

	    	orderByUser.setOrderStatus(order.getOrderStatus());

	    	orderByUser.setOrderTimestamp(new Date(getTimeFromUUID(key.getOrderId())));
	    	orderByUser.setOrderTotal(order.getOrderTotal());
	    	
	    	returnVal.add(orderByUser);
    	}    	
    	return returnVal;
    }
    
    private OrderStatusHistory mapOrderStatusHistory(OrderStatusHistoryEntity entity) {
    	OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
    	OrderStatusHistoryPrimaryKey key = entity.getKey();
    	orderStatusHistory.setOrderId(key.getOrderId());
    	orderStatusHistory.setStatusTimestamp(key.getStatusTimestamp());
    	orderStatusHistory.setOrderStatus(entity.getOrderStatus());

    	return orderStatusHistory;
    }

    private Address mapAddress(AddressEntity ae) {

		Address a = new Address();
		
		a.setType(ae.getType());
		a.setMailtoName(ae.getMailtoName());
		a.setStreet(ae.getStreet());
		a.setStreet2(ae.getStreet2());
		a.setCity(ae.getCity());
		a.setStateProvince(ae.getStateProvince());
		a.setPostalCode(ae.getPostalCode());
		a.setCountry(ae.getCountry());
		
    	return a;
    }

    private AddressEntity mapAddressEntity(Address a) {

		AddressEntity ae = new AddressEntity();
		
		ae.setType(a.getType());
		ae.setMailtoName(a.getMailtoName());
		ae.setStreet(a.getStreet());
		ae.setStreet2(a.getStreet2());
		ae.setCity(a.getCity());
		ae.setStateProvince(a.getStateProvince());
		ae.setPostalCode(a.getPostalCode());
		ae.setCountry(a.getCountry());
		
		return ae;
    }
    
    private OrderStatusEnum computeOrderStatus(String status) {

    	
    	switch (status) {
    		case "PENDING":
    			return OrderStatusEnum.PENDING;
    		case "PICKED":
    			return OrderStatusEnum.PICKED;
    		case "SHIPPED":
    			return OrderStatusEnum.SHIPPED;
    		case "COMPLETE":
    			return OrderStatusEnum.COMPLETE;
    		case "CANCELLED":
    			return OrderStatusEnum.CANCELLED;
    		case "ERROR":
    			return OrderStatusEnum.ERROR;
    	}

    	return OrderStatusEnum.ERROR;
    }
    
    private static long getTimeFromUUID(UUID uuid) {
      return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
    }

    protected void finalize() throws PulsarClientException {

        orderProducer.close();
        client.close();
    }
}
